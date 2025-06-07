package Billing;

import java.util.List;

public abstract class BillingProcessTemplate {

    public final void processBilling() {
        List<BillItem> billItems = collectItems();
        generateFinalBill(billItems);
    }

    protected abstract String validateItemCode();
    protected abstract ItemInfo queryItemInfo(String itemCode);
    protected abstract int getQuantity();
    protected abstract boolean addMoreItems();
    protected abstract List<BillItem> collectItems();
    protected abstract void generateFinalBill(List<BillItem> billItems);
}