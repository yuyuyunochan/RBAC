import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        CommandRegistry.registerAll(parser);
    }


    @Test
    @DisplayName("Все пользовательские команды зарегистрированы")
    void testUserCommandsRegistered() {
        assertTrue(parser.hasCommand("user-list"));
        assertTrue(parser.hasCommand("user-create"));
        assertTrue(parser.hasCommand("user-view"));
        assertTrue(parser.hasCommand("user-update"));
        assertTrue(parser.hasCommand("user-delete"));
        assertTrue(parser.hasCommand("user-search"));
    }

    @Test
    @DisplayName("Все команды ролей зарегистрированы")
    void testRoleCommandsRegistered() {
        assertTrue(parser.hasCommand("role-list"));
        assertTrue(parser.hasCommand("role-create"));
        assertTrue(parser.hasCommand("role-view"));
        assertTrue(parser.hasCommand("role-update"));
        assertTrue(parser.hasCommand("role-delete"));
        assertTrue(parser.hasCommand("role-add-permission"));
        assertTrue(parser.hasCommand("role-remove-permission"));
        assertTrue(parser.hasCommand("role-search"));
    }

    @Test
    @DisplayName("Все команды назначений зарегистрированы")
    void testAssignmentCommandsRegistered() {
        assertTrue(parser.hasCommand("assign-role"));
        assertTrue(parser.hasCommand("revoke-role"));
        assertTrue(parser.hasCommand("assignment-list"));
        assertTrue(parser.hasCommand("assignment-list-user"));
        assertTrue(parser.hasCommand("assignment-list-role"));
        assertTrue(parser.hasCommand("assignment-active"));
        assertTrue(parser.hasCommand("assignment-expired"));
        assertTrue(parser.hasCommand("assignment-extend"));
        assertTrue(parser.hasCommand("assignment-search"));
    }

    @Test
    @DisplayName("Все команды прав зарегистрированы")
    void testPermissionCommandsRegistered() {
        assertTrue(parser.hasCommand("permissions-user"));
        assertTrue(parser.hasCommand("permissions-check"));
    }

    @Test
    @DisplayName("Все служебные команды зарегистрированы")
    void testServiceCommandsRegistered() {
        assertTrue(parser.hasCommand("help"));
        assertTrue(parser.hasCommand("stats"));
        assertTrue(parser.hasCommand("clear"));
        assertTrue(parser.hasCommand("exit"));
    }

    @Test
    @DisplayName("user-list — выполняется без ошибок")
    void testUserListExecutes() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("user-list", scanner, system);
        });
    }

    @Test
    @DisplayName("user-create — создаёт пользователя")
    void testUserCreate() {
        String input = "new_user\nNew User\nnew@example.com\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("user-create", scanner, system);
        assertTrue(system.getUserManager().exists("new_user"));
    }

    @Test
    @DisplayName("user-view — показывает информацию")
    void testUserView() {
        String input = "admin\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("user-view", scanner, system);
        });
    }

    @Test
    @DisplayName("user-view — несуществующий пользователь")
    void testUserViewNotFound() {
        String input = "nonexistent\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("user-view", scanner, system);
        });
    }

    @Test
    @DisplayName("user-update — обновляет пользователя")
    void testUserUpdate() {
        String input = "admin\nUpdated Admin\nadmin_new@system.com\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("user-update", scanner, system);

        var user = system.getUserManager().findByUsername("admin").get();
        assertEquals("Updated Admin", user.fullName());
        assertEquals("admin_new@system.com", user.email());
    }

    @Test
    @DisplayName("user-delete — удаляет пользователя с подтверждением")
    void testUserDelete() {
        User testUser = User.validate("to_delete", "Delete Me", "del@test.com");
        system.getUserManager().add(testUser);

        String input = "to_delete\nда\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("user-delete", scanner, system);
        assertFalse(system.getUserManager().exists("to_delete"));
    }

    @Test
    @DisplayName("user-delete — отмена удаления")
    void testUserDeleteCancelled() {
        String input = "admin\nнет\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("user-delete", scanner, system);
        assertTrue(system.getUserManager().exists("admin"));
    }

    @Test
    @DisplayName("user-search — поиск по username")
    void testUserSearch() {
        String input = "1\nadmin\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("user-search", scanner, system);
        });
    }

    @Test
    @DisplayName("role-list — выполняется без ошибок")
    void testRoleList() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("role-list", scanner, system);
        });
    }

    @Test
    @DisplayName("role-create — создаёт роль без прав")
    void testRoleCreate() {
        String input = "TestRole\nTest Description\nнет\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("role-create", scanner, system);
        assertTrue(system.getRoleManager().exists("TestRole"));
    }

    @Test
    @DisplayName("role-create — создаёт роль с правами")
    void testRoleCreateWithPermission() {
        String input = "TestRole\nTest Description\nда\nread\ntests\nRead tests\nнет\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("role-create", scanner, system);
        assertTrue(system.getRoleManager().exists("TestRole"));

        var role = system.getRoleManager().findByName("TestRole").get();
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    @DisplayName("role-view — показывает роль")
    void testRoleView() {
        String input = "Admin\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("role-view", scanner, system);
        });
    }

    @Test
    @DisplayName("role-add-permission — добавляет право")
    void testRoleAddPermission() {
        String input = "Viewer\nexecute\ntasks\nЗапуск задач\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("role-add-permission", scanner, system);

        var role = system.getRoleManager().findByName("Viewer").get();
        assertTrue(role.hasPermission("EXECUTE", "tasks"));
    }

    @Test
    @DisplayName("role-search — по имени")
    void testRoleSearch() {
        String input = "1\nAdmin\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("role-search", scanner, system);
        });
    }

    @Test
    @DisplayName("assign-role — назначает постоянную роль")
    void testAssignRole() {
        User newUser = User.validate("new_user", "New User", "new@test.com");
        system.getUserManager().add(newUser);

        List<Role> roles = system.getRoleManager().findAll();

        int viewerIndex = -1;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getName().equals("Viewer")) {
                viewerIndex = i + 1;
                break;
            }
        }

        if (viewerIndex == -1) {
            viewerIndex = 1;
        }

        String input = "new_user\n" + viewerIndex + "\n1\nТестовое назначение\n";
        Scanner scanner = new Scanner(input);

        parser.executeCommand("assign-role", scanner, system);

        Role viewerRole = system.getRoleManager().findByName("Viewer").get();
        assertTrue(system.getAssignmentManager().userHasRole(newUser, viewerRole));
    }

    @Test
    @DisplayName("assignment-list — выполняется без ошибок")
    void testAssignmentList() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("assignment-list", scanner, system);
        });
    }

    @Test
    @DisplayName("assignment-active — выполняется без ошибок")
    void testAssignmentActive() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("assignment-active", scanner, system);
        });
    }

    @Test
    @DisplayName("assignment-expired — выполняется без ошибок")
    void testAssignmentExpired() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("assignment-expired", scanner, system);
        });
    }

    @Test
    @DisplayName("permissions-user — показывает права")
    void testPermissionsUser() {
        String input = "admin\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("permissions-user", scanner, system);
        });
    }

    @Test
    @DisplayName("permissions-check — проверяет право (есть)")
    void testPermissionsCheckExists() {
        String input = "admin\nREAD\nusers\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("permissions-check", scanner, system);
        });
    }

    @Test
    @DisplayName("permissions-check — проверяет право (нет)")
    void testPermissionsCheckNotExists() {
        String input = "admin\nSUPERPOWER\nmagic\n";
        Scanner scanner = new Scanner(input);

        assertDoesNotThrow(() -> {
            parser.executeCommand("permissions-check", scanner, system);
        });
    }

    @Test
    @DisplayName("stats — выполняется без ошибок")
    void testStats() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("stats", scanner, system);
        });
    }

    @Test
    @DisplayName("help — выполняется без ошибок")
    void testHelp() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("help", scanner, system);
        });
    }

    @Test
    @DisplayName("clear — выполняется без ошибок")
    void testClear() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("clear", scanner, system);
        });
    }
}