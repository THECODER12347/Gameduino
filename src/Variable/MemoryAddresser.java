package Variable;

import AbstactSyntaxTree.ASTTreeNode;

import java.util.List;


public final class MemoryAddresser {

    public static List<Object> returnBasicMemoryLocation(ASTTreeNode definitionNode){
        int varIndex = VariableHandler.getIndexOfVariable((definitionNode.parent.parent!=null? definitionNode.parent.parent.name:"")+definitionNode.parent.name+definitionNode.name);
        int index = VariableHandler.numbersList.indexOf(varIndex);
        if(index != -1){
            return List.of("numberList",index);
        }
        index = VariableHandler.decimalList.indexOf(varIndex);
        if(index != -1){
            return List.of("decimalList",index);
        }
        index = VariableHandler.animationFrameDataList.indexOf(varIndex);
        if(index != -1){
            return List.of(Integer.toString(index*8+1));
        }
        return null;
    }
}
