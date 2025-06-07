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
        String query = "SELECT item_code, item_name, quantity, selling_price, discount_price FROM shelf";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String[] stock = new String[5]; // Updated to 5 elements to include discount price
                stock[0] = resultSet.getString("item_code");
                stock[1] = resultSet.getString("item_name");
                stock[2] = String.valueOf(resultSet.getInt("quantity"));

                // Handle selling_price - it might be null for existing records
                double sellingPrice = resultSet.getDouble("selling_price");
                if (resultSet.wasNull()) {
                    stock[3] = "N/A";
                } else {
                    stock[3] = String.format("%.2f", sellingPrice);
                }

                // Handle discount_price - it might be null
                double discountPrice = resultSet.getDouble("discount_price");
                if (resultSet.wasNull()) {
                    stock[4] = "N/A";
                } else {
                    stock[4] = String.format("%.2f", discountPrice);
                }

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