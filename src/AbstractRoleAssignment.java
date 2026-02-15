import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private String assignmentId;
    private User user;
    private Role role;
    private AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assign_" + UUID.randomUUID().toString().substring(0, 8);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AbstractRoleAssignment other = (AbstractRoleAssignment) obj;
        return assignmentId.equals(other.assignmentId);
    }

    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }

    public String summary() {
        String status;
        if (isActive()) {
            status = "ACTIVE";
        } else {
            status = "INACTIVE";
        }

        String result = "[" + assignmentType() + "] " + role.getName()
                + " assigned to " + user.username()
                + " by " + metadata.assignedBy()
                + " at " + metadata.assignedAt() + "\n";

        if (metadata.reason() != null && !metadata.reason().isEmpty()) {
            result = result + "  Reason: " + metadata.reason() + "\n";
        }

        result = result + "  Status: " + status;

        return result;
    }
}