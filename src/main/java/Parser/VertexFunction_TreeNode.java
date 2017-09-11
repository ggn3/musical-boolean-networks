package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

import java.util.ArrayList;

public class VertexFunction_TreeNode extends MutableTreeNodeImpl {
    String vertexFunctionName;
    ArrayList<String> argumentNames;
    ArrayList<ArrayList<Boolean>> functionRuleInputs;
    ArrayList<Boolean> functionRuleOutputs;
    Boolean defaultOutput;

    public VertexFunction_TreeNode(String vertexFunctionName,
                                   ArrayList<String> argumentNames,
                                   ArrayList<ArrayList<Boolean>> functionRuleInputs,
                                   ArrayList<Boolean> functionRuleOutputs,
                                   Boolean defaultOutput) {
        this.vertexFunctionName = vertexFunctionName;
        this.argumentNames = argumentNames;
        this.functionRuleInputs = functionRuleInputs;
        this.functionRuleOutputs = functionRuleOutputs;
        this.defaultOutput = defaultOutput;
    }

    public String getVertexFunctionName() {
        return vertexFunctionName;
    }

    public ArrayList<String> getArgumentNames() {
        return argumentNames;
    }

    public ArrayList<ArrayList<Boolean>> getFunctionRuleInputs() {
        return functionRuleInputs;
    }

    public ArrayList<Boolean> getFunctionRuleOutputs() {
        return functionRuleOutputs;
    }

    public Boolean getDefaultOutput() {
        return defaultOutput;
    }
}

