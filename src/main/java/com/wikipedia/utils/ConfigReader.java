package com.wikipedia.utils; // Pacote ajustado conforme sua estrutura

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Classe utilitária para ler as configurações do arquivo config.properties.
 * Utiliza o padrão Singleton para garantir que as propriedades sejam carregadas apenas uma vez por arquivo.
 * Inclui métodos para carregar arquivos de teste e restaurar o padrão.
 */
public class ConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    // Caminho padrão para o arquivo de configuração principal
    private static final String DEFAULT_CONFIG_FILE_PATH = "src/test/resources/config.properties";
    private static Properties properties;
    // Variável para rastrear qual arquivo de configuração está atualmente carregado
    private static String currentConfigPath = DEFAULT_CONFIG_FILE_PATH;

    // Bloco estático para carregar as propriedades padrão na inicialização da classe
    static {
        loadProperties(DEFAULT_CONFIG_FILE_PATH); // Carrega o arquivo padrão
    }

    // Construtor privado para evitar instanciação externa
    private ConfigReader() {}

    /**
     * Carrega as propriedades do arquivo de configuração especificado.
     * Este método é privado e sincronizado para garantir a atomicidade ao trocar as propriedades.
     * @param filePath O caminho para o arquivo .properties a ser carregado.
     */
    private static synchronized void loadProperties(String filePath) {
        properties = new Properties(); // Sempre cria um novo objeto Properties para limpar o anterior
        currentConfigPath = filePath; // Atualiza o caminho atual
        logger.info("Carregando propriedades do arquivo: {}", filePath);

        // Verifica se o arquivo existe antes de tentar carregar
        if (!Files.exists(Paths.get(filePath))) {
            logger.error("Arquivo de configuração não encontrado em: {}", filePath);
            // Lança uma exceção pois a configuração é essencial
            throw new RuntimeException("Arquivo de configuração não encontrado: " + filePath);
        }

        // Usa try-with-resources para garantir o fechamento do InputStream
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            logger.info("Propriedades carregadas com sucesso de {}.", filePath);
            // Logar algumas propriedades essenciais para verificação (opcional, nível DEBUG)
            // logger.debug("platformName (de {}): {}", filePath, properties.getProperty("platformName"));

        } catch (IOException ex) {
            logger.error("Erro ao carregar o arquivo de configuração: {}", filePath, ex);
            // Lança uma exceção para indicar falha crítica
            throw new RuntimeException("Não foi possível carregar o arquivo de configuração: " + filePath, ex);
        }
    }

    /**
     * Força o carregamento de propriedades de um arquivo específico, substituindo as atuais.
     * Use com cuidado e sempre restaure com resetToDefaultProperties() após o teste.
     * @param testFilePath Caminho para o arquivo de propriedades de teste.
     */
    public static synchronized void loadTestProperties(String testFilePath) {
        logger.warn("--- CARREGANDO PROPRIEDADES DE TESTE DE: {} ---", testFilePath);
        loadProperties(testFilePath);
    }

    /**
     * Restaura o carregamento para o arquivo de propriedades padrão (definido em DEFAULT_CONFIG_FILE_PATH).
     * Deve ser chamado em um método @AfterAll ou @AfterEach nos testes unitários.
     */
    public static synchronized void resetToDefaultProperties() {
        // Só recarrega se o arquivo atual NÃO for o padrão
        if (!currentConfigPath.equals(DEFAULT_CONFIG_FILE_PATH)) {
            logger.warn("--- RESTAURANDO PROPRIEDADES PADRÃO DE: {} ---", DEFAULT_CONFIG_FILE_PATH);
            loadProperties(DEFAULT_CONFIG_FILE_PATH);
        } else {
            logger.debug("Propriedades padrão já estavam carregadas. Nenhuma ação necessária.");
        }
    }

    /**
     * Obtém o valor de uma propriedade de configuração atualmente carregada.
     *
     * @param propertyName A chave (nome) da propriedade desejada.
     * @return O valor da propriedade como String, ou null se a propriedade não for encontrada.
     */
    public static String getProperty(String propertyName) {
        // Verifica se 'properties' foi inicializado (embora o bloco estático deva garantir isso)
        if (properties == null) {
            logger.error("Objeto Properties não inicializado ao buscar '{}'. Verifique o fluxo de inicialização.", propertyName);
            // Tentar recarregar pode mascarar o problema original, melhor falhar ou retornar null.
            return null;
            // Se optar por tentar recarregar:
            // logger.warn("Tentando recarregar propriedades de {}.", currentConfigPath);
            // loadProperties(currentConfigPath);
            // if (properties == null) return null; // Se ainda falhar
        }
        String value = properties.getProperty(propertyName);
        if (value == null) {
            // Usar WARN para propriedades não encontradas é útil para debug
            logger.warn("Propriedade '{}' não encontrada no arquivo {}", propertyName, currentConfigPath);
        }
        // Log TRACE pode ser útil para depuração detalhada
        // logger.trace("Obtendo propriedade: {} = {}", propertyName, value);
        return value;
    }

    /**
     * Obtém o valor de uma propriedade de configuração, retornando um valor padrão se não for encontrada.
     *
     * @param propertyName A chave (nome) da propriedade desejada.
     * @param defaultValue O valor padrão a ser retornado se a chave não existir.
     * @return O valor da propriedade como String, ou o valor padrão.
     */
    public static String getProperty(String propertyName, String defaultValue) {
        String value = getProperty(propertyName);
        // Retorna o valor lido se não for nulo, caso contrário retorna o padrão
        return (value != null) ? value : defaultValue;
    }

    /**
     * Obtém o valor de uma propriedade de configuração como um Inteiro.
     *
     * @param propertyName A chave (nome) da propriedade desejada.
     * @param defaultValue O valor padrão a ser retornado se a propriedade não for encontrada ou não for um número inteiro válido.
     * @return O valor da propriedade como int, ou o valor padrão.
     */
    public static int getIntProperty(String propertyName, int defaultValue) {
        String value = getProperty(propertyName);
        if (value != null && !value.trim().isEmpty()) { // Verifica também se não está vazio após trim
            try {
                // trim() remove espaços extras antes de tentar converter
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                // Log mais detalhado sobre a falha de conversão
                logger.warn("Falha ao converter a propriedade '{}' ('{}') do arquivo {} para inteiro. Usando valor padrão: {}. Erro: {}",
                        propertyName, value, currentConfigPath, defaultValue, e.getMessage());
            }
        } else if (value != null && value.trim().isEmpty()){
            logger.warn("Propriedade '{}' está presente mas vazia no arquivo {}. Usando valor padrão: {}", propertyName, currentConfigPath, defaultValue);
        }
        // Retorna o padrão se a propriedade não existe, está vazia ou falhou na conversão
        return defaultValue;
    }


    public static final String PLATFORM_NAME = "platformName";
    public static final String AUTOMATION_NAME = "automationName";
    public static final String DEVICE_NAME = "deviceName";
    public static final String APP_PACKAGE = "appPackage";
    public static final String APP_WAIT_ACTIVITY = "appWaitActivity";
    public static final String IMPLICIT_WAIT = "implicitWaitTimeout"; // Em segundos
    public static final String APPIUM_SERVER_URL = "appiumServerUrl";


    public static String getPlatformName() {
        return getProperty(PLATFORM_NAME);
    }

    public static String getAutomationName() {
        return getProperty(AUTOMATION_NAME);
    }

    public static String getDeviceName() {
        return getProperty(DEVICE_NAME);
    }

    public static String getAppPackage() {
        return getProperty(APP_PACKAGE);
    }

    public static String getAppWaitActivity() {
        return getProperty(APP_WAIT_ACTIVITY);
    }

    public static int getImplicitWait() {
        // Padrão de 10 segundos se não definido no arquivo ou inválido
        return getIntProperty(IMPLICIT_WAIT, 10);
    }

    public static String getAppiumServerUrl() {
        // Padrão local se não definido no arquivo
        return getProperty(APPIUM_SERVER_URL, "http://127.0.0.1:4723");
    }

}