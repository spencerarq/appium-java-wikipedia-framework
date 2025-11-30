package com.wikipedia.pages;

import com.wikipedia.commons.CommonActions;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class SearchPage {

    private final AppiumDriver driver;
    private final CommonActions actions;

    // --- Elementos da Tela Inicial ---

    // Container da barra de pesquisa (na tela inicial, antes de clicar)
    @AndroidFindBy(id = "org.wikipedia.alpha:id/search_container")
    private WebElement searchContainer;

    // [NOVO] Botão "Negative" do Banner (Ex: "Não, obrigado" ou "Fechar")
    // ID retirado do seu PDF de mapeamento da Home
    @AndroidFindBy(id = "org.wikipedia.alpha:id/view_announcement_action_negative")
    private WebElement announcementCloseButton;

    // --- Elementos da Tela de Pesquisa (após clicar no container) ---

    // Campo de input de texto onde digitamos a busca
    @AndroidFindBy(id = "org.wikipedia.alpha:id/search_src_text")
    private WebElement searchInput;

    // Botão de fechar/limpar pesquisa (o 'X')
    @AndroidFindBy(id = "org.wikipedia.alpha:id/search_close_btn")
    private WebElement closeButton;

    // Lista de títulos dos resultados da pesquisa
    @AndroidFindBy(id = "org.wikipedia.alpha:id/page_list_item_title")
    private List<WebElement> searchResults;

    // --- Elementos de Mensagem de "Sem Resultados" (NOVO) ---

    // Mensagem exibida quando não há resultados
    // XPath genérico que procura por TextView contendo "No results"
    // AJUSTE ESTE XPATH conforme necessário após inspecionar o app
    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text, 'No results') or contains(@text, 'no results') or contains(@text, 'nenhum resultado')]")
    private WebElement noResultsMessage;

    // --- Construtor ---

    public SearchPage(AppiumDriver driver) {
        this.driver = driver;
        this.actions = new CommonActions(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    // --- Métodos de Ação ---

    /**
     * [NOVO] Tenta fechar o banner de anúncio se ele estiver visível na Home.
     * Usa um try-catch para não falhar o teste caso o banner NÃO apareça.
     */
    public void closeBannerIfPresent() {
        try {
            System.out.println("SearchPage: Verificando se há banner/anúncio na Home...");
            // Tenta clicar. Se não existir, o CommonActions vai lançar erro após o timeout.
            // O try-catch captura esse erro e permite que o teste continue.
            actions.clickElement(announcementCloseButton);
            System.out.println("SearchPage: Banner fechado.");
        } catch (Exception e) {
            System.out.println("SearchPage: Nenhum banner obstrutivo encontrado na Home. Seguindo...");
        }
    }

    /**
     * Clica na barra de pesquisa na tela inicial para expandi-la.
     */
    public void activateSearch() {
        System.out.println("SearchPage: Clicando na barra de pesquisa...");
        actions.clickElement(searchContainer);
    }

    /**
     * Digita o termo de pesquisa.
     * @param text Termo a ser pesquisado (ex: "Appium")
     */
    public void typeSearch(String text) {
        System.out.println("SearchPage: Digitando '" + text + "'...");
        actions.inputText(searchInput, text);
    }

    /**
     * Combinação de ativar e digitar.
     */
    public void searchFor(String text) {
        activateSearch();
        typeSearch(text);
    }

    /**
     * Seleciona um resultado da lista pelo índice.
     * @param index Índice do resultado (0 para o primeiro).
     */
    public void clickResult(int index) {
        if (searchResults == null || searchResults.isEmpty()) {
            throw new RuntimeException("Nenhum resultado encontrado na lista.");
        }
        if (index >= searchResults.size()) {
            throw new IndexOutOfBoundsException("Índice " + index + " inválido. Total de resultados: " + searchResults.size());
        }

        WebElement result = searchResults.get(index);
        System.out.println("SearchPage: Clicando no resultado [" + index + "]: " + result.getText());
        actions.clickElement(result);
    }

    /**
     * Verifica se existem resultados na lista.
     * @return true se houver pelo menos um resultado.
     */
    public boolean hasResults() {
        return !searchResults.isEmpty();
    }

    /**
     * [NOVO] Verifica se a mensagem "No results found" está sendo exibida.
     * Utilizado no TC02 para validar buscas sem resultados.
     * @return true se a mensagem de "sem resultados" está visível.
     */
    public boolean isNoResultsMessageDisplayed() {
        System.out.println("SearchPage: Verificando se mensagem 'No results' está exibida...");
        try {
            // Tenta obter o texto da mensagem. Se conseguir, ela está visível.
            String text = actions.getTextFromElement(noResultsMessage);
            System.out.println("SearchPage: Mensagem 'No results' encontrada: '" + text + "'");
            return true;
        } catch (Exception e) {
            System.out.println("SearchPage: Mensagem 'No results' NÃO encontrada. " + e.getMessage());
            return false;
        }
    }
}