package Parser;


import java.io.IOException;
import java.util.*;

import AbstactSyntaxTree.ASTNodeType;
import AbstactSyntaxTree.ASTTreeNode;
import AbstactSyntaxTree.ProgStartNode;
import Body.NonTerminalBody;
import Body.TerminalLine;
import KeywordsRef.Keywords;
import Tokeniser.Token;
import Variable.*;

public final class Parser {

    public static Parser ref;
    private final List<String> tokenList;
    private final List<Integer> tokenIDList;

    private static final Stack<NonTerminalBody> bodyStack = new Stack<>();
    private static final List<Token> currentTermLine = new ArrayList<>();

    private static final VariableHandler variableHandler = new VariableHandler();
    private static final List<Actions> requiredActionsList = new ArrayList<>();

    private static final Keywords keyRef = new Keywords();

    private static boolean isFunctionCall = false;
    private static final Stack<List<Token>> parametersTokenList = new Stack<>();
    private static final List<Token> returnTokenList = new ArrayList<>();
    private static boolean isParametersTokens = false;
    private static boolean isReturnTokens = false;
    private static ASTTreeNode bodyAccessToken = null;
    private static int ifBodyCount = 0;

    public static final ProgStartNode startNode = new ProgStartNode();
    public static ASTTreeNode currentParent = startNode;

    private Parser(List<Integer> list1, List<String> list2) {
        tokenIDList = list1;
        tokenList = list2;
    }

    public static void getInstance(List<Integer> TokenIdList, List<String> TokenList) throws IOException {
        if (ref == null){
            ref = new Parser(TokenIdList, TokenList);
        }
    }

