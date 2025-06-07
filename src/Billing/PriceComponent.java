package Billing;

public interface PriceComponent {
    double getPrice();
    int getQuantity();
    ItemInfo getItemInfo();
}