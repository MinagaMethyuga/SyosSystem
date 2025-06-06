package Billing;

import Common.DatabaseConnection;
import Common.ScannerInstance;

import java.util.Scanner;

public class MakeBill extends BillingProcessTemplate {

    @Override
    protected String validateItemCode() {
        // Get the scanner instance from ScannerInstance
        Scanner sc = ScannerInstance.getScanner();
        System.out.print("Enter Item Code: ");
        String itemCode = sc.nextLine().trim();

        return itemCode;
    }

    @Override
    protected void QueryItemInfo(String itemCode) {
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT item_name, selling_price FROM shelf WHERE item_code = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, itemCode);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String itemName = rs.getString("item_name");
                        double selling_price = rs.getDouble("selling_price");
                        System.out.println("Item Name: " + itemName);
                        System.out.println("Price: " + selling_price);
                    } else {
                        System.out.println("Item not found for code: " + itemCode);
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
