import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadConcurrencyTest {

    @Test
    void stressTestManagers() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();

        int threadsCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadsCount);

        for (int i = 0; i < threadsCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    String username = "vasilina_" + index;
                    User user = User.validate(username,
                            "Vasilina " + index,
                            "vasilina" + index + "@company.ru");

                    try {
                        system.getUserManager().add(user);
                    } catch (Exception ignored) {
                    }

                    system.getUserManager().findByUsername(username);
                    system.getUserManager().findByFilterParallel(
                            UserFilters.byUsernameContains("vasilina"));

                    Role viewer = system.getRoleManager().findByName("Viewer").orElse(null);
                    if (viewer != null && system.getUserManager().exists(username)) {
                        try {
                            AssignmentMetadata metadata = AssignmentMetadata.now(
                                    "admin",
                                    "нагрузочный тест");
                            PermanentAssignment assignment =
                                    new PermanentAssignment(user, viewer, metadata);
                            system.getAssignmentManager().add(assignment);
                        } catch (Exception ignored) {
                        }
                    }

                } catch (Exception ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executorService.shutdown();

        assertTrue(system.getUserManager().count() >= 1);
        assertTrue(system.getRoleManager().count() >= 3);
    }
}