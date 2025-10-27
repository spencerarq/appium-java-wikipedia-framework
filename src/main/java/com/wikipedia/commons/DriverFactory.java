package com.wikipedia.commons;

import com.wikipedia.utils.ConfigReader;
import com.wikipedia.utils.LoggerHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.SessionNotCreatedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Classe Factory (Singleton) responsável por inicializar, gerenciar e encerrar
 * as instâncias do AppiumDriver.
 * Utiliza ThreadLocal para garantir o isolamento da sessão do driver
 * em execuções paralelas.
 */
public class DriverFactory {

    // --- Refatoração Singleton ---
    private static final DriverFactory instance = new DriverFactory();

    // Construtor privado para garantir o Singleton
    private DriverFactory() {}

    /**
     * Obtém a instância única da DriverFactory.
     *
     * @return A instância do Singleton.
     */
    public static DriverFactory getInstance() {
        return instance;
    }
    // --- Fim da Refatoração Singleton ---

    private final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * Inicializa e retorna um AppiumDriver (especificamente AndroidDriver)
     * com base nas configurações do ConfigReader.
     * Se um driver já existir para a thread atual, ele é retornado.
     *
     * @return A instância do AppiumDriver para a thread atual.
     * @throws RuntimeException Se a URL do Appium for malformada ou se a sessão
     * não puder ser criada (ex: servidor offline).
     */
    public synchronized AppiumDriver initializeAndroidDriver() {
        if (driverThreadLocal.get() != null) {
            LoggerHelper.debug("Driver já inicializado para esta thread. Retornando instância existente.");
            return driverThreadLocal.get();
        }

        LoggerHelper.info("Iniciando AndroidDriver...");
        AppiumDriver driver;
        URL serverUrl;

        try {
            serverUrl = new URL(ConfigReader.getAppiumServerUrl());
        } catch (MalformedURLException e) {
            LoggerHelper.error("URL do Appium Server é inválida: " + ConfigReader.getAppiumServerUrl(), e);
            throw new RuntimeException("URL do Appium Server é inválida: " + ConfigReader.getAppiumServerUrl(), e);
        }

        try {
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName(ConfigReader.getPlatformName())
                    .setAutomationName(ConfigReader.getAutomationName())
                    .setDeviceName(ConfigReader.getDeviceName())
                    .setAppPackage(ConfigReader.getAppPackage())
                    .setAppWaitActivity(ConfigReader.getAppWaitActivity())
                    .setNewCommandTimeout(Duration.ofSeconds(3600));

            // 3. Inicializar o Driver (MÉTODO EXTRAÍDO)
            // Este é o método que vamos "espionar" (Spy) no teste unitário
            driver = createDriver(serverUrl, options);

            // 4. Configurar Implicit Wait
            int implicitWaitSeconds = ConfigReader.getImplicitWait();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitSeconds));
            LoggerHelper.info("Implicit wait definido para {} segundos.", implicitWaitSeconds);

            // 5. Armazenar o driver na ThreadLocal
            driverThreadLocal.set(driver);
            LoggerHelper.info("AndroidDriver inicializado com sucesso. Session ID: {}", driver.getSessionId());

        } catch (SessionNotCreatedException e) {
            LoggerHelper.error("Falha ao criar sessão do Appium. Verifique se o servidor está online e as capabilities estão corretas.", e);
            throw new RuntimeException("Não foi possível iniciar uma nova sessão com o Appium Server. Causa: " + e.getMessage(), e);
        } catch (Exception e) {
            LoggerHelper.error("Erro inesperado ao inicializar o driver.", e);
            throw new RuntimeException("Erro inesperado ao inicializar o driver.", e);
        }

        return driver;
    }

    /**
     * [PROTEGIDO PARA TESTES]
     * Extrai a criação real do driver para permitir que seja mockado/espionado.
     *
     * @param serverUrl URL do Appium
     * @param options   Capabilities
     * @return Um novo AppiumDriver
     */
    public AppiumDriver createDriver(URL serverUrl, UiAutomator2Options options) {
        return new AndroidDriver(serverUrl, options);
    }

    /**
     * Retorna a instância do AppiumDriver associada à thread atual.
     *
     * @return O AppiumDriver da thread atual, ou null se não houver.
     */
    public synchronized AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * Encerra a sessão do driver (quit()) e remove a instância da ThreadLocal
     * para a thread atual.
     */
    public synchronized void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                LoggerHelper.info("Encerrando driver... Session ID: {}", driver.getSessionId());
                driver.quit();
            } catch (Exception e) {
                LoggerHelper.error("Erro ao tentar encerrar o driver. A sessão pode já estar fechada.", e);
            } finally {
                driverThreadLocal.remove();
                LoggerHelper.info("Driver removido da ThreadLocal.");
            }
        } else {
            LoggerHelper.debug("Nenhum driver para encerrar na thread atual.");
        }
    }
}