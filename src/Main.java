import Code_Generation.ASTReorganiser;
import Code_Generation.AssemblyCodeSourceHandler;
import Code_Generation.CodeGeneration;
import Code_Generation.FinalPrintOut;
import FileIO.FileReader;
import FileIO.FinalFileWriter;
import Parser.*;
import Tokeniser.*;

import java.io.IOException;


public class Main {
    static String filePath;

    public static void main(String[] args) {

        if (args.length<1){
            throw new RuntimeException("No Inputs");
        }
        filePath = args[0];

        FileReader.getRef(filePath);

        FileReader.readFile();

        Tokeniser.getRef(FileReader.FileReaderRef.fileLines);

        Tokeniser.tokeniseLines();

        System.out.println(Tokeniser.tokeniserRef.tokens);

        TokenIdentifierSys.getRef(Tokeniser.tokeniserRef.tokens);

        TokenIdentifierSys.identifyTokens();

        System.out.println(TokenIdentifierSys.tokenIdentifiers);

        try {
            Parser.getInstance(TokenIdentifierSys.tokenIdentifiers,Tokeniser.tokeniserRef.tokens);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Parser.parseTokenList();

        System.out.println("\n\n\n");

        ASTReorganiser.getRef(Parser.startNode);

        ASTReorganiser.shuffleVariablesForward();

        Parser.startNode.printTree(0);

        System.out.println("\n");

        System.out.println("-".repeat(145));

        System.out.println("\n");

        CodeGeneration.generateCode(Parser.startNode);

        FinalPrintOut.cleanUpGameSpecDefs();

        FinalPrintOut.finishCommonResources();

        FinalFileWriter.setFileName(filePath);

        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getSpecAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getCommonResourcesAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getGetSingularInputAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getGetRandomNumberAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getScreenDataTransferAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getGameDrawAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getShowFuncImageAssemblyText());
        FinalFileWriter.appendASMCodeToList(AssemblyCodeSourceHandler.getEntryInitAssemblyText());
        FinalFileWriter.appendASMCodeToList(FinalPrintOut.finalUserCodePrint);



        FinalFileWriter.compileASMCodeData();

        ExternalResourcesHandler.setName(FileReader.getFileName());

        ExternalResourcesHandler.sendToMicroController();


    }
}