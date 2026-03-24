import java.util.ArrayList;
import java.util.List;

public class MultiThread {

    static final int THREAD_COUNT = 5;
    static final int PROGRESS_LENGTH = 20;
    static final int STEP_DELAY_MS = 100;

    static String[] threadStatus;

    public static void main(String[] args) throws InterruptedException {

        threadStatus = new String[THREAD_COUNT];
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            threadStatus[i] = "";
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadNumber = i + 1;

            Thread thread = new Thread(() -> {
                runCalculation(threadNumber);
            });

            threads.add(thread);
            thread.start();
        }

        Thread displayThread = new Thread(() -> {
            while (!allThreadsFinished(threads)) {
                printAllProgress();
                sleep(50);
            }
            printAllProgress();
        });
        displayThread.start();

        for (Thread thread : threads) {
            thread.join();
        }
        displayThread.join();
    }

    static void runCalculation(int threadNumber) {
        long threadId = Thread.currentThread().getId();
        long startTime = System.currentTimeMillis();

        StringBuilder progressBar = new StringBuilder();

        for (int step = 0; step < PROGRESS_LENGTH; step++) {
            progressBar.append("#");

            String status = String.format(
                    "Поток %d ID: %3d [%-" + PROGRESS_LENGTH + "s]",
                    threadNumber,
                    threadId,
                    progressBar.toString()
            );

            threadStatus[threadNumber - 1] = status;

            sleep(STEP_DELAY_MS + (int)(Math.random() * 100));
        }

        long totalTime = System.currentTimeMillis() - startTime;

        threadStatus[threadNumber - 1] = String.format(
                "Поток %d ID: %3d [%-" + PROGRESS_LENGTH + "s]",
                threadNumber,
                threadId,
                progressBar.toString(),
                totalTime
        );
    }

    static synchronized void printAllProgress() {
        System.out.print("\033[" + THREAD_COUNT + "A");
        System.out.print("\033[J");

        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.println(threadStatus[i]);
        }
    }

    static boolean allThreadsFinished(List<Thread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return false;
            }
        }
        return true;
    }

    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}