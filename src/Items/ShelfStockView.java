package Items;

import java.util.List;

public class ShelfStockView {

    public void shelfStockView(List<String[]> shelfStocks){
        System.out.printf("%-10s %-15s %-10s\n",
                "Code", "Name", "Quantity");
        System.out.println("-----------------------------------------------------------------------------");
        for (String[] shelf : shelfStocks) {
            System.out.printf("%-10s %-15s %-10s\n",
                    shelf[0], shelf[1], shelf[2]);
        }
    }
}
