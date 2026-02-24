import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoleManager implements Repository<Role> {

    private Map<String, Role> rolesById;
    private Map<String, Role> rolesByName;

    private AssignmentManager assignmentManager;

    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
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
        if (rolesByName.containsKey(item.getName())) {
            throw new IllegalArgumentException(
                    "Роль с именем '" + item.getName() + "' уже существует");
        }
        rolesById.put(item.getId(), item);
        rolesByName.put(item.getName(), item);
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) {
            return false;
        }

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

        rolesById.remove(item.getId());
        rolesByName.remove(item.getName());
        return true;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (filter.test(role)) {
                result.add(role);
            }
        }
        return result;
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException(
                    "Роль с именем '" + roleName + "' не найдена");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException(
                    "Роль с именем '" + roleName + "' не найдена");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> result = new ArrayList<>();
        for (Role role : rolesById.values()) {
            if (role.hasPermission(permissionName, resource)) {
                result.add(role);
            }
        }
        return result;
    }
}