public record User(String username, String fullName, String email) {

    public static User validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "Username");
        ValidationUtils.requireNonEmpty(fullName, "Full name");
        ValidationUtils.requireNonEmpty(email, "Email");

        username = ValidationUtils.normalizeString(username);
        fullName = ValidationUtils.normalizeString(fullName);
        email = ValidationUtils.normalizeString(email);

        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException(
                    "Username должен содержать 3-20 символов: латинские буквы, цифры, подчёркивание");
        }

        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Email должен содержать @ и точку после @");
        }

        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}