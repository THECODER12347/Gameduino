package Code_Generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class FinalPrintOut {



    static String lessThanComparisonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcpi r%3$s, %4$s
\tbrlo %2$sTrue
\trjmp %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String lessThanComparisonNonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcp r%3$s, r%4$s
\tbrlo %2$sTrue
\trjmp %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String greaterThanComparisonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcpi r%3$s, %4$s
\tbrlo %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String greaterThanComparisonNonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcpi r%3$s, %4$s
\tbrlo %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String equalComparisonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcpi r%3$s, %4$s
\tbreq %2$sTrue
\trjmp %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String equalComparisonNonImmediateValueAssemblyText = """
%1$s
\t%2$s:
\tcp r%3$s, r%4$s
\tbreq %2$sTrue
\trjmp %2$sFalse
\t
\t%2$sTrue:
%5$s
\t\trjmp %2$sContinue
\t
\t%2$sFalse:
%6$s
\t
\t%2$sContinue:
\t""";

    static String subImmediateValueAssemblyText = """
\tpush r16
\tlds r16, (%1$s+%2$d)
\tsubi r16, %3$s
\tsts (%1$s+%2$d), r16
\tpop r16
""";

    static String addImmediateValueAssemblyText = """
\tpush r16
\tlds r16, (%1$s+%2$d)
\tsubi r16, -%3$s
\tsts (%1$s+%2$d), r16
\tpop r16
""";

    static String functionEndAssemblyText = """
\tpop YH
\tpop YL
\tret

""";

    static String defineNumberImmediateAssemblyText = """
\tpush r16
\tldi r16, %s
\tsts (%s+%d), r16
\tpop r16
""";

    static String functionStartAssemblyText = """
%s_%s:
\tpush YL
\tpush YH
\tin YL, SPL
\tin YH, SPH
""";

    static String gameStartAssemblyText = """
gameStart:
%s
ret

""";

    static String gameLoopAssemblyText = """
gameLoop:
%s
	gameDrawPush:
		rcall gameDraw
		call delay_5ms
		call delay_5ms
		call delay_5ms
		rjmp gameLoop
""";

    static String gameEndAssemblyText = """
gameEnd:
    rcall gameDraw
%s
    rcall gameDraw
	gameEndLoopForever:
		rjmp gameEndLoopForever
""";


    static String getAnimationFrameLocAssemblyText = """
\tldi r%d, %s
""";

    static String getNumberAssemblyText = """
\tlds r%d, (numberList+%d)
""";

    static String getNumberParamAssemblyText = """
\tldd r%d, Y+%d
""";


    static String defineNumberNonImmediateAssemblyText = """
\tsts (%s+%d), r%d
""";


    static String pushStack = """
\tpush r%d
""";


    static String callFunction = """
\tcall %s
""";

    static String characterInitAssemblyText = "%sInit:\n";

    static List<String> objectCallInit = new ArrayList<>();

    public static String finalUserCodePrint = "";

    static int conditionalDataSetFunctionCount = 0;
    static Stack<Conditional> conditionalStack = new Stack<>();
    static List<Integer> completedGameSpecDefs = new ArrayList<>();
    public static int gameSpecDefinitionTypeState = 0;
    /* 0 = default state
       1 = gameStart
       2 = gameLoop
       3 = gameEnd
     */

    static GameSpecDefState currentGameSpec;

    public static void createCharacterInit(String charName){
        if (conditionalStack.empty() || conditionalStack.peek().state == 0) {
           finalUserCodePrint+=characterInitAssemblyText.formatted(charName);
           objectCallInit.add(characterInitAssemblyText.formatted(charName));
        }
    }

    public static void finishCharacterInit(){
        if (conditionalStack.empty() || conditionalStack.peek().state == 0) {
            finalUserCodePrint+="\tret\n";
        }
    }

    public static void createFunctionInit(String characterName, String functionName){
        finalUserCodePrint+=functionStartAssemblyText.formatted(characterName,functionName);
    }

    public static void closeFunction(){
        finalUserCodePrint+=functionEndAssemblyText;
    }

    public static void loadNumber(int register,int position, boolean defIf){
        if(defIf){
            conditionalStack.peek().prevDef+=String.format(getNumberAssemblyText, register,position)+"\n";
            return;
        }
        if(currentGameSpec!=null){
            if(conditionalStack.isEmpty()){
                currentGameSpec.addCommands(String.format(getNumberAssemblyText, register,position));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(String.format(getNumberAssemblyText, register,position).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+=String.format(getNumberAssemblyText, register,position).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=String.format(getNumberAssemblyText, register,position).replace("\t","\t\t");
            }
        }
        if(conditionalStack.isEmpty()){
            finalUserCodePrint+=String.format(getNumberAssemblyText, register,position);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=String.format(getNumberAssemblyText, register,position);
            case 1->conditionalStack.peek().trueConditionExecution+=String.format(getNumberAssemblyText, register,position).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=String.format(getNumberAssemblyText, register,position).replace("\t","\t\t");
        }
    }

    public static void loadAnimationFrame(int register,String position, boolean defIf){
        if(defIf){
            conditionalStack.peek().prevDef+=String.format(getAnimationFrameLocAssemblyText, register,position)+"\n";
            return;
        }

        if(currentGameSpec!=null){
            if(conditionalStack.isEmpty()){
                currentGameSpec.addCommands(String.format(getAnimationFrameLocAssemblyText, register,position));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(String.format(getAnimationFrameLocAssemblyText, register,position).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+=String.format(getAnimationFrameLocAssemblyText, register,position).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=String.format(getAnimationFrameLocAssemblyText, register,position).replace("\t","\t\t");
            }
        }
        if(conditionalStack.isEmpty()){
            finalUserCodePrint+=String.format(getAnimationFrameLocAssemblyText, register,position);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=String.format(getAnimationFrameLocAssemblyText, register,position);
            case 1->conditionalStack.peek().trueConditionExecution+=String.format(getAnimationFrameLocAssemblyText, register,position).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=String.format(getAnimationFrameLocAssemblyText, register,position).replace("\t","\t\t");
        }
    }

    public static void loadParam(int register,int position){
        finalUserCodePrint+=String.format(getNumberParamAssemblyText, register,position);
    }

    public static void pushStack(int register){
        if(currentGameSpec!=null){
            if(conditionalStack.isEmpty()){
                currentGameSpec.addCommands(String.format(pushStack,register));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(String.format(pushStack,register).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+=String.format(pushStack,register).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=String.format(pushStack,register).replace("\t","\t\t");
            }
        }
        if(conditionalStack.isEmpty()){
            finalUserCodePrint+=String.format(pushStack,register);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=String.format(pushStack,register);
            case 1->conditionalStack.peek().trueConditionExecution+=String.format(pushStack,register).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=String.format(pushStack,register).replace("\t","\t\t");
        }
    }


    public static void callFunction(String functionName){
        if(currentGameSpec!=null){
            if(conditionalStack.isEmpty()){
                currentGameSpec.addCommands(String.format(callFunction,functionName));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(String.format(callFunction,functionName).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+=String.format(callFunction,functionName).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=String.format(callFunction,functionName).replace("\t","\t\t");
            }
        }
        if(conditionalStack.isEmpty()){
            finalUserCodePrint+=String.format(callFunction,functionName);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=String.format(callFunction,functionName);
            case 1->conditionalStack.peek().trueConditionExecution+=String.format(callFunction,functionName).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=String.format(callFunction,functionName).replace("\t","\t\t");
        }
    }

    public static void setAnimationFrameData(String data){
        AssemblyCodeSourceHandler.setCommonResourcesAssemblyText(AssemblyCodeSourceHandler.getCommonResourcesAssemblyText().formatted(", "+data+"%s"));
    }

    public static void finishCommonResources(){
        AssemblyCodeSourceHandler.setCommonResourcesAssemblyText(AssemblyCodeSourceHandler.getCommonResourcesAssemblyText().formatted(""));
    }

    public static void defineNumberCreation(String value,String region, int location){
        if(currentGameSpec!=null){
            if(conditionalStack.empty()){
                currentGameSpec.addCommands(defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t"));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+= defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+= defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t");
            }
        }

        if(conditionalStack.empty()){
            finalUserCodePrint+=defineNumberImmediateAssemblyText.formatted(value,region,location);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=defineNumberImmediateAssemblyText.formatted(value,region,location);
            case 1->conditionalStack.peek().trueConditionExecution+= defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+= defineNumberImmediateAssemblyText.formatted(value,region,location).replace("\t","\t\t");
        }
    }

    public static void defineNumberNonImmediateCreation(int register,String region, int location){
        if(currentGameSpec!=null){
            if(conditionalStack.empty()){
                currentGameSpec.addCommands(defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t"));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t"));
                case 1->conditionalStack.peek().trueConditionExecution+= defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+= defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t");
            }
        }

        if(conditionalStack.empty()){
            finalUserCodePrint+=defineNumberNonImmediateAssemblyText.formatted(region,location,register);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=defineNumberNonImmediateAssemblyText.formatted(region,location,register);
            case 1->conditionalStack.peek().trueConditionExecution+= defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+= defineNumberNonImmediateAssemblyText.formatted(region,location,register).replace("\t","\t\t");
        }
    }

    public static void additionNumberConstantCreation(String region, int location, String value){
        if(currentGameSpec!=null){
            if(conditionalStack.empty()){
                currentGameSpec.addCommands(addImmediateValueAssemblyText.formatted(region,location,value));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(addImmediateValueAssemblyText.formatted(region,location,value));
                case 1->conditionalStack.peek().trueConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
            }
        }

        if(conditionalStack.empty()){
            finalUserCodePrint+=addImmediateValueAssemblyText.formatted(region,location,value);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=addImmediateValueAssemblyText.formatted(region,location,value);
            case 1->conditionalStack.peek().trueConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
        }
    }

    public static void subtractionNumberConstantCreation(String region, int location, String value){
        if(currentGameSpec!=null){
            if(conditionalStack.empty()){
                currentGameSpec.addCommands(subImmediateValueAssemblyText.formatted(region,location,value));
                return;
            }
            switch (conditionalStack.peek().state){
                case 0->currentGameSpec.addCommands(subImmediateValueAssemblyText.formatted(region,location,value));
                case 1->conditionalStack.peek().trueConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
                case 3->conditionalStack.peek().falseConditionExecution+=addImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
            }
        }

        if(conditionalStack.empty()){
            finalUserCodePrint+=subImmediateValueAssemblyText.formatted(region,location,value);
            return;
        }
        switch (conditionalStack.peek().state){
            case 0->finalUserCodePrint+=subImmediateValueAssemblyText.formatted(region,location,value);
            case 1->conditionalStack.peek().trueConditionExecution+=subImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
            case 3->conditionalStack.peek().falseConditionExecution+=subImmediateValueAssemblyText.formatted(region,location,value).replace("\t","\t\t");
        }
    }

    public static void ifConditionData(String data){
        switch (conditionalDataSetFunctionCount){
            case 0:{
                conditionalStack.push(new Conditional(data));
                break;
            }
            case 1:{
                conditionalStack.peek().comp1Value = data;
                break;
            }
            case 2:{
                // comparison value
                try{
                    Integer.parseInt(data);
                    conditionalStack.peek().isImmediateComparison = true;
                }catch (NumberFormatException e){
                    conditionalStack.peek().isImmediateComparison = false;
                    data = data.substring(1);
                }
                conditionalStack.peek().comp2Value = data;
                break;
            }
            case 3:{
                // conditional type determiner
                switch (data){
                    case "isEqualTo":{
                        conditionalStack.peek().conditionalType = 1;
                        break;
                    }
                    case "isLessThan":{
                        conditionalStack.peek().conditionalType = 2;
                        break;
                    }
                    case "isGreaterThan":{
                        conditionalStack.peek().conditionalType = 3;
                        break;
                    }
                }
                conditionalDataSetFunctionCount = -1;
                break;
            }
        }
        conditionalDataSetFunctionCount++;
    }

    public static void setIfState(int state){
        if(!conditionalStack.empty()) {
            conditionalStack.peek().state = state;
        }
    }

    public static void setGameSpecDefinitionTypeState(int state){
        if(completedGameSpecDefs.contains(state)){
            throw new RuntimeException("Parallel definitions of a Game Spec Function");
        }
        completedGameSpecDefs.add(state);
        gameSpecDefinitionTypeState = state;
        if(state!=0){
            currentGameSpec = new GameSpecDefState(state);
        }
    }

    public static void cleanUpGameSpecDefs(){
        if(!completedGameSpecDefs.contains(1)){
            currentGameSpec = new GameSpecDefState(1);
            compileGameSpecDefinition();
        }
        if(!completedGameSpecDefs.contains(2)){
            currentGameSpec = new GameSpecDefState(2);
            compileGameSpecDefinition();
        }
        if(!completedGameSpecDefs.contains(3)){
            currentGameSpec = new GameSpecDefState(3);
            compileGameSpecDefinition();
        }
    }

    public static void compileGameSpecDefinition(){
        finalUserCodePrint+=currentGameSpec.returnFinalStrOutput();
        currentGameSpec = null;
    }

    public static void compileIfCondition(){
        if (conditionalStack.empty()){
            return;
        }
        Conditional condi = conditionalStack.pop();

        String data = condi.printConditionalData();
        if (!conditionalStack.empty()){
            switch (conditionalStack.peek().state){
                case 1 -> conditionalStack.peek().trueConditionExecution += data;
                case 3 -> conditionalStack.peek().falseConditionExecution += data;
            }
        }else{
            if (currentGameSpec!=null){
                currentGameSpec.addCommands(data);
            }else{
                finalUserCodePrint+=data;
            }
        }

        setIfState(0);
    }


    static class Conditional{
        String name;
        int conditionalType;
        boolean isImmediateComparison;
        String falseConditionExecution = "";
        String trueConditionExecution = "";
        String comp1Value;
        String comp2Value;
        String prevDef="";
        int state;

        public Conditional(String name){
            this.name = name;
        }

        public String printConditionalData(){
            switch (conditionalType){
                case 1->{
                    if(isImmediateComparison){
                        return equalComparisonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                    }
                    return equalComparisonNonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                }

                case 2->{
                    if(isImmediateComparison){
                        return lessThanComparisonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                    }
                    return lessThanComparisonNonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                }

                case 3->{
                    if(isImmediateComparison){
                        return greaterThanComparisonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                    }
                    return greaterThanComparisonNonImmediateValueAssemblyText.formatted(prevDef,name,comp1Value,comp2Value,trueConditionExecution,falseConditionExecution);
                }
            }
            return "";
        }
    }

    static class GameSpecDefState{
        private String mainText;
        private String additionalText = "";
        private int state = 0;

        public GameSpecDefState(int state){
            this.state = state;
            switch (state){
                case 1 -> this.mainText = gameStartAssemblyText;
                case 2 -> this.mainText = gameLoopAssemblyText;
                case 3 -> this.mainText = gameEndAssemblyText;
            }
        }

        public void addCommands(String data){
            additionalText += data;
        }

        public String returnFinalStrOutput(){
            if(state==1){
                for (String s : objectCallInit) {
                    callFunction(s.replace(":",""));
                }
            }
            return mainText.formatted(additionalText);
        }
    }

}
