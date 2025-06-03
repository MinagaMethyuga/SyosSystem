package Dashboard;

import Common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Proxy implements DashboardAccess{
    private final String username;
    private final String password;

    //constructor to accept user credentials
    public Proxy(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean viewDashboard() {
        String role = authenticatedUser();
        DashboardAccess realDashboard;

        if (role != null) {
            // Direct the user to the appropriate dashboard
            if (role.equalsIgnoreCase("manager")) {
                // open manager dashboard
                realDashboard = new ManagerDashboard();
                realDashboard.viewDashboard();
            } else if (role.equalsIgnoreCase("cashier")) {
                // Load cashier dashboard
                realDashboard = new CashierDashboard();
                realDashboard.viewDashboard();
            } else {
                System.out.println("Unknown role. Contact administrator.");
            }
            return true;
        } else {
            System.out.println("You are not logged in. User not found in the database. Please try again.");
            return false;
        }
    }

    private String authenticatedUser() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        if (connection != null) {
            // query to check if the user exists in the database
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username.trim());
                statement.setString(2, password.trim());

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    //get users role
                    String role = resultSet.getString("role");
                    //disconnect the database connection
                    connection.close();
                    return role;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Debug: Database connection is having a issue.");
        }
        return null;
    }
}