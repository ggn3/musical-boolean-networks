package BoolNets;

import Sound.NetTrack;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSequencer {

    private static HashMap<String, NetTrack> trackVariables = new HashMap<>();

    private ArrayList<String> flaggedTracksToPlay = new ArrayList<>();
    /**
     * Add a named Track to this NetworkSequencer
     *
     * @param name    A unique name for the track
     * @param network The boolean network that this track gets its data from
     * @throws Exception When the given name has already been used
     */
    public void addTrack(String name, BooleanNetwork network) throws Exception {
        if (trackVariables.containsKey(name)) {
            throw new Exception("The track name " + name + " is already in use.");
        } else {
            this.trackVariables.put(name, new NetTrack(network));
        }
    }

    public NetTrack getTrackByName(String track) {
        return trackVariables.get(track);
    }

    /**
     * Clear all of the data associated with this NetworkSequencer.
     */
    public void clear() {
        for (NetTrack track : trackVariables.values()) {
            track.clear();
        }
        trackVariables.clear();
    }

    /**
     * Add the data from the specified track's network to the MIDI sequencer
     *
     * @param trackName The name of the track to sequence
     * @throws InvalidMidiDataException
     */
    public void sequenceTrack(String trackName) throws InvalidMidiDataException, MidiUnavailableException {
        NetTrack track = getTrackByName(trackName);
        ArrayList<String> startStates = track.getStates();
        ArrayList<Integer> durations = track.getSteps();
        BooleanNetwork net = track.getNetwork();
        for (int i = 0; i < startStates.size(); i++) {
            int lastEnd = 0;
            if (i > 0) {
                lastEnd = sumIntArray(durations, 0, i - 1);
            }
            String startState = startStates.get(i);
            //Sequence the track if the start state is non-null
            track.getNetwork().setStartState(startState);
            net.sequenceForDurationFromStep(durations.get(i), lastEnd);
            track.getNetwork().saveNetworkBuffers(durations.get(i), startState == null);
        }
    }

    public void flagPlayTracks(ArrayList<String> trackNames){
        this.flaggedTracksToPlay = trackNames;
    }

    public void sequenceTracks() throws InvalidMidiDataException, MidiUnavailableException {
        ArrayList<String> trackNames = flaggedTracksToPlay;
        if(trackNames.size()>0) {
            for (String trackName : trackNames) {
                sequenceTrack(trackName);
            }
        }
    }

    public void playTracks() throws InvalidMidiDataException, MidiUnavailableException {
        ArrayList<String> trackNames = flaggedTracksToPlay;
        if(trackNames.size()>0) {
            NetTrack track = getTrackByName(trackNames.get(0));
            track.getNetwork().getSoundMaker().play();
        }
        flaggedTracksToPlay.clear();
    }

    private int sumIntArray(ArrayList<Integer> array, int fromIndex, int toIndex) {
        int total = 0;
        for (int i = fromIndex; i <= toIndex; i++) {
            total += array.get(i);
        }
        return total;
    }
}
