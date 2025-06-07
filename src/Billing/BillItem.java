package Billing;

public class BillItem implements PriceComponent {
    private ItemInfo itemInfo;
    private int quantity;
    private double total;

    public BillItem(ItemInfo itemInfo, int quantity) {
        this.itemInfo = itemInfo;
        this.quantity = quantity;
        this.total = itemInfo.getPrice() * quantity;
    }

    @Override
    public double getPrice() {
        return total;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public double getTotal() {
        return total;
    }
}
