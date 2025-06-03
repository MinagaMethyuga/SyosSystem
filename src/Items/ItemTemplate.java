package Items;

import java.time.LocalDate;

public abstract class ItemTemplate {

    // Template method defining the steps
    public final void addItemToStock(String itemCode, String itemName, int quantity, double price) {
        // Step 1: Validate inputs
        validateItemDetails(itemCode, itemName, quantity, price);

        // Step 2: Add validated purchase date
        LocalDate purchaseDate = addValidatedPurchaseDate(itemCode, itemName, quantity, price);

        // Step 3: Add validated Expiry date
        LocalDate expirationDate = addExpirationDate(itemCode, itemName, quantity, price, purchaseDate);

        // Step 4: Add the restocking Level
        int restockLevel = addRestockLevel(itemCode, itemName, quantity, price, purchaseDate , expirationDate);

        // Step 4: Apply discounts
        ValidateInforAndConfirm(itemCode, itemName, quantity, price, purchaseDate , expirationDate, restockLevel);

        // Step 5: Save the item to the stockDatabase
        saveItemToDatabase(itemCode, itemName, quantity, price, purchaseDate, expirationDate, restockLevel);

    }

    // Abstract methods to be implemented by subclasses
    protected abstract void validateItemDetails(String itemCode, String itemName, int quantity, double price);

    protected abstract LocalDate addValidatedPurchaseDate(String itemCode, String itemName, int quantity, double price);

    protected abstract LocalDate addExpirationDate(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate);

    protected abstract int addRestockLevel(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate);

    protected abstract void ValidateInforAndConfirm(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate , int restockLevel);

    protected  abstract void saveItemToDatabase(String itemCode, String itemName, int quantity, double price, LocalDate purchaseDate, LocalDate expirationDate, int restockLevel);

}
