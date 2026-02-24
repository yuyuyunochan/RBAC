import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleManagerTest {

    private RoleManager roleManager;
    private Role admin;
    private Role viewer;
    private Role editor;
    private Permission readUsers;
    private Permission writeUsers;
    private Permission deleteUsers;

    @Mock
    private AssignmentManager mockAssignmentManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
        roleManager.setAssignmentManager(mockAssignmentManager);

        readUsers = new Permission("read", "users", "Просмотр");
        writeUsers = new Permission("write", "users", "Редактирование");
        deleteUsers = new Permission("delete", "users", "Удаление");

        admin = new Role("Administrator", "Полный доступ");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);

        viewer = new Role("Viewer", "Только просмотр");
        viewer.addPermission(readUsers);

        editor = new Role("Editor", "Редактирование");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);
    }

    @Test
    @DisplayName("add — добавляет роль")
    void testAdd() {
        roleManager.add(admin);
        assertEquals(1, roleManager.count());
        assertTrue(roleManager.exists("Administrator"));
    }

    @Test
    @DisplayName("add — добавляет несколько ролей")
    void testAddMultiple() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);
        assertEquals(3, roleManager.count());
    }

    @Test
    @DisplayName("add null — выбрасывает исключение")
    void testAddNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            roleManager.add(null);
        });
    }

    @Test
    @DisplayName("add дубликат имени — выбрасывает исключение")
    void testAddDuplicateName() {
        roleManager.add(admin);
        Role anotherAdmin = new Role("Administrator", "Другое описание");
        assertThrows(IllegalArgumentException.class, () -> {
            roleManager.add(anotherAdmin);
        });
    }

    @Test
    @DisplayName("remove — удаляет роль без активных назначений")
    void testRemove() {
        roleManager.add(admin);

        when(mockAssignmentManager.findByRole(admin))
                .thenReturn(new ArrayList<>());

        boolean removed = roleManager.remove(admin);
        assertTrue(removed);
        assertEquals(0, roleManager.count());
        assertFalse(roleManager.exists("Administrator"));
    }

    @Test
    @DisplayName("remove — роль с активными назначениями выбрасывает исключение")
    void testRemoveWithActiveAssignments() {
        roleManager.add(admin);

        RoleAssignment mockAssignment = mock(RoleAssignment.class);
        when(mockAssignment.isActive()).thenReturn(true);

        List<RoleAssignment> activeList = new ArrayList<>();
        activeList.add(mockAssignment);
        when(mockAssignmentManager.findByRole(admin)).thenReturn(activeList);

        assertThrows(IllegalStateException.class, () -> {
            roleManager.remove(admin);
        });
        assertEquals(1, roleManager.count());
    }

    @Test
    @DisplayName("remove — роль с неактивными назначениями удаляется")
    void testRemoveWithInactiveAssignments() {
        roleManager.add(admin);

        RoleAssignment mockAssignment = mock(RoleAssignment.class);
        when(mockAssignment.isActive()).thenReturn(false);

        List<RoleAssignment> inactiveList = new ArrayList<>();
        inactiveList.add(mockAssignment);
        when(mockAssignmentManager.findByRole(admin)).thenReturn(inactiveList);

        boolean removed = roleManager.remove(admin);
        assertTrue(removed);
        assertEquals(0, roleManager.count());
    }

    @Test
    @DisplayName("remove null — возвращает false")
    void testRemoveNull() {
        boolean removed = roleManager.remove(null);
        assertFalse(removed);
    }

    @Test
    @DisplayName("findById — находит роль")
    void testFindById() {
        roleManager.add(admin);
        Optional<Role> found = roleManager.findById(admin.getId());
        assertTrue(found.isPresent());
        assertEquals(admin, found.get());
    }

    @Test
    @DisplayName("findById — не находит несуществующую")
    void testFindByIdNotFound() {
        Optional<Role> found = roleManager.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByName — находит роль")
    void testFindByName() {
        roleManager.add(admin);
        Optional<Role> found = roleManager.findByName("Administrator");
        assertTrue(found.isPresent());
        assertEquals("Administrator", found.get().getName());
    }

    @Test
    @DisplayName("findByName — не находит несуществующую")
    void testFindByNameNotFound() {
        Optional<Role> found = roleManager.findByName("NonExistent");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll — возвращает все роли")
    void testFindAll() {
        roleManager.add(admin);
        roleManager.add(viewer);
        List<Role> all = roleManager.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll — пустой менеджер")
    void testFindAllEmpty() {
        List<Role> all = roleManager.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("findByFilter — по имени")
    void testFindByFilterByName() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        List<Role> result = roleManager.findByFilter(
                RoleFilters.byName("Viewer"));
        assertEquals(1, result.size());
        assertEquals("Viewer", result.get(0).getName());
    }

    @Test
    @DisplayName("findByFilter — по подстроке в имени")
    void testFindByFilterByNameContains() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        List<Role> result = roleManager.findByFilter(
                RoleFilters.byNameContains("edit"));
        assertEquals(1, result.size());
        assertEquals("Editor", result.get(0).getName());
    }

    @Test
    @DisplayName("findByFilter — по минимальному количеству прав")
    void testFindByFilterMinPermissions() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        List<Role> result = roleManager.findByFilter(
                RoleFilters.hasAtLeastNPermissions(2));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByFilter — по наличию права")
    void testFindByFilterHasPermission() {
        roleManager.add(admin);
        roleManager.add(viewer);

        List<Role> result = roleManager.findByFilter(
                RoleFilters.hasPermission("DELETE", "users"));
        assertEquals(1, result.size());
        assertEquals("Administrator", result.get(0).getName());
    }

    @Test
    @DisplayName("findByFilter — комбинация AND")
    void testFindByFilterAnd() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        RoleFilter filter = RoleFilters.hasPermission(readUsers)
                .and(RoleFilters.hasAtLeastNPermissions(2));
        List<Role> result = roleManager.findByFilter(filter);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByFilter — ничего не найдено")
    void testFindByFilterEmpty() {
        roleManager.add(viewer);

        List<Role> result = roleManager.findByFilter(
                RoleFilters.hasAtLeastNPermissions(5));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll с сортировкой по имени")
    void testFindAllSortedByName() {
        roleManager.add(viewer);
        roleManager.add(admin);
        roleManager.add(editor);

        List<Role> sorted = roleManager.findAll(
                role -> true,
                RoleSorters.byName());

        assertEquals("Administrator", sorted.get(0).getName());
        assertEquals("Editor", sorted.get(1).getName());
        assertEquals("Viewer", sorted.get(2).getName());
    }

    @Test
    @DisplayName("findAll с сортировкой по количеству прав")
    void testFindAllSortedByPermissionCount() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        List<Role> sorted = roleManager.findAll(
                role -> true,
                RoleSorters.byPermissionCount());

        assertEquals("Viewer", sorted.get(0).getName());
        assertEquals("Editor", sorted.get(1).getName());
        assertEquals("Administrator", sorted.get(2).getName());
    }

    @Test
    @DisplayName("count — пустой менеджер")
    void testCountEmpty() {
        assertEquals(0, roleManager.count());
    }

    @Test
    @DisplayName("exists — существующая роль")
    void testExistsTrue() {
        roleManager.add(admin);
        assertTrue(roleManager.exists("Administrator"));
    }

    @Test
    @DisplayName("exists — несуществующая роль")
    void testExistsFalse() {
        assertFalse(roleManager.exists("NonExistent"));
    }

    @Test
    @DisplayName("addPermissionToRole — добавляет право")
    void testAddPermissionToRole() {
        roleManager.add(viewer);
        roleManager.addPermissionToRole("Viewer", writeUsers);
        assertTrue(viewer.hasPermission(writeUsers));
    }

    @Test
    @DisplayName("addPermissionToRole — несуществующая роль выбрасывает исключение")
    void testAddPermissionToNonExistentRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            roleManager.addPermissionToRole("NonExistent", readUsers);
        });
    }

    @Test
    @DisplayName("removePermissionFromRole — удаляет право")
    void testRemovePermissionFromRole() {
        roleManager.add(admin);
        roleManager.removePermissionFromRole("Administrator", deleteUsers);
        assertFalse(admin.hasPermission(deleteUsers));
    }

    @Test
    @DisplayName("removePermissionFromRole — несуществующая роль выбрасывает исключение")
    void testRemovePermissionFromNonExistentRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            roleManager.removePermissionFromRole("NonExistent", readUsers);
        });
    }

    @Test
    @DisplayName("findRolesWithPermission — находит роли с правом READ/users")
    void testFindRolesWithPermission() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.add(editor);

        List<Role> roles = roleManager.findRolesWithPermission("READ", "users");
        assertEquals(3, roles.size()); // все три имеют READ/users
    }

    @Test
    @DisplayName("findRolesWithPermission — находит роли с правом DELETE/users")
    void testFindRolesWithPermissionDelete() {
        roleManager.add(admin);
        roleManager.add(viewer);

        List<Role> roles = roleManager.findRolesWithPermission("DELETE", "users");
        assertEquals(1, roles.size());
        assertEquals("Administrator", roles.get(0).getName());
    }

    @Test
    @DisplayName("clear — очищает все роли")
    void testClear() {
        roleManager.add(admin);
        roleManager.add(viewer);
        roleManager.clear();
        assertEquals(0, roleManager.count());
        assertFalse(roleManager.exists("Administrator"));
        assertFalse(roleManager.exists("Viewer"));
    }
}