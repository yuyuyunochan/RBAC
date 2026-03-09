import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (required && input.isEmpty()) {
                System.out.println("Это поле обязательно. Попробуйте ещё раз.");
                continue;
            }
            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message + " (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Число должно быть от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Введите число.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да") || input.equals("yes") || input.equals("y") || input.equals("д")) {
                return true;
            }
            if (input.equals("нет") || input.equals("no") || input.equals("n") || input.equals("н")) {
                return false;
            }
            System.out.println("Введите 'да' или 'нет'.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options.isEmpty()) {
            System.out.println("Список пуст.");
            return null;
        }

        System.out.println(message);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i).toString());
        }

        int choice = promptInt(scanner, "Ваш выбор", 1, options.size());
        return options.get(choice - 1);
    }
}