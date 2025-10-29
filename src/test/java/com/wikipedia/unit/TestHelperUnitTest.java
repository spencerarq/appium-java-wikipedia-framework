package com.wikipedia.unit;

import com.wikipedia.commons.DriverFactory;
import com.wikipedia.utils.TestHelper;
import io.appium.java_client.AppiumDriver;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;

import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a classe TestHelper.
 * @author Renato Spencer
 * @version 1.5
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestHelper - Testes Unitários Corrigidos v2")
class TestHelperUnitTest {

    @Mock
    private AppiumDriver mockDriver;
    @Mock
    private DriverFactory mockDriverFactory;

    private TestHelper testHelper;

    private static MockedStatic<DriverFactory> mockedFactory;
    private static MockedStatic<Files> mockedFiles;

    @BeforeAll
    static void beforeAll() {
        mockedFactory = mockStatic(DriverFactory.class);
        mockedFiles = mockStatic(Files.class);
    }

    @AfterAll
    static void afterAll() {
        mockedFactory.close();
        mockedFiles.close();
    }

    @BeforeEach
    void setUp() {
        mockedFactory.when(DriverFactory::getInstance).thenReturn(mockDriverFactory);
        when(mockDriverFactory.getDriver()).thenReturn(mockDriver);
        testHelper = new TestHelper();
        mockedFiles.reset();
        mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
        mockedFiles.when(() -> Files.copy(
                        any(Path.class),
                        any(Path.class),
                        any(CopyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    @DisplayName("takeScreenshot: Deve salvar screenshot com sucesso")
    void takeScreenshot_Success() {
        String testName = "meuTesteDeSucesso";
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockScreenshotFile.delete()).thenReturn(true);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);

        String resultPath = testHelper.takeScreenshot(testName);

        assertNotNull(resultPath);
        assertTrue(resultPath.contains(testName));
        assertTrue(resultPath.endsWith(".png"));

        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.copy(
                        eq(mockTempPath),
                        argThat(p -> p.toString().contains(testName)),
                        eq(StandardCopyOption.REPLACE_EXISTING)),
                times(1));
        verify(mockScreenshotFile, times(1)).delete();
    }

    @Test
    @DisplayName("takeScreenshot: Deve usar UUID se testName for nulo")
    void takeScreenshot_NullTestName_ShouldUseUUID() {
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_null.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockScreenshotFile.delete()).thenReturn(true);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);

        String resultPath = testHelper.takeScreenshot(null);

