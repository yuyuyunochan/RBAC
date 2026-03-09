import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    @DisplayName("formatTable — создаёт таблицу с рамками")
    void testFormatTable() {
        String[] headers = {"USERNAME", "EMAIL"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"ivan_Kydrya", "ivan@company.ru"});
        rows.add(new String[]{"vasilina_l", "vasilina@company.ru"});

        String table = FormatUtils.formatTable(headers, rows);
        assertTrue(table.contains("ivan_Kydrya"));
        assertTrue(table.contains("vasilina_l"));
        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
    }

    @Test
    @DisplayName("formatBox — обрамляет текст рамкой")
    void testFormatBox() {
        String result = FormatUtils.formatBox("Hello World");
        assertTrue(result.contains("+"));
        assertTrue(result.contains("Hello World"));
    }

    @Test
    @DisplayName("formatHeader — заголовок секции")
    void testFormatHeader() {
        String result = FormatUtils.formatHeader("Тест");
        assertTrue(result.contains("Тест"));
        assertTrue(result.contains("="));
    }

    @Test
    @DisplayName("truncate — обрезает длинную строку")
    void testTruncate() {
        assertEquals("Hello...", FormatUtils.truncate("Hello World!", 8));
        assertEquals("Short", FormatUtils.truncate("Short", 10));
        assertEquals("", FormatUtils.truncate(null, 10));
    }

    @Test
    @DisplayName("padRight — дополняет пробелами справа")
    void testPadRight() {
        assertEquals("abc   ", FormatUtils.padRight("abc", 6));
        assertEquals("abcdef", FormatUtils.padRight("abcdef", 3));
        assertEquals("      ", FormatUtils.padRight(null, 6));
    }

    @Test
    @DisplayName("padLeft — дополняет пробелами слева")
    void testPadLeft() {
        assertEquals("   abc", FormatUtils.padLeft("abc", 6));
        assertEquals("abcdef", FormatUtils.padLeft("abcdef", 3));
    }

    @Test
    @DisplayName("formatTable — пустая таблица")
    void testFormatTableEmpty() {
        String[] headers = {"COL1", "COL2"};
        List<String[]> rows = new ArrayList<>();
        String table = FormatUtils.formatTable(headers, rows);
        assertTrue(table.contains("COL1"));
    }
}