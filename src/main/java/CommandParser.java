import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private Map<String, Command> commands;
    private Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new LinkedHashMap<>();
        this.commandDescriptions = new LinkedHashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());
        if (command == null) {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд");
            return;
        }
        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении команды: " + e.getMessage());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        String trimmed = input.trim();
        String commandName = trimmed.split("\\s+")[0].toLowerCase();
        executeCommand(commandName, scanner, system);
    }

    public void printHelp() {
        System.out.println("********************************************************");
        System.out.println("*                  ДОСТУПНЫЕ КОМАНДЫ                   *");
        System.out.println("********************************************************");

        String currentCategory = "";

        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            String name = entry.getKey();
            String description = entry.getValue();

            String category = "";
            if (name.startsWith("user")) {
                category = "ПОЛЬЗОВАТЕЛИ";
            } else if (name.startsWith("role")) {
                category = "РОЛИ";
            } else if (name.startsWith("assign") || name.startsWith("revoke")) {
                category = "НАЗНАЧЕНИЯ";
            } else if (name.startsWith("perm")) {
                category = "ПРАВА";
            } else {
                category = "СЛУЖЕБНЫЕ";
            }

            if (!category.equals(currentCategory)) {
                currentCategory = category;
                System.out.println("                                                      ");
                System.out.println(" - " + category + " - ");
            }

            System.out.printf("  %-25s %s%n", name, description);
        }

        System.out.println("*******************************************************");
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name.toLowerCase());
    }

    public int getCommandCount() {
        return commands.size();
    }
}