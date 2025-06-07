package Billing;

import Common.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillManager {
    private static BillManager instance;

    private BillManager() {}

    public static BillManager getInstance() {
        if (instance == null) {
            instance = new BillManager();
        }
        return instance;
    }

    public int getNextBillSerialNumber() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT COALESCE(MAX(bill_serial_no), 0) + 1 AS next_serial FROM bills";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_serial");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting next serial number: " + e.getMessage());
        }
        return 1; // Default to 1 if there's an error
    }

    public boolean saveBill(int serialNo, String billDate, List<BillItem> billItems,
                            double totalAmount, double cashTendered, double changeAmount) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Insert bill header
            String billQuery = "INSERT INTO bills (bill_serial_no, bill_date, total_amount, cash_tendered, change_amount) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement billStmt = conn.prepareStatement(billQuery)) {
                billStmt.setInt(1, serialNo);
                billStmt.setString(2, billDate);
                billStmt.setDouble(3, totalAmount);
                billStmt.setDouble(4, cashTendered);
                billStmt.setDouble(5, changeAmount);
                billStmt.executeUpdate();
            }

            // Insert bill items
            String itemQuery = "INSERT INTO bill_items (bill_serial_no, item_code, item_name, quantity, unit_price, discount_price, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                for (BillItem item : billItems) {
                    ItemInfo info = item.getItemInfo();
                    itemStmt.setInt(1, serialNo);
                    itemStmt.setString(2, info.getItemCode());
                    itemStmt.setString(3, info.getItemName());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setDouble(5, info.getPrice());
                    itemStmt.setObject(6, info.getDiscountPrice());

                    // Calculate actual total (with discount if applicable)
                    double actualTotal = item.getTotal();
                    if (info.getDiscountPrice() != null && info.getDiscountPrice() > 0) {
                        actualTotal = info.getDiscountPrice() * item.getQuantity();
                    }
                    itemStmt.setDouble(7, actualTotal);
                    itemStmt.executeUpdate();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving bill: " + e.getMessage());
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            return false;
        }
    }
}