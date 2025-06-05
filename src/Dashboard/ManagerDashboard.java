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
                viewStockWithOptions();
                break;
            case 3:
                System.out.println("Managing reports functionality is not implemented yet.");
                break;
            case 4:
                System.out.println("Exiting the Manager Dashboard.");
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }

    private void viewStockWithOptions() {
        Scanner scanner = ScannerInstance.getScanner();
        StockDAO stockDAO = new StockDAO();

        while (true) {
            System.out.println("...................................................................................");
            System.out.println("Stock Viewing Options:");
            System.out.println("1. View All Stock (Default)");
            System.out.println("2. Filter by Purchase Date (Oldest First)");
            System.out.println("3. Filter by Purchase Date (Newest First)");
            System.out.println("4. Filter by Expiry Date (Oldest First)");
            System.out.println("5. Filter by Expiry Date (Newest First)");
            System.out.println("6. Back to Dashboard");
            System.out.print("Select filtering option: ");

            int filterChoice;
            try {
                filterChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1-6.");
                continue;
            }

            StockDAO.FilterOption filterOption;
            String filterDescription;

            switch (filterChoice) {
                case 1:
                    filterOption = StockDAO.FilterOption.DEFAULT;
                    filterDescription = "All Stock (Default View)";
                    break;
                case 2:
                    filterOption = StockDAO.FilterOption.PURCHASE_DATE_OLDEST_FIRST;
                    filterDescription = "Purchase Date (Oldest First)";
                    break;
                case 3:
                    filterOption = StockDAO.FilterOption.PURCHASE_DATE_NEWEST_FIRST;
                    filterDescription = "Purchase Date (Newest First)";
                    break;
                case 4:
                    filterOption = StockDAO.FilterOption.EXPIRY_DATE_OLDEST_FIRST;
                    filterDescription = "Expiry Date (Oldest First)";
                    break;
                case 5:
                    filterOption = StockDAO.FilterOption.EXPIRY_DATE_NEWEST_FIRST;
                    filterDescription = "Expiry Date (Newest First)";
                    break;
                case 6:
                    // Return to main dashboard
                    new ManagerDashboard().viewDashboard();
                    return;
                default:
                    System.out.println("Invalid choice. Please select a number between 1-6.");
                    continue;
            }

            // Fetch and display filtered stock
            List<String[]> stockList = stockDAO.getStockDetails(filterOption);

            if (stockList.isEmpty()) {
                System.out.println("No stock available.");
            } else {
                System.out.println("...................................................................................");
                System.out.println("Stock List - Filter Applied: " + filterDescription);
                System.out.println("...................................................................................");
                StockView.displayStock(stockList);
            }

            System.out.println("...................................................................................");

            // Options after viewing stock
            while (true) {
                System.out.println("What would you like to do next?");
                System.out.println("1. Apply Different Filter");
                System.out.println("2. Back to Dashboard");
                System.out.print("Enter your choice: ");

                int nextChoice;
                try {
                    nextChoice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter 1 or 2.");
                    continue;
                }

                if (nextChoice == 1) {
                    // Break inner loop to show filter options again
                    break;
                } else if (nextChoice == 2) {
                    // Navigate back to the dashboard
                    new ManagerDashboard().viewDashboard();
                    return;
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            }
        }
    }
}