import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AssignmentManager implements Repository<RoleAssignment> {

    private final Map<String, RoleAssignment> assignments;
    private final ReadWriteLock lock;
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignments = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }

        lock.writeLock().lock();
        try {
            if (!userManager.exists(item.user().username())) {
                throw new IllegalArgumentException(
                        "Пользователь '" + item.user().username() + "' не найден");
            }

            if (!roleManager.exists(item.role().getName())) {
                throw new IllegalArgumentException(
                        "Роль '" + item.role().getName() + "' не найдена");
            }

            for (RoleAssignment existing : assignments.values()) {
                if (existing.user().equals(item.user())
                        && existing.role().equals(item.role())
                        && existing.isActive()) {
                    throw new IllegalArgumentException(
                            "Роль '" + item.role().getName()
                                    + "' уже активно назначена пользователю '"
                                    + item.user().username() + "'");
                }
            }

            assignments.put(item.assignmentId(), item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) {
            return false;
        }

        lock.writeLock().lock();
        try {
            return assignments.remove(item.assignmentId()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignments.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(assignments.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return assignments.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            assignments.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user)) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByRole(Role role) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.role().equals(role)) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignments.values()) {
                if (filter.test(assignment)) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        lock.readLock().lock();
        try {
            return assignments.values()
                    .parallelStream()
                    .filter(filter::test)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> findAllParallel(AssignmentFilter filter,
                                                Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = new ArrayList<>(findByFilterParallel(filter));
        if (sorter != null) {
            result.sort(sorter);
        }
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>();
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment instanceof TemporaryAssignment temp && temp.isExpired()) {
                    result.add(assignment);
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasRole(User user, Role role) {
        lock.readLock().lock();
        try {
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user)
                        && assignment.role().equals(role)
                        && assignment.isActive()) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> allPermissions = getUserPermissions(user);
        for (Permission permission : allPermissions) {
            if (permission.name().equals(permissionName.toUpperCase())
                    && permission.resource().equals(resource.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        lock.readLock().lock();
        try {
            Set<Permission> allPermissions = new HashSet<>();
            for (RoleAssignment assignment : assignments.values()) {
                if (assignment.user().equals(user) && assignment.isActive()) {
                    allPermissions.addAll(assignment.role().getPermissions());
                }
            }
            return allPermissions;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void revokeAssignment(String assignmentId) {
        lock.writeLock().lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException(
                        "Назначение с id '" + assignmentId + "' не найдено");
            }

            if (assignment instanceof PermanentAssignment permanent) {
                permanent.revoke();
            } else {
                throw new IllegalArgumentException(
                        "Только постоянные назначения можно отозвать");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        lock.writeLock().lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException(
                        "Назначение с id '" + assignmentId + "' не найдено");
            }

            if (assignment instanceof TemporaryAssignment temporary) {
                temporary.extend(newExpirationDate);
            } else {
                throw new IllegalArgumentException(
                        "Продлить можно только временное назначение");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}