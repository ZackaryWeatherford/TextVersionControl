package com.example.textversioncontrol.managers;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.nio.file.*;
import java.io.*;
import java.util.TimeZone;
import java.io.File;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 *  The <code>VersionManager</code> class is a utility class that
 *
 */
public abstract class VersionManager {

    /**
     *
     */
    public static void startTracking(String trackingPathway) throws IOException, GitAPIException, SQLException {

        // Columns to be stored in the database
        String fileName = extractFileName(trackingPathway);
        String directoryPathway;
        String copyPathway;
        String repoPathway = null;

        // Files
        File directory;
        File trackingFile = new File(trackingPathway);


        // Check if the trackingPathway is valid
        try {
            Paths.get(trackingPathway);
        } catch (InvalidPathException | NullPointerException ex) {
            throw new IOException("Invalid tracking pathway for tracking file");
        }

        // Checks if file is a text file
        if(!trackingFile.getName().contains(".txt"))
            throw new IOException("The file trying to be tracked isn't a text file");


        // Set directory pathway
        directoryPathway = getTrackedFilesPathway() + "\\" + trackingFile.getName().replace(".txt","");

        // Set file to directory pathway
        directory = new File(directoryPathway);

        String temp = fileName;

        // Loop through directory names until a available one is found.
        for(int i = 0; directory.exists() && i < 30; i++){
            directoryPathway = getTrackedFilesPathway() + "\\" + trackingFile.getName().replace(".txt","") + " (" + i + ")";
            directory = new File(directoryPathway);
            fileName = temp + " (" + i + ")";
        }

        // Set pathways
        copyPathway = directoryPathway + "\\" + trackingFile.getName();
        repoPathway = directoryPathway + "\\.git";

        // Create directory file
        if (!directory.mkdir())
            throw new IOException("Failed to create directory");


        // Create copy of text file
        File copiedFile = new File(String.valueOf(Files.copy(trackingFile.toPath(), Path.of(copyPathway))));

        // Save file info to database
        try {
            DatabaseManager.insert(fileName, directoryPathway, copyPathway, trackingPathway, repoPathway);
        } catch (SQLException e) {
            directory.delete();
            copiedFile.delete();
            throw e;
        }

        //Create Git init and save changes to git
        try {
            Git git = Git.init().setDirectory(directory).call();
            AddCommand add = git.add();
            add.addFilepattern(copyPathway).call();
            CommitCommand commit = git.commit();
            commit.setMessage("initial commit").call();
        }
        catch(Exception e){
            directory.delete();
            copiedFile.delete();
            DatabaseManager.deleteRecord(fileName);
            throw e;
        }
    }

    /**
     *
     */
    public static String extractFileName(String pathway){
        return pathway.substring(pathway.lastIndexOf('\\')+1, pathway.lastIndexOf('.'));
    }

    /**
     * Loops through tracked files in the database to update the copied files and to save the changes if there are any.
     *
     * @throws IOException if a file couldn't be found, read, or written to
     * @throws GitAPIException if JGit couldn't connect or write to repository
     * @throws SQLException if entries couldn't be retrieved from the database
     */
    public static void updateTextFiles() throws IOException, GitAPIException, SQLException {

        // Pathways to currently tracked files
        ArrayList<String> copyPathways = DatabaseManager.getEntries(DatabaseManager.Columns.COPY_PATHWAY);
        ArrayList<String> trackingPathways = DatabaseManager.getEntries(DatabaseManager.Columns.TRACKING_PATHWAY);
        ArrayList<String> gitPathways = DatabaseManager.getEntries(DatabaseManager.Columns.GIT_PATHWAY);

        // Loop through entries in database
        for(int i = 0; i < copyPathways.size() && i < trackingPathways.size(); i++){

            // Check if there are changes between the two text files
            long mismatch = Files.mismatch(Paths.get(copyPathways.get(i)), Paths.get(trackingPathways.get(i)));

            // If there are no changes, then skip to next
            if(mismatch == -1)
                continue;

            // Copy file content over
            Files.copy(Path.of(trackingPathways.get(i)), Path.of(copyPathways.get(i)), StandardCopyOption.REPLACE_EXISTING);

            // Save changes to git
            commitChanges(gitPathways.get(i), copyPathways.get(i));
        }
    }

    /**
     * Connects to repository from provided <code>gitPathway</code> and commits changes made to the <code>copyPathway</code> file.
     *
     * @param gitPathway the string pathway to the git folder
     * @param copyPathway the string pathway to the copied text file
     * @throws IOException if <code>gitPathway</code> isn't a valid pathway
     * @throws GitAPIException if connection to git folder couldn't be made or writing to it fails
     */
    public static void commitChanges(String gitPathway, String copyPathway) throws IOException, GitAPIException {

        // Establish connection to local git
        try(Git git = Git.open(new File(gitPathway))) {

            // Add file to staging area
            AddCommand add = git.add();
            add.addFilepattern(copyPathway).call();

            // Commit added file
            CommitCommand commit = git.commit();
            commit.setMessage("Auto Save").call();
        }
    }

    /**
     * Finds the absolute path to the TrackedFiles directory.
     * @return absolute path to TrackedFile directory
     */
    public static Path getTrackedFilesPathway(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles");
    }

