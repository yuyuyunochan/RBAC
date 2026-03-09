import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    }

    //команды пользователей
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
            system.getAuditLog().log("USER_CREATE", system.getCurrentUser(),
                    username, "Создан пользователь: " + fullName);
            System.out.println("Пользователь '" + username + "' успешно создан.");
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }

            User user = userOpt.get();
            System.out.println("\nИнформация о пользователе");
            System.out.println("Username:  " + user.username());
            System.out.println("Полное имя: " + user.fullName());
            System.out.println("Email:     " + user.email());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            System.out.println("\nНазначенные роли (" + assignments.size() + "):");
            for (RoleAssignment ra : assignments) {
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.println("  - " + ra.role().getName()
                        + " [" + ra.assignmentType() + "] " + status);
            }

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            System.out.println("\nВсе права (" + permissions.size() + "):");
            for (Permission p : permissions) {
                System.out.println("  - " + p.format());
            }
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            if (!system.getUserManager().exists(username)) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }

            System.out.print("Введите новое полное имя: ");
            String fullName = scanner.nextLine().trim();
            System.out.print("Введите новый email: ");
            String email = scanner.nextLine().trim();

            system.getUserManager().update(username, fullName, email);
            System.out.println("Пользователь '" + username + "' обновлён.");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }

            System.out.print("Вы уверены? Введите 'да' для подтверждения: ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equals("да")) {
                System.out.println("Удаление отменено");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment ra : assignments) {
                system.getAssignmentManager().remove(ra);
            }
            system.getUserManager().remove(user);
            system.getAuditLog().log("USER_DELETE", system.getCurrentUser(),
                    username, "Пользователь удалён");
            System.out.println("Пользователь '" + username + "' удалён.");
        });

        parser.registerCommand("user-search", "Поиск пользователей", (scanner, system) -> {
            System.out.println("Выберите фильтр:");
            System.out.println("  1. По username (содержит)");
            System.out.println("  2. По email (содержит)");
            System.out.println("  3. По домену email");
            System.out.println("  4. По полному имени (содержит)");
            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            System.out.print("Введите значение для поиска: ");
            String value = scanner.nextLine().trim();

            UserFilter filter;
            switch (choice) {
                case "1":
                    filter = UserFilters.byUsernameContains(value);
                    break;
                case "2":
                    filter = UserFilters.byEmail(value);
                    break;
                case "3":
                    filter = UserFilters.byEmailDomain(value);
                    break;
                case "4":
                    filter = UserFilters.byFullNameContains(value);
                    break;
                default:
                    System.out.println("Неверный выбор.");
                    return;
            }

            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("Ничего не найдено");
                return;
            }

            System.out.printf("%-20s %-25s %-30s%n", "USERNAME", "FULL NAME", "EMAIL");
            System.out.println("-".repeat(75));
            for (User user : results) {
                System.out.printf("%-20s %-25s %-30s%n",
                        user.username(), user.fullName(), user.email());
            }
            System.out.println("Найдено: " + results.size());
        });
    }

    //команды ролей
    private static void registerRoleCommands(CommandParser parser) {

        parser.registerCommand("role-list", "Список всех ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Ролей нет.");
                return;
            }
            System.out.printf("%-20s %-10s %-15s%n", "НАЗВАНИЕ", "ПРАВ", "ID");
            System.out.println("-".repeat(45));
            for (Role role : roles) {
                System.out.printf("%-20s %-10d %-15s%n",
                        role.getName(), role.getPermissions().size(), role.getId());
            }
            System.out.println("Всего: " + roles.size());
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.print("Введите название роли: ");
            String name = scanner.nextLine().trim();
            System.out.print("Введите описание роли: ");
            String description = scanner.nextLine().trim();

            Role role = new Role(name, description);
            system.getRoleManager().add(role);
            system.getAuditLog().log("ROLE_CREATE", system.getCurrentUser(),
                    name, "Создана роль: " + description);
            System.out.println("Роль '" + name + "' создана.");

            while (true) {
                System.out.print("Добавить право? (да/нет): ");
                String answer = scanner.nextLine().trim();
                if (!answer.equals("да")) break;
                System.out.print("  Название права: ");
                String permName = scanner.nextLine().trim();
                System.out.print("  Ресурс: ");
                String resource = scanner.nextLine().trim();
                System.out.print("  Описание: ");
                String permDesc = scanner.nextLine().trim();
                try {
                    Permission perm = new Permission(permName, resource, permDesc);
                    role.addPermission(perm);
                    System.out.println("  Право добавлено: " + perm.format());
                } catch (IllegalArgumentException e) {
                    System.out.println("  Ошибка: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + name + "' не найдена");
                return;
            }
            System.out.println(roleOpt.get().format());
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + name + "' не найдена.");
                return;
            }

            System.out.println("Обновление роли '" + name + "'");
            System.out.println("(Для изменения названия/описания нужно пересоздать роль)");
            System.out.println("Текущая информация:");
            System.out.println(roleOpt.get().format());
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String name = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + name + "' не найдена");
                return;
            }

            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            List<RoleAssignment> activeAssignments = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    activeAssignments.add(ra);
                }
            }

            if (!activeAssignments.isEmpty()) {
                System.out.println("Роль назначена пользователям:");
                for (RoleAssignment ra : activeAssignments) {
                    System.out.println("  - " + ra.user().username());
                }
            }

            System.out.print("Вы уверены? Введите 'да' для подтверждения: ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equals("да")) {
                System.out.println("Удаление отменено");
                return;
            }

            for (RoleAssignment ra : assignments) {
                system.getAssignmentManager().remove(ra);
            }

            system.getRoleManager().remove(role);
            System.out.println("Роль '" + name + "' удалена");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + roleName + "' не найдена");
                return;
            }

            System.out.print("Название права (например read): ");
            String permName = scanner.nextLine().trim();
            System.out.print("Ресурс (например users): ");
            String resource = scanner.nextLine().trim();
            System.out.print("Описание: ");
            String description = scanner.nextLine().trim();

            Permission perm = new Permission(permName, resource, description);
            system.getRoleManager().addPermissionToRole(roleName, perm);
            System.out.println("Право добавлено: " + perm.format());
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + roleName + "' не найдена.");
                return;
            }

            Role role = roleOpt.get();
            List<Permission> permList = new ArrayList<>(role.getPermissions());

            if (permList.isEmpty()) {
                System.out.println("У роли нет прав");
                return;
            }

            System.out.println("Права роли '" + roleName + "':");
            for (int i = 0; i < permList.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + permList.get(i).format());
            }

            System.out.print("Введите номер права для удаления: ");
            String numStr = scanner.nextLine().trim();
            int num;
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                System.out.println("Неверный номер");
                return;
            }

            if (num < 1 || num > permList.size()) {
                System.out.println("Номер вне диапазона");
                return;
            }

            Permission toRemove = permList.get(num - 1);
            role.removePermission(toRemove);
            System.out.println("Право удалено: " + toRemove.format());
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.println("Выберите фильтр:");
            System.out.println("  1. По имени (содержит)");
            System.out.println("  2. По наличию права");
            System.out.println("  3. По минимальному количеству прав");
            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            RoleFilter filter;
            switch (choice) {
                case "1":
                    System.out.print("Введите подстроку имени: ");
                    String substring = scanner.nextLine().trim();
                    filter = RoleFilters.byNameContains(substring);
                    break;
                case "2":
                    System.out.print("Введите название права: ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Введите ресурс: ");
                    String resource = scanner.nextLine().trim();
                    filter = RoleFilters.hasPermission(permName, resource);
                    break;
                case "3":
                    System.out.print("Введите минимальное количество прав: ");
                    String numStr = scanner.nextLine().trim();
                    int minPerms = Integer.parseInt(numStr);
                    filter = RoleFilters.hasAtLeastNPermissions(minPerms);
                    break;
                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("Ничего не найдено");
                return;
            }

            for (Role role : results) {
                System.out.println("  " + role.getName() + " ("
                        + role.getPermissions().size() + " прав)");
            }
            System.out.println("Найдено: " + results.size());
        });
    }
    //команды назначения
    private static void registerAssignmentCommands(CommandParser parser) {

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден");
                return;
            }
            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            System.out.println("Доступные роли:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + roles.get(i).getName());
            }
            System.out.print("Выберите номер роли: ");
            String numStr = scanner.nextLine().trim();
            int num;
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                System.out.println("Неверный номер");
                return;
            }
            if (num < 1 || num > roles.size()) {
                System.out.println("Номер вне диапазона");
                return;
            }
            Role role = roles.get(num - 1);

            System.out.print("Тип назначения (1 — постоянное, 2 — временное): ");
            String typeChoice = scanner.nextLine().trim();

            System.out.print("Причина назначения: ");
            String reason = scanner.nextLine().trim();

            AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);

            if (typeChoice.equals("2")) {
                System.out.print("Дата истечения (yyyy-MM-dd HH:mm): ");
                String expiresAt = scanner.nextLine().trim();
                System.out.print("Автопродление? (да/нет): ");
                boolean autoRenew = scanner.nextLine().trim().equals("да");

                TemporaryAssignment ta = new TemporaryAssignment(
                        user, role, meta, expiresAt, autoRenew);
                system.getAssignmentManager().add(ta);
                System.out.println("Временное назначение создано.");
                system.getAuditLog().log("ROLE_ASSIGN", system.getCurrentUser(),
                        username, "Назначена роль: " + role + " (временно до " + expiresAt + ")");
            } else {
                PermanentAssignment pa = new PermanentAssignment(user, role, meta);
                system.getAssignmentManager().add(pa);
                System.out.println("Постоянное назначение создано");
                system.getAuditLog().log("ROLE_ASSIGN", system.getCurrentUser(),
                        username, "Назначена роль: " + role + " (постоянно)");
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден");
                return;
            }
            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            List<RoleAssignment> activeAssignments = new ArrayList<>();
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    activeAssignments.add(ra);
                }
            }

            if (activeAssignments.isEmpty()) {
                System.out.println("У пользователя нет активных назначений");
                return;
            }

            System.out.println("Активные назначения:");
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment ra = activeAssignments.get(i);
                System.out.println("  " + (i + 1) + ". " + ra.role().getName()
                        + " [" + ra.assignmentType() + "]");
            }

            System.out.print("Выберите номер для отзыва: ");
            String numStr = scanner.nextLine().trim();
            int num;
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                System.out.println("Неверный номер");
                return;
            }
            if (num < 1 || num > activeAssignments.size()) {
                System.out.println("Номер вне диапазона");
                return;
            }

            RoleAssignment selected = activeAssignments.get(num - 1);
            if (selected instanceof PermanentAssignment) {
                ((PermanentAssignment) selected).revoke();
                System.out.println("Назначение отозвано");
                system.getAuditLog().log("ROLE_REVOKE", system.getCurrentUser(),
                        username, "Отозвана роль: " + selected.role().getName());
            } else {
                system.getAssignmentManager().remove(selected);
                System.out.println("Назначение удалено");
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            List<RoleAssignment> all = system.getAssignmentManager().findAll();
            if (all.isEmpty()) {
                System.out.println("Назначений нет");
                return;
            }
            System.out.printf("%-15s %-15s %-12s %-10s %-20s%n",
                    "USERNAME", "ROLE", "TYPE", "STATUS", "ASSIGNED AT");
            System.out.println("-".repeat(72));
            for (RoleAssignment ra : all) {
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.printf("%-15s %-15s %-12s %-10s %-20s%n",
                        ra.user().username(), ra.role().getName(),
                        ra.assignmentType(), status, ra.metadata().assignedAt());
            }
            System.out.println("Всего: " + all.size());
        });

        parser.registerCommand("assignment-list-user", "Назначения пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get());
            if (assignments.isEmpty()) {
                System.out.println("Назначений нет");
                return;
            }

            for (RoleAssignment ra : assignments) {
                if (ra instanceof AbstractRoleAssignment) {
                    System.out.println(((AbstractRoleAssignment) ra).summary());
                    System.out.println();
                }
            }
        });

        parser.registerCommand("assignment-list-role", "Пользователи с ролью", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль не найдена");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(roleOpt.get());
            if (assignments.isEmpty()) {
                System.out.println("Никому не назначена");
                return;
            }

            System.out.println("Пользователи с ролью '" + roleName + "':");
            for (RoleAssignment ra : assignments) {
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.println("  " + ra.user().username()
                        + " [" + ra.assignmentType() + "] " + status);
            }
        });

        parser.registerCommand("assignment-active", "Активные назначения", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                System.out.println("Активных назначений нет");
                return;
            }
            System.out.printf("%-15s %-15s %-12s %-20s%n",
                    "USERNAME", "ROLE", "TYPE", "ASSIGNED AT");
            System.out.println("-".repeat(62));
            for (RoleAssignment ra : active) {
                System.out.printf("%-15s %-15s %-12s %-20s%n",
                        ra.user().username(), ra.role().getName(),
                        ra.assignmentType(), ra.metadata().assignedAt());
            }
            System.out.println("Всего активных: " + active.size());
        });

        parser.registerCommand("assignment-expired", "Истёкшие назначения", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
            if (expired.isEmpty()) {
                System.out.println("Истёкших назначений нет");
                return;
            }
            for (RoleAssignment ra : expired) {
                System.out.println(ra.user().username() + " -> "
                        + ra.role().getName() + " [EXPIRED]");
            }
            System.out.println("Всего истёкших: " + expired.size());
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get());
            TemporaryAssignment found = null;
            for (RoleAssignment ra : assignments) {
                if (ra instanceof TemporaryAssignment
                        && ra.role().getName().equals(roleName)) {
                    found = (TemporaryAssignment) ra;
                    break;
                }
            }

            if (found == null) {
                System.out.println("Временное назначение не найдено");
                return;
            }

            System.out.print("Новая дата истечения (yyyy-MM-dd HH:mm): ");
            String newDate = scanner.nextLine().trim();
            found.extend(newDate);
            System.out.println("Назначение продлено до " + newDate);
        });

        parser.registerCommand("assignment-search", "Поиск назначений", (scanner, system) -> {
            System.out.println("Выберите фильтр:");
            System.out.println("  1. По пользователю");
            System.out.println("  2. По роли");
            System.out.println("  3. По типу (постоянное/временное)");
            System.out.println("  4. По статусу (активное/неактивное)");
            System.out.println("  5. Назначенные после даты");
            System.out.println("  6. Истекающие до даты");
            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            AssignmentFilter filter;
            switch (choice) {
                case "1":
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    filter = AssignmentFilters.byUsername(username);
                    break;
                case "2":
                    System.out.print("Имя роли: ");
                    String roleName = scanner.nextLine().trim();
                    filter = AssignmentFilters.byRoleName(roleName);
                    break;
                case "3":
                    System.out.print("Тип (PERMANENT/TEMPORARY): ");
                    String type = scanner.nextLine().trim();
                    filter = AssignmentFilters.byType(type);
                    break;
                case "4":
                    System.out.print("Статус (1 — активные, 2 — неактивные): ");
                    String statusChoice = scanner.nextLine().trim();
                    filter = statusChoice.equals("1")
                            ? AssignmentFilters.activeOnly()
                            : AssignmentFilters.inactiveOnly();
                    break;
                case "5":
                    System.out.print("После даты (yyyy-MM-dd HH:mm): ");
                    String afterDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignedAfter(afterDate);
                    break;
                case "6":
                    System.out.print("До даты (yyyy-MM-dd HH:mm): ");
                    String beforeDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(beforeDate);
                    break;
                default:
                    System.out.println("Неверный выбор");
                    return;
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("Ничего не найдено");
                return;
            }

            System.out.printf("%-15s %-15s %-12s %-10s%n",
                    "USERNAME", "ROLE", "TYPE", "STATUS");
            System.out.println("-".repeat(52));
            for (RoleAssignment ra : results) {
                String status = ra.isActive() ? "ACTIVE" : "INACTIVE";
                System.out.printf("%-15s %-15s %-12s %-10s%n",
                        ra.user().username(), ra.role().getName(),
                        ra.assignmentType(), status);
            }
            System.out.println("Найдено: " + results.size());
        });
    }

