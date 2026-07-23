package Variable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

public final class VariableHandler {
    public static final ArrayList<Variable> paramList = new ArrayList<>();
    public static final ArrayList<Variable> variableList = new ArrayList<>();
    public static final ArrayList<Integer> numbersList = new ArrayList<>(32);
    public static final ArrayList<Integer> animationFrameDataList = new ArrayList<>(31);

    static final ArrayList<Integer> decimalList = new ArrayList<>(32);


    public static void addVariable(Variable variable){
        System.out.println(variable);
        if(!variable.isParameter){
            variableList.add(variable);
            switch (variable.type){
                case 0: {
                    numbersList.add(variableList.size()-1);
                    break;
                }
                case 1: {
                    decimalList.add(variableList.size()-1);
                    break;
                }
                case 6: {
                    animationFrameDataList.add(variableList.size()-1);
                    break;
                }
            }
        }else{
            paramList.add(variable);
        }
    }

    public static boolean checkVariableExistance(String identifier){
        return variableList.stream().anyMatch(variable -> Objects.equals(variable.identifier, identifier));
    }

    public static int getIndexOfVariable(String identifier){
       return IntStream.range(0, variableList.size())
                .filter(i -> Objects.equals(variableList.get(i).identifier, identifier))
                .findFirst()
                .orElse(-1);
    }

    public static boolean checkVariableType(String identifier, int type){
        return variableList.stream().anyMatch(variable -> Objects.equals(variable.identifier,identifier) && Objects.equals(variable.type, type));
    }

    @Override
    public String toString() {
        return "Variable.Variable Handler: "+variableList;
    }
}
