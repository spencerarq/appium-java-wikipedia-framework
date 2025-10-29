package com.wikipedia.utils;

import com.wikipedia.commons.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Classe utilitária para auxiliar nos testes, como tirar screenshots.
 * @author Renato Spencer
 * @version 1.0
 * @since 2025-10-29
 */
public class TestHelper {

    private static final Logger logger = LoggerFactory.getLogger(TestHelper.class);
    // Diretório base para salvar screenshots (dentro do target para limpeza automática)
    private static final String SCREENSHOT_DIR = "target/screenshots/";

    private final DriverFactory driverFactory;

    //Construtor padrão. Obtém a instância da DriverFactory.

    public TestHelper() {
        this.driverFactory = DriverFactory.getInstance();
    }

    /**
     * Tira um screenshot da tela atual do driver.
     * Salva o arquivo em target/screenshots/ com um nome único.
     *
     * @param testName Nome do teste (usado para nomear o arquivo). Se nulo ou vazio, usa UUID.
     * @return O caminho absoluto do arquivo de screenshot salvo, ou null se ocorrer erro.
     */
    public String takeScreenshot(String testName) {
        AppiumDriver driver = driverFactory.getDriver();

        // Validação 1: Driver existe?
        if (driver == null) {
            logger.error("Não é possível tirar screenshot: Driver é null.");
            return null;
        }

        // Validação 2: Driver suporta screenshots?
        if (!(driver instanceof TakesScreenshot)) {
            logger.error("Não é possível tirar screenshot: Driver da classe {} não suporta TakesScreenshot.", driver.getClass().getName());
            return null;
        }

        File screenshotFile = null;
        try {
            // Captura o screenshot como um arquivo temporário
            screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            logger.debug("Screenshot capturado temporariamente em: {}", screenshotFile.getAbsolutePath());

            // Cria o diretório de destino se não existir
            Path targetDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(targetDir); // Cria diretórios pai se necessário

            // Gera um nome de arquivo único
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String safeTestName = (testName == null || testName.trim().isEmpty()) ?
                    UUID.randomUUID().toString().substring(0, 8) :
                    testName.replaceAll("[^a-zA-Z0-9.-]", "_"); // Sanitiza o nome do teste
            String fileName = String.format("%s_%s.png", safeTestName, timestamp);
            Path targetPath = targetDir.resolve(fileName);

            // Copia o arquivo temporário para o destino final
            Files.copy(screenshotFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Screenshot salvo com sucesso em: {}", targetPath.toAbsolutePath());
            return targetPath.toAbsolutePath().toString();

        } catch (WebDriverException e) {
            logger.error("Erro do WebDriver ao tentar capturar screenshot: {}", e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error("Erro de I/O ao tentar salvar screenshot: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Erro inesperado ao tirar/salvar screenshot: {}", e.getMessage(), e);
            return null;
        } finally {
            // Tenta deletar o arquivo temporário do Selenium/Appium (se foi criado)
            if (screenshotFile != null && screenshotFile.exists()) {
                if (!screenshotFile.delete()) {
                    logger.warn("Não foi possível deletar o arquivo de screenshot temporário: {}", screenshotFile.getAbsolutePath());
                }
            }
        }
    }
}