public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
) {
    public String format() {
        return "[" + timestamp + "] " + action + " | Кто: " + performer
                + " | Цель: " + target + " | " + details;
    }
}