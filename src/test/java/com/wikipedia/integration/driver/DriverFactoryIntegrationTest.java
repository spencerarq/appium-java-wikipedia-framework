package com.wikipedia.integration.driver;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.utils.ConfigReader;
import com.wikipedia.utils.LoggerHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.SessionId;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para DriverFactory.
 * Testa o fluxo completo de inicialização do AndroidDriver COM dependências mockadas.
 *
 * Diferença dos testes unitários:
 * - Testa a integração entre DriverFactory, ConfigReader, e AndroidDriver
 * - Usa MockedConstruction para simular a criação real do AndroidDriver
 * - Valida o fluxo completo de initializeAndroidDriver()
 * - Testa cenários de erro (servidor offline, URL inválida)
 *
 * @author Renato Spencer
 * @version 2.0
 * @since 2025-11-30
 */
@Tag("integration")
@ExtendWith(MockitoExtension.class)
class DriverFactoryIntegrationTest {

    @Spy
    private DriverFactory driverFactory = DriverFactory.getInstance();

    private MockedStatic<ConfigReader> mockedConfig;
    private MockedStatic<LoggerHelper> mockedLogger;

    @BeforeEach
    void setUp() {
        // Mock das classes estáticas
        mockedConfig = mockStatic(ConfigReader.class);
        mockedLogger = mockStatic(LoggerHelper.class);

        // Configuração padrão dos mocks (simula config.properties)
        mockedConfig.when(ConfigReader::getAppiumServerUrl).thenReturn("http://127.0.0.1:4723");
        mockedConfig.when(ConfigReader::getPlatformName).thenReturn("Android");
        mockedConfig.when(ConfigReader::getAutomationName).thenReturn("UiAutomator2");
        mockedConfig.when(ConfigReader::getDeviceName).thenReturn("emulator-5554");
        mockedConfig.when(ConfigReader::getAppPackage).thenReturn("org.wikipedia");
        mockedConfig.when(ConfigReader::getAppWaitActivity).thenReturn("org.wikipedia.main.MainActivity");
        mockedConfig.when(ConfigReader::getImplicitWait).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        driverFactory.quitDriver();
        mockedConfig.close();
        mockedLogger.close();
    }

    /**
     * TESTE DE INTEGRAÇÃO: Caminho feliz - inicialização bem-sucedida.
     * Testa o fluxo completo de initializeAndroidDriver() com todas as dependências.
     */
    @Test
    void testInitializeAndroidDriver_Success() {
        // Mock das dependências do driver
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        SessionId mockSessionId = new SessionId("integration-test-session-123");

        // Intercepta a construção do AndroidDriver (simula criação real)
        try (MockedConstruction<AndroidDriver> mockedDriver = mockConstruction(
                AndroidDriver.class,
                (mock, context) -> {
                    // Valida os argumentos passados ao construtor
                    assertEquals(2, context.arguments().size(),
                            "AndroidDriver deve ser construído com 2 argumentos: URL e Options");

                    // Valida URL
                    URL capturedUrl = (URL) context.arguments().get(0);
                    assertEquals("http://127.0.0.1:4723/", capturedUrl.toString(),
                            "URL deve ser a configurada no ConfigReader");

                    // Valida Options
                    UiAutomator2Options capturedOptions = (UiAutomator2Options) context.arguments().get(1);
                    assertNotNull(capturedOptions, "Options não deve ser null");
                    assertEquals("Android", capturedOptions.getPlatformName(), "PlatformName deve ser Android");

                    // Configura o comportamento do mock
                    when(mock.manage()).thenReturn(mockOptions);
                    when(mockOptions.timeouts()).thenReturn(mockTimeouts);
                    when(mock.getSessionId()).thenReturn(mockSessionId);
                    when(mockTimeouts.implicitlyWait(any(Duration.class))).thenReturn(mockTimeouts);
                })) {

            // Executa o método (integração completa)
            AppiumDriver driver = driverFactory.initializeAndroidDriver();

            // Asserts
            assertNotNull(driver, "Driver não deve ser null");
            assertTrue(driver instanceof AndroidDriver, "Driver deve ser AndroidDriver");
            assertSame(driver, driverFactory.getDriver(), "Driver deve estar armazenado no ThreadLocal");

            // Verifica que AndroidDriver foi construído exatamente 1 vez
            assertEquals(1, mockedDriver.constructed().size(),
                    "Deve ter construído exatamente 1 AndroidDriver");

            AndroidDriver constructedDriver = mockedDriver.constructed().get(0);

            // Verifica que implicit wait foi configurado
            verify(constructedDriver.manage().timeouts()).implicitlyWait(Duration.ofSeconds(10));

            // Verifica logs de sucesso
            mockedLogger.verify(() -> LoggerHelper.info(
                    eq("AndroidDriver inicializado. Session ID: {}"),
                    eq(mockSessionId)
            ));
        }
    }

