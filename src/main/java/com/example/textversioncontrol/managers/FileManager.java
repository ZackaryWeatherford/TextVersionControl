package com.example.textversioncontrol.managers;

import javafx.scene.chart.PieChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList;

public class FileManager {







    /** */
    public static Path getTrackedFilesPathway(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles");
    }
}






        /*
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

         */