package Body;

import AbstactSyntaxTree.ASTNodeType;
import AbstactSyntaxTree.ASTTreeNode;
import Tokeniser.Token;

import java.util.*;

public final class NonTerminalBody extends CodeComponent {
    public ASTNodeType bodyType;
    public String bodyName;
    public ArrayList<List<Token>> bodyOptionals;


    public NonTerminalBody(ASTNodeType bodyType, Optional<String> bodyName, Optional<ArrayList<List<Token>>> bodyOptionals){
        this.bodyType = bodyType;
        this.bodyName = bodyName.orElse(null);
        this.bodyOptionals = bodyOptionals.orElse(new ArrayList<>());
    }

    public void determineNodeStruct(ASTTreeNode parent){
        switch (bodyType){
            case FUNCTIONDEFINITIONTYPE: {
                for (List<Token> bodyOptional : bodyOptionals) {
                    if (bodyOptional.stream().anyMatch(token -> token.tokenID() == 13 || token.tokenID() == 14)){
                        // parameters
                        ArrayList<List<Token>> paramList = new ArrayList<>();
                        for (Token token : bodyOptional) {
                            if(token.tokenID() == 17 ||token.tokenID() == 18){
                                ArrayList<Token> tokenList = new ArrayList<>();
                                tokenList.add(token);
                                paramList.add(tokenList);
                            } else {
                                if(!paramList.isEmpty()){
                                    paramList.getLast().add(token);
                                }
                            }
                        }
                        if(!paramList.isEmpty()){
                            for (List<Token> parameters : paramList) {
                                TerminalLine paramDef = new TerminalLine(parameters,true);
                                paramDef.lineTypeDeterminer();
                                ASTTreeNode h = new ASTTreeNode(parent,paramDef,Optional.empty());
                                System.out.println(h);
                                parent.addOptionalChild(h);
                            }
                        }

                    }

                    if (bodyOptional.stream().anyMatch(token -> token.tokenID() == 7 || token.tokenID() == 8)){
                        TerminalLine paramDef = new TerminalLine(bodyOptional,false,true);
                        paramDef.lineTypeDeterminer();
                        ASTTreeNode h = new ASTTreeNode(parent,paramDef,Optional.empty());
                        parent.addOptionalChild(h);
                    }
                }
                break;
            }
            case CONDITIONALTYPE: {
                if (bodyOptionals.getFirst().stream().anyMatch(token -> token.tokenID() >=32 && token.tokenID() <=34)){
                    System.out.println(bodyOptionals.getFirst());
                    TerminalLine conditionalDef = new TerminalLine(bodyOptionals.getFirst(),false,false,true);
                    conditionalDef.lineTypeDeterminer();
                    ASTTreeNode h = new ASTTreeNode(parent,conditionalDef,Optional.empty());
                    parent.addOptionalChild(h);
                }
                break;
            }
        }
    }

    public ASTNodeType getBodyType() {
        return bodyType;
    }

    public void addBodyOptionals(List<Token> tokenList){
        bodyOptionals.add(tokenList);
    }

}
