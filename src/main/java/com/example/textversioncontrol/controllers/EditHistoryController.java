package com.example.textversioncontrol.controllers;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.managers.VersionManager;
import com.example.textversioncontrol.models.EditData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class EditHistoryController {

    @FXML
    TableView historyTable;

    @FXML
    Label fileNameLabel;

    @FXML
    TextField currentPathwayField;

    /** Name of the tracked file that is being viewed */
    public static String fileName;


    /** Button action to switch to the main screen */
    public void backToMain(ActionEvent e) throws IOException {
        SceneController.switchToMain(e);
    }

    /**
     *
     *
     */
    @FXML
    public void initialize() throws Exception {

        // Clear the table of all items
        historyTable.getItems().clear();

        // Define columns
        TableColumn<EditData, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EditData, String> datesColumn = new TableColumn<>("Date");
        datesColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<EditData, Void> revertColumn = new TableColumn<>("Revert To");
        revertColumn.setCellFactory(revertButtonFactory());

        // Add columns to table
        historyTable.getColumns().addAll(idColumn, datesColumn, revertColumn);

        // Populate table items with file history
        populateTable();

        // Set the file name label
        fileNameLabel.setText("File Name: " + fileName);

        // Set the current pathway of the tracked file being viewed
        currentPathwayField.setText(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.TRACKING_PATHWAY));
    }

    /**
     * Populates <code>historyTable</code> with ids, commit dates, and revert buttons.
     *
     * @throws SQLException if <code>DatabaseManager.getEntry(String, Columns)</code> fails to get the repo_pathway entry.
     * @throws IOException if <code>VersionManager.getCommitDates(String)</code> fails to open git repository.
     * @throws GitAPIException if git fails to get commit log in <code>VersionManager.getCommitDates(String)</code>.
     */
    public void populateTable() throws SQLException, IOException, GitAPIException {

        // Clear table of all items
        historyTable.getItems().clear();

        ArrayList<String> dates;

        // Get lists
        dates = VersionManager.getCommitDates(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.REPO_PATHWAY));

        // Populate Rows
        for(int i = 0; i < dates.size(); i++)
            historyTable.getItems().add(new EditData(i + 1, dates.get(i)));
    }

    /**
     * Button event to change the pathway of the file being tracked.
     *
     * @throws SQLException if <code>DatabaseManager</code> fails to retrieve and update entries.
     * @throws IOException if the file at the pathway doesn't exist.
     */
    @FXML
    public void changeTrackedPathway(ActionEvent e) throws SQLException, IOException {
        VersionManager.updatePathway(fileName, currentPathwayField.getText());
    }

    /** Create Delete button*/
    private Callback<TableColumn<EditData, Void>, TableCell<EditData, Void>> revertButtonFactory() {
        return new Callback<>() {
            @Override
            public TableCell<EditData, Void> call(TableColumn<EditData, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Revert");

                    {
                        button.setOnAction(event -> {
                            EditData data = getTableView().getItems().get(getIndex());
                            try {
                                String gitPathway = DatabaseManager.getEntry(fileName, DatabaseManager.Columns.REPO_PATHWAY);

                                VersionManager.revert(fileName, gitPathway, VersionManager.getCommitId(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.REPO_PATHWAY), data.getId()-1));
                            } catch (IOException | GitAPIException | SQLException e) {
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