//команды прав доступа
    private static void registerPermissionCommands(CommandParser parser) {

        parser.registerCommand("permissions-user", "Все права пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                return;
            }

            Set<Permission> permissions = system.getAssignmentManager()
                    .getUserPermissions(userOpt.get());

            if (permissions.isEmpty()) {
                System.out.println("У пользователя нет прав");
                return;
            }

            Map<String, List<Permission>> byResource = new HashMap<>();
            for (Permission p : permissions) {
                if (!byResource.containsKey(p.resource())) {
                    byResource.put(p.resource(), new ArrayList<>());
                }
                byResource.get(p.resource()).add(p);
            }

            System.out.println("Права пользователя '" + username + "':");
            for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                System.out.println("\n  Ресурс: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("    - " + p.name() + ": " + p.description());
                }
            }
        });

        parser.registerCommand("permissions-check", "Проверить право пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Введите название права (например READ): ");
            String permName = scanner.nextLine().trim();
            System.out.print("Введите ресурс (например users): ");
            String resource = scanner.nextLine().trim();

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден");
                return;
            }

            boolean hasPermission = system.getAssignmentManager()
                    .userHasPermission(userOpt.get(), permName, resource);

            if (hasPermission) {
                System.out.println("✓ Пользователь '" + username
                        + "' ИМЕЕТ право " + permName.toUpperCase()
                        + " на " + resource.toLowerCase());

                List<RoleAssignment> assignments = system.getAssignmentManager()
                        .findByUser(userOpt.get());
                for (RoleAssignment ra : assignments) {
                    if (ra.isActive() && ra.role().hasPermission(permName, resource)) {
                        System.out.println("  (из роли: " + ra.role().getName() + ")");
                    }
                }
            } else {
                System.out.println("✗ Пользователь '" + username
                        + "' НЕ ИМЕЕТ право " + permName.toUpperCase()
                        + " на " + resource.toLowerCase());
            }
        });
    }

