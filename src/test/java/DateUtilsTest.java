import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    @DisplayName("getCurrentDate — возвращает дату в формате yyyy-MM-dd")
    void testGetCurrentDate() {
        String date = DateUtils.getCurrentDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    @DisplayName("getCurrentDateTime — возвращает дату и время")
    void testGetCurrentDateTime() {
        String dateTime = DateUtils.getCurrentDateTime();
        assertNotNull(dateTime);
        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("isBefore — первая дата раньше второй")
    void testIsBefore() {
        assertTrue(DateUtils.isBefore("2024-01-01", "2025-01-01"));
        assertFalse(DateUtils.isBefore("2025-01-01", "2024-01-01"));
        assertFalse(DateUtils.isBefore("2025-01-01", "2025-01-01"));
    }

    @Test
    @DisplayName("isAfter — первая дата позже второй")
    void testIsAfter() {
        assertTrue(DateUtils.isAfter("2025-01-01", "2024-01-01"));
        assertFalse(DateUtils.isAfter("2024-01-01", "2025-01-01"));
        assertFalse(DateUtils.isAfter("2025-01-01", "2025-01-01"));
    }

    @Test
    @DisplayName("addDays — добавляет дни")
    void testAddDays() {
        assertEquals("2025-01-11", DateUtils.addDays("2025-01-01", 10));
        assertEquals("2025-02-01", DateUtils.addDays("2025-01-31", 1));
        assertEquals("2024-12-31", DateUtils.addDays("2025-01-01", -1));
    }

    @Test
    @DisplayName("addDays — работает с датой-временем")
    void testAddDaysWithTime() {
        String result = DateUtils.addDays("2025-01-01 12:30", 5);
        assertEquals("2025-01-06", result);
    }

    @Test
    @DisplayName("formatRelativeTime — сегодня")
    void testFormatRelativeTimeToday() {
        String today = DateUtils.getCurrentDate();
        assertEquals("сегодня", DateUtils.formatRelativeTime(today));
    }

    @Test
    @DisplayName("formatRelativeTime — прошлая дата")
    void testFormatRelativeTimePast() {
        String result = DateUtils.formatRelativeTime("2020-01-01");
        assertTrue(result.contains("дн. назад"));
    }

    @Test
    @DisplayName("formatRelativeTime — будущая дата")
    void testFormatRelativeTimeFuture() {
        String result = DateUtils.formatRelativeTime("2030-12-31");
        assertTrue(result.contains("через"));
    }
}