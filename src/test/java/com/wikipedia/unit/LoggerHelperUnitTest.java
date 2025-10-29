package com.wikipedia.unit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wikipedia.utils.LoggerHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe LoggerHelper.
 * Usa ListAppender para capturar e validar as mensagens de log reais.
 * @author Renato Spencer
 * @version 1.1 (Refatorado com ListAppender)
 * @since 2025-10-29
 */
class LoggerHelperUnitTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Obtém o Logger raiz do Logback usado pelo LoggerHelper
        logger = (Logger) LoggerFactory.getLogger(LoggerHelper.class);

        // Cria e inicia o ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();

        // Adiciona o ListAppender ao logger
        logger.addAppender(listAppender);

        // Importante: Definir um nível que capture todos os logs (ex: TRACE ou DEBUG)
        // para garantir que os testes de DEBUG funcionem, mesmo se o root for INFO.
        // Guardar o nível original para restaurar depois.

    }

    @AfterEach
    void tearDown() {
        // Remove o appender e o para
        logger.detachAppender(listAppender);
        listAppender.stop();
        // Restaurar nível do logger se alterado no setup
    }

    @Test
    @DisplayName("Deve logar mensagem INFO simples")
    void testInfoLogging_Simple() {
        String testMessage = "Mensagem de teste INFO simples.";
        LoggerHelper.info(testMessage);

        assertEquals(1, listAppender.list.size(), "Deveria haver 1 evento de log");
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel(), "Nível do log deve ser INFO");
        assertEquals(testMessage, loggingEvent.getFormattedMessage(), "Mensagem logada deve ser igual");
    }

    @Test
    @DisplayName("Deve logar mensagem INFO formatada")
    void testInfoLogging_Formatted() {
        String format = "Mensagem de teste INFO formatada: {}";
        String arg = "valor";
        String expectedMessage = "Mensagem de teste INFO formatada: valor";
        LoggerHelper.info(format, arg);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals(expectedMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    @DisplayName("Deve logar mensagem DEBUG simples (se nível habilitado)")
    void testDebugLogging_Simple() {
        // Se root for INFO, este teste PODE não logar nada.
        // Ajuste o logback-test.xml ou o nível no setUp() se precisar garantir.
        logger.setLevel(Level.DEBUG); // Força o nível DEBUG para este teste
        String testMessage = "Mensagem de teste DEBUG simples.";
        LoggerHelper.debug(testMessage);

        // Assume que DEBUG está habilitado
        if (logger.isDebugEnabled()) {
            assertEquals(1, listAppender.list.size());
            ILoggingEvent loggingEvent = listAppender.list.get(0);
            assertEquals(Level.DEBUG, loggingEvent.getLevel());
            assertEquals(testMessage, loggingEvent.getFormattedMessage());
        } else {
            assertEquals(0, listAppender.list.size(), "Nenhum log DEBUG esperado se nível for > DEBUG");
        }
        logger.setLevel(Level.INFO); // Restaura o nível
    }

    @Test
    @DisplayName("Deve logar mensagem DEBUG formatada (se nível habilitado)")
    void testDebugLogging_Formatted() {
        logger.setLevel(Level.DEBUG); // Força o nível DEBUG
        String format = "Mensagem de teste DEBUG formatada: {} e {}";
        String arg1 = "valor1";
        int arg2 = 123;
        String expectedMessage = "Mensagem de teste DEBUG formatada: valor1 e 123";
        LoggerHelper.debug(format, arg1, arg2);

        if (logger.isDebugEnabled()) {
            assertEquals(1, listAppender.list.size());
            ILoggingEvent loggingEvent = listAppender.list.get(0);
            assertEquals(Level.DEBUG, loggingEvent.getLevel());
            assertEquals(expectedMessage, loggingEvent.getFormattedMessage());
        } else {
            assertEquals(0, listAppender.list.size());
        }
        logger.setLevel(Level.INFO); // Restaura
    }

    @Test
    @DisplayName("Deve logar mensagem WARN simples")
    void testWarnLogging_Simple() {
        String testMessage = "Mensagem de teste WARN simples.";
        LoggerHelper.warn(testMessage);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertEquals(testMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    @DisplayName("Deve logar mensagem WARN formatada")
    void testWarnLogging_Formatted() {
        String format = "Mensagem de teste WARN formatada: {}";
        boolean arg = true;
        String expectedMessage = "Mensagem de teste WARN formatada: true";
        LoggerHelper.warn(format, arg);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertEquals(expectedMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    @DisplayName("Deve logar mensagem ERROR simples")
    void testErrorLogging_Simple() {
        String testMessage = "Mensagem de teste ERROR simples.";
        LoggerHelper.error(testMessage);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals(testMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    @DisplayName("Deve logar mensagem ERROR formatada")
    void testErrorLogging_Formatted() {
        String format = "Mensagem de teste ERROR formatada: {}";
        int arg = 404;
        String expectedMessage = "Mensagem de teste ERROR formatada: 404";
        LoggerHelper.error(format, arg);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals(expectedMessage, loggingEvent.getFormattedMessage());
    }

    @Test
    @DisplayName("Deve logar mensagem ERROR com exceção")
    void testErrorLogging_WithException() {
        String testMessage = "Mensagem de teste ERROR com exceção.";
        RuntimeException exception = new RuntimeException("Erro simulado");
        LoggerHelper.error(testMessage, exception);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals(testMessage, loggingEvent.getMessage()); // Mensagem bruta, sem stack trace
        assertNotNull(loggingEvent.getThrowableProxy(), "Deveria haver uma exceção associada");
        assertEquals("Erro simulado", loggingEvent.getThrowableProxy().getMessage(), "Mensagem da exceção");
    }
}