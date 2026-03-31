import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditLog {

    private final List<AuditEntry> entries;
    private final BlockingQueue<AuditEntry> queue;
    private final Thread workerThread;
    private volatile boolean running;

    public AuditLog() {
        this.entries = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>();
        this.running = true;

        this.workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    AuditEntry entry = queue.take();
                    synchronized (entries) {
                        entries.add(entry);
                    }
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }
        });

        this.workerThread.setDaemon(true);
        this.workerThread.setName("AuditLog-Worker");
        this.workerThread.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = DateUtils.getCurrentDateTime();
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        queue.offer(entry);
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        List<AuditEntry> result = new ArrayList<>();
        synchronized (entries) {
            for (AuditEntry entry : entries) {
                if (entry.performer().equals(performer)) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public List<AuditEntry> getByAction(String action) {
        List<AuditEntry> result = new ArrayList<>();
        synchronized (entries) {
            for (AuditEntry entry : entries) {
                if (entry.action().equals(action)) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public int size() {
        synchronized (entries) {
            return entries.size();
        }
    }

    public void printLog() {
        List<AuditEntry> snapshot = getAll();

        if (snapshot.isEmpty()) {
            System.out.println("Журнал аудита пуст.");
            return;
        }

        System.out.println("=== ЖУРНАЛ АУДИТА ===");
        for (AuditEntry entry : snapshot) {
            System.out.println(entry.format());
        }
        System.out.println("Всего записей: " + snapshot.size());
    }

    public void saveToFile(String filename) {
        List<AuditEntry> snapshot = getAll();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (AuditEntry entry : snapshot) {
                writer.write(entry.format());
                writer.newLine();
            }
            writer.close();
            System.out.println("Журнал аудита сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения журнала: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        workerThread.interrupt();
    }
}