    public static void parseTokenList(){
        int itemIndex = 0;
        for (Integer i : ref.tokenIDList) {
            switch (i){
                case 0: {
                    if(!requiredActionsList.isEmpty()){
                        if (bodyAccessToken != null){
                            String varName = compoundVariableSetDown(itemIndex);
                            forwardActionDeterminer(itemIndex);
                            if (isParametersTokens){
                                parametersTokenList.peek().add(new Token(varName,0));
                            }else {
                                currentTermLine.add(new Token(ref.tokenList.get(itemIndex), i));
                            }
                            bodyAccessToken = null;
                            break;
                        }


                        if (ref.tokenIDList.get(itemIndex-1)==17 && VariableHandler.checkVariableExistance(currentParent.getFullName()+ref.tokenList.get(itemIndex))){
                            throw new RuntimeException("Variable.Variable name has alternate description already");
                        }

                        if (requiredActionsList.getFirst() == Actions.IDENTIFIER){
                            if(isParametersTokens){
                                parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                                requiredActionsList.removeFirst();
                                VariableHandler.addVariable(new Variable(bodyStack.peek().bodyName+ref.tokenList.get(itemIndex),ref.tokenIDList.get(itemIndex-1)-17,true));
                                break;
                            }
                            requiredActionsList.removeFirst();
                            VariableHandler.addVariable(new Variable(currentParent.getFullName()+ref.tokenList.get(itemIndex),ref.tokenIDList.get(itemIndex-1)-17));
                            if (bodyStack.peek().bodyName == null){
                                bodyStack.peek().bodyName = ref.tokenList.get(itemIndex);
                                break;
                            }
                        }

                        if (requiredActionsList.getFirst() == Actions.VALUEIDENTIFIER){
                            requiredActionsList.removeFirst();
                            if (VariableHandler.checkVariableType(startNode.getFullName()+ref.tokenList.get(itemIndex),11)){
                                compoundVariableSetUp(itemIndex);
                            }else{
                                if (VariableHandler.checkVariableExistance(currentParent.parent.getFullName()+ref.tokenList.get(itemIndex))){
                                    parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                                }else{
                                    throw new RuntimeException("No Variable "+ref.tokenList.get(itemIndex)+" exists within the given scope");
                                }
                            }
                            break;
                        }

                        if (requiredActionsList.getFirst() == Actions.FUNCIDENTIFIER){
                            requiredActionsList.removeFirst();
                            if (VariableHandler.checkVariableType(startNode.getFullName()+ref.tokenList.get(itemIndex),11)){
                                compoundVariableSetUp(itemIndex);
                            }else{
                                if (VariableHandler.checkVariableExistance(currentParent.parent.getFullName()+ref.tokenList.get(itemIndex))){
                                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                                }else{
                                    throw new RuntimeException("No Function "+ref.tokenList.get(itemIndex)+" exists within the given scope");
                                }
                            }
                            break;
                        }

                        if (isFunctionCall){
                            if (VariableHandler.checkVariableType(startNode.getFullName()+ref.tokenList.get(itemIndex),11)){
                                compoundVariableSetUp(itemIndex);
                            }else{
                                if (VariableHandler.checkVariableExistance(currentParent.parent.getFullName()+ref.tokenList.get(itemIndex))){
                                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                                }else{
                                    throw new RuntimeException("No Variable "+ref.tokenList.get(itemIndex)+" exists within the given scope");
                                }
                            }
                            break;
                        }

                    }else{
                        if(!VariableHandler.checkVariableExistance(currentParent.getFullName()+ref.tokenList.get(itemIndex))){
                            if(!VariableHandler.checkVariableExistance(startNode.getFullName()+ref.tokenList.get(itemIndex))){
                                throw new RuntimeException("Unexpected Identifier "+currentParent);
                            }

                            if (VariableHandler.checkVariableType(startNode.getFullName()+ref.tokenList.get(itemIndex),11)){
                                compoundVariableSetUp(itemIndex);
                                break;
                            }
                        }
                        if (!currentTermLine.isEmpty()){
                            throw new RuntimeException("Unexpected Identifier");
                        }


                        if (VariableHandler.checkVariableType(currentParent.getFullName()+ref.tokenList.get(itemIndex),0)){
                            forwardActionDeterminer(itemIndex);
                        }
                    }
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 1: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Constant");
                    }
                    if (requiredActionsList.getFirst() != Actions.NUMBERVALUE && requiredActionsList.getFirst() != Actions.VALUEIDENTIFIER){
                        throw new RuntimeException("Unexpected Constant "+requiredActionsList);
                    }
                    if(isParametersTokens){
                        parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                        requiredActionsList.removeFirst();
                        break;
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Constant");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 3: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Access Operator");
                    }
                    if (requiredActionsList.getFirst() != Actions.ACCESSOPERATOR){
                        throw new RuntimeException("Unexpected Access Operator: "+ref.tokenList.get(itemIndex-4)+ref.tokenList.get(itemIndex-3)+ref.tokenList.get(itemIndex-2)+ref.tokenList.get(itemIndex-1)+ref.tokenList.get(itemIndex)+ref.tokenList.get(itemIndex+1)+ref.tokenList.get(itemIndex+2));
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Access Operator");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 4: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Assignment");
                    }
                    if (requiredActionsList.getFirst() != Actions.ASSIGNMENTOPERATOR){
                        throw new RuntimeException("Unexpected Assignment "+requiredActionsList);
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Assignment"+currentTermLine);
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 5: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Open Bracket placed");
                    }
                    if (requiredActionsList.getFirst() != Actions.OPENBRACKET){
                        throw new RuntimeException("Unexpected Open Bracket placed "+requiredActionsList);
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Open Bracket placed");
                    }
                    requiredActionsList.removeFirst();
                    ASTTreeNode bodyNode = new ASTTreeNode(currentParent,bodyStack.peek(),Optional.empty());
                    currentParent.addChild(bodyNode);
                    currentParent = bodyNode;
                    if(bodyStack.peek().bodyType!=ASTNodeType.CONDITIONALTYPE){
                        bodyStack.peek().determineNodeStruct(currentParent);
                    }
                    if(bodyStack.peek().bodyType!=ASTNodeType.CONDITIONALFALSETYPE&&bodyStack.peek().bodyType!=ASTNodeType.CONDITIONALTYPE&&bodyStack.peek().bodyType!=ASTNodeType.CONDITIONALTRUETYPE){
                        if(!parametersTokenList.isEmpty()){
                            parametersTokenList.pop();
                        }
                        returnTokenList.clear();
                    }
                    break;
                }

                case 6: {
                    if (!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Close Bracket placed");
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Close Bracket placed");
                    }


                    if(!bodyStack.isEmpty()) {
                        bodyStack.pop();
                    }

                    if(isFunctionCall){
                        isFunctionCall = false;
                    }

                    if ((currentParent.nodeType == ASTNodeType.CONDITIONALTRUETYPE && ref.tokenIDList.get(itemIndex+1) != 25)|| currentParent.nodeType == ASTNodeType.CONDITIONALFALSETYPE){
                        currentParent = currentParent.parent.parent;
                    }else {
                        currentParent = currentParent.parent;
                    }

                    break;
                }

