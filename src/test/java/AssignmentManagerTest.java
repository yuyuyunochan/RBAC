import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    private User ivan;
    private User vasilina;
    private Role admin;
    private Role viewer;
    private Permission readUsers;
    private Permission writeUsers;
    private Permission deleteUsers;
    private AssignmentMetadata meta;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        roleManager.setAssignmentManager(assignmentManager);

        ivan = User.validate("ivan", "Ivan", "ivan@example.com");
        vasilina = User.validate("vasilina", "Vasilina", "vasilina@example.com");
        userManager.add(ivan);
        userManager.add(vasilina);

        readUsers = new Permission("read", "users", "Просмотр");
        writeUsers = new Permission("write", "users", "Редактирование");
        deleteUsers = new Permission("delete", "users", "Удаление");

        admin = new Role("Admin", "Полный доступ");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);

        viewer = new Role("Viewer", "Только просмотр");
        viewer.addPermission(readUsers);

        roleManager.add(admin);
        roleManager.add(viewer);

        meta = AssignmentMetadata.now("admin", "Тестовое назначение");
    }

    @Test
    @DisplayName("add — добавляет постоянное назначение")
    void testAddPermanent() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    @DisplayName("add — добавляет временное назначение")
    void testAddTemporary() {
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(ta);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    @DisplayName("add — добавляет несколько назначений")
    void testAddMultiple() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);
        assertEquals(2, assignmentManager.count());
    }

    @Test
    @DisplayName("add null — выбрасывает исключение")
    void testAddNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(null);
        });
    }

    @Test
    @DisplayName("add — дубликат активного назначения выбрасывает исключение")
    void testAddDuplicateActive() {
        PermanentAssignment pa1 = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa1);

        PermanentAssignment pa2 = new PermanentAssignment(ivan, admin, meta);
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(pa2);
        });
    }

    @Test
    @DisplayName("add — назначение после revoke предыдущего допускается")
    void testAddAfterRevoke() {
        PermanentAssignment pa1 = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa1);
        pa1.revoke();

        PermanentAssignment pa2 = new PermanentAssignment(ivan, admin, meta);
        assertDoesNotThrow(() -> {
            assignmentManager.add(pa2);
        });
    }

    @Test
    @DisplayName("add — несуществующий пользователь выбрасывает исключение")
    void testAddNonExistentUser() {
        User unknown = User.validate("unknown", "Unknown", "u@mail.com");
        PermanentAssignment pa = new PermanentAssignment(unknown, admin, meta);
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(pa);
        });
    }

    @Test
    @DisplayName("add — несуществующая роль выбрасывает исключение")
    void testAddNonExistentRole() {
        Role unknownRole = new Role("Unknown", "Unknown role");
        PermanentAssignment pa = new PermanentAssignment(ivan, unknownRole, meta);
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(pa);
        });
    }

    @Test
    @DisplayName("remove — удаляет назначение")
    void testRemove() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);
        boolean removed = assignmentManager.remove(pa);
        assertTrue(removed);
        assertEquals(0, assignmentManager.count());
    }

    @Test
    @DisplayName("remove — несуществующее возвращает false")
    void testRemoveNonExistent() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        boolean removed = assignmentManager.remove(pa);
        assertFalse(removed);
    }

    @Test
    @DisplayName("remove null — возвращает false")
    void testRemoveNull() {
        assertFalse(assignmentManager.remove(null));
    }

    @Test
    @DisplayName("findById — находит назначение")
    void testFindById() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        Optional<RoleAssignment> found = assignmentManager.findById(pa.assignmentId());
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findById — не находит несуществующее")
    void testFindByIdNotFound() {
        Optional<RoleAssignment> found = assignmentManager.findById("nonexistent");
        assertFalse(found.isPresent());
    }
    @Test
    @DisplayName("findAll — возвращает все назначения")
    void testFindAll() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        List<RoleAssignment> all = assignmentManager.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findByUser — находит назначения пользователя")
    void testFindByUser() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                ivan, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        List<RoleAssignment> result = assignmentManager.findByUser(ivan);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByUser — пользователь без назначений")
    void testFindByUserEmpty() {
        List<RoleAssignment> result = assignmentManager.findByUser(vasilina);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByRole — находит назначения роли")
    void testFindByRole() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        List<RoleAssignment> result = assignmentManager.findByRole(admin);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByRole — роль без назначений")
    void testFindByRoleEmpty() {
        List<RoleAssignment> result = assignmentManager.findByRole(viewer);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByFilter — только активные")
    void testFindByFilterActive() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        TemporaryAssignment expired = new TemporaryAssignment(
                vasilina, viewer, meta, "2020-01-01 00:00", false);
        assignmentManager.add(expired);

        List<RoleAssignment> active = assignmentManager.findByFilter(
                AssignmentFilters.activeOnly());
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("findByFilter — по типу PERMANENT")
    void testFindByFilterByType() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        List<RoleAssignment> result = assignmentManager.findByFilter(
                AssignmentFilters.byType("PERMANENT"));
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByFilter — по username")
    void testFindByFilterByUsername() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        List<RoleAssignment> result = assignmentManager.findByFilter(
                AssignmentFilters.byUsername("ivan"));
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByFilter — комбинация AND")
    void testFindByFilterAnd() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        AssignmentFilter filter = AssignmentFilters.activeOnly()
                .and(AssignmentFilters.byType("TEMPORARY"));
        List<RoleAssignment> result = assignmentManager.findByFilter(filter);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findAll с сортировкой по username")
    void testFindAllSorted() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa);
        assignmentManager.add(ta);

        List<RoleAssignment> sorted = assignmentManager.findAll(
                assignment -> true,
                AssignmentSorters.byUsername());

        assertEquals("ivan", sorted.get(0).user().username());
        assertEquals("vasilina", sorted.get(1).user().username());
    }

    @Test
    @DisplayName("getActiveAssignments — возвращает только активные")
    void testGetActiveAssignments() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        TemporaryAssignment expired = new TemporaryAssignment(
                vasilina, viewer, meta, "2020-01-01 00:00", false);
        assignmentManager.add(expired);

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(1, active.size());
    }

    @Test
    @DisplayName("getExpiredAssignments — возвращает просроченные")
    void testGetExpiredAssignments() {
        TemporaryAssignment expired = new TemporaryAssignment(
                vasilina, viewer, meta, "2020-01-01 00:00", false);
        assignmentManager.add(expired);

        TemporaryAssignment active = new TemporaryAssignment(
                ivan, admin, meta, "2030-12-31 23:59", false);
        assignmentManager.add(active);

        List<RoleAssignment> expiredList = assignmentManager.getExpiredAssignments();
        assertEquals(1, expiredList.size());
    }

    @Test
    @DisplayName("getExpiredAssignments — постоянные не попадают")
    void testGetExpiredAssignmentsNoPermanent() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
        assertTrue(expired.isEmpty());
    }

    @Test
    @DisplayName("userHasRole — пользователь имеет активную роль")
    void testUserHasRole() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        assertTrue(assignmentManager.userHasRole(ivan, admin));
        assertFalse(assignmentManager.userHasRole(vasilina, admin));
    }

    @Test
    @DisplayName("userHasRole — после revoke возвращает false")
    void testUserHasRoleAfterRevoke() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);
        pa.revoke();

        assertFalse(assignmentManager.userHasRole(ivan, admin));
    }

    @Test
    @DisplayName("userHasRole — истёкшее временное возвращает false")
    void testUserHasRoleExpired() {
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2020-01-01 00:00", false);
        assignmentManager.add(ta);

        assertFalse(assignmentManager.userHasRole(vasilina, viewer));
    }

    @Test
    @DisplayName("userHasPermission — пользователь имеет право через роль")
    void testUserHasPermission() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        assertTrue(assignmentManager.userHasPermission(ivan, "READ", "users"));
        assertTrue(assignmentManager.userHasPermission(ivan, "WRITE", "users"));
        assertTrue(assignmentManager.userHasPermission(ivan, "DELETE", "users"));
    }

    @Test
    @DisplayName("userHasPermission — пользователь не имеет право")
    void testUserHasPermissionFalse() {
        PermanentAssignment pa = new PermanentAssignment(vasilina, viewer, meta);
        assignmentManager.add(pa);

        assertTrue(assignmentManager.userHasPermission(vasilina, "READ", "users"));
        assertFalse(assignmentManager.userHasPermission(vasilina, "WRITE", "users"));
        assertFalse(assignmentManager.userHasPermission(vasilina, "DELETE", "users"));
    }

    @Test
    @DisplayName("userHasPermission — неактивное назначение не даёт прав")
    void testUserHasPermissionInactive() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);
        pa.revoke();

        assertFalse(assignmentManager.userHasPermission(ivan, "READ", "users"));
    }

    @Test
    @DisplayName("getUserPermissions — все права из активных ролей")
    void testGetUserPermissions() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        Set<Permission> perms = assignmentManager.getUserPermissions(ivan);
        assertEquals(3, perms.size());
        assertTrue(perms.contains(readUsers));
        assertTrue(perms.contains(writeUsers));
        assertTrue(perms.contains(deleteUsers));
    }

    @Test
    @DisplayName("getUserPermissions — объединяет права из нескольких ролей")
    void testGetUserPermissionsMultipleRoles() {
        PermanentAssignment pa1 = new PermanentAssignment(ivan, admin, meta);
        TemporaryAssignment ta = new TemporaryAssignment(
                ivan, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(pa1);
        assignmentManager.add(ta);

        Set<Permission> perms = assignmentManager.getUserPermissions(ivan);
        // admin: read, write, delete + viewer: read = 3 уникальных (read не дублируется)
        assertEquals(3, perms.size());
    }

    @Test
    @DisplayName("getUserPermissions — пустой набор для пользователя без ролей")
    void testGetUserPermissionsEmpty() {
        Set<Permission> perms = assignmentManager.getUserPermissions(vasilina);
        assertTrue(perms.isEmpty());
    }

    @Test
    @DisplayName("revokeAssignment — отменяет постоянное назначение")
    void testRevokeAssignment() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        assignmentManager.revokeAssignment(pa.assignmentId());
        assertFalse(pa.isActive());
        assertTrue(pa.isRevoked());
    }

    @Test
    @DisplayName("revokeAssignment — несуществующий id выбрасывает исключение")
    void testRevokeNonExistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.revokeAssignment("nonexistent_id");
        });
    }

    @Test
    @DisplayName("revokeAssignment — временное назначение выбрасывает исключение")
    void testRevokeTemporary() {
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2030-12-31 23:59", false);
        assignmentManager.add(ta);

        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.revokeAssignment(ta.assignmentId());
        });
    }

    @Test
    @DisplayName("extendTemporaryAssignment — продлевает дату")
    void testExtendTemporary() {
        TemporaryAssignment ta = new TemporaryAssignment(
                vasilina, viewer, meta, "2020-01-01 00:00", false);
        assignmentManager.add(ta);

        assertTrue(ta.isExpired());

        assignmentManager.extendTemporaryAssignment(
                ta.assignmentId(), "2030-12-31 23:59");
        assertFalse(ta.isExpired());
        assertEquals("2030-12-31 23:59", ta.getExpiresAt());
    }

    @Test
    @DisplayName("extendTemporaryAssignment — несуществующий id выбрасывает исключение")
    void testExtendNonExistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.extendTemporaryAssignment("nonexistent", "2030-12-31 23:59");
        });
    }

    @Test
    @DisplayName("extendTemporaryAssignment — постоянное назначение выбрасывает исключение")
    void testExtendPermanent() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);

        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.extendTemporaryAssignment(
                    pa.assignmentId(), "2030-12-31 23:59");
        });
    }

    @Test
    @DisplayName("count — пустой менеджер")
    void testCountEmpty() {
        assertEquals(0, assignmentManager.count());
    }

    @Test
    @DisplayName("clear — очищает все назначения")
    void testClear() {
        PermanentAssignment pa = new PermanentAssignment(ivan, admin, meta);
        assignmentManager.add(pa);
        assignmentManager.clear();
        assertEquals(0, assignmentManager.count());
    }
}