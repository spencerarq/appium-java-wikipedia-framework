package com.wikipedia.utils;

import com.wikipedia.commons.DriverFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
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
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Classe utilitária para auxiliar nos testes: screenshots e gravação de vídeo.
 * @author Renato Spencer
 * @version 2.0
 * @since 2025-10-29
 */
public class TestHelper {

    private static final Logger logger = LoggerFactory.getLogger(TestHelper.class);
    private static final String SCREENSHOT_DIR = "target/screenshots/";
    private static final String VIDEO_DIR = "target/videos/";

    private final DriverFactory driverFactory;
    private boolean isRecording = false;

    public TestHelper() {
        this.driverFactory = DriverFactory.getInstance();
    }

    // ====================================================================
    // GRAVAÇÃO DE VÍDEO
    // ====================================================================

    /**
     * Inicia a gravação de vídeo do teste.
     * DEVE ser chamado no @BeforeMethod (ou via Listener).
     *
     * @return true se iniciou com sucesso, false caso contrário
     */
    public boolean startVideoRecording() {
        AppiumDriver driver = driverFactory.getDriver();

        if (driver == null) {
            logger.error("Não é possível iniciar gravação: Driver é null");
            return false;
        }

        if (!(driver instanceof AndroidDriver)) {
            logger.warn("Driver não é AndroidDriver. Gravação de vídeo não suportada.");
            return false;
        }

        try {
            AndroidDriver androidDriver = (AndroidDriver) driver;
            androidDriver.startRecordingScreen();
            isRecording = true;
            logger.info("Gravação de vídeo iniciada com sucesso");
            return true;

        } catch (Exception e) {
            logger.error("Erro ao iniciar gravação de vídeo: {}", e.getMessage(), e);
            isRecording = false;
            return false;
        }
    }

    /**
     * Para a gravação e salva o vídeo.
     * DEVE ser chamado no @AfterMethod (ou via Listener).
     *
     * @param testName Nome do teste (usado no nome do arquivo)
     * @return Caminho absoluto do vídeo salvo, ou null se falhar
     */
    public String stopAndSaveVideo(String testName) {
        AppiumDriver driver = driverFactory.getDriver();

        if (driver == null) {
            logger.error("Não é possível parar gravação: Driver é null");
            return null;
        }

        if (!isRecording) {
            logger.debug("Nenhuma gravação em andamento para parar");
            return null;
        }

        if (!(driver instanceof AndroidDriver)) {
            logger.warn("Driver não é AndroidDriver. Não é possível parar gravação.");
            return null;
        }

        try {
            AndroidDriver androidDriver = (AndroidDriver) driver;

            String base64Video = androidDriver.stopRecordingScreen();
            isRecording = false;

            if (base64Video == null || base64Video.isEmpty()) {
                logger.error("Vídeo retornado está vazio ou null");
                return null;
            }

            byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            logger.info("Vídeo capturado: {} bytes", videoBytes.length);

            Path videoDir = Paths.get(VIDEO_DIR);
            Files.createDirectories(videoDir);

            String sanitizedTestName = sanitizeFileName(testName);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String videoFileName = String.format("%s_%s.mp4", sanitizedTestName, timestamp);
            Path videoPath = videoDir.resolve(videoFileName);

            Files.write(videoPath, videoBytes);

            String absolutePath = videoPath.toAbsolutePath().toString();
            logger.info("Vídeo salvo com sucesso: {}", absolutePath);

            return absolutePath;

        } catch (IllegalArgumentException e) {
            logger.error("Erro ao decodificar Base64 do vídeo: {}", e.getMessage(), e);
            isRecording = false;
            return null;

        } catch (IOException e) {
            logger.error("Erro de I/O ao salvar vídeo: {}", e.getMessage(), e);
            isRecording = false;
            return null;

        } catch (Exception e) {
            logger.error("Erro inesperado ao parar/salvar vídeo: {}", e.getMessage(), e);
            isRecording = false;
            return null;
        }
    }

    /**
     * Verifica se há uma gravação em andamento.
     */
    public boolean isRecording() {
        return isRecording;
    }

    // ====================================================================
    // SCREENSHOT
    // ====================================================================

    /**
     * Tira um screenshot da tela atual do driver.
     * Salva o arquivo em target/screenshots/ com um nome único.
     *
     * @param testName Nome do teste (usado para nomear o arquivo). Se nulo ou vazio, usa UUID.
     * @return O caminho absoluto do arquivo de screenshot salvo, ou null se ocorrer erro.
     */
    public String takeScreenshot(String testName) {
        AppiumDriver driver = driverFactory.getDriver();

        if (driver == null) {
            logger.error("Não é possível tirar screenshot: Driver é null.");
            return null;
        }

        if (!(driver instanceof TakesScreenshot)) {
            logger.error("Não é possível tirar screenshot: Driver da classe {} não suporta TakesScreenshot.",
                    driver.getClass().getName());
            return null;
        }

        File screenshotFile = null;
        try {
            screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            logger.debug("Screenshot capturado temporariamente em: {}", screenshotFile.getAbsolutePath());

            Path targetDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(targetDir);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String safeTestName = sanitizeFileName(testName);
            String fileName = String.format("%s_%s.png", safeTestName, timestamp);
            Path targetPath = targetDir.resolve(fileName);

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
            if (screenshotFile != null && screenshotFile.exists()) {
                if (!screenshotFile.delete()) {
                    logger.warn("Não foi possível deletar o arquivo de screenshot temporário: {}",
                            screenshotFile.getAbsolutePath());
                }
            }
        }
    }

    // ====================================================================
    // UTILITÁRIOS
    // ====================================================================

    /**
     * Sanitiza nome de arquivo removendo caracteres inválidos.
     */
    private String sanitizeFileName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}