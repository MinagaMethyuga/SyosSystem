package Items;

import Common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RestockManager {

    public static class RestockAlert {
        private String itemCode;
        private String itemName;
        private int totalStockQuantity;
        private int restockLevel;
        private int recommendedRestockQuantity;

        public RestockAlert(String itemCode, String itemName, int totalStockQuantity,
                            int restockLevel, int recommendedRestockQuantity) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.totalStockQuantity = totalStockQuantity;
            this.restockLevel = restockLevel;
            this.recommendedRestockQuantity = recommendedRestockQuantity;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getTotalStockQuantity() { return totalStockQuantity; }
        public int getRestockLevel() { return restockLevel; }
        public int getRecommendedRestockQuantity() { return recommendedRestockQuantity; }
    }

    public static List<RestockAlert> getLowStockAlerts() {
        List<RestockAlert> alerts = new ArrayList<>();

        // Modified query: Check if total stock quantity is below restock level
        String query = """
            SELECT 
                s.item_code, 
                s.item_name, 
                s.restockLevel,
                SUM(s.quantity) as total_stock_quantity
            FROM stock s
            GROUP BY s.item_code, s.item_name, s.restockLevel
            HAVING SUM(s.quantity) < s.restockLevel
            ORDER BY s.item_code
        """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String itemCode = resultSet.getString("item_code");
                String itemName = resultSet.getString("item_name");
                int restockLevel = resultSet.getInt("restockLevel");
                int totalStockQuantity = resultSet.getInt("total_stock_quantity");

                // Calculate how much more stock is needed to reach the restock level
                int recommendedRestock = restockLevel - totalStockQuantity;

                alerts.add(new RestockAlert(itemCode, itemName, totalStockQuantity,
                        restockLevel, recommendedRestock));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching low stock alerts: " + e.getMessage());
        }

        return alerts;
    }

    public static void displayLowStockAlerts() {
        List<RestockAlert> alerts = getLowStockAlerts();

        if (alerts.isEmpty()) {
            System.out.println("No low stock alerts at this time.");
            return;
        }

        System.out.println("...................................................................................");
        System.out.println("LOW STOCK ALERTS (Based on Total Stock Quantity)");
        System.out.println("...................................................................................");
        System.out.printf("%-10s %-20s %-15s %-15s %-20s\n",
                "Code", "Name", "Current Stock", "Restock Level", "Need to Purchase");
        System.out.println("...................................................................................");

        for (RestockAlert alert : alerts) {
            System.out.printf("%-10s %-20s %-15d %-15d %-20d\n",
                    alert.getItemCode(),
                    alert.getItemName(),
                    alert.getTotalStockQuantity(),
                    alert.getRestockLevel(),
                    alert.getRecommendedRestockQuantity());
        }

        System.out.println("...................................................................................");
        System.out.println("Note: 'Need to Purchase' shows how many more items need to be bought to reach restock level");
        System.out.println("...................................................................................");
    }

    public static boolean hasLowStockItems() {
        return !getLowStockAlerts().isEmpty();
    }
}