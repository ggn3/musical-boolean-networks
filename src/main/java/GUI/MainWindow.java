package GUI;

import BoolNets.BooleanNetwork;
import BoolNets.Identifier_Node;
import BoolNets.NetworkCollection;
import BoolNets.NodeState;
import Parser.Interpreter;
import Utilities.Pair;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Sound.MIDIMetaListener;
import Sound.SoundMaker;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The main GUI for the player
 */
public class MainWindow extends Application {

    //Responsible for producing and using the Java MIDI objects including sequencer and synthesiser
    SoundMaker soundMaker;

    //All the different BooleanNetwork instances involved in the current sequence file are encapsulated in this object
    NetworkCollection networkCollection;

    //Reusable container variable where element a is an n-element collection of arrays of pre-computed states for each node,
    //and element b is an n-element array of the identifier objects associated with each node
    Pair<ArrayList<NodeState[]>, Identifier_Node[]> bufferedData;

    //Reusable container for an iterator through which all the BooleanNetwork instances in 'networkCollection' can be accessed
    Iterator<BooleanNetwork> networkIterator;

    //This JavaFX GUI TabPane is re-initialised when a new file is loaded
    TabPane tabPane;

    //Stores associations between a network's name string with a corresponding visualisation object to be used in the GUI
    HashMap<String, VisualisationRoll> visRolls;

    //Passed into the SoundMaker instance, listens for all the Java sequencer's MetaEvents which it uses to trigger changes in the GUI visualisation.
    MIDIMetaListener midiMetaListener;

    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Musical Boolean Network Player");

        midiMetaListener = new MIDIMetaListener();
        soundMaker = new SoundMaker(midiMetaListener);
        networkCollection = new NetworkCollection(soundMaker);
        visRolls = new HashMap<>();

        BorderPane root = new BorderPane();
        tabPane = new TabPane();
        ToolBar toolBar = new ToolBar();
        Button reload = new Button("Reload");
        TextField fileField = new TextField();
        Button openButton = new Button("Open");
        Button exportMidiButton = new Button("Export MIDI");

        toolBar.getItems().add(reload);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(fileField);
        toolBar.getItems().add(openButton);
        toolBar.getItems().add(exportMidiButton);

        root.setTop(toolBar);
        root.setCenter(tabPane);
        stage.setScene(new Scene(root, 400, 600));

        //Open button press. Just allows a filepath to be selected.
        //Opening the file is handled when the reload button is pressed
        FileChooser fileChooser = new FileChooser();
        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(stage);
                        if (file != null) {
                            fileField.setText(file.getAbsolutePath());
                        }
                    }
                });

        //Export button press
        exportMidiButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showSaveDialog(stage);
                        soundMaker.saveSequence(file);
                    }
                });

        //Reload button press
        reload.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        try {
                            runCode(fileField.getText());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        stage.show();
    }


    /**
     * Insert a tab containing a visualisation of the pre-computed state data from the given {@link BoolNets.BooleanNetwork}
     * @param bn The {@link BoolNets.BooleanNetwork} whose buffered data should be visualised
     * @throws InvalidMidiDataException
     * @throws MidiUnavailableException
     */
    private void addTab(BooleanNetwork bn) throws InvalidMidiDataException, MidiUnavailableException {
        Tab tab = new Tab();

        //See if there already is a VisualisationRoll instance with this name that we can reuse
        VisualisationRoll vRoll = visRolls.get(bn.getName());
        if (vRoll == null) {
            vRoll = new VisualisationRoll();
            visRolls.put(bn.getName(), vRoll);
        }
        //Register the VisualisationRoll with our MIDI Meta-event listener so that it can be updated dynamically during playback
        midiMetaListener.addVisualisationRoll(bn.getName(), vRoll);

        bufferedData = bn.getBufferedStates(true, true);
        vRoll.addData(bufferedData.a, bufferedData.b, bn);
        System.out.println(bufferedData.a);
        ScrollPane scrollPane = new ScrollPane();
        Pane contentPane = new Pane();
        contentPane.getChildren().add(vRoll);
        scrollPane.setContent(contentPane);
        tab.setContent(scrollPane);
        tab.setText(bn.getName());
        tabPane.getTabs().add(tab);
    }


    /**
     * Clear any existing GUI elements, try to load the file from the given location, attempt to parse it,
     * interpret it and play it (if the file contained a play command).
     * @param fileLocation
     * @throws Exception
     */
    public void runCode(String fileLocation) throws Exception {
        soundMaker.clear();
        for (VisualisationRoll vr : this.visRolls.values()) {
            vr.clear();
        }
        tabPane.getTabs().clear();
        midiMetaListener.clear();

        Interpreter interpreter = new Interpreter();
        if (networkCollection != null) {
            networkCollection.clearEverything();
        }
        interpreter.parseAndRun(fileLocation, soundMaker, networkCollection);
        networkIterator = networkCollection.getNetworksIterator();

        //Prepare the data for playback, but don't play it yet
        networkCollection.getNetworkSequencer().sequenceTracks();

        while (networkIterator.hasNext()) {
            BooleanNetwork network = networkIterator.next();
            addTab(network);
        }

        //After the GUI is ready, start playing, if the interpreter encountered a play command
        networkCollection.getNetworkSequencer().playTracks();
    }

}
