package Sound;

import Parser.DialogMaker;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static javax.sound.midi.MidiSystem.getSynthesizer;

/**
 * Responsible for controlling and interacting with Java's {@link Sequencer}, {@link Sequence} and {@link Synthesizer} objects to sequence the state data from the required
 * {@link BoolNets.BooleanNetwork} instances as a standard MIDI file.
 */
public class SoundMaker {
    //How many pulses happen for every quarter note in the sequencer.
    //A "Time Step" as used below means one quarter note.
    private static final int TICK_RESOLUTION = 4;

    //Currently we have a fixed, non-user-defined tempo and metre.
    private static final int TEMPO = 180;

    private static Sequencer sequencer;
    private static Sequence sequence;
    private static Synthesizer synth;

    private ShortMessage onMessage = new ShortMessage();
    private ShortMessage offMessage = new ShortMessage();

    //Listens for when MetaEvents are encountered during sequence playback
    private MIDIMetaListener midiMetaListener;

    //Used in bypassing the Sequencer for instant playback of a MIDI note
    private Receiver synthRcvr;

    //The current MIDI channel to use. Each instrument has a different channel, and 9 is reserved for percussion.
    private static Integer channelCount = 0;
    //Associates an instrument index with a channel number.
    private HashMap<Integer, Integer> instrumentChannelMap = new HashMap<>();
    //Associates an network name with a Track.
    private HashMap<String, Track> netTrackMap = new HashMap<>();

    //How many steps have been counted in total for each net. Used for scheduling MetaEvents.
    private HashMap<String, Integer> tickCountHashMap = new HashMap<>();


    public SoundMaker(MIDIMetaListener midiMetaListener) {
        this.midiMetaListener = midiMetaListener;
        try {
            sequencer = MidiSystem.getSequencer();
            if (sequence == null) {
                sequence = new Sequence(Sequence.PPQ, TICK_RESOLUTION);
            }
            if (!sequencer.isOpen()) {
                sequencer.open();
            }

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            DialogMaker.showMidiUnavailableDialog();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            //This exception will never occur
        }

    }

    /**
     * Load the given data into the sequencer, ready to be streamed to the synthesiser during playback.
     *
     * @param data            The series of MIDI note indices (pitches) to be played. A Null value is interpreted as silence - no note.
     *                        Two or more identical indices in direct succession will result in the note being sustained for the length
     *                        of the series of identical indices.
     * @param startStep       The time step from which the given notes will be played. The data is sequenced starting at that time location in the sequence.
     * @param instrumentIndex The instrument Sound to use for playing these notes. Uses standard Java MIDI indexing.
     * @param networkName     The name of the network from which the input data is derived. Used to trigger visualisations registered to the same network name.
     * @param sendMeta        Should MetaMessages be sent at every time step during playback of the sequence? They are used to trigger visualisation updates.
     * @throws InvalidMidiDataException
     */
    public void sequenceNodeData(ArrayList<Integer> data, int startStep, Integer instrumentIndex, String networkName, Boolean sendMeta) throws InvalidMidiDataException {
        //Every network has its own track, used for all nodes
        Track newTrack;
        if (!netTrackMap.containsKey(networkName)) {
            newTrack = sequence.createTrack();
            netTrackMap.put(networkName, newTrack);
        } else {
            newTrack = netTrackMap.get(networkName);
        }

        /*
         * INSTRUMENTS and CHANNELS
         */
        int channel = 9;//Default to percussion. Set to another instrument only when instrumentIndex is not null.

        if (instrumentIndex != null) {
            if (instrumentChannelMap.containsKey(instrumentIndex)) {
                //If the instrument has already been used, reuse the same channel that was assigned to it previously
                channel = instrumentChannelMap.get(instrumentIndex);
            } else {
                if (instrumentChannelMap.keySet().size() < 16) {
                    //If the instrument hasn't been used before, and we have space for new channels, assign a new channel to this instrument
                    instrumentChannelMap.put(instrumentIndex, channelCount);
                    channel = channelCount;
                    channelCount += 1;
                    if (channelCount == 9) {
                        channelCount += 1;
                    }
                } else {
                    throw new InvalidMidiDataException("Cannot assign any more network channels. Use an existing instrument/channel instead.");
                }
            }
        }

        if (synth == null) {
            try {
                synth = getSynthesizer();
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
                DialogMaker.showMidiUnavailableDialog();
            }
        }

        Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

        if (instrumentIndex != null) {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instr[instrumentIndex].getPatch().getProgram(), instr[instrumentIndex].getPatch().getBank());
            newTrack.add(new MidiEvent(msg, 0));
        }

        /*
         * SEQUENCING
         */
        //The exact number of ticks to after t=0 at which the current sequence data should start
        int offset = startStep * TICK_RESOLUTION;

