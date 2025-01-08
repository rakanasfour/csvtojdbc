package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;


public class ModelCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String modelCsvPath = "src/main/resources/models.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader modelReader = new CSVReader(new FileReader(modelCsvPath))
        ) {
            // SQL queries
            String checkModelQuery = "SELECT COUNT(*) FROM models WHERE model_name = ?";
            String checkBrandQuery = "SELECT COUNT(*) FROM brands WHERE brand_id = ?";
            String insertModelQuery = "INSERT INTO models (model_id, model_name, model_description, model_brand_id ) VALUES (?, ?, ?, ?)";

            try (
                PreparedStatement checkModelstmt = connection.prepareStatement(checkModelQuery );
                PreparedStatement checkBrandstmt = connection.prepareStatement(checkBrandQuery);
                PreparedStatement insertModelStmt = connection.prepareStatement(insertModelQuery)
            ) {
                // Skip the header row
            	modelReader.readNext();

                // Read and process each row
                String[] modelRow;
                while ((modelRow = modelReader.readNext()) != null) {
                    try {
                        // Parse and map CSV columns
                        Integer modelId = Integer.parseInt(modelRow[0]);
                        String modelName = modelRow[2]; // Assuming 3rd column is brand_name
                        Integer modelBrandId = Integer.parseInt(modelRow[3]); // Assuming 2nd column is manufacturer_id
                        String modelDescription = modelRow[5]; // Assuming 4th column is brand_description
                        

                        // Check if brand name already exists
                        checkModelstmt.setString(1, modelName);
                        try (ResultSet modelResultSet = checkModelstmt.executeQuery()) {
                        	modelResultSet.next();
                            int modelExists = modelResultSet.getInt(1);
                            if (modelExists > 0) {
                                System.err.println("Duplicate model name: " + modelName + ". Skipping...");
                                continue; // Skip this brand
                            }
                        }

                        // Check if manufacturer ID exists
                        checkBrandstmt.setInt(1, modelBrandId);
                        try (ResultSet brandResultSet = checkBrandstmt.executeQuery()) {
                        	brandResultSet.next();
                            int brandExists = brandResultSet.getInt(1);
                            if (brandExists == 0) {
                                System.err.println("brand ID " + modelBrandId + " does not exist. Skipping brand: " + modelName);
                                continue; // Skip this brand
                            }
                        }

                        // Set parameters for the query
                        insertModelStmt.setInt(1, modelId);
                        insertModelStmt.setString(2, modelName);
                        insertModelStmt.setString(3, modelDescription);
                        insertModelStmt.setInt(4, modelBrandId);
                 

                        // Execute the query
                        insertModelStmt.executeUpdate();
                        System.out.println("Inserted model: " + modelName);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(",", modelRow));
                    }
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
