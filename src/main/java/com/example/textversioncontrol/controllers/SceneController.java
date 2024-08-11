package com.example.textversioncontrol.controllers;

import com.example.textversioncontrol.TextFileTracker;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class SceneController {

    /**
     * Changes the current scene to the <code>EditHistory.fxml</code> file.
     *
     * @param event used for getting the current window stage
     * @param fileName the file to load into the window
     * @throws IOException if an I/O error occurs
     */
    public static void switchToEditHistory(ActionEvent event, String fileName) throws IOException {
        EditHistoryController.fileName = fileName;

        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("EditHistory.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Changes the current scene to the <code>MainView.fxml</code> file.
     *
     * @param event used for getting the current window stage
     * @throws IOException if an I/O error occurs
     */
    public static void switchToMain(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

}