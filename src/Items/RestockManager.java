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
        private int currentShelfQuantity;
        private int restockLevel;
        private int recommendedRestockQuantity;
        private int availableStockQuantity;

        public RestockAlert(String itemCode, String itemName, int currentShelfQuantity,
                            int restockLevel, int recommendedRestockQuantity, int availableStockQuantity) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.currentShelfQuantity = currentShelfQuantity;
            this.restockLevel = restockLevel;
            this.recommendedRestockQuantity = recommendedRestockQuantity;
            this.availableStockQuantity = availableStockQuantity;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getCurrentShelfQuantity() { return currentShelfQuantity; }
        public int getRestockLevel() { return restockLevel; }
        public int getRecommendedRestockQuantity() { return recommendedRestockQuantity; }
        public int getAvailableStockQuantity() { return availableStockQuantity; }
    }

    public static List<RestockAlert> getLowStockAlerts() {
        List<RestockAlert> alerts = new ArrayList<>();

        // Fixed query: properly aggregate stock quantities by item_code and get a single restock level per item
        String query = """
            SELECT 
                s.item_code, 
                s.item_name, 
                s.restockLevel,
                SUM(s.quantity) as total_stock_quantity,
                COALESCE(sh.quantity, 0) as shelf_quantity
            FROM stock s
            LEFT JOIN shelf sh ON s.item_code = sh.item_code
            GROUP BY s.item_code, s.item_name, s.restockLevel, sh.quantity
            HAVING COALESCE(sh.quantity, 0) < s.restockLevel
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
                int shelfQuantity = resultSet.getInt("shelf_quantity");

                // Calculate how much is needed to reach the restock level
                int recommendedRestock = Math.min(restockLevel - shelfQuantity, totalStockQuantity);

                alerts.add(new RestockAlert(itemCode, itemName, shelfQuantity,
                        restockLevel, recommendedRestock, totalStockQuantity));
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
        System.out.println("LOW STOCK ALERTS");
        System.out.println("...................................................................................");
        System.out.printf("%-10s %-20s %-12s %-12s %-12s %-15s\n",
                "Code", "Name", "Current Shelf", "Restock Level", "Need to Add", "Available Stock");
        System.out.println("...................................................................................");

        for (RestockAlert alert : alerts) {
            System.out.printf("%-10s %-20s %-12d %-12d %-12d %-15d\n",
                    alert.getItemCode(),
                    alert.getItemName(),
                    alert.getCurrentShelfQuantity(),
                    alert.getRestockLevel(),
                    alert.getRecommendedRestockQuantity(),
                    alert.getAvailableStockQuantity());
        }

        System.out.println("...................................................................................");
        System.out.println("Note: 'Need to Add' shows how many items to move from stock to reach restock level");
        System.out.println("...................................................................................");
    }

    public static boolean hasLowStockItems() {
        return !getLowStockAlerts().isEmpty();
    }
}