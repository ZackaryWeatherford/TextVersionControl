package com.example.textversioncontrol;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.managers.VersionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;

/**
 * The <code>TextFileTracker</code> class is the starting point of the application and loads the main view of the application.
 */
public class TextFileTracker extends Application {

    /**
     * The <code>start</code> method is the main entry point of the JavaFX application and
     * loads the main view window from the MainView.fxml file.
     *
     * @param stage the primary stage (top-level container)
     * @throws IOException if an I/O error occurs while loading fxml.ui file
     */
    @Override
    public void start(Stage stage) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
        stage.getIcons().add(new Image(String.valueOf(Paths.get("").toAbsolutePath().resolve("src/main/resources/images/control_icon.png"))));
    }

    /**
     * The <code>main</code> method launches the JavaFX application class,
     * establishes the connection to the database, and checks for changes in text files.
     *
     * @param args the argument list for the main method
     * @throws SQLException if connection to the database couldn't be made in <code>DatabaseManager.createConnection()</code>.
     * @throws ClassNotFoundException if JDBC driver can't be found in <code>DatabaseManager.createConnection()</code>.
     * @throws GitAPIException if in <code>VersionManager.updateTextFiles</code> JGit couldn't connect or write to repository.
     * @throws IOException if an I/O error occurs in <code>VersionManager.updateTextFiles()</code>.
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, GitAPIException, IOException {
        DatabaseManager.createConnection();
        VersionManager.updateTextFiles();
        launch();
    }
}