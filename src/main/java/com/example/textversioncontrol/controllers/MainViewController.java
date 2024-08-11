package com.example.textversioncontrol.controllers;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.models.FileData;
import com.example.textversioncontrol.managers.VersionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * The <code>MainViewController</code> class handles the MainView events and loading data to the screen.
 */
public class MainViewController     {

    /** Used to control what screens are currently loaded to stage */
    public static SceneController sceneController = new SceneController();

    /** Table view showing what files are currently being tracked and updated*/
    @FXML
    TableView trackedFilesTable;

    /** Tries to track a new file when clicked*/
    @FXML
    Button trackingButton;

    /** Line of text that contains the pathway of new tracked file users enter. */
    @FXML
    TextField pathwayLineEdit;

    /**
     * Set table columns and populate the table.
     *
     * @throws SQLException if <code>DatabaseManager</code> fails to get records entries from the database.
     * @throws IOException if <code>VersionManager</code> fails to read commit dates from the git repositories.
     */
    @FXML
    public void initialize() throws SQLException, IOException {

        // Define column names and cell factories
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

        // Update table view
        populateTable();
    }

    /**
     * Populates <code>trackedFilesTable</code> with data from the database using the database manager.
     *
     * @throws SQLException if records from the database couldn't be retrieved
     * @throws IOException if a git repository fails to open
     */
    public void populateTable() throws SQLException, IOException{

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

    /**
     * Button event to call startTracking method from VersionManager.
     *
     * @throws GitAPIException if JGit couldn't connect or write to repository.
     * @throws SQLException if sql exception occurs
     * @throws IOException I/O error occurs
     */
    public void trackFile() throws GitAPIException, SQLException, IOException {
        // Start tracking file and repopulate the table to show changes
        VersionManager.startTracking(pathwayLineEdit.getText());
        populateTable();
    }

    /** Button event to check for changes and to update the table view. */
    public void checkForChanges() throws GitAPIException, SQLException, IOException {
        VersionManager.updateTextFiles();
        populateTable();
    }

    /** Button event to resolve file pathways*/
    public void resolvePathways() throws SQLException {
        DatabaseManager.resolvePathways();
    }

    /** Button event to clear files, database, and update the table view. */
    public void clear() throws SQLException, IOException {
        VersionManager.clearTrackedFiles();
        DatabaseManager.clear();
        populateTable();
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
                            // Get file data associated with row
                            FileData data = getTableView().getItems().get(getIndex());

                            try {
                                // Switch to edit history view
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
                            try {
                                VersionManager.stopTracking(data.getFileName());
                                populateTable();
                            } catch (SQLException | IOException e) {
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

}