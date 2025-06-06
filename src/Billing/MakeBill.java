package Billing;

import Common.DatabaseConnection;
import Common.ScannerInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MakeBill extends BillingProcessTemplate {

    @Override
    protected String validateItemCode() {
        Scanner sc = ScannerInstance.getScanner();
        System.out.print("Enter Item Code: ");
        String itemCode = sc.nextLine().trim();
        return itemCode;
    }

    @Override
    protected ItemInfo queryItemInfo(String itemCode) {
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT item_name, selling_price FROM shelf WHERE item_code = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemCode);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String itemName = rs.getString("item_name");
                        double sellingPrice = rs.getDouble("selling_price");

                        System.out.println("Item Found:");
                        System.out.println("Item Code: " + itemCode);
                        System.out.println("Item Name: " + itemName);
                        System.out.println("Price: LKR " + String.format("%.2f", sellingPrice));

                        return new ItemInfo(itemCode, itemName, sellingPrice);
                    } else {
                        System.out.println("Item not found for code: " + itemCode);
                        return null;
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected int getQuantity() {
        Scanner sc = ScannerInstance.getScanner();
        int quantity = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("Enter Quantity: ");
                quantity = Integer.parseInt(sc.nextLine().trim());
                if (quantity > 0) {
                    validInput = true;
                } else {
                    System.out.println("Quantity must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity. Please enter a valid number.");
            }
        }

        return quantity;
    }

    @Override
    protected boolean addMoreItems() {
        Scanner sc = ScannerInstance.getScanner();
        System.out.print("Add more items? (y/n): ");
        String response = sc.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    @Override
    protected List<BillItem> collectItems() {
        List<BillItem> billItems = new ArrayList<>();
        boolean continueAdding = true;

        System.out.println("=== BILLING SYSTEM ===");

        while (continueAdding) {
            // Get item code
            String itemCode = validateItemCode();

            // Query item information
            ItemInfo itemInfo = queryItemInfo(itemCode);

            if (itemInfo != null) {
                // Get quantity
                int quantity = getQuantity();

                // Create bill item
                BillItem billItem = new BillItem(itemInfo, quantity);
                billItems.add(billItem);

                // Show item total
                System.out.println("Item Total: LKR " + String.format("%.2f", billItem.getTotal()));
                System.out.println("-".repeat(30));

                // Ask if user wants to add more items
                continueAdding = addMoreItems();
            } else {
                // Item not found, ask if they want to try again or continue
                Scanner sc = ScannerInstance.getScanner();
                System.out.print("Try with another item code? (y/n): ");
                String response = sc.nextLine().trim().toLowerCase();
                continueAdding = response.equals("y") || response.equals("yes");
            }
        }

        return billItems;
    }

    @Override
    protected void generateFinalBill(List<BillItem> billItems) {
        if (billItems.isEmpty()) {
            System.out.println("No items to bill.");
            return;
        }

        // Display final bill
        System.out.println("\n" + "=".repeat(50));
        System.out.println("                 FINAL BILL");
        System.out.println("=".repeat(50));
        System.out.printf("%-10s %-20s %-8s %-10s %-10s%n",
                "Code", "Item Name", "Price", "Qty", "Total");
        System.out.println("-".repeat(50));

        double grandTotal = 0.0;

        for (BillItem item : billItems) {
            ItemInfo info = item.getItemInfo();
            System.out.printf("%-10s %-20s LKR%-6.2f %-10d LKR%-8.2f%n",
                    info.getItemCode(),
                    info.getItemName(),
                    info.getPrice(),
                    item.getQuantity(),
                    item.getTotal());
            grandTotal += item.getTotal();
        }

        System.out.println("-".repeat(50));
        System.out.printf("%-40s LKR%-8.2f%n", "GRAND TOTAL:", grandTotal);
        System.out.println("=".repeat(50));
        System.out.println("Thank you for your purchase!");
    }
}