package br.edu.utfpr.td.tsi.robo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.openqa.selenium.WebDriver;

import br.edu.utfpr.td.tsi.modelo.Medico;
import br.edu.utfpr.td.tsi.paginas.PaginaCadastroMedico;
import br.edu.utfpr.td.tsi.paginas.PaginaHabilitarCadastro;
import br.edu.utfpr.td.tsi.paginas.PaginaListagemMedicos;
import br.edu.utfpr.td.tsi.paginas.PaginaLogin;
import br.edu.utfpr.td.tsi.paginas.PaginaPrincipal;
import br.edu.utfpr.td.tsi.relatorio.Relatorio;
import br.edu.utfpr.td.tsi.relatorio.Relatorio.Etapa;

/**
 * Orquestra o processo, sempre pela interface web (como um usuário faria):
 * <ol>
 *   <li>Habilitação: admin faz login e habilita o CPF de cada médico.</li>
 *   <li>Cadastro: preenche o formulário público de cadastro para cada médico.</li>
 *   <li>Verificação: confere na listagem se todos os médicos aparecem; recadastra os que faltarem.</li>
 *   <li>Remoção: cada médico faz login e remove o próprio registro na listagem.</li>
 * </ol>
 * Cada operação tem novas tentativas em caso de falha, e é idempotente
 * (CPF já habilitado / médico já cadastrado não geram duplicidade).
 */
public class RoboClinicaMedica {

    private static final int TENTATIVAS = 3;

    private final WebDriver driver;
    private final String usuarioAdmin;
    private final String senhaAdmin;
    private final Relatorio relatorio;

    private final PaginaLogin login;
    private final PaginaHabilitarCadastro habilitar;
    private final PaginaCadastroMedico cadastro;
    private final PaginaListagemMedicos listagem;
    private final PaginaPrincipal principal;

    public RoboClinicaMedica(WebDriver driver, String urlBase, String usuarioAdmin, String senhaAdmin,
            Relatorio relatorio) {
        this.driver = driver;
        this.usuarioAdmin = usuarioAdmin;
        this.senhaAdmin = senhaAdmin;
        this.relatorio = relatorio;
        this.login = new PaginaLogin(driver, urlBase);
        this.habilitar = new PaginaHabilitarCadastro(driver, urlBase);
        this.cadastro = new PaginaCadastroMedico(driver, urlBase);
        this.listagem = new PaginaListagemMedicos(driver, urlBase);
        this.principal = new PaginaPrincipal(driver, urlBase);
    }

    public void executar(List<Medico> medicos, Modo modo) {
        if (modo == Modo.CADASTRAR || modo == Modo.COMPLETO) {
            habilitarCpfs(medicos);
            cadastrarMedicos(medicos);
            verificarCadastros(medicos);
        }
        if (modo == Modo.REMOVER || modo == Modo.COMPLETO) {
            removerMedicos(medicos);
        }
    }

    // ------------------------------------------------------------------ 1. Habilitação

    void habilitarCpfs(List<Medico> medicos) {
        titulo("ETAPA 1 - Habilitação dos CPFs (administrador)");
        encerrarSessao();
        if (!login.abrir().entrar(usuarioAdmin, senhaAdmin)) {
            throw new IllegalStateException("Login do administrador recusado. Verifique usuário/senha.");
        }

        habilitar.abrir();
        Set<String> habilitados = habilitar.cpfsHabilitados();
        System.out.println("CPFs já habilitados no sistema: " + habilitados.size());

        int i = 0;
        for (Medico m : medicos) {
            i++;
            if (habilitados.contains(m.getCpf())) {
                registrar(i, medicos.size(), m, Etapa.HABILITACAO, "JA_HABILITADO", null);
                continue;
            }
            boolean[] primeiraTentativa = {true};
            Boolean ok = comTentativas("habilitar " + m.getCpf(), () -> {
                // Após cada envio a aplicação já volta para esta tela, então na 1ª tentativa basta enviar.
                // Numa nova tentativa, recarrega e confere antes: a aplicação aceita CPF repetido
                // e um envio que falhou "pela metade" poderia gerar duplicidade.
                if (!primeiraTentativa[0]) {
                    habilitar.abrir();
                    if (habilitar.cpfsHabilitados().contains(m.getCpf())) {
                        return true;
                    }
                }
                primeiraTentativa[0] = false;
                habilitar.habilitar(m.getCpf());
                return true;
            }, false);
            if (ok) {
                habilitados.add(m.getCpf());
            }
            registrar(i, medicos.size(), m, Etapa.HABILITACAO, ok ? "HABILITADO" : "FALHA", null);
        }

        // Conferência final: lê a lista da tela e compara com a fonte.
        habilitar.abrir();
        Set<String> naTela = habilitar.cpfsHabilitados();
        long faltando = medicos.stream().filter(m -> !naTela.contains(m.getCpf())).count();
        System.out.println("Conferência: " + (medicos.size() - faltando) + "/" + medicos.size()
                + " CPFs constam como habilitados.");

        habilitar.sair();
    }

