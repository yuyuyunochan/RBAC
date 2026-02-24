import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssignmentManager implements Repository<RoleAssignment> {

    private Map<String, RoleAssignment> assignments;

    private UserManager userManager;
    private RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignments = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }

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
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) {
            return false;
        }
        return assignments.remove(item.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment assignment : assignments.values()) {
            if (assignment.user().equals(user)) {
                result.add(assignment);
            }
        }
        return result;
    }

    public List<RoleAssignment> findByRole(Role role) {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment assignment : assignments.values()) {
            if (assignment.role().equals(role)) {
                result.add(assignment);
            }
        }
        return result;
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment assignment : assignments.values()) {
            if (filter.test(assignment)) {
                result.add(assignment);
            }
        }
        return result;
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter,
                                        Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment assignment : assignments.values()) {
            if (assignment instanceof TemporaryAssignment) {
                TemporaryAssignment temp = (TemporaryAssignment) assignment;
                if (temp.isExpired()) {
                    result.add(assignment);
                }
            }
        }
        return result;
    }

    public boolean userHasRole(User user, Role role) {
        for (RoleAssignment assignment : assignments.values()) {
            if (assignment.user().equals(user)
                    && assignment.role().equals(role)
                    && assignment.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> allPermissions = getUserPermissions(user);
        for (Permission p : allPermissions) {
            if (p.name().equals(permissionName.toUpperCase())
                    && p.resource().equals(resource.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> allPermissions = new HashSet<>();
        for (RoleAssignment assignment : assignments.values()) {
            if (assignment.user().equals(user) && assignment.isActive()) {
                allPermissions.addAll(assignment.role().getPermissions());
            }
        }
        return allPermissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException(
                    "Назначение с id '" + assignmentId + "' не найдено");
        }

        if (assignment instanceof PermanentAssignment) {
            PermanentAssignment permanent = (PermanentAssignment) assignment;
            permanent.revoke();
        } else {
            throw new IllegalArgumentException(
                    "Только постоянные назначения можно отменить (revoke)");
        }
    }

    public void extendTemporaryAssignment(String assignmentId,
                                          String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException(
                    "Назначение с id '" + assignmentId + "' не найдено");
        }

        if (assignment instanceof TemporaryAssignment) {
            TemporaryAssignment temp = (TemporaryAssignment) assignment;
            temp.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException(
                    "Продлить можно только временные назначения");
        }
    }
}