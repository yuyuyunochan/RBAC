import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    @DisplayName("log — добавляет запись")
    void testLog() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан пользователь");
        assertEquals(1, auditLog.size());
    }

    @Test
    @DisplayName("log — несколько записей")
    void testLogMultiple() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "vasilina_l", "Назначена роль");
        auditLog.log("USER_DELETE", "admin", "ivan_Kydrya", "Удалён");
        assertEquals(3, auditLog.size());
    }

    @Test
    @DisplayName("getAll — возвращает все записи")
    void testGetAll() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "vasilina_l", "Назначена роль");
        List<AuditEntry> all = auditLog.getAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("getByPerformer — фильтрация по исполнителю")
    void testGetByPerformer() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан");
        auditLog.log("USER_CREATE", "vasilina_l", "new_user", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "new_user", "Роль назначена");

        List<AuditEntry> byAdmin = auditLog.getByPerformer("admin");
        assertEquals(2, byAdmin.size());

        List<AuditEntry> byVasilina = auditLog.getByPerformer("vasilina_l");
        assertEquals(1, byVasilina.size());
    }

    @Test
    @DisplayName("getByAction — фильтрация по действию")
    void testGetByAction() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан");
        auditLog.log("ROLE_ASSIGN", "admin", "ivan_Kydrya", "Роль назначена");
        auditLog.log("USER_CREATE", "admin", "vasilina_l", "Создана");

        List<AuditEntry> creates = auditLog.getByAction("USER_CREATE");
        assertEquals(2, creates.size());
    }

    @Test
    @DisplayName("clear — очищает лог")
    void testClear() {
        auditLog.log("TEST", "admin", "target", "details");
        auditLog.clear();
        assertEquals(0, auditLog.size());
    }

    @Test
    @DisplayName("AuditEntry.format — форматирование записи")
    void testEntryFormat() {
        auditLog.log("USER_CREATE", "admin", "ivan_Kydrya", "Создан пользователь");
        AuditEntry entry = auditLog.getAll().get(0);
        String formatted = entry.format();
        assertTrue(formatted.contains("USER_CREATE"));
        assertTrue(formatted.contains("admin"));
        assertTrue(formatted.contains("ivan_Kydrya"));
    }
}