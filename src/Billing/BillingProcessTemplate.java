package Billing;

import java.util.List;

public abstract class BillingProcessTemplate {

    // Template method defining the steps
    public final void processBilling() {
        List<BillItem> billItems = collectItems();
        generateFinalBill(billItems);
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String validateItemCode();
    protected abstract ItemInfo queryItemInfo(String itemCode);
    protected abstract int getQuantity();
    protected abstract boolean addMoreItems();
    protected abstract List<BillItem> collectItems();
    protected abstract void generateFinalBill(List<BillItem> billItems);

    // Inner class to hold item information
    public static class ItemInfo {
        private String itemCode;
        private String itemName;
        private double price;

        public ItemInfo(String itemCode, String itemName, double price) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.price = price;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public double getPrice() { return price; }
    }

    // Inner class to hold bill item with quantity and total
    public static class BillItem {
        private ItemInfo itemInfo;
        private int quantity;
        private double total;

        public BillItem(ItemInfo itemInfo, int quantity) {
            this.itemInfo = itemInfo;
            this.quantity = quantity;
            this.total = itemInfo.getPrice() * quantity;
        }

        // Getters
        public ItemInfo getItemInfo() { return itemInfo; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return total; }
    }
}