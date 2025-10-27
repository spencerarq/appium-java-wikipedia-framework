package com.wikipedia.unit;

import com.wikipedia.utils.ConfigReader; // Importa a classe a ser testada
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir; // Para criar arquivos temporários se necessário

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ConfigReader.
 */
class ConfigReaderUnitTest {

    private static final String TEST_CONFIG_PATH = "src/test/resources/test-config.properties";

    @BeforeAll
    static void setUp() {
        // Garante que o arquivo de teste exista (opcional, mas bom para robustez)
        Path path = Paths.get(TEST_CONFIG_PATH);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                List<String> lines = Arrays.asList(
                        "platformName=TestPlatform",
                        "deviceName=TestDevice",
                        "implicitWaitTimeout=5",
                        "nonExistentProperty=",
                        "numericProperty=123",
                        "invalidNumericProperty=abc",
                        "propertyWithSpaces=  value with spaces  "
                );
                Files.write(path, lines);
            } catch (IOException e) {
                fail("Não foi possível criar o arquivo de configuração de teste: " + e.getMessage());
            }
        }
        // Carrega as propriedades de teste ANTES de qualquer teste rodar
        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);
    }

    @AfterAll
    static void tearDown() {
        // Restaura para as propriedades padrão DEPOIS de todos os testes rodarem
        ConfigReader.resetToDefaultProperties();
    }

    @Test
    void testGetExistingStringProperty() {
        assertEquals("TestPlatform", ConfigReader.getProperty("platformName"),
                "Deve retornar o valor correto para platformName");
        assertEquals("TestDevice", ConfigReader.getProperty(ConfigReader.DEVICE_NAME), // Usando constante
                "Deve retornar o valor correto para deviceName");
    }

    @Test
    void testGetPropertyWithSpaces() {
        assertEquals(" value with spaces ", ConfigReader.getProperty("propertyWithSpaces"),
                "Deve retornar o valor com espaços");
    }


    @Test
    void testGetNonExistentProperty() {
        assertNull(ConfigReader.getProperty("chaveInexistente"),
                "Deve retornar null para propriedade inexistente");
    }

    @Test
    void testGetPropertyWithDefaultValue_Exists() {
        assertEquals("TestPlatform", ConfigReader.getProperty("platformName", "Default"),
                "Deve retornar o valor do arquivo quando a propriedade existe");
    }

    @Test
    void testGetPropertyWithDefaultValue_NotExists() {
        assertEquals("DefaultValue", ConfigReader.getProperty("chaveInexistente", "DefaultValue"),
                "Deve retornar o valor padrão quando a propriedade não existe");
    }

    @Test
    void testGetExistingIntProperty() {
        assertEquals(5, ConfigReader.getIntProperty(ConfigReader.IMPLICIT_WAIT, 99),
                "Deve retornar o valor inteiro correto para implicitWaitTimeout");
        assertEquals(123, ConfigReader.getIntProperty("numericProperty", 99),
                "Deve retornar o valor inteiro correto para numericProperty");
    }

    @Test
    void testGetIntProperty_InvalidFormat() {
        assertEquals(99, ConfigReader.getIntProperty("invalidNumericProperty", 99),
                "Deve retornar o valor padrão quando o valor não é um inteiro");
    }

    @Test
    void testGetIntProperty_NotExists() {
        assertEquals(50, ConfigReader.getIntProperty("intInexistente", 50),
                "Deve retornar o valor padrão quando a propriedade inteira não existe");
    }

    // Teste para verificar se o carregamento falha se o arquivo não existe
    @Test
    void testLoadProperties_FileNotFound() {
        String nonExistentPath = "src/test/resources/non-existent-config.properties";
        // Verifica se RuntimeException é lançada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ConfigReader.loadTestProperties(nonExistentPath));
        assertTrue(exception.getMessage().contains("Arquivo de configuração não encontrado"),
                "A mensagem de exceção deve indicar arquivo não encontrado");

        // IMPORTANTE: Restaurar para um estado válido após o teste de exceção
        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);
    }

    // Testar métodos de conveniência
    @Test
    void testConvenienceMethods() {
        assertEquals("TestPlatform", ConfigReader.getPlatformName());
        assertEquals("TestDevice", ConfigReader.getDeviceName());
        assertEquals(5, ConfigReader.getImplicitWait());
        // Testa o padrão do método getAppiumServerUrl, pois não está no test-config.properties
        assertEquals("http://127.0.0.1:4723", ConfigReader.getAppiumServerUrl());
    }
}