package com.example.textversioncontrol.managers;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

/**
 * The <code>DatabaseManager</code> class manages connecting, editing, and reading the FilesPathways database.
 */
public class DatabaseManager {

    /** Commands for what pathway to extract from the database. */
    public enum Pathways {DIRECTORY_PATHWAY, COPY_PATHWAY, TRACKING_PATHWAY, REPO_PATHWAY}

    /** Connection to SQL dateabase */
    public static Connection connection = null;

    /**
     *  Connects the sql connection object to the database.
     *  Must be called at least once for every other method in class to work.
     *
     * @throws ClassNotFoundException if the name of the class can't be found
     * @throws SQLException if connection to the database couldn't be made
     */
    public static void createConnection() throws ClassNotFoundException, SQLException {
        // Configure the url to SQLite database
        String url = "jdbc:sqlite:" + Paths.get("").toAbsolutePath().resolve("src/main/resources/FilePathways.db");
        url = url.replace("\\","/");

        // Connect to database
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(url);
    }

    /**
     * Add table columns to the database pathways table for file information in case they are deleted.
     *
     * @throws SQLException if database access error occurs
     */
    public static void setColumns() throws SQLException {

        // SQL query command to be executed
        String query = "CREATE TABLE IF NOT EXISTS pathways ("
                + "	file_name text NOT NULL,"
                + "	directory_pathway text NOT NULL,"
                + "	copy_pathway text NOT NULL,"
                + "	tracking_pathway text NOT NULL,"
                + "	repo_pathway text NOT NULL"
                + ");";

        // Execute the query statement to add table columns
        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (Exception e) {
            throw new SQLException();
        }
    }

    /**
     * Inserts a new row of file data into the database with the passed file arguments.
     *
     * @param fileName the name of the directory storing the copied file and .git file.
     * @param directoryPathway the pathway to the directory.
     * @param copyPathway the pathway to the copied text file.
     * @param trackingPathway the pathway to the file that is being tracked for changes.
     * @param repoPathway the pathway to git repository within the directory.
     * @throws SQLException if database access error occurs during the creation or execution of the prepared statement.
     */
    public static void insert(String fileName, String directoryPathway, String copyPathway, String trackingPathway, String repoPathway) throws SQLException {

        // Query statement
        String query = "INSERT INTO pathways (file_name, directory_pathway, copy_pathway, tracking_pathway, repo_pathway) VALUES (?, ?, ?, ?, ?)";

        // Create the prepared statement
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        // Insert values into statement
        preparedStatement.setString(1, fileName);
        preparedStatement.setString(2, directoryPathway);
        preparedStatement.setString(3, copyPathway);
        preparedStatement.setString(4, trackingPathway);
        preparedStatement.setString(5, repoPathway);

        // Execute statement
        preparedStatement.executeUpdate();
    }

    /**
     * Delete a file's information from the pathway's table
     *
     * @param fileName the name of the file to be deleted from the table.
     * @throws SQLException if SQL database access error occurs during the creation or execution of the prepared statement.
     */
    public static void deleteFile(String fileName) throws SQLException {

        // Query statement
        String query = "DELETE FROM pathways WHERE file_name = ?";

        // Create prepared statement
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        // Insert fileName into statement
        preparedStatement.setString(1, fileName);

        // Execute statement
        preparedStatement.executeUpdate();
    }

    /**
     * Delete all file info from database table.
     *
     * @throws SQLException if database access error occurs during creation or execution of statement.
     */
    public static void clear() throws SQLException {

        // Query Statement
        String query = "DELETE FROM pathways";

        // Create Statement
        Statement statement = connection.createStatement();

        // Execute Query
        statement.execute(query);
    }


    /**
     * Returns a pathway from the table based on the pathway argument passed.
     *
     * @param fileName the file to extract pathway from.
     * @param pathway the pathway to be extracted from SQL table.
     * @throws SQLException if the prepared statement or result set have trouble accessing the database.
     * @throws FileNotFoundException if no entry with fileName was found.
     * @return the requested pathway in the form of a String.
     */
    public static String getPathway(String fileName, Pathways pathway) throws SQLException, FileNotFoundException {

        // Pathway based on what was Pathways value was passed
        String requestedPathway = switch (pathway) {
            case DIRECTORY_PATHWAY -> "directory_pathway";
            case COPY_PATHWAY -> "copy_pathway";
            case TRACKING_PATHWAY -> "tracking_pathway";
            case REPO_PATHWAY -> "repo_pathway";
        };

        // Query statement
        String query = "SELECT " + requestedPathway +  " from pathways WHERE file_name = ?";

        // Create the prepared statement and add fileName to it
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, fileName);

        // Get the result set of the query
        ResultSet resultSet = preparedStatement.executeQuery();

        // Get the directory_pathway from the first result
        while(resultSet.next()) {
            return resultSet.getString(requestedPathway);
        }

        // Throw exception if no entries were found
        throw new FileNotFoundException("fileName was not found in SQL table");
    }


    /**
     * Retrieves all entries in the file_name columns of the table.
     *
     * @throws SQLException if SQL statement or result set have trouble accessing the database.
     * @return a list of all file names in table.
     */
    public static ArrayList<String> getFileNames() throws SQLException {

        // List of file names
        ArrayList<String> fileNames = new ArrayList<>();

        // Query statement
        String query = "SELECT file_name FROM pathways";

        // Create statement
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        // Loop through table and add file_name to fileNames list
        while(resultSet.next())
            fileNames.add(resultSet.getString("file_name"));

        return fileNames;
    }

    /** */
    public static ArrayList<String> get

}