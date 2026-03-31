import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledTasksTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("scheduled tasks — добавляют записи в audit log")
    void testScheduledTasksWriteAuditLog() throws InterruptedException {
        system.startScheduledTasks(1);

        TimeUnit.SECONDS.sleep(3);

        assertTrue(system.getAuditLog().size() > 0);

        system.shutdown();
    }

    @Test
    @DisplayName("scheduled tasks — не падают на пустой системе")
    void testScheduledTasksOnEmptySystem() throws InterruptedException {
        RBACSystem emptySystem = new RBACSystem();

        emptySystem.startScheduledTasks(1);

        TimeUnit.SECONDS.sleep(2);

        assertDoesNotThrow(() -> {
            emptySystem.shutdown();
        });
    }

    @Test
    @DisplayName("scheduled tasks — очищают истёкшие временные назначения")
    void testScheduledTasksRemoveExpiredAssignments() throws InterruptedException {
        User vasilina = User.validate("vasilina_k", "Vasilina Kovalenko", "vasilina@company.ru");
        system.getUserManager().add(vasilina);

        Role viewer = system.getRoleManager().findByName("Viewer").get();

        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Тест истечения");
        TemporaryAssignment expiredAssignment = new TemporaryAssignment(
                vasilina,
                viewer,
                metadata,
                "2020-01-01 00:00",
                false
        );

        system.getAssignmentManager().add(expiredAssignment);

        assertEquals(2, system.getAssignmentManager().count());

        system.startScheduledTasks(1);

        TimeUnit.SECONDS.sleep(3);

        assertEquals(1, system.getAssignmentManager().count());

        system.shutdown();
    }

    @Test
    @DisplayName("scheduled tasks — пишут статистику в лог")
    void testScheduledTasksWriteStatistics() throws InterruptedException {
        system.startScheduledTasks(1);

        TimeUnit.SECONDS.sleep(3);

        boolean foundStatsEntry = !system.getAuditLog()
                .getByAction("SYSTEM_STATS").isEmpty();

        assertTrue(foundStatsEntry);

        system.shutdown();
    }

    @Test
    @DisplayName("shutdown — корректно останавливает scheduled tasks")
    void testShutdownStopsScheduledTasks() {
        assertDoesNotThrow(() -> {
            system.startScheduledTasks(1);
            system.shutdown();
        });
    }
}