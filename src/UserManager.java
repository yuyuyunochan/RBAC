import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserManager implements Repository<User> {

    private Map<String, User> users;

    public UserManager() {
        this.users = new HashMap<>();
    }
    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (users.containsKey(item.username())) {
            throw new IllegalArgumentException(
                    "Пользователь с username '" + item.username() + "' уже существует");
        }
        users.put(item.username(), item);
    }

    @Override
    public boolean remove(User item) {
        if (item == null) {
            return false;
        }
        return users.remove(item.username()) != null;
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
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        for (User user : users.values()) {
            if (user.email().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public List<User> findByFilter(UserFilter filter) {
        List<User> result = new ArrayList<>();
        for (User user : users.values()) {
            if (filter.test(user)) {
                result.add(user);
            }
        }
        return result;
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
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
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UserManager other = (UserManager) obj;
        return users.equals(other.users);
    }

    @Override
    public int hashCode() {
        return users.hashCode();
    }
}