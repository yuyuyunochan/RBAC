import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);

        System.out.println("** Welcome to RBAC Management System  **");
        System.out.println("*     RBAC Management System           *");
        System.out.println("*     Введите 'help' для справки       *");
        System.out.println("****************************************");
        System.out.println();

        while (true) {
            System.out.print("[" + system.getCurrentUser() + "] > ");
            String input = scanner.nextLine();
            parser.parseAndExecute(input, scanner, system);
            System.out.println();
        }
    }
}