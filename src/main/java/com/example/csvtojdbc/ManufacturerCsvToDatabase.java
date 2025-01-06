package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class ManufacturerCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the CSV file
        String manufacturerCsvPath = "src/main/resources/manufacturers.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader manufacturerReader = new CSVReader(new FileReader(manufacturerCsvPath))
            		
        ) {
            // SQL query to check for duplicate manufacturer names
            String checkQuery = "SELECT COUNT(*) FROM manufacturers WHERE manufacturer_name = ?";
            // SQL query to insert data into the database
            String insertQuery = "INSERT INTO manufacturers (manufacturer_id,manufacturer_name, manufacturer_description, manufacturer_status) VALUES (?,?, ?, ?)";

            // Prepare the statements
            try (
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery)
            ) {
                // Read manufacturers
                String[] manufacturerRow = manufacturerReader.readNext();;
                while ((manufacturerRow = manufacturerReader.readNext()) != null) {
                	Integer manufacturerId = Integer.parseInt(manufacturerRow[0]);
                    String manufacturerName = manufacturerRow[2]; // Assuming the 3rd column contains the manufacturer name
                    String manufacturerDescription = "test";
                    String manufacturerStatus = "ACTIVE";

                    // Check for duplicate
                    checkStatement.setString(1, manufacturerName);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            System.out.println("Duplicate found for manufacturer: " + manufacturerName + ". Skipping...");
                            continue; // Skip to the next row
                        }
                    }

                    // Insert the manufacturer if not duplicate
                    insertStatement.setInt(1, manufacturerId);
                    insertStatement.setString(2, manufacturerName);
                    insertStatement.setString(3, manufacturerDescription);
                    insertStatement.setString(4, manufacturerStatus);
                    insertStatement.executeUpdate();
                    System.out.println("Inserted manufacturer: " + manufacturerName);
                }
            }

            System.out.println("Data processing completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
