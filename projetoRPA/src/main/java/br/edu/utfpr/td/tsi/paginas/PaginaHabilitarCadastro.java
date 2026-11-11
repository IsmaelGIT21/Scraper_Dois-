package br.edu.utfpr.td.tsi.paginas;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Tela do administrador para habilitar CPFs (/admin/habilitarCadastroMedico). */
public class PaginaHabilitarCadastro extends Pagina {

    private static final By CAMPO_CPF = By.id("cpf");
    private static final By BOTAO_HABILITAR = By.cssSelector("form.form button[type=submit]");
    private static final By BOTAO_SAIR = By.cssSelector("button.btn-logout");

    public PaginaHabilitarCadastro(WebDriver driver, String urlBase) {
        super(driver, urlBase);
    }

    public PaginaHabilitarCadastro abrir() {
        abrir("/admin/habilitarCadastroMedico");
        aguardarVisivel(CAMPO_CPF);
        return this;
    }

    /** CPFs que já aparecem na lista "CPFs Habilitados". */
    public Set<String> cpfsHabilitados() {
        return new HashSet<>(textos(".lista .item span"));
    }

    public void habilitar(String cpf) {
        preencher(CAMPO_CPF, cpf);
        clicarESubmeter(driver.findElement(BOTAO_HABILITAR));
        aguardarVisivel(CAMPO_CPF);
    }

    public void sair() {
        clicarESubmeter(aguardarVisivel(BOTAO_SAIR));
    }
}
