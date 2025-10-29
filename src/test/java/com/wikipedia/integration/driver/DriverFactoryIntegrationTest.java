package com.wikipedia.integration.driver;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.utils.ConfigReader;
import com.wikipedia.utils.LoggerHelper;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

@Tag("integration")
@ExtendWith(MockitoExtension.class)
class DriverFactoryIntegrationTest {

    // @Spy permite chamar os métodos reais, exceto os que mockamos.
    @Spy
    private DriverFactory driverFactory = DriverFactory.getInstance();

    @Mock
    private AppiumDriver mockDriver;

    private MockedStatic<ConfigReader> mockedConfig;
    private MockedStatic<LoggerHelper> mockedLogger;

    @BeforeEach
    void setUp() {
        // Configuração padrão dos mocks estáticos
        mockedConfig = mockStatic(ConfigReader.class);
        mockedLogger = mockStatic(LoggerHelper.class);

        // Configuração mínima para os testes (Usa sintaxe Java 8/17)
        mockedConfig.when(ConfigReader::getAppiumServerUrl).thenReturn("http://127.0.0.1:4723");
        mockedConfig.when(ConfigReader::getPlatformName).thenReturn("Android");
        mockedConfig.when(ConfigReader::getAutomationName).thenReturn("UiAutomator2");
        mockedConfig.when(ConfigReader::getDeviceName).thenReturn("Emulator");
        mockedConfig.when(ConfigReader::getAppPackage).thenReturn("pkg");
        mockedConfig.when(ConfigReader::getAppWaitActivity).thenReturn("act");
        mockedConfig.when(ConfigReader::getImplicitWait).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        driverFactory.quitDriver();
        mockedConfig.close();
        mockedLogger.close();
    }

    /**
     * Testa o caminho feliz: inicialização bem-sucedida do driver.
     */
    @Test
    void testInitializeAndroidDriver_Success() {
        // 1. Configurar Mocks (driver.manage().timeouts()...)
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.timeouts()).thenReturn(mockTimeouts);
        when(mockDriver.getSessionId()).thenReturn(new SessionId("mock-session-123"));

        // 2. Configurar o @Spy: Intercepta a criação real
        doReturn(mockDriver).when(driverFactory).createDriver(any(URL.class), any());

        // 3. Executar o método
        AppiumDriver driver = driverFactory.initializeAndroidDriver();

        // 4. Asserts
        assertNotNull(driver);
        assertSame(mockDriver, driver, "O driver retornado deve ser o mock");
        assertSame(mockDriver, driverFactory.getDriver(), "O driver no ThreadLocal deve ser o mock");

        // Verificar se o implicit wait foi chamado no mock
        verify(mockDriver.manage().timeouts()).implicitlyWait(Duration.ofSeconds(10));

