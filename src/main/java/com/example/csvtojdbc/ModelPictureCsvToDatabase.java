package com.example.csvtojdbc;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.opencsv.CSVReader;

public class ModelPictureCsvToDatabase {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://web-api-db.cimwiuozxitn.us-east-1.rds.amazonaws.com:3306/web_api_base";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Password2025";

    public static void main(String[] args) {
        // Path to the model CSV file
        String modelPictureCsvPath = "src/main/resources/modelpictures.csv";

        try (
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Open the CSV file
            CSVReader modelPictureReader = new CSVReader(new FileReader(modelPictureCsvPath))
        ) {
            // SQL queries
         
            String checkModelQuery = "SELECT COUNT(*) FROM models WHERE model_id = ?";
            String insertModelPictureQuery = "INSERT INTO model_pictures (model_picture_id, model_picture_name,model_picture_link, m_p_model_id) VALUES (?, ?, ?,?)";

            try (
               
                PreparedStatement checkModelStmt = connection.prepareStatement(checkModelQuery);
                PreparedStatement insertModelPictureStmt = connection.prepareStatement(insertModelPictureQuery)
            ) {
                // Skip the header row
                modelPictureReader.readNext();
                
       
                // Read and process each row
                String[] modelRow;
                while ((modelRow = modelPictureReader.readNext()) != null) {
                	
                    try {
                        // Parse and map CSV columns
                        Integer modelPictureId = Integer.parseInt(modelRow[0]); 
                        Integer mPModelId = Integer.parseInt(modelRow[1]);
                        String modelPictureName = modelRow[2];
                        String modelPictureLink = modelRow[4];
 



        
                        // Check if manufacturer ID exists
                        checkModelStmt.setInt(1, mPModelId);
                        try (ResultSet modelResultSet = checkModelStmt.executeQuery()) {
                        	modelResultSet.next();
                            int modelExists = modelResultSet.getInt(1);
                            if (modelExists == 0) {
                                System.err.println("Model ID " + mPModelId + " does not exist. Skipping item: " + modelPictureName);
                                continue; // Skip this model
                            }
                        }

                        // Set parameters for the query
                        insertModelPictureStmt.setInt(1, modelPictureId);
                        insertModelPictureStmt.setString(2, modelPictureName);
                        insertModelPictureStmt.setString(3, modelPictureLink);
                        insertModelPictureStmt.setInt(4, mPModelId);
          

                        // Execute the query
                        insertModelPictureStmt.executeUpdate();
                        System.out.println("Inserted model Picture: " + modelPictureName);
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
