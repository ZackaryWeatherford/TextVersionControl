package com.example.textversioncontrol.models;

/**
 * The <code>FileData</code> class serves a data model for the table view in the <code>MainViewController</code>
 */
public class FileData {
    private final String fileName;
    private final String lastEdit;
    private final String trackedPathway;

    public FileData(String fileName, String lastEdit, String pathway) {
        this.fileName = fileName;
        this.lastEdit = lastEdit;
        this.trackedPathway = pathway;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastEdit() {
        return lastEdit;
    }

    public String getTrackedPathway() {
        return trackedPathway;
    }


}

