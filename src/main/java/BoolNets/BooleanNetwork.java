package BoolNets;


import Parser.DialogMaker;
import Utilities.Pair;
import Sound.SoundMaker;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BooleanNetwork {

    //We can track backwards and forwards through the state of the network.
    //The value of transportPosition is the current location in the array of system states.
    public transient int transportPosition;

    //How far the computation of future states has currently gone.
    private transient int bufferPosition;

    //Mapping from Identifiers' UUID Strings to the BooleanNodes to which they belong
    private HashMap<String, BooleanNode> nodes;
    //Mapping from unique identification strings (variable names) to their corresponding NetworkState instances;
    private HashMap<String, NetworkState> stateVariables;
    //Mapping from unique identification strings (variable names) to their corresponding VertexFunction instances;
    private HashMap<String, VertexFunction> vertexFunctionVariables;
    //The MIDI instrument number for this network
    private Integer instrumentIndex = 0;

    //Produces the noises...
    private transient SoundMaker soundMaker;

    //A name for the network. Should be unique, but this isn't enforced.
    private String name;

    private Boolean resting = false;


    /*
     * CONSTRUCTOR and INITIALISATION
     */

    /**
     * The BooleanNetwork object combines all the information about the structure of the network, each
     * of its nodes' vertex functions, and the musicalisation through MIDI. It also exposes methods for
     * constructing and modifying the network, as well as computing its evolution.
     *
     * @param soundMaker The SoundMaker instance which will take care of sequencing the MIDI for this network.
     */
    public BooleanNetwork(SoundMaker soundMaker, String name) {
        this.soundMaker = soundMaker;
        this.name = name;
        initialiseNetwork();
    }

    /**
     * Perform the initial network setup
     */
    private void initialiseNetwork() {
        this.transportPosition = 0;
        this.bufferPosition = 0;
        this.nodes = new HashMap<String, BooleanNode>();
        this.stateVariables = new HashMap<>();
        this.vertexFunctionVariables = new HashMap<>();
    }

    void clearEverything() {
        this.transportPosition = 0;
        this.bufferPosition = 0;
        instrumentIndex = 0;
        this.nodes.clear();
        this.stateVariables.clear();
        this.vertexFunctionVariables.clear();
        this.soundMaker.clear();
        Identifier_Node.clear();
    }

    /*
     * NETWORK BUILDING and MODIFICATION
     */

    /**
     * Add a node to the network, with default (all false) vertex function.
     *
     * @param initialState The state this node has at time-step 0
     * @param noteName     The string name (e.g. "G#3", which has MIDI value 68) of the note this node plays when active
     * @param label        A unique name for this node.
     * @return The newly created BooleanNode instance
     * @throws Exception If the given label has already been assigned to another node
     */
    public BooleanNode addNode(boolean initialState, String noteName, String label) throws Exception {
        BooleanNode node = new BooleanNode(this, initialState, noteName, label);
        this.nodes.put(node.getID().getUUID().toString(), node);

        //When a new node is added, its rules may change the network dynamics,
        //so we need to clear the previously computed states.
        clearNodeBuffers();

        return node;
    }


    /**
     * Add a new named NetwokState to the collection. These can be used during sequencing to start the network from a particular state.
     *
     * @param stateName  The unique name for the state
     * @param nodeLabels The nodes making up the state definition (they will all be set to have initial state = state:boolean).
     * @param state      The initial state to be applied to the given nodes (all other nodes will have the opposite state)
     * @throws Exception If the given name has already been used.
     */
    public void addStartState(String stateName, ArrayList<String> nodeLabels, boolean state) throws Exception {
        ArrayList<Identifier_Node> nodeIDs = new ArrayList<>();
        for (String label : nodeLabels) {
            nodeIDs.add(Identifier_Node.getIdentifierByLabel(this.name, label));
        }
        NetworkState networkState = new NetworkState(stateName, nodeIDs, state);
        this.stateVariables.put(stateName, networkState);
    }

    /**
     * Set the starting state of the network by specifying a collection of nodes whose
     * values will all be set to either true or false. The remaining nodes will be set to the opposite state.
     *
     * @param nodeLabels The nodes whose state states will equal 'state'. The other nodes will have the opposite start state to 'state'.
     * @param state      The state in which to start the given nodes.
     */
    public void setStartState(ArrayList<String> nodeLabels, boolean state) {
        for (BooleanNode node : this.nodes.values()) {
            node.setInitialState(!state);
        }
        for (String label : nodeLabels) {
            this.getNodeByID(Identifier_Node.getIdentifierByLabel(this.name, label)).setInitialState(state);
        }
        clearNodeBuffers();
    }

    /**
     * Set the start state according to the NetworkState variable with the given name
     *
     * @param stateName The name of the NetworkState instance to be used
     */
    public void setStartState(String stateName) {
        if(stateName!=null) {
            resting = false;
            NetworkState networkState = stateVariables.get(stateName);
            boolean state = networkState.getTruthValue();
            ArrayList<Identifier_Node> nodeIDs = networkState.getNodeIDs();

            for (BooleanNode node : this.nodes.values()) {
                node.setInitialState(!state);
            }
            for (Identifier_Node nodeID : nodeIDs) {
                this.getNodeByID(nodeID).setInitialState(state);
                System.out.println("Set " + nodeID.getLabel() + " to " + state);
            }
            clearNodeBuffers();
        }else{
            resting = true;
        }
    }


    /**
     * Add a new named vertex function to the collection (name is a property of the vertex function instance)
     *
     * @param arity        The arity of the vertex function
     * @param name         The unique name for the new vertex function
     * @param defaultValue The default output of the function, mapped to by inputs whose mapping is not explicitly set.
     * @throws Exception If the given name is already in use by another named vertex function
     */
    public VertexFunction addVertexFunction(int arity, String name, String[] inputNames, boolean defaultValue) throws Exception {
        if (!this.vertexFunctionVariables.containsKey(name)) {
            VertexFunction vf = new VertexFunction(arity, name, inputNames, defaultValue);
            this.vertexFunctionVariables.put(name, vf);
            return vf;
        } else {
            DialogMaker.showErrorDialog("Duplicate Vertex Function Name","The vertex function name '" + name + "' is already in use.");
            throw new Exception("The vertex function name '" + name + "' is already in use.");
        }
    }

    /**
     * Set the vertex function of the given node and update its inputs
     *
     * @param nodeName           The name of the node whose vertex function to set
     * @param vertexFunctionName The name of the vertex function to be assigned to this node
     * @param inputNodeNames     The names of the nodes whose values will be taken as inputs to the vertex function.
     */
    public void setNamedNodeVertexFunction(String nodeName, String vertexFunctionName, ArrayList<String> inputNodeNames) {
        BooleanNode node = this.nodes.get(Identifier_Node.getIdentifierByLabel(this.name, nodeName).getUUID().toString());
        VertexFunction vertexFunction = this.vertexFunctionVariables.get(vertexFunctionName);

        BooleanNode[] inputs = new BooleanNode[inputNodeNames.size()];
        for (int i = 0; i < inputNodeNames.size(); i++) {
            inputs[i] = getNodeByID(Identifier_Node.getIdentifierByLabel(this.name, inputNodeNames.get(i)));

        }

        if (vertexFunction.getArity() == inputs.length) {
            node.setVertexFunction(vertexFunction);
            node.setInputNodes(inputs);
        } else {
            DialogMaker.showErrorDialog("Vertex Function Arity Error",
                    "The given input array does not match the arity of the vertex function '" + vertexFunctionName +"' . Input count: " + Integer.toString(inputs.length) + ", Expected: " + Integer.toString(vertexFunction.getArity()));
            throw new IllegalArgumentException("The given input array does not match the arity of the vertex function '" + vertexFunctionName +"' . Input count: " + Integer.toString(inputs.length) + ", Expected: " + Integer.toString(vertexFunction.getArity()));
        }
    }



    /*
     * COMPUTING NETWORK DYNAMICS
     */

    /**
     * Compute the states of the network up to the given time-step. Each node stores its own series of states.
     *
     * @param endPosition The time-step at which to stop computing the series of network states
     */
    //TODO use concurrency here? Also detect loop/steady state
    private void bufferNetwork(int endPosition) {
        if (endPosition > this.bufferPosition) {
            for (BooleanNode bn : this.nodes.values()) {
                bn.bufferStates(endPosition);
            }
            this.bufferPosition = endPosition;
        }
    }

    /**
     * Have each node add its current buffered data to a list. This will be saved even if the node is re-buffered.
     * We can use it as a history of the nodes' states throughout a sequence with several initialisations and re-bufferings.
     */
    void saveNetworkBuffers(int duration, boolean isResting) {

        for (BooleanNode bn : this.nodes.values()) {
            bn.saveStateBuffer(duration,isResting);
        }
    }

    /**
     * Delete the computed series of network states for every node, re-adding only the initial state to the series.
     */
    private void clearNodeBuffers() {
        for (BooleanNode bn : this.nodes.values()) {
            bn.clearBuffer();
        }
        this.bufferPosition = 0;
    }

    /*
     * SOUND, SEQUENCING and PLAYBACK
     */

    /**
     * Line up the sequence of notes in the sequencer, ready to be sent to the synthesiser. The sequence
     * is calculated from the precomputed states in the state buffer for each node.
     *
     * @param startStep the time step at which to start the sequence
     * @param howManySteps The number of steps from the data for which to sequence it
     * @throws InvalidMidiDataException When {@link SoundMaker#sequenceNodeData(ArrayList, int, Integer, String, Boolean)} does so.
     */
    private void sequenceNodeNotes(int startStep, int howManySteps) throws InvalidMidiDataException {
        //All the beats for a single node (all its sequential states) are sequenced at once.
        //This flag to indicates that one node has already been sequenced.
        //It is used to prevent multiple MetaMessages being sent by the SoundMaker for each beat,
        //when each node passes that beat.
        Boolean sentOne = false;
        for (BooleanNode node : this.nodes.values()) {
            Integer noteIndex = node.getMidiNote();
            ArrayList<Integer> noteIndices = new ArrayList<Integer>(howManySteps);
            for (int i = 0; i <= howManySteps; i++) {
                if (!resting) {
                    if (node.getStateAtTransportPosition(i)) {
                        noteIndices.add(noteIndex);
                    } else {
                        noteIndices.add(null);
                    }
                } else {
                    noteIndices.add(null);
                }
            }
            this.soundMaker.sequenceNodeData(noteIndices, startStep, instrumentIndex, this.name, !sentOne);
            sentOne = true;
        }

    }

    /**
     * Have the SoundMaker instance for this network attempt to load the synthesiser and receive
     * the stream of notes from the sequencer. (i.e. Play the music)
     *
     * @param steps the number of time steps for which the network should be played.
     * @throws InvalidMidiDataException
     */
    public void playFor(int steps) throws InvalidMidiDataException {
        if (bufferPosition < steps) {
            bufferNetwork(steps);
        }
        sequenceNodeNotes(0, steps);
        saveNetworkBuffers(bufferPosition,false);
        printStateBuffer();
        this.soundMaker.play();
    }


    public void play() throws InvalidMidiDataException, MidiUnavailableException {
        this.soundMaker.play();
    }

    /**
     * Sequence the data, buffering the network if necessary, starting the position in the playback sequence from startStep
     * and sequencing 'duration' number of beats. For instance, sequence from startStep 4 for duration 6 will add 6 notes (or rests)
     * to the sequence at time steps 4,5,...,9 in turn.
     * @param duration
     * @param startStep
     * @throws InvalidMidiDataException
     */
    public void sequenceForDurationFromStep(Integer duration, Integer startStep) throws InvalidMidiDataException {
            if (bufferPosition < duration) {
                bufferNetwork(duration);
            }
            sequenceNodeNotes(startStep, duration);
    }


    /*
     * GETTERS and SETTERS
     */

    /**
     * Set the note for the node with the given name, as identified during parsing of a network file. The note is given
     * as a string in scientific pitch notation like, for example, "F#6", "C2", or "Bb4".
     * @param nodeName
     * @param note
     */
    public void setNoteForNamedNode(String nodeName, String note) {
        this.getNodeByID(Identifier_Node.getIdentifierByLabel(this.name, nodeName)).setMIDINote(note);
    }

    /**
     * Set which instrument the network should be assigned during playback. Uses Java MIDI (standard MIDI) instrument numbers.
     * @param instrumentIndex
     */
    public void setInstrumentIndex(Integer instrumentIndex) {
        this.instrumentIndex = instrumentIndex;
    }

    /**
     * @param id The Identifier uniquely associated with the BooleanNode to be found
     * @return The BooleanNode instance with the given Identifier
     */
    public BooleanNode getNodeByID(Identifier_Node id) {
        return this.nodes.get(id.getUUID().toString());
    }

    /**
     * @return Collection containing all the BooleanNodes in this network
     */
    public Collection<BooleanNode> getAllNodes() {
        return this.nodes.values();
    }

    /**
     * @return The name string for this network instance
     */
    public String getName() {
        return name;
    }

    /**
     * @return the SoundMaker instance associated with this network.
     */
    public SoundMaker getSoundMaker() {
        return this.soundMaker;
    }

    /**
     * @param historic Should the buffered states be from the current node buffer (false), or the list of saved node buffers (true)
     * @param sort Should the returned states be sorted by their labels
     * @return A pair containing a list of ordered arrays of the states of each node (through all buffered positions)
     * and an array of string labels for each node.
     * Sort with silent nodes first in alphabetical order and musical nodes last in order of ascending pitch.
     */
    public Pair<ArrayList<NodeState[]>, Identifier_Node[]> getBufferedStates(boolean sort, boolean historic) {
        int nodeCount = this.nodes.values().size();
        ArrayList<NodeState[]> nodeStates = new ArrayList<>();
        Identifier_Node[] identifierNodes = new Identifier_Node[nodeCount];
        BooleanNode[] nodes;
        System.out.println(nodeCount);

        if (sort) {
            ArrayList<BooleanNode> sortedNodes = new ArrayList<>();
            sortedNodes.addAll(this.nodes.values());

            sortedNodes.sort((bn1, bn2) -> {
                Integer bn1Note = bn1.getMidiNote();
                Integer bn2Note = bn2.getMidiNote();
                if (bn1Note != null && bn2Note != null) {
                    return bn1Note - bn2Note;
                } else if (bn1Note != null) {
                    return bn1Note;
                } else if (bn2Note != null) {
                    return -bn2Note;
                } else {
                    return Collator.getInstance().compare(bn1.getID().getLabel(), bn2.getID().getLabel());
                }
            });
            nodes = sortedNodes.toArray(new BooleanNode[0]);
        } else {
            nodes = this.nodes.values().toArray(new BooleanNode[0]);
        }

        for (int nodeIndex = 0; nodeIndex < nodeCount; nodeIndex++) {
            BooleanNode node = nodes[nodeIndex];
            identifierNodes[nodeIndex] = node.getID();

            int maxIndex;
            if (!historic) {
                maxIndex = node.getNodeBufferPosition();
            } else {
                maxIndex = node.getSavedStateCount();
            }

            for (int pos = 0; pos < maxIndex; pos++) {
                if (nodeStates.size() < pos + 1) {
                    nodeStates.add(new NodeState[nodeCount]);
                }
                if (nodeStates.get(pos) == null) {
                    nodeStates.add(new NodeState[nodeCount]);
                }
                NodeState s = NodeState.InactiveSilent;
                boolean currentNodeState;
                if (!historic) {
                    currentNodeState = node.getStateAtTransportPosition(pos);
                }else{
                    currentNodeState = node.getSavedStateAtTransportPosition(pos);
                }
                if (currentNodeState == true) {
                    if (node.isSilent()) {
                        s = NodeState.ActiveSilent;
                    } else {
                        s = NodeState.ActivePlay;
                    }
                } else {
                    if (node.isSilent()) {
                        s = NodeState.InactiveSilent;
                    } else {
                        s = NodeState.InactivePlay;
                    }
                }
                nodeStates.get(pos)[nodeIndex] = s;
            }
        }
        return new Pair<ArrayList<NodeState[]>, Identifier_Node[]>(nodeStates, identifierNodes);
    }
    /*
     * ADDITIONAL UTILITIES
     */

    /**
     * Print the true/false values for all the notes at each point in time, up to the maximum currently
     * computed time-step
     */
    public void printStateBuffer() {
        for (int i = 0; i <= bufferPosition; i++) {
            for (BooleanNode bn : this.nodes.values()) {
                System.out.print(bn.getStateAtTransportPosition(i));
                System.out.print(" | ");
            }
            System.out.println("\n");
        }
    }

}
