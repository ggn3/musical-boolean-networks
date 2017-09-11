package BoolNets;

import java.util.ArrayList;
import java.util.HashSet;

public class NetworkState {
    private static HashSet<String> stateNames = new HashSet<>();

    private String name;
    private boolean truthValue;
    private ArrayList<Identifier_Node> nodeIDs;

    public NetworkState(String name, ArrayList<Identifier_Node> nodeIDs, boolean truthValue) throws Exception {
        if (stateNames.contains(name)) {
            throw new Exception("The state name " + "'" + name + "'" + " is already in use.");
        } else {
            this.name = name;
            this.nodeIDs = nodeIDs;
            this.truthValue = truthValue;
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<Identifier_Node> getNodeIDs() {
        return nodeIDs;
    }

    public boolean getTruthValue() {
        return truthValue;
    }
}
