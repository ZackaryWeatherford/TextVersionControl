package com.example.textversioncontrol.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

/**
 * The <code>DatabaseManager</code> class manages connecting, editing, and reading of the FilesPathways database table.
 * The database pathways table consists of five columns: file_name, directory_pathway, copy_pathway, tracking_pathway, git_pathway.
 */
public class DatabaseManager {

    /** Commands for what pathway to extract from the database. */
    public enum Columns {FILE_NAME, DIRECTORY_PATHWAY, COPY_PATHWAY, TRACKING_PATHWAY, REPO_PATHWAY}

    /** Connection to the SQL database */
    public static Connection connection = null;

    /**
     *  Establishes a connection with the SQL database through the <code>connection</code> object.
     *  Must be called before using any other method in class.
     *
     * @throws ClassNotFoundException if JDBC driver can't be found
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
     * Five columns are added: file_name, directory_pathway, copy_pathway, tracking_pathway, repo_pathway.
     *
     * @throws SQLException if database access error occurs
     */
    public static void createColumns() throws SQLException {

        // SQL query command to be executed
        String query = "CREATE TABLE IF NOT EXISTS pathways ("
                + "	file_name text NOT NULL,"
                + "	directory_pathway text NOT NULL,"
                + "	copy_pathway text NOT NULL,"
                + "	tracking_pathway text NOT NULL,"
                + "	repo_pathway text NOT NULL"
                + ");";

        // Execute the query statement to add table columns
        try(Statement statement = connection.createStatement();){
            statement.execute(query);
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
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Insert values into statement
            preparedStatement.setString(1, fileName);
            preparedStatement.setString(2, directoryPathway);
            preparedStatement.setString(3, copyPathway);
            preparedStatement.setString(4, trackingPathway);
            preparedStatement.setString(5, repoPathway);

            // Execute statement
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Delete a file's information from the pathway's table
     *
     * @param fileName the name of the file to be deleted from the table.
     * @throws SQLException if SQL database access error occurs during the creation or execution of the prepared statement.
     */
    public static void deleteRecord(String fileName) throws SQLException {

        // Query statement
        String query = "DELETE FROM pathways WHERE file_name = ?";

        // Create the prepared statement
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Insert fileName into statement
            preparedStatement.setString(1, fileName);

            // Execute statement
            preparedStatement.executeUpdate();
        }
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
        try(Statement statement = connection.createStatement()){
            // Execute Query
            statement.execute(query);
        }
    }

    /**
     * Removes all empty records from the database pathways table.
     *
     * @throws SQLException if connection fails to create statement or executing query fails.
     */
    public static void clean() throws SQLException {

        // Query statement
        String query = "DELETE FROM pathways WHERE file_name IS NULL;";

        // Execute statement
        try(Statement statement = connection.createStatement()){
            statement.executeUpdate(query);
        }
    }

    /**
     * Returns a entry based on <code>fileName</code> and <code>column</code> arguments.
     *
     * @param fileName the file to extract pathway from.
     * @param column the pathway to be extracted from SQL table.
     * @throws SQLException if the prepared statement or result set have trouble accessing the database.
     * @throws FileNotFoundException if no entry with fileName was found.
     * @return the value of the entry
     */
    public static String getEntry(String fileName, Columns column) throws SQLException, FileNotFoundException {

        // Pathway based on what was Columns value was passed
        String requestedColumn = switch (column) {
            case FILE_NAME -> "file_name";
            case DIRECTORY_PATHWAY -> "directory_pathway";
            case COPY_PATHWAY -> "copy_pathway";
            case TRACKING_PATHWAY -> "tracking_pathway";
            case REPO_PATHWAY -> "repo_pathway";
        };

        // Query statement
        String query = "SELECT " + requestedColumn +  " FROM pathways WHERE file_name = ?";

        // Initialize result set
        ResultSet resultSet = null;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            // Insert filename into statement
            preparedStatement.setString(1, fileName);

            // Get the result set of the query
            resultSet = preparedStatement.executeQuery();

            // Return the first result from the result set
            return resultSet.getString(requestedColumn);
        }
        finally{
            if(resultSet != null)
                resultSet.close();
        }
    }

