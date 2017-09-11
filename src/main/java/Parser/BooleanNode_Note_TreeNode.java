package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

public class BooleanNode_Note_TreeNode extends MutableTreeNodeImpl{
    String nodeLabel;
    String noteName;

    public BooleanNode_Note_TreeNode(String nodeLabel, String noteName){
        this.nodeLabel = nodeLabel;
        this.noteName = noteName;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public String getNoteName() {
        return noteName;
    }
}

