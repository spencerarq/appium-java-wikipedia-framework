package com.wikipedia.unit;

import com.wikipedia.utils.ConfigReader; // Importa a classe a ser testada
import org.junit.jupiter.api.*; // Importa anotações JUnit 5

import java.io.IOException; // Para testes de exceção (se necessário)
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*; // Importa métodos de asserção estáticos

/**
 * Testes unitários para a classe ConfigReader.
 * Cobre cenários de carregamento padrão, carregamento de teste,
 * reset, obtenção de propriedades (String, int), e tratamento de erros/padrões.
 * @author Renato Spencer
 * @version 1.1
 * @since 2025-10-29
 */
class ConfigReaderUnitTest {

    private static final String TEST_CONFIG_PATH = "src/test/resources/test-config.properties";
    private static final String DEFAULT_CONFIG_PATH = "src/test/resources/config.properties";

    /**
     * Garante que o ConfigReader volte ao arquivo padrão DEPOIS de CADA teste
     * que possa ter carregado o arquivo de teste. Essencial para isolamento.
     */
    @AfterEach
    void tearDown() {
        ConfigReader.resetToDefaultProperties();
    }

    // TESTES DE ARQUIVO PADRÃO (config.properties)

    @Test
    @DisplayName("getProperty (Padrão): Deve ler propriedade existente do config.properties")
    void testGetProperty_DefaultFile_Exists() {

        String platform = ConfigReader.getProperty("platformName"); // Usa o método estático diretamente

        assertNotNull(platform, "platformName não deveria ser nula no arquivo padrão");

        assertEquals("Android", platform, "Valor de platformName do arquivo padrão incorreto");
    }

    @Test
    @DisplayName("getIntProperty (Padrão): Deve ler propriedade numérica existente do config.properties")
    void testGetIntProperty_DefaultFile_Exists() {

        int timeout = ConfigReader.getIntProperty("implicitWaitTimeout", -1); // Usa -1 como padrão inválido para teste

        assertEquals(15, timeout, "Valor de implicitWaitTimeout do arquivo padrão incorreto");
    }

    @Test
    @DisplayName("getProperty (Padrão): Deve retornar null para chave inexistente")
    void testGetProperty_DefaultFile_NotExists() {

        String nonExistent = ConfigReader.getProperty("chave.que.realmente.nao.existe.no.padrao");

        assertNull(nonExistent, "Deveria retornar null para chave inexistente no arquivo padrão");
    }

    @Test
    @DisplayName("loadTestProperties: Deve carregar valores do arquivo de teste")
    void testLoadTestProperties() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);

        assertEquals("TestPlatform", ConfigReader.getProperty("platformName"), "Deveria ler 'TestPlatform' do arquivo de teste");
        assertEquals("TestDevice", ConfigReader.getProperty("deviceName"), "Deveria ler 'TestDevice' do arquivo de teste");
        assertEquals(5, ConfigReader.getIntProperty("implicitWaitTimeout", 0), "Deveria ler '5' do arquivo de teste");
    }

    @Test
    @DisplayName("getIntProperty: Deve retornar padrão para 'invalidNumericProperty'")
    void testGetIntProperty_InvalidNumberFormat() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);
        int result = ConfigReader.getIntProperty("invalidNumericProperty", 10);

        assertEquals(10, result, "Deveria retornar o valor padrão 10 para formato inválido");

    }


    @Test
    @DisplayName("getIntProperty: Deve retornar padrão para propriedade vazia")
    void testGetIntProperty_EmptyProperty() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);

        int result = ConfigReader.getIntProperty("nonExistentProperty", 10);

        assertEquals(10, result, "Deveria retornar o valor padrão 10 para propriedade vazia");
    }

    @Test
    @DisplayName("getIntProperty: Deve retornar padrão para propriedade inexistente")
    void testGetIntProperty_NotExists() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);


        int result = ConfigReader.getIntProperty("chave.que.nao.existe.no.teste", 99);

        assertEquals(99, result, "Deveria retornar o valor padrão 99 para chave inexistente");
    }

    @Test
    @DisplayName("getIntProperty: Deve ler propriedade numérica válida do arquivo de teste")
    void testGetIntProperty_ValidNumericFromTestFile() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);


        int result = ConfigReader.getIntProperty("numericProperty", 0);

        assertEquals(123, result, "Deveria ler o valor numérico 123 do arquivo de teste");
    }

    @Test
    @DisplayName("getProperty(default): Deve retornar padrão para chave inexistente")
    void testGetProperty_WithDefault_WhenNotExists() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);


        String result = ConfigReader.getProperty("chave.que.nao.existe.no.teste", "valor-padrao");

        assertEquals("valor-padrao", result, "Deveria retornar o valor padrão quando chave não existe");
    }

    @Test
    @DisplayName("getProperty(default): Deve retornar valor da chave quando existente")
    void testGetProperty_WithDefault_WhenExists() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);

        String result = ConfigReader.getProperty("platformName", "valor-padrao");
        assertEquals("TestPlatform", result, "Deveria retornar o valor do arquivo ('TestPlatform'), não o padrão");
    }

    @Test
    @DisplayName("resetToDefaultProperties: Deve recarregar o config.properties padrão")
    void testResetToDefaultProperties() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);

        assertEquals("TestPlatform", ConfigReader.getProperty("platformName"), "Arquivo de teste deveria estar carregado inicialmente");
        ConfigReader.resetToDefaultProperties();

        assertEquals("Android", ConfigReader.getProperty("platformName"), "Arquivo padrão (config.properties) deveria ter sido recarregado");
    }

    @Test
    @DisplayName("resetToDefaultProperties: Não deve recarregar se já estiver no padrão")
    void testResetToDefaultProperties_WhenAlreadyDefault() {

        ConfigReader.resetToDefaultProperties();
        String initialValue = ConfigReader.getProperty("platformName");
        assertEquals("Android", initialValue, "Assume que o valor padrão é 'Android'");

        ConfigReader.resetToDefaultProperties();

        assertEquals(initialValue, ConfigReader.getProperty("platformName"), "Valor não deve mudar se reset for chamado no arquivo padrão");

    }

    @Test
    @DisplayName("getProperty: Deve retornar valor COM espaços corretamente")
    void testGetProperty_WithSpaces() {

        ConfigReader.loadTestProperties(TEST_CONFIG_PATH);
        String result = ConfigReader.getProperty("propertyWithSpaces");
        assertNotNull(result);

        String expectedValue = " value with spaces ";
        assertEquals(expectedValue, result, "Valor com espaços inicial/final não foi preservado corretamente");

    }


}