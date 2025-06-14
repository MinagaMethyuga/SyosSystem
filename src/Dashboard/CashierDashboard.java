package Dashboard;

import Auth.Login;
import Billing.BillingProcessTemplate;
import Billing.MakeBill;
import Common.ScannerInstance;
import Reports.SalesReport;

import java.util.Scanner;

public class CashierDashboard implements DashboardAccess {
    @Override
    public boolean viewDashboard() {
        System.out.println("......................................................................................");
        System.out.println("Welcome to the Cashier Billing System");
        System.out.println("......................................................................................");

        System.out.println("1. Process Billing");
        System.out.println("2. View Sales Report");
        System.out.println("3. Exit");

        Scanner scanner = ScannerInstance.getScanner();
        System.out.print("Please select an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                // Use the new continuous billing method instead of single billing
                MakeBill billingProcess = new MakeBill();
                billingProcess.startContinuousBilling();
                break;
            case 2:
                // Display sales report
                SalesReport salesReport = new SalesReport();
                salesReport.displaySalesReportMenu();
                break;
            case 3:
                System.out.println("Exiting the Cashier Dashboard.");
                Login login = new Login();
                login.login();
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
        return true;
    }
}