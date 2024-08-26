package com.example.textversioncontrol.tests;

import com.example.textversioncontrol.managers.DatabaseManager;
import com.example.textversioncontrol.managers.VersionManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class VersionManagerTest {

    @BeforeAll
    public static void setUpBeforeClass() throws SQLException, ClassNotFoundException {
        DatabaseManager.createConnection();
        VersionManager.clearTrackedFiles();
        DatabaseManager.clear();
    }

    @BeforeEach
    void setUp(){

    }

    @AfterEach
    void tearDown() throws SQLException {
        VersionManager.clearTrackedFiles();
        DatabaseManager.clear();
    }

    @Test
    void startTracking() throws GitAPIException, SQLException, IOException {
        VersionManager.startTracking(String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt")));

        // Check to see if file has been added to tracked files directory
        File directory = new File(String.valueOf(VersionManager.getTrackedFilesPathway()));
        assertEquals(1, Objects.requireNonNull(directory.listFiles()).length);

        // Check if the database stored the new tracked file
        assertEquals(1, DatabaseManager.getEntries(DatabaseManager.Columns.FILE_NAME).size());
    }

    @Test
    void updateTextFiles() throws IOException, GitAPIException, SQLException {

        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));
        String testText = "test" + Math.random();

        VersionManager.startTracking(filePath);

        // Overwrites test.txt
        try(FileWriter writer = new FileWriter(filePath)){
            writer.write(testText);
        }
        VersionManager.updateTextFiles();

        File storedFile = new File(VersionManager.getTrackedFilesPathway() + "\\test");

        // Assert only 1 file is in directory and the stored file exists
        assertEquals(1, Objects.requireNonNull(new File(String.valueOf(VersionManager.getTrackedFilesPathway())).listFiles()).length);
        assertTrue(storedFile.exists());

        // Assert that the file is updated and reflects new text
        try(BufferedReader br = new BufferedReader(new FileReader(new File(VersionManager.getTrackedFilesPathway() + "\\test" +"\\test.txt")))){
            assertEquals(testText, br.readLine());
        } finally {
            VersionManager.clearTrackedFiles();
            DatabaseManager.clear();
        }
    }

    @Test
    void commitTwoChanges() throws GitAPIException, SQLException, IOException {

        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));
        String testText = "test" + Math.random();
        VersionManager.startTracking(filePath);

        // Overwrites test.txt
        try(FileWriter writer = new FileWriter(filePath)){
            writer.write(testText);
        }
        VersionManager.updateTextFiles();

        String gitPathway = DatabaseManager.getEntry("test", DatabaseManager.Columns.REPO_PATHWAY);
        Git git = Git.open(new File(gitPathway));

        Iterable<RevCommit> commits = git.log().call();
        int count = 0;
        for( RevCommit commit : commits ) {
            count++;
        }

        assertEquals(2, count);
    }

    @Test
    void getTrackedFilesPathway() {
        assertTrue(new File(String.valueOf(VersionManager.getTrackedFilesPathway())).exists());
    }

    @Test
    void stopTracking() throws GitAPIException, SQLException, IOException {

        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));
        VersionManager.startTracking(filePath);

        VersionManager.stopTracking("test");

        File trackedFile = new File(String.valueOf(VersionManager.getTrackedFilesPathway()));
        assertEquals(0, Objects.requireNonNull(trackedFile.listFiles()).length);
    }

    @Test
    void updatePathway() throws GitAPIException, SQLException, IOException {

        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));
        VersionManager.startTracking(filePath);

        VersionManager.updatePathway("test", String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test1.txt")));

        //
        File trackedFile = new File(String.valueOf(VersionManager.getTrackedFilesPathway()));
        assertEquals(1, Objects.requireNonNull(trackedFile.listFiles()).length);

        //
        String oldRepoPathway = DatabaseManager.getEntry("test", DatabaseManager.Columns.REPO_PATHWAY);
        assertNull(oldRepoPathway);

        //
        String newTrackedFile = DatabaseManager.getEntry("test1", DatabaseManager.Columns.TRACKING_PATHWAY);
        assertTrue(new File(newTrackedFile).exists());

    }

    @Test
    void revert() throws GitAPIException, SQLException, IOException {

        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));
        String testText = "test" + Math.random();
        VersionManager.startTracking(filePath);

        // Overwrites test.txt
        try(FileWriter writer = new FileWriter(filePath)){
            writer.write(testText);
        }
        VersionManager.updateTextFiles();

        // Overwrites test.txt
        try(FileWriter writer = new FileWriter(filePath)){
            writer.write("test");
        }

        //
        VersionManager.revert("test",DatabaseManager.getEntry("test", DatabaseManager.Columns.REPO_PATHWAY), VersionManager.getCommitId(DatabaseManager.getEntry("test", DatabaseManager.Columns.REPO_PATHWAY), 0));

        // Assert that the file is updated and reflects new text
        try(BufferedReader br = new BufferedReader(new FileReader(new File(VersionManager.getTrackedFilesPathway() + "\\test" +"\\test.txt")))){

            assertEquals(testText, br.readLine());
        } finally {
            VersionManager.clearTrackedFiles();
            DatabaseManager.clear();
        }
    }

    @Test
    void clearTrackedFiles() throws GitAPIException, SQLException, IOException {
        File directory = new File(String.valueOf(VersionManager.getTrackedFilesPathway()));
        String filePath = String.valueOf(Paths.get("").toAbsolutePath().resolve("src/test/java/com/example/textversioncontrol/tests/test.txt"));

        VersionManager.startTracking(filePath);
        VersionManager.clearTrackedFiles();
        assertEquals(0, Objects.requireNonNull(directory.listFiles()).length);
    }
}