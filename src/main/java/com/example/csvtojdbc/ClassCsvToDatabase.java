package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class ClassCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the CSV file
        String classCsvPath = "src/main/resources/classes.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader classReader = new CSVReader(new FileReader(classCsvPath))
            		
        ) {
            // SQL query to check for duplicate manufacturer names
            String checkQuery = "SELECT COUNT(*) FROM classes WHERE class_name = ?";
            // SQL query to insert data into the database
            String insertQuery = "INSERT INTO classes (class_id,class_name,) VALUES (?,?)";

            // Prepare the statements
            try (
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery)
            ) {
                // Read manufacturers
                String[] classRow = classReader.readNext();;
                while ((classRow = classReader.readNext()) != null) {
                	Integer classId = Integer.parseInt(classRow[0]);
                    String className = classRow[2]; // Assuming the 3rd column contains the manufacturer name
                

                    // Check for duplicate
                    checkStatement.setString(1, className);
                    try (ResultSet resultSet = checkStatement.executeQuery()) {
                        resultSet.next();
                        int count = resultSet.getInt(1);
                        if (count > 0) {
                            System.out.println("Duplicate found for class: " + className + ". Skipping...");
                            continue; // Skip to the next row
                        }
                    }

                    // Insert the manufacturer if not duplicate
                    insertStatement.setInt(1, classId);
                    insertStatement.setString(2, className);
   
                    insertStatement.executeUpdate();
                    System.out.println("Inserted manufacturer: " + className);
                }
            }

            System.out.println("Data processing completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
