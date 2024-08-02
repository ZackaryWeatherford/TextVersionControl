package com.example.textversioncontrol;

import com.example.textversioncontrol.managers.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;

/**
 * The <code>TextFileTracker</code> class is the starting point of the application and loads the main view of the application.
 */
public class TextFileTracker extends Application {

    /**
     * The <code>start</code> method is the main entry point of the JavaFX applicaiton.
     * Loads the main view window from the MainView.fxml file and displays it to user.
     *
     * @param stage the primary stage (top-level container)
     * @throws IOException if an I/O error occurs while loading fxml file
     * */
    @Override
    public void start(Stage stage) throws IOException{
        createTable();
        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }



    public void createTable(){

        Connection c = null;

        String url = "jdbc:sqlite:" + Paths.get("").toAbsolutePath().resolve("src/main/resources/FilePathways.db");
        url = url.replace("\\","/");
        System.out.println(url);

        var sql = "CREATE TABLE IF NOT EXISTS pathways ("
                + "	file_name text NOT NULL,"
                + "	directory_pathway text NOT NULL,"
                + "	copy_pathway text NOT NULL,"
                + "	tracking_pathway text NOT NULL,"
                + "	repo_pathway text NOT NULL"
                + ");";

        var sql2 = "DROP TABLE warehouses";

        try {
            DatabaseManager.createConnection();
            //DatabaseManager.insert("test2", "test2","test3","test4","test5");
            //DatabaseManager.delete("test1");
            //System.out.println(DatabaseManager.getDirectoryPathway("test2"));
            //DatabaseManager.clear();
            //System.out.println(DatabaseManager.getCopyPathway("test2"));
            //System.out.println(DatabaseManager.getFileNames());
            //System.out.println(DatabaseManager.getTrackingPathway("test2"));
            //System.out.println(DatabaseManager.getPathway("test2", DatabaseManager.Pathways.TRACKING_PATHWAY));

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
            Statement a = c.createStatement();
            a.execute(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            e.printStackTrace();
            System.exit(1);
        }




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


    /**
     * The <code>main</code> method launches the JavaFX application class.
     * */
    public static void main(String[] args) {
        launch();
    }
}