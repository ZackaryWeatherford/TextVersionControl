package com.example.textversioncontrol;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.managers.VersionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.sql.*;

/**
 * The <code>TextFileTracker</code> class is the starting point of the application and loads the main view of the application.
 */
public class TextFileTracker extends Application {

    /**
     * The <code>start</code> method is the main entry point of the JavaFX applicaiton.
     * Loads the main view window from the MainView.fxml file and creates connection to database.
     *
     * @param stage the primary stage (top-level container)
     * @throws IOException if an I/O error occurs while loading fxml file
     * */
    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException, GitAPIException {

        DatabaseManager.createConnection();
       // VersionManager.startTracking("C:\\Users\\zackd\\Downloads\\test.txt");

        try {
            //VersionManager.startTracking("C:\\Users\\zackd\\Downloads\\test.txt");
            //System.out.println(DatabaseManager.getEntry("test", DatabaseManager.Columns.FILE_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The <code>main</code> method launches the JavaFX application class.
     * */
    public static void main(String[] args) {
        launch();
    }
}