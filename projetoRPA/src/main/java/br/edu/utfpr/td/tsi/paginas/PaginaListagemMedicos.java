package br.edu.utfpr.td.tsi.paginas;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Tela "Lista de Médicos" (/listarTodosMedicos). Exige médico logado. */
public class PaginaListagemMedicos extends Pagina {

    private static final By TABELA = By.cssSelector("table");

    public PaginaListagemMedicos(WebDriver driver, String urlBase) {
        super(driver, urlBase);
    }

    public PaginaListagemMedicos abrir() {
        abrir("/listarTodosMedicos");
        aguardarVisivel(TABELA);
        return this;
    }

    /** CPFs (3ª coluna) de todos os médicos listados. */
    public Set<String> cpfsListados() {
        return new HashSet<>(textos("table tbody tr td:nth-child(3)"));
    }

    /**
     * Remove o médico logado: a aplicação só exibe o link "Remover" na linha do próprio médico.
     * Clica no link, confirma o alerta do navegador e espera voltar para a listagem.
     *
     * @return true se a linha com esse CPF sumiu da listagem.
     */
    public boolean removerProprioCadastro(String cpf) {
        By linkRemover = By.xpath("//tbody/tr[td[3][normalize-space()='" + cpf + "']]"
                + "//a[contains(normalize-space(),'Remover')]");
        List<WebElement> links = driver.findElements(linkRemover);
        if (links.isEmpty()) {
            return false;
        }
        WebElement link = links.get(0);
        link.click();
        espera.until(ExpectedConditions.alertIsPresent()).accept();
        espera.until(ExpectedConditions.stalenessOf(link));
        aguardarCarregamento();
        return !cpfsListados().contains(cpf);
    }
}