        // Verificar logs
        mockedLogger.verify(() -> LoggerHelper.info(eq("Implicit wait definido para {} segundos."), eq(10)));
        mockedLogger.verify(() -> LoggerHelper.info(eq("AndroidDriver inicializado com sucesso. Session ID: {}"), any(SessionId.class)));
    }

    /**
     * Testa o cenário em que o servidor Appium está offline
     */
    @Test
    void testInitializeDriver_ServerOffline() {
        // 1. Configurar o @Spy para lançar uma exceção
        doThrow(new SessionNotCreatedException("Simulação: Servidor Appium offline"))
                .when(driverFactory).createDriver(any(URL.class), any());

        // 2. Executar e Verificar Exceção
        RuntimeException ex = assertThrows(RuntimeException.class,
                driverFactory::initializeAndroidDriver,
                "Deveria lançar RuntimeException se a sessão não for criada");

        assertTrue(ex.getMessage().contains("Não foi possível iniciar uma nova sessão"), "Mensagem de erro deve ser amigável");
        assertTrue(ex.getCause() instanceof SessionNotCreatedException, "Causa original deve ser preservada");

        // 3. Garantir que nada foi para o ThreadLocal
        assertNull(driverFactory.getDriver(), "Driver não deve ser salvo no ThreadLocal em caso de falha");
    }

    /**
     * Testa o cenário em que a URL no config.properties é inválida.
     */
    @Test
    void testInitializeDriver_InvalidURL() {
        // 1. Configurar Mocks (Sobrescrever o @BeforeEach)
        mockedConfig.when(ConfigReader::getAppiumServerUrl).thenReturn("nao_e_uma_url");

        // 2. Executar e Verificar Exceção
        RuntimeException ex = assertThrows(RuntimeException.class,
                driverFactory::initializeAndroidDriver,
                "Deveria lançar RuntimeException se a URL for malformada");

        assertTrue(ex.getMessage().contains("URL do Appium Server é inválida"), "Mensagem de erro deve indicar problema na URL");
        assertTrue(ex.getCause() instanceof MalformedURLException, "Causa original deve ser MalformedURLException");

        // 3. Garantir que nada foi para o ThreadLocal
        assertNull(driverFactory.getDriver(), "Driver não deve ser salvo no ThreadLocal em caso de falha");
    }

    /**
     * Testa se, ao chamar initializeAndroidDriver() duas vezes,
     * o método createDriver() é chamado apenas uma vez.
     */
    @Test
    void testInitializeDriver_ReturnsExistingDriver() {
        // 1. Configurar Mocks (como no teste de sucesso)
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.timeouts()).thenReturn(mockTimeouts);
        when(mockDriver.getSessionId()).thenReturn(new SessionId("mock-session-123"));

        // 2. Configurar o @Spy
        doReturn(mockDriver).when(driverFactory).createDriver(any(URL.class), any());

        // 3. Primeira chamada
        AppiumDriver driver1 = driverFactory.initializeAndroidDriver();

        // 4. Segunda chamada
        AppiumDriver driver2 = driverFactory.initializeAndroidDriver();

        // 5. Asserts
        assertNotNull(driver1);
        assertSame(driver1, driver2, "Ambas as chamadas devem retornar a MESMA instância");
        // Verifica que o método createDriver() foi chamado APENAS UMA VEZ
        verify(driverFactory, times(1)).createDriver(any(URL.class), any());
    }

    /**
     * Testa explicitamente os métodos getDriver() e quitDriver().
     */
    @Test
    void testGetDriverAndQuitDriver() {
        // 1. Configura Mocks
        when(mockDriver.getSessionId()).thenReturn(new SessionId("mock-session-456"));
        WebDriver.Options mockOptions = mock(WebDriver.Options.class);
        WebDriver.Timeouts mockTimeouts = mock(WebDriver.Timeouts.class);
        when(mockDriver.manage()).thenReturn(mockOptions);
        when(mockOptions.timeouts()).thenReturn(mockTimeouts);

        // 2. Configura o @Spy
        doReturn(mockDriver).when(driverFactory).createDriver(any(URL.class), any());

        // 3. Inicializa
        driverFactory.initializeAndroidDriver();

        // 4. Testa getDriver()
        assertSame(mockDriver, driverFactory.getDriver(), "getDriver() deve retornar o driver ativo");

        // 5. Testa quitDriver()
        driverFactory.quitDriver();

        // 6. Verifica se o mock.quit() foi chamado
        verify(mockDriver, times(1)).quit();

        // 7. Verifica se o ThreadLocal foi limpo
        assertNull(driverFactory.getDriver(), "getDriver() deve retornar null após quitDriver()");

        // 8. Testa quitDriver() de novo (não deve fazer nada, nem lançar exceção)
        assertDoesNotThrow(driverFactory::quitDriver, "Chamar quit() em um driver nulo não deve falhar");
        verify(mockDriver, times(1)).quit(); // A contagem de chamadas deve permanecer 1
    }
}