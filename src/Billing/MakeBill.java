package Billing;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.CashierDashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MakeBill extends BillingProcessTemplate {

    // New method to handle continuous billing
    public void startContinuousBilling() {
        Scanner sc = ScannerInstance.getScanner();
        boolean continueBilling = true;

        System.out.println("=== SYOS RETAIL BILLING SYSTEM ===");
        System.out.println("Welcome to the Billing Module");
        System.out.println("=".repeat(40));

        while (continueBilling) {
            try {
                // Process a single bill
                processBilling();

                // Ask if cashier wants to create another bill
                System.out.println("\n" + "=".repeat(50));
                System.out.print("Create another bill? (y/n) or type 'exit' to return to dashboard: ");
                String response = sc.nextLine().trim().toLowerCase();

                if (response.equals("n") || response.equals("no") || response.equals("exit")) {
                    continueBilling = false;
                    CashierDashboard realDashboard = new CashierDashboard();
                    realDashboard.viewDashboard();
                    System.out.println("=".repeat(50));
                } else if (!response.equals("y") && !response.equals("yes")) {
                    System.out.println("Invalid input. Please enter 'y', 'n', or 'exit'.");
                }

            } catch (Exception e) {
                System.err.println("An error occurred while processing the bill: " + e.getMessage());
                System.out.print("Would you like to try again? (y/n): ");
                String retry = sc.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    continueBilling = false;
                }
            }
        }
    }

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
            String query = "SELECT item_name, selling_price, discount_price, quantity FROM shelf WHERE item_code = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemCode);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String itemName = rs.getString("item_name");
                        double sellingPrice = rs.getDouble("selling_price");
                        Double discountPrice = rs.getObject("discount_price") != null ?
                                rs.getDouble("discount_price") : null;
                        int availableQuantity = rs.getInt("quantity");

                        if (availableQuantity <= 0) {
                            System.out.println("Item '" + itemName + "' is out of stock!");
                            return null;
                        }

                        System.out.println("Item Found:");
                        System.out.println("Item Code: " + itemCode);
                        System.out.println("Item Name: " + itemName);
                        System.out.println("Available Quantity: " + availableQuantity);
                        System.out.println("Price: LKR " + String.format("%.2f", sellingPrice));
                        if (discountPrice != null) {
                            System.out.println("Discount Price: LKR " + String.format("%.2f", discountPrice));
                        }

                        return new ItemInfo(itemCode, itemName, sellingPrice, discountPrice);
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

    // New method to validate quantity against available stock
    private boolean validateQuantityAvailability(String itemCode, int requestedQuantity) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT quantity FROM shelf WHERE item_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int availableQuantity = rs.getInt("quantity");
                        if (requestedQuantity > availableQuantity) {
                            System.out.println("Insufficient stock! Available: " + availableQuantity +
                                    ", Requested: " + requestedQuantity);
                            return false;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock availability: " + e.getMessage());
            return false;
        }
        return false;
    }

    @Override
    protected boolean addMoreItems() {
        Scanner sc = ScannerInstance.getScanner();
        System.out.print("Add more items to this bill? (y/n): ");
        String response = sc.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    @Override
    protected List<BillItem> collectItems() {
        List<BillItem> billItems = new ArrayList<>();
        boolean continueAdding = true;

        System.out.println("\n=== NEW BILL ===");

        while (continueAdding) {
            String itemCode = validateItemCode();
            ItemInfo itemInfo = queryItemInfo(itemCode);

            if (itemInfo != null) {
                int quantity = getQuantity();

                // Validate quantity against available stock
                if (!validateQuantityAvailability(itemCode, quantity)) {
                    Scanner sc = ScannerInstance.getScanner();
                    System.out.print("Try with a different quantity or item? (y/n): ");
                    String response = sc.nextLine().trim().toLowerCase();
                    continueAdding = response.equals("y") || response.equals("yes");
                    continue;
                }

                BillItem billItem = new BillItem(itemInfo, quantity);
                billItems.add(billItem);

                // Show individual item total
                if (itemInfo.getDiscountPrice() != null && itemInfo.getDiscountPrice() > 0) {
                    DiscountDecorator discountedItem = new DiscountDecorator(billItem, itemInfo.getDiscountPrice());
                    System.out.println("Item Total (Discounted): LKR " + String.format("%.2f", discountedItem.getPrice()));
                    System.out.println("You saved: LKR " + String.format("%.2f",
                            (itemInfo.getPrice() * quantity) - discountedItem.getPrice()));
                } else {
                    System.out.println("Item Total: LKR " + String.format("%.2f", billItem.getTotal()));
                }
                System.out.println("-".repeat(30));

                continueAdding = addMoreItems();
            } else {
                Scanner sc = ScannerInstance.getScanner();
                System.out.print("Try with another item code? (y/n): ");
                String response = sc.nextLine().trim().toLowerCase();
                continueAdding = response.equals("y") || response.equals("yes");
            }
        }

        return billItems;
    }

    // New method to update shelf quantities after successful checkout
    private boolean updateShelfQuantities(List<BillItem> billItems) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            String updateQuery = "UPDATE shelf SET quantity = quantity - ? WHERE item_code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                for (BillItem item : billItems) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setString(2, item.getItemInfo().getItemCode());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("No rows updated for item: " + item.getItemInfo().getItemCode());
                    }
                }
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating shelf quantities: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    @Override
    protected void generateFinalBill(List<BillItem> billItems) {
        if (billItems.isEmpty()) {
            System.out.println("No items to bill.");
            return;
        }

        // Generate bill serial number and date
        int billSerialNo = BillManager.getInstance().getNextBillSerialNumber();
        String billDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String displayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        System.out.println("\n" + "=".repeat(70));
        System.out.println("                     SYOS RETAIL SYSTEM");
        System.out.println("                        CASH RECEIPT");
        System.out.println("=".repeat(70));
        System.out.printf("Bill No: %06d                          Date: %s%n", billSerialNo, displayDate);
        System.out.println("=".repeat(70));
        System.out.printf("%-20s %-6s %-10s %-10s %-12s%n",
                "Item Name", "Qty", "Unit Price", "Discount", "Total");
        System.out.println("-".repeat(70));

        double grandTotal = 0.0;
        double totalSavings = 0.0;

        for (BillItem item : billItems) {
            ItemInfo info = item.getItemInfo();
            double originalPrice = info.getPrice();
            Double discountPrice = info.getDiscountPrice();

            double itemTotal;
            double discountAmount = 0.0;

            // Only apply discount if discountPrice is not null AND greater than 0
            if (discountPrice != null && discountPrice > 0) {
                itemTotal = discountPrice * item.getQuantity();
                discountAmount = (originalPrice - discountPrice) * item.getQuantity();
                totalSavings += discountAmount;
            } else {
                itemTotal = item.getTotal();
            }

            grandTotal += itemTotal;

            System.out.printf("%-20s %-6d LKR%-7.2f LKR%-7.2f LKR%-8.2f%n",
                    truncateName(info.getItemName(), 20),
                    item.getQuantity(),
                    originalPrice,
                    discountAmount,
                    itemTotal);
        }

        System.out.println("-".repeat(70));
        System.out.printf("%-50s LKR%-8.2f%n", "SUBTOTAL:", grandTotal);
        if (totalSavings > 0) {
            System.out.printf("%-50s LKR%-8.2f%n", "TOTAL DISCOUNT:", totalSavings);
        }
        System.out.printf("%-50s LKR%-8.2f%n", "TOTAL AMOUNT:", grandTotal);
        System.out.println("=".repeat(70));

        // Handle cash payment
        double cashTendered = handleCashPayment(grandTotal);
        double changeAmount = cashTendered - grandTotal;

        System.out.printf("%-50s LKR%-8.2f%n", "CASH TENDERED:", cashTendered);
        System.out.printf("%-50s LKR%-8.2f%n", "CHANGE:", changeAmount);
        System.out.println("=".repeat(70));
        System.out.println("           ** SYOS ONLY ACCEPTS CASH PAYMENTS **");
        System.out.println("                 Thank you for your purchase!");
        if (totalSavings > 0) {
            System.out.printf("           You saved LKR %.2f today!%n", totalSavings);
        }
        System.out.println("=".repeat(70));

        // Save bill to database and update shelf quantities
        boolean billSaved = BillManager.getInstance().saveBill(billSerialNo, billDate, billItems,
                grandTotal, cashTendered, changeAmount);

        if (billSaved) {
            System.out.println("✓ Bill saved successfully with Serial No: " + String.format("%06d", billSerialNo));

            // Update shelf quantities after successful bill save
            boolean shelfUpdated = updateShelfQuantities(billItems);
            if (shelfUpdated) {
                System.out.println("✓ Shelf inventory updated successfully");
            } else {
                System.out.println("⚠ Warning: Bill saved but shelf inventory could not be updated!");
                System.out.println("  Please manually update shelf quantities for the following items:");
                for (BillItem item : billItems) {
                    System.out.println("  - " + item.getItemInfo().getItemCode() +
                            " (" + item.getItemInfo().getItemName() + "): -" + item.getQuantity());
                }
            }
        } else {
            System.out.println("⚠ Warning: Bill could not be saved to database.");
            System.out.println("  Shelf quantities were not updated.");
        }
    }

    private double handleCashPayment(double totalAmount) {
        Scanner sc = ScannerInstance.getScanner();
        double cashTendered = 0.0;
        boolean validPayment = false;

        while (!validPayment) {
            try {
                System.out.printf("%nTotal Amount: LKR %.2f%n", totalAmount);
                System.out.print("Enter cash tendered: LKR ");
                cashTendered = Double.parseDouble(sc.nextLine().trim());

                if (cashTendered >= totalAmount) {
                    validPayment = true;
                } else {
                    System.out.printf("Insufficient amount. You need at least LKR %.2f more.%n",
                            totalAmount - cashTendered);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }

        return cashTendered;
    }

    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}