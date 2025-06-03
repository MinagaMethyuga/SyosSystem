package Dashboard;

import Common.ScannerInstance;
import Items.AddItem;
import Items.ItemTemplate;

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

                String itemCode;
                do {
                    System.out.print("Enter Item Code: ");
                    itemCode = scanner.nextLine();
                    if (itemCode.isEmpty()) {
                        System.out.println("Item code cannot be empty. Please enter a valid Item Code.");
                    }
                } while (itemCode.isEmpty());

                // Prompt and validate item name
                String itemName;
                do {
                    System.out.print("Enter Item Name: ");
                    itemName = scanner.nextLine();
                    if (itemName.isEmpty()) {
                        System.out.println("Item name cannot be empty. Please enter a valid Item Name.");
                    }
                } while (itemName.isEmpty());

                // Prompt and validate quantity
                int quantity;
                while (true) {
                    System.out.print("Enter Item quantity: ");
                    String quantityInput = scanner.nextLine();
                    try {
                        quantity = Integer.parseInt(quantityInput);
                        if (quantity <= 0) {
                            System.out.println("Quantity must be greater than zero.");
                        } else {
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a numeric value for quantity.");
                    }
                }

                // Prompt and validate price
                double price;
                while (true) {
                    System.out.print("Enter Item unit price: ");
                    String priceInput = scanner.nextLine();
                    try {
                        price = Double.parseDouble(priceInput);
                        if (price <= 0) {
                            System.out.println("Price must be greater than zero.");
                        } else {
                            break;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a numeric value for price.");
                    }
                }
                ItemTemplate itemTemplate = new AddItem();
                itemTemplate.addItemToStock(itemCode, itemName, quantity, price);
                break;
            case 2:
                System.out.println("Viewing stock functionality is not implemented yet.");
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
}