    /**
     * TESTE DE INTEGRAÇÃO: Servidor Appium offline.
     * Simula exceção ao tentar conectar com o servidor.
     */
    @Test
    void testInitializeAndroidDriver_ServerOffline() {
        // Simula exceção ao tentar criar o AndroidDriver
        try (MockedConstruction<AndroidDriver> mockedDriver = mockConstruction(
                AndroidDriver.class,
                (mock, context) -> {
                    throw new SessionNotCreatedException(
                            "Could not start a new session. Possible causes are invalid address of the remote server " +
                                    "or browser start-up failure.\n" +
                                    "Build info: version: '4.0.0'\n" +
                                    "System info: host: 'localhost'\n" +
                                    "Driver info: driver.version: AndroidDriver"
                    );
                })) {

            // Executa e verifica exceção
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> driverFactory.initializeAndroidDriver(),
                    "Deve lançar RuntimeException quando Appium Server está offline"
            );

            // Verifica a causa raiz
            assertNotNull(ex.getCause(), "Exceção deve ter uma causa");
            assertTrue(ex.getCause() instanceof SessionNotCreatedException,
                    "Causa deve ser SessionNotCreatedException");

            // Verifica que o driver NÃO foi salvo no ThreadLocal
            assertNull(driverFactory.getDriver(),
                    "Driver não deve estar no ThreadLocal após falha de conexão");

            // Verifica que nenhum AndroidDriver foi construído com sucesso
            assertEquals(0, mockedDriver.constructed().size(),
                    "Nenhum AndroidDriver deve ter sido construído com sucesso");

            // Verifica log de erro
            mockedLogger.verify(() -> LoggerHelper.error(
                    eq("Falha ao inicializar AndroidDriver"),
                    any(RuntimeException.class)
            ));
        }
    }

    /**
     * TESTE DE INTEGRAÇÃO: URL do Appium Server inválida.
     */
    @Test
    void testInitializeAndroidDriver_InvalidURL() {
        // Sobrescreve a configuração da URL para uma URL inválida
        mockedConfig.when(ConfigReader::getAppiumServerUrl).thenReturn("this-is-not-a-valid-url");

        // Executa e verifica exceção
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> driverFactory.initializeAndroidDriver(),
                "Deve lançar RuntimeException para URL malformada"
        );

        // Verifica a causa raiz
        assertNotNull(ex.getCause(), "Exceção deve ter uma causa");
        assertTrue(ex.getCause() instanceof MalformedURLException,
                "Causa deve ser MalformedURLException");

        // Verifica que o driver NÃO foi salvo no ThreadLocal
        assertNull(driverFactory.getDriver(),
                "Driver não deve estar no ThreadLocal após erro de URL");

        // Verifica log de erro
        mockedLogger.verify(() -> LoggerHelper.error(
                eq("Falha ao inicializar AndroidDriver"),
                any(RuntimeException.class)
        ));
    }

    /**
     * TESTE DE INTEGRAÇÃO: Chamadas múltiplas retornam a mesma instância.
     * Verifica o comportamento de cache do driver no ThreadLocal.
     */
    @Test
    void testInitializeAndroidDriver_ReturnsExistingDriver() {
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        SessionId mockSessionId = new SessionId("cached-session-456");

        try (MockedConstruction<AndroidDriver> mockedDriver = mockConstruction(
                AndroidDriver.class,
                (mock, context) -> {
                    when(mock.manage()).thenReturn(mockOptions);
                    when(mockOptions.timeouts()).thenReturn(mockTimeouts);
                    when(mock.getSessionId()).thenReturn(mockSessionId);
                    when(mockTimeouts.implicitlyWait(any(Duration.class))).thenReturn(mockTimeouts);
                })) {

            // Primeira chamada
            AppiumDriver driver1 = driverFactory.initializeAndroidDriver();

            // Segunda chamada (deve retornar o mesmo driver)
            AppiumDriver driver2 = driverFactory.initializeAndroidDriver();

            // Terceira chamada (deve retornar o mesmo driver)
            AppiumDriver driver3 = driverFactory.initializeAndroidDriver();

            // Asserts
            assertNotNull(driver1);
            assertNotNull(driver2);
            assertNotNull(driver3);

            assertSame(driver1, driver2, "Segunda chamada deve retornar a MESMA instância");
            assertSame(driver2, driver3, "Terceira chamada deve retornar a MESMA instância");

            // Verifica que apenas 1 AndroidDriver foi construído
            assertEquals(1, mockedDriver.constructed().size(),
                    "Deve construir apenas 1 AndroidDriver, mesmo com 3 chamadas");
        }
    }

    /**
     * TESTE DE INTEGRAÇÃO: quitDriver() encerra corretamente o driver.
     */
    @Test
    void testQuitDriver_ClosesDriverProperly() {
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        SessionId mockSessionId = new SessionId("quit-test-session-789");

        try (MockedConstruction<AndroidDriver> mockedDriver = mockConstruction(
                AndroidDriver.class,
                (mock, context) -> {
                    when(mock.manage()).thenReturn(mockOptions);
                    when(mockOptions.timeouts()).thenReturn(mockTimeouts);
                    when(mock.getSessionId()).thenReturn(mockSessionId);
                    when(mockTimeouts.implicitlyWait(any(Duration.class))).thenReturn(mockTimeouts);
                    doNothing().when(mock).quit();
                })) {

            // Inicializa
            driverFactory.initializeAndroidDriver();
            AndroidDriver constructedDriver = mockedDriver.constructed().get(0);

            // Verifica que o driver existe
            assertNotNull(driverFactory.getDriver(), "Driver deve existir antes de quitDriver()");

            // Chama quitDriver()
            driverFactory.quitDriver();

            // Verifica que quit() foi chamado no driver mock
            verify(constructedDriver, times(1)).quit();

            // Verifica que o ThreadLocal foi limpo
            assertNull(driverFactory.getDriver(), "ThreadLocal deve estar limpo após quitDriver()");

            // Verifica log de sucesso
            mockedLogger.verify(() -> LoggerHelper.info("Driver encerrado."));
        }
    }

    /**
     * TESTE DE INTEGRAÇÃO: quitDriver() quando driver.quit() lança exceção.
     */
    @Test
    void testQuitDriver_HandlesExceptionGracefully() {
        AndroidDriver mockDriver = mock(AndroidDriver.class);

        // Configura o mock para lançar exceção ao chamar quit()
        doThrow(new RuntimeException("Simulação: Erro ao fechar sessão do driver"))
                .when(mockDriver).quit();

        // Configura o driver manualmente no ThreadLocal
        driverFactory.setDriver(mockDriver);

        // Verifica que o driver está configurado
        assertNotNull(driverFactory.getDriver());

        // quitDriver() NÃO deve propagar a exceção
        assertDoesNotThrow(() -> driverFactory.quitDriver(),
                "quitDriver() não deve propagar exceção ao fechar driver");

        // Verifica que o ThreadLocal foi limpo mesmo com erro
        assertNull(driverFactory.getDriver(),
                "ThreadLocal deve ser limpo mesmo quando quit() lança exceção");

        // Verifica log de erro
        mockedLogger.verify(() -> LoggerHelper.error(
                eq("Erro ao encerrar driver."),
                any(RuntimeException.class)
        ));
    }

    /**
     * TESTE DE INTEGRAÇÃO: quitDriver() sem driver não falha.
     */
    @Test
    void testQuitDriver_NoDriverDoesNotFail() {
        // Garante que não há driver
        assertNull(driverFactory.getDriver());

        // Não deve lançar exceção
        assertDoesNotThrow(() -> driverFactory.quitDriver(),
                "quitDriver() deve ser seguro quando não há driver");

        // Deve continuar retornando null
        assertNull(driverFactory.getDriver());
    }
}