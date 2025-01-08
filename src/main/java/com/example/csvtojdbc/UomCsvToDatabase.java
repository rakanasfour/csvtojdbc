package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class UomCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String uomCsvPath = "src/main/resources/uoms.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader uomReader = new CSVReader(new FileReader(uomCsvPath))
        ) {
            // SQL queries
            String checkUomQuery = "SELECT COUNT(*) FROM uoms WHERE uoms = ?";
            String checkItemQuery = "SELECT COUNT(*) FROM items WHERE item_id = ?";
            String insertUomQuery = "INSERT INTO uoms (uom_id, uom_type,uom_sub_type, uom_quantity, uom_level,uom_weight,uom_status,uom_item_id) VALUES (?, ?, ?, ?,?,?,?,?)";

            try (
                PreparedStatement checkUomStmt = connection.prepareStatement(checkUomQuery);
                PreparedStatement checkItemStmt = connection.prepareStatement(checkItemQuery);
                PreparedStatement insertUomStmt = connection.prepareStatement(insertUomQuery)
            ) {
                // Skip the header row
                uomReader.readNext();
                
       
                // Read and process each row
                String[] uomRow;
                while ((uomRow = uomReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer uomId = Integer.parseInt(uomRow[0]); 
                        Float uomWeight = (uomRow[7] != null && !uomRow[7].isEmpty()) ? Float.parseFloat(uomRow[7]) : null;
                        Float uomQuantity = (uomRow[30] != null && !uomRow[30].isEmpty()) ? Float.parseFloat(uomRow[30]) : null;
                        String uomType = uomRow[31].toUpperCase();
                        String uomSubType = uomRow[31].toUpperCase();
                        Integer uomLevel = 5;
                        String uomStatus = "AVAILABLE"; 
                        Integer uomItemId = Integer.parseInt(uomRow[4]);
       
                 
                        if (uomWeight == null ) {
                        	
                        	uomWeight=(float) 8.8;
                        	
                        }
                        if (uomQuantity== null) {
                        	uomQuantity=(float) 8.8;
                        }

        
                        // Check if manufacturer ID exists
                        checkItemStmt.setInt(1, uomItemId);
                        try (ResultSet itemResultSet = checkItemStmt.executeQuery()) {
                        	itemResultSet.next();
                            int itemExists = itemResultSet.getInt(1);
                            if (itemExists == 0) {
                                System.err.println("Model ID " + uomItemId + " does not exist. Skipping uom: " + uomId);
                                continue; // Skip this brand
                            }
                        }

                        // Set parameters for the query
                        insertUomStmt.setInt(1, uomId);
                        insertUomStmt.setString(2,uomType);
                        insertUomStmt.setString(3,uomSubType);
                        int uomQuantityInt = (uomQuantity != null) ? Math.round(uomQuantity) : 0; 
                        insertUomStmt.setFloat(4,uomQuantityInt);
                        insertUomStmt.setInt(5, uomLevel);
                        int uomWeightInt = (uomWeight != null) ? Math.round(uomWeight) : 0; 
                        insertUomStmt.setInt(6,uomWeightInt);
                        insertUomStmt.setString(7, uomStatus);
                        insertUomStmt.setInt(8,uomItemId);
                      

                        // Execute the query
                        insertUomStmt.executeUpdate();
                        System.out.println("Inserted item: " + uomId);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(",", uomRow));
                    }
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
