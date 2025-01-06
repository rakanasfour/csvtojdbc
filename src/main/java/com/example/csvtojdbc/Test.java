/*
package com.example.csvtojdbc;

package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class BrandCsvToDatabase2 {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Paths to the CSV files
        String brandCsvPath = "src/main/resources/brands.csv";
        String manufacturerCsvPath = "src/main/resources/manufacturers.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV files
            CSVReader brandReader = new CSVReader(new FileReader(brandCsvPath));
            CSVReader manufacturerReader = new CSVReader(new FileReader(manufacturerCsvPath))
        ) {
            // SQL query to insert data into the database
            String insertQuery = "INSERT INTO BrandManufacturer (brand_id,brand_name,brand_description,brand_status,"
            		+ "brand_manufacturer_id, brand_distributor_id) VALUES (?,?, ?,?,?,?)";

            // Prepare the statement
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                // Read brands and manufacturers
                String[] brandRow;
                String[] manufacturerRow;

                while ((brandRow = brandReader.readNext()) != null &&
                        (manufacturerRow = manufacturerReader.readNext()) != null) {
                    String brandName = brandRow[0]; // Assuming the first column contains the brand name
                    int manufacturerId = Integer.parseInt(manufacturerRow[0]); // Assuming the first column contains the manufacturer ID

                    // Set the parameters and execute the query
                    preparedStatement.setString(1, brandName);
                    preparedStatement.setInt(2, manufacturerId);
                    preparedStatement.executeUpdate();
                }
            }

            System.out.println("Data inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/