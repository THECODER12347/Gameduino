package Tokeniser;

import KeywordsRef.Keywords;

import java.util.ArrayList;
import java.util.List;

public final class TokenIdentifierSys {

    private final static List<String> keyCharacters1 = List.of(".","=","{","}","<",">",",",";","\"","'","(",")","+","-");
    private final static List<String> keyCharacters2 = List.of("[","]");
    public static TokenIdentifierSys ref;
    private final List<String> tokens;
    public static List<Integer> tokenIdentifiers = new ArrayList<>();
    private static final Keywords keywords = new Keywords();

    private TokenIdentifierSys(List<String> tokens){
        this.tokens = tokens;
    }

    public static void getRef(List<String> tokenList){
        if (ref == null){
            ref = new TokenIdentifierSys(tokenList);
        }
    }

    public static void identifyTokens(){
        /* types: 0 - Identifier
                  1 - Constant number
                  2 - Constant decimal

                  3 - Access Operator
                  4 - Assignment Operator
                  5 - Body Open
                  6 - Body Close
                  7 - Alligator Open
                  8 - Alligator Open
                  9 - Item seperator
                  10 - Line ender
                  11 - String determiner
                  12 - String determiner
                  13 - Conditional Entry Open
                  14 - Conditional Entry Close
                  15 - Addition
                  16 - Subtraction

                  17 - Number Datatype
                  18 - Decimal Datatype
                  19 - Word Datatype
                  20 - Boolean Datatype
                  21 - Void
                  22 - AnimationFrame Datatype
                  23 - List Datatype

                  ----------------------------
                  KeywordsRef.Keywords follow from 22 to 32
                  Special Functions follow from 33 to 38
          */

        for (String token : ref.tokens) {
            int lengthPassed = 3;
            int tokenListIDTemp;
            token = token.trim();

            // Key characters
            tokenListIDTemp = keyCharacters1.indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keyCharacters1.size();

            // Basic Datatypes
            tokenListIDTemp = keywords.BasicDatatypes().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.BasicDatatypes().size();

            // Special Datatypes
            tokenListIDTemp = keywords.SpecialDatatypes().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.SpecialDatatypes().size();

            // ComplexDatatypes
            tokenListIDTemp = keywords.ComplexDatatypes().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.ComplexDatatypes().size();


            // Body KeywordsRef.Keywords
            tokenListIDTemp = keywords.BodyKeywords().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.BodyKeywords().size();

            // Comparison keywords
            tokenListIDTemp = keywords.ComparisonKeywords().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.ComparisonKeywords().size();

            // Operational keywords
            tokenListIDTemp = keywords.OperationalKeywords().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.OperationalKeywords().size();

            tokenListIDTemp = keyCharacters2.indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keyCharacters2.size();

            //Special Functions
            tokenListIDTemp = keywords.SpecialFunctions().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }
            lengthPassed += keywords.SpecialFunctions().size();

            //GameSpec Functions
            tokenListIDTemp = keywords.GameSpecKeywords().indexOf(token);
            if (tokenListIDTemp != -1){
                tokenIdentifiers.add(tokenListIDTemp+lengthPassed);
                continue;
            }

            // Constant Number
            try{
                int ing = Integer.parseInt(token);
                if(ing<256 && ing>-1) {
                    tokenIdentifiers.add(1);
                    continue;
                }else{
                    throw new RuntimeException("Illegal Number");
                }
            } catch (NumberFormatException ignored){}

            // Constant Decimal
            try{
                float flt = Float.parseFloat(token);
                if(flt<255.997 && flt>=0) {
                    if((flt*256)%1 != 0){
                        System.out.println("Caution: This decimal value can not be accurately represented due to SYS LIMITATIONS. Refer to Documentation for more info.");
                    }
                    tokenIdentifiers.add(2);
                    continue;
                }else{
                    throw new RuntimeException("Illegal Decimal");
                }
            } catch (NumberFormatException ignored){}

            tokenIdentifiers.add(0);
        }

    }
}
