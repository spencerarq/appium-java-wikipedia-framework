package com.wikipedia.unit;

import com.wikipedia.commons.DriverFactory;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Testes unitários para DriverFactory.
 * Foca em testar a lógica interna SEM dependências externas (Appium Server).
 *
 * Testa:
 * - Singleton pattern
 * - ThreadLocal isolation
 * - Métodos públicos: getInstance(), getDriver(), setDriver(), quitDriver()
 *
 * @author Renato Spencer
 * @version 2.0
 * @since 2025-11-30
 */
@Tag("unit")
class DriverFactoryUnitTest {

    private DriverFactory driverFactory;

    @BeforeEach
    void setUp() {
        driverFactory = DriverFactory.getInstance();
    }

    @AfterEach
    void tearDown() {
        // Limpa o driver após cada teste
        driverFactory.quitDriver();
    }

    /**
     * Testa se getInstance() sempre retorna a mesma instância (Singleton).
     */
    @Test
    @DisplayName("getInstance() deve retornar sempre a mesma instância (Singleton)")
    void testGetInstance_ReturnsSameInstance() {
        DriverFactory instance1 = DriverFactory.getInstance();
        DriverFactory instance2 = DriverFactory.getInstance();
        DriverFactory instance3 = DriverFactory.getInstance();

        assertNotNull(instance1, "getInstance() não deve retornar null");
        assertNotNull(instance2, "getInstance() não deve retornar null");
        assertNotNull(instance3, "getInstance() não deve retornar null");

        assertSame(instance1, instance2, "getInstance() deve retornar sempre a mesma instância");
        assertSame(instance2, instance3, "getInstance() deve retornar sempre a mesma instância");
        assertSame(instance1, driverFactory, "getInstance() deve retornar a instância do Singleton");
    }

    /**
     * Testa getDriver() quando não há driver inicializado.
     */
    @Test
    @DisplayName("getDriver() deve retornar null quando não há driver")
    void testGetDriver_ReturnsNullWhenNoDriver() {
        AppiumDriver driver = driverFactory.getDriver();
        assertNull(driver, "getDriver() deve retornar null quando não há driver inicializado");
    }

    /**
     * Testa setDriver() e getDriver().
     */
    @Test
    @DisplayName("setDriver() e getDriver() devem funcionar corretamente")
    void testSetDriverAndGetDriver() {
        // Cria um mock (não precisa ser funcional para teste unitário)
        AppiumDriver mockDriver = mock(AppiumDriver.class);

        // Define o driver
        driverFactory.setDriver(mockDriver);

        // Recupera o driver
        AppiumDriver retrievedDriver = driverFactory.getDriver();

        assertNotNull(retrievedDriver, "getDriver() não deve retornar null após setDriver()");
        assertSame(mockDriver, retrievedDriver, "getDriver() deve retornar o driver configurado via setDriver()");
    }

    /**
     * Testa quitDriver() quando há um driver.
     */
    @Test
    @DisplayName("quitDriver() deve remover o driver do ThreadLocal")
    void testQuitDriver_WithDriver() {
        // Configura um mock
        AppiumDriver mockDriver = mock(AppiumDriver.class);
        driverFactory.setDriver(mockDriver);

        // Verifica que o driver foi configurado
        assertNotNull(driverFactory.getDriver(), "Driver deve estar presente antes de quitDriver()");

        // Chama quitDriver()
        driverFactory.quitDriver();

        // Verifica que o driver foi removido
        assertNull(driverFactory.getDriver(), "getDriver() deve retornar null após quitDriver()");
    }

    /**
     * Testa quitDriver() quando não há driver (não deve lançar exceção).
     */
    @Test
    @DisplayName("quitDriver() não deve lançar exceção quando não há driver")
    void testQuitDriver_WithoutDriver() {
        // Garante que não há driver
        assertNull(driverFactory.getDriver());

        // Não deve lançar exceção
        assertDoesNotThrow(() -> driverFactory.quitDriver(),
                "quitDriver() deve ser seguro quando não há driver");

        // Deve continuar retornando null
        assertNull(driverFactory.getDriver());
    }

