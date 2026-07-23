package Tokeniser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Tokeniser {
    public static Tokeniser tokeniserRef;
    private final List<String> linesList;
    public List<String> tokens = new ArrayList<>();

    private Tokeniser(List<String> lines){
        this.linesList = lines;
    }

    public static void getRef(List<String> fileData){
        if (tokeniserRef == null){
            tokeniserRef = new Tokeniser(fileData);
        }
    }

    public static void tokeniseLines(){
        for (String line : tokeniserRef.linesList) {
            line = line.trim();
            String regexExpression = generateBracketTokeniserRegex() + generateLogicalOperationsTokeniserRegex();
            regexExpression += generateArithmaticOperationsTokeniserRegex() + generateParenthesesTokeniserRegex();
            regexExpression += generateArrayBracketsTokeniserRegex() + generateArrayTypeTokeniserRegex();
            regexExpression += generateSeparatorsTokeniserRegex() + generateStringIdentifierTokeniserRegex();
            regexExpression += generateAccessTokenTokeniserRegex() + generateEqualTokenTokeniserRegex();
            tokeniserRef.tokens.addAll(Arrays.stream(line.split("("+regexExpression+"(\\s)"+")+")).filter(token -> !token.isEmpty()).toList());
        }
        tokeniserRef.tokens.removeIf(token -> token.trim().isEmpty());
    }

    private static String generateBracketTokeniserRegex(){
        return "(?=[{}])|(?<=[{}])|";
    }

    private static String generateArithmaticOperationsTokeniserRegex(){
        return "(?=[+\\-*/])|(?<=[+\\-*/])|";
    }

    private static String generateParenthesesTokeniserRegex(){
        return "(?=[()])|(?<=[()])|";
    }

    private static String generateLogicalOperationsTokeniserRegex(){
        return "(?=[|&!])|(?<=[|&!])|";
    }

    private static String generateArrayBracketsTokeniserRegex(){
        return "(?=[\\[\\]])|(?<=[\\[\\]])|";
    }

    private static String generateArrayTypeTokeniserRegex(){
        return "(?=[<>])|(?<=[<>])|";
    }

    private static String generateSeparatorsTokeniserRegex(){return "(?=[,])|(?<=[,])|(?=[;])|(?<=[;])|";}

    private static String generateAccessTokenTokeniserRegex(){return "(?=[.])|(?<=[.])|";}

    private static String generateStringIdentifierTokeniserRegex(){return "(?=[\"])|(?<=[\"])|(?=['])|(?<=['])|";}

    private static String generateEqualTokenTokeniserRegex(){return "(?=[=])|(?<=[=])|";}
}
