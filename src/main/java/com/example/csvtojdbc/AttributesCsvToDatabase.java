package com.example.csvtojdbc;


import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

public class AttributesCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String brandCsvPath = "src/main/resources/attributes.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader attributeReader = new CSVReader(new FileReader(brandCsvPath))
        ) {
            // SQL queries
            String checkAttributeQuery = "SELECT COUNT(*) FROM attributes WHERE attribute_name = ?";
     
            String insertAttributeQuery = "INSERT INTO attributes (attribute_id, attribute_name, attribute_type, attribute_assist_text) VALUES (?, ?, ?, ?)";

            try (
                PreparedStatement checkAttributeStmt = connection.prepareStatement(checkAttributeQuery);
                PreparedStatement insertAttributeStmt = connection.prepareStatement(insertAttributeQuery)
            ) {
                // Skip the header row
            	attributeReader.readNext();

                // Read and process each row
                String[] attributesRow;
                while ((attributesRow = attributeReader.readNext()) != null) {
                    try {
                        // Parse and map CSV columns
                        Integer attributeId = Integer.parseInt(attributesRow[0]); 
                        String attributeName = attributesRow[1]; 
                        String attributeType = attributesRow[4].toUpperCase();
                        String attributeAssistText = attributesRow[6]; 
                        
                        //Converting attributeType
                       if ( attributeType =="TEXTBOX") {
                    	   attributeType="TASTE";
                       }
                       else if (attributeType =="CHECKBOX") {
                    	   attributeType="SIZE";
                       }
                       else {
                    	   attributeType="LOCTION";
                       }
       

                        // Check if brand name already exists
                        checkAttributeStmt.setString(1, attributeName);
                        try (ResultSet attributeResultSet = checkAttributeStmt.executeQuery()) {
                        	attributeResultSet.next();
                            int attributeExists = attributeResultSet.getInt(1);
                            if (attributeExists > 0) {
                                System.err.println("Duplicate attribute name: " + attributeName + ". Skipping...");
                                continue; // Skip this brand
                            }
                            
                           // List<String> validAttributeTypes = Arrays.asList("TEXTBOX", "CHECKBOX", "DROPDOWN");
                            List<String> validAttributeTypes = Arrays.asList("TASTE", "SIZE", "LOCTION");

                            if (!validAttributeTypes.contains(attributeType.toUpperCase())) {
                                System.err.println("Invalid attribute type: " + attributeType);
                                continue; // Skip this row
                            }
                        }

                        // Set parameters for the query
                        insertAttributeStmt.setInt(1, attributeId);
                        insertAttributeStmt.setString(2, attributeName);
                        insertAttributeStmt.setString(3, attributeType.toUpperCase());
                        insertAttributeStmt.setString(4, attributeAssistText);
                      

                        // Execute the query
                        insertAttributeStmt.executeUpdate();
                        System.out.println("Inserted attribute: " + attributeName);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid row: " + String.join(",", attributesRow));
                    }
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
