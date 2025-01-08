package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class ItemCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String itemCsvPath = "src/main/resources/items.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader itemReader = new CSVReader(new FileReader(itemCsvPath))
        ) {
            // SQL queries
            String checkItemQuery = "SELECT COUNT(*) FROM items WHERE item_name = ?";
            String checkModelQuery = "SELECT COUNT(*) FROM models WHERE model_id = ?";
            String insertItemQuery = "INSERT INTO items (item_id, item_name,item_sku, item_description, item_status,item_model_id) VALUES (?, ?, ?, ?,?,?)";

            try (
                PreparedStatement checkItemStmt = connection.prepareStatement(checkItemQuery);
                PreparedStatement checkModelStmt = connection.prepareStatement(checkModelQuery);
                PreparedStatement insertItemStmt = connection.prepareStatement(insertItemQuery)
            ) {
                // Skip the header row
                itemReader.readNext();
                
                int counter = 1;
                // Read and process each row
                String[] itemRow;
                while ((itemRow = itemReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer itemId = Integer.parseInt(itemRow[0]); 
                        String itemName = itemRow[3]; 
                        Integer itemModelId = Integer.parseInt(itemRow[16]);
                    
                        Integer itemSku = counter++;
                        String itemDescription = "test description";
                        String itemStatus = "AVAILABLE"; 

                        // Check if brand name already exists
                        checkItemStmt.setString(1, itemName);
                        try (ResultSet itemResultSet = checkItemStmt.executeQuery()) {
                        	itemResultSet.next();
                            int itemExists = itemResultSet.getInt(1);
                            if (itemExists > 0) {
                                System.err.println("Duplicate item name: " + itemName + ". Skipping...");
                                continue; // Skip this brand
                            }
                        }

        
                        // Check if manufacturer ID exists
                        checkModelStmt.setInt(1, itemModelId);
                        try (ResultSet modelResultSet = checkModelStmt.executeQuery()) {
                            modelResultSet.next();
                            int modelExists = modelResultSet.getInt(1);
                            if (modelExists == 0) {
                                System.err.println("Model ID " + itemModelId + " does not exist. Skipping item: " + itemName);
                                continue; // Skip this brand
                            }
                        }

                        // Set parameters for the query
                        insertItemStmt.setInt(1, itemId);
                        insertItemStmt.setString(2, itemName);
                        insertItemStmt.setInt(3, itemSku);
                        insertItemStmt.setString(4, itemDescription);
                        insertItemStmt.setString(5, itemStatus);
                        insertItemStmt.setInt(6, itemModelId);

                        // Execute the query
                        insertItemStmt.executeUpdate();
                        System.out.println("Inserted item: " + itemName);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(",", itemRow));
                    }
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
