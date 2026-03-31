import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerServiceCommands(parser);
        registerAsyncCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser) {

        parser.registerCommand("user-list", "Список всех пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("Пользователей нет.");
                return;
            }

            System.out.printf("%-20s %-25s %-30s%n", "USERNAME", "FULL NAME", "EMAIL");
            System.out.println("-".repeat(75));
            for (User user : users) {
                System.out.printf("%-20s %-25s %-30s%n",
                        user.username(), user.fullName(), user.email());
            }
            System.out.println("Всего: " + users.size());
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Введите полное имя: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Введите email: ");
            String email = scanner.nextLine().trim();

            User user = User.validate(username, fullName, email);
            system.getUserManager().add(user);

            system.getAuditLog().log(
                    "USER_CREATE",
                    system.getCurrentUser(),
                    username,
                    "Создан пользователь"
            );

            System.out.println("Пользователь '" + username + "' успешно создан.");
        });

        parser.registerCommand("user-view", "Просмотр пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            User user = userOpt.get();
            System.out.println("Username: " + user.username());
            System.out.println("Full Name: " + user.fullName());
            System.out.println("Email: " + user.email());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            System.out.println("Назначения:");
            if (assignments.isEmpty()) {
                System.out.println("  Нет назначений.");
            } else {
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.role().getName()
                            + " [" + assignment.assignmentType() + "] "
                            + (assignment.isActive() ? "ACTIVE" : "INACTIVE"));
                }
            }
        });

        parser.registerCommand("user-update", "Обновить пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Введите новое полное имя: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Введите новый email: ");
            String email = scanner.nextLine().trim();

            system.getUserManager().update(username, fullName, email);

            system.getAuditLog().log(
                    "USER_UPDATE",
                    system.getCurrentUser(),
                    username,
                    "Обновлены данные пользователя"
            );

            System.out.println("Пользователь обновлён.");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            System.out.print("Подтвердите удаление (да/нет): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equals("да")) {
                System.out.println("Удаление отменено.");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }

            system.getUserManager().remove(user);

            system.getAuditLog().log(
                    "USER_DELETE",
                    system.getCurrentUser(),
                    username,
                    "Удалён пользователь и его назначения"
            );

            System.out.println("Пользователь удалён.");
        });

        parser.registerCommand("user-search", "Поиск пользователей", (scanner, system) -> {
            System.out.println("1. По username");
            System.out.println("2. По email домену");
            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.print("Введите часть username: ");
                String value = scanner.nextLine().trim();

                List<User> result = system.getUserManager()
                        .findByFilter(UserFilters.byUsernameContains(value));

                if (result.isEmpty()) {
                    System.out.println("Ничего не найдено.");
                    return;
                }

                for (User user : result) {
                    System.out.println(user.format());
                }
            } else if (choice.equals("2")) {
                System.out.print("Введите домен (например @company.com): ");
                String value = scanner.nextLine().trim();

                List<User> result = system.getUserManager()
                        .findByFilter(UserFilters.byEmailDomain(value));

                if (result.isEmpty()) {
                    System.out.println("Ничего не найдено.");
                    return;
                }

                for (User user : result) {
                    System.out.println(user.format());
                }
            } else {
                System.out.println("Неверный выбор.");
            }
        });
    }

    private static void registerRoleCommands(CommandParser parser) {

        parser.registerCommand("role-list", "Список ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Ролей нет.");
                return;
            }

            for (Role role : roles) {
                System.out.println(role.getName()
                        + " | permissions: " + role.getPermissions().size()
                        + " | id: " + role.getId());
            }
        });

        parser.registerCommand("role-create", "Создать роль", (scanner, system) -> {
            System.out.print("Введите название роли: ");
            String name = scanner.nextLine().trim();

            System.out.print("Введите описание роли: ");
            String description = scanner.nextLine().trim();

            Role role = new Role(name, description);
            system.getRoleManager().add(role);

            system.getAuditLog().log(
                    "ROLE_CREATE",
                    system.getCurrentUser(),
                    name,
                    "Создана роль"
            );

            System.out.println("Роль создана.");
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена.");
                return;
            }

            System.out.println(roleOpt.get().format());
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + name + "' не найдена.");
                return;
            }

            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            boolean hasActiveAssignments = assignments.stream().anyMatch(RoleAssignment::isActive);

            if (hasActiveAssignments) {
                System.out.println("Нельзя удалить роль, она назначена пользователям.");
                return;
            }

            System.out.print("Вы уверены? Введите 'да': ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equals("да")) {
                System.out.println("Удаление отменено.");
                return;
            }

            for (RoleAssignment assignment : assignments) {
                system.getAssignmentManager().remove(assignment);
            }

            system.getRoleManager().remove(role);

            system.getAuditLog().log(
                    "ROLE_DELETE",
                    system.getCurrentUser(),
                    name,
                    "Роль удалена"
            );

            System.out.println("Роль удалена.");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            System.out.print("Введите permission name: ");
            String permissionName = scanner.nextLine().trim();

            System.out.print("Введите resource: ");
            String resource = scanner.nextLine().trim();

            System.out.print("Введите description: ");
            String description = scanner.nextLine().trim();

            Permission permission = new Permission(permissionName, resource, description);
            system.getRoleManager().addPermissionToRole(roleName, permission);

            System.out.println("Право добавлено.");
        });
    }

    private static void registerAssignmentCommands(CommandParser parser) {

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Ролей нет.");
                return;
            }

            for (int i = 0; i < roles.size(); i++) {
                System.out.println((i + 1) + ". " + roles.get(i).getName());
            }

            System.out.print("Выберите номер роли: ");
            int number = Integer.parseInt(scanner.nextLine().trim());

            if (number < 1 || number > roles.size()) {
                System.out.println("Неверный номер.");
                return;
            }

            Role role = roles.get(number - 1);

            System.out.print("Тип назначения (1 - постоянное, 2 - временное): ");
            String type = scanner.nextLine().trim();

            System.out.print("Введите причину: ");
            String reason = scanner.nextLine().trim();

            AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);

            if (type.equals("2")) {
                System.out.print("Введите дату истечения (yyyy-MM-dd HH:mm): ");
                String expiresAt = scanner.nextLine().trim();

                TemporaryAssignment assignment =
                        new TemporaryAssignment(userOpt.get(), role, metadata, expiresAt, false);

                system.getAssignmentManager().add(assignment);

                system.getAuditLog().log(
                        "ROLE_ASSIGN",
                        system.getCurrentUser(),
                        username,
                        "Назначена временная роль " + role.getName()
                );

                System.out.println("Временное назначение создано.");
            } else {
                PermanentAssignment assignment =
                        new PermanentAssignment(userOpt.get(), role, metadata);

                system.getAssignmentManager().add(assignment);

                system.getAuditLog().log(
                        "ROLE_ASSIGN",
                        system.getCurrentUser(),
                        username,
                        "Назначена постоянная роль " + role.getName()
                );

                System.out.println("Постоянное назначение создано.");
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager()
                    .findByUser(userOpt.get())
                    .stream()
                    .filter(RoleAssignment::isActive)
                    .toList();

            if (assignments.isEmpty()) {
                System.out.println("Нет активных назначений.");
                return;
            }

            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment assignment = assignments.get(i);
                System.out.println((i + 1) + ". "
                        + assignment.role().getName()
                        + " [" + assignment.assignmentType() + "]");
            }

            System.out.print("Выберите номер: ");
            int number = Integer.parseInt(scanner.nextLine().trim());

            if (number < 1 || number > assignments.size()) {
                System.out.println("Неверный номер.");
                return;
            }

            RoleAssignment selected = assignments.get(number - 1);

            if (selected instanceof PermanentAssignment permanentAssignment) {
                permanentAssignment.revoke();
            } else {
                system.getAssignmentManager().remove(selected);
            }

            system.getAuditLog().log(
                    "ROLE_REVOKE",
                    system.getCurrentUser(),
                    username,
                    "Отозвана роль " + selected.role().getName()
            );

            System.out.println("Роль отозвана.");
        });

        parser.registerCommand("assignment-list", "Список назначений", (scanner, system) -> {
            List<RoleAssignment> all = system.getAssignmentManager().findAll();
            if (all.isEmpty()) {
                System.out.println("Назначений нет.");
                return;
            }

            for (RoleAssignment assignment : all) {
                System.out.println(
                        assignment.user().username() + " -> "
                                + assignment.role().getName() + " | "
                                + assignment.assignmentType() + " | "
                                + (assignment.isActive() ? "ACTIVE" : "INACTIVE")
                );
            }
        });
    }

    private static void registerPermissionCommands(CommandParser parser) {
        parser.registerCommand("permissions-user", "Показать все права пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            Set<Permission> permissions = system.getAssignmentManager()
                    .getUserPermissions(userOpt.get());

            if (permissions.isEmpty()) {
                System.out.println("У пользователя нет прав.");
                return;
            }

            for (Permission permission : permissions) {
                System.out.println(permission.format());
            }
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден.");
                return;
            }

            System.out.print("Введите permission name: ");
            String permissionName = scanner.nextLine().trim();

            System.out.print("Введите resource: ");
            String resource = scanner.nextLine().trim();

            boolean result = system.getAssignmentManager()
                    .userHasPermission(userOpt.get(), permissionName, resource);

            if (result) {
                System.out.println("Пользователь имеет это право.");
            } else {
                System.out.println("Пользователь НЕ имеет это право.");
            }
        });
    }

    private static void registerServiceCommands(CommandParser parser) {

        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("audit-log", "Показать журнал аудита", (scanner, system) -> {
            system.getAuditLog().printLog();
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for (int i = 0; i < 30; i++) {
                System.out.println();
            }
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Вы уверены что хотите выйти? (да/нет): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equals("да")) {
                system.shutdown();
                System.out.println("До свидания!");
                System.exit(0);
            } else {
                System.out.println("Выход отменён.");
            }
        });
    }

    private static void registerAsyncCommands(CommandParser parser) {

        parser.registerCommand("report-users-async", "Сформировать отчёт по пользователям в фоне",
                (scanner, system) -> {
                    System.out.println("Запущена фоновая генерация отчёта...");

                    system.getBackgroundExecutor().execute(() -> {
                        String report = ReportGenerator.generateUserReportParallel(
                                system.getUserManager(),
                                system.getAssignmentManager()
                        );

                        ReportGenerator.exportToFile(report, "report_users_async.txt");

                        system.getAuditLog().log(
                                "ASYNC_REPORT_USERS",
                                system.getCurrentUser(),
                                "report_users_async.txt",
                                "Сформирован отчёт по пользователям в фоне"
                        );

                        System.out.println("[ФОН] Отчёт по пользователям сохранён в report_users_async.txt");
                    });
                });
    }
}