package Billing;

public class MakeBill extends BillingProcessTemplate {

    @Override
    protected String validateItemCode() {
        String itemCode = "100";
        return itemCode;
    }
}
