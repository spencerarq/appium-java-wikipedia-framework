package com.wikipedia.unit;

import com.wikipedia.utils.LoggerHelper; // Importa a classe a ser testada
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários (simples) para a classe LoggerHelper.
 * Verifica principalmente se os métodos podem ser chamados sem lançar exceções.
 */
class LoggerHelperUnitTest {

    @Test
    void testInfoLogging() {
        assertDoesNotThrow(() -> LoggerHelper.info("Mensagem de teste INFO simples."),
                "Método info(String) não deve lançar exceção");
        assertDoesNotThrow(() -> LoggerHelper.info("Mensagem de teste INFO formatada: {}", "valor"),
                "Método info(String, Object...) não deve lançar exceção");
    }

    @Test
    void testDebugLogging() {
        // Nota: A mensagem só será realmente logada se o nível DEBUG estiver habilitado no logback-test.xml
        assertDoesNotThrow(() -> LoggerHelper.debug("Mensagem de teste DEBUG simples."),
                "Método debug(String) não deve lançar exceção");
        assertDoesNotThrow(() -> LoggerHelper.debug("Mensagem de teste DEBUG formatada: {} e {}", "valor1", 123),
                "Método debug(String, Object...) não deve lançar exceção");
    }

    @Test
    void testWarnLogging() {
        assertDoesNotThrow(() -> LoggerHelper.warn("Mensagem de teste WARN simples."),
                "Método warn(String) não deve lançar exceção");
        assertDoesNotThrow(() -> LoggerHelper.warn("Mensagem de teste WARN formatada: {}", true),
                "Método warn(String, Object...) não deve lançar exceção");
    }

    @Test
    void testErrorLogging() {
        assertDoesNotThrow(() -> LoggerHelper.error("Mensagem de teste ERROR simples."),
                "Método error(String) não deve lançar exceção");
        assertDoesNotThrow(() -> LoggerHelper.error("Mensagem de teste ERROR formatada: {}", 404),
                "Método error(String, Object...) não deve lançar exceção");
        assertDoesNotThrow(() -> LoggerHelper.error("Mensagem de teste ERROR com exceção.", new RuntimeException("Erro simulado")),
                "Método error(String, Throwable) não deve lançar exceção");
    }
}