        //An arbitrary delay before playback, stops lagging happening during the first few notes.
        int delay = 16;

        //Keep track of the number of time steps for which each network has been played in total.
        //Includes repeated playback, or playing from different start states.
        if (!tickCountHashMap.containsKey(networkName)) {
            tickCountHashMap.put(networkName, 0);
        }

        if (sendMeta) {
            //Set up a MetaMessage to send information to the visualiser about which notes are being played
            for (int i = 0; i < data.size() - 1; i++) {
                String message = networkName + "," + Integer.toString(tickCountHashMap.get(networkName));
                MetaMessage stringMessage = new MetaMessage(1, message.getBytes(), message.getBytes().length);
                int tick = offset + delay + i * TICK_RESOLUTION;
                MidiEvent meta = new MidiEvent(stringMessage, tick);
                newTrack.add(meta);
                tickCountHashMap.put(networkName, tickCountHashMap.get(networkName) + 1);
            }
        }

        //Sequence the notes!
        for (int start = 0; start < data.size() - 1; start++) {
            Integer note = data.get(start);
            if (note != null) {
                ShortMessage noteOn = new ShortMessage();
                noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, 93);
                ShortMessage noteOff = new ShortMessage();
                noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 93);

                MidiEvent noteOnEvent = new MidiEvent(noteOn, offset + delay + start * TICK_RESOLUTION);

                for (int end = start; end < data.size(); end++) {
                    //Counting how long each note is sustained for.
                    if (data.get(end) == null || end == data.size() - 1) {
                        MidiEvent noteOffEvent = new MidiEvent(noteOff, offset + delay + (end) * TICK_RESOLUTION);
                        newTrack.add(noteOnEvent);
                        newTrack.add(noteOffEvent);
                        start = end;
                        break;
                    }

                }
            }
        }
    }

    /**
     * Have the sequence object stream its contents to the synthesiser, thereby playing the sequenced music.
     */
    public void play() {
        try {
            if (synth == null) {
                synth = getSynthesizer();
            }
            if (!(synth.isOpen())) {
                synth.open();
            }
            if (!sequencer.isOpen()) {
                sequencer.open();
            }
            if (sequencer.getSequence() == null) {
                sequencer.setSequence(sequence);
            }
            sequencer.addMetaEventListener(midiMetaListener);
            sequencer.setTempoInBPM(TEMPO);
            sequencer.start();
        } catch (MidiUnavailableException e) {
            DialogMaker.showMidiUnavailableDialog();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            //This exception should never occur
        }
    }

    public void saveSequence(File file) {
        //We create a copy of the sequence which doesn't contain all the MetaMessage tags, and export it.
        if (sequencer != null) {
            try {
                Sequence seq_noMeta = new Sequence(Sequence.PPQ, TICK_RESOLUTION);
                for (Track t : sequence.getTracks()) {
                    Track newTrack = seq_noMeta.createTrack();
                    for (int i = 0; i < t.size(); i++) {
                        MidiEvent e = t.get(i);
                        if (e.getMessage().getStatus() != MetaMessage.META) {
                            newTrack.add(e);
                        }
                    }
                }
                int[] midiFileTypes = MidiSystem.getMidiFileTypes(seq_noMeta);
                if (midiFileTypes.length > 0) {
                    try {
                        MidiSystem.write(seq_noMeta, midiFileTypes[0], file);
                    } catch (IOException e) {
                        DialogMaker.showErrorDialog("MIDI Export Error", "Could not export the MIDI file.");
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();//This should never occur.
            }
        }

    }

    /**
     * Close all the Java MIDI objects, clear the sequenced data and the beat counters
     */
    public void clear() {
        if (sequencer != null) {
            sequencer.removeMetaEventListener(midiMetaListener);
            sequencer.close();
        }
        if (synth != null) {
            synth.close();
        }
        for (Track t : sequence.getTracks()) {
            sequence.deleteTrack(t);
        }
        this.instrumentChannelMap.clear();
        tickCountHashMap.clear();
        netTrackMap.clear();
        channelCount = 0;
    }

    /**
     * Attempt to instantaneously play the MIDI note with the given index
     *
     * @param noteIndex
     */
    public void playNote(int noteIndex) {
        try {
            if (synth == null) {
                synth = getSynthesizer();
            }
            if (!(synth.isOpen())) {
                synth.open();
            }
            onMessage.setMessage(ShortMessage.NOTE_ON, 4, noteIndex, 93);
            offMessage.setMessage(ShortMessage.NOTE_OFF, 4, noteIndex, 93);
            if (synthRcvr == null) {
                synthRcvr = synth.getReceiver();
            }
            synthRcvr.send(onMessage, -1);
            synthRcvr.send(offMessage, 2000);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            DialogMaker.showMidiUnavailableDialog();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            //This exception should never occur
        }

    }
}
