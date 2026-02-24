import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;
    private User ivan;
    private User vasilina;
    private User denis;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        ivan = User.validate("ivan", "ivan Doe", "ivan@company.com");
        vasilina = User.validate("vasilina", "vasilina Smith", "vasilina@company.com");
        denis = User.validate("denis_admin", "denis Brown", "denis@gmail.com");
    }

    @Test
    @DisplayName("add — добавляет пользователя")
    void testAdd() {
        userManager.add(ivan);
        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("ivan"));
    }

    @Test
    @DisplayName("add — добавляет нескольких пользователей")
    void testAddMultiple() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);
        assertEquals(3, userManager.count());
    }

    @Test
    @DisplayName("add null — выбрасывает исключение")
    void testAddNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.add(null);
        });
    }

    @Test
    @DisplayName("add дубликат username — выбрасывает исключение")
    void testAddDuplicate() {
        userManager.add(ivan);
        User duplicate = User.validate("ivan", "Other Name", "other@mail.com");
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.add(duplicate);
        });
    }

    @Test
    @DisplayName("remove — удаляет существующего пользователя")
    void testRemove() {
        userManager.add(ivan);
        boolean removed = userManager.remove(ivan);
        assertTrue(removed);
        assertEquals(0, userManager.count());
        assertFalse(userManager.exists("ivan"));
    }

    @Test
    @DisplayName("remove — несуществующий пользователь возвращает false")
    void testRemoveNonExisting() {
        boolean removed = userManager.remove(ivan);
        assertFalse(removed);
    }

    @Test
    @DisplayName("remove null — возвращает false")
    void testRemoveNull() {
        boolean removed = userManager.remove(null);
        assertFalse(removed);
    }

    @Test
    @DisplayName("findById — находит пользователя по username")
    void testFindById() {
        userManager.add(ivan);
        Optional<User> found = userManager.findById("ivan");
        assertTrue(found.isPresent());
        assertEquals(ivan, found.get());
    }

    @Test
    @DisplayName("findById — несуществующий возвращает пустой Optional")
    void testFindByIdNotFound() {
        Optional<User> found = userManager.findById("nobody");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByUsername — находит пользователя")
    void testFindByUsername() {
        userManager.add(ivan);
        Optional<User> found = userManager.findByUsername("ivan");
        assertTrue(found.isPresent());
        assertEquals("ivan Doe", found.get().fullName());
    }

    @Test
    @DisplayName("findByUsername — не находит несуществующего")
    void testFindByUsernameNotFound() {
        Optional<User> found = userManager.findByUsername("unknown");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findByEmail — находит пользователя")
    void testFindByEmail() {
        userManager.add(ivan);
        Optional<User> found = userManager.findByEmail("ivan@company.com");
        assertTrue(found.isPresent());
        assertEquals("ivan", found.get().username());
    }

    @Test
    @DisplayName("findByEmail — не находит несуществующего")
    void testFindByEmailNotFound() {
        Optional<User> found = userManager.findByEmail("unknown@mail.com");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll — возвращает всех пользователей")
    void testFindAll() {
        userManager.add(ivan);
        userManager.add(vasilina);
        List<User> all = userManager.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("findAll — пустой менеджер возвращает пустой список")
    void testFindAllEmpty() {
        List<User> all = userManager.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("findByFilter — по домену email")
    void testFindByFilterEmailDomain() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        List<User> companyUsers = userManager.findByFilter(
                UserFilters.byEmailDomain("@company.com"));
        assertEquals(2, companyUsers.size());
    }

    @Test
    @DisplayName("findByFilter — по подстроке в username")
    void testFindByFilterUsernameContains() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        List<User> result = userManager.findByFilter(
                UserFilters.byUsernameContains("admin"));
        assertEquals(1, result.size());
        assertEquals("denis_admin", result.get(0).username());
    }

    @Test
    @DisplayName("findByFilter — комбинация AND")
    void testFindByFilterAnd() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        UserFilter filter = UserFilters.byEmailDomain("@company.com")
                .and(UserFilters.byFullNameContains("ivan"));
        List<User> result = userManager.findByFilter(filter);
        assertEquals(1, result.size());
        assertEquals("ivan", result.get(0).username());
    }

    @Test
    @DisplayName("findByFilter — комбинация OR")
    void testFindByFilterOr() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        UserFilter filter = UserFilters.byUsername("ivan")
                .or(UserFilters.byUsername("denis_admin"));
        List<User> result = userManager.findByFilter(filter);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findByFilter — ничего не найдено")
    void testFindByFilterEmpty() {
        userManager.add(ivan);

        List<User> result = userManager.findByFilter(
                UserFilters.byEmailDomain("@nonexistent.com"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll с сортировкой по username")
    void testFindAllSortedByUsername() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        List<User> sorted = userManager.findAll(
                user -> true,
                UserSorters.byUsername());

        assertEquals("denis_admin", sorted.get(0).username());
        assertEquals("ivan", sorted.get(1).username());
        assertEquals("vasilina", sorted.get(2).username());
    }

    @Test
    @DisplayName("findAll с фильтром и сортировкой")
    void testFindAllFilteredAndSorted() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.add(denis);

        List<User> result = userManager.findAll(
                UserFilters.byEmailDomain("@company.com"),
                UserSorters.byFullName());

        assertEquals(2, result.size());
        assertEquals("ivan", result.get(0).username());
        assertEquals("vasilina", result.get(1).username());
    }

    @Test
    @DisplayName("count — пустой менеджер")
    void testCountEmpty() {
        assertEquals(0, userManager.count());
    }

    @Test
    @DisplayName("count — после добавления")
    void testCountAfterAdd() {
        userManager.add(ivan);
        userManager.add(vasilina);
        assertEquals(2, userManager.count());
    }

    @Test
    @DisplayName("exists — существующий пользователь")
    void testExistsTrue() {
        userManager.add(ivan);
        assertTrue(userManager.exists("ivan"));
    }

    @Test
    @DisplayName("exists — несуществующий пользователь")
    void testExistsFalse() {
        assertFalse(userManager.exists("nobody"));
    }

    @Test
    @DisplayName("update — обновляет данные пользователя")
    void testUpdate() {
        userManager.add(ivan);
        userManager.update("ivan", "ivan Updated", "ivan_new@company.com");

        User updated = userManager.findByUsername("ivan").get();
        assertEquals("ivan Updated", updated.fullName());
        assertEquals("ivan_new@company.com", updated.email());
    }

    @Test
    @DisplayName("update — несуществующий пользователь выбрасывает исключение")
    void testUpdateNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.update("nobody", "Name", "email@mail.com");
        });
    }

    @Test
    @DisplayName("update — невалидные данные выбрасывают исключение")
    void testUpdateInvalidData() {
        userManager.add(ivan);
        assertThrows(IllegalArgumentException.class, () -> {
            userManager.update("ivan", "", "bad_email");
        });
    }

    @Test
    @DisplayName("clear — очищает всех пользователей")
    void testClear() {
        userManager.add(ivan);
        userManager.add(vasilina);
        userManager.clear();
        assertEquals(0, userManager.count());
        assertFalse(userManager.exists("ivan"));
    }
}