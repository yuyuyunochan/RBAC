import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    @DisplayName("isValidUsername — допустимые имена")
    void testValidUsernames() {
        assertTrue(ValidationUtils.isValidUsername("ivan_Kydrya"));
        assertTrue(ValidationUtils.isValidUsername("vasilina"));
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("abc"));
    }

    @Test
    @DisplayName("isValidUsername — недопустимые имена")
    void testInvalidUsernames() {
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("ivan Kydrya"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21)));
    }

    @Test
    @DisplayName("isValidEmail — допустимые email")
    void testValidEmails() {
        assertTrue(ValidationUtils.isValidEmail("vasilina@company.ru"));
        assertTrue(ValidationUtils.isValidEmail("ivan@mail.com"));
        assertTrue(ValidationUtils.isValidEmail("test@sub.domain.org"));
    }

    @Test
    @DisplayName("isValidEmail — недопустимые email")
    void testInvalidEmails() {
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("noatsign"));
        assertFalse(ValidationUtils.isValidEmail("@nodomain"));
        assertFalse(ValidationUtils.isValidEmail("user@.com"));
        assertFalse(ValidationUtils.isValidEmail("user@com."));
    }

    @Test
    @DisplayName("isValidDate — допустимые даты")
    void testValidDates() {
        assertTrue(ValidationUtils.isValidDate("2025-01-15"));
        assertTrue(ValidationUtils.isValidDate("2025-12-31 23:59"));
        assertTrue(ValidationUtils.isValidDate("2030-06-15 00:00"));
    }

    @Test
    @DisplayName("isValidDate — недопустимые даты")
    void testInvalidDates() {
        assertFalse(ValidationUtils.isValidDate(null));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate("15-01-2025"));
        assertFalse(ValidationUtils.isValidDate("2025-13-01"));
        assertFalse(ValidationUtils.isValidDate("2025-01-32"));
        assertFalse(ValidationUtils.isValidDate("not a date"));
    }

    @Test
    @DisplayName("normalizeString — убирает лишние пробелы")
    void testNormalizeString() {
        assertEquals("Ivan Kydrya", ValidationUtils.normalizeString("  Ivan   Kydrya  "));
        assertEquals("Vasilina", ValidationUtils.normalizeString("  Vasilina  "));
        assertEquals("", ValidationUtils.normalizeString(null));
    }

    @Test
    @DisplayName("requireNonEmpty — пустая строка бросает исключение")
    void testRequireNonEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty(null, "Поле"));
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty("", "Поле"));
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty("   ", "Поле"));
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonEmpty("значение", "Поле"));
    }
}