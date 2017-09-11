package Parser;

import org.parboiled.trees.MutableTreeNodeImpl;

import java.util.ArrayList;

public class TrackDefinition_TreeNode extends MutableTreeNodeImpl{
    String trackName;
    String netFileName;
    ArrayList<TrackPlayType> playTypes = new ArrayList<>();
    ArrayList<String> startStates = new ArrayList<>();
    ArrayList<Integer> durations = new ArrayList<>();

    public TrackDefinition_TreeNode(String trackName, String netFileName, ArrayList<TrackPlayType> playTypes, ArrayList<String> startStates, ArrayList<Integer> durations){
        this.trackName = trackName;
        this.netFileName = netFileName;
        this.playTypes = playTypes;
        this.startStates = startStates;
        this.durations = durations;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getNetFileName() {
        return netFileName;
    }

    public ArrayList<TrackPlayType> getPlayTypes() {
        return playTypes;
    }

    public ArrayList<String> getStartStates() {
        return startStates;
    }

    public ArrayList<Integer> getDurations() {
        return durations;
    }
}

