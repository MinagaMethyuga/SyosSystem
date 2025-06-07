package Reports;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.CashierDashboard;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class SalesReport {

    public void displaySalesReportMenu() {
        Scanner scanner = ScannerInstance.getScanner();
        boolean continueReports = true;

        while (continueReports) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("                           SALES REPORT MENU");
            System.out.println("=".repeat(80));
            System.out.println("1. Today's Sales Report");
            System.out.println("2. Custom Date Sales Report");
            System.out.println("3. Return to Dashboard");
            System.out.println("=".repeat(80));
            System.out.print("Please select an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        displayTodaysSalesReport();
                        break;
                    case 2:
                        displayCustomDateSalesReport();
                        break;
                    case 3:
                        continueReports = false;
                        System.out.println("Returning to Dashboard...");
                        CashierDashboard realDashboard = new CashierDashboard();
                        realDashboard.viewDashboard();
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }

            if (continueReports && choice != 3) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    public void displayTodaysSalesReport() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        generateDailySalesReport(today, "Today's Sales Report");
    }

    public void displayCustomDateSalesReport() {
        Scanner scanner = ScannerInstance.getScanner();
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        try {
            // Validate date format
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            generateDailySalesReport(date, "Sales Report for " + date);
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
        }
    }

    private void generateDailySalesReport(String date, String reportTitle) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            // Header
            System.out.println("\n" + "=".repeat(100));
            System.out.println("                              SYOS RETAIL SYSTEM");
            System.out.println("                               " + reportTitle.toUpperCase());
            System.out.println("                                 " + date);
            System.out.println("=".repeat(100));

            // Query for detailed sales data
            String query = """
                SELECT 
                    bi.item_code,
                    bi.item_name,
                    SUM(bi.quantity) as total_quantity,
                    bi.unit_price,
                    bi.discount_price,
                    SUM(bi.total_price) as total_revenue
                FROM bill_items bi
                INNER JOIN bills b ON bi.bill_serial_no = b.bill_serial_no
                WHERE DATE(b.bill_date) = ?
                GROUP BY bi.item_code, bi.item_name, bi.unit_price, bi.discount_price
                ORDER BY total_revenue DESC
                """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                // Table header
                System.out.printf("%-12s %-25s %-8s %-12s %-12s %-12s%n",
                        "Item Code", "Item Name", "Qty Sold", "Unit Price", "Discount", "Total Revenue");
                System.out.println("-".repeat(100));

                double grandTotal = 0.0;
                double totalDiscount = 0.0;
                int totalItemsSold = 0;
                int uniqueItems = 0;

                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    uniqueItems++;

                    String itemCode = rs.getString("item_code");
                    String itemName = rs.getString("item_name");
                    int quantity = rs.getInt("total_quantity");
                    double unitPrice = rs.getDouble("unit_price");
                    Double discountPrice = rs.getObject("discount_price") != null ?
                            rs.getDouble("discount_price") : null;
                    double totalRevenue = rs.getDouble("total_revenue");

                    totalItemsSold += quantity;
                    grandTotal += totalRevenue;

                    // Calculate discount savings
                    if (discountPrice != null && discountPrice > 0) {
                        double originalTotal = unitPrice * quantity;
                        double discountSavings = originalTotal - totalRevenue;
                        totalDiscount += discountSavings;
                    }

                    System.out.printf("%-12s %-25s %-8d LKR%-9.2f %-12s LKR%-9.2f%n",
                            itemCode,
                            truncateName(itemName, 25),
                            quantity,
                            unitPrice,
                            (discountPrice != null && discountPrice > 0) ?
                                    String.format("LKR%.2f", discountPrice) : "N/A",
                            totalRevenue);
                }

                if (!hasData) {
                    System.out.println("                           No sales data found for " + date);
                    System.out.println("=".repeat(100));
                    return;
                }

                // Summary section
                System.out.println("=".repeat(100));
                System.out.println("                                   SUMMARY");
                System.out.println("=".repeat(100));
                System.out.printf("Total Unique Items Sold: %d%n", uniqueItems);
                System.out.printf("Total Quantity Sold: %d items%n", totalItemsSold);
                System.out.printf("Total Discount Given: LKR %.2f%n", totalDiscount);
                System.out.printf("TOTAL REVENUE: LKR %.2f%n", grandTotal);

                // Bill summary
                displayBillSummary(date);

                System.out.println("=".repeat(100));

            }
        } catch (SQLException e) {
            System.err.println("Error generating sales report: " + e.getMessage());
        }
    }

    private void displayBillSummary(String date) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            String billSummaryQuery = """
                SELECT 
                    COUNT(*) as total_bills,
                    AVG(total_amount) as avg_bill_amount,
                    MIN(total_amount) as min_bill_amount,
                    MAX(total_amount) as max_bill_amount
                FROM bills 
                WHERE DATE(bill_date) = ?
                """;

            try (PreparedStatement stmt = conn.prepareStatement(billSummaryQuery)) {
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int totalBills = rs.getInt("total_bills");
                    double avgBillAmount = rs.getDouble("avg_bill_amount");
                    double minBillAmount = rs.getDouble("min_bill_amount");
                    double maxBillAmount = rs.getDouble("max_bill_amount");

                    System.out.println("-".repeat(100));
                    System.out.printf("Total Bills Processed: %d%n", totalBills);
                    System.out.printf("Average Bill Amount: LKR %.2f%n", avgBillAmount);
                    System.out.printf("Smallest Bill: LKR %.2f%n", minBillAmount);
                    System.out.printf("Largest Bill: LKR %.2f%n", maxBillAmount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bill summary: " + e.getMessage());
        }
    }

    private String truncateName(String name, int maxLength) {
        if (name == null) return "";
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }
}