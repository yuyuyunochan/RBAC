import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportGenerator {

    public static String generateUserReport(UserManager userManager,
                                            AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ ===\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("Пользователей нет.\n");
            return sb.toString();
        }

        for (User user : users) {
            sb.append("Пользователь: ").append(user.format()).append("\n");

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            if (assignments.isEmpty()) {
                sb.append("  Роли: нет назначений\n");
            } else {
                sb.append("  Роли:\n");
                for (RoleAssignment ra : assignments) {
                    String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                    sb.append("    - ").append(ra.role().getName())
                            .append(" [").append(ra.assignmentType()).append("] ")
                            .append(status).append("\n");
                }
            }

            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            if (!perms.isEmpty()) {
                sb.append("  Права (").append(perms.size()).append("):\n");
                for (Permission p : perms) {
                    sb.append("    - ").append(p.format()).append("\n");
                }
            }

            sb.append("\n");
        }

        sb.append("Всего пользователей: ").append(users.size()).append("\n");
        return sb.toString();
    }

    public static String generateRoleReport(RoleManager roleManager,
                                            AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ОТЧЁТ ПО РОЛЯМ ===\n\n");

        List<Role> roles = roleManager.findAll();

        if (roles.isEmpty()) {
            sb.append("Ролей нет.\n");
            return sb.toString();
        }

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);

            int activeCount = 0;
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    activeCount++;
                }
            }

            sb.append("Роль: ").append(role.getName()).append("\n");
            sb.append("  Описание: ").append(role.getDescription()).append("\n");
            sb.append("  Прав: ").append(role.getPermissions().size()).append("\n");
            sb.append("  Назначена пользователям: ").append(activeCount).append("\n");

            if (activeCount > 0) {
                sb.append("  Пользователи:\n");
                for (RoleAssignment ra : assignments) {
                    if (ra.isActive()) {
                        sb.append("    - ").append(ra.user().username()).append("\n");
                    }
                }
            }

            sb.append("\n");
        }

        sb.append("Всего ролей: ").append(roles.size()).append("\n");
        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager,
                                                  AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== МАТРИЦА ПРАВ ДОСТУПА ===\n\n");

        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            sb.append("Пользователей нет.\n");
            return sb.toString();
        }

        Set<String> allResources = new HashSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            for (Permission p : perms) {
                allResources.add(p.resource());
            }
        }

        List<String> resources = new ArrayList<>(allResources);

        if (resources.isEmpty()) {
            sb.append("Нет назначенных прав.\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s", "ПОЛЬЗОВАТЕЛЬ"));
        for (String res : resources) {
            sb.append(String.format("%-15s", res));
        }
        sb.append("\n");
        sb.append("-".repeat(20 + resources.size() * 15)).append("\n");

        for (User user : users) {
            sb.append(String.format("%-20s", user.username()));

            Set<Permission> userPerms = assignmentManager.getUserPermissions(user);

            for (String res : resources) {
                List<String> permNames = new ArrayList<>();
                for (Permission p : userPerms) {
                    if (p.resource().equals(res)) {
                        permNames.add(p.name());
                    }
                }

                if (permNames.isEmpty()) {
                    sb.append(String.format("%-15s", "-"));
                } else {
                    String joined = String.join(",", permNames);
                    if (joined.length() > 14) {
                        joined = joined.substring(0, 11) + "...";
                    }
                    sb.append(String.format("%-15s", joined));
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void exportToFile(String report, String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(report);
            writer.close();
            System.out.println("Отчёт сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения отчёта: " + e.getMessage());
        }
    }
}