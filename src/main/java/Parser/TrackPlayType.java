package Parser;

/**
 * Tags the {@link TrackDefinition_TreeNode} of a parsed track to tell the the {@link Interpreter} that the track either
 * requires start state data to be specified (in the case of PLAY) or should use a null start state (in the case of REST)
 */
public enum TrackPlayType {
    PLAY,
    REST;
}
