package Billing;

public class ItemInfo {
    private String itemCode;
    private String itemName;
    private double price;
    private Double discountPrice;

    public ItemInfo(String itemCode, String itemName, double price, Double discountPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.price = price;
        this.discountPrice = discountPrice;
    }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public Double getDiscountPrice() { return discountPrice; }

    public void setDiscountPrice(Double discountPrice) {
        this.discountPrice = discountPrice;
    }
}