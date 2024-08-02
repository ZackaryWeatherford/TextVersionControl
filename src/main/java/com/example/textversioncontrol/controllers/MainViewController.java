package com.example.textversioncontrol.controllers;

import com.example.textversioncontrol.models.FileData;
import com.example.textversioncontrol.VersionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;

public class MainViewController     {

    public static SceneController sceneController = new SceneController();

    /** */
    @FXML
    TableView trackedFilesTable;

    /** */
    @FXML
    Button trackingButton;

    /** */
    @FXML
    TextField pathwayLineEdit;

    /** */
    public void trackFile(ActionEvent e){
        try {
            VersionManager.startTracking(pathwayLineEdit.getText());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

        populateTable();
    }

    /** */
    @FXML
    public void initialize() {

        // Define columns
        TableColumn<FileData, String> nameColumn = new TableColumn<>("File Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<FileData, String> lastEditColumn = new TableColumn<>("Last Edit");
        lastEditColumn.setCellValueFactory(new PropertyValueFactory<>("lastEdit"));

        TableColumn<FileData, String> pathwayColumn = new TableColumn<>("Pathway");
        pathwayColumn.setCellValueFactory(new PropertyValueFactory<>("pathway"));

        TableColumn<FileData, Void> openColumn = new TableColumn<>("Open");
        openColumn.setCellFactory(createButtonCellFactory());

        TableColumn<FileData, Void> closeColumn = new TableColumn<>("Close");
        closeColumn.setCellFactory(createCloseButtonCellFactory());

        // Add columns to table
        trackedFilesTable.getColumns().addAll(nameColumn, lastEditColumn, pathwayColumn, openColumn, closeColumn);

        // Populate table with data in textfile_repos
        populateTable();

        try {
            VersionManager.updateTextFiles();
        }
        catch(Exception e){
            e.printStackTrace();
        }


        try {
            VersionManager.getCommitDates("discord_backup_codes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /** */
    public void populateTable() {

        // Clear tables items
        trackedFilesTable.getItems().clear();

        // Get file names
        ArrayList<String> fileNames = VersionManager.getTextFileNames();
        ArrayList<String> filePathways = null;
        ArrayList<String> lastEdits = null;

        // Get file pathways
        try {
            filePathways = VersionManager.getPathways();
            lastEdits = VersionManager.getLastEditDates();
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        // Populate Rows
        for(int i = 0; i < fileNames.size(); i++){
            trackedFilesTable.getItems().add(new FileData(fileNames.get(i), lastEdits.get(i), filePathways.get(i)));
        }

    }

    /** Create Open Button*/
    private Callback<TableColumn<FileData, Void>, TableCell<FileData, Void>> createButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<FileData, Void> call(TableColumn<FileData, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Open");

                    {
                        button.setOnAction(event -> {
                            FileData data = getTableView().getItems().get(getIndex());
                            System.out.println("Button clicked for file: " + data.getFileName());
                            try {
                                sceneController.switchToEditHistory(event, data.getFileName());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
            }
        };
    }

    /** Create Delete button*/
    private Callback<TableColumn<FileData, Void>, TableCell<FileData, Void>> createCloseButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<FileData, Void> call(TableColumn<FileData, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Close");
                    {
                        button.setOnAction(event -> {
                            FileData data = getTableView().getItems().get(getIndex());
                            System.out.println("Button clicked for file: " + data.getFileName());
                            // Implement your button action here
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
            }
        };
    }


    // Read and write objects
        /*
        writer = new BufferedWriter(new FileWriter(copiedFile));
        Scanner reader = new Scanner(textFile);


        // Copy content of copied file to
        while(reader.hasNextLine()){
            writer.write(reader.nextLine());
        }

        //
        writer.close();
        reader.close();


        Iterable<RevCommit> commits = git.log().call();

        // Iterate over the commits
        for (RevCommit commited : commits) {
            System.out.println("Commit: " + commited.getName());
            System.out.println("Author: " + commited.getAuthorIdent().getName());
            System.out.println("Date: " + commited.getAuthorIdent().getWhen());
            System.out.println("Message: " + commited.getFullMessage());
            System.out.println("----------------------------------");
        }

        */

}
