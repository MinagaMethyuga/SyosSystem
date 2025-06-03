package Users;

public class UserFactory {
    public static User createUser(String username, String password, int roleChoice) {
        String role = (roleChoice == 1) ? "Manager" : "Cashier";
        return new User(username, password, role);
    }
}
