package Items;

import java.util.List;

public class ShelfStockView {

    public void shelfStockView(List<String[]> shelfStocks){
        System.out.printf("%-10s %-15s %-10s %-15s %-15s\n",
                "Code", "Name", "Quantity", "Selling Price", "Discount Price");
        System.out.println("-----------------------------------------------------------------------------");
        for (String[] shelf : shelfStocks) {
            System.out.printf("%-10s %-15s %-10s LKR %-13s %-15s\n",
                    shelf[0], shelf[1], shelf[2], shelf[3] != null ? shelf[3] : "N/A" , shelf[4] != null ? shelf[4] : "N/A");
        }
    }
}