package com.example.textversioncontrol.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class FileManager {


    /** */
    public static Path getTextReposPath(){
        return Paths.get("").toAbsolutePath().resolve("src/main/resources/TrackedFiles");
    }




}
