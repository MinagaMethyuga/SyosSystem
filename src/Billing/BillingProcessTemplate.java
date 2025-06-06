package Billing;

public abstract class BillingProcessTemplate {

    // Template method defining the steps
    public final void processBilling() {
        // Step 1: Validate inputs and get itemCode
        String itemCode = validateItemCode();
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String validateItemCode(); // itemCode input and validation
}
