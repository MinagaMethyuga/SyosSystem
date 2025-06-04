package Items;

import java.time.LocalDate;

public abstract class ItemTemplate {

    // Template method defining the steps
    public final void addItemToStock() {

        // Initialize variables
        String itemCode = "";
        String itemName = "";
        int quantity = 0;
        double price = 0;

        // Step 1: Validate inputs and update itemCode, itemName, quantity, price
        itemCode = validateInputs();
        itemName = fetchItemName(itemCode);

        // Step 2: Add validated purchase date and update quantity and price
        LocalDate purchaseDate = addValidatedPurchaseDate(itemCode, itemName, quantity, price);

        // Step 3: Add validated Expiry date
        LocalDate expirationDate = addExpirationDate(itemCode, itemName, quantity, price, purchaseDate);

        // Step 4: Add the restocking level and update the quantity
        int restockLevel = addRestockLevel(itemCode, itemName, quantity, price, purchaseDate, expirationDate);

        // Step 5: Apply discounts and confirm
        ValidateInforAndConfirm(itemCode, itemName, quantity, price, purchaseDate , expirationDate, restockLevel);

        // Step 6: Save the item to the database
        saveItemToDatabase(itemCode, itemName, quantity, price, purchaseDate, expirationDate, restockLevel);

        // Step 7: Continuation method
        Continuation();
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String validateInputs(); // itemCode input and validation

    protected abstract String fetchItemName(String itemCode); // Fetch item name based on item code

    protected abstract LocalDate addValidatedPurchaseDate(String itemCode, String itemName, int quantity, double price); // Purchase Date input

    protected abstract LocalDate addExpirationDate(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate); // Expiration Date input

    protected abstract int addRestockLevel(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate); // Restock Level input

    protected abstract void ValidateInforAndConfirm(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate , int restockLevel);

    protected abstract void saveItemToDatabase(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel);

    protected abstract void Continuation();
}