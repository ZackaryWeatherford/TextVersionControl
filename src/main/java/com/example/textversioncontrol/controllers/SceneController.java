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

    /** */
    private Scene scene;
    private Parent root;

    /** */
    public void switchToEditHistory(ActionEvent event, String fileName) throws IOException {
        EditHistoryController.fileName = fileName;

        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("EditHistory.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

    /** */
    public void switchToChanges(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("VersionChanges.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

    /** */
    public static void switchToMain(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextFileTracker.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 872, 872);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Text Version History");
        stage.setScene(scene);
        stage.show();
    }

}