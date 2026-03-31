import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogAsyncTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    @DisplayName("async log — добавляет запись в фоне")
    void testAsyncLog() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан пользователь");

        Thread.sleep(200);

        assertEquals(1, auditLog.size());
    }

    @Test
    @DisplayName("async log — несколько записей добавляются в фоне")
    void testAsyncLogMultiple() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "vasilina_l", "Назначена роль");
        auditLog.log("USER_DELETE", "admin", "ivan_kydrya", "Удалён");

        Thread.sleep(300);

        assertEquals(3, auditLog.size());
    }

    @Test
    @DisplayName("async getAll — возвращает все записи")
    void testAsyncGetAll() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "vasilina_l", "Назначена роль");

        Thread.sleep(300);

        List<AuditEntry> all = auditLog.getAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("async getByPerformer — фильтрация по исполнителю")
    void testAsyncGetByPerformer() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан");
        auditLog.log("USER_CREATE", "vasilina_l", "new_user", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "new_user", "Роль назначена");

        Thread.sleep(300);

        List<AuditEntry> byAdmin = auditLog.getByPerformer("admin");
        assertEquals(2, byAdmin.size());

        List<AuditEntry> byVasilina = auditLog.getByPerformer("vasilina_l");
        assertEquals(1, byVasilina.size());
    }

    @Test
    @DisplayName("async getByAction — фильтрация по действию")
    void testAsyncGetByAction() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "ivan_kydrya", "Роль назначена");
        auditLog.log("USER_CREATE", "admin", "vasilina_l", "Создана");

        Thread.sleep(300);

        List<AuditEntry> creates = auditLog.getByAction("USER_CREATE");
        assertEquals(2, creates.size());
    }

    @Test
    @DisplayName("async clear — очищает лог")
    void testAsyncClear() throws InterruptedException {
        auditLog.log("TEST", "admin", "target", "details");

        Thread.sleep(200);

        auditLog.clear();
        assertEquals(0, auditLog.size());
    }

    @Test
    @DisplayName("async AuditEntry.format — форматирование записи")
    void testAsyncEntryFormat() throws InterruptedException {
        auditLog.log("USER_CREATE", "admin", "ivan_kydrya", "Создан пользователь");

        Thread.sleep(200);

        AuditEntry entry = auditLog.getAll().get(0);
        String formatted = entry.format();

        assertTrue(formatted.contains("USER_CREATE"));
        assertTrue(formatted.contains("admin"));
        assertTrue(formatted.contains("ivan_kydrya"));
    }

    @Test
    @DisplayName("shutdown — логгер корректно завершает работу")
    void testShutdown() {
        assertDoesNotThrow(() -> {
            auditLog.shutdown();
        });
    }
}