package com.example.textversioncontrol.managers;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.nio.file.*;
import java.io.*;
import java.util.Scanner;
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

    /** */
    public static void startTracking(String trackingPathway) throws IOException, GitAPIException, SQLException {

        // Pathways to be stored in the database
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
            throw new IOException("Invalid trackingPathway for tracking file");
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
            throw new RuntimeException(e);
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
            DatabaseManager.deleteFile(fileName);
            throw e;
        }
    }


    public static String extractFileName(String pathway){
        return pathway.substring(pathway.lastIndexOf('\\')+1, pathway.lastIndexOf('.'));
    }

    /** */
    public static void updateTextFiles() throws IOException, GitAPIException {

        // Get list of files in repos directory
        File reposDirectory = getTrackedFilesPathway().toFile();
        File[] files = reposDirectory.listFiles();

        // If repos folder is empty end method
        if(files == null)
            return;







        //
        for(File directory : files){
            if(directory.isDirectory()){
                // List of files in directory
                File[] directoryFiles = directory.listFiles();

                // Skip directory if empty
                if(directoryFiles == null)
                    continue;

                // Copied and original files
                File copiedFile = grabTextFile(directory);
                File originalFile = new File(grabPathway(directory));

                try {
                    if (Files.mismatch(copiedFile.toPath(), originalFile.toPath()) != -1) {

                        // Copy new data into copied file
                        Files.copy(originalFile.toPath(), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Establish connection to local git
                        Git git = Git.open(getRepository(directory));

                        // Add file to staging area
                        AddCommand add = git.add();
                        add.addFilepattern(directory.getAbsolutePath() + "\\" + grabTextFile(directory).getName().replace(".txt", "")).call();

                        // Commit added file
                        CommitCommand commit = git.commit();
                        commit.setMessage("initial commit").call();
                        git.close();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    /** */
    public static Path getTrackedFilesPathway(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles");
    }

    /** */
    public static ArrayList<String> getLastEditDates() throws IOException {

        // List to store most recent edits
        ArrayList<String> lastEditDates = new ArrayList<>();

        // Grab directory containing text repos
        File textReposDir = getTrackedFilesPathway().toFile();
        File[] fileArray = textReposDir.listFiles();


        // THROW ERROR HERE IN FUTURE
        if(fileArray == null)
            return null;

        // Loop through folders in TrackedFiles
        for(File directory : fileArray){
            if(directory.isDirectory() && directory.listFiles() != null){

                // Find .git file in current directory
                File dir = null;
                for(File subFile : directory.listFiles()){
                    if(subFile.getName().equals(".git"))
                        dir = subFile;
                }

                //Open Repository
                Git git = Git.open(dir);

                RevWalk walk = new RevWalk(git.getRepository());

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

                // Close git resources
                walk.close();
                git.close();
            }

        }

        return lastEditDates;
    }

    /** */
    public static File getRepository(File directory) throws FileNotFoundException {

        File[] files = directory.listFiles();

        if(files == null)
            throw new NullPointerException();

        for(File file : files){
            if(file.isHidden() && file.getName().equals(".git"))
                return file;
        }

        throw new FileNotFoundException();
    }

    /** */
    public static File getDirectory(String directoryname){

        // Grab directory containing text repos
        File textReposDir = getTrackedFilesPathway().toFile();
        File[] fileArray = textReposDir.listFiles();

        //
        if(fileArray == null)
            return null;

        //
        for(File file : fileArray){
            if(file.getName().equals(directoryname))
                return file;
        }

        return null;
    }

    /** */
    public static ArrayList<String> getCommitDates(String directoryName) throws IOException, GitAPIException {

        // Dates list
        ArrayList<String> dates = new ArrayList<>();

        // Get directory of repo folder
        File directory = getDirectory(directoryName);

        // Throw NullPointer if directory is null
        if(directory == null)
            throw new NullPointerException();

        // Find local git repository
        File file = getRepository(directory);

        // Establish connection to local git
        Git git = Git.open(file);

        // Create iterator for commits history
        Iterable<RevCommit> commits = git.log().call();

        // Loop through commit history
        for (RevCommit commit : commits) {

            // Convert commit time to localdatetime
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