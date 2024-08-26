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

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

/**
 * The <code>VersionManager</code> class is a utility class that manages files.
 */
public abstract class VersionManager {

    /**
     * Starts tracking the text file. Creates a new directory and database record to save the file's information.
     *
     * @param trackingPathway the pathway of the file to be tracked.
     * @throws IOException if the <code>trackingPathway</code> is not a valid pathway or isn't a text file
     * @throws GitAPIException if the git repository couldn't be created
     * @throws SQLException if the file's information couldn't be inserted into the database
     */
    public static void startTracking(String trackingPathway) throws IOException, GitAPIException, SQLException {

        // Columns to be stored in the database
        String fileName = parseFileName(trackingPathway);
        String directoryPathway;
        String copyPathway;
        String repoPathway = null;

        // Files
        File directory;
        File trackingFile = new File(trackingPathway);


        // Check if the trackingFile exists
        if(!trackingFile.exists())
            throw new FileNotFoundException("The following file does not exist: " + trackingPathway);

        // Checks if file is a text file
        if(!trackingFile.getName().contains(".txt"))
            throw new IOException("Attempting to track a non-text file at " + trackingPathway);


        // Set directory pathway
        directoryPathway = getTrackedFilesPathway() + "\\" + trackingFile.getName().replace(".txt","");

        // Set file to directory pathway
        directory = new File(directoryPathway);

        String temp = fileName;

        // Loop through directory names until an available name is found.
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

        Git git = null;

        try {
            // Initialize repository
            git = Git.init().setDirectory(directory).call();

            // Add the text file to the staging area
            AddCommand add = git.add();
            add.addFilepattern(fileName+".txt").call();

            // Commit changes
            CommitCommand commit = git.commit();
            commit.setMessage("initial commit").call();

            git.close();
        }
        catch(Exception e){
            // Delete resources if fail
            if(git != null)
                git.close();
            directory.delete();
            copiedFile.delete();
            DatabaseManager.deleteRecord(fileName);
            throw e;
        }
    }

    /**
     * Parse the file name without a file name extension
     *
     * @param pathway the file pathway to extract the file name from
     * @return string value containing the file name
     */
    public static String parseFileName(String pathway){
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
        ArrayList<String> gitPathways = DatabaseManager.getEntries(DatabaseManager.Columns.REPO_PATHWAY);

        // Loop through entries in database
        for (int i = 0; i < copyPathways.size() && i < trackingPathways.size(); i++) {
            Path copyPath = Paths.get(copyPathways.get(i));
            Path trackingPath = Paths.get(trackingPathways.get(i));

            // Check if there are changes between the two text files
            long mismatch = Files.mismatch(copyPath, trackingPath);

            // If there are no changes, then skip to next
            if (mismatch == -1) {
                continue;
            }

            // Copy file content over
            Files.copy(trackingPath, copyPath, StandardCopyOption.REPLACE_EXISTING);

            // Save changes to git
            commitChanges(gitPathways.get(i), copyPath.getFileName().toString());
        }
    }

    /**
     * Connects to repository from provided <code>gitPathway</code> and commits changes made to the <code>fileName</code> file.
     *
     * @param gitPathway the string pathway to the git folder
     * @param fileName the name of the file to commit changes of
     * @throws IOException if <code>gitPathway</code> isn't a valid pathway
     * @throws GitAPIException if connection to git folder couldn't be made or writing to it fails
     */
    public static void commitChanges(String gitPathway, String fileName) throws IOException, GitAPIException {
        try (Git git = Git.open(new File(gitPathway))) {
            // Add file to staging area
            git.add().addFilepattern(fileName).call();

            // Commit added file
            git.commit().setMessage("Auto Save").call();
        }
    }

