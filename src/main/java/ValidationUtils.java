public class ValidationUtils {

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches("[a-zA-Z0-9_]{3,20}");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 1) {
            return false;
        }
        String afterAt = email.substring(atIndex + 1);
        if (!afterAt.contains(".") || afterAt.indexOf('.') == 0 || afterAt.endsWith(".")) {
            return false;
        }
        return true;
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return isValidDateParts(date.substring(0, 4),
                    date.substring(5, 7), date.substring(8, 10));
        }
        if (date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
            boolean datePart = isValidDateParts(date.substring(0, 4),
                    date.substring(5, 7), date.substring(8, 10));
            int hour = Integer.parseInt(date.substring(11, 13));
            int minute = Integer.parseInt(date.substring(14, 16));
            return datePart && hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        }
        return false;
    }

    private static boolean isValidDateParts(String yearStr, String monthStr, String dayStr) {
        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);
        return year >= 1900 && year <= 2100
                && month >= 1 && month <= 12
                && day >= 1 && day <= 31;
    }

    public static String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
}