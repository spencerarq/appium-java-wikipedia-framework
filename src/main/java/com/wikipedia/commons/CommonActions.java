package com.wikipedia.commons;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;

/**
 * Classe utilitária que encapsula ações comuns de interação com elementos mobile
 * usando Appium. Implementa waits explícitos e tratamento robusto de exceções.
 * @author Renato Spencer
 * @version 1.4
 * @since 2025-01-28
 */
public class CommonActions {

    private static final Logger logger = LoggerFactory.getLogger(CommonActions.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int SWIPE_DURATION_MS = 800;

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    /**
     * Construtor Padrão. Inicializa com o driver fornecido e o timeout padrão.
     * @param driver A instância do AppiumDriver. Não pode ser null.
     */
    public CommonActions(AppiumDriver driver) {
        this(driver, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Timeout Customizado.
     * @param driver A instância do AppiumDriver. Não pode ser null.
     * @param timeoutSeconds O timeout em segundos para waits explícitos. Deve ser > 0.
     */
    public CommonActions(AppiumDriver driver, int timeoutSeconds) {
        if (driver == null) {
            logger.error("Driver não pode ser null ao inicializar CommonActions");
            throw new IllegalArgumentException("AppiumDriver não pode ser null");
        }
        if (timeoutSeconds <= 0) {
            logger.error("Timeout deve ser maior que zero, recebido: {}", timeoutSeconds);
            throw new IllegalArgumentException("Timeout deve ser maior que zero");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        logger.info("CommonActions inicializado com timeout customizado de {} segundos", timeoutSeconds);
    }

    /**
     * Permite a Injeção de Dependência (DI) de um WebDriverWait mockado.
     * @param driver O mock do AppiumDriver. Não pode ser null.
     * @param wait O mock do WebDriverWait. Não pode ser null.
     */
    public CommonActions(AppiumDriver driver, WebDriverWait wait) {
        if (driver == null || wait == null) {
            logger.error("Driver e Wait não podem ser null no construtor de DI");
            throw new IllegalArgumentException("Driver e Wait não podem ser null");
        }
        this.driver = driver;
        this.wait = wait; // Recebe o mock injetado
        logger.info("CommonActions inicializado via DI para testes.");
    }

    /**
     * Aguarda um elemento estar visível na tela.
     * @param element O WebElement a ser aguardado. Não pode ser null.
     * @return O WebElement visível.
     */
    public WebElement waitForElementVisible(WebElement element) {
        if (element == null) {
            logger.error("Elemento não pode ser null em waitForElementVisible");
            throw new IllegalArgumentException("WebElement não pode ser null");
        }
        try {
            logger.debug("Aguardando elemento ficar visível");
            WebElement visibleElement = getWait().until(ExpectedConditions.visibilityOf(element));
            logger.info("Elemento ficou visível com sucesso");
            return visibleElement;
        } catch (Exception e) {
            logger.error("Erro ao aguardar elemento ficar visível: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Clica em um elemento após aguardar que ele esteja visível e clicável.
     * @param element O WebElement a ser clicado. Não pode ser null.
     */
    public void clickElement(WebElement element) {
        if (element == null) {
            logger.error("Elemento não pode ser null em clickElement");
            throw new IllegalArgumentException("WebElement não pode ser null");
        }
        try {
            logger.debug("Aguardando elemento ficar clicável");
            WebElement clickableElement = getWait().until(ExpectedConditions.elementToBeClickable(element));
            clickableElement.click();
            logger.info("Clique realizado com sucesso no elemento");
        } catch (Exception e) {
            logger.error("Erro ao clicar no elemento: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Insere texto num campo de input após aguardar visibilidade e limpá-lo.
     * @param element O WebElement (campo de texto). Não pode ser null.
     * @param text O texto a ser inserido. Não pode ser null nem vazio.
     */
    public void inputText(WebElement element, String text) {
        if (element == null) {
            logger.error("Elemento não pode ser null em inputText");
            throw new IllegalArgumentException("WebElement não pode ser null");
        }
        if (text == null || text.isEmpty()) {
            logger.error("Texto não pode ser null ou vazio em inputText");
            throw new IllegalArgumentException("Texto não pode ser null ou vazio");
        }
        try {
            logger.debug("Aguardando campo de texto ficar visível para input");
            WebElement visibleElement = getWait().until(ExpectedConditions.visibilityOf(element));
            visibleElement.clear();
            visibleElement.sendKeys(text);
            logger.info("Texto '{}' inserido com sucesso no elemento", text);
        } catch (Exception e) {
            logger.error("Erro ao inserir texto '{}' no elemento: {}", text, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtém o texto visível de um elemento após aguardar a visibilidade.
     * @param element O WebElement do qual obter o texto. Não pode ser null.
     * @return O texto do elemento como String.
     */
    public String getTextFromElement(WebElement element) {
        if (element == null) {
            logger.error("Elemento não pode ser null em getTextFromElement");
            throw new IllegalArgumentException("WebElement não pode ser null");
        }
        try {
            logger.debug("Aguardando elemento ficar visível para extrair texto");
            WebElement visibleElement = getWait().until(ExpectedConditions.visibilityOf(element));
            String text = visibleElement.getText();
            logger.info("Texto extraído com sucesso do elemento: '{}'", text);
            return text;
        } catch (Exception e) {
            logger.error("Erro ao extrair texto do elemento: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Realiza um swipe vertical padrão (scroll down), de 80% a 20% da altura da tela.
     */
    public void swipe() {
        Dimension size = driver.manage().window().getSize();
        if (size == null) {
            logger.error("Não foi possível obter dimensões da tela");
            throw new IllegalStateException("Dimensões da tela não disponíveis");
        }
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);

        logger.debug("Iniciando swipe de ({}, {}) até ({}, {})", startX, startY, startX, endY);
        swipe(startX, startY, startX, endY);
    }

    /**
     * Realiza um swipe customizado usando a API W3C Actions.
     * @param startX Coordenada X inicial. Deve ser >= 0.
     * @param startY Coordenada Y inicial. Deve ser >= 0.
     * @param endX Coordenada X final. Deve ser >= 0.
     * @param endY Coordenada Y final. Deve ser >= 0.
     */
    public void swipe(int startX, int startY, int endX, int endY) {
        if (startX < 0 || startY < 0 || endX < 0 || endY < 0) {
            logger.error("Coordenadas não podem ser negativas: ({},{}) até ({},{})", startX, startY, endX, endY);
            throw new IllegalArgumentException("Coordenadas devem ser não-negativas");
        }
        try {
            logger.debug("Iniciando swipe W3C de ({}, {}) até ({}, {})", startX, startY, endX, endY);

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipeSequence = new Sequence(finger, 1);

            swipeSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipeSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipeSequence.addAction(finger.createPointerMove(Duration.ofMillis(SWIPE_DURATION_MS), PointerInput.Origin.viewport(), endX, endY));
            swipeSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            driver.perform(Collections.singletonList(swipeSequence));

            logger.info("Swipe W3C customizado realizado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao realizar swipe W3C customizado: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retorna a instância do WebDriverWait configurada.
     * @return A instância do WebDriverWait.
     */
    public WebDriverWait getWait() {
        return wait;
    }

    /**
     * Retorna a instância do AppiumDriver atual.
     * @return A instância do AppiumDriver.
     */
    public AppiumDriver getDriver() {
        return driver;
    }
}