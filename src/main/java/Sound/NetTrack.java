package Sound;

import BoolNets.BooleanNetwork;

import java.util.ArrayList;

/**
 * A NetTrack contains playback/timing information for a single (not necessarily unique) {@link BoolNets.BooleanNetwork}.
 * It specifies each of the start states the network should play from, and for how many time steps to play
 * from each start state in sequence. This sequential playback is not enforced here, but comes from the
 * implementation of {@link BoolNets.NetworkSequencer#sequenceTrack(String)}
 */
public class NetTrack {

    //The network associated with this track
    BooleanNetwork network;
    //from which start states to play the network
    ArrayList<String> states = new ArrayList<>();
    //How many steps each network should be played for
    ArrayList<Integer> steps = new ArrayList<>();

    public NetTrack(BooleanNetwork network){
        this.network = network;
    }
    public void addSequenceElement(String state, Integer steps){
        this.states.add(state);
        this.steps.add(steps);
    }

    public BooleanNetwork getNetwork() {
        return network;
    }

    public ArrayList<String> getStates() {
        return states;
    }

    public ArrayList<Integer> getSteps() {
        return steps;
    }

    public void clear(){
        this.states.clear();
        this.steps.clear();
    }
}
