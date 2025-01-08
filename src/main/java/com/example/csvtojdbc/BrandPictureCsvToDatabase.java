package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class BrandPictureCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String brandPictureCsvPath = "src/main/resources/brandpictures.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader brandPictureReader = new CSVReader(new FileReader(brandPictureCsvPath))
        ) {
            // SQL queries
         
            String checkBrandQuery = "SELECT COUNT(*) FROM brands WHERE brand_id = ?";
            String insertBrandPictureQuery = "INSERT INTO brand_pictures (brand_picture_id, brand_picture_name,brand_picture_link, b_p_brand_id) VALUES (?, ?, ?,?)";

            try (
               
                PreparedStatement checkBrandStmt = connection.prepareStatement(checkBrandQuery);
                PreparedStatement insertBrandPictureStmt = connection.prepareStatement(insertBrandPictureQuery)
            ) {
                // Skip the header row
                brandPictureReader.readNext();
                
                int counter = 1;
                // Read and process each row
                String[] brandRow;
                while ((brandRow = brandPictureReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer brandPictureId = Integer.parseInt(brandRow[0]); 
                        Integer bPBrandId = Integer.parseInt(brandRow[1]);
                        String brandPictureName = brandRow[2];
                        String brandPictureLink = brandRow[4];
 



        
                        // Check if manufacturer ID exists
                        checkBrandStmt.setInt(1, bPBrandId);
                        try (ResultSet brandResultSet = checkBrandStmt.executeQuery()) {
                        	brandResultSet.next();
                            int brandExists = brandResultSet.getInt(1);
                            if (brandExists == 0) {
                                System.err.println("Model ID " + bPBrandId + " does not exist. Skipping item: " + brandPictureName);
                                continue; // Skip this brand
                            }
                        }

                        // Set parameters for the query
                        insertBrandPictureStmt.setInt(1, brandPictureId);
                        insertBrandPictureStmt.setString(2, brandPictureName);
                        insertBrandPictureStmt.setString(3, brandPictureLink);
                        insertBrandPictureStmt.setInt(4, bPBrandId);
          

                        // Execute the query
                        insertBrandPictureStmt.executeUpdate();
                        System.out.println("Inserted brand Picture: " + brandPictureName);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(",", brandRow));
                    }
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
