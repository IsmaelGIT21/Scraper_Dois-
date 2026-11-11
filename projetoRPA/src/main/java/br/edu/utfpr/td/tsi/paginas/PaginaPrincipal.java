package br.edu.utfpr.td.tsi.paginas;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Página principal do médico (/paginaPrincipal), usada para fazer logout. */
public class PaginaPrincipal extends Pagina {

    private static final By BOTAO_LOGOUT = By.cssSelector("button.btn-logout");

    public PaginaPrincipal(WebDriver driver, String urlBase) {
        super(driver, urlBase);
    }

    /** Faz logout pela interface; se a tela não oferecer o botão, encerra a sessão limpando os cookies. */
    public void sair() {
        try {
            abrir("/paginaPrincipal");
            aguardarCarregamento();
            List<WebElement> botoes = driver.findElements(BOTAO_LOGOUT);
            if (!botoes.isEmpty()) {
                clicarESubmeter(botoes.get(0));
                return;
            }
        } catch (RuntimeException e) {
            // segue para o plano B
        }
        driver.manage().deleteAllCookies();
    }
}
