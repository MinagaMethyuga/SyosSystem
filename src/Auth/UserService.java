package Auth;

import Common.DatabaseConnection;
import Users.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserService {
    public static void saveUser(User user) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getRole());
            preparedStatement.executeUpdate();


            // pass the user to the login
            Login login = new Login();
            login.login();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to the database", e);
        }
    }
}