package com.example.java;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    String path;
    String regex;
    String zipFileName;
    Pattern pattern;
    List<File> zipFiles = new ArrayList<File>();

    public static void main(String[] args) {
        // write your code here
        Main app = new Main();
        switch(Math.min(args.length, 3)){
            case 0:
                System.out.println("Usage: FileSearch path [regex] [zipfile]");
                return;
            case 3: app.setZipFileName(args[2]);
            case 2: app.setRegex(args[1]);
            case 1: app.setPath(args[0]);
        }
        try {
            app.walkDirectory(app.getPath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
// Java 6
//    public void walkDirectory(String path) throws IOException{
//        File dir = new File(path);
//        File[] files = dir.listFiles();
//
//        for(File file : files){
//            if (file.isDirectory()) {
//                walkDirectory(file.getAbsolutePath());
//            } else {
//                processFile(file);
//            }
//        }
//
//    }

// Java 7
    public void walkDirectory(String path) throws IOException{
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>(){
           public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
               throws IOException {
               processFile(file.toFile());
               return FileVisitResult.CONTINUE;
           }
        });
        zipFiles();
    }

    public void processFile(File file) throws IOException {
        if (searchFile(file));
        {
            addFileToZip(file);
        }
    }


//    public boolean searchFile(File file){
//        boolean found = false;
//        Scanner scanner = new Scanner(file, "UTF-8");
//        while(scanner.hasNextLine()) {
//            found = searchText(scanner.nextLine());
//            if (found) {
//                break;
//            }
//        }
//        scanner.close();
//        return found;
//    }

    // Java 7

    public boolean searchFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines){
            if (searchText(line)){
                return true;
            }
        }
        return false;
    }

    public String getRelativeFilename(File file, File baseDir){
        String fileName = file.getAbsolutePath().substring(
                baseDir.getAbsolutePath().length());

        fileName = fileName.replace('\\','/');

        while (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    public void zipFiles() throws IOException{
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getZipFileName()))){
            File baseDir = new File(getPath());

            for (File file : zipFiles){
                String fileName = getRelativeFilename(file, baseDir);

                ZipEntry zipEntry = new ZipEntry(fileName);
                zipEntry.setTime(file.lastModified());
                out.putNextEntry(zipEntry);

                Files.copy(file.toPath(),out);
                out.closeEntry();
            }
        }
    }

    public boolean searchText(String text){
        if (this.getRegex() == null){
            return true;
        }
        return this.pattern.matcher(text).matches();
    }

    public void addFileToZip(File file){
        if (getZipFileName()!= null){
            zipFiles.add(file);
        }
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
        this.pattern = pattern.compile(regex);
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}