import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String actualReason = reason;
        if (actualReason == null) {
            actualReason = "";
        }

        return new AssignmentMetadata(assignedBy, currentTime, actualReason);
    }

    public String format() {
        String result = "Assigned by: " + assignedBy + " at " + assignedAt;
        if (reason != null && !reason.isEmpty()) {
            result = result + "\n  Reason: " + reason;
        }
        return result;
    }
}