import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditLog {

    private List<AuditEntry> entries;

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry entry : entries) {
            if (entry.performer().equals(performer)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<AuditEntry> getByAction(String action) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry entry : entries) {
            if (entry.action().equals(action)) {
                result.add(entry);
            }
        }
        return result;
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Журнал аудита пуст.");
            return;
        }
        System.out.println("=== ЖУРНАЛ АУДИТА ===");
        System.out.printf("%-20s %-20s %-15s %-15s %s%n",
                "ВРЕМЯ", "ДЕЙСТВИЕ", "КТО", "ЦЕЛЬ", "ДЕТАЛИ");
        System.out.println("-".repeat(90));
        for (AuditEntry entry : entries) {
            System.out.printf("%-20s %-20s %-15s %-15s %s%n",
                    entry.timestamp(), entry.action(),
                    entry.performer(), entry.target(), entry.details());
        }
        System.out.println("Всего записей: " + entries.size());
    }

    public void saveToFile(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write("ВРЕМЯ | ДЕЙСТВИЕ | КТО | ЦЕЛЬ | ДЕТАЛИ\n");
            writer.write("-".repeat(90) + "\n");
            for (AuditEntry entry : entries) {
                writer.write(entry.format() + "\n");
            }
            writer.close();
            System.out.println("Журнал аудита сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения лога: " + e.getMessage());
        }
    }
}