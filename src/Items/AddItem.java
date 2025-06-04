package Items;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.ManagerDashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class AddItem extends ItemTemplate {

    private Scanner scanner = ScannerInstance.getScanner();

    @Override
    protected String validateInputs() {

        String itemCode;
        String itemName = null;

        do {
            System.out.print("Enter Item Code: ");
            itemCode = scanner.nextLine().toUpperCase();
            if (itemCode.isEmpty()) {
                System.out.println("Item code cannot be empty.");
            } else {
                itemName = fetchItemNameByCode(itemCode);
                if (itemName != null) {
                    System.out.println("Item Code exists. Auto-filling item name: " + itemName);
                    break;
                }
            }
        } while (itemCode.isEmpty());

        if (itemName == null) {
            do {
                System.out.print("Enter Item Name: ");
                itemName = scanner.nextLine();
                if (itemName.isEmpty()) {
                    System.out.println("Item name cannot be empty.");
                }
            } while (itemName.isEmpty());
        }

        return itemCode;  // Return the updated item code
    }

    private String fetchItemNameByCode(String itemCode) {
        String query = "SELECT item_name FROM stock WHERE item_code = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, itemCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("item_name");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching item name by code: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected String fetchItemName(String itemCode) {
        return fetchItemNameByCode(itemCode); // Fetch item name by code
    }

    @Override
    protected LocalDate addValidatedPurchaseDate(String itemCode, String itemName, int quantity, double price) {
        // Update quantity
        while (true) {
            System.out.print("Enter Quantity: ");
            try {
                quantity = Integer.parseInt(scanner.nextLine());
                if (quantity > 0) {
                    break;
                } else {
                    System.out.println("Quantity must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid quantity.");
            }
        }

        // Update price
        while (true) {
            System.out.print("Enter Price: ");
            try {
                price = Double.parseDouble(scanner.nextLine());
                if (price > 0) {
                    break;
                } else {
                    System.out.println("Price must be a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid price.");
            }
        }

        // Ask for purchase date
        LocalDate purchaseDate;
        while (true) {
            System.out.print("Enter Purchase Date (YYYY-MM-DD or leave blank for today's date): ");
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                purchaseDate = LocalDate.now();
                break;
            }
            try {
                purchaseDate = LocalDate.parse(input);
                break;
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }
        return purchaseDate;  // Return purchase date and updated quantity and price
    }

    @Override
    protected LocalDate addExpirationDate(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate) {
        // Expiration date logic
        LocalDate expirationDate = null;
        while (true) {
            System.out.print("Enter Expiration Date (YYYY-MM-DD) or 'none' if not applicable: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("none")) {
                break;
            }
            try {
                expirationDate = LocalDate.parse(input);
                if (!expirationDate.isBefore(purchaseDate)) {
                    break;
                } else {
                    System.out.println("Expiration date cannot be before purchase date.");
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }
        return expirationDate;  // Return expiration date
    }

    @Override
    protected int addRestockLevel(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate) {
        // Restock level logic
        int restockLevel;
        while (true) {
            System.out.print("Enter Restock Level (positive integer): ");
            try {
                restockLevel = Integer.parseInt(scanner.nextLine());
                if (restockLevel > 0) {
                    break;
                } else {
                    System.out.println("Restock level must be a positive integer.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a positive integer.");
            }
        }
        return restockLevel;  // Return restock level
    }

    @Override
    protected void ValidateInforAndConfirm(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel) {
        // Display item details and confirm
        System.out.printf(""" 
                Item Details:
                - Code: %s
                - Name: %s
                - Quantity: %d
                - Price: %.2f
                - Purchase Date: %s
                - Expiration Date: %s
                - Restock Level: %d
                """, itemCode, itemName, quantity, price, purchaseDate,
                (expirationDate != null ? expirationDate : "N/A"), restockLevel);

        while (true) {
            System.out.print("Confirm addition to stock? (Y/N): ");
            String input = scanner.nextLine().toUpperCase();
            if (input.equals("Y")) {
                break;
            } else if (input.equals("N")) {
                System.out.println("Item addition canceled.");
                new ManagerDashboard().viewDashboard();
                return;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N'.");
            }
        }
    }

    @Override
    protected void saveItemToDatabase(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel) {
        // Save item to database
        String query = "INSERT INTO stock (item_code, item_name, quantity, price, purchaseDate, expirationDate, restockLevel) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, itemCode);
            preparedStatement.setString(2, itemName);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setDouble(4, price);
            preparedStatement.setObject(5, purchaseDate);
            preparedStatement.setObject(6, expirationDate);
            preparedStatement.setInt(7, restockLevel);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Item successfully added to stock.");
            } else {
                System.out.println("Failed to add item to stock.");
            }
        } catch (SQLException e) {
            System.err.println("Error saving item to database: " + e.getMessage());
        }
    }

    @Override
    protected void Continuation() {
        while (true) {
            System.out.print("Add another item? (Y/N): ");
            String input = scanner.nextLine().toUpperCase();
            if (input.equals("Y")) {
                addItemToStock();
                break;
            } else if (input.equals("N")) {
                System.out.println("Returning to Manager Dashboard.");
                new ManagerDashboard().viewDashboard();
                break;
            } else {
                System.out.println("Invalid input. Please enter 'Y' or 'N'.");
            }
        }
    }
}
