package com.wikipedia.e2e;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.pages.ArticlePage;
import com.wikipedia.pages.OnboardingPage;
import com.wikipedia.pages.SearchPage;
import com.wikipedia.utils.TestHelper;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Funcionalidade de Pesquisa")
@Feature("Busca de Artigos")
public class TC01_SearchArticleE2ETest {

    private AppiumDriver driver;
    private OnboardingPage onboardingPage;
    private SearchPage searchPage;
    private ArticlePage articlePage;
    private TestHelper testHelper;

    @BeforeMethod
    @Step("Configurar ambiente de teste")
    public void setup() {
        // Inicializa driver
        driver = DriverFactory.getInstance().initializeAndroidDriver();

        // Inicializa páginas
        onboardingPage = new OnboardingPage(driver);
        searchPage = new SearchPage(driver);
        articlePage = new ArticlePage(driver);

        // Inicializa helper (vídeo é gravado automaticamente pelo Listener)
        testHelper = new TestHelper();
    }

    @Test(description = "TC01 - Pesquisar por 'Appium', selecionar resultado e validar")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida se o usuário consegue realizar uma pesquisa com sucesso e acessar o artigo.")
    @Step("Executar busca e validar resultados")
    public void testSearchArticleSuccess() {
        // 1. Onboarding (Se houver)
        boolean skipped = onboardingPage.skipOnboardingIfPresent();
        if (skipped) {
            Allure.step("Onboarding pulado com sucesso");
        }

        // 2. Banner na Home (Se houver)
        searchPage.closeBannerIfPresent();

        // 3. Fluxo de Busca
        Allure.step("Ativar busca");
        searchPage.activateSearch();

        String searchTerm = "Appium";
        Allure.step("Digitar termo de busca: " + searchTerm);
        searchPage.typeSearch(searchTerm);

        Allure.step("Validar resultados na lista");
        Assert.assertTrue(searchPage.hasResults(),
                "A lista de resultados não deveria estar vazia para: " + searchTerm);

        Allure.step("Selecionar primeiro resultado");
        searchPage.clickResult(0);

        // 4. Validação do Artigo
        // O método isTitleCorrect agora chama handleArticleInterventions internamente
        // para fechar o banner antes de ler o título.
        Allure.step("Validar título do artigo aberto");
        boolean isTitleCorrect = articlePage.isTitleCorrect(searchTerm);

        Assert.assertTrue(isTitleCorrect,
                "O título do artigo aberto não contém o termo pesquisado: " + searchTerm);

        // Evidência de sucesso
        Allure.step("Capturar evidência visual de sucesso");
        testHelper.takeScreenshot("TC01_Sucesso_" + searchTerm);
    }

    @AfterMethod
    @Step("Finalizar teste")
    public void tearDown() {
        DriverFactory.getInstance().quitDriver();
    }
}