    // ------------------------------------------------------------------ 2. Cadastro

    void cadastrarMedicos(List<Medico> medicos) {
        titulo("ETAPA 2 - Cadastro dos médicos");
        encerrarSessao();
        int i = 0;
        for (Medico m : medicos) {
            i++;
            cadastrar(m, i, medicos.size());
        }
    }

    private void cadastrar(Medico m, int i, int total) {
        String[] mensagem = {null};
        PaginaCadastroMedico.Resultado resultado = comTentativas("cadastrar " + m.getCpf(), () -> {
            PaginaCadastroMedico.Resultado r = cadastro.abrir().cadastrar(m);
            mensagem[0] = cadastro.getUltimaMensagem();
            if (r == PaginaCadastroMedico.Resultado.ERRO) {
                throw new IllegalStateException(mensagem[0]);
            }
            return r;
        }, PaginaCadastroMedico.Resultado.ERRO);
        String status = resultado == PaginaCadastroMedico.Resultado.ERRO ? "FALHA" : resultado.name();
        registrar(i, total, m, Etapa.CADASTRO, status, resultado == PaginaCadastroMedico.Resultado.ERRO ? mensagem[0] : null);
    }

    // ------------------------------------------------------------------ 3. Verificação

    void verificarCadastros(List<Medico> medicos) {
        titulo("ETAPA 3 - Verificação na listagem de médicos");
        Medico observador = medicos.stream()
                .filter(m -> !"FALHA".equals(relatorio.status(m, Etapa.CADASTRO)))
                .findFirst().orElse(null);
        if (observador == null) {
            System.out.println("Nenhum médico cadastrado; nada a verificar.");
            return;
        }

        for (int rodada = 1; rodada <= 2; rodada++) {
            encerrarSessao();
            if (!login.abrir().entrar(observador.getCpf(), observador.getSenha())) {
                System.out.println("Não foi possível entrar como " + observador + " para verificar a listagem.");
                return;
            }
            Set<String> listados = listagem.abrir().cpfsListados();
            principal.sair();

            List<Medico> faltando = new ArrayList<>();
            for (Medico m : medicos) {
                if (!listados.contains(m.getCpf())) {
                    faltando.add(m);
                }
            }
            System.out.println("Listagem contém " + (medicos.size() - faltando.size()) + "/" + medicos.size()
                    + " médicos da fonte.");
            if (faltando.isEmpty() || rodada == 2) {
                faltando.forEach(m -> relatorio.registrar(m, Etapa.CADASTRO, "NAO_LISTADO"));
                return;
            }
            System.out.println("Recadastrando " + faltando.size() + " médico(s) ausente(s)...");
            int i = 0;
            for (Medico m : faltando) {
                cadastrar(m, ++i, faltando.size());
            }
        }
    }

    // ------------------------------------------------------------------ 4. Remoção

    void removerMedicos(List<Medico> medicos) {
        titulo("ETAPA 4 - Remoção (cada médico remove o próprio cadastro)");
        int i = 0;
        for (Medico m : medicos) {
            i++;
            String status = comTentativas("remover " + m.getCpf(), () -> {
                encerrarSessao();
                if (!login.abrir().entrar(m.getCpf(), m.getSenha())) {
                    // Login recusado = médico não está (mais) cadastrado.
                    return "NAO_CADASTRADO";
                }
                return listagem.abrir().removerProprioCadastro(m.getCpf()) ? "REMOVIDO" : "FALHA";
            }, "FALHA");
            principal.sair();
            registrar(i, medicos.size(), m, Etapa.REMOCAO, status, null);
        }
    }

    // ------------------------------------------------------------------ utilitários

    private <T> T comTentativas(String descricao, Supplier<T> acao, T valorSeFalhar) {
        for (int t = 1; t <= TENTATIVAS; t++) {
            try {
                return acao.get();
            } catch (RuntimeException e) {
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage().split("\n")[0];
                System.out.printf("    ! tentativa %d/%d de %s falhou: %s%n", t, TENTATIVAS, descricao, msg);
            }
        }
        return valorSeFalhar;
    }

    private void encerrarSessao() {
        driver.manage().deleteAllCookies();
    }

    private void registrar(int i, int total, Medico m, Etapa etapa, String status, String obs) {
        relatorio.registrar(m, etapa, status, obs);
        System.out.printf("[%s %d/%d] %-45s %-20s %s%n", etapa, i, total, m, status, obs == null ? "" : obs);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(">>> " + texto);
    }
}
