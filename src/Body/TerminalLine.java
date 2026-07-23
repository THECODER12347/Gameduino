package Body;

import AbstactSyntaxTree.ASTNodeType;
import AbstactSyntaxTree.ASTTreeNode;
import KeywordsRef.Keywords;
import Tokeniser.Token;

import java.util.*;
import java.util.stream.Collectors;

public final class TerminalLine extends CodeComponent {
    public ASTNodeType lineType;
    private final List<Token> lineTokens;
    public String name;
    private Token definitionToken;
    boolean isParamDef;
    boolean isReturnDef;
    boolean isConditionalArg;
    boolean isParamInputDef;

    public TerminalLine(List<Token> lineTokens, boolean isParamDef, boolean isReturnDef,boolean isConditionalArg, boolean isParamInputDef){
        this.lineTokens = lineTokens;
        this.isParamDef = isParamDef;
        this.isReturnDef = isReturnDef;
        this.isConditionalArg = isConditionalArg;
        this.isParamInputDef = isParamInputDef;
    }

    public TerminalLine(List<Token> lineTokens, boolean isParamDef, boolean isReturnDef,boolean isConditionalArg){
        this.lineTokens = lineTokens;
        this.isParamDef = isParamDef;
        this.isReturnDef = isReturnDef;
        this.isConditionalArg = isConditionalArg;
        this.isParamInputDef = false;
    }

    public TerminalLine(List<Token> lineTokens, boolean isParamDef, boolean isReturnDef){
        this.lineTokens = lineTokens;
        this.isParamDef = isParamDef;
        this.isReturnDef = isReturnDef;
        this.isConditionalArg = false;
        this.isParamInputDef = false;
    }

    public TerminalLine(List<Token> lineTokens, boolean isParamDef){
        this.lineTokens = lineTokens;
        this.isParamDef = isParamDef;
        this.isReturnDef = false;
        this.isConditionalArg = false;
        this.isParamInputDef = false;
    }

    public TerminalLine(List<Token> lineTokens){
        this.lineTokens = lineTokens;
        this.isParamDef = false;
        this.isReturnDef = false;
        this.isConditionalArg = false;
        this.isParamInputDef = false;
    }

    public void lineTypeDeterminer(){
        if(isParamDef){
            lineType = ASTNodeType.PARAMDEFINITIONTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0).findAny().orElse(new Token(null,0)).token();
            definitionToken = lineTokens.stream().filter(identifier -> identifier.tokenID() == 17 || identifier.tokenID() == 18).findAny().orElse(new Token(null,0));
            return;
        }

        if(isReturnDef){
            lineType = ASTNodeType.RETURNDEFINITIONTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() >= 17 && identifier.tokenID() <= 21).findAny().orElse(new Token(null,0)).token();
            return;
        }

        if(isConditionalArg){
            lineType = ASTNodeType.CONDITIONALDETERMINERTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0||identifier.tokenID() == 1).map(Token::token).collect(Collectors.joining("|"));
            definitionToken = lineTokens.stream().filter(token -> token.tokenID() >=32 && token.tokenID() <=34).findFirst().orElseThrow(() -> new RuntimeException("FATAL COMPILER ERROR!! NO COMPARISON FOUND DESPITE IF CONDITIONAL!!!!"));
            return;
        }

        if(isParamInputDef){
            lineType = ASTNodeType.ACCESSVALUETYPE;
            name = lineTokens.stream().filter(identifier->identifier.tokenID()==0).findFirst().orElseThrow(()->new RuntimeException("Fatal error in TerminalLine")).token();
            return;
        }

        if (lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 35)){
            name = lineTokens.stream().filter(token -> token.tokenID() == 0||(token.tokenID() >=38 && token.tokenID() <=41)||(token.tokenID() >=42 && token.tokenID() <=44)).findFirst().orElseThrow(()->new RuntimeException("Error")).token();
            Keywords ref = new Keywords();
            if(ref.SpecialFunctions().contains(name)){
                lineType = ASTNodeType.CALLSPECIALTYPE;
            }else if(ref.GameSpecKeywords().contains(name)){
                lineType = ASTNodeType.CALLGAMESPECTYPE;
            }else{
                lineType = ASTNodeType.CALLTYPE;
            }
            return;
        }

        if(lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 4) && lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 15 || lineToken.tokenID() == 16)){
            lineType = ASTNodeType.OPERATIONTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0).findAny().orElse(new Token(null,0)).token();
            definitionToken = lineTokens.stream().filter(identifier -> identifier.tokenID() > 0 && identifier.tokenID() < 3 ).findAny().orElse(new Token("0",1));
            return;
        }

        if(lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 4) && lineTokens.stream().filter(numberCount -> numberCount.tokenID() == 1).count()==40){
            lineType = ASTNodeType.ANIMATIONFRAMEDEFINITIONTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0).findAny().orElse(new Token(null,0)).token();
            StringBuilder stringBuilder = new StringBuilder(64);
            lineTokens.stream().filter(token->token.tokenID()==1).forEach(token -> stringBuilder.append(token.token()));
            definitionToken = new Token(stringBuilder.toString(), 100);
            return;
        }

        if (lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 4) && lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 39 || lineToken.tokenID() == 40)){
            lineType = ASTNodeType.CALLSPECIALTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() ==39 || identifier.tokenID() == 40 ).findAny().orElse(new Token("0",1)).token();
            definitionToken = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0).findAny().orElseThrow(()->new RuntimeException("No variable defined"));
            return;
        }

        if (lineTokens.stream().anyMatch(lineToken -> lineToken.tokenID() == 4) && lineTokens.stream().noneMatch(lineToken -> lineToken.tokenID() == 15 || lineToken.tokenID() == 16)){
            lineType = ASTNodeType.DEFINITIONTYPE;
            name = lineTokens.stream().filter(identifier -> identifier.tokenID() == 0).findAny().orElse(new Token(null,0)).token();
            definitionToken = lineTokens.stream().filter(identifier -> identifier.tokenID() > 0 && identifier.tokenID() < 3 ).findAny().orElse(new Token("0",1));
        }

    }

    public Token getDefinition(){
        return definitionToken;
    }

    public void getParametersDefinition(ASTTreeNode parent){
        List<Token> tokens = lineTokens.stream()
                // 1. Drop everything before (and including) "START"
                .dropWhile(item -> item.tokenID() != 13)
                .skip(1)
                // 2. Take everything before "END"
                .takeWhile(item -> item.tokenID() != 14)
                .toList();
        for (Token token : tokens) {
            TerminalLine terRef = new TerminalLine(List.of(token),false,false,false,true);
            terRef.lineTypeDeterminer();
            parent.addChild(new ASTTreeNode(parent,terRef,Optional.empty()));
        }

    }


    public Token getOperation(){
        return lineTokens.stream().filter(operation -> operation.tokenID()==15 || operation.tokenID()==16).findAny().orElse(new Token(null,-1));
    }
}
