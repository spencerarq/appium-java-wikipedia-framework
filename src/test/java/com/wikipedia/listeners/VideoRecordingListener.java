package com.wikipedia.listeners;

import com.wikipedia.utils.TestHelper;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Listener TestNG que automatiza a gravação de vídeo para todos os testes E2E.
 *
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Inicia gravação automaticamente antes de cada teste (@Test)</li>
 *   <li>Para gravação e salva vídeo automaticamente após cada teste</li>
 *   <li>Anexa vídeo ao relatório Allure (sucesso, falha ou pulado)</li>
 *   <li>Tira screenshot adicional automaticamente em caso de falha</li>
 *   <li>Logs detalhados de início/fim de testes e suites</li>
 * </ul>
 *
 * <p><b>Configuração:</b></p>
 * <p>Adicione este listener ao testng.xml:</p>
 * <pre>
 * {@code
 * <suite name="Sua Suite">
 *     <listeners>
 *         <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
 *         <listener class-name="com.wikipedia.listeners.VideoRecordingListener"/>
 *     </listeners>
 *     ...
 * </suite>
 * }
 * </pre>
 *
 * <p><b>Requisitos:</b></p>
 * <ul>
 *   <li>TestHelper deve implementar startVideoRecording() e stopAndSaveVideo()</li>
 *   <li>Driver deve ser AndroidDriver (suporte a CanRecordScreen)</li>
 *   <li>Appium Server deve estar rodando</li>
 * </ul>
 *
 * <p><b>Thread-Safety:</b></p>
 * <p>Usa ThreadLocal para suportar execução paralela de testes.</p>
 *
 * @author Renato Spencer
 * @version 1.0
 * @since 2025-11-30
 */