    /**
     * Testa quitDriver() múltiplas vezes (idempotência).
     */
    @Test
    @DisplayName("quitDriver() deve ser idempotente")
    void testQuitDriver_Idempotent() {
        AppiumDriver mockDriver = mock(AppiumDriver.class);
        driverFactory.setDriver(mockDriver);

        // Primeira chamada
        driverFactory.quitDriver();
        assertNull(driverFactory.getDriver());

        // Segunda chamada (não deve falhar)
        assertDoesNotThrow(() -> driverFactory.quitDriver());

        // Terceira chamada (não deve falhar)
        assertDoesNotThrow(() -> driverFactory.quitDriver());
    }

    /**
     * Testa isolamento entre threads (ThreadLocal).
     * Cada thread deve ter seu próprio driver.
     */
    @Test
    @DisplayName("ThreadLocal deve isolar drivers entre threads diferentes")
    void testThreadLocalIsolation() throws InterruptedException {
        AppiumDriver mockDriver1 = mock(AppiumDriver.class);
        AppiumDriver mockDriver2 = mock(AppiumDriver.class);

        // Array para capturar resultados das threads
        final AppiumDriver[] thread1Result = new AppiumDriver[1];
        final AppiumDriver[] thread2Result = new AppiumDriver[1];
        final boolean[] thread1Success = {false};
        final boolean[] thread2Success = {false};

        // Thread 1 configura seu driver
        Thread thread1 = new Thread(() -> {
            driverFactory.setDriver(mockDriver1);
            thread1Result[0] = driverFactory.getDriver();
            thread1Success[0] = (thread1Result[0] == mockDriver1);
        });

        // Thread 2 configura seu driver
        Thread thread2 = new Thread(() -> {
            driverFactory.setDriver(mockDriver2);
            thread2Result[0] = driverFactory.getDriver();
            thread2Success[0] = (thread2Result[0] == mockDriver2);
        });

        // Executa as threads
        thread1.start();
        thread2.start();

        // Aguarda conclusão
        thread1.join();
        thread2.join();

        // Verifica que cada thread viu seu próprio driver
        assertTrue(thread1Success[0], "Thread 1 deve ter visto mockDriver1");
        assertTrue(thread2Success[0], "Thread 2 deve ter visto mockDriver2");
        assertSame(mockDriver1, thread1Result[0], "Thread 1 deve ter retornado mockDriver1");
        assertSame(mockDriver2, thread2Result[0], "Thread 2 deve ter retornado mockDriver2");

        // Thread principal não deve ter driver configurado pelas outras threads
        assertNull(driverFactory.getDriver(),
                "Thread principal não deve ver os drivers configurados em outras threads");
    }

    /**
     * Testa se setDriver(null) limpa o ThreadLocal.
     */
    @Test
    @DisplayName("setDriver(null) deve permitir configurar driver como null")
    void testSetDriver_Null() {
        AppiumDriver mockDriver = mock(AppiumDriver.class);

        // Configura um driver
        driverFactory.setDriver(mockDriver);
        assertNotNull(driverFactory.getDriver());

        // Configura como null
        driverFactory.setDriver(null);
        assertNull(driverFactory.getDriver(), "setDriver(null) deve limpar o ThreadLocal");
    }

    /**
     * Testa que o Singleton é thread-safe.
     */
    @Test
    @DisplayName("getInstance() deve ser thread-safe")
    void testGetInstance_ThreadSafe() throws InterruptedException {
        final DriverFactory[] instances = new DriverFactory[10];
        Thread[] threads = new Thread[10];

        // Cria 10 threads que chamam getInstance() simultaneamente
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = DriverFactory.getInstance();
            });
        }

        // Inicia todas as threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Aguarda todas as threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Verifica que todas as instâncias são a mesma
        DriverFactory firstInstance = instances[0];
        for (int i = 1; i < 10; i++) {
            assertSame(firstInstance, instances[i],
                    "Todas as threads devem receber a mesma instância do Singleton");
        }
    }
}