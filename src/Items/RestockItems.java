package Items;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.ManagerDashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RestockItems {
    private final Scanner scanner = ScannerInstance.getScanner();

    // Inner class to represent stock batches
    private static class StockBatch {
        String itemCode;
        String itemName;
        int quantity;
        double price;
        LocalDate purchaseDate;
        LocalDate expirationDate;
        int stockId;

        public StockBatch(String itemCode, String itemName, int quantity, double price,
                          LocalDate purchaseDate, LocalDate expirationDate, int stockId) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
            this.purchaseDate = purchaseDate;
            this.expirationDate = expirationDate;
            this.stockId = stockId;
        }
    }

    public void restockItemsMenu() {
        while (true) {
            System.out.println("...................................................................................");
            System.out.println("Restock Management System");
            System.out.println("...................................................................................");
            System.out.println("1. Move Items to Shelf");
            System.out.println("2. View Current Shelf Status");
            System.out.println("3. View Low Stock Alerts");
            System.out.println("4. Back to Dashboard");
            System.out.print("Please select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1-4.");
                continue;
            }

            switch (choice) {
                case 1:
                    moveItemsToShelfWithFIFO();
                    break;
                case 2:
                    displayCurrentShelfStatus();
                    break;
                case 3:
                    displayLowStockAlerts();
                    break;
                case 4:
                    new ManagerDashboard().viewDashboard();
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1-4.");
            }
        }
    }

    private void displayLowStockAlerts() {
        System.out.println("...................................................................................");
        RestockManager.displayLowStockAlerts();
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private void moveItemsToShelfWithFIFO() {
        System.out.println("...................................................................................");
        System.out.println("Move Items from Stock to Shelf");
        System.out.println("...................................................................................");

        // Get available item codes
        List<String> availableItemCodes = getAvailableItemCodes();

        if (availableItemCodes.isEmpty()) {
            System.out.println("No items available in stock to move to shelf.");
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return;
        }

        // Display available items summary
        System.out.println("Available Items (Total quantities):");
        displayItemSummary(availableItemCodes);
        System.out.println("...................................................................................");

        String itemCode;
        do {
            System.out.print("Enter Item Code to move to shelf: ");
            itemCode = scanner.nextLine().toUpperCase().trim();
            if (itemCode.isEmpty()) {
                System.out.println("Item code cannot be empty.");
            } else if (!availableItemCodes.contains(itemCode)) {
                System.out.println("Item with code '" + itemCode + "' not found in stock.");
                itemCode = "";
            }
        } while (itemCode.isEmpty());

        // Get all batches for this item
        List<StockBatch> itemBatches = getStockBatchesForItem(itemCode);

        if (itemBatches.isEmpty()) {
            System.out.println("No batches found for item code: " + itemCode);
            return;
        }

        // Display batch details
        displayBatchDetails(itemBatches);

        // Calculate total available quantity
        int totalAvailable = itemBatches.stream().mapToInt(batch -> batch.quantity).sum();

        // Get quantity to move
        int quantityToMove = getQuantityToMove(totalAvailable);

        if (quantityToMove <= 0) {
            return;
        }

        // Determine optimal batch selection using FIFO with expiry logic
        List<BatchSelection> batchSelections = selectBatchesForShelving(itemBatches, quantityToMove);

        // Display selection summary
        displayBatchSelectionSummary(batchSelections, itemBatches.getFirst().itemName);

        // Confirm the move
        if (confirmMove()) {
            performBatchedMoveToShelf(batchSelections, itemBatches.getFirst().itemName);
        } else {
            System.out.println("Move operation cancelled.");
        }

        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    private List<String> getAvailableItemCodes() {
        List<String> itemCodes = new ArrayList<>();
        String query = "SELECT DISTINCT item_code FROM stock WHERE quantity > 0 ORDER BY item_code";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                itemCodes.add(resultSet.getString("item_code"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available item codes: " + e.getMessage());
        }

        return itemCodes;
    }

    private void displayItemSummary(List<String> itemCodes) {
        for (String itemCode : itemCodes) {
            String query = "SELECT item_name, SUM(quantity) as total_qty FROM stock " +
                    "WHERE item_code = ? GROUP BY item_code, item_name";

            try (Connection connection = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, itemCode);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    System.out.printf("%-10s %-20s (Total: %d units)\n",
                            itemCode,
                            resultSet.getString("item_name"),
                            resultSet.getInt("total_qty"));
                }

            } catch (SQLException e) {
                System.err.println("Error fetching item summary: " + e.getMessage());
            }
        }
    }

    private List<StockBatch> getStockBatchesForItem(String itemCode) {
        List<StockBatch> batches = new ArrayList<>();
        String query = "SELECT id, item_code, item_name, quantity, price, purchaseDate, expirationDate " +
                "FROM stock WHERE item_code = ? AND quantity > 0 ORDER BY purchaseDate ASC";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, itemCode);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                StockBatch batch = new StockBatch(
                        resultSet.getString("item_code"),
                        resultSet.getString("item_name"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price"),
                        resultSet.getDate("purchaseDate").toLocalDate(),
                        resultSet.getDate("expirationDate") != null ?
                                resultSet.getDate("expirationDate").toLocalDate() : null,
                        resultSet.getInt("id")
                );
                batches.add(batch);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching stock batches for item: " + e.getMessage());
        }

        return batches;
    }

    private void displayBatchDetails(List<StockBatch> batches) {
        System.out.println("Available Batches for " + batches.getFirst().itemName + ":");
        System.out.printf("%-8s %-12s %-12s %-8s %-15s\n",
                "Batch", "Purchase", "Expiry", "Qty", "FIFO Priority");
        System.out.println("...................................................................................");

        for (int i = 0; i < batches.size(); i++) {
            StockBatch batch = batches.get(i);
            String priority = determinePriority(batches, i);

            System.out.printf("%-8d %-12s %-12s %-8d %-15s\n",
                    i + 1,
                    batch.purchaseDate.toString(),
                    batch.expirationDate != null ? batch.expirationDate.toString() : "N/A",
                    batch.quantity,
                    priority);
        }
        System.out.println("...................................................................................");
    }

    private String determinePriority(List<StockBatch> batches, int currentIndex) {
        StockBatch currentBatch = batches.get(currentIndex);

        // If it's the oldest batch (first in list)
        if (currentIndex == 0) {
            return "OLDEST";
        }

        // Check if this batch has closer expiry than older batches
        if (currentBatch.expirationDate != null) {
            for (int i = 0; i < currentIndex; i++) {
                StockBatch olderBatch = batches.get(i);
                if (olderBatch.expirationDate == null ||
                        currentBatch.expirationDate.isBefore(olderBatch.expirationDate)) {
                    return "EXPIRY PRIORITY";
                }
            }
        }

        return "NORMAL";
    }

    private int getQuantityToMove(int totalAvailable) {
        int quantityToMove;
        while (true) {
            System.out.print("Enter quantity to move to shelf (max " + totalAvailable + "): ");
            try {
                quantityToMove = Integer.parseInt(scanner.nextLine());
                if (quantityToMove <= 0) {
                    System.out.println("Quantity must be a positive number.");
                } else if (quantityToMove > totalAvailable) {
                    System.out.println("Cannot move more than available quantity (" + totalAvailable + ").");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return quantityToMove;
    }

    // Inner class to represent batch selection
    private static class BatchSelection {
        int stockId;
        int quantityToTake;
        LocalDate purchaseDate;
        LocalDate expirationDate;

        public BatchSelection(int stockId, int quantityToTake, LocalDate purchaseDate, LocalDate expirationDate) {
            this.stockId = stockId;
            this.quantityToTake = quantityToTake;
            this.purchaseDate = purchaseDate;
            this.expirationDate = expirationDate;
        }
    }

    /**
     * Implements the business logic: FIFO with expiry date exception
     * "Stock should be reduced from the oldest batch of items and put on the shelf.
     * However, when the expiry date of another set is closer than the one in the oldest batch of items,
     * the newer batch is chosen to stock the SYOS shelves."
     */
    private List<BatchSelection> selectBatchesForShelving(List<StockBatch> batches, int totalQuantityNeeded) {
        List<BatchSelection> selections = new ArrayList<>();
        int remainingQuantity = totalQuantityNeeded;

        // Create a working copy of batches that we can modify
        List<StockBatch> workingBatches = new ArrayList<>(batches);

        while (remainingQuantity > 0 && !workingBatches.isEmpty()) {
            // Find the batch to use based on FIFO with expiry logic
            StockBatch selectedBatch = selectNextBatch(workingBatches);

            // Determine how much to take from this batch
            int quantityToTake = Math.min(remainingQuantity, selectedBatch.quantity);

            // Add to selections
            selections.add(new BatchSelection(
                    selectedBatch.stockId,
                    quantityToTake,
                    selectedBatch.purchaseDate,
                    selectedBatch.expirationDate
            ));

            // Update remaining quantity
            remainingQuantity -= quantityToTake;

            // Update or remove the batch from working list
            selectedBatch.quantity -= quantityToTake;
            if (selectedBatch.quantity == 0) {
                workingBatches.remove(selectedBatch);
            }
        }

        return selections;
    }

    /**
     * Selects the next batch based on FIFO with expiry date exception logic
     */
    private StockBatch selectNextBatch(List<StockBatch> availableBatches) {
        if (availableBatches.isEmpty()) {
            return null;
        }

        // Start with the oldest batch (FIFO)
        StockBatch oldestBatch = availableBatches.getFirst();

        // If oldest batch has no expiry date, use it
        if (oldestBatch.expirationDate == null) {
            return oldestBatch;
        }

        // Check if any newer batch has a closer expiry date
        StockBatch batchWithClosestExpiry = oldestBatch;

        for (int i = 1; i < availableBatches.size(); i++) {
            StockBatch currentBatch = availableBatches.get(i);

            // Skip batches without expiry dates when comparing
            if (currentBatch.expirationDate == null) {
                continue;
            }

            // If this batch expires sooner than our current selection, choose it
            if (currentBatch.expirationDate.isBefore(batchWithClosestExpiry.expirationDate)) {
                batchWithClosestExpiry = currentBatch;
            }
        }

        return batchWithClosestExpiry;
    }

    private void displayBatchSelectionSummary(List<BatchSelection> selections, String itemName) {
        System.out.println("...................................................................................");
        System.out.println("Batch Selection Summary for: " + itemName);
        System.out.println("...................................................................................");
        System.out.printf("%-12s %-12s %-8s %-15s\n",
                "Purchase", "Expiry", "Qty", "Selection Reason");
        System.out.println("...................................................................................");

        int totalQuantity = 0;
        for (int i = 0; i < selections.size(); i++) {
            BatchSelection selection = selections.get(i);
            String reason = i == 0 ? "FIFO/Expiry Logic" : "Continuation";

            System.out.printf("%-12s %-12s %-8d %-15s\n",
                    selection.purchaseDate.toString(),
                    selection.expirationDate != null ? selection.expirationDate.toString() : "N/A",
                    selection.quantityToTake,
                    reason);

            totalQuantity += selection.quantityToTake;
        }

        System.out.println("...................................................................................");
        System.out.println("Total quantity to be moved: " + totalQuantity);
        System.out.println("...................................................................................");
    }

    private boolean confirmMove() {
        while (true) {
            System.out.print("Confirm move to shelf using FIFO logic? (Y/N): ");
            String confirmation = scanner.nextLine().toUpperCase().trim();
            if (confirmation.equals("Y")) {
                return true;
            } else if (confirmation.equals("N")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N'.");
            }
        }
    }

    private void performBatchedMoveToShelf(List<BatchSelection> selections, String itemName) {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start transaction

            String itemCode = null;
            int totalQuantityMoved = 0;

            // Process each batch selection
            for (BatchSelection selection : selections) {
                // Get item code from first selection
                if (itemCode == null) {
                    itemCode = getItemCodeFromStockId(selection.stockId, connection);
                }

                // Update stock quantity (set to 0 instead of deleting)
                updateStockBatch(selection.stockId, selection.quantityToTake, connection);
                totalQuantityMoved += selection.quantityToTake;
            }

            // Update shelf quantity
            updateShelfStock(itemCode, itemName, totalQuantityMoved, connection);

            connection.commit(); // Commit transaction
            System.out.println("Successfully moved " + totalQuantityMoved + " units of " + itemName + " to shelf using FIFO logic.");

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

    private String getItemCodeFromStockId(int stockId, Connection connection) throws SQLException {
        String query = "SELECT item_code FROM stock WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, stockId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("item_code");
            }
        }
        throw new SQLException("Item code not found for stock ID: " + stockId);
    }

    private void updateStockBatch(int stockId, int quantityToRemove, Connection connection) throws SQLException {
        // First, get current quantity
        String selectQuery = "SELECT quantity FROM stock WHERE id = ?";
        int currentQuantity;

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, stockId);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                currentQuantity = resultSet.getInt("quantity");
            } else {
                throw new SQLException("Stock batch not found with ID: " + stockId);
            }
        }

        int newQuantity = currentQuantity - quantityToRemove;

        // Always update the quantity, even if it becomes 0
        // Don't delete the entry, just set quantity to 0
        if (newQuantity < 0) {
            newQuantity = 0;
        }

        String updateQuery = "UPDATE stock SET quantity = ? WHERE id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, newQuantity);
            updateStatement.setInt(2, stockId);
            updateStatement.executeUpdate();
        }
    }

    private void updateShelfStock(String itemCode, String itemName, int quantityToAdd, Connection connection) throws SQLException {
        // Check if item already exists on shelf
        String checkQuery = "SELECT quantity FROM shelf WHERE item_code = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, itemCode);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // Item exists, update quantity
                int currentShelfQuantity = resultSet.getInt("quantity");
                String updateQuery = "UPDATE shelf SET quantity = ? WHERE item_code = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, currentShelfQuantity + quantityToAdd);
                    updateStatement.setString(2, itemCode);
                    updateStatement.executeUpdate();
                }
            } else {
                // Item doesn't exist, insert new record
                String insertQuery = "INSERT INTO shelf (item_code, item_name, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    insertStatement.setString(1, itemCode);
                    insertStatement.setString(2, itemName);
                    insertStatement.setInt(3, quantityToAdd);
                    insertStatement.executeUpdate();
                }
            }
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