package com.wikipedia.unit;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.utils.ConfigReader;
import com.wikipedia.utils.LoggerHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Testes Unitários para a classe DriverFactory.
 * Foco em testar a lógica interna (criação de Options, injeção de Mocks)
 * sem dependência externa (Appium, Emulador).
 */
@ExtendWith(MockitoExtension.class)
class DriverFactoryUnitTest {

    // @Spy para testar a instância real do Singleton
    @Spy
    private DriverFactory driverFactory = DriverFactory.getInstance();

    @Mock
    private AppiumDriver mockDriver;

    // Mocks para os helpers estáticos (ConfigReader, LoggerHelper)
    private MockedStatic<ConfigReader> mockedConfig;
    private MockedStatic<LoggerHelper> mockedLogger;

    @BeforeEach
    void setUp() {
        // Mocka as classes estáticas
        mockedConfig = mockStatic(ConfigReader.class);
        mockedLogger = mockStatic(LoggerHelper.class);
    }

    @AfterEach
    void tearDown() {
        // Limpa a ThreadLocal e fecha os mocks estáticos
        driverFactory.quitDriver();
        mockedConfig.close();
        mockedLogger.close();
    }

    /**
     * Testa o método extraído createAndroidOptions().
     * Garante que as propriedades lidas do ConfigReader são
     * corretamente aplicadas ao objeto UiAutomator2Options.
     */
    @Test
    @DisplayName("Deve criar UiAutomator2Options com as propriedades corretas")
    void testCreateAndroidOptions_ReturnsValidOptions() {
        // 1. Configuração (Arrange)
        // Simula o que o ConfigReader retornaria
        mockedConfig.when(ConfigReader::getPlatformName).thenReturn("Android");
        mockedConfig.when(ConfigReader::getAutomationName).thenReturn("UiAutomator2");
        mockedConfig.when(ConfigReader::getDeviceName).thenReturn("EmulatorTest");
        mockedConfig.when(ConfigReader::getAppPackage).thenReturn("com.test.pkg");
        mockedConfig.when(ConfigReader::getAppWaitActivity).thenReturn(".TestActivity");
        // Nota: O timeout (3600s) está "hardcoded" no método refatorado

        // 2. Execução (Act)
        // Chama o método protegido que queremos testar
        UiAutomator2Options options = driverFactory.createAndroidOptions();

        // 3. Verificação (Assert)
        assertNotNull(options);
        assertEquals("ANDROID", options.getPlatformName().toString());
        assertEquals("UiAutomator2", options.getAutomationName().get());
        assertEquals("EmulatorTest", options.getDeviceName().get());
        assertEquals("com.test.pkg", options.getAppPackage().get());
        assertEquals(".TestActivity", options.getAppWaitActivity().get());
        assertEquals(Duration.ofSeconds(3600), options.getNewCommandTimeout().get());
    }

    /**
     * Testa método setDriver() e o getDriver().
     * Garante que a injeção de mock funciona e que o driver
     * é armazenado e recuperado corretamente da ThreadLocal.
     */
    @Test
    @DisplayName("Deve injetar e recuperar um mock driver via setDriver/getDriver")
    void testSetDriverAndGetDriver() {
        // 1. Configuração (Arrange)
        // O mockDriver é injetado pelo @Mock

        // 2. Execução (Act)
        // Injeta o mock driver
        driverFactory.setDriver(mockDriver);
        // Tenta recuperar o driver
        AppiumDriver retrievedDriver = driverFactory.getDriver();

        // 3. Verificação (Assert)
        assertNotNull(retrievedDriver);
        // Garante que o driver recuperado é EXATAMENTE a instância mockada
        assertSame(mockDriver, retrievedDriver, "O driver recuperado deve ser a instância mockada");
    }
}