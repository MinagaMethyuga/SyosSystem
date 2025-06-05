package Items;

import Common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ShelfStockDAO {

    // Method name should be consistent - using getShelfStocks to match usage in ManagerDashboard
    public static List<String[]> getShelfStocks() {
        List<String[]> shelfStocks = new ArrayList<>();
        String query = "SELECT item_code, item_name, quantity FROM shelf";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String[] stock = new String[3]; // Only need 3 elements for shelf data
                stock[0] = resultSet.getString("item_code");
                stock[1] = resultSet.getString("item_name");
                stock[2] = String.valueOf(resultSet.getInt("quantity"));

                shelfStocks.add(stock);
            }

        } catch (Exception e) {
            System.out.println("Error fetching shelf stock data: " + e.getMessage());
            e.printStackTrace();
        }
        return shelfStocks;
    }

    // Alternative method name for consistency with your original code
    public static List<String[]> getShelfDetails() {
        return getShelfStocks();
    }
}