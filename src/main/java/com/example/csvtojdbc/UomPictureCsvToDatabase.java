package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class UomPictureCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the uom CSV file
        String uomPictureCsvPath = "src/main/resources/uompictures.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader uomPictureReader = new CSVReader(new FileReader(uomPictureCsvPath))
        ) {
            // SQL queries
         
            String checkUomQuery = "SELECT COUNT(*) FROM uoms WHERE uom_id = ?";
            String insertUomPictureQuery = "INSERT INTO uom_pictures (uom_picture_id, uom_picture_name,uom_picture_link, uom_picture_uom_id) VALUES (?, ?, ?,?)";

            try (
               
                PreparedStatement checkUomStmt = connection.prepareStatement(checkUomQuery);
                PreparedStatement insertUomPictureStmt = connection.prepareStatement(insertUomPictureQuery)
            ) {
                // Skip the header row
                uomPictureReader.readNext();
                
       
                // Read and process each row
                String[] uomRow;
                while ((uomRow = uomPictureReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer uomPictureId = Integer.parseInt(uomRow[0]); 
                        Integer uomPictureUomId = Integer.parseInt(uomRow[1]);
                        String uomPictureName = uomRow[2];
                        String uomPictureLink = uomRow[4];
 



        
                        // Check if manufacturer ID exists
                        checkUomStmt.setInt(1, uomPictureUomId);
                        try (ResultSet uomResultSet = checkUomStmt.executeQuery()) {
                        	uomResultSet.next();
                            int uomExists = uomResultSet.getInt(1);
                            if (uomExists == 0) {
                                System.err.println("Uom ID " + uomPictureUomId + " does not exist. Skipping item: " + uomPictureName);
                                continue; // Skip this uom
                            }
                        }

                        // Set parameters for the query
                        insertUomPictureStmt.setInt(1, uomPictureId);
                        insertUomPictureStmt.setString(2, uomPictureName);
                        insertUomPictureStmt.setString(3, uomPictureLink);
                        insertUomPictureStmt.setInt(4, uomPictureUomId);
          

                        // Execute the query
                        insertUomPictureStmt.executeUpdate();
                        System.out.println("Inserted uom Picture: " + uomPictureName);
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
