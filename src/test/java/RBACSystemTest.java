import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    @DisplayName("Конструктор — менеджеры инициализированы")
    void testConstructor() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
        assertEquals("system", system.getCurrentUser());
    }

    @Test
    @DisplayName("setCurrentUser и getCurrentUser")
    void testSetGetCurrentUser() {
        system.setCurrentUser("admin");
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    @DisplayName("initialize — создаёт пользователя admin")
    void testInitializeCreatesAdmin() {
        system.initialize();
        assertTrue(system.getUserManager().exists("admin"));
    }

    @Test
    @DisplayName("initialize — создаёт роль Admin")
    void testInitializeCreatesAdminRole() {
        system.initialize();
        assertTrue(system.getRoleManager().exists("Admin"));
    }

    @Test
    @DisplayName("initialize — создаёт роль Manager")
    void testInitializeCreatesManagerRole() {
        system.initialize();
        assertTrue(system.getRoleManager().exists("Manager"));
    }

    @Test
    @DisplayName("initialize — создаёт роль Viewer")
    void testInitializeCreatesViewerRole() {
        system.initialize();
        assertTrue(system.getRoleManager().exists("Viewer"));
    }

    @Test
    @DisplayName("initialize — назначает Admin роль пользователю admin")
    void testInitializeAssignsAdminRole() {
        system.initialize();
        var user = system.getUserManager().findByUsername("admin").get();
        var adminRole = system.getRoleManager().findByName("Admin").get();
        assertTrue(system.getAssignmentManager().userHasRole(user, adminRole));
    }

    @Test
    @DisplayName("initialize — у admin есть все права")
    void testInitializeAdminHasPermissions() {
        system.initialize();
        var user = system.getUserManager().findByUsername("admin").get();
        assertTrue(system.getAssignmentManager().userHasPermission(user, "READ", "users"));
        assertTrue(system.getAssignmentManager().userHasPermission(user, "WRITE", "users"));
        assertTrue(system.getAssignmentManager().userHasPermission(user, "DELETE", "users"));
    }

    @Test
    @DisplayName("initialize — currentUser устанавливается в admin")
    void testInitializeCurrentUser() {
        system.initialize();
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    @DisplayName("generateStatistics — содержит количество пользователей")
    void testGenerateStatisticsUsers() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Пользователей:"));
    }

    @Test
    @DisplayName("generateStatistics — содержит количество ролей")
    void testGenerateStatisticsRoles() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Ролей:"));
    }

    @Test
    @DisplayName("generateStatistics — содержит информацию о назначениях")
    void testGenerateStatisticsAssignments() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Назначений всего:"));
        assertTrue(stats.contains("Активных:"));
    }

}