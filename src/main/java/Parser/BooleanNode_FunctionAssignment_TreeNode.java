package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

import java.util.ArrayList;

public class BooleanNode_FunctionAssignment_TreeNode extends MutableTreeNodeImpl{
    String nodeLabel;
    String functionLabel;
    ArrayList<String> inputNodeLabels;

    public BooleanNode_FunctionAssignment_TreeNode(String nodeLabel, String noteName, ArrayList<String> inputNodeLabels){
        this.nodeLabel = nodeLabel;
        this.functionLabel = noteName;
        this.inputNodeLabels = inputNodeLabels;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public void setFunctionLabel(String functionLabel) {
        this.functionLabel = functionLabel;
    }

    public void setInputNodeLabels(ArrayList<String> inputNodeLabels) {
        this.inputNodeLabels = inputNodeLabels;
    }

    public ArrayList<String> getInputNodeLabels() {
        return inputNodeLabels;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public String getFunctionLabel() {
        return functionLabel;
    }
}

