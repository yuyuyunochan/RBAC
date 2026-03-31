import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoleManager implements Repository<Role> {

    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    private final ReadWriteLock lock;
    private AssignmentManager assignmentManager;

    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.assignmentManager = null;
    }

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }

        lock.writeLock().lock();
        try {
            if (rolesByName.containsKey(item.getName())) {
                throw new IllegalArgumentException(
                        "Роль с именем '" + item.getName() + "' уже существует");
            }
            rolesById.put(item.getId(), item);
            rolesByName.put(item.getName(), item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) {
            return false;
        }

        lock.writeLock().lock();
        try {
            if (assignmentManager != null) {
                List<RoleAssignment> assignments = assignmentManager.findByRole(item);
                for (RoleAssignment assignment : assignments) {
                    if (assignment.isActive()) {
                        throw new IllegalStateException(
                                "Нельзя удалить роль '" + item.getName()
                                        + "' — она назначена пользователям");
                    }
                }
            }

            Role removedById = rolesById.remove(item.getId());
            Role removedByName = rolesByName.remove(item.getName());
            return removedById != null || removedByName != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Role> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return rolesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            rolesById.clear();
            rolesByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Role> findByName(String name) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesByName.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilter(RoleFilter filter) {
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>();
            for (Role role : rolesById.values()) {
                if (filter.test(role)) {
                    result.add(role);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        lock.readLock().lock();
        try {
            return rolesById.values()
                    .parallelStream()
                    .filter(filter::test)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<Role> findAllParallel(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = new ArrayList<>(findByFilterParallel(filter));
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public boolean exists(String name) {
        lock.readLock().lock();
        try {
            return rolesByName.containsKey(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException(
                        "Роль с именем '" + roleName + "' не найдена");
            }
            role.addPermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException(
                        "Роль с именем '" + roleName + "' не найдена");
            }
            role.removePermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>();
            for (Role role : rolesById.values()) {
                if (role.hasPermission(permissionName, resource)) {
                    result.add(role);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }
}