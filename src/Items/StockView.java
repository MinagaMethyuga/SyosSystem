package Items;

import java.util.List;

public class StockView {

    public static void displayStock(List<String[]> stockList) {
        System.out.printf("%-10s %-15s %-10s %-10s %-15s %-15s\n",
                "Code", "Name", "Quantity", "Price", "Date Purchased", "Expiry Date");
        System.out.println("-----------------------------------------------------------------------------");
        for (String[] stock : stockList) {
            System.out.printf("%-10s %-15s %-10s %-10s %-15s %-15s\n",
                    stock[0], stock[1], stock[2], stock[3], stock[4], stock[5]);
        }
    }
}
