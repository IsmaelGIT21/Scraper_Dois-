package br.edu.utfpr.td.tsi.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Tela de login (/login). Admin entra com usuário "admin"; médico entra com o CPF. */
public class PaginaLogin extends Pagina {

    private static final By USUARIO = By.id("username");
    private static final By SENHA = By.id("password");
    private static final By ENTRAR = By.cssSelector("form.form button[type=submit]");

    public PaginaLogin(WebDriver driver, String urlBase) {
        super(driver, urlBase);
    }

    public PaginaLogin abrir() {
        abrir("/login");
        aguardarVisivel(USUARIO);
        return this;
    }

    /** @return true se o login foi aceito (a aplicação saiu da tela de login). */
    public boolean entrar(String usuario, String senha) {
        preencher(USUARIO, usuario);
        preencher(SENHA, senha);
        clicarESubmeter(driver.findElement(ENTRAR));
        return !urlAtual().contains("/login");
    }
}
