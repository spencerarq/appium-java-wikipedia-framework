package com.wikipedia.utils; // Pacote ajustado conforme sua estrutura

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para centralizar e facilitar o logging usando SLF4j.
 * Fornece métodos estáticos para logar em diferentes níveis.
 */
public class LoggerHelper {

    // Obtém um logger para esta classe utilitária.
    // Alternativamente, poderíamos ter métodos que recebem a classe que está logando,
    // mas para simplificar, usaremos um logger centralizado aqui.
    private static final Logger logger = LoggerFactory.getLogger(LoggerHelper.class);

    // Construtor privado para evitar instanciação
    private LoggerHelper() {}

    /**
     * Loga uma mensagem no nível INFO.
     * @param message A mensagem a ser logada.
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Loga uma mensagem formatada no nível INFO.
     * Exemplo: info("Usuário {} logado com sucesso.", username);
     * @param format O formato da mensagem (usando {} para placeholders).
     * @param args Os argumentos para preencher os placeholders.
     */
    public static void info(String format, Object... args) {
        logger.info(format, args);
    }

    /**
     * Loga uma mensagem no nível DEBUG.
     * @param message A mensagem a ser logada.
     */
    public static void debug(String message) {
        // Verifica se o nível DEBUG está habilitado antes de logar (performance)
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    /**
     * Loga uma mensagem formatada no nível DEBUG.
     * @param format O formato da mensagem.
     * @param args Os argumentos.
     */
    public static void debug(String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, args);
        }
    }

    /**
     * Loga uma mensagem no nível WARN.
     * @param message A mensagem a ser logada.
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Loga uma mensagem formatada no nível WARN.
     * @param format O formato da mensagem.
     * @param args Os argumentos.
     */
    public static void warn(String format, Object... args) {
        logger.warn(format, args);
    }

    /**
     * Loga uma mensagem no nível ERROR.
     * @param message A mensagem a ser logada.
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Loga uma mensagem formatada no nível ERROR.
     * @param format O formato da mensagem.
     * @param args Os argumentos.
     */
    public static void error(String format, Object... args) {
        logger.error(format, args);
    }

    /**
     * Loga uma mensagem de erro juntamente com a stack trace da exceção.
     * @param message A mensagem de erro.
     * @param throwable A exceção ocorrida.
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}