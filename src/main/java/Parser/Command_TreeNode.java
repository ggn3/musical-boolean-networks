package Parser;

import Parser.CommandType;
import org.parboiled.trees.MutableTreeNodeImpl;

import java.util.ArrayList;

public class Command_TreeNode extends MutableTreeNodeImpl {
    CommandType commandType;
    String stateName;
    Integer bufferLength;
    Integer instrumentIndex;
    ArrayList<String> tracks;


    //Play tracks command
    public Command_TreeNode(CommandType commandType, ArrayList<String> tracks){
        this.commandType = commandType;
        this.tracks = tracks;
    }

    //Play single network command
    public Command_TreeNode(CommandType commandType, String stateName, Integer bufferLength){
        this.commandType = commandType;
        this.stateName = stateName;
        this.bufferLength = bufferLength;
    }

    //Set instrument command
    public Command_TreeNode(CommandType commandType, Integer instrumentIndex){
        this.commandType = commandType;
        this.instrumentIndex = instrumentIndex;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getStateName() {
        return stateName;
    }

    public Integer getBufferLength() {
        return bufferLength;
    }

    public Integer getInstrumentIndex() {
        return instrumentIndex;
    }

    public ArrayList<String> getTracks() {
        return tracks;
    }
}
