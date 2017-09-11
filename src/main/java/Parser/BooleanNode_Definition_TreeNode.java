package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

public class BooleanNode_Definition_TreeNode extends MutableTreeNodeImpl{
    String nodeLabel;

    public BooleanNode_Definition_TreeNode(String nodeLabel){
        this.nodeLabel = nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }
}

