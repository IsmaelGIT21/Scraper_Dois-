package br.edu.utfpr.td.tsi.robo;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Cria o navegador controlado pelo robô (Google Chrome).
 * O Selenium Manager, embutido no Selenium 4, baixa o chromedriver compatível automaticamente.
 */
public final class Navegador {

    private Navegador() {
    }

    public static WebDriver criar(boolean semJanela) {
        ChromeOptions opcoes = new ChromeOptions();
        if (semJanela) {
            opcoes.addArguments("--headless=new");
        }
        opcoes.addArguments("--window-size=1280,900", "--disable-notifications", "--no-first-run");
        // Evita os pop-ups de "salvar senha" / "senha vazada" do Chrome durante a automação.
        opcoes.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));

        WebDriver driver = new ChromeDriver(opcoes);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        return driver;
    }
}
