package Code_Generation;

import AbstactSyntaxTree.ASTNodeType;
import AbstactSyntaxTree.ASTTreeNode;
import AbstactSyntaxTree.ProgStartNode;

import java.util.Collections;

public final class ASTReorganiser {
    private static ProgStartNode startNode;
    static ASTReorganiser ref;

    private ASTReorganiser(ProgStartNode startNode){
        ASTReorganiser.startNode = startNode;
    }

    public static ASTReorganiser getRef(ProgStartNode startNode){
        if(ref==null){
            ref = new ASTReorganiser(startNode);
        }
        return ref;
    }

    public static void shuffleVariablesForward(){
        for (ASTTreeNode child : startNode.children) {
            if(child.nodeType != ASTNodeType.OBJECTDEFINITIONTYPE){
                continue;
            }
            int indexOfLastVariable = 0;
            boolean isFunctionDefStarted = false;
            for (ASTTreeNode astTreeNode : child.children) {
                if(isFunctionDefStarted){
                    if(astTreeNode.isTerminalNode){
                        Collections.swap(child.children,indexOfLastVariable,child.children.indexOf(astTreeNode));
                    }
                }else{
                    if(astTreeNode.isTerminalNode){
                        indexOfLastVariable++;
                    }else{
                        isFunctionDefStarted = true;
                    }
                }
            }
        }
    }
}

