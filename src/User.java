public record User(String username, String fullName, String email) {

    public static User validate(String username, String fullName, String email) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        if (fullName == null || fullName.isEmpty()) {
            throw new IllegalArgumentException("Полное имя не может быть пустым");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Электронная почта не может быть пустой");
        }

        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Имя пользователя должен быть от 3 до 20 символов");
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Имя пользователя может содержать только латинские буквы, цифры и подчёркивание");
        }

        int atIndex = email.indexOf('@');
        if (atIndex < 1) {
            throw new IllegalArgumentException("Электронная почта должна содержать @");
        }
        String afterAt = email.substring(atIndex + 1);
        if (!afterAt.contains(".") || afterAt.indexOf('.') == 0 || afterAt.endsWith(".")) {
            throw new IllegalArgumentException("Электронная почта должна содержать точку после @");
        }

        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}