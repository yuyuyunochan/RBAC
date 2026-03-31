import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ReportGenerator {

    public static String generateUserReport(UserManager userManager,
                                            AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователей нет.\n");
            return sb.toString();
        }

        for (User user : users) {
            appendSingleUserReport(sb, user, assignmentManager);
        }

        sb.append("Всего пользователей: ").append(users.size()).append("\n");
        return sb.toString();
    }

    public static String generateUserReportParallel(UserManager userManager,
                                                    AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            return "ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ\n\nПользователей нет.\n";
        }

        String body = users.parallelStream()
                .map(user -> {
                    StringBuilder sb = new StringBuilder();
                    appendSingleUserReport(sb, user, assignmentManager);
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));

        return "ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ (PARALLEL)\n\n"
                + body
                + "\nВсего пользователей: " + users.size() + "\n";
    }

    private static void appendSingleUserReport(StringBuilder sb,
                                               User user,
                                               AssignmentManager assignmentManager) {
        sb.append("Пользователь: ").append(user.format()).append("\n");

        List<RoleAssignment> assignments = assignmentManager.findByUser(user);
        if (assignments.isEmpty()) {
            sb.append("  Роли: нет назначений\n");
        } else {
            sb.append("  Роли:\n");
            for (RoleAssignment assignment : assignments) {
                String status = assignment.isActive() ? "ACTIVE" : "INACTIVE";
                sb.append("    - ")
                        .append(assignment.role().getName())
                        .append(" [")
                        .append(assignment.assignmentType())
                        .append("] ")
                        .append(status)
                        .append("\n");
            }
        }

        Set<Permission> permissions = assignmentManager.getUserPermissions(user);
        if (!permissions.isEmpty()) {
            sb.append("  Права (").append(permissions.size()).append("):\n");
            for (Permission permission : permissions) {
                sb.append("    - ").append(permission.format()).append("\n");
            }
        }

        sb.append("\n");
    }

    public static String generateRoleReport(RoleManager roleManager,
                                            AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("ОТЧЁТ ПО РОЛЯМ\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("Ролей нет.\n");
            return sb.toString();
        }

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);

            int activeCount = 0;
            for (RoleAssignment assignment : assignments) {
                if (assignment.isActive()) {
                    activeCount++;
                }
            }

            sb.append("Роль: ").append(role.getName()).append("\n");
            sb.append("  Описание: ").append(role.getDescription()).append("\n");
            sb.append("  Количество прав: ").append(role.getPermissions().size()).append("\n");
            sb.append("  Назначена пользователям: ").append(activeCount).append("\n\n");
        }

        sb.append("Всего ролей: ").append(roles.size()).append("\n");
        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager,
                                                  AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("МАТРИЦА ПРАВ\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Пользователей нет.\n");
            return sb.toString();
        }

        Set<String> resources = new ConcurrentSkipListSet<>();
        for (User user : users) {
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            for (Permission permission : permissions) {
                resources.add(permission.resource());
            }
        }

        if (resources.isEmpty()) {
            sb.append("Нет назначенных прав.\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s", "USERNAME"));
        for (String resource : resources) {
            sb.append(String.format("%-20s", resource));
        }
        sb.append("\n");
        sb.append("-".repeat(20 + resources.size() * 20)).append("\n");

        for (User user : users) {
            sb.append(String.format("%-20s", user.username()));
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);

            for (String resource : resources) {
                List<String> permissionNames = new ArrayList<>();
                for (Permission permission : permissions) {
                    if (permission.resource().equals(resource)) {
                        permissionNames.add(permission.name());
                    }
                }

                if (permissionNames.isEmpty()) {
                    sb.append(String.format("%-20s", "-"));
                } else {
                    String text = String.join(",", permissionNames);
                    if (text.length() > 18) {
                        text = text.substring(0, 15) + "...";
                    }
                    sb.append(String.format("%-20s", text));
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String generatePermissionMatrixParallel(UserManager userManager,
                                                          AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();

        if (users.isEmpty()) {
            return "МАТРИЦА ПРАВ\n\nПользователей нет.\n";
        }

        Set<String> resources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));

        if (resources.isEmpty()) {
            return "МАТРИЦА ПРАВ\n\nНет назначенных прав.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" МАТРИЦА ПРАВ (PARALLEL) \n\n");

        sb.append(String.format("%-20s", "USERNAME"));
        for (String resource : resources) {
            sb.append(String.format("%-20s", resource));
        }
        sb.append("\n");
        sb.append("-".repeat(20 + resources.size() * 20)).append("\n");

        List<String> rows = users.parallelStream()
                .map(user -> {
                    StringBuilder row = new StringBuilder();
                    row.append(String.format("%-20s", user.username()));

                    Set<Permission> permissions = assignmentManager.getUserPermissions(user);

                    for (String resource : resources) {
                        List<String> names = permissions.stream()
                                .filter(permission -> permission.resource().equals(resource))
                                .map(Permission::name)
                                .toList();

                        if (names.isEmpty()) {
                            row.append(String.format("%-20s", "-"));
                        } else {
                            String text = String.join(",", names);
                            if (text.length() > 18) {
                                text = text.substring(0, 15) + "...";
                            }
                            row.append(String.format("%-20s", text));
                        }
                    }

                    return row.toString();
                })
                .toList();

        for (String row : rows) {
            sb.append(row).append("\n");
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