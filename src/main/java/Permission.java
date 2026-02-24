public record Permission(String name, String resource, String description) {

    public Permission {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Название права не может быть пустым");
        }
        if (resource == null || resource.isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }

        name = name.toUpperCase();

        if (name.contains(" ")) {
            throw new IllegalArgumentException("Название права не должно содержать пробелов");
        }

        resource = resource.toLowerCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = name.contains(namePattern.toUpperCase());
        boolean resourceMatches = resource.contains(resourcePattern.toLowerCase());
        return nameMatches && resourceMatches;
    }
}