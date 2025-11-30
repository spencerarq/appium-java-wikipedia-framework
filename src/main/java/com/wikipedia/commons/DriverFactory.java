package com.wikipedia.commons;

import com.wikipedia.utils.ConfigReader;
import com.wikipedia.utils.LoggerHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {

    private static final DriverFactory instance = new DriverFactory();
    private final ThreadLocal<AppiumDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverFactory() {}

    public static DriverFactory getInstance() {
        return instance;
    }

    public synchronized AppiumDriver initializeAndroidDriver() {
        if (driverThreadLocal.get() != null) {
            return driverThreadLocal.get();
        }

        try {
            // MANTENHA SIMPLES: Sem capabilities de vídeo aqui.
            // O controle será feito programaticamente pelo VideoRecordingListener -> TestHelper.
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName(ConfigReader.getPlatformName())
                    .setAutomationName(ConfigReader.getAutomationName())
                    .setDeviceName(ConfigReader.getDeviceName())
                    .setAppPackage(ConfigReader.getAppPackage())
                    .setAppWaitActivity(ConfigReader.getAppWaitActivity())
                    .setNewCommandTimeout(Duration.ofSeconds(3600))
                    .setNoReset(false);

            URL appiumServerUrl = new URL(ConfigReader.getAppiumServerUrl());
            AppiumDriver driver = new AndroidDriver(appiumServerUrl, options);

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));

            driverThreadLocal.set(driver);
            LoggerHelper.info("AndroidDriver inicializado. Session ID: {}", driver.getSessionId());
            return driver;
        } catch (Exception e) {
            LoggerHelper.error("Falha ao inicializar AndroidDriver", e);
            throw new RuntimeException(e);
        }
    }

    public synchronized AppiumDriver getDriver() {
        return driverThreadLocal.get();
    }

    public synchronized void setDriver(AppiumDriver driver) {
        driverThreadLocal.set(driver);
    }

    public synchronized void quitDriver() {
        AppiumDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                LoggerHelper.info("Driver encerrado.");
            } catch (Exception e) {
                LoggerHelper.error("Erro ao encerrar driver.", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}