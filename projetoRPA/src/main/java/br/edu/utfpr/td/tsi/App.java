package br.edu.utfpr.td.tsi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import br.edu.utfpr.td.tsi.conversao.ConversorMedico;
import br.edu.utfpr.td.tsi.leitura.LeitorJsonMedicos;
import br.edu.utfpr.td.tsi.modelo.Medico;
import br.edu.utfpr.td.tsi.modelo.MedicoFonte;
import br.edu.utfpr.td.tsi.relatorio.Relatorio;
import br.edu.utfpr.td.tsi.robo.Modo;
import br.edu.utfpr.td.tsi.robo.Navegador;
import br.edu.utfpr.td.tsi.robo.RoboClinicaMedica;

/**
 * Ponto de entrada do RPA.
 *
 * <pre>
 * java -jar projetoRPA.jar [--url=...] [--dados=...] [--modo=completo|cadastrar|remover]
 *                          [--headless] [--limite=N] [--admin=admin] [--senha-admin=12345]
 * </pre>
 */
public class App {

    private static final String URL_PADRAO = "http://localhost:8080/clinica.medica";

    public static void main(String[] args) throws Exception {
        Map<String, String> opcoes = lerArgumentos(args);
        if (opcoes.containsKey("ajuda") || opcoes.containsKey("help")) {
            imprimirAjuda();
            return;
        }

        String url = opcoes.getOrDefault("url", URL_PADRAO).replaceAll("/+$", "");
        Path dados = Path.of(opcoes.getOrDefault("dados", localizarPastaDados()));
        Modo modo = Modo.valueOf(opcoes.getOrDefault("modo", "completo").toUpperCase(Locale.ROOT));
        boolean semJanela = opcoes.containsKey("headless");
        int limite = Integer.parseInt(opcoes.getOrDefault("limite", "0"));
        String admin = opcoes.getOrDefault("admin", "admin");
        String senhaAdmin = opcoes.getOrDefault("senha-admin", "12345");

        System.out.println("RPA Clínica Médica");
        System.out.println("  Aplicação: " + url);
        System.out.println("  Dados:     " + dados.toAbsolutePath().normalize());
        System.out.println("  Modo:      " + modo);
        System.out.println();
        System.out.println("Lendo arquivos JSON...");

        List<MedicoFonte> fonte = new LeitorJsonMedicos().ler(dados);
        ConversorMedico conversor = new ConversorMedico();
        List<Medico> medicos = fonte.stream().map(conversor::converter).toList();
        if (limite > 0 && limite < medicos.size()) {
            medicos = medicos.subList(0, limite);
            System.out.println("Limitado aos primeiros " + limite + " médicos (--limite).");
        }
        System.out.println("Total de médicos a processar: " + medicos.size());

        Relatorio relatorio = new Relatorio(medicos);
        Instant inicio = Instant.now();
        WebDriver driver = Navegador.criar(semJanela);
        try {
            new RoboClinicaMedica(driver, url, admin, senhaAdmin, relatorio).executar(medicos, modo);
        } finally {
            driver.quit();
            relatorio.imprimirResumo();
            Path csv = Path.of("relatorio-" + modo.name().toLowerCase(Locale.ROOT) + "-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".csv");
            relatorio.salvarCsv(csv);
            Duration duracao = Duration.between(inicio, Instant.now());
            System.out.printf("Tempo total: %dmin %ds%n", duracao.toMinutes(), duracao.toSecondsPart());
            System.out.println("Relatório salvo em " + csv.toAbsolutePath());
        }
    }

    /** Usa ./dados se existir; senão ../dados (útil ao rodar de dentro da pasta do projeto). */
    private static String localizarPastaDados() {
        for (String candidato : new String[] {"dados", "../dados"}) {
            if (Files.isDirectory(Path.of(candidato))) {
                return candidato;
            }
        }
        return "dados";
    }

    private static Map<String, String> lerArgumentos(String[] args) {
        Map<String, String> opcoes = new LinkedHashMap<>();
        for (String arg : args) {
            String a = arg.replaceFirst("^-+", "");
            int igual = a.indexOf('=');
            if (igual < 0) {
                opcoes.put(a, "true");
            } else {
                opcoes.put(a.substring(0, igual), a.substring(igual + 1));
            }
        }
        return opcoes;
    }

    private static void imprimirAjuda() {
        System.out.println("""
                Uso: java -jar projetoRPA.jar [opções]

                  --url=URL            endereço da aplicação (padrão: %s)
                  --dados=CAMINHO      arquivo .json ou pasta com arquivos .json (padrão: ./dados ou ../dados)
                  --modo=MODO          completo (padrão) | cadastrar | remover
                  --headless           executa o Chrome sem abrir janela
                  --limite=N           processa só os N primeiros médicos (útil para testes)
                  --admin=USUARIO      usuário administrador (padrão: admin)
                  --senha-admin=SENHA  senha do administrador (padrão: 12345)
                """.formatted(URL_PADRAO));
    }
}
