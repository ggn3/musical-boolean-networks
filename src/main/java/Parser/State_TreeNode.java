package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

import java.util.ArrayList;

public class State_TreeNode extends MutableTreeNodeImpl {
    private String stateName;
    private ArrayList<String> nodeLabels;
    private boolean stateSetting;

    public State_TreeNode(String stateName, ArrayList<String> nodeLabels, boolean stateSetting){
        this.stateName = stateName;
        this.nodeLabels = nodeLabels;
        this.stateSetting = stateSetting;
    }

    public String getStateName() {
        return stateName;
    }

    public ArrayList<String> getNodeLabels() {
        return nodeLabels;
    }

    public boolean getStateSetting() {
        return stateSetting;
    }
}
