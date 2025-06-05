package Items;

import Common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    // Enum for filter options
    public enum FilterOption {
        DEFAULT,
        PURCHASE_DATE_OLDEST_FIRST,
        PURCHASE_DATE_NEWEST_FIRST,
        EXPIRY_DATE_OLDEST_FIRST,
        EXPIRY_DATE_NEWEST_FIRST
    }

    // Default method - keeps existing functionality
    public List<String[]> getStockDetails() {
        return getStockDetails(FilterOption.DEFAULT);
    }

    // Enhanced method with filtering options
    public List<String[]> getStockDetails(FilterOption filterOption) {
        List<String[]> stockList = new ArrayList<>();
        String query = buildQuery(filterOption);

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String[] stock = new String[6];
                stock[0] = resultSet.getString("item_code");
                stock[1] = resultSet.getString("item_name");
                stock[2] = String.valueOf(resultSet.getInt("quantity"));
                stock[3] = String.valueOf(resultSet.getDouble("price"));
                stock[4] = resultSet.getDate("purchaseDate").toString();
                stock[5] = resultSet.getDate("expirationDate") != null
                        ? resultSet.getDate("expirationDate").toString() : "N/A";

                stockList.add(stock);
            }

        } catch (Exception e) {
            System.out.println("Error fetching stock data: " + e.getMessage());
        }
        return stockList;
    }

    // Build SQL query based on filter option
    private String buildQuery(FilterOption filterOption) {
        String baseQuery = "SELECT item_code, item_name, quantity, price, purchaseDate, expirationDate FROM stock";

        switch (filterOption) {
            case PURCHASE_DATE_OLDEST_FIRST:
                return baseQuery + " ORDER BY purchaseDate ASC";
            case PURCHASE_DATE_NEWEST_FIRST:
                return baseQuery + " ORDER BY purchaseDate DESC";
            case EXPIRY_DATE_OLDEST_FIRST:
                return baseQuery + " ORDER BY expirationDate ASC";
            case EXPIRY_DATE_NEWEST_FIRST:
                return baseQuery + " ORDER BY expirationDate DESC";
            default:
                return baseQuery; // No ordering for default
        }
    }
}