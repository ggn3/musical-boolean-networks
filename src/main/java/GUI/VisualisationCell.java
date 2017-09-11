package GUI;


import BoolNets.BooleanNetwork;
import BoolNets.BooleanNode;
import BoolNets.Identifier_Node;
import BoolNets.NodeState;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import Sound.SoundMaker;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.HashMap;

/**
 * A {@link Rectangle} with some extra properties - An associated {@link BooleanNode}, a {@link Tooltip} displaying
 * the name of the associated node, a {@link NodeState} defining how this object should be displayed graphically and a
 * reference to the {@link SoundMaker} instance used to produce a Sound when the rectangle is clicked.
 */
public class VisualisationCell extends Rectangle {
    private int x;
    private int y;

    private BooleanNode node;
    private NodeState state;

    private Tooltip t;

    private SoundMaker soundMaker;

    private EventHandler<MouseEvent> clickHandler;

    //Tooltip instances take up a lot of memory. We want to reuse them when a network is reloaded or we have duplicate tooltips.
    //This HashMap associates the name of a node with the appropriate ToolTip object so we can find the tooltip knowing only its label.
    private static HashMap<String, Tooltip> stringTooltipHashMap = new HashMap<>();

    /**
     * Create the cell at the given location with visual properties set out according the the parameters below.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param state {@link NodeState#ActivePlay} results in a light blue cell,
     *              {@link NodeState#ActiveSilent} results in a light gray cell,
     *              {@link NodeState#InactivePlay} results in a dark gray cell,
     *              {@link NodeState#InactiveSilent} results in a black cell,
     * @param nodeID ID for the node for which this cell is a visualisation
     * @param net Reference to the {@link BooleanNetwork} of which this node is part
     * @throws InvalidMidiDataException When {@link SoundMaker#playNote(int)} does so.
     * @throws MidiUnavailableException When {@link SoundMaker#playNote(int)} does so.
     */
    VisualisationCell(int x, int y, int width, int height, NodeState state, Identifier_Node nodeID, BooleanNetwork net) throws InvalidMidiDataException, MidiUnavailableException {
        super(x,y,width,height);

        this.setStrokeWidth(1);
        this.setStroke(Color.WHITE);

        setState(state);

        node = net.getNodeByID(nodeID);

        if(stringTooltipHashMap.containsKey(nodeID.getLabel())){
            t = stringTooltipHashMap.get(nodeID.getLabel());
        }else{
            t = new Tooltip(nodeID.getLabel());
            stringTooltipHashMap.put(nodeID.getLabel(),t);
        }

        Tooltip.install(this, t);


        clickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                    Integer note = node.getMidiNote();
                    if(note!=null) {
                        net.getSoundMaker().playNote(node.getMidiNote());
                    }
            }
        };
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

    }


    /**
     * Used to set the data for this VisualisationCell. Could be useful if VisualisationCell instances were to be re-used when
     * networks/sequences are reloaded
     * @param x
     * @param y
     * @param width
     * @param height
     * @param state
     * @param nodeID
     * @param net
     * @throws InvalidMidiDataException
     * @throws MidiUnavailableException
     */
    public void setData(int x, int y, int width, int height, NodeState state, Identifier_Node nodeID, BooleanNetwork net) throws InvalidMidiDataException, MidiUnavailableException {
        setState(state);
        this.setStrokeWidth(1);
        this.setStroke(Color.WHITE);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        node = net.getNodeByID(nodeID);
        t.setText(nodeID.getLabel());

        this.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);
        clickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                Integer note = node.getMidiNote();
                if(note!=null) {
                    net.getSoundMaker().playNote(node.getMidiNote());
                }

            }
        };
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);

    }

    /**
     * Set the state of this node, changing its visual properties accordingly.
     * @param state
     */
    private void setState(NodeState state){
        switch (state) {
            case ActivePlay:
                this.setFill(Color.POWDERBLUE);
                break;
            case ActiveSilent:
                this.setFill(Color.SLATEGRAY);
                break;
            case InactivePlay:
                this.setFill(Color.DARKGRAY);
                break;
            case InactiveSilent:
                this.setFill(Color.BLACK);
                break;
        }

        this.state = state;
    }


    public void clear(){
        soundMaker = null;
        node = null;
        clickHandler = null;
    }

}
