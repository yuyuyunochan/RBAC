import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserManager implements Repository<User> {

    private final ConcurrentMap<String, User> users;

    public UserManager() {
        this.users = new ConcurrentHashMap<>();
    }

    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        User existing = users.putIfAbsent(item.username(), item);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Пользователь с username '" + item.username() + "' уже существует");
        }
    }

    @Override
    public boolean remove(User item) {
        if (item == null) {
            return false;
        }
        return users.remove(item.username(), item);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        for (User user : users.values()) {
            if (user.email().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> findByFilter(UserFilter filter) {
        List<User> result = new ArrayList<>();
        if (filter == null) {
            return result;
        }

        for (User user : users.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        if (filter == null) {
            return new ArrayList<>();
        }

        return users.values()
                .parallelStream()
                .filter(filter::test)
                .toList();
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<User> findAllParallel(UserFilter filter, Comparator<User> sorter) {
        List<User> result = new ArrayList<>(findByFilterParallel(filter));
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String username) {
        if (username == null) {
            return false;
        }
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        if (username == null) {
            throw new IllegalArgumentException("Username не может быть null");
        }

        if (!users.containsKey(username)) {
            throw new IllegalArgumentException(
                    "Пользователь с username '" + username + "' не найден");
        }

        User updated = User.validate(username, newFullName, newEmail);
        users.put(username, updated);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserManager other)) return false;
        return users.equals(other.users);
    }

    @Override
    public int hashCode() {
        return users.hashCode();
    }
}