    /**
     * Extracts the edit dates from all repositories last commit
     * and returns them as a String arraylist with the dates formatted as MM-dd-yyyy HH:mm:ss.
     *
     * @throws IOException if git fails to open repository file
     * @return string list of all the last edit dates
     */
    public static ArrayList<String> getLastEditDates() throws IOException {

        // List to store most recent edits
        ArrayList<String> lastEditDates = new ArrayList<>();

        // Grab directory containing text repos
        File textReposDir = getTrackedFilesPathway().toFile();
        File[] fileArray = textReposDir.listFiles();

        // Throw exception if TrackedFiles directory is empty
        if(fileArray == null)
            throw new FileNotFoundException("TrackedFiles Directory empty");

        // Loop through folders in TrackedFiles
        for(File directory : fileArray){
            if(directory.isDirectory() && directory.listFiles() != null){

                // Find .git file in the current directory
                File dir = null;
                for(File subFile : directory.listFiles()){
                    if(subFile.getName().equals(".git"))
                        dir = subFile;
                }

                //Open Repository
                try(Git git = Git.open(dir); RevWalk walk = new RevWalk(git.getRepository())){

                    // Get the most recent commit
                    org.eclipse.jgit.lib.ObjectId commitId = git.getRepository().findRef("master").getObjectId();
                    RevCommit commit = walk.parseCommit(commitId);

                    // Convert commit time to localdatetime
                    LocalDateTime date =
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(commit.getCommitTime()),
                                    TimeZone.getDefault().toZoneId());

                    // Format date and add to list
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                    lastEditDates.add(date.format(format));
                }
            }

        }

        return lastEditDates;
    }

    /**
     * Finds all the commit dates in a git repository and returns it as a String array list.
     *
     * @param gitPathway the pathway to the repository to extract commit dates from
     * @throws IOException if git file doesn't exist
     * @throws GitAPIException if git has trouble accessing the commit history
     */
    public static ArrayList<String> getCommitDates(String gitPathway) throws IOException, GitAPIException {

        // Dates list
        ArrayList<String> dates = new ArrayList<>();

        // Get directory of repo folder
        File gitFile = new File(gitPathway);

        // Establish connection to local git
        try(Git git = Git.open(gitFile)) {

            // Create iterator for commits history
            Iterable<RevCommit> commits = git.log().call();

            // Loop through commit history
            for (RevCommit commit : commits) {

                // Convert commit time to local-datetime
                LocalDateTime date =
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(commit.getCommitTime()),
                                TimeZone.getDefault().toZoneId());

                // Format date and add to list
                DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                dates.add(date.format(format));
            }

            return dates;
        }
    }

    /**
     * Stops tracking a file by deleting its directory and file in the project.
     *
     * @param fileName the name of the file to stop tracking
     * @throws SQLException if the connection to the database hasn't been made or an error occurs while deleting record.
     * @throws IOException if error occurs while deleting files.
     */
    public static void stopTracking(String fileName) throws SQLException, IOException {

        // Get the directory to be deleted
        String directoryPathway = DatabaseManager.getEntry(fileName, DatabaseManager.Columns.DIRECTORY_PATHWAY);
        File directory = new File(directoryPathway);

        // Delete directory
        deleteDirectory(directory);

        // Delete record in database
        DatabaseManager.deleteRecord(fileName);
    }

    /**
     * Recursively deletes a directory and all it child files.
     *
     * @param directory the directory to be deleted
     */
    public static void deleteDirectory(File directory) {

        // If file is a directory then recursively delete
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        // Delete the directory itself
        directory.delete();
    }

    /** */
    public static void updateTrackedFile(String newPathway) throws IOException {

        // Columns to be stored in the database
        String fileName = extractFileName(trackingPathway);
        String directoryPathway;
        String copyPathway;
        String repoPathway = null;

        // Files
        File directory;
        File trackingFile = new File(trackingPathway);


        // Check if the newPathway is valid
        if(!new File(newPathway).exists())
            throw new FileNotFoundException("No file found for new pathway");

        // Checks if file is a text file
        if(!trackingFile.getName().contains(".txt"))
            throw new IOException("The file trying to be tracked isn't a text file");


        // Set directory pathway
        directoryPathway = getTrackedFilesPathway() + "\\" + trackingFile.getName().replace(".txt","");

        // Set file to directory pathway
        directory = new File(directoryPathway);

        String temp = fileName;

        // Loop through directory names until a available one is found.
        for(int i = 0; directory.exists() && i < 30; i++){
            directoryPathway = getTrackedFilesPathway() + "\\" + trackingFile.getName().replace(".txt","") + " (" + i + ")";
            directory = new File(directoryPathway);
            fileName = temp + " (" + i + ")";
        }

        // Set pathways
        copyPathway = directoryPathway + "\\" + trackingFile.getName();
        repoPathway = directoryPathway + "\\.git";

        // Create directory file
        if (!directory.mkdir())
            throw new IOException("Failed to create directory");


        // Create copy of text file
        File copiedFile = new File(String.valueOf(Files.copy(trackingFile.toPath(), Path.of(copyPathway))));

        // Save file info to database
        try {
            DatabaseManager.insert(fileName, directoryPathway, copyPathway, trackingPathway, repoPathway);
        } catch (SQLException e) {
            directory.delete();
            copiedFile.delete();
            throw e;
        }

        //Create Git init and save changes to git
        try {
            Git git = Git.init().setDirectory(directory).call();
            AddCommand add = git.add();
            add.addFilepattern(copyPathway).call();
            CommitCommand commit = git.commit();
            commit.setMessage("initial commit").call();
        }
        catch(Exception e){
            directory.delete();
            copiedFile.delete();
            DatabaseManager.deleteRecord(fileName);
            throw e;
        }

    }
}