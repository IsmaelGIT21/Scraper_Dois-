package br.edu.utfpr.td.tsi.paginas;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import br.edu.utfpr.td.tsi.modelo.Especialidade;
import br.edu.utfpr.td.tsi.modelo.Medico;

/** Tela pública de cadastro de médico (/cadastrarMedico). */
public class PaginaCadastroMedico extends Pagina {

    public enum Resultado { CADASTRADO, JA_CADASTRADO, ERRO }

    private static final By NOME = By.name("nome");
    private static final By EMAIL = By.name("email");
    private static final By CPF = By.name("cpf");
    private static final By CRM = By.name("crm");
    private static final By SENHA = By.name("senha");
    private static final By CONFIRMAR_SENHA = By.name("confirmarSenha");
    private static final By SALVAR = By.cssSelector("form.form button[type=submit]");
    private static final By MENSAGEM_ERRO = By.cssSelector(".erro");
    private static final By TITULO = By.tagName("h1");

    private String ultimaMensagem = "";

    public PaginaCadastroMedico(WebDriver driver, String urlBase) {
        super(driver, urlBase);
    }

    public PaginaCadastroMedico abrir() {
        abrir("/cadastrarMedico");
        aguardarVisivel(NOME);
        return this;
    }

    public Resultado cadastrar(Medico medico) {
        preencher(NOME, medico.getNome());
        preencher(EMAIL, medico.getEmail());
        preencher(CPF, medico.getCpf());
        preencher(CRM, medico.getCrm());
        preencher(SENHA, medico.getSenha());
        preencher(CONFIRMAR_SENHA, medico.getSenha());
        marcarEspecialidades(medico);

        clicarESubmeter(driver.findElement(SALVAR));

        String titulo = aguardarVisivel(TITULO).getText();
        if (titulo.contains("sucesso")) {
            ultimaMensagem = titulo;
            return Resultado.CADASTRADO;
        }
        List<WebElement> erros = driver.findElements(MENSAGEM_ERRO);
        ultimaMensagem = erros.isEmpty() ? "Resposta inesperada: " + titulo : erros.get(0).getText();
        return ultimaMensagem.contains("já cadastrado") ? Resultado.JA_CADASTRADO : Resultado.ERRO;
    }

    private void marcarEspecialidades(Medico medico) {
        for (Especialidade esp : medico.getEspecialidades()) {
            By checkbox = By.cssSelector("input[type=checkbox][name=especialidades][value='" + esp.name() + "']");
            WebElement caixa = driver.findElement(checkbox);
            if (!caixa.isSelected()) {
                caixa.click();
            }
        }
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }
}
