package br.edu.utfpr.td.tsi.paginas;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base dos Page Objects: cada subclasse representa uma tela da aplicação
 * e esconde do robô os detalhes de localização dos elementos (seletores, esperas).
 */
public abstract class Pagina {

    protected final WebDriver driver;
    protected final WebDriverWait espera;
    protected final String urlBase;

    protected Pagina(WebDriver driver, String urlBase) {
        this.driver = driver;
        this.urlBase = urlBase;
        // Durante a troca de página o Chrome às vezes reclama de elementos do documento antigo;
        // a espera ignora esses erros e tenta de novo até o tempo limite.
        this.espera = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.espera.ignoring(StaleElementReferenceException.class);
        this.espera.ignoring(WebDriverException.class);
    }

    protected void abrir(String caminho) {
        driver.get(urlBase + caminho);
    }

    protected WebElement aguardarVisivel(By seletor) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(seletor));
    }

    /**
     * Preenche um campo. Se o Chrome ainda estiver trocando de página e o elemento "sumir"
     * no meio da operação, localiza de novo e tenta mais uma vez.
     */
    protected void preencher(By seletor, String valor) {
        try {
            WebElement campo = aguardarVisivel(seletor);
            campo.clear();
            campo.sendKeys(valor);
        } catch (WebDriverException e) {
            aguardarCarregamento();
            WebElement campo = aguardarVisivel(seletor);
            campo.clear();
            campo.sendKeys(valor);
        }
    }

    /** Clica num botão que submete formulário e espera a página seguinte carregar. */
    protected void clicarESubmeter(WebElement botao) {
        botao.click();
        espera.until(ExpectedConditions.stalenessOf(botao));
        aguardarCarregamento();
    }

    protected void aguardarCarregamento() {
        espera.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /** Lê os textos de vários elementos de uma vez via JavaScript (bem mais rápido que findElements em listas grandes). */
    @SuppressWarnings("unchecked")
    protected List<String> textos(String seletorCss) {
        Object r = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll(arguments[0])).map(e => e.textContent.trim());",
                seletorCss);
        return (List<String>) r;
    }

    public String urlAtual() {
        return driver.getCurrentUrl();
    }
}
