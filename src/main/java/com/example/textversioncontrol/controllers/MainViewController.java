package com.example.textversioncontrol.controllers;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.models.FileData;
import com.example.textversioncontrol.managers.VersionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The <code>MainViewController</code> class handles the MainView events and loading data to the screen.
 *
 */
public class MainViewController     {

    /** Used to control what screens are currently loaded to stage */
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

    /**
     * Set table columns and populate the table.
     */
    @FXML
    public void initialize() {

        // Define columns
        TableColumn<FileData, String> nameColumn = new TableColumn<>("File Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<FileData, String> lastEditColumn = new TableColumn<>("Last Edit");
        lastEditColumn.setCellValueFactory(new PropertyValueFactory<>("lastEdit"));

        TableColumn<FileData, String> pathwayColumn = new TableColumn<>("Pathway");
        pathwayColumn.setCellValueFactory(new PropertyValueFactory<>("trackedPathway"));

        TableColumn<FileData, Void> openColumn = new TableColumn<>("Open");
        openColumn.setCellFactory(createButtonCellFactory());

        TableColumn<FileData, Void> closeColumn = new TableColumn<>("Delete");
        closeColumn.setCellFactory(createDeleteButtonCellFactory());

        // Add columns to table
        trackedFilesTable.getColumns().addAll(nameColumn, lastEditColumn, pathwayColumn, openColumn, closeColumn);

        // Update files being currently tracked and populate the table
        try {
            //VersionManager.updateTextFiles();
            //DatabaseManager.clean();
            //DatabaseManager.updateEntry("test", DatabaseManager.Columns.DIRECTORY_PATHWAY, "C:\\Users\\katie\\Code\\Java Projects\\TextVersionControl\\src\\main\\resources\\TrackedFiles\\test");
            //DatabaseManager.resolvePathways();
            //VersionManager.stopTracking("test");

            populateTable();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /** */
    public void populateTable() throws SQLException, IOException {

        // Clear tables items
        trackedFilesTable.getItems().clear();

        // Lists of file data to be displayed on table
        ArrayList<String> fileNames = DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME);
        ArrayList<String> trackedPathways = DatabaseManager.getEntries(DatabaseManager.Columns.TRACKING_PATHWAY);
        ArrayList<String> lastEdits = VersionManager.getLastEditDates();

        // Populate Rows
        for(int i = 0; i < fileNames.size(); i++)
            trackedFilesTable.getItems().add(new FileData(fileNames.get(i), lastEdits.get(i), trackedPathways.get(i)));
    }

    /** */
    public void trackFile(ActionEvent e){
        // Start tracking file and repopulate the table
        try {
            VersionManager.startTracking(pathwayLineEdit.getText());
            populateTable();
        }
        catch(Exception ex){
            ex.printStackTrace();
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
    private Callback<TableColumn<FileData, Void>, TableCell<FileData, Void>> createDeleteButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<FileData, Void> call(TableColumn<FileData, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Delete");
                    {
                        button.setOnAction(event -> {
                            FileData data = getTableView().getItems().get(getIndex());
                            System.out.println("Button clicked for file: " + data.getFileName());
                            try {
                                VersionManager.stopTracking(data.getFileName());
                                populateTable();
                            } catch (SQLException | IOException e) {
                                throw new RuntimeException(e);
                            }
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
