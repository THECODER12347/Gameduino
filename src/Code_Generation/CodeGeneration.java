package Code_Generation;

import AbstactSyntaxTree.ASTNodeType;
import AbstactSyntaxTree.ASTTreeNode;
import Variable.MemoryAddresser;
import Variable.VariableHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CodeGeneration {

    private static final List<Integer> parametersStored = new ArrayList<>(14);

    private static final List<List<Object>> generalRegistersInUse = new ArrayList<>(9);

    private static final List<ASTTreeNode> callFuncParametersAccess = new ArrayList<>();

    private static String currentSectionDefinition = "";

    private static int currentFunctionParameterCount = 0;

    public static void generateCode(ASTTreeNode node){
        for (ASTTreeNode child : node.children) {
            if(child.isTerminalNode){
                terminalLineCodeDeterminer(child);
                generalRegistersInUse.clear();
            }else{
                nonTerminalLineCodeDeterminer(child);
                generalRegistersInUse.clear();
                generateCode(child);
            }


            if (!Objects.equals(currentSectionDefinition, "") && (child.parent.nodeType != ASTNodeType.CONDITIONALTYPE && child.parent.nodeType != ASTNodeType.CONDITIONALTRUETYPE && child.parent.nodeType != ASTNodeType.CONDITIONALFALSETYPE) && (currentSectionDefinition.startsWith("condi"))){
                if (FinalPrintOut.conditionalStack.size() != 1) {
                    for (int i = 0; i < Integer.parseInt(currentSectionDefinition.replaceAll("\\D", "")); i++) {
                        FinalPrintOut.compileIfCondition();
                    }
                }
                currentSectionDefinition = child.parent.name+":func";

                if(child.parent.children.indexOf(child)+1<child.parent.children.size()&&child.parent.children.get(child.parent.children.indexOf(child)+1).nodeType==ASTNodeType.CONDITIONALFALSETYPE){
                    generateCode(child.parent.children.get(child.parent.children.indexOf(child)+1));
                }

                FinalPrintOut.compileIfCondition();
                continue;
            }

            if (!Objects.equals(currentSectionDefinition, "") && child.parent.nodeType != ASTNodeType.GAMESPECFUNCDEFINITION && (currentSectionDefinition.contains("gameStart")||currentSectionDefinition.contains("gameLoop")||currentSectionDefinition.contains("gameEnd"))){
                FinalPrintOut.compileGameSpecDefinition();
                currentSectionDefinition = "";
                continue;

            }

            if (!Objects.equals(currentSectionDefinition, "") && child.parent.nodeType != ASTNodeType.FUNCTIONDEFINITIONTYPE && currentSectionDefinition.endsWith(":func")){
                FinalPrintOut.closeFunction();
                currentSectionDefinition = "";
                currentFunctionParameterCount=0;
                continue;
            }

            if (!Objects.equals(currentSectionDefinition, "") && currentSectionDefinition.endsWith(":init")){
                if(node.children.size()<=node.children.indexOf(child)+1||!node.children.get(node.children.indexOf(child)+1).isTerminalNode){
                    FinalPrintOut.finishCharacterInit();
                    currentSectionDefinition = "";
                }
            }
        }
    }

    private static void terminalLineCodeDeterminer(ASTTreeNode node){
        switch (node.nodeType){
            case DEFINITIONTYPE:{
                ASTTreeNode originalNode = node;
                if (node.name.contains("SUPER PARENT")){
                    node = nodeToMemlocConverter(node);
                }
                List<Object> memoryAddress = MemoryAddresser.returnBasicMemoryLocation(node);
                assert memoryAddress != null;
                FinalPrintOut.defineNumberCreation(originalNode.setValue.token(), (String) memoryAddress.getFirst(), (Integer) memoryAddress.get(1));
                break;
            }

            case ANIMATIONFRAMEDEFINITIONTYPE:{
                ASTTreeNode finalNode = node;
                FinalPrintOut.setAnimationFrameData(IntStream.iterate(0, i -> i + 5)
                        .limit(8)
                        .mapToObj(i -> "0b000" + finalNode.setValue.token().substring(i, i + 5))
                        .collect(Collectors.joining(", ")));
                break;
            }

            case PARAMDEFINITIONTYPE:{
                getParameter((node.setValue.tokenID() == 17 || node.setValue.tokenID() == 18),currentFunctionParameterCount);
                currentFunctionParameterCount++;
                break;
            }

            case ACCESSVALUETYPE:{
                callFuncParametersAccess.add(nodeToMemlocConverter(node));
                break;
            }

            case CALLSPECIALTYPE:{
                switch (node.name.strip()){
                    case "showImage" ->{
                        if(callFuncParametersAccess.size()!=3){
                            throw new RuntimeException("Parameter count doesn't match function definition");
                        }

                        if(!VariableHandler.checkVariableType(callFuncParametersAccess.getFirst().parent.getFullName()+callFuncParametersAccess.getFirst().name,6)){
                            throw new RuntimeException("Parameter type doesn't match the showImage function's requirements");
                        }

                        if(!VariableHandler.checkVariableType(callFuncParametersAccess.get(1).parent.getFullName()+callFuncParametersAccess.get(1).name,0)){
                            throw new RuntimeException("Parameter type doesn't match the showImage function's requirements");
                        }

                        if(!VariableHandler.checkVariableType(callFuncParametersAccess.get(2).parent.getFullName()+callFuncParametersAccess.get(2).name,0)){
                            throw new RuntimeException("Parameter type doesn't match the showImage function's requirements");
                        }
                        FinalPrintOut.loadAnimationFrame(16, (String) MemoryAddresser.returnBasicMemoryLocation(callFuncParametersAccess.getFirst()).getFirst(),false);
                        FinalPrintOut.loadNumber(17,(int) MemoryAddresser.returnBasicMemoryLocation(callFuncParametersAccess.get(1)).get(1),false);
                        FinalPrintOut.loadNumber(18,(int) MemoryAddresser.returnBasicMemoryLocation(callFuncParametersAccess.get(2)).get(1),false);
                        FinalPrintOut.callFunction("showFuncImage");
                    }

                    case "getSingularInput" ->{
                        if(!callFuncParametersAccess.isEmpty()){
                            throw new RuntimeException("Parameter count doesn't match function definition");
                        }

                        if(node.setValue != null){
                            node.name = node.setValue.token();
                            FinalPrintOut.callFunction("getSingularInput");
                            FinalPrintOut.defineNumberNonImmediateCreation(16, (String) MemoryAddresser.returnBasicMemoryLocation(nodeToMemlocConverter(node)).getFirst(), (Integer) MemoryAddresser.returnBasicMemoryLocation(nodeToMemlocConverter(node)).get(1));
                        }
                    }

                    case "getRandomNumber" ->{
                        if(!callFuncParametersAccess.isEmpty()){
                            throw new RuntimeException("Parameter count doesn't match function definition");
                        }

                        if(node.setValue != null){
                            node.name = node.setValue.token();
                            FinalPrintOut.callFunction("getRandomNumber");
                            FinalPrintOut.defineNumberNonImmediateCreation(16, (String) MemoryAddresser.returnBasicMemoryLocation(nodeToMemlocConverter(node)).getFirst(), (Integer) MemoryAddresser.returnBasicMemoryLocation(nodeToMemlocConverter(node)).get(1));
                        }
                    }
                }
                callFuncParametersAccess.clear();
                break;
            }

            case CALLTYPE:{
                ASTTreeNode defNode = nodeToMemlocConverter(node);
                List<ASTTreeNode> list = defNode.children.stream().filter(childNode -> childNode.nodeType==ASTNodeType.PARAMDEFINITIONTYPE).toList();

                if(callFuncParametersAccess.size()!=list.size()){
                    throw new RuntimeException("Parameter count doesn't match function definition");
                }

                for (ASTTreeNode paramDefAccess : callFuncParametersAccess) {
                    int register = registerAllocation(paramDefAccess);
                    FinalPrintOut.loadNumber(register, (Integer) MemoryAddresser.returnBasicMemoryLocation(paramDefAccess).get(1),false);
                    FinalPrintOut.pushStack(register);
                }
                FinalPrintOut.callFunction(defNode.parent.name+"_"+defNode.name);
                callFuncParametersAccess.clear();
                break;
            }

            case CALLGAMESPECTYPE: {
                FinalPrintOut.callFunction(node.name);
                break;
            }

            case CONDITIONALDETERMINERTYPE: {
                List<String> names = new ArrayList<>(Arrays.asList(node.name.split("\\|")));
                ASTTreeNode originalNode = node;
                boolean isSecondVar = false;
                for (String name : names) {
                    node = originalNode;
                    if (name.contains("SUPER PARENT")) {
                        node.name = name;
                        node = nodeToMemlocConverter(node);
                        int register = registerAllocation(node);
                        FinalPrintOut.loadNumber(register, (Integer) MemoryAddresser.returnBasicMemoryLocation(node).get(1), true);
                        if (isSecondVar) {
                            FinalPrintOut.ifConditionData("r" + register);
                        } else {
                            FinalPrintOut.ifConditionData(String.valueOf(register));
                            isSecondVar = true;
                        }
                        continue;
                    }

                    try {
                        //int value
                        Integer.parseInt(name);
                        FinalPrintOut.ifConditionData(name);
                    } catch (NumberFormatException e) {
                        //register value
                        int register = registerAllocation(node);
                        FinalPrintOut.loadNumber(register, (Integer) MemoryAddresser.returnBasicMemoryLocation(node).get(1), true);
                        FinalPrintOut.ifConditionData("r" + register);
                    }
                }
                FinalPrintOut.ifConditionData(originalNode.setValue.token());
                break;
            }

            case OPERATIONTYPE:{
                ASTTreeNode originalNode = node;
                if (node.name.contains("SUPER PARENT")){
                    node = nodeToMemlocConverter(node);
                }
                List<Object> memoryAddress = MemoryAddresser.returnBasicMemoryLocation(node);
                assert memoryAddress != null;
                switch(originalNode.operationToken.tokenID()){
                    case 15:{
                        FinalPrintOut.additionNumberConstantCreation((String) memoryAddress.getFirst(), (Integer) memoryAddress.get(1), originalNode.setValue.token());
                        break;
                    }

                    case 16: {
                        FinalPrintOut.subtractionNumberConstantCreation((String) memoryAddress.getFirst(), (Integer) memoryAddress.get(1), originalNode.setValue.token());
                        break;
                    }
                }
                break;
            }
        }
    }

    private static void nonTerminalLineCodeDeterminer(ASTTreeNode node){
        switch (node.nodeType){
            case OBJECTDEFINITIONTYPE: {
                if (node.children.stream().anyMatch(isVariable -> isVariable.nodeType == ASTNodeType.DEFINITIONTYPE)){
                    FinalPrintOut.createCharacterInit(node.name);
                    currentSectionDefinition =  node.name + ":init";
                }
                break;
            }

            case FUNCTIONDEFINITIONTYPE: {
                currentFunctionParameterCount=0;
                parametersStored.clear();
                FinalPrintOut.createFunctionInit(node.parent.name,node.name);
                currentSectionDefinition = node.parent.getFullName()+node.name+":func";
                break;
            }

            case GAMESPECFUNCDEFINITION:{
                switch (node.name){
                    case "gameStart" -> FinalPrintOut.setGameSpecDefinitionTypeState(1);

                    case "gameLoop" -> FinalPrintOut.setGameSpecDefinitionTypeState(2);

                    case "gameEnd" -> FinalPrintOut.setGameSpecDefinitionTypeState(3);

                }
                currentSectionDefinition = node.parent.getFullName()+node.name;
                break;
            }

            case CONDITIONALTYPE: {
                currentSectionDefinition = node.name;
                FinalPrintOut.ifConditionData(currentSectionDefinition);
                break;
            }

            case CONDITIONALTRUETYPE: {
                FinalPrintOut.setIfState(1);
                break;
            }

            case CONDITIONALFALSETYPE: {
                FinalPrintOut.setIfState(3);
                break;
            }
        }
    }



    private static void getParameter(boolean isNumber, int parameterIndex){
        int capacityLeft = 14 - parametersStored.size();
        if (capacityLeft-(isNumber? 1:2) < 0){
            throw new RuntimeException("Too many parameters in one of the functions! Not enough space to store");
        }

        if(isNumber){
            if(parametersStored.contains(parameterIndex)){
                return;
            }

            if(parametersStored.isEmpty()){
                parametersStored.add(0);

            }else{
                parametersStored.add(parametersStored.getLast()+1);
            }

            FinalPrintOut.loadParam(parametersStored.getLast()+2,parametersStored.getLast()+5);

            parametersStored.getLast();
        }
    }

    private static ASTTreeNode nodeToMemlocConverter(ASTTreeNode node){
        ASTTreeNode originalNodeRef = node;
        while (node.parent != null){
            node = node.parent;
        }

        while (!Objects.equals((node.parent != null?node.parent.getFullName():"")+node.name,originalNodeRef.name)){
            ASTTreeNode finalNode = node;
            node = node.children.stream().filter(i -> Objects.equals(originalNodeRef.name, i.getFullName()+i.name)).findFirst().orElse(null);
            if (node == null){
                node = finalNode.children.stream().filter(i -> originalNodeRef.name.contains(i.name)).findFirst().orElseThrow(() -> new RuntimeException("No Child Found"));
            }
        }
        return node;
    }


    private static int registerAllocation(ASTTreeNode variableNode){
        List<Object> variableAddress = MemoryAddresser.returnBasicMemoryLocation(variableNode);
        if (generalRegistersInUse.contains(variableAddress)){
            return generalRegistersInUse.indexOf(variableAddress)+16;
        }

        generalRegistersInUse.add(variableAddress);
        return (generalRegistersInUse.size()-1)+16;
    }
}
