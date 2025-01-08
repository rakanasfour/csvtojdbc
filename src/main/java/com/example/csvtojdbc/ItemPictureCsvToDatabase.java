package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class ItemPictureCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the item CSV file
        String itemPictureCsvPath = "src/main/resources/itempictures.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader itemPictureReader = new CSVReader(new FileReader(itemPictureCsvPath))
        ) {
            // SQL queries
         
            String checkItemQuery = "SELECT COUNT(*) FROM items WHERE item_id = ?";
            String insertItemPictureQuery = "INSERT INTO item_pictures (item_picture_id,item_picture_main, item_p_item_id) VALUES (?,?,?)";

            try (
               
                PreparedStatement checkItemStmt = connection.prepareStatement(checkItemQuery);
                PreparedStatement insertItemPictureStmt = connection.prepareStatement(insertItemPictureQuery)
            ) {
                // Skip the header row
                itemPictureReader.readNext();
                
       
                // Read and process each row
                String[] itemRow;
                while ((itemRow = itemPictureReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer itemPictureId = Integer.parseInt(itemRow[0]); 
                        Integer itemPItemId = Integer.parseInt(itemRow[1]);
                        String itemPictureMain = itemRow[4];
 



        
                        // Check if manufacturer ID exists
                        checkItemStmt.setInt(1, itemPItemId);
                        try (ResultSet itemResultSet = checkItemStmt.executeQuery()) {
                        	itemResultSet.next();
                            int itemExists = itemResultSet.getInt(1);
                            if (itemExists == 0) {
                                System.err.println("Item ID " + itemPItemId + " does not exist. Skipping item: " + itemPictureMain);
                                continue; // Skip this item
                            }
                        }

                        // Set parameters for the query
                        insertItemPictureStmt.setInt(1, itemPictureId);
                        insertItemPictureStmt.setString(2, itemPictureMain);
                        insertItemPictureStmt.setInt(3, itemPItemId);
          

                        // Execute the query
                        insertItemPictureStmt.executeUpdate();
                        System.out.println("Inserted item Picture: " + itemPictureMain);
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
