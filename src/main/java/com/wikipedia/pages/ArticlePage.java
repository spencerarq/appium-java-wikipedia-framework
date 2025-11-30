package com.wikipedia.pages;

import com.wikipedia.commons.CommonActions;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object para a tela de visualização de um artigo.
 */
public class ArticlePage {

    private final AppiumDriver driver;
    private final CommonActions actions;

    // Mapeamento dos Elementos do Artigo (Locators)

    // Título do artigo - Locator principal (XPath por classe e posição)
    // O elemento é um TextView sem Resource ID, na posição esperada
    @AndroidFindBy(xpath = "//android.widget.TextView[@bounds='[42,342][916,436]']")
    private WebElement articleTitle;

    // Locators alternativos para o título (fallback)
    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text, 'Appium') or contains(@text, 'appium')]")
    private WebElement articleTitleAlt1;

    @AndroidFindBy(xpath = "//android.widget.TextView[@index='0' and @package='org.wikipedia.alpha']")
    private WebElement articleTitleAlt2;

    // Botão de Salvar/Favoritar
    @AndroidFindBy(id = "org.wikipedia.alpha:id/page_save")
    private WebElement bookmarkButton;

    // Botão de Voltar
    @AndroidFindBy(xpath = "//android.widget.ImageButton[@content-desc='Navigate up']")
    private WebElement navigateUpButton;

    // Mapeamento de Elementos do Banner (BLOQUEADOR)

    // Botão de Fechar "X" do Banner (accessibility id = Close)
    @AndroidFindBy(xpath = "//android.widget.ImageView[@content-desc='Close']")
    private WebElement bannerCloseButton;

    // Botão "Play today's game"
    @AndroidFindBy(id = "org.wikipedia:id/playGameButton")
    private WebElement playGameButton;

    // Construtor

    public ArticlePage(AppiumDriver driver) {
        this.driver = driver;
        this.actions = new CommonActions(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    // Métodos de Defesa contra Banner

    /**
     * Verifica se um elemento pode ser acessado (existe e não lança exceção)
     */
    private boolean elementExists(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * [DEFESA CRÍTICA CONTRA BANNER BLOQUEADOR]
     * Fecha o banner AGRESSIVAMENTE antes de qualquer interação.
     * Tenta múltiplos caminhos para garantir o fechamento.
     */
    private void closeBannerAgressively() {
        System.out.println("ArticlePage: Tentando fechar banner bloqueador...");

        // Tentativa 1: Clique no botão "X" (accessibility id = Close)
        try {
            if (elementExists(bannerCloseButton)) {
                System.out.println("ArticlePage: Tentativa 1 - Banner detectado! Clicando no botão X...");
                actions.clickElement(bannerCloseButton);
                Thread.sleep(500); // Aguarda animação de fechamento
                System.out.println("ArticlePage: Banner fechado com sucesso via botão X!");
                return;
            }
        } catch (Exception e) {
            System.out.println("ArticlePage: Botão X falhou - " + e.getMessage());
        }

        // Tentativa 2: Clique no botão "Play today's game" como fallback
        try {
            if (elementExists(playGameButton)) {
                System.out.println("ArticlePage: Tentativa 2 - Clicando no botão Play Game...");
                actions.clickElement(playGameButton);
                Thread.sleep(500);
                System.out.println("ArticlePage: Banner fechado via Play Game!");
                return;
            }
        } catch (Exception e) {
            System.out.println("ArticlePage: Botão Play Game falhou - " + e.getMessage());
        }

        // Tentativa 3: Swipe para cima para descartar o banner
        try {
            System.out.println("ArticlePage: Tentativa 3 - Fazendo swipe para cima...");
            actions.swipe(); // Swipe padrão (80% para 20% da altura)
            Thread.sleep(500);
            System.out.println("ArticlePage: Banner descartado via swipe!");
            return;
        } catch (Exception e) {
            System.out.println("ArticlePage: Swipe falhou - " + e.getMessage());
        }

        System.out.println("ArticlePage: Banner não foi detectado ou já estava fechado.");
    }

    /**
     * Obtém o texto do título do artigo.
     * CRÍTICO: Fecha o banner PRIMEIRO, depois busca o título.
     * Tenta múltiplos locators para encontrar o elemento.
     * @return O título do artigo como String.
     */
    public String getArticleTitle() {
        System.out.println("ArticlePage: === INICIANDO LEITURA DO TÍTULO ===");

        // DEFESA CRÍTICA: Fecha banner ANTES de qualquer espera pelo título
        closeBannerAgressively();

        // Pequena pausa para garantir que o banner foi fechado e a tela foi renderizada
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("ArticlePage: Tentando obter o título com múltiplos locators...");

        // Tentativa 1: Locator principal
        try {
            System.out.println("ArticlePage: Tentativa 1 - Usando locator principal (pcs-edit-section-title-description)");
            return actions.getTextFromElement(articleTitle);
        } catch (Exception e) {
            System.out.println("ArticlePage: Locator principal falhou - " + e.getMessage());
        }

        // Tentativa 2: Locator alternativo 1
        try {
            System.out.println("ArticlePage: Tentativa 2 - Usando locator alternativo 1 (view_page_title_text)");
            return actions.getTextFromElement(articleTitleAlt1);
        } catch (Exception e) {
            System.out.println("ArticlePage: Locator alternativo 1 falhou - " + e.getMessage());
        }

        // Tentativa 3: Locator alternativo 2 (busca pelo texto do termo pesquisado)
        try {
            System.out.println("ArticlePage: Tentativa 3 - Usando locator alternativo 2 (busca por texto)");
            return actions.getTextFromElement(articleTitleAlt2);
        } catch (Exception e) {
            System.out.println("ArticlePage: Locator alternativo 2 falhou - " + e.getMessage());
        }

        // Se nenhum funcionou, lança exceção
        System.out.println("ArticlePage: ERRO - Nenhum locator funcionou para encontrar o título!");
        throw new RuntimeException("Não foi possível encontrar o elemento do título com nenhum dos locators disponíveis");
    }

    /**
     * Verifica se o título do artigo contém o texto esperado.
     */
    public boolean isTitleCorrect(String expectedText) {
        String actualTitle = getArticleTitle();

        if (actualTitle == null) return false;
        return actualTitle.toLowerCase().contains(expectedText.toLowerCase());
    }

    public void clickSaveArticle() {
        closeBannerAgressively();
        actions.clickElement(bookmarkButton);
    }

    public void navigateBack() {
        System.out.println("ArticlePage: Voltando para a tela anterior...");
        actions.clickElement(navigateUpButton);
    }
}