    /**
     * Finds the absolute path to the TrackedFiles directory.
     *
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
     * @return list of all commit dates found from the specified repository
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
     * Finds all the commit dates in a git repository and returns it as a String array list.
     *
     * @param gitPathway the pathway to the repository to extract commit dates from.
     * @param index the index of the commit to get the commit id from.
     * @throws IOException if git file doesn't exist.
     * @throws GitAPIException if git has trouble accessing the commit history.
     * @return the commit id of the commit found
     */
    public static String getCommitId(String gitPathway, int index) throws IOException, GitAPIException {

        try (Git git = Git.open(new File(gitPathway))) {
            // Checkout to master branch
            git.checkout().setName("master").call();

            // Get commits
            Iterable<RevCommit> commits = git.log().setMaxCount(100).call();

            int i = 0;
            for (RevCommit commit : commits) {
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                }

                // Return commit hash if index matches
                if (i == index) {
                    String commitHash = commit.getName();
                    return commitHash;
                }
                i++;
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Stops tracking a file by deleting its directory and file in the project folder and database.
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
     * @return whether the directory was deleted or not
     */
    public static boolean deleteDirectory(File directory) {

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
        return directory.delete();
    }

    /**
     * Updates the file being tracked and updates its record and directory.
     *
     * @param fileName the name of the file to update the pathway of.
     * @param newTrackingPathway the new pathway to change <code>fileName</code> to.
     * @throws IOException if the new tracking file couldn't be found.
     * @throws SQLException if DatabaseManager fails to get or update an entry.
     * @return boolean value indicating whether the pathway was successfully changed or not.
     */
    public static boolean updatePathway(String fileName, String newTrackingPathway) throws IOException, SQLException {

        File newTextFile = new File(newTrackingPathway);

        // Check if there's a file at the new pathway
        if(!newTextFile.exists())
            throw new FileNotFoundException();

        // Check if the file is a text file
        if(!newTextFile.getName().contains(".txt"))
            return false;

        // Throw new IOException if the new pathway is the same as the old one
        if(newTrackingPathway.equals(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.TRACKING_PATHWAY)))
            return false;

        // Set directory pathway
        String newDirectoryPathway = getTrackedFilesPathway() + "\\" + new File(newTrackingPathway).getName().replace(".txt","");

        // Set file to directory pathway
        File directory = new File(newDirectoryPathway);

        // Extract new file name
        String newFileName = parseFileName(newTrackingPathway);
        String temp = newFileName;

        // Find available name
        for(int i = 0; directory.exists() && i < 30; i++){
            newDirectoryPathway = getTrackedFilesPathway() + "\\" + new File(newTrackingPathway).getName().replace(".txt","") + " (" + i + ")";
            directory = new File(newDirectoryPathway);
            newFileName = temp + " (" + i + ")";
        }

        // Make new directory
        directory.mkdir();

        // Get old pathways from database
        String oldDirectoryPath = DatabaseManager.getEntry(fileName, DatabaseManager.Columns.DIRECTORY_PATHWAY);
        String oldCopyPathway = DatabaseManager.getEntry(fileName, DatabaseManager.Columns.COPY_PATHWAY);
        String oldGitPathway = DatabaseManager.getEntry(fileName, DatabaseManager.Columns.REPO_PATHWAY);

        // Move text and git files to new directory
        new File(oldCopyPathway).renameTo(new File(newDirectoryPathway + "\\" + newTextFile.getName()));
        new File(oldGitPathway).renameTo(new File(newDirectoryPathway + "\\" + new File(oldGitPathway).getName()));

        // Delete old directory
        new File(oldDirectoryPath).delete();

        // Update file information record
        DatabaseManager.updateEntry(fileName, DatabaseManager.Columns.TRACKING_PATHWAY, newTrackingPathway);
        DatabaseManager.updateEntry(fileName, DatabaseManager.Columns.FILE_NAME, newFileName);
        DatabaseManager.updateEntry(newFileName, DatabaseManager.Columns.COPY_PATHWAY, newDirectoryPathway + "\\" + newTextFile.getName());
        DatabaseManager.updateEntry(newFileName, DatabaseManager.Columns.REPO_PATHWAY, newDirectoryPathway + "\\" + new File(oldGitPathway).getName());
        DatabaseManager.updateEntry(newFileName, DatabaseManager.Columns.DIRECTORY_PATHWAY, newDirectoryPathway);

        return true;
    }

    /**
     * Reverts the tracked files text back to previous commit.
     *
     * @param fileName the name of the file to revert
     * @param gitPathway the pathway to the git to get commits from
     * @param commitID the id of the commit to revert to
     * @throws IOException if I/O error occurs
     * @throws GitAPIException if JGit fails to check out to master branch
     */
    public static void revert(String fileName, String gitPathway, String commitID) throws IOException, GitAPIException {

        try (Git git = Git.open(new File(gitPathway))) {
            Repository repository = git.getRepository();

            // Ensure you are on the branch where you want to restore the file
            git.checkout().setName("master").call(); // Replace "master" with your branch name if necessary

            // Resolve the commit ID
            ObjectId commitObjectId = repository.resolve(commitID);
            if (commitObjectId == null) {
                throw new IllegalArgumentException("Commit ID not found");
            }

            // Parse the commit and get the tree
            RevCommit commit = repository.parseCommit(commitObjectId);
            RevTree tree = commit.getTree();

            // Read file content from the specific commit
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(fileName + ".txt")); // Ensure this path is correct

                if (treeWalk.next()) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    byte[] data = repository.open(objectId).getBytes();

                    // Construct the correct file path
                    Path realOutput = Paths.get(DatabaseManager.getEntry(fileName, DatabaseManager.Columns.TRACKING_PATHWAY));

                    // Write the file to the specified path
                    Files.write(realOutput, data);
                }

                repository.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Delete all files in TrackedFiles directory */
    public static void clearTrackedFiles(){

        File directory = new File(String.valueOf(getTrackedFilesPathway()));
        File[] files = directory.listFiles();

        // Loop through all files in TrackedFiles directory and delete them
        while(files != null && files.length != 0){
            deleteDirectory(files[0]);

            files = directory.listFiles();
        }
    }
}