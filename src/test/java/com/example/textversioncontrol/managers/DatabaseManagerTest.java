package com.example.textversioncontrol.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @BeforeAll
    public static void setUpBeforeClass() throws SQLException, ClassNotFoundException {
        DatabaseManager.createConnection();
        DatabaseManager.clear();
    }

    @AfterEach
    void tearDown() throws SQLException {
        VersionManager.clearTrackedFiles();
        DatabaseManager.clear();
    }

    @Test
    void insert() throws SQLException, FileNotFoundException {

        String fileNameTest = "fileNameTest";
        String directoryPathTest = "directoryPathTest";
        String copyPathTest = "copyPathTest";
        String trackingPathTest = "trackingPathTest";
        String repoPathTest = "repoPathTest";

        DatabaseManager.insert(fileNameTest,directoryPathTest, copyPathTest, trackingPathTest, repoPathTest);

        assertEquals(fileNameTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.FILE_NAME));
        assertEquals(directoryPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.DIRECTORY_PATHWAY));
        assertEquals(copyPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.COPY_PATHWAY));
        assertEquals(trackingPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.TRACKING_PATHWAY));
        assertEquals(repoPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.REPO_PATHWAY));
    }

    @Test
    void deleteRecord() throws SQLException {
        String fileNameTest = "fileNameTest";

        DatabaseManager.insert(fileNameTest,"test", "test", "test", "test");

        DatabaseManager.deleteRecord(fileNameTest);
        assertEquals(0, DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME).size());
    }

    @Test
    void clear() throws SQLException {

        DatabaseManager.insert("test1","test", "test", "test", "test");
        DatabaseManager.insert("test2","test", "test", "test", "test");

        DatabaseManager.clear();

        assertEquals(0, DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME).size());
    }

    @Test
    void getEntry() throws SQLException, FileNotFoundException {

        String fileNameTest = "fileNameTest";
        String directoryPathTest = "directoryPathTest";
        String copyPathTest = "copyPathTest";
        String trackingPathTest = "trackingPathTest";
        String repoPathTest = "repoPathTest";

        DatabaseManager.insert(fileNameTest,directoryPathTest, copyPathTest, trackingPathTest, repoPathTest);

        assertEquals(fileNameTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.FILE_NAME));
        assertEquals(directoryPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.DIRECTORY_PATHWAY));
        assertEquals(copyPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.COPY_PATHWAY));
        assertEquals(trackingPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.TRACKING_PATHWAY));
        assertEquals(repoPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.REPO_PATHWAY));

    }

    @Test
    void getEntries() throws SQLException {

        DatabaseManager.insert("test","test", "test", "test", "test");
        DatabaseManager.insert("test","test", "test", "test", "test");
        DatabaseManager.insert("test","test", "test", "test", "test");

        assertEquals(3, DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME).size());
    }

    @Test
    void updateEntry() throws SQLException, FileNotFoundException {

        String fileNameTest = "fileNameTest";
        String directoryPathTest = "directoryPathTest";
        String copyPathTest = "copyPathTest";
        String trackingPathTest = "trackingPathTest";
        String repoPathTest = "repoPathTest";

        DatabaseManager.insert(fileNameTest,directoryPathTest, copyPathTest, trackingPathTest, repoPathTest);
        copyPathTest += "1";
        DatabaseManager.updateEntry(fileNameTest, DatabaseManager.Columns.COPY_PATHWAY, copyPathTest);

        assertEquals(copyPathTest, DatabaseManager.getEntry(fileNameTest, DatabaseManager.Columns.COPY_PATHWAY));
    }
}