import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final AuditLog auditLog;
    private final BackgroundExecutor backgroundExecutor;
    private final ScheduledExecutorService scheduledExecutorService;

    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.roleManager.setAssignmentManager(assignmentManager);

        this.auditLog = new AuditLog();
        this.backgroundExecutor = new BackgroundExecutor();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);

        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public BackgroundExecutor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void initialize() {
        Permission readUsers = new Permission("read", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("write", "users", "Редактирование пользователей");
        Permission deleteUsers = new Permission("delete", "users", "Удаление пользователей");

        Permission readRoles = new Permission("read", "roles", "Просмотр ролей");
        Permission writeRoles = new Permission("write", "roles", "Редактирование ролей");
        Permission deleteRoles = new Permission("delete", "roles", "Удаление ролей");

        Permission readReports = new Permission("read", "reports", "Просмотр отчётов");
        Permission writeReports = new Permission("write", "reports", "Редактирование отчётов");

        Role adminRole = new Role("Admin", "Полный доступ к системе");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        roleManager.add(adminRole);

        Role managerRole = new Role("Manager", "Управление пользователями и отчётами");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);
        roleManager.add(managerRole);

        Role viewerRole = new Role("Viewer", "Только просмотр");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readReports);
        roleManager.add(viewerRole);

        User admin = User.validate("admin", "System Administrator", "admin@system.com");
        userManager.add(admin);

        AssignmentMetadata metadata =
                AssignmentMetadata.now("system", "Начальная инициализация системы");
        PermanentAssignment assignment =
                new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(assignment);

        currentUser = "admin";

        auditLog.log("SYSTEM_INIT", "system", "system", "Система инициализирована");
    }

    public void startScheduledTasks(int periodSeconds) {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                List<RoleAssignment> expiredAssignments = assignmentManager.getExpiredAssignments();

                int expiredCount = 0;
                for (RoleAssignment assignment : expiredAssignments) {
                    if (assignment instanceof TemporaryAssignment temporaryAssignment) {
                        assignmentManager.remove(temporaryAssignment);
                        expiredCount++;
                    }
                }

                if (expiredCount > 0) {
                    auditLog.log(
                            "EXPIRED_ASSIGNMENTS_CLEANUP",
                            "scheduler",
                            "assignments",
                            "Удалено истёкших назначений: " + expiredCount
                    );
                }

                auditLog.log(
                        "SYSTEM_STATS",
                        "scheduler",
                        "system",
                        "Пользователей=" + userManager.count()
                                + ", ролей=" + roleManager.count()
                                + ", назначений=" + assignmentManager.count()
                );

            } catch (Exception e) {
                auditLog.log(
                        "SCHEDULER_ERROR",
                        "scheduler",
                        "system",
                        "Ошибка периодической задачи: " + e.getMessage()
                );
            }
        }, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void stopScheduledTasks() {
        scheduledExecutorService.shutdown();
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("****************************************\n");
        sb.append("*        СТАТИСТИКА СИСТЕМЫ            *\n");
        sb.append("****************************************\n");

        sb.append(" Пользователей: ").append(userManager.count()).append("\n");
        sb.append(" Ролей: ").append(roleManager.count()).append("\n");

        int totalAssignments = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();

        sb.append(" Назначений всего: ").append(totalAssignments).append("\n");
        sb.append(" Активных: ").append(activeAssignments).append("\n");
        sb.append("   Истёкших: ").append(expiredAssignments).append("\n");

        int userCount = userManager.count();
        if (userCount > 0) {
            double averageRoles = (double) activeAssignments / userCount;
            sb.append(" Среднее ролей на пользователя: ")
                    .append(String.format("%.1f", averageRoles))
                    .append("\n");
        }

        sb.append("\n");
        sb.append("****************************************\n");
        return sb.toString();
    }

    public void shutdown() {
        stopScheduledTasks();
        backgroundExecutor.shutdown();
        auditLog.shutdown();
    }
}