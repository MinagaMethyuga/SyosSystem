package Dashboard;

import Common.ScannerInstance;
import Items.AddItem;
import Items.ItemTemplate;
import Items.StockDAO;
import Items.StockView;

import java.util.List;
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
        System.out.println("3. Manage Reports");
        System.out.println("4. Exit");

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
                StockDAO stockDAO = new StockDAO();
                List<String[]> stockList = stockDAO.getStockDetails();

                if (stockList.isEmpty()) {
                    System.out.println("No stock available.");
                } else {
                    StockView.displayStock(stockList);
                }

                System.out.println("...................................................................................");

                // Input validation loop
                int backChoice;
                do {
                    System.out.println("1) To go back to the dashboard.");
                    while (!scanner.hasNextInt()) { // Ensure valid integer input
                        System.out.println("Invalid input. Please enter a valid number.");
                        scanner.next(); // Clear invalid input
                    }
                    backChoice = scanner.nextInt();
                    if (backChoice != 1) {
                        System.out.println("Invalid choice. Please type 1 to go back to the dashboard.");
                    }
                } while (backChoice != 1);

                // Navigate back to the dashboard
                new ManagerDashboard().viewDashboard();
                break;
            case 3:
                System.out.println("Managing reports functionality is not implemented yet.");
                break;
            case 5:
                System.out.println("Exiting the Manager Dashboard.");
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }
}
