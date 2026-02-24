public class Main {
    public static void main(String[] args) {
        System.out.println("Создание пользователей");

        User user1 = User.validate("user1", "main.java.User Name1", "email@gmail.com");
        System.out.println(user1.format());

        try {
            User badUser = User.validate("no", "No", "email@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            User badEmail = User.validate("username", "main.java.User Name2", "email@com");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        try {
            User badUsername = User.validate("no username", "main.java.User Name3", "email@gmail.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\nСоздание прав доступа");

        Permission readUsers = new Permission("read", "Users", "Может просматривать список пользователей");
        System.out.println(readUsers.format());

        Permission writeUsers = new Permission("write", "Users", "Может создавать и редактировать пользователей");
        System.out.println(writeUsers.format());

        Permission deleteUsers = new Permission("delete", "Users", "Может удалять пользователей");
        System.out.println(deleteUsers.format());

        System.out.println("matches READ/users: " + readUsers.matches("READ", "users"));
        System.out.println("matches WRITE/users: " + readUsers.matches("WRITE", "users"));

        System.out.println("\nСоздание ролей");

        Role adminRole = new Role("Admin", "Полный доступ к пользователям");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        System.out.println(adminRole.format());

        System.out.println("Has READ on users: " + adminRole.hasPermission("READ", "users"));
        System.out.println("Has WRITE on reports: " + adminRole.hasPermission("WRITE", "reports"));

        System.out.println("\nМетаданные назначения");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Initial setup");
        System.out.println(meta.format());

        System.out.println("\nПостоянное назначение");

        PermanentAssignment permAssign = new PermanentAssignment(user1, adminRole, meta);
        System.out.println(permAssign.summary());
        System.out.println("Active: " + permAssign.isActive());

        permAssign.revoke();
        System.out.println("\nПосле отмены:");
        System.out.println(permAssign.summary());
        System.out.println("Active: " + permAssign.isActive());

        System.out.println("\nВременное назначение");

        User user2 = User.validate("username", "main.java.User Name1", "email@gmail.com");
        Role viewerRole = new Role("Viewer", "Доступ только к просмотру");
        viewerRole.addPermission(readUsers);

        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Временный доступ");

        TemporaryAssignment tempAssign = new TemporaryAssignment(
                user2, viewerRole, meta2, "2026-12-31 23:59", false);
        System.out.println(tempAssign.summary());

        TemporaryAssignment expiredAssign = new TemporaryAssignment(
                user2, viewerRole, meta2, "2020-01-01 00:00", false);
        System.out.println("\nПросроченное назначение:");
        System.out.println(expiredAssign.summary());
    }
}