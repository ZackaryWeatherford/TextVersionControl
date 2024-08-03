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

import java.io.FileNotFoundException;
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

    public static String fileName;


    /** */
    public void backToMain(ActionEvent e) throws IOException {
        SceneController.switchToMain(e);
    }

    /** */
    public void populateHistoryTable(){

        historyTable.getItems().clear();

        // Get history data
        ArrayList<String> dates;


    }

    /** */
    @FXML
    public void initialize() throws SQLException, IOException, GitAPIException {

        if(historyTable == null){
            System.out.println("table null");
            return;
        }

        historyTable.getItems().clear();

        // Define columns
        TableColumn<EditData, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EditData, String> datesColumn = new TableColumn<>("Date");
        datesColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<EditData, Void> revertColumn = new TableColumn<>("Revert To");
        revertColumn.setCellFactory(revertButtonFactory());

        TableColumn<EditData, Void> openColumn = new TableColumn<>("Open");
        openColumn.setCellFactory(openButtonFactory());

        // Add columns to table
        historyTable.getColumns().addAll(idColumn, datesColumn, revertColumn, openColumn);

        //
        populateTable();

        //
        fileNameLabel.setText("File Name: " + fileName);

        //
        currentPathwayField.setText(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.TRACKING_PATHWAY));
    }

    /** */
    public void populateTable() throws SQLException, IOException, GitAPIException {

        ArrayList<String> dates;

        // Get lists
        dates = VersionManager.getCommitDates(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.GIT_PATHWAY));

        // Populate Rows
        for(int i = 0; i < dates.size(); i++){
            historyTable.getItems().add(new EditData(i + 1, dates.get(i)));
        }

    }

    /** */
    public void changeTrackedPathway(){



    }
    /*
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


    /** Create Open Button*/
    private Callback<TableColumn<EditData, Void>, TableCell<EditData, Void>> openButtonFactory() {
        return new Callback<>() {
            @Override
            public TableCell<EditData, Void> call(TableColumn<EditData, Void> param) {
                return new TableCell<>() {
                    private final Button button = new Button("Open");

                    {
                        button.setOnAction(event -> {
                            EditData data = getTableView().getItems().get(getIndex());
                            System.out.println("Button clicked for file: ");
                            try {
                                System.out.println();
                            } catch (Exception e) {
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
                            System.out.println("Button clicked for file: " );
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
}
