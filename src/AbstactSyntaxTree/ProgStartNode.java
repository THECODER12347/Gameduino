package AbstactSyntaxTree;

public final class ProgStartNode extends ASTTreeNode {

    public ProgStartNode() {
        super(null, null, java.util.Optional.empty());
        super.name = "SUPER PARENT";
    }

    public String toString() {
        return "AbstactSyntaxTree.ProgStartNode[NodeType: SUPER PARENT]";
    }
}
