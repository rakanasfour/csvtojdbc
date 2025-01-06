package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class BrandCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the brand CSV file
        String brandCsvPath = "src/main/resources/brands.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader brandReader = new CSVReader(new FileReader(brandCsvPath))
        ) {
            // SQL queries
            String checkBrandQuery = "SELECT COUNT(*) FROM brands WHERE brand_name = ?";
            String checkManufacturerQuery = "SELECT COUNT(*) FROM manufacturers WHERE manufacturer_id = ?";
            String insertBrandQuery = "INSERT INTO brands (brand_id, brand_name, brand_description, brand_status, brand_manufacturer_id) VALUES (?, ?, ?, ?, ?)";

            try (
                PreparedStatement checkBrandStmt = connection.prepareStatement(checkBrandQuery);
                PreparedStatement checkManufacturerStmt = connection.prepareStatement(checkManufacturerQuery);
                PreparedStatement insertBrandStmt = connection.prepareStatement(insertBrandQuery)
            ) {
                // Skip the header row
                brandReader.readNext();

                // Read and process each row
                String[] brandRow;
                while ((brandRow = brandReader.readNext()) != null) {
                    try {
                        // Parse and map CSV columns
                        Integer brandId = Integer.parseInt(brandRow[0]); // Assuming 1st column is brand_id
                        Integer brandManufacturerId = Integer.parseInt(brandRow[1]); // Assuming 2nd column is manufacturer_id
                        String brandName = brandRow[2]; // Assuming 3rd column is brand_name
                        String brandDescription = brandRow[3]; // Assuming 4th column is brand_description
                        String brandStatus = "AVAILABLE"; // Default status

                        // Check if brand name already exists
                        checkBrandStmt.setString(1, brandName);
                        try (ResultSet brandResultSet = checkBrandStmt.executeQuery()) {
                            brandResultSet.next();
                            int brandExists = brandResultSet.getInt(1);
                            if (brandExists > 0) {
                                System.err.println("Duplicate brand name: " + brandName + ". Skipping...");
                                continue; // Skip this brand
                            }
                        }

                        // Check if manufacturer ID exists
                        checkManufacturerStmt.setInt(1, brandManufacturerId);
                        try (ResultSet manufacturerResultSet = checkManufacturerStmt.executeQuery()) {
                            manufacturerResultSet.next();
                            int manufacturerExists = manufacturerResultSet.getInt(1);
                            if (manufacturerExists == 0) {
                                System.err.println("Manufacturer ID " + brandManufacturerId + " does not exist. Skipping brand: " + brandName);
                                continue; // Skip this brand
                            }
                        }

                        // Set parameters for the query
                        insertBrandStmt.setInt(1, brandId);
                        insertBrandStmt.setString(2, brandName);
                        insertBrandStmt.setString(3, brandDescription);
                        insertBrandStmt.setString(4, brandStatus);
                        insertBrandStmt.setInt(5, brandManufacturerId);

                        // Execute the query
                        insertBrandStmt.executeUpdate();
                        System.out.println("Inserted brand: " + brandName);
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
