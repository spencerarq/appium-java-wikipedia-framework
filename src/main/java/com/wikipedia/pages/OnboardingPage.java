package com.wikipedia.pages;

import com.wikipedia.commons.CommonActions;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class OnboardingPage {

    private final AppiumDriver driver;
    private final CommonActions actions;

    // Mapeamento dos Elementos (Locators via PageFactory)

    // Botão "Pular" (Skip)
    @AndroidFindBy(id = "org.wikipedia.alpha:id/fragment_onboarding_skip_button")
    private WebElement skipButton;

    // Botão "Continuar" (Continue / Seta para a direita)
    @AndroidFindBy(id = "org.wikipedia.alpha:id/fragment_onboarding_forward_button")
    private WebElement continueButton;

    // Botão "Começar" (Get Started / Done) - Visível apenas na última tela
    @AndroidFindBy(id = "org.wikipedia.alpha:id/fragment_onboarding_done_button")
    private WebElement getStartedButton;

    // Construtor

    public OnboardingPage(AppiumDriver driver) {
        this.driver = driver;
        this.actions = new CommonActions(driver);
        // Inicializa os elementos anotados com @AndroidFindBy
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    // Métodos de Interação

    /**
     * Clica no botão "Pular" para ir direto à tela inicial.
     */
    public void clickSkip() {
        System.out.println("Navegando: Clicando em Skip no Onboarding...");
        actions.clickElement(skipButton);
    }

    /**
     * Clica no botão "Continuar" para avançar o carrossel.
     */
    public void clickContinue() {
        actions.clickElement(continueButton);
    }

    /**
     * Clica no botão "Get Started" (disponível na última tela do onboarding).
     */
    public void clickGetStarted() {
        System.out.println("Navegando: Clicando em Get Started...");
        actions.clickElement(getStartedButton);
    }

    /**
     * Método auxiliar para pular o onboarding completamente se o botão estiver visível.
     * Útil para pré-condições de teste.
     * @return true se pulou, false se o botão não estava visível (talvez já tenha passado).
     */
    public boolean skipOnboardingIfPresent() {
        try {
            // Verifica se o botão está visível antes de tentar clicar
            // try-catch simples ou um método específico de verificação
            actions.waitForElementVisible(skipButton);
            clickSkip();
            return true;
        } catch (Exception e) {
            // Botão não encontrado ou timeout, assume que já passou do onboarding
            return false;
        }
    }
}