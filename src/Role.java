import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.UUID;

public class Role {
    private String id;
    private String name;
    private String description;
    private Set<Permission> permissions;

    public Role(String name, String description) {
        this.id = "role_" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        for (Permission p : permissions) {
            if (p.name().equals(permissionName.toUpperCase())
                    && p.resource().equals(resource.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Role other = (Role) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "'}";
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("  Description: ").append(description).append("\n");
        sb.append("  Permissions (").append(permissions.size()).append("):\n");
        for (Permission p : permissions) {
            sb.append("    - ").append(p.format()).append("\n");
        }
        return sb.toString();
    }
}