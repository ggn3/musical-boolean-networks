package Utilities;

import Parser.DialogMaker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Basic file input/output utilities
 */
public class IO {

    public static String readStringFromFile(String fileLocation) {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(fileLocation)));
        } catch (IOException e) {
            DialogMaker.showErrorDialog("File Not Loaded", "The file '"+ fileLocation +"' cold not be loaded. Please check the file-path and try again.");
        }
        return content;
    }


}
