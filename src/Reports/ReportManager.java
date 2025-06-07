package Reports;

import Common.DatabaseConnection;
import Common.ScannerInstance;
import Dashboard.ManagerDashboard;

import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ReportManager {

    public void displayReportMenu() {
        Scanner scanner = ScannerInstance.getScanner();
        boolean continueReports = true;

        while (continueReports) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("                           REPORT MANAGEMENT SYSTEM");
            System.out.println("=".repeat(80));
            System.out.println("1. Sales Reports");
            System.out.println("3. Bill Reports");
            System.out.println("4. Return to Dashboard");
            System.out.println("=".repeat(80));
            System.out.print("Please select an option: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        SalesReport salesReport = new SalesReport();
                        salesReport.displaySalesReportMenu();
                        break;
                    case 2:
                        displayBillReport();
                        break;
                    case 3:
                        continueReports = false;
                        System.out.println("Returning to Dashboard...");
                        ManagerDashboard realDashboard = new ManagerDashboard();
                        realDashboard.viewDashboard();
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1-5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }

            if (continueReports && choice != 5) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void displayBillReport() {
        Scanner scanner = ScannerInstance.getScanner();
        System.out.print("Enter date for bill report (YYYY-MM-DD) or press Enter for today: ");
        String dateInput = scanner.nextLine().trim();

        String date = dateInput.isEmpty() ?
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : dateInput;

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            System.out.println("\n" + "=".repeat(100));
            System.out.println("                               BILL REPORT FOR " + date);
            System.out.println("=".repeat(100));

            String query = """
                SELECT 
                    bill_serial_no,
                    bill_date,
                    total_amount,
                    cash_tendered,
                    change_amount
                FROM bills 
                WHERE DATE(bill_date) = ?
                ORDER BY bill_serial_no
                """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();

                System.out.printf("%-12s %-20s %-15s %-15s %-15s%n",
                        "Bill No", "Date & Time", "Total Amount", "Cash Tendered", "Change");
                System.out.println("-".repeat(100));

                double totalSales = 0.0;
                int billCount = 0;

                while (rs.next()) {
                    billCount++;
                    double totalAmount = rs.getDouble("total_amount");
                    totalSales += totalAmount;

                    System.out.printf("%-12s %-20s LKR%-12.2f LKR%-12.2f LKR%-12.2f%n",
                            String.format("%06d", rs.getInt("bill_serial_no")),
                            rs.getString("bill_date"),
                            totalAmount,
                            rs.getDouble("cash_tendered"),
                            rs.getDouble("change_amount"));
                }

                System.out.println("=".repeat(100));
                System.out.printf("Total Bills: %d | Total Sales: LKR %.2f%n", billCount, totalSales);
                System.out.println("=".repeat(100));

            }
        } catch (SQLException e) {
            System.err.println("Error generating bill report: " + e.getMessage());
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