                case 7: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Set type Open");
                    }
                    if (requiredActionsList.getFirst() != Actions.SETTYPEOPEN){
                        throw new RuntimeException("Unexpected Set type Open "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    returnTokenList.add(new Token(ref.tokenList.get(itemIndex),i));
                    isReturnTokens = true;
                    break;
                }

                case 8: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Set type Close");
                    }
                    if (requiredActionsList.getFirst() != Actions.SETTYPECLOSE){
                        throw new RuntimeException("Unexpected Set type Close "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    returnTokenList.add(new Token(ref.tokenList.get(itemIndex),i));
                    isReturnTokens = false;
                    bodyStack.peek().addBodyOptionals(returnTokenList);
                    break;
                }

                case 9: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Array Seperator");
                    }
                    if (requiredActionsList.getFirst() != Actions.ARRAYSEPERATOR){
                        throw new RuntimeException("Unexpected Array Seperator "+requiredActionsList);
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Array Seperator");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 10: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected EOL (End of Line)");
                    }
                    if (requiredActionsList.getFirst() != Actions.LINEEND){
                        throw new RuntimeException("Unexpected EOL (End of Line) "+requiredActionsList);
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected EOL (End of Line)");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    TerminalLine terminalLine = new TerminalLine(currentTermLine);
                    terminalLine.lineTypeDeterminer();
                    ASTTreeNode terminalLineEnd = new ASTTreeNode(currentParent,terminalLine,Optional.empty());
                    currentParent.addChild(terminalLineEnd);
                    currentTermLine.clear();
                    break;
                }

                case 13: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Open Parentheses");
                    }
                    if (requiredActionsList.getFirst() != Actions.PARENTHESESOPEN){
                        throw new RuntimeException("Unexpected Open Parentheses "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    if(!isFunctionCall){
                        parametersTokenList.push(new ArrayList<>()).add(new Token(ref.tokenList.get(itemIndex),i));
                        isParametersTokens = true;
                    }else{
                        currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    }
                    break;
                }

                case 14: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Close Parentheses");
                    }
                    if (requiredActionsList.getFirst() != Actions.PARENTHESESCLOSE){
                        throw new RuntimeException("Unexpected Close Parentheses "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    if(!isFunctionCall){
                        parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex), i));
                        isParametersTokens = false;
                        bodyStack.peek().addBodyOptionals(parametersTokenList.pop());
                        if (bodyStack.peek().bodyType == ASTNodeType.CONDITIONALTYPE) {

                            ASTTreeNode bodyNode = new ASTTreeNode(currentParent, bodyStack.peek(), Optional.empty());
                            currentParent.addChild(bodyNode);
                            currentParent = bodyNode;
                            bodyStack.peek().determineNodeStruct(currentParent);

                            NonTerminalBody conditionalTrueBody = new NonTerminalBody(ASTNodeType.CONDITIONALTRUETYPE, Optional.of("condiTrue_" + ifBodyCount), Optional.empty());
                            bodyStack.push(conditionalTrueBody);
                            break;
                        }
                    }else{
                        currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    }

                    break;
                }

                case 15: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Addition");
                    }
                    if (requiredActionsList.getFirst() != Actions.ADDITIONMODIFY){
                        throw new RuntimeException("Unexpected Addition "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 16: {
                    if (requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Subtraction");
                    }
                    if (requiredActionsList.getFirst() != Actions.SUBTRACTIONMODIFY){
                        throw new RuntimeException("Unexpected Subtraction "+requiredActionsList);
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 17: {
                    if(!requiredActionsList.isEmpty()){
                        if(isReturnTokens){
                            returnTokenList.add(new Token(ref.tokenList.get(itemIndex),i));
                            requiredActionsList.removeFirst();
                            break;
                        }
                        if (isParametersTokens){
                            parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                            requiredActionsList.addFirst(Actions.IDENTIFIER);
                            break;
                        }
                        if (requiredActionsList.getFirst() == Actions.NUMBERVALUE){
                            requiredActionsList.removeFirst();
                        }
                    }else{
                        if (!currentTermLine.isEmpty()){
                            throw new RuntimeException("Unexpected Type");
                        }
                        requiredActionsList.add(Actions.IDENTIFIER);
                        requiredActionsList.add(Actions.ASSIGNMENTOPERATOR);
                        requiredActionsList.add(Actions.NUMBERVALUE);
                        requiredActionsList.add(Actions.LINEEND);
                    }
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 21: {
                    if(requiredActionsList.isEmpty()){
                       throw new RuntimeException("Unexpected Return Type");
                    }
                    if (requiredActionsList.getFirst() != Actions.VALUERETURN){
                        throw new RuntimeException("Unexpected Return Type");
                    }
                    requiredActionsList.removeFirst();
                    returnTokenList.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 23: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected AnimationFrame definition "+requiredActionsList);
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected AnimationFrame definition");
                    }

                    requiredActionsList.add(Actions.IDENTIFIER);
                    requiredActionsList.add(Actions.ASSIGNMENTOPERATOR);
                    requiredActionsList.add(Actions.ARRAYBRACKETOPEN);
                    for (int j = 0; j < 40; j++) {
                        requiredActionsList.add(Actions.NUMBERVALUE);
                        if (j!=39){
                            requiredActionsList.add(Actions.ARRAYSEPERATOR);
                        }
                    }
                    requiredActionsList.add(Actions.ARRAYBRACKETEND);
                    requiredActionsList.add(Actions.LINEEND);
                    break;
                }

                case 24: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected IF conditional"+requiredActionsList);
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected IF conditional");
                    }
                    ifBodyCount++;
                    NonTerminalBody ifBlock = new NonTerminalBody(ASTNodeType.CONDITIONALTYPE, Optional.of("condiBody_" + ifBodyCount),Optional.empty());
                    bodyStack.push(ifBlock);
                    requiredActionsList.add(Actions.PARENTHESESOPEN);
                    requiredActionsList.add(Actions.VALUEIDENTIFIER);
                    requiredActionsList.add(Actions.COMPARISION);
                    requiredActionsList.add(Actions.VALUEIDENTIFIER);
                    requiredActionsList.add(Actions.PARENTHESESCLOSE);
                    requiredActionsList.add(Actions.OPENBRACKET);
                    break;
                }

                case 25: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected ELSE conditional"+requiredActionsList);
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected ELSE conditional");
                    }
                    NonTerminalBody condiFalse = new NonTerminalBody(ASTNodeType.CONDITIONALFALSETYPE, Optional.of("condiFalse_" + ifBodyCount),Optional.empty());
                    bodyStack.push(condiFalse);
                    requiredActionsList.add(Actions.OPENBRACKET);
                    break;
                }

                case 26: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected new Function definition "+requiredActionsList);
                    }else{
                        if (!currentTermLine.isEmpty()){
                            throw new RuntimeException("Unexpected new Function definition");
                        }
                        NonTerminalBody characterBody = new NonTerminalBody(ASTNodeType.FUNCTIONDEFINITIONTYPE,Optional.empty(),Optional.empty());
                        bodyStack.push(characterBody);
                        requiredActionsList.add(Actions.IDENTIFIER);
                        requiredActionsList.add(Actions.PARENTHESESOPEN);
                        requiredActionsList.add(Actions.PARENTHESESCLOSE);
                        requiredActionsList.add(Actions.SETTYPEOPEN);
                        requiredActionsList.add(Actions.VALUERETURN);
                        requiredActionsList.add(Actions.SETTYPECLOSE);
                        requiredActionsList.add(Actions.OPENBRACKET);
                    }
                    break;
                }

                case 27: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Game Spec Definition definition");
                    }else{
                        if (!currentTermLine.isEmpty()){
                            throw new RuntimeException("Unexpected new Game Spec Definition definition");
                        }
                        NonTerminalBody characterBody = new NonTerminalBody(ASTNodeType.GAMESPECFUNCDEFINITION,Optional.empty(),Optional.empty());
                        bodyStack.push(characterBody);
                        requiredActionsList.add(Actions.GAMESPECIFICDEFINITION);
                        requiredActionsList.add(Actions.OPENBRACKET);
                    }
                    break;
                }

                case 28: {
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected new Character definition");
                    }else{
                        if (!currentTermLine.isEmpty()){
                            throw new RuntimeException("Unexpected new Character definition");
                        }
                        NonTerminalBody characterBody = new NonTerminalBody(ASTNodeType.OBJECTDEFINITIONTYPE,Optional.empty(),Optional.empty());
                        bodyStack.push(characterBody);
                        requiredActionsList.add(Actions.IDENTIFIER);
                        requiredActionsList.add(Actions.OPENBRACKET);
                    }
                    break;
                }

                case 32: {
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    if (requiredActionsList.getFirst() != Actions.COMPARISION){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    currentTermLine.clear();
                    if(isParametersTokens){
                        parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                        requiredActionsList.removeFirst();
                        break;
                    }
                }

                case 33: {
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    if (requiredActionsList.getFirst() != Actions.COMPARISION){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    currentTermLine.clear();
                    if(isParametersTokens){
                        parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                        requiredActionsList.removeFirst();
                        break;
                    }
                }

                case 34: {
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    if (requiredActionsList.getFirst() != Actions.COMPARISION){
                        throw new RuntimeException("Unexpected Comparison");
                    }
                    currentTermLine.clear();
                    if(isParametersTokens){
                        parametersTokenList.peek().add(new Token(ref.tokenList.get(itemIndex),i));
                        requiredActionsList.removeFirst();
                        break;
                    }
                }

                case 35:{
                    if(!requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Function Call"+requiredActionsList);
                    }
                    if (!currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Function Call");
                    }
                    isFunctionCall = true;
                    if(keyRef.SpecialFunctions().contains(ref.tokenList.get(itemIndex+1))){
                        requiredActionsList.add(Actions.SPECIALFUNCTION);
                        requiredActionsList.add(Actions.PARENTHESESOPEN);
                        requiredActionsList.add(Actions.PARENTHESESCLOSE);
                        requiredActionsList.add(Actions.LINEEND);
                        currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                        break;
                    }
                    if(keyRef.GameSpecKeywords().contains(ref.tokenList.get(itemIndex+1))){
                        requiredActionsList.add(Actions.GAMESPECIFICDEFINITION);
                        requiredActionsList.add(Actions.PARENTHESESOPEN);
                        requiredActionsList.add(Actions.PARENTHESESCLOSE);
                        requiredActionsList.add(Actions.LINEEND);
                        currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                        break;
                    }
                    requiredActionsList.add(Actions.FUNCIDENTIFIER);
                    requiredActionsList.add(Actions.PARENTHESESOPEN);
                    requiredActionsList.add(Actions.PARENTHESESCLOSE);
                    requiredActionsList.add(Actions.LINEEND);
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 36: {
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Array Opener");
                    }
                    if (requiredActionsList.getFirst() != Actions.ARRAYBRACKETOPEN){
                        throw new RuntimeException("Unexpected Array Opener");
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Array Opener");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 37: {
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected Array Closer");
                    }
                    if (requiredActionsList.getFirst() != Actions.ARRAYBRACKETEND){
                        throw new RuntimeException("Unexpected Array Closer");
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected Array Closer");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 38:{
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected show Func reference");
                    }
                    if (requiredActionsList.getFirst() != Actions.SPECIALFUNCTION){
                        throw new RuntimeException("Unexpected show Func reference");
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected show Func reference");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 39:{
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected getSingularInput reference");
                    }
                    if (requiredActionsList.getFirst() != Actions.NUMBERVALUEFUNC){
                        throw new RuntimeException("Unexpected getSingularInput reference");
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected getSingularInput reference");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 40:{
                    //getRandomBackground func
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected getRandomNumber reference");
                    }
                    if (requiredActionsList.getFirst() != Actions.NUMBERVALUEFUNC){
                        throw new RuntimeException("Unexpected getRandomNumber reference");
                    }
                    if (currentTermLine.isEmpty()){
                        throw new RuntimeException("Unexpected getRandomNumber reference");
                    }
                    requiredActionsList.removeFirst();
                    currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                    break;
                }

                case 41:{
                    throw new RuntimeException("FEATURE NOT DEVELOPED YET>>>>>>");
                }

                case 42:{
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected GameStart definition");
                    }
                    if (requiredActionsList.getFirst() != Actions.GAMESPECIFICDEFINITION){
                        throw new RuntimeException("Unexpected GameStart definition");
                    }
                    if (!currentTermLine.isEmpty()){
                        if(ref.tokenIDList.get(itemIndex-1)!=35) {
                            throw new RuntimeException("Unexpected GameStart definition");
                        }else{
                            requiredActionsList.removeFirst();
                            currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                            break;
                        }
                    }
                    requiredActionsList.removeFirst();
                    bodyStack.peek().bodyName = "gameStart";
                    break;
                }

                case 43:{
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected GameLoop definition");
                    }
                    if (requiredActionsList.getFirst() != Actions.GAMESPECIFICDEFINITION){
                        throw new RuntimeException("Unexpected GameLoop definition");
                    }
                    if (!currentTermLine.isEmpty()){
                        if(ref.tokenIDList.get(itemIndex-1)!=35) {
                            throw new RuntimeException("Unexpected GameLoop definition");
                        }else{
                            requiredActionsList.removeFirst();
                            currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                            break;
                        }
                    }
                    requiredActionsList.removeFirst();
                    bodyStack.peek().bodyName = "gameLoop";
                    break;
                }

                case 44:{
                    if(requiredActionsList.isEmpty()){
                        throw new RuntimeException("Unexpected GameEnd definition");
                    }
                    if (requiredActionsList.getFirst() != Actions.GAMESPECIFICDEFINITION){
                        throw new RuntimeException("Unexpected GameEnd definition");
                    }
                    if (!currentTermLine.isEmpty()){
                        if(ref.tokenIDList.get(itemIndex-1)!=35) {
                            throw new RuntimeException("Unexpected GameEnd definition");
                        }else{
                            requiredActionsList.removeFirst();
                            currentTermLine.add(new Token(ref.tokenList.get(itemIndex),i));
                            break;
                        }
                    }
                    requiredActionsList.removeFirst();
                    bodyStack.peek().bodyName = "gameEnd";
                    break;
                }
            }
            System.out.println(requiredActionsList);
            itemIndex++;

        }
        startNode.printTree(0);
        System.out.println(variableHandler.toString());
    }

    private static void compoundVariableSetUp(int itemIndex){
        requiredActionsList.addFirst(Actions.IDENTIFIER);
        requiredActionsList.addFirst(Actions.ACCESSOPERATOR);
        bodyAccessToken = startNode.children.stream().filter(astTreeNode -> Objects.equals(astTreeNode.name, ref.tokenList.get(itemIndex))).findFirst().orElse(null);
        currentTermLine.add(new Token(ref.tokenList.get(itemIndex),ref.tokenIDList.get(itemIndex)));
    }

    private static String compoundVariableSetDown(int itemIndex){
        String varName = bodyAccessToken.getFullName()+ref.tokenList.get(itemIndex);
        if (VariableHandler.checkVariableExistance(bodyAccessToken.getFullName()+ref.tokenList.get(itemIndex))){
            currentTermLine.removeLast();
            currentTermLine.removeLast();
            ref.tokenList.set(itemIndex, varName);
            requiredActionsList.removeFirst();
        }else{
            throw new RuntimeException("Unexpected Identifier");
        }
        return varName;
    }

    private static void forwardActionDeterminer(int itemIndex){

        System.out.println(ref.tokenIDList.get(itemIndex+2));
        if (ref.tokenIDList.get(itemIndex+2) == 39 || ref.tokenIDList.get(itemIndex+2) == 40){
            requiredActionsList.addFirst(Actions.LINEEND);
            requiredActionsList.addFirst(Actions.PARENTHESESCLOSE);
            requiredActionsList.addFirst(Actions.PARENTHESESOPEN);
            requiredActionsList.addFirst(Actions.NUMBERVALUEFUNC);
            requiredActionsList.addFirst(Actions.ASSIGNMENTOPERATOR);
            return;
        }

        if (ref.tokenIDList.get(itemIndex+1) == 4){
            requiredActionsList.addFirst(Actions.LINEEND);
            requiredActionsList.addFirst(Actions.NUMBERVALUE);
            requiredActionsList.addFirst(Actions.ASSIGNMENTOPERATOR);
            return;
        }

        if (ref.tokenIDList.get(itemIndex+1) == 15){
            requiredActionsList.addFirst(Actions.LINEEND);
            requiredActionsList.addFirst(Actions.NUMBERVALUE);
            requiredActionsList.addFirst(Actions.ASSIGNMENTOPERATOR);
            requiredActionsList.addFirst(Actions.ADDITIONMODIFY);
            return;
        }

        if (ref.tokenIDList.get(itemIndex+1) == 16){
            requiredActionsList.addFirst(Actions.LINEEND);
            requiredActionsList.addFirst(Actions.NUMBERVALUE);
            requiredActionsList.addFirst(Actions.ASSIGNMENTOPERATOR);
            requiredActionsList.addFirst(Actions.SUBTRACTIONMODIFY);
        }

    }
}
