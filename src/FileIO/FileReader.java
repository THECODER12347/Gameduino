package FileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FileReader {
    public static FileReader FileReaderRef = null;
    private final File fileRef;
    public List<String> fileLines = new ArrayList<>();

    private FileReader (String filePath){
        fileRef = new File(filePath);
    }

    public static void getRef(String path){
        if(FileReaderRef == null){
            FileReaderRef = new FileReader(path);
        }
    }

    public static void readFile(){
        try (BufferedReader br = new BufferedReader(new java.io.FileReader(FileReaderRef.fileRef))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.isBlank()){
                    continue;
                }
                FileReaderRef.fileLines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(FileReaderRef.fileRef.getPath()+" File not exist");
        }
    }

    public static String getFileName(){
        return FileReaderRef.fileRef.getName().split("\\.")[0];
    }
}
