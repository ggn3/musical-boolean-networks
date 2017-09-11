package Parser;

import org.parboiled.trees.*;

public class BN_TreeNode extends MutableTreeNodeImpl{
    String nodeLabel;
    String noteName;

    public BN_TreeNode(String nodeLabel, String noteName){
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