    /**
     * Returns all the entries from a column from the table based on the column argument passed.
     *
     * @param column the pathway to be extracted from SQL table.
     * @throws SQLException if the prepared statement or result set have trouble accessing the database.
     * @return list of all found entries for that column
     */
    public static ArrayList<String> getEntries(Columns column) throws SQLException {

        // Store retrieved entries from database
        ArrayList<String> entries = new ArrayList<>();

        // Column based on what column value was given
        String requestedPathway = switch (column) {
            case FILE_NAME ->  "file_name";
            case DIRECTORY_PATHWAY -> "directory_pathway";
            case COPY_PATHWAY -> "copy_pathway";
            case TRACKING_PATHWAY -> "tracking_pathway";
            case REPO_PATHWAY -> "repo_pathway";
        };

        // Query statement
        String query = "SELECT " + requestedPathway +  " FROM pathways";

        // Create the statement and extract the result set from the execution
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {

            // Extract entries from the result set
            while (resultSet.next())
                entries.add(resultSet.getString(requestedPathway));
        }

        return entries;
    }

    /**
     * Updates an entry within a record with a new value using the specified parameters.
     *
     * @param fileName the record to change the cell of
     * @param column the column within the record to change
     * @param value the value to put in the record's cell
     * @throws SQLException if database access error occurs
     */
    public static void updateEntry(String fileName, Columns column, String value) throws SQLException {

        // Convert column to string
        String requestedColumn = column.toString().toLowerCase();

        // Create query statement
        String query = "UPDATE pathways SET " + requestedColumn + " = ? " +  "WHERE file_name = ?";

        // Loading and executing the prepared statement
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Load values into statement
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, fileName);

            // Execute query
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Checks if directory, copy, and repository pathways lead to valid files and resolve any of the pathways that don't
     *
     * @throws SQLException if DatabaseManager encounters error attempting to get column data from the database
     */
    public static void resolvePathways() throws SQLException {

        // List of database columns
        ArrayList<String> fileNames = DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME);
        ArrayList<String> directoryPathways = DatabaseManager.getEntries(DatabaseManager.Columns.DIRECTORY_PATHWAY);
        ArrayList<String> copyPathways = DatabaseManager.getEntries(DatabaseManager.Columns.COPY_PATHWAY);
        ArrayList<String> repoPathways = DatabaseManager.getEntries(DatabaseManager.Columns.REPO_PATHWAY);

        // Resolve invalid directory pathways
        for(int i = 0; i < directoryPathways.size(); i++) {
            String pathway = directoryPathways.get(i);
            if (!new File(pathway).exists()) {
                try {
                    String resolvedPathway = Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles/" + Paths.get(pathway).getFileName()).toString();
                    DatabaseManager.updateEntry(fileNames.get(i), DatabaseManager.Columns.DIRECTORY_PATHWAY, resolvedPathway);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        // Resolve invalid copy pathways
        for(int i = 0; i < copyPathways.size(); i++) {
            String pathway = copyPathways.get(i);
            if (!new File(pathway).exists()) {
                try{
                    String resolvedPathway = Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles/" + Paths.get(pathway).getFileName()).toString();
                    DatabaseManager.updateEntry(fileNames.get(i), DatabaseManager.Columns.COPY_PATHWAY, resolvedPathway);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        // Resolve invalid repo pathways
        for(int i = 0; i < repoPathways.size(); i++) {
            String pathway = repoPathways.get(i);
            if (!new File(pathway).exists()) {
                try{
                    String resolvedPathway = Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles/" + Paths.get(pathway).getFileName()).toString();
                    DatabaseManager.updateEntry(fileNames.get(i), DatabaseManager.Columns.COPY_PATHWAY, resolvedPathway);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }
}