package Billing;

public class DiscountDecorator implements PriceComponent {
    private final PriceComponent billItem;
    private final double discountPrice;

    public DiscountDecorator(PriceComponent billItem, double discountPrice) {
        this.billItem = billItem;
        this.discountPrice = discountPrice;
    }

    @Override
    public double getPrice() {
        return discountPrice * billItem.getQuantity();
    }

    @Override
    public int getQuantity() {
        return billItem.getQuantity();
    }

    @Override
    public ItemInfo getItemInfo() {
        return billItem.getItemInfo();
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public double getOriginalPrice() {
        return billItem.getPrice();
    }

    public void displayPriceBreakdown() {
        ItemInfo item = getItemInfo();
        System.out.printf("Item: %s, Original Price: %.2f, Discount Price: %.2f, Qty: %d, Total: %.2f%n",
                item.getItemName(),
                item.getPrice(),
                discountPrice,
                getQuantity(),
                getPrice());
    }
}