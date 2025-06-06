package Items;

import java.util.List;

public class ShelfStockView {

    public void shelfStockView(List<String[]> shelfStocks){
        System.out.printf("%-10s %-15s %-10s %-15s\n",
                "Code", "Name", "Quantity", "Selling Price");
        System.out.println("-----------------------------------------------------------------------------");
        for (String[] shelf : shelfStocks) {
            System.out.printf("%-10s %-15s %-10s LKR %-13s\n",
                    shelf[0], shelf[1], shelf[2], shelf[3]);
        }
    }
}