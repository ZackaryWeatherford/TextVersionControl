package com.example.textversioncontrol;

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

    /**
     * The <code>startTracking</code> method 
     */
    public static void startTracking(String pathway) throws IOException {

        // Check if pathway is valid
        try {
            Paths.get(pathway);
        } catch (InvalidPathException | NullPointerException ex) {
            System.out.println("Invalid Pathway");
            return;
        }

        // File to track
        File textFile = new File(pathway);

        // Checks if file is a text file
        if(!textFile.getName().contains(".txt")){
            System.out.println("Not a text file");
            return;
        }

        // Pathways
        String repoPathway = getTextReposPath() + "\\" + textFile.getName().replace(".txt","");
        String infoDirPathway = repoPathway + "\\info";

        // Directories
        File repoDirectory = new File(repoPathway);
        File infoDirectory = new File(infoDirPathway);

        // Create repo directory
        if (repoDirectory.mkdir()) {
            System.out.println("Repo folder created successfully!");
        } else {
            System.out.println("Repo failed to create folder.");
            return;
        }

        // Create info directory
        if (infoDirectory.mkdir()) {
            System.out.println("Info folder created successfully!");
        } else {
            System.out.println("Info failed to create folder.");
            repoDirectory.delete();
            return;
        }

        // Create info text in info directory
        File infoFile = new File(infoDirectory.getAbsolutePath() + "\\info.txt");
        infoFile.createNewFile();

        // Create copied text file in repo directory
        // File copiedFile = new File(repoPathway + "\\" + textFile.getName().replace(".txt","") + ".txt");
        //copiedFile.createNewFile();

        // Add pathway into info text file
        BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile));
        writer.write(pathway);
        writer.close();

        // Create copy of text file
        Files.copy(textFile.toPath(), Path.of(repoPathway + "\\" + textFile.getName().replace(".txt", "") + ".txt"));

        //Create Git init
        try {

            Git git = Git.init().setDirectory(repoDirectory).call();
            AddCommand add = git.add();
            add.addFilepattern(repoPathway + "\\" + textFile.getName().replace(".txt", "")).call();
            CommitCommand commit = git.commit();
            commit.setMessage("initial commit").call();


        }
        catch(Exception e){
            e.printStackTrace();
        }

        //Git add


        //Git Commit

        //Update Display
    }

    /** */
    public static void updateTextFiles() throws IOException, GitAPIException {

        // Get list of files in repos directory
        File reposDirectory = getTextReposPath().toFile();
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
    public static String grabPathway(File directory) throws FileNotFoundException {

        //
        File[] filesList = directory.listFiles();

        Scanner reader;

        //
        for(File file : filesList){
            //
            if(file.isDirectory() && file.getName().equals("info")) {
                reader = new Scanner(file.listFiles()[0]);
                String pathway = reader.nextLine();
                reader.close();
                return pathway;
            }
        }

        throw new FileNotFoundException();
    }

    /** */
    public static File grabTextFile(File directory) throws FileNotFoundException {

        File[] files = directory.listFiles();

        for(File file : files){
            if(!file.isHidden() && !file.isDirectory())
                return file;
        }

        throw new FileNotFoundException();
    }

    /** */
    public static File grabInfoFile(File directory) throws FileNotFoundException {
        return new File(grabPathway(directory));
    }

    /** */
    public static Path getTextReposPath(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/textfile_repos");
    }

    /** */
    public static ArrayList<String> getTextFileNames(){

        ArrayList<String> fileNames = new ArrayList<String>();

        File textReposDir = getTextReposPath().toFile();
        File[] fileArray = textReposDir.listFiles();

        //THROW ERROR HERE IN FUTURE
        if(fileArray == null)
            return null;

        //
        for(File file : fileArray){

            //
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File subFile : files)
                    if(!subFile.isHidden() && !subFile.isDirectory()) {
                        fileNames.add(subFile.getName());
                        break;
                    }


            }

        }

        // Remove File Name Extension
        for(int i = 0; i < fileNames.size(); i++){
            String string = fileNames.get(i);
            fileNames.set(i, string.substring(0, string.length() - 4));

        }

        return fileNames;
    }

    /** */
    public static ArrayList<String> getPathways() throws FileNotFoundException {

        //
        ArrayList<String> pathways = new ArrayList<String>();

        // Grab directory containing text repos
        File textReposDir = getTextReposPath().toFile();
        File[] fileArray = textReposDir.listFiles();

        //THROW ERROR HERE IN FUTURE
        if(fileArray == null)
            return null;

        //
        Scanner reader;

        //
        for(File file : fileArray){

            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File subFile : files)
                    //Check if info.txt
                    if(subFile.isDirectory() && !subFile.isHidden()) {
                        //Get Pathway from info.txt
                        reader = new Scanner(subFile.listFiles()[0]);
                        pathways.add(reader.next());
                        reader.close();
                    }
            }
        }

        return pathways;
    }

    /** */
    public static ArrayList<String> getLastEditDates() throws IOException {

        // List to store most recent edits
        ArrayList<String> lastEditDates = new ArrayList<>();

        // Grab directory containing text repos
        File textReposDir = getTextReposPath().toFile();
        File[] fileArray = textReposDir.listFiles();


        // THROW ERROR HERE IN FUTURE
        if(fileArray == null)
            return null;

        // Loop through folders in textfile_repos
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
        File textReposDir = getTextReposPath().toFile();
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