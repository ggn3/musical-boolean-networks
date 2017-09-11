package Sound;

import GUI.VisualisationRoll;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Listener for MIDI MetaEvents triggered when the {@link SoundMaker} instance begins playback of the MIDI sequence.
 * Used to trigger updates for the visualisation components of the GUI.
 */
public class MIDIMetaListener implements MetaEventListener {
    VisualisationRoll currentVisRoll;
    HashMap<String, VisualisationRoll> visualisationRollHashMap = new HashMap<>();

    /**
     * Associate the given network (identified by name) with the given {@link VisualisationRoll} instance.
     * When a meta message is received, it contains the name of the network responsible the beat
     * on which the meta message was sent, along with the beat number. See {@link SoundMaker#sequenceNodeData(ArrayList, int, Integer, String, Boolean)}
     * @param networkName
     * @param visualisationRoll
     */
    public void addVisualisationRoll(String networkName, VisualisationRoll visualisationRoll) {
        visualisationRollHashMap.put(networkName, visualisationRoll);
    }

    /**
     * What to do when a MetaMessage is received.
     *
     * Extracts the data, assuming that the message contained the name of a boolean network and a positive integer
     * as a comma-separated string. This data is the name of the network associated with the current beat
     * along with the number of that beat in the sequence.
     * @param meta The received MetaMessage
     */
    @Override
    public void meta(MetaMessage meta) {

        String[] data = (new String(meta.getData())).split(",");

        String netName = data[0];
        Integer tickValue = Integer.parseInt(data[1]);

        currentVisRoll = visualisationRollHashMap.get(netName);
        currentVisRoll.tick(tickValue);


        System.out.println(tickValue);


    }

    public void clear() {
        currentVisRoll = null;
        visualisationRollHashMap.clear();
    }
}