//служебные команды
    private static void registerServiceCommands(CommandParser parser) {

        parser.registerCommand("help", "Справка по командам", (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> System.out.println(system.generateStatistics()));

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        });

        parser.registerCommand("audit-log", "Журнал аудита", (scanner, system) -> {
            System.out.println("1. Показать весь лог");
            System.out.println("2. Фильтр по исполнителю");
            System.out.println("3. Фильтр по действию");
            System.out.println("4. Сохранить лог в файл");
            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    system.getAuditLog().printLog();
                    break;
                case "2":
                    System.out.print("Введите имя исполнителя: ");
                    String performer = scanner.nextLine().trim();
                    List<AuditEntry> byPerformer = system.getAuditLog().getByPerformer(performer);
                    if (byPerformer.isEmpty()) {
                        System.out.println("Записей не найдено.");
                    } else {
                        for (AuditEntry e : byPerformer) {
                            System.out.println(e.format());
                        }
                    }
                    break;
                case "3":
                    System.out.print("Введите действие (USER_CREATE, ROLE_ASSIGN и т.д.): ");
                    String action = scanner.nextLine().trim();
                    List<AuditEntry> byAction = system.getAuditLog().getByAction(action);
                    if (byAction.isEmpty()) {
                        System.out.println("Записей не найдено.");
                    } else {
                        for (AuditEntry e : byAction) {
                            System.out.println(e.format());
                        }
                    }
                    break;
                case "4":
                    System.out.print("Имя файла: ");
                    String filename = scanner.nextLine().trim();
                    system.getAuditLog().saveToFile(filename);
                    break;
                default:
                    System.out.println("Неверный выбор.");
            }
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Вы уверены что хотите выйти? (да/нет): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("да") || confirm.equals("yes") || confirm.equals("y") || confirm.equals("д")) {
                System.out.println("Выход");
                System.exit(0);
            } else {
                System.out.println("Выход отменён.");
            }
        });
    }
}