        assertNotNull(resultPath);
        Path resultPathObj = Paths.get(resultPath);
        String fileName = resultPathObj.getFileName().toString();
        assertTrue(fileName.matches("^[a-f0-9]{8}_.*\\.png$"));

        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), times(1));
        verify(mockScreenshotFile, times(1)).delete();
    }

    @Test
    @DisplayName("takeScreenshot: Deve usar UUID se testName for vazio")
    void takeScreenshot_EmptyTestName_ShouldUseUUID() {
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_empty.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockScreenshotFile.delete()).thenReturn(true);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);

        String resultPath = testHelper.takeScreenshot("   ");

        assertNotNull(resultPath);
        Path resultPathObj = Paths.get(resultPath);
        String fileName = resultPathObj.getFileName().toString();
        assertTrue(fileName.matches("^[a-f0-9]{8}_.*\\.png$"));

        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), times(1));
        verify(mockScreenshotFile, times(1)).delete();
    }

    @Test
    @DisplayName("takeScreenshot: Deve sanitizar caracteres inválidos no testName")
    void takeScreenshot_InvalidCharsInTestName_ShouldSanitize() {

        String testName = "teste/Com\\Caracteres?Inválidos*";

        String expectedSanitizedPart = "teste_Com_Caracteres_Inv_lidos_";
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_invalid.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockScreenshotFile.delete()).thenReturn(true);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);


        String resultPath = testHelper.takeScreenshot(testName);

        assertNotNull(resultPath);

        String actualFileName = Paths.get(resultPath).getFileName().toString();

        assertTrue(actualFileName.contains(expectedSanitizedPart),
                "Nome do arquivo deveria conter a parte sanitizada '" + expectedSanitizedPart + "', mas foi '" + actualFileName + "'");

        assertFalse(actualFileName.contains("/"), "Nome do arquivo não deveria conter '/'");
        assertFalse(actualFileName.contains("\\"), "Nome do arquivo não deveria conter '\\'");
        assertFalse(actualFileName.contains("?"), "Nome do arquivo não deveria conter '?'");
        assertFalse(actualFileName.contains("*"), "Nome do arquivo não deveria conter '*'");
        assertFalse(actualFileName.contains("á"), "Nome do arquivo não deveria conter 'á'"); // Verifica remoção do acento

        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.copy(
                any(Path.class),
                argThat(p -> p.getFileName().toString().contains(expectedSanitizedPart)),
                any(CopyOption.class)), times(1));
        verify(mockScreenshotFile, times(1)).delete();
    }

    @Test
    @DisplayName("takeScreenshot: Deve retornar null se o driver for null")
    void takeScreenshot_DriverNull() {
        when(mockDriverFactory.getDriver()).thenReturn(null);
        testHelper = new TestHelper();
        String resultPath = testHelper.takeScreenshot("testeComDriverNulo");
        assertNull(resultPath);
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), never());
    }

    @Test
    @DisplayName("takeScreenshot: Deve retornar null se o driver não for TakesScreenshot")
    void takeScreenshot_DriverNotScreenshotCapable() {
        AppiumDriver mockBasicDriver = mock(AppiumDriver.class);
        when(mockDriverFactory.getDriver()).thenReturn(mockBasicDriver);
        testHelper = new TestHelper();
        String resultPath = testHelper.takeScreenshot("testeDriverIncompativel");
        assertNull(resultPath);
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), never());
    }

    @Test
    @DisplayName("takeScreenshot: Deve retornar null se getScreenshotAs falhar")
    void takeScreenshot_ScreenshotFails() {
        when(mockDriver.getScreenshotAs(OutputType.FILE))
                .thenThrow(new WebDriverException("Erro simulado na captura"));
        String resultPath = testHelper.takeScreenshot("testeFalhaCaptura");
        assertNull(resultPath);
        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), never());
    }

    @Test
    @DisplayName("takeScreenshot: Deve retornar null se Files.createDirectories falhar")
    void takeScreenshot_CreateDirectoryFails() {
        File mockScreenshotFile = mock(File.class);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);
        IOException ioException = new IOException("Permissão negada simulada");
        mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenThrow(ioException);

        String resultPath = testHelper.takeScreenshot("testeFalhaCriarDir");
        assertNull(resultPath);
        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.createDirectories(any(Path.class)), times(1));
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), never());
        verify(mockScreenshotFile, times(1)).exists();
    }

    @Test
    @DisplayName("takeScreenshot: Deve retornar null se Files.copy falhar")
    void takeScreenshot_FileCopyFails() {
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_copy_fail.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);
        IOException ioException = new IOException("Disco cheio simulado");
        mockedFiles.when(() -> Files.copy(
                        any(Path.class),
                        any(Path.class),
                        any(CopyOption.class)))
                .thenThrow(ioException);
        String resultPath = testHelper.takeScreenshot("testeFalhaCopia");

        assertNull(resultPath);
        verify(mockDriver, times(1)).getScreenshotAs(OutputType.FILE);
        mockedFiles.verify(() -> Files.createDirectories(any(Path.class)), times(1));
        mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(CopyOption.class)), times(1));
        verify(mockScreenshotFile, times(1)).exists();
    }

    @Test
    @DisplayName("takeScreenshot: Deve tentar deletar arquivo temporário mesmo se cópia falhar")
    void takeScreenshot_ShouldAttemptDeleteTempFileEvenOnCopyFailure() {
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_delete_check.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);
        mockedFiles.when(() -> Files.copy(
                        any(Path.class),
                        any(Path.class),
                        any(CopyOption.class)))
                .thenThrow(new IOException("Falha simulada na cópia"));

        testHelper.takeScreenshot("testeDeleteAposFalha");

        verify(mockScreenshotFile, times(1)).delete();
    }

    @Test
    @DisplayName("takeScreenshot: Não deve falhar se arquivo temporário não puder ser deletado")
    void takeScreenshot_ShouldNotFailIfTempFileDeleteFails() {
        File mockScreenshotFile = mock(File.class);
        Path mockTempPath = Paths.get("temp/screenshot_delete_fail.png");
        when(mockScreenshotFile.toPath()).thenReturn(mockTempPath);
        when(mockScreenshotFile.exists()).thenReturn(true);
        when(mockScreenshotFile.delete()).thenReturn(false); // Simula falha ao deletar
        when(mockDriver.getScreenshotAs(OutputType.FILE)).thenReturn(mockScreenshotFile);
        mockedFiles.when(() -> Files.copy(
                        any(Path.class),
                        any(Path.class),
                        any(CopyOption.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        String resultPath = assertDoesNotThrow(() -> testHelper.takeScreenshot("testeDeleteFalha"));

        assertNotNull(resultPath);
        verify(mockScreenshotFile, times(1)).delete();
    }
}