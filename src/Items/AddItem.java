package Items;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.ManagerDashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class AddItem extends ItemTemplate{

    @Override
    protected void validateItemDetails(String itemCode, String itemName, int quantity, double price) {
        System.out.println("...................................................................................");
        System.out.println("Item Code: " + itemCode);
        System.out.println("Item Name: " + itemName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Price: LKR" + price);
        System.out.println("...................................................................................");
    }

    @Override
    protected LocalDate addValidatedPurchaseDate(String itemCode, String itemName, int quantity, double price) {
        Scanner scanner = ScannerInstance.getScanner();
        LocalDate purchaseDate = null;

        System.out.println("1) Today's Date");
        System.out.println("2) Custom Date");
        System.out.print("What's the purchasing date of the item?: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear the buffer

        if (choice == 1) {
            // Get the current date
            purchaseDate = LocalDate.now();
            System.out.println("Purchase Date: " + purchaseDate);
        } else if (choice == 2) {
            while (true) {
                System.out.print("Enter the custom date (YYYY-MM-DD): ");
                String customDateInput = scanner.nextLine();
                if (customDateInput.isEmpty()) {
                    System.out.println("Date cannot be empty. Please provide a valid date.");
                    continue;
                }

                try {
                    purchaseDate = LocalDate.parse(customDateInput);
                    System.out.println("Purchase Date: " + purchaseDate);
                    break; // Exit the loop if date is valid
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                }
            }
        } else {
            System.out.println("Invalid choice. Please try again. Choose between 1 and 2.");
        }
        return purchaseDate;
    }

    @Override
    protected LocalDate addExpirationDate(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate) {
        Scanner scanner = ScannerInstance.getScanner();
        LocalDate expirationDate = null;

        int choice = 0;
        while (true) {
            System.out.println("1) No Expiration Date");
            System.out.println("2) Custom Expiration Date");
            System.out.print("Does the item have an expiration date? (1/2): ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer
                if (choice == 1 || choice == 2) {
                    break; // Valid choice, exit the loop
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number (1 or 2).");
                scanner.nextLine(); // Clear invalid input
            }
        }

        if (choice == 1) {
            System.out.println("No expiration date set for this item.");
            // No expiration date
        } else if (choice == 2) {
            while (true) {
                System.out.print("Enter the custom expiration date (YYYY-MM-DD): ");
                String customDateInput = scanner.nextLine();
                if (customDateInput.isEmpty()) {
                    System.out.println("Expiration date cannot be empty. Please provide a valid date.");
                    continue;
                }

                try {
                    expirationDate = LocalDate.parse(customDateInput);

                    // Check if expiration date is before the purchase date
                    if (expirationDate.isBefore(purchaseDate)) {
                        System.out.println("Expiration date cannot be before the purchase date (" + purchaseDate + "). Please enter a valid date.");
                        continue; // Prompt the user again
                    }

                    System.out.println("Expiration Date: " + expirationDate);
                    break; // Exit the loop if date is valid
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                }
            }
        }

        return expirationDate;
    }

    @Override
    protected int addRestockLevel(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate) {
        Scanner scanner = ScannerInstance.getScanner();
        int restockLevel;

        while (true) {
            System.out.print("Enter the restock level for the item (must be a positive integer): ");
            try {
                restockLevel = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer

                if (restockLevel > 0) {
                    System.out.println("Restock Level for " + itemName + " (Item Code: " + itemCode + ") is set to: " + restockLevel);
                    break; // Exit loop if input is valid
                } else {
                    System.out.println("Restock level must be a positive integer. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a positive integer.");
                scanner.nextLine(); // Clear invalid input
            }
        }
        return restockLevel;
    }

    @Override
    protected void ValidateInforAndConfirm(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel) {
        System.out.println("...................................................................................");
        System.out.println("Item Code: " + itemCode);
        System.out.println("Item Name: " + itemName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Price: LKR" + price);
        System.out.println("Purchase Date: " + (purchaseDate != null ? purchaseDate : "Not Provided"));
        System.out.println("Expiration Date: " + (expirationDate != null ? expirationDate : "No expiration date"));
        System.out.println("Restock Level: " + restockLevel);
        System.out.println("...................................................................................");

        Scanner scanner = ScannerInstance.getScanner();
        String confirmation;

        while (true) {
            System.out.print("Do you want to add this Item to the stock? (Y=Yes/N=No): ");
            confirmation = scanner.nextLine().trim().toUpperCase();

            if (confirmation.equals("Y")) {
                System.out.println("Item " + itemName + " (Code: " + itemCode + ") has been successfully added to the stock.");
                break;
            } else if (confirmation.equals("N")) {
                System.out.println("Item addition cancelled.");
                ManagerDashboard realDashboard = new ManagerDashboard();
                realDashboard.viewDashboard();
            } else {
                System.out.println("Invalid input. Please enter 'Y' for Yes or 'N' for No.");
            }
        }
    }

    @Override
    protected void saveItemToDatabase(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel) {
        String insertQuery = "INSERT INTO stock (item_code, item_name, quantity, price, purchaseDate, expirationDate, restockLevel) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, itemCode);
            preparedStatement.setString(2, itemName);
            preparedStatement.setInt(3, quantity);
            preparedStatement.setDouble(4, price);
            preparedStatement.setObject(5, purchaseDate);
            preparedStatement.setObject(6, expirationDate);
            preparedStatement.setInt(7, restockLevel);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Item successfully added to the database.");
            } else {
                System.out.println("Failed to add item to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error while adding item to the database: " + e.getMessage());
        }
    }

}
