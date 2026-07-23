package FileIO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FinalFileWriter {
    private static String fileName="%s.asm";
    private static final List<String> dataList = new ArrayList<>();


    public static void setFileName(String fileName){
        FinalFileWriter.fileName = FinalFileWriter.fileName.formatted(fileName.replace(".txt",""));
    }

    public static void appendASMCodeToList(String data){
        dataList.add(data);
    }

    public static void compileASMCodeData(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String s : dataList) {
                writer.write(s);
            }
        } catch (IOException e) {
            throw new RuntimeException("COMPILER FATAL ERROR!!!");
        }
    }
}
