package com.example.textversioncontrol.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

/**
 * The <code>DatabaseManager</code> class manages the editing and reading of files and directories.
 *
 * */
public class DatabaseManager {

    public enum Pathways {DIRECTORY_PATHWAY, COPY_PATHWAY, TRACKING_PATHWAY, REPO_PATHWAY}

    /** */
    public static Path getTextReposPath(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/textfile_repos");
    }

    /**
     * The <code>grabPathway</code> method extracts the pathway for the tracked file within the info folder from the provided directory.
     *
     * @param directory the directory to extract pathway from info
     * @throws FileNotFoundException if the info file is not found within the directory
     * @return the pathway the directory is tracking
     */
    public static String grabPathway(File directory) throws FileNotFoundException {
        // List of files within directory
        File[] filesList = directory.listFiles();

        // File reader
        Scanner reader;

        // Loop through the files within the directory
        for(File file : filesList){
            // If the file is a directory with the name "info"
            if(file.isDirectory() && file.getName().equals("info")) {
                // Read the first file within file directory and return the first line
                reader = new Scanner(Objects.requireNonNull(file.listFiles())[0]);
                String pathway = reader.nextLine();
                reader.close();
                return pathway;
            }
        }

        // If info file is not found throw exception
        throw new FileNotFoundException();
    }


    /** */
    public static Connection sql = null;

    /** */
    public static void createConnection() throws ClassNotFoundException, SQLException {
        String url = "jdbc:sqlite:" + Paths.get("").toAbsolutePath().resolve("src/main/resources/FilePathways.db");
        url = url.replace("\\","/");
        Class.forName("org.sqlite.JDBC");
        sql = DriverManager.getConnection(url);
    }

    /**
     *
     *
     */
    public static void insert(String fileName, String directoryPathway, String copyPathway, String trackingPathway, String repoPathway) throws SQLException {

        // Query statement
        String query = "INSERT INTO pathways (file_name, directory_pathway, copy_pathway, tracking_pathway, repo_pathway) VALUES (?, ?, ?, ?, ?)";

        // Create prepared statement
        PreparedStatement preparedStatement = sql.prepareStatement(query);

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
     *
     */
    public static void delete(String fileName) throws SQLException {

        // Query Statement
        String query = "DELETE FROM pathways WHERE file_name = ?";

        // Create prepared statement
        PreparedStatement preparedStatement = sql.prepareStatement(query);

        // Insert fileName into statement
        preparedStatement.setString(1, fileName);

        // Execute Statement
        preparedStatement.executeUpdate();
    }

    /** */
    public static void clear() throws SQLException {

        // Query Statement
        String query = "DELETE FROM pathways";

        // Create Statement
        Statement statement = sql.createStatement();

        // Execute Query
        statement.execute(query);
    }

    /**
     *
     */
    public static ArrayList<String> getFileNames() throws SQLException {

        // List of file names
        ArrayList<String> fileNames = new ArrayList<>();

        // Query statement
        String query = "SELECT file_name FROM pathways";

        // Create statement
        Statement statement = sql.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        // Loop through table and add file_name to fileNames list
        while(resultSet.next())
            fileNames.add(resultSet.getString("file_name"));

        return fileNames;
    }

    /** */
    public static String getPathway(String fileName, Pathways pathway) throws SQLException{

        //
        String pathwayQuery = null;

        //
        switch(pathway){
            case DIRECTORY_PATHWAY:
                pathwayQuery = "directory_pathway";
                break;
            case COPY_PATHWAY:
                pathwayQuery = "copy_pathway";
                break;
            case TRACKING_PATHWAY:
                pathwayQuery = "tracking_pathway";
                break;
            case REPO_PATHWAY:
                pathwayQuery = "repo_pathway";
                break;
        }

        // Query Statement
        String query = "SELECT " + pathwayQuery +  " from pathways WHERE file_name = ?";

        // Set prepared statement and add fileName to it
        PreparedStatement preparedStatement = sql.prepareStatement(query);
        preparedStatement.setString(1, fileName);

        // Get the result of the query
        ResultSet resultSet = preparedStatement.executeQuery();

        // Get the directory_pathway from the first result
        while(resultSet.next()) {
            return resultSet.getString(pathwayQuery);
        }

        return null;

    }


    /**
     *  Database
     *  - File Name
     *  - Directory pathway
     *  - Directory file tracking pathway
     *  - Directory repo pathway
     *
     *  - Directory = .git and text file
     *   */


}
