package Dashboard;

import Auth.Login;
import Common.ScannerInstance;
import Items.*;
import Reports.ReportManager;

import java.util.Scanner;

public class ManagerDashboard implements DashboardAccess {
    @Override
    public boolean viewDashboard(){
        System.out.println("......................................................................................");
        System.out.println("Welcome to the Manager Dashboard");
        System.out.println("Here you can manage the system, view reports, and perform administrative tasks.");

        //get scanner instance
        Scanner scanner = ScannerInstance.getScanner();
        System.out.println("......................................................................................");
        System.out.println("1. Add Item");
        System.out.println("2. View Stock");
        System.out.println("3. Restock Items");
        System.out.println("4. Manage Reports");
        System.out.println("5. Exit");

        System.out.print("Please select an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.println("...................................................................................");
                System.out.println("Item Code Categories");
                System.out.println(" ");
                System.out.println("Fruits and Vegetables = A0000");
                System.out.println("Dairy Products = B0000");
                System.out.println("Meat and Fish = C0000");
                System.out.println("Beverages = D0000");
                System.out.println("Snacks = E0000");
                System.out.println("Other Items = F0000");
                System.out.println("...................................................................................");

                ItemTemplate itemTemplate = new AddItem();
                itemTemplate.addItemToStock();
                break;
            case 2:
                ViewStock viewStock = new ViewStock();
                viewStock.viewStockWithOptions();
                break;
            case 3:
                RestockItems restockItems = new RestockItems();
                restockItems.restockItemsMenu();
                break;
            case 4:
                ReportManager reportManager = new ReportManager();
                reportManager.displayReportMenu();
                break;
            case 5:
                System.out.println("Exiting the Manager Dashboard.");
                Login login = new Login();
                login.login();
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }
}