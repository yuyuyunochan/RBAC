import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("registerCommand — команда регистрируется")
    void testRegisterCommand() {
        parser.registerCommand("test", "Test command", (scanner, sys) -> {});
        assertTrue(parser.hasCommand("test"));
        assertEquals(1, parser.getCommandCount());
    }

    @Test
    @DisplayName("registerCommand — несколько команд")
    void testRegisterMultipleCommands() {
        parser.registerCommand("cmd1", "Command 1", (scanner, sys) -> {});
        parser.registerCommand("cmd2", "Command 2", (scanner, sys) -> {});
        parser.registerCommand("cmd3", "Command 3", (scanner, sys) -> {});
        assertEquals(3, parser.getCommandCount());
    }

    @Test
    @DisplayName("hasCommand — существующая команда")
    void testHasCommandTrue() {
        parser.registerCommand("test", "Test", (scanner, sys) -> {});
        assertTrue(parser.hasCommand("test"));
    }

    @Test
    @DisplayName("hasCommand — несуществующая команда")
    void testHasCommandFalse() {
        assertFalse(parser.hasCommand("nonexistent"));
    }

    @Test
    @DisplayName("hasCommand — регистронезависимый поиск")
    void testHasCommandCaseInsensitive() {
        parser.registerCommand("Test", "Test", (scanner, sys) -> {});
        assertTrue(parser.hasCommand("test"));
        assertTrue(parser.hasCommand("TEST"));
    }

    @Test
    @DisplayName("executeCommand — выполняет существующую команду")
    void testExecuteCommand() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (scanner, sys) -> {
            executed[0] = true;
        });

        Scanner scanner = new Scanner("");
        parser.executeCommand("test", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("executeCommand — несуществующая команда не падает")
    void testExecuteNonExistentCommand() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("nonexistent", scanner, system);
        });
    }

    @Test
    @DisplayName("parseAndExecute — выполняет команду из строки")
    void testParseAndExecute() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (scanner, sys) -> {
            executed[0] = true;
        });

        Scanner scanner = new Scanner("");
        parser.parseAndExecute("test", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("parseAndExecute — пустая строка не падает")
    void testParseAndExecuteEmpty() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("", scanner, system);
        });
    }

    @Test
    @DisplayName("parseAndExecute — null не падает")
    void testParseAndExecuteNull() {
        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.parseAndExecute(null, scanner, system);
        });
    }

    @Test
    @DisplayName("parseAndExecute — строка с пробелами в начале")
    void testParseAndExecuteWithSpaces() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (scanner, sys) -> {
            executed[0] = true;
        });

        Scanner scanner = new Scanner("");
        parser.parseAndExecute("  test  ", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("parseAndExecute — команда с аргументами")
    void testParseAndExecuteWithArgs() {
        boolean[] executed = {false};
        parser.registerCommand("test", "Test", (scanner, sys) -> {
            executed[0] = true;
        });

        Scanner scanner = new Scanner("");
        parser.parseAndExecute("test arg1 arg2", scanner, system);
        assertTrue(executed[0]);
    }

    @Test
    @DisplayName("printHelp — не падает")
    void testPrintHelp() {
        parser.registerCommand("test1", "First command", (scanner, sys) -> {});
        parser.registerCommand("test2", "Second command", (scanner, sys) -> {});

        assertDoesNotThrow(() -> {
            parser.printHelp();
        });
    }

    @Test
    @DisplayName("executeCommand — обрабатывает исключение в команде")
    void testExecuteCommandWithException() {
        parser.registerCommand("bad", "Bad command", (scanner, sys) -> {
            throw new RuntimeException("Test error");
        });

        Scanner scanner = new Scanner("");
        assertDoesNotThrow(() -> {
            parser.executeCommand("bad", scanner, system);
        });
    }
}