package Items;

import java.util.List;

public class ViewShelfStock {

    // Static method to view shelf stock
    public static void ViewShelfStock(List<String[]> shelfStocks) {
        if (shelfStocks.isEmpty()) {
            System.out.println("No items found in shelf stock.");
            return;
        }

        System.out.println("...................................................................................");
        System.out.println("Shelf Stock Status");
        System.out.println("...................................................................................");

        // Create an instance of ShelfStockView to display the data
        ShelfStockView shelfStockView = new ShelfStockView();
        shelfStockView.shelfStockView(shelfStocks);

        System.out.println("...................................................................................");
    }

    // Instance method to view shelf stock with fetching
    public void viewShelfStock() {
        List<String[]> shelfStocks = ShelfStockDAO.getShelfDetails();
        ViewShelfStock(shelfStocks);
    }
}