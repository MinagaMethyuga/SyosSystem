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
        private int recommendedRestockQuantity;
        private int availableStockQuantity;

        public RestockAlert(String itemCode, String itemName, int currentShelfQuantity,
                            int recommendedRestockQuantity, int availableStockQuantity) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.currentShelfQuantity = currentShelfQuantity;
            this.recommendedRestockQuantity = recommendedRestockQuantity;
            this.availableStockQuantity = availableStockQuantity;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getCurrentShelfQuantity() { return currentShelfQuantity; }
        public int getRecommendedRestockQuantity() { return recommendedRestockQuantity; }
        public int getAvailableStockQuantity() { return availableStockQuantity; }
    }

    public static List<RestockAlert> getLowStockAlerts() {
        List<RestockAlert> alerts = new ArrayList<>();

        String query = """
            SELECT s.item_code, s.item_name, s.restockLevel, s.quantity as stock_quantity,
                   COALESCE(sh.quantity, 0) as shelf_quantity
            FROM stock s
            LEFT JOIN shelf sh ON s.item_code = sh.item_code
            WHERE COALESCE(sh.quantity, 0) < s.restockLevel
            ORDER BY s.item_code
        """;

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String itemCode = resultSet.getString("item_code");
                String itemName = resultSet.getString("item_name");
                int restockLevel = resultSet.getInt("restockLevel");
                int stockQuantity = resultSet.getInt("stock_quantity");
                int shelfQuantity = resultSet.getInt("shelf_quantity");

                int recommendedRestock = Math.min(restockLevel - shelfQuantity, stockQuantity);

                alerts.add(new RestockAlert(itemCode, itemName, shelfQuantity,
                        recommendedRestock, stockQuantity));
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
        System.out.printf("%-10s %-20s %-12s %-12s %-12s\n",
                "Code", "Name", "Shelf Qty", "Recommended", "Stock Qty");
        System.out.println("...................................................................................");

        for (RestockAlert alert : alerts) {
            System.out.printf("%-10s %-20s %-12d %-12d %-12d\n",
                    alert.getItemCode(),
                    alert.getItemName(),
                    alert.getCurrentShelfQuantity(),
                    alert.getRecommendedRestockQuantity(),
                    alert.getAvailableStockQuantity());
        }

        System.out.println("...................................................................................");
    }

    public static boolean hasLowStockItems() {
        return !getLowStockAlerts().isEmpty();
    }
}