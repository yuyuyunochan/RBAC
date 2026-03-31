import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundExecutorTest {

    private BackgroundExecutor backgroundExecutor;

    @BeforeEach
    void setUp() {
        backgroundExecutor = new BackgroundExecutor();
    }

    @Test
    @DisplayName("execute — выполняет одну задачу в фоне")
    void testExecuteSingleTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        backgroundExecutor.execute(() -> {
            latch.countDown();
        });

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed);
    }

    @Test
    @DisplayName("execute — выполняет несколько задач в фоне")
    void testExecuteMultipleTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        backgroundExecutor.execute(() -> latch.countDown());
        backgroundExecutor.execute(() -> latch.countDown());
        backgroundExecutor.execute(() -> latch.countDown());

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed);
    }

    @Test
    @DisplayName("execute — задача действительно изменяет данные")
    void testTaskChangesValue() throws InterruptedException {
        final int[] value = {0};
        CountDownLatch latch = new CountDownLatch(1);

        backgroundExecutor.execute(() -> {
            value[0] = 42;
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);

        assertEquals(42, value[0]);
    }

    @Test
    @DisplayName("shutdown — завершает executor без ошибок")
    void testShutdown() {
        assertDoesNotThrow(() -> {
            backgroundExecutor.shutdown();
        });
    }

    @Test
    @DisplayName("shutdownNow — аварийное завершение без ошибок")
    void testShutdownNow() {
        assertDoesNotThrow(() -> {
            backgroundExecutor.shutdownNow();
        });
    }

    @Test
    @DisplayName("awaitTermination — ожидает завершения executor")
    void testAwaitTermination() throws InterruptedException {
        backgroundExecutor.shutdown();
        boolean finished = backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS);
        assertTrue(finished);
    }
}