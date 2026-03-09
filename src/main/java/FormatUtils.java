import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length && i < widths.length; i++) {
                if (row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
        }

        for (int i = 0; i < widths.length; i++) {
            widths[i] += 2;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(buildSeparator(widths)).append("\n");

        sb.append(buildRow(headers, widths)).append("\n");

        sb.append(buildSeparator(widths)).append("\n");

        for (String[] row : rows) {
            sb.append(buildRow(row, widths)).append("\n");
        }

        sb.append(buildSeparator(widths)).append("\n");

        return sb.toString();
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int width : widths) {
            for (int i = 0; i < width; i++) {
                sb.append("-");
            }
            sb.append("+");
        }
        return sb.toString();
    }

    private static String buildRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < widths.length; i++) {
            String value = i < values.length ? values[i] : "";
            sb.append(" ").append(padRight(value, widths[i] - 1));
            sb.append("|");
        }
        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLen = 0;
        for (String line : lines) {
            if (line.length() > maxLen) {
                maxLen = line.length();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("+").append("-".repeat(maxLen + 2)).append("+\n");
        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxLen)).append(" |\n");
        }
        sb.append("+").append("-".repeat(maxLen + 2)).append("+\n");
        return sb.toString();
    }

    public static String formatHeader(String text) {
        int totalWidth = text.length() + 6;
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(totalWidth)).append("\n");
        sb.append("== ").append(text).append(" ==\n");
        sb.append("=".repeat(totalWidth)).append("\n");
        return sb.toString();
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        if (maxLength <= 3) return text.substring(0, maxLength);
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - text.length()) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }
}