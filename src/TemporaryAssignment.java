import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isExpired() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return now.compareTo(expiresAt) > 0;
    }

    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "Срок истёк";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime expireDate = LocalDateTime.parse(expiresAt, formatter);
        LocalDateTime nowDate = LocalDateTime.now();

        long days = ChronoUnit.DAYS.between(nowDate, expireDate);
        long hours = ChronoUnit.HOURS.between(nowDate, expireDate) % 24;

        return "Осталось: " + days + " дней " + hours + " часов";
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    @Override
    public String summary() {
        String base = super.summary();
        base = base + "\n  Expires at: " + expiresAt;
        if (autoRenew) {
            base = base + " (auto-renew)";
        }
        if (!isExpired()) {
            base = base + "\n  " + getTimeRemaining();
        }
        return base;
    }
}