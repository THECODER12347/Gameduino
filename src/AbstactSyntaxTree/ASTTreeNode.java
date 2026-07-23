package AbstactSyntaxTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import Body.*;
import Tokeniser.Token;

public class ASTTreeNode {
    public ASTTreeNode parent;
    public ASTNodeType nodeType;
    public boolean isTerminalNode;
    public List<ASTTreeNode> children;
    public String name;
    public Token setValue;
    public Token operationToken;


    public ASTTreeNode(ASTTreeNode parent, CodeComponent classRef , Optional<List<ASTTreeNode>> children){
        this.parent = parent;
        this.children = (children != null) ? children.orElse(new ArrayList<>()): new ArrayList<>();
        if(classRef != null) {
            try {
                if (classRef.getClass() == TerminalLine.class) {
                    nodeType = ((TerminalLine) classRef).lineType;
                    name = (((TerminalLine) classRef).name != null ? ((TerminalLine) classRef).name : "No Name Needed");
                    if (((TerminalLine) classRef).getDefinition() != null) {
                        setValue = ((TerminalLine) classRef).getDefinition();
                    }
                    if (((TerminalLine) classRef).getOperation().tokenID() != -1){
                        operationToken = ((TerminalLine) classRef).getOperation();
                    }
                    if(nodeType == ASTNodeType.CALLTYPE||nodeType == ASTNodeType.CALLGAMESPECTYPE||nodeType == ASTNodeType.CALLSPECIALTYPE){
                        ((TerminalLine) classRef).getParametersDefinition(this.parent);
                    }
                    isTerminalNode = true;
                } else {
                    nodeType = ((NonTerminalBody) classRef).getBodyType();
                    name = ((NonTerminalBody) classRef).bodyName;
                    isTerminalNode = false;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void addChild(ASTTreeNode child){
        children.addLast(child);
        child.parent = this;
    }

    public void addOptionalChild(ASTTreeNode child){
        children.addFirst(child);
    }

    public String getFullName(){
        return (parent!=null?parent.name:"") +name;
    }

    public void printTree(int indentAmount){
        if(isTerminalNode && nodeType!=ASTNodeType.CALLTYPE){
            System.out.println(this);
        }else{
            System.out.println(this +": ");
            for (ASTTreeNode i: children){
                System.out.print("\t".repeat(indentAmount+1)+"> ");
                i.printTree(indentAmount+1);
            }
        }
    }

    @Override
    public String toString() {
        return "AbstactSyntaxTree.ASTTreeNode[NodeType: " + this.nodeType +", NodeName: "+name+", is terminal?: "+isTerminalNode+", Operation type: "+operationToken+", Parent: "+parent.name+"]";
    }
}
