package com.example.textversioncontrol.models;

import javafx.beans.property.SimpleStringProperty;

public class FileData {
    private final String fileName;
    private final String lastEdit;
    private final String pathway;

    public FileData(String fileName, String lastEdit, String pathway) {
        this.fileName = fileName;
        this.lastEdit = lastEdit;
        this.pathway = pathway;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastEdit() {
        return lastEdit;
    }

    public String getPathway() {
        return pathway;
    }


}

