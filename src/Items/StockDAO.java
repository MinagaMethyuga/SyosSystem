package Items;

import Common.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public List<String[]> getStockDetails() {
        List<String[]> stockList = new ArrayList<>();
        String query = "SELECT item_code, item_name, quantity, price, purchaseDate, expirationDate FROM stock";

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String[] stock = new String[6]; // Adjust array size for the number of fields you need
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
}