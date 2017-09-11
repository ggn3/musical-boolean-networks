package GUI;

import BoolNets.BooleanNetwork;
import BoolNets.Identifier_Node;
import BoolNets.NodeState;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.ArrayList;

/**
 * A JavaFX {@link javafx.scene.Node} (- not to be confused with a {@link BoolNets.BooleanNode}) which can contain a graphical depiction of the sequence of states occupied by a network sequence.
 * The visualisation is a vertical collection of rows of cells. Each row has one cell corresponding to each node of a network.
 * The states of the cells reflect the states and properties of the network nodes, being turned on or turned off and
 * playing a note when on or playing no note when on. The successive rows progress down a scroll pane, with each row representing
 * the state of the network at a successive time step.
 *
 * This class has known issues with efficiency. Repeated instantiation of complex JavaFX nodes uses a lot of memory, causing the
 * GUI to lag and eventually crash when networks/sequences are repeatedly reloaded.
 */
public class VisualisationRoll extends StackPane {

    private final int CELL_WIDTH = 20;
    private final int CELL_HEIGHT = 20;
    //The circles mark out every four beats and are positioned at the left of the pane.
    private final int CIRCLE_RADIUS = 1;

    private ArrayList<VisualisationCell[]> cells = new ArrayList<>();
    private Group content = new Group();

    //An explicit px offset for every cell in the x-direction.
    private int offsetX = 0;
    //An offset in the y-direction by a number of cell-heights (not px values).
    private int rowOffsetY = 0;

    //A bar overlaid on top of the cells to indicate, during playback, where the player's current position is
    private Rectangle ticker;

    /**
     * Fill in the data with which the visualisation will be produced and make the visual objects for the GUI, if required.
     * @param data A list of {@link NodeState} arrays where the j-th element of the i-th array in the list is the state of a
     *             unique node in the network at time step i. We expect that each VisualisationRoll has data from only one
     *             {@link BooleanNetwork} instance, so this input data tracks the evolution of only one network.
     * @param nodeID An array of {@link Identifier_Node} instances corresponding positionally to the nodes
     *               whose {@link NodeState} arrays are given in the 'data' parameter. The ID in position n corresponds to the NodeState
     *               in position n of any and all of the NodeState arrays in 'data'.
     * @param net The {@link BooleanNetwork} instance that we assume provided the data for this VisualisationRoll
     * @throws InvalidMidiDataException When {@link VisualisationCell} does so.
     * @throws MidiUnavailableException When {@link VisualisationCell} does so.
     */
    public void addData(ArrayList<NodeState[]> data, Identifier_Node[] nodeID, BooleanNetwork net) throws InvalidMidiDataException, MidiUnavailableException {
        if (data.size() > 0) {

            int rowWidth = data.get(0).length;
            ticker = new Rectangle(CELL_WIDTH, 0, rowWidth * CELL_WIDTH, CELL_HEIGHT);
            ticker.setFill(Color.color(0, 0.6353, 0.698, 0.4));

            VisualisationCell[] rowCells = new VisualisationCell[rowWidth];
            content = new Group();
            for (int row = 0; row < data.size(); row++) {
                for (int col = 0; col < data.get(0).length; col++) {

                    VisualisationCell vc = new VisualisationCell((1 + col + offsetX) * CELL_WIDTH, (rowOffsetY + row) * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT, data.get(row)[col], nodeID[col], net);
                    content.getChildren().add(vc);
                    rowCells[col] = vc;

                    if (col == 0) {
                        if (row % 4 == 0) {
                            Circle c = new Circle(offsetX + CELL_WIDTH / 2, (CELL_WIDTH / 2) + (rowOffsetY + row) * CELL_HEIGHT, CIRCLE_RADIUS, Color.GRAY);
                            content.getChildren().add(c);
                        }
                    }
                }
                cells.add(rowCells);
            }
            rowOffsetY += data.size();
            content.getChildren().add(ticker);
            getChildren().add(content);

        }
    }


    /**
     * Set the position of the ticker bar (which should indicate on the visualisation diagram the location of the current network state)
     * @param tickerPos
     */
    public void tick(Integer tickerPos) {
        ticker.setY(tickerPos * CELL_HEIGHT);
        ticker.toFront();

    }

    public void clear() {
        cells.clear();
        content.getChildren().clear();
        rowOffsetY = 0;
        offsetX = 0;
        System.gc();

    }


}
