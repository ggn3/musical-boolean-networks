package BoolNets;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A unique identifier to be assigned to a {@link BooleanNode}.
 */
public class Identifier_Node extends Identifier {
    private static HashMap<String, Identifier_Node> nodeLabels = new HashMap<>();
    private static final String NODE_LABEL_BASE = "node";
    private static int nodeCount = 0;

    Identifier_Node(){
        super(getUnusedNodeName());
        nodeLabels.put(this.getLabel(),this);
        nodeCount+=1;
    }

    /**
     *
     * @param netName The name of the {@link BooleanNetwork} to which the node belongs
     * @param label A unique label for this node.
     * @throws Exception
     */
    Identifier_Node(String netName, String label) throws Exception {
        //TODO The use of the "netName" parameter here is a clumsy way to ensure that multiple network files with the same node names can be imported via a single sequence file without duplicate node names. A better way to do this should be found.
        super(netName+":"+label);
        if(nodeLabels.containsKey(netName+":"+label)){
            throw new Exception("The node label " + "'" + netName+":"+label + "'" +" has already been assigned.");
        }
        nodeLabels.put(netName+":"+label, this);
        nodeCount+=1;
    }

    private static String getUnusedNodeName(){
        int i = nodeCount;
        String name = NODE_LABEL_BASE+Integer.toString(i);
        while(nodeLabels.containsKey(name)){
            i+=1;
            name = NODE_LABEL_BASE+Integer.toString(i);
        }
        return name;
    }

    public static void clear(){
        nodeLabels.clear();
        nodeCount = 0;
    }

    public static Identifier_Node getIdentifierByLabel(String netName, String label){
        //TODO Another example of the clumsy namespace/variable scoping fix. Stops duplicate node names in different network files causing problems.
        return nodeLabels.get(netName+":"+label);
    }
}
