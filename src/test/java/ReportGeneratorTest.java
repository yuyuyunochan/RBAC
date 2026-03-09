import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();

        User vasilina = User.validate("vasilina_l", "Vasilina Larina", "vasilina@company.ru");
        User ivan = User.validate("ivan_Kydrya", "Ivan Kydrya", "ivan@company.ru");
        system.getUserManager().add(vasilina);
        system.getUserManager().add(ivan);

        Role manager = system.getRoleManager().findByName("Manager").get();
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Назначение");
        PermanentAssignment pa = new PermanentAssignment(vasilina, manager, meta);
        system.getAssignmentManager().add(pa);
    }

    @Test
    @DisplayName("generateUserReport — содержит всех пользователей")
    void testUserReport() {
        String report = ReportGenerator.generateUserReport(
                system.getUserManager(), system.getAssignmentManager());
        assertTrue(report.contains("admin"));
        assertTrue(report.contains("vasilina_l"));
        assertTrue(report.contains("ivan_Kydrya"));
        assertTrue(report.contains("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ"));
    }

    @Test
    @DisplayName("generateUserReport — содержит роли пользователей")
    void testUserReportRoles() {
        String report = ReportGenerator.generateUserReport(
                system.getUserManager(), system.getAssignmentManager());
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Manager"));
    }

    @Test
    @DisplayName("generateRoleReport — содержит все роли")
    void testRoleReport() {
        String report = ReportGenerator.generateRoleReport(
                system.getRoleManager(), system.getAssignmentManager());
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Manager"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("ОТЧЁТ ПО РОЛЯМ"));
    }

    @Test
    @DisplayName("generatePermissionMatrix — содержит пользователей и ресурсы")
    void testPermissionMatrix() {
        String matrix = ReportGenerator.generatePermissionMatrix(
                system.getUserManager(), system.getAssignmentManager());
        assertTrue(matrix.contains("admin"));
        assertTrue(matrix.contains("vasilina_l"));
        assertTrue(matrix.contains("МАТРИЦА ПРАВ"));
    }

    @Test
    @DisplayName("generateUserReport — пустая система")
    void testUserReportEmpty() {
        RBACSystem empty = new RBACSystem();
        String report = ReportGenerator.generateUserReport(
                empty.getUserManager(), empty.getAssignmentManager());
        assertTrue(report.contains("Пользователей нет"));
    }
}