package Items;

import java.time.LocalDate;

public abstract class ItemTemplate {

    // Template method defining the steps
    public final void addItemToStock() {

        // Step 1: Validate inputs and get itemCode
        String itemCode = validateInputs();
        String itemName = fetchItemName(itemCode);

        if (itemName == null || itemName.trim().isEmpty()) {
            // Prompt user to enter a new item name if not found in DB
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            itemName = scanner.nextLine();
        }

        // Step 2: Get quantity and price from user input
        QuantityPriceResult qpResult = getQuantityAndPriceInput();
        int quantity = qpResult.quantity;
        double price = qpResult.price;

        // Step 3: Add validated purchase date
        LocalDate purchaseDate = addValidatedPurchaseDate(itemCode, itemName, quantity, price);

        // Step 4: Add validated Expiry date
        LocalDate expirationDate = addExpirationDate(itemCode, itemName, quantity, price, purchaseDate);

        // Step 5: Add the restocking level
        int restockLevel = addRestockLevel(itemCode, itemName, quantity, price, purchaseDate, expirationDate);

        // Step 6: Validate information and confirm - THIS NOW RETURNS A BOOLEAN
        boolean confirmed = ValidateInforAndConfirm(itemCode, itemName, quantity, price, purchaseDate, expirationDate, restockLevel);

        // Only proceed if user confirmed
        if (confirmed) {
            // Step 7: Save the item to the database
            saveItemToDatabase(itemCode, itemName, quantity, price, purchaseDate, expirationDate, restockLevel);

            // Step 8: Continuation method
            Continuation();
        }
    }

    // Helper class to return both quantity and price
    protected static class QuantityPriceResult {
        public final int quantity;
        public final double price;

        public QuantityPriceResult(int quantity, double price) {
            this.quantity = quantity;
            this.price = price;
        }
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String validateInputs(); // itemCode input and validation

    protected abstract String fetchItemName(String itemCode); // Fetch item name based on item code

    protected abstract QuantityPriceResult getQuantityAndPriceInput(); // Get quantity and price from user

    protected abstract LocalDate addValidatedPurchaseDate(String itemCode, String itemName, int quantity, double price); // Purchase Date input

    protected abstract LocalDate addExpirationDate(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate); // Expiration Date input

    protected abstract int addRestockLevel(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate); // Restock Level input

    // CHANGED: Now returns boolean to indicate if user confirmed
    protected abstract boolean ValidateInforAndConfirm(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel);

    protected abstract void saveItemToDatabase(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel);

    protected abstract void Continuation();
}