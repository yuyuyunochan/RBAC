import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    @Test
    @DisplayName("promptString — обязательное поле")
    void testPromptStringRequired() {
        Scanner scanner = new Scanner("Ivan Kydrya\n");
        String result = ConsoleUtils.promptString(scanner, "Имя: ", true);
        assertEquals("Ivan Kydrya", result);
    }

    @Test
    @DisplayName("promptString — необязательное пустое поле")
    void testPromptStringOptional() {
        Scanner scanner = new Scanner("\n");
        String result = ConsoleUtils.promptString(scanner, "Имя: ", false);
        assertEquals("", result);
    }

    @Test
    @DisplayName("promptInt — корректный ввод")
    void testPromptInt() {
        Scanner scanner = new Scanner("3\n");
        int result = ConsoleUtils.promptInt(scanner, "Число", 1, 5);
        assertEquals(3, result);
    }

    @Test
    @DisplayName("promptInt — повторный запрос при ошибке")
    void testPromptIntRetry() {
        Scanner scanner = new Scanner("abc\n0\n3\n");
        int result = ConsoleUtils.promptInt(scanner, "Число", 1, 5);
        assertEquals(3, result);
    }

    @Test
    @DisplayName("promptYesNo — да")
    void testPromptYesNoYes() {
        Scanner scanner = new Scanner("да\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Подтвердить?"));
    }

    @Test
    @DisplayName("promptYesNo — нет")
    void testPromptYesNoNo() {
        Scanner scanner = new Scanner("нет\n");
        assertFalse(ConsoleUtils.promptYesNo(scanner, "Подтвердить?"));
    }

    @Test
    @DisplayName("promptChoice — выбор из списка")
    void testPromptChoice() {
        List<String> options = new ArrayList<>();
        options.add("Admin");
        options.add("Manager");
        options.add("Viewer");

        Scanner scanner = new Scanner("2\n");
        String result = ConsoleUtils.promptChoice(scanner, "Выберите роль:", options);
        assertEquals("Manager", result);
    }

    @Test
    @DisplayName("promptChoice — пустой список")
    void testPromptChoiceEmpty() {
        List<String> options = new ArrayList<>();
        Scanner scanner = new Scanner("");
        String result = ConsoleUtils.promptChoice(scanner, "Выберите:", options);
        assertNull(result);
    }
}