package BoolNets;

import Parser.DialogMaker;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A node in the boolean network. A node has a particular state at a particular time step.
 * The state at time step n is determined by the value of the node's vertex function
 * (the evaluation of which which we refer to as "buffering"). The inputs of the vertex
 * function for this node are the other nodes to which this node connects in the boolean network.
 * The node may also have a musical note associated with it. As the evolution of the network is traced forwards in time,
 * one time step corresponds to one musical beat. If the node is in the "true" state at time n, the corresponding musical note will be played.
 * If the node was in the "true" state at time n-1, the note will be sustained. Otherwise it will begin playing until the timestep at which
 * the node's state becomes "false". Nodes with no assigned note are called "silent", and they do not result in the production
 * of a sound. They can however influence the dynamics/evolution of the network.
 */
public class BooleanNode {

    private BooleanNetwork parentNetwork;

    //Unique identifier for this node instance
    private Identifier_Node ID;

    //The state for this node at time step 0.
    private boolean initialState;

    //The musical note that this node will "play" (or, more properly, cause to be sequenced) when true.
    private MIDINote midiNote;

    //All the states that this node will have at each time step. Calculated dynamically.
    private ArrayList<Boolean> states = new ArrayList<Boolean>();

    //A collection of states copied from the 'states' array.
    //'states' will be overwritten when the node's states are sequenced from different start states,
    //so this allows us to retain some or all of the already sequenced data
    private ArrayList<Boolean> savedStates = new ArrayList<>();

    //Which other nodes to take as inputs to this node
    private BooleanNode[] inputNodes;

    //The vertex function that defines the dynamical behaviour of this node.
    private VertexFunction vertexFunction;

    /**
     *
     * @param parentNetwork
     * @param initialState The initial state for this node at time step 0.
     * @param noteName Which note this node should play, in scientific pitch notation. See {@link MIDINote}.
     * @param label A name for this node.
     * @throws Exception
     */
    BooleanNode(BooleanNetwork parentNetwork, boolean initialState, String noteName, String label) throws Exception {
        this.parentNetwork = parentNetwork;
        this.ID = new Identifier_Node(parentNetwork.getName(), label);
        this.initialState = initialState;
        try {
            this.midiNote = new MIDINote(noteName);
        }catch (ValueException e){
            DialogMaker.showErrorDialog("Note Name Error", "The note name " + noteName + " is not a valid MIDI note name.");
            e.printStackTrace();
        }
        this.states.add(this.initialState);
        //Set up a default vertex function and input consisting of this node only.
        this.inputNodes = new BooleanNode[]{this};
        this.vertexFunction = new VertexFunction(1, ID.getLabel() + "_default", new String[]{"self"}, false);
    }

    public void setVertexFunction(VertexFunction vertexFunction) {
        this.vertexFunction = vertexFunction;
    }

    /**
     * Which other BooleanNodes should be taken as inputs to this node. These inputs are also taken as inputs to the
     * vertex function, so their order matters.
     * @param inputNodes
     */
    public void setInputNodes(BooleanNode[] inputNodes) {
        this.inputNodes = inputNodes;
    }


    /**
     * Add some of the already existing buffered data to the end of another list so that we can use it
     * if the node is reset to a different start state and buffered again. The saved data can be used as a
     * sequence of multiple "runs" of the node's evolution, possibly starting from different start states.
     * @param duration How much of the data to save (starting from time step 0).
     * @param isResting If false, the real buffered data is saved; if true, blank (all false) data is saved.
     *                  This is used in sequencing, when we want to have the node's playback pause, exactly
     *                  like a rest in a piece of music.
     */
    public void saveStateBuffer(int duration, Boolean isResting){
        if(!isResting) {
            this.savedStates.addAll(this.states.subList(0, duration));
        }else{
            ArrayList<Boolean> extension = new ArrayList<>(Collections.nCopies(duration,false));
            this.savedStates.addAll(extension);
        }
    }

    /**
     * Compute the states occupied by this node until the given time step. Will (indirectly) trigger other
     * BooleanNode instances to buffer if their state values are needed.
     * @param endTimeStep
     */
    public void bufferStates(int endTimeStep) {
        int currentBufferPosition = getNodeBufferPosition();
        if (endTimeStep > currentBufferPosition) {
            for (int i = currentBufferPosition; i < endTimeStep; i++) {
                this.states.add(this.vertexFunction.evaluate(inputNodes, i));
            }
        }
    }

    /**
     * Delete all the buffered data (except the initial state)
     */
    protected void clearBuffer() {
        this.states.clear();
        this.states.add(this.initialState);
    }

    /**
     * @return The frequency with which, in the currently buffered data, the node's value is "true"
     */
    public int getTrueFrequency() {
        int frequency = 0;
        int stateCount = this.getNodeBufferPosition();
        for (int i = 0; i < stateCount; i++) {
            if (this.getStateAtTransportPosition(i)) {
                frequency += 1;
            }
        }
        return frequency;
    }

    public int getNodeBufferPosition() {
        return this.states.size() - 1;
    }

    public ArrayList<Boolean> getSavedStates(){
        return savedStates;
    }

    public Integer getSavedStateCount(){
        return this.savedStates.size();
    }

    /**
     *
     * @param position The time step at which to find the state of this node.
     * @return The state of this node at the given time step
     */
    public Boolean getStateAtTransportPosition(Integer position) {
        if (position < states.size()) {
            return states.get(position);
        } else {
            this.bufferStates(position);
            return states.get(position);
        }
    }

    /**
     * From the list of saved state values, which may represent a sequence of
     * several successive "runs" of the node's evolution, return the state at the given time step.
     * @param position
     * @return The saved state at the given time step
     */
    public Boolean getSavedStateAtTransportPosition(Integer position){
        return savedStates.get(position);
    }

    protected void setMIDINote(String noteName) {
        try {
            this.midiNote = new MIDINote(noteName);
        }catch (ValueException e){
            DialogMaker.showErrorDialog("Note Name Error", "The note name " + noteName + " is not a valid MIDI note name.");
            e.printStackTrace();
        }

    }

    public Integer getMidiNote() {
        return this.midiNote.getMIDINoteIndex();
    }

    public void printVertexFunctionRules() {
        this.vertexFunction.printRules();
    }

    public Identifier_Node getID() {
        return ID;
    }

    public boolean isSilent() {
        return this.getMidiNote() == null;
    }

    public void setInitialState(boolean initialState) {
        this.initialState = initialState;
    }


    public void clear() {
        this.midiNote = null;
        this.inputNodes = null;
        this.vertexFunction.clear();
        this.vertexFunction = null;
        this.ID = null;
        this.states.clear();
    }

}
