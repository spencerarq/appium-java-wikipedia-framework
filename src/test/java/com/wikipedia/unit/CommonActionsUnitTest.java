package com.wikipedia.unit;

import com.wikipedia.commons.CommonActions;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para CommonActions (Cobertura >=80%)
 * @author Renato Spencer
 * @version 1.7
 * @since 2025-10-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommonActions - Testes Unitários")
class CommonActionsUnitTest {

    @Mock private AppiumDriver mockDriver;
    @Mock private WebElement mockElement;
    @Mock private org.openqa.selenium.WebDriver.Options mockOptions;
    @Mock private org.openqa.selenium.WebDriver.Window mockWindow;
    @Mock private WebDriverWait mockWait;

    private CommonActions commonActions;

    @BeforeEach
    void setUp() {
        commonActions = new CommonActions(mockDriver, mockWait);
    }

    // TESTES DE CONSTRUTOR (5)

    @Test
    @DisplayName("Construtor(driver): Deve inicializar com driver válido")
    void testConstructor_WithValidDriver_ShouldInitialize() {
        CommonActions actions = new CommonActions(mockDriver);
        assertNotNull(actions);
        assertNotNull(actions.getDriver());
        assertNotNull(actions.getWait());
    }

    @Test
    @DisplayName("Construtor(driver): Deve lançar exceção com driver null")
    void testConstructor_WithNullDriver_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new CommonActions(null));
    }

    @Test
    @DisplayName("Construtor(driver, int): Deve aceitar timeout válido")
    void testConstructor_WithCustomTimeout_ShouldInitialize() {
        CommonActions actions = new CommonActions(mockDriver, 60);
        assertNotNull(actions);
        assertNotNull(actions.getWait());
    }

    @Test
    @DisplayName("Construtor(driver, int): Deve lançar exceção com timeout = 0")
    void testConstructor_WithZeroTimeout_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new CommonActions(mockDriver, 0));
    }

    @Test
    @DisplayName("Construtor(driver, int): Deve lançar exceção com timeout < 0")
    void testConstructor_WithNegativeTimeout_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new CommonActions(mockDriver, -1));
    }

    // TESTES waitForElementVisible (3)

    @Test
    @DisplayName("waitForElementVisible: Deve retornar elemento quando visível")
    void testWaitForElementVisible_WithVisibleElement_ShouldReturnElement() {
        // Usar any(Function.class) ao invés de matcher específico
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);

        WebElement result = commonActions.waitForElementVisible(mockElement);

        assertNotNull(result);
        assertEquals(mockElement, result);
        verify(mockWait, times(1)).until(any(Function.class));
    }

    @Test
    @DisplayName("waitForElementVisible: Deve lançar exceção com elemento null")
    void testWaitForElementVisible_WithNullElement_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.waitForElementVisible(null));
        verify(mockWait, never()).until(any());
    }

    @Test
    @DisplayName("waitForElementVisible: Deve propagar TimeoutException")
    void testWaitForElementVisible_WithTimeout_ShouldThrowTimeoutException() {
        when(mockWait.until(any(Function.class)))
                .thenThrow(new TimeoutException("Timeout simulado"));

        assertThrows(TimeoutException.class,
                () -> commonActions.waitForElementVisible(mockElement));
        verify(mockWait, times(1)).until(any(Function.class));
    }

    //  TESTES clickElement (3)

    @Test
    @DisplayName("clickElement: Deve clicar em elemento clicável")
    void testClickElement_WithClickableElement_ShouldClick() {
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);
        doNothing().when(mockElement).click();

        assertDoesNotThrow(() -> commonActions.clickElement(mockElement));

        verify(mockWait, times(1)).until(any(Function.class));
        verify(mockElement, times(1)).click();
    }

    @Test
    @DisplayName("clickElement: Deve lançar exceção com elemento null")
    void testClickElement_WithNullElement_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.clickElement(null));
        verify(mockWait, never()).until(any());
    }

    @Test
    @DisplayName("clickElement: Deve propagar exceção quando clique falha")
    void testClickElement_WhenClickFails_ShouldThrowException() {
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);
        doThrow(new WebDriverException("Elemento não clicável"))
                .when(mockElement).click();

        assertThrows(WebDriverException.class,
                () -> commonActions.clickElement(mockElement));
        verify(mockWait, times(1)).until(any(Function.class));
        verify(mockElement, times(1)).click();
    }

    //  TESTES inputText (4)

    @Test
    @DisplayName("inputText: Deve inserir texto válido em elemento")
    void testInputText_WithValidText_ShouldInputText() {
        String testText = "Appium Test";
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);
        doNothing().when(mockElement).clear();
        doNothing().when(mockElement).sendKeys(anyString());

        assertDoesNotThrow(() -> commonActions.inputText(mockElement, testText));

        verify(mockWait, times(1)).until(any(Function.class));
        verify(mockElement, times(1)).clear();
        verify(mockElement, times(1)).sendKeys(testText);
    }

    @Test
    @DisplayName("inputText: Deve lançar exceção com elemento null")
    void testInputText_WithNullElement_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.inputText(null, "text"));
        verify(mockWait, never()).until(any());
    }

    @Test
    @DisplayName("inputText: Deve lançar exceção com texto null")
    void testInputText_WithNullText_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.inputText(mockElement, null));
        verify(mockWait, never()).until(any());
    }

    @Test
    @DisplayName("inputText: Deve lançar exceção com texto vazio")
    void testInputText_WithEmptyText_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.inputText(mockElement, ""));
        verify(mockWait, never()).until(any());
    }

    //  TESTES getTextFromElement (3)

    @Test
    @DisplayName("getTextFromElement: Deve extrair texto de elemento")
    void testGetTextFromElement_WithValidElement_ShouldReturnText() {
        String expectedText = "Appium Framework";
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);
        when(mockElement.getText()).thenReturn(expectedText);

        String result = commonActions.getTextFromElement(mockElement);

        assertNotNull(result);
        assertEquals(expectedText, result);
        verify(mockWait, times(1)).until(any(Function.class));
        verify(mockElement, times(1)).getText();
    }

    @Test
    @DisplayName("getTextFromElement: Deve lançar exceção com elemento null")
    void testGetTextFromElement_WithNullElement_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.getTextFromElement(null));
        verify(mockWait, never()).until(any());
    }

    @Test
    @DisplayName("getTextFromElement: Deve propagar exceção quando getText falha")
    void testGetTextFromElement_WhenGetTextFails_ShouldThrowException() {
        when(mockWait.until(any(Function.class))).thenReturn(mockElement);
        when(mockElement.getText())
                .thenThrow(new WebDriverException("Elemento stale"));

        assertThrows(WebDriverException.class,
                () -> commonActions.getTextFromElement(mockElement));
        verify(mockWait, times(1)).until(any(Function.class));
        verify(mockElement, times(1)).getText();
    }

    //  TESTES swipe (4)

    @Test
    @DisplayName("swipe (padrão): Deve executar swipe W3C com sucesso")
    void testSwipe_Default_ShouldExecuteW3CSwipe() {
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.window()).thenReturn(mockWindow);
        when(mockWindow.getSize()).thenReturn(new Dimension(1080, 1920));
        doNothing().when(mockDriver).perform(any(List.class));

        assertDoesNotThrow(() -> commonActions.swipe());

        verify(mockDriver, times(1)).perform(any(List.class));
    }

    @Test
    @DisplayName("swipe (padrão): Deve lançar exceção quando dimensões são null")
    void testSwipe_Default_WhenDimensionsNull_ShouldThrowException() {
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.window()).thenReturn(mockWindow);
        when(mockWindow.getSize()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> commonActions.swipe());
    }

    @Test
    @DisplayName("swipe (custom): Deve executar swipe W3C customizado")
    void testSwipe_Custom_WithValidCoordinates_ShouldExecuteW3CSwipe() {
        doNothing().when(mockDriver).perform(any(List.class));

        assertDoesNotThrow(() -> commonActions.swipe(500, 1500, 500, 300));

        verify(mockDriver, times(1)).perform(any(List.class));
    }

    @Test
    @DisplayName("swipe (custom): Deve lançar exceção com coordenadas negativas")
    void testSwipe_Custom_WithNegativeCoordinates_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.swipe(-10, 1500, 500, 300));
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.swipe(500, -10, 500, 300));
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.swipe(500, 1500, -10, 300));
        assertThrows(IllegalArgumentException.class,
                () -> commonActions.swipe(500, 1500, 500, -10));
    }
}