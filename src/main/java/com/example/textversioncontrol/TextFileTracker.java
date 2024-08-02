package com.example.textversioncontrol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

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