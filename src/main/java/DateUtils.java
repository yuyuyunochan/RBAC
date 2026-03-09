import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static boolean isBefore(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        String datePart = date;
        if (date.length() > 10) {
            datePart = date.substring(0, 10);
        }

        LocalDate localDate = LocalDate.parse(datePart,
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate newDate = localDate.plusDays(days);
        return newDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String formatRelativeTime(String date) {
        String datePart = date;
        if (date.length() > 10) {
            datePart = date.substring(0, 10);
        }

        LocalDate targetDate = LocalDate.parse(datePart,
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate now = LocalDate.now();

        long daysDiff = ChronoUnit.DAYS.between(now, targetDate);

        if (daysDiff == 0) {
            return "сегодня";
        } else if (daysDiff == 1) {
            return "завтра";
        } else if (daysDiff == -1) {
            return "вчера";
        } else if (daysDiff > 0) {
            return "через " + daysDiff + " дн.";
        } else {
            return Math.abs(daysDiff) + " дн. назад";
        }
    }
}