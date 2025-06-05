package Items;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.ManagerDashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RestockItems {
    private Scanner scanner = ScannerInstance.getScanner();

    public void restockItemsMenu() {
        while (true) {
            System.out.println("...................................................................................");
            System.out.println("Restock Management System");
            System.out.println("...................................................................................");

            // Show low stock alerts if any
            if (RestockManager.hasLowStockItems()) {
                System.out.println("⚠️  LOW STOCK ALERT: Some items need restocking!");
                System.out.println("...................................................................................");
            }

            System.out.println("1. View Available Stock Items");
            System.out.println("2. Move Items to Shelf");
            System.out.println("3. View Current Shelf Status");
            System.out.println("4. View Low Stock Alerts");
            System.out.println("5. Back to Dashboard");
            System.out.print("Please select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1-5.");
                continue;
            }

            switch (choice) {
                case 1:
                    displayAvailableStock();
                    break;
                case 2:
                    moveItemsToShelf();
                    break;
                case 3:
                    displayCurrentShelfStatus();
                    break;
                case 4:
                    displayLowStockAlerts();
                    break;
                case 5:
                    new ManagerDashboard().viewDashboard();
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1-5.");
            }
        }
    }

    private void displayAvailableStock() {
        System.out.println("...................................................................................");
        System.out.println("Available Stock Items");
        System.out.println("...................................................................................");

        StockDAO stockDAO = new StockDAO();
        List<String[]> stockList = stockDAO.getStockDetails();

        if (stockList.isEmpty()) {
            System.out.println("No items available in stock.");
        } else {
            StockView.displayStock(stockList);
        }

        System.out.println("...................................................................................");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private void displayLowStockAlerts() {
        System.out.println("...................................................................................");
        RestockManager.displayLowStockAlerts();
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private void moveItemsToShelf() {
        System.out.println("...................................................................................");
        System.out.println("Move Items from Stock to Shelf");
        System.out.println("...................................................................................");

        // First, show available stock
        StockDAO stockDAO = new StockDAO();
        List<String[]> stockList = stockDAO.getStockDetails();

        if (stockList.isEmpty()) {
            System.out.println("No items available in stock to move to shelf.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return;
        }

        System.out.println("Available Stock Items:");
        StockView.displayStock(stockList);
        System.out.println("...................................................................................");

        String itemCode;
        do {
            System.out.print("Enter Item Code to move to shelf: ");
            itemCode = scanner.nextLine().toUpperCase().trim();
            if (itemCode.isEmpty()) {
                System.out.println("Item code cannot be empty.");
            }
        } while (itemCode.isEmpty());

        // Check if item exists in stock and get details
        String[] stockItem = getStockItemDetails(itemCode);
        if (stockItem == null) {
            System.out.println("Item with code '" + itemCode + "' not found in stock.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return;
        }

        String itemName = stockItem[1];
        int availableQuantity = Integer.parseInt(stockItem[2]);

        System.out.println("Item found: " + itemName);
        System.out.println("Available quantity in stock: " + availableQuantity);

        // Get quantity to move
        int quantityToMove;
        while (true) {
            System.out.print("Enter quantity to move to shelf (max " + availableQuantity + "): ");
            try {
                quantityToMove = Integer.parseInt(scanner.nextLine());
                if (quantityToMove <= 0) {
                    System.out.println("Quantity must be a positive number.");
                } else if (quantityToMove > availableQuantity) {
                    System.out.println("Cannot move more than available quantity (" + availableQuantity + ").");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        // Confirm the move
        System.out.println("...................................................................................");
        System.out.println("Move Confirmation:");
        System.out.println("Item: " + itemName + " (" + itemCode + ")");
        System.out.println("Quantity to move: " + quantityToMove);
        System.out.println("Remaining in stock: " + (availableQuantity - quantityToMove));
        System.out.println("...................................................................................");

        while (true) {
            System.out.print("Confirm move to shelf? (Y/N): ");
            String confirmation = scanner.nextLine().toUpperCase().trim();
            if (confirmation.equals("Y")) {
                performMoveToShelf(itemCode, itemName, quantityToMove, availableQuantity);
                break;
            } else if (confirmation.equals("N")) {
                System.out.println("Move operation cancelled.");
                break;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N'.");
            }
        }

        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private String[] getStockItemDetails(String itemCode) {
        String query = "SELECT item_code, item_name, quantity, price, purchaseDate, expirationDate FROM stock WHERE item_code = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, itemCode);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String[] stockItem = new String[6];
                stockItem[0] = resultSet.getString("item_code");
                stockItem[1] = resultSet.getString("item_name");
                stockItem[2] = String.valueOf(resultSet.getInt("quantity"));
                stockItem[3] = String.valueOf(resultSet.getDouble("price"));
                stockItem[4] = resultSet.getDate("purchaseDate").toString();
                stockItem[5] = resultSet.getDate("expirationDate") != null
                        ? resultSet.getDate("expirationDate").toString() : "N/A";
                return stockItem;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching stock item details: " + e.getMessage());
        }
        return null;
    }

    private void performMoveToShelf(String itemCode, String itemName, int quantityToMove, int availableQuantity) {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Check if item already exists on shelf
            int currentShelfQuantity = getShelfQuantity(itemCode, connection);

            if (currentShelfQuantity >= 0) {
                // Item exists on shelf, update quantity
                updateShelfQuantity(itemCode, currentShelfQuantity + quantityToMove, connection);
            } else {
                // Item doesn't exist on shelf, insert new record
                insertNewShelfItem(itemCode, itemName, quantityToMove, connection);
            }

            // Update stock quantity
            int newStockQuantity = availableQuantity - quantityToMove;
            if (newStockQuantity == 0) {
                // Remove item from stock if quantity becomes 0
                removeFromStock(itemCode, connection);
            } else {
                // Update stock quantity
                updateStockQuantity(itemCode, newStockQuantity, connection);
            }

            connection.commit(); // Commit transaction
            System.out.println("Successfully moved " + quantityToMove + " units of " + itemName + " to shelf.");

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback on error
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error moving items to shelf: " + e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Reset auto-commit
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private int getShelfQuantity(String itemCode, Connection connection) throws SQLException {
        String query = "SELECT quantity FROM shelf WHERE item_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemCode);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("quantity");
            }
        }
        return -1; // Item not found on shelf
    }

    private void updateShelfQuantity(String itemCode, int newQuantity, Connection connection) throws SQLException {
        String query = "UPDATE shelf SET quantity = ? WHERE item_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newQuantity);
            statement.setString(2, itemCode);
            statement.executeUpdate();
        }
    }

    private void insertNewShelfItem(String itemCode, String itemName, int quantity, Connection connection) throws SQLException {
        String query = "INSERT INTO shelf (item_code, item_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemCode);
            statement.setString(2, itemName);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        }
    }

    private void updateStockQuantity(String itemCode, int newQuantity, Connection connection) throws SQLException {
        String query = "UPDATE stock SET quantity = ? WHERE item_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newQuantity);
            statement.setString(2, itemCode);
            statement.executeUpdate();
        }
    }

    private void removeFromStock(String itemCode, Connection connection) throws SQLException {
        String query = "DELETE FROM stock WHERE item_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, itemCode);
            statement.executeUpdate();
        }
    }

    private void displayCurrentShelfStatus() {
        System.out.println("...................................................................................");
        System.out.println("Current Shelf Status");
        System.out.println("...................................................................................");

        List<String[]> shelfStocks = ShelfStockDAO.getShelfStocks();
        if (shelfStocks.isEmpty()) {
            System.out.println("No items currently on shelf.");
        } else {
            ShelfStockView shelfStockView = new ShelfStockView();
            shelfStockView.shelfStockView(shelfStocks);
        }

        System.out.println("...................................................................................");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}