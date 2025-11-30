package com.wikipedia.e2e;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.pages.OnboardingPage;
import com.wikipedia.pages.SearchPage;
import com.wikipedia.utils.TestHelper;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.UUID;

@Epic("Funcionalidade de Pesquisa")
@Feature("Busca com Sem Resultados")
public class TC02_NoResultsE2ETest {

    private AppiumDriver driver;
    private OnboardingPage onboardingPage;
    private SearchPage searchPage;
    private TestHelper testHelper;
    private String searchTermNoResults;

    @BeforeMethod
    @Step("Configurar ambiente de teste")
    public void setup() {
        // Inicializa driver
        driver = DriverFactory.getInstance().initializeAndroidDriver();

        // Inicializa páginas
        onboardingPage = new OnboardingPage(driver);
        searchPage = new SearchPage(driver);

        // Inicializa helper
        testHelper = new TestHelper();

        // Gera termo de busca garantidamente sem resultados
        // Padrão: "NoResults_" + UUID (muito improvável que exista artigo com esse nome)
        searchTermNoResults = "NoResults_" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("TC02: Termo de busca gerado: " + searchTermNoResults);
    }

    @Test(description = "TC02 - Pesquisar por termo inexistente, validar mensagem de 'No results'")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valida se a aplicação exibe corretamente a mensagem 'No results found' " +
            "quando uma pesquisa não retorna artigos.")
    @Step("Executar busca sem resultados e validar mensagem")
    public void testSearchNoResults() {
        // 1. Onboarding (Se houver)
        boolean skipped = onboardingPage.skipOnboardingIfPresent();
        if (skipped) {
            Allure.step("Onboarding pulado com sucesso");
        }

        // 2. Banner na Home (Se houver)
        searchPage.closeBannerIfPresent();

        // 3. Ativar busca
        Allure.step("Ativar campo de pesquisa");
        searchPage.activateSearch();

        // 4. Digitar termo que não retorna resultados
        Allure.step("Digitar termo de busca garantidamente sem resultados: " + searchTermNoResults);
        searchPage.typeSearch(searchTermNoResults);

        // 5. Aguardar a conclusão da busca
        Allure.step("Aguardar conclusão da busca");
        try {
            Thread.sleep(1500); // Aguarda servidor processar a busca
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. Validar que NÃO há resultados
        Allure.step("Validar ausência de resultados");
        boolean hasResults = searchPage.hasResults();
        Assert.assertFalse(hasResults,
                "Esperava que não houvesse resultados para o termo: " + searchTermNoResults +
                        ", mas foram encontrados resultados.");

        // 7. Validar mensagem "No results found"
        Allure.step("Validar mensagem 'No results found'");
        boolean hasNoResultsMessage = searchPage.isNoResultsMessageDisplayed();
        Assert.assertTrue(hasNoResultsMessage,
                "A mensagem 'No results found' não foi exibida para o termo: " + searchTermNoResults);

        // 8. Capturar evidência de sucesso
        Allure.step("Capturar evidência visual de sucesso");
        testHelper.takeScreenshot("TC02_NoResults_" + searchTermNoResults);
    }

    @AfterMethod
    @Step("Finalizar teste")
    public void tearDown() {
        DriverFactory.getInstance().quitDriver();
    }
}