public class VideoRecordingListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(VideoRecordingListener.class);

    /**
     * Armazena instância do TestHelper por thread para suportar execução paralela.
     * Cada thread de teste terá seu próprio TestHelper isolado.
     */
    private static final ThreadLocal<TestHelper> testHelperThreadLocal = new ThreadLocal<>();

    /**
     * Chamado ANTES de cada método @Test iniciar.
     * Inicializa TestHelper e inicia gravação de vídeo.
     *
     * @param result Resultado do teste (contém metadados como nome do método)
     */
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();

        logger.info("===============================================");
        logger.info("INICIANDO TESTE: {}.{}", className, testName);
        logger.info("Descrição: {}", result.getMethod().getDescription());
        logger.info("===============================================");

        // Cria nova instância do TestHelper para este teste
        TestHelper testHelper = new TestHelper();
        testHelperThreadLocal.set(testHelper);

        // Tenta iniciar gravação de vídeo
        boolean started = testHelper.startVideoRecording();
        if (started) {
            logger.info("Gravação de vídeo iniciada com sucesso para: {}", testName);
        } else {
            logger.warn("Não foi possível iniciar gravação de vídeo para: {}", testName);
            logger.warn("Teste continuará sem gravação de vídeo");
        }
    }

    /**
     * Chamado quando um teste PASSA (sucesso).
     *
     * @param result Resultado do teste
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestFinish(result, "SUCESSO");
    }

    /**
     * Chamado quando um teste FALHA.
     * Tira screenshot adicional antes de finalizar.
     *
     * @param result Resultado do teste (contém exceção da falha)
     */
    @Override
    public void onTestFailure(ITestResult result) {
        TestHelper testHelper = testHelperThreadLocal.get();
        String testName = result.getMethod().getMethodName();

        // Captura screenshot adicional em caso de falha
        if (testHelper != null) {
            logger.info("Teste falhou. Capturando screenshot de falha...");

            try {
                String screenshotPath = testHelper.takeScreenshot(testName + "_FALHA");

                if (screenshotPath != null && Files.exists(Paths.get(screenshotPath))) {
                    logger.info("Screenshot de falha salvo: {}", screenshotPath);

                    // Anexa screenshot ao Allure
                    Allure.addAttachment(
                            "Screenshot Falha - " + testName,
                            "image/png",
                            Files.newInputStream(Paths.get(screenshotPath)),
                            ".png"
                    );
                    logger.info("Screenshot de falha anexado ao relatório Allure");
                } else {
                    logger.warn("Não foi possível salvar screenshot de falha");
                }
            } catch (IOException e) {
                logger.error("Erro ao anexar screenshot de falha ao Allure: {}", e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Erro inesperado ao capturar screenshot de falha: {}", e.getMessage(), e);
            }
        }

        // Log da exceção que causou a falha
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            logger.error("Causa da falha: {}", throwable.getMessage());
            logger.debug("Stack trace completo:", throwable);
        }

        handleTestFinish(result, "FALHA");
    }

    /**
     * Chamado quando um teste é PULADO (skip).
     *
     * @param result Resultado do teste
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Teste pulado: {}", result.getMethod().getMethodName());

        // Log da razão do skip (se houver)
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            logger.warn("Razão do skip: {}", throwable.getMessage());
        }

        handleTestFinish(result, "PULADO");
    }

    /**
     * Método comum para finalizar teste (sucesso, falha ou pulado).
     * Para gravação de vídeo, salva arquivo e anexa ao Allure.
     *
     * @param result Resultado do teste
     * @param status Status do teste ("SUCESSO", "FALHA" ou "PULADO")
     */
    private void handleTestFinish(ITestResult result, String status) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        TestHelper testHelper = testHelperThreadLocal.get();

        logger.info("-----------------------------------------------");
        logger.info("FINALIZANDO TESTE: {}.{}", className, testName);
        logger.info("Status: {}", status);

        // Calcula duração do teste
        long durationMs = result.getEndMillis() - result.getStartMillis();
        logger.info("Duração: {} ms ({} segundos)", durationMs, durationMs / 1000.0);
        logger.info("-----------------------------------------------");

        if (testHelper == null) {
            logger.warn("TestHelper não encontrado para: {}", testName);
            logger.warn("Vídeo não será salvo para este teste");
            return;
        }

        try {
            // Para gravação e salva vídeo
            logger.info("Parando gravação de vídeo...");
            String videoPath = testHelper.stopAndSaveVideo(testName);

            if (videoPath != null && Files.exists(Paths.get(videoPath))) {
                logger.info("Vídeo salvo com sucesso: {}", videoPath);

                // Anexa vídeo ao relatório Allure
                try {
                    Allure.addAttachment(
                            String.format("Video - %s - %s", testName, status),
                            "video/mp4",
                            Files.newInputStream(Paths.get(videoPath)),
                            ".mp4"
                    );
                    logger.info("Vídeo anexado ao relatório Allure");
                } catch (IOException e) {
                    logger.error("Erro ao anexar vídeo ao Allure: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("Vídeo não foi salvo ou arquivo não existe");
                if (videoPath != null) {
                    logger.debug("Caminho retornado: {}", videoPath);
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao processar vídeo do teste: {}", e.getMessage(), e);
        } finally {
            // Remove TestHelper da ThreadLocal para liberar memória
            testHelperThreadLocal.remove();
            logger.info("===============================================\n");
        }
    }

    /**
     * Chamado ANTES da suite de testes iniciar.
     *
     * @param context Contexto da suite (contém metadados e configurações)
     */
    @Override
    public void onStart(ITestContext context) {
        logger.info("###############################################");
        logger.info("INICIANDO SUITE DE TESTES: {}", context.getName());
        logger.info("Total de testes a executar: {}", context.getAllTestMethods().length);
        logger.info("Parallel mode: {}", context.getSuite().getParallel());
        logger.info("Thread count: {}", context.getSuite().getXmlSuite().getThreadCount());
        logger.info("###############################################\n");
    }

    /**
     * Chamado DEPOIS da suite de testes finalizar.
     * Exibe estatísticas consolidadas.
     *
     * @param context Contexto da suite com resultados consolidados
     */
    @Override
    public void onFinish(ITestContext context) {
        int totalTests = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();

        // Calcula duração total da suite
        long durationMs = context.getEndDate().getTime() - context.getStartDate().getTime();

        logger.info("###############################################");
        logger.info("FINALIZANDO SUITE DE TESTES: {}", context.getName());
        logger.info("-----------------------------------------------");
        logger.info("Total de Testes: {}", totalTests);
        logger.info("Sucessos: {} ({}%)", passed, calculatePercentage(passed, totalTests));
        logger.info("Falhas: {} ({}%)", failed, calculatePercentage(failed, totalTests));
        logger.info("Pulados: {} ({}%)", skipped, calculatePercentage(skipped, totalTests));
        logger.info("-----------------------------------------------");
        logger.info("Duração Total: {} ms ({} segundos)", durationMs, durationMs / 1000.0);
        logger.info("###############################################\n");

        // Alerta se houver falhas
        if (failed > 0) {
            logger.warn("ATENÇÃO: {} teste(s) falharam!", failed);
            logger.warn("Verifique os vídeos e screenshots em target/videos/ e target/screenshots/");
        }
    }

    /**
     * Calcula percentual com 2 casas decimais.
     *
     * @param part Parte do total
     * @param total Total
     * @return Percentual formatado (ex: "75.50")
     */
    private String calculatePercentage(int part, int total) {
        if (total == 0) {
            return "0.00";
        }
        double percentage = (part * 100.0) / total;
        return String.format("%.2f", percentage);
    }
}