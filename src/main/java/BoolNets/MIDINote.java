package BoolNets;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.parboiled.support.ValueStack;

import java.util.Arrays;
import java.util.List;

/**
 * A musical note. MIDI note value be specified using scientific pitch notation, or by using the MIDI note index directly.
 */
public class MIDINote {

    private Integer MIDINoteIndex;
    private static final List<String> sharpNoteNames = Arrays.asList("C","C#","D","D#","E","F","F#","G","G#","A","A#","B");
    private static final List<String> flatNoteNames =  Arrays.asList("C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B");


    public MIDINote(int number){
        this.MIDINoteIndex = number;
    }

    /**
     * @param str The scientific pitch notation name for the desired MIDI note. E.g 'Ab3' is the same as 'G#3' which corresponds to MIDI note 56.
     */
    public MIDINote(String str) throws ValueException {
        this.MIDINoteIndex = getMIDINoteIndexFromString(str);
    }


    private Integer getMIDINoteIndexFromString(String str) throws ValueException{
        int baseIndex = 0;
        int octaveIndex = 0;
        if(str != null) {
            if (str.length() <= 3 && str.length() > 1) {
                if (str.contains("#")) {
                    baseIndex = sharpNoteNames.indexOf(str.substring(0, 2));
                    octaveIndex = Integer.parseInt(str.substring(2));
                } else if (str.contains("b")) {
                    baseIndex = flatNoteNames.indexOf(str.substring(0, 2));
                    octaveIndex = Integer.parseInt(str.substring(2));
                } else {
                    baseIndex = sharpNoteNames.indexOf(str.substring(0, 1));
                    octaveIndex = Integer.parseInt(str.substring(1));
                }
            } else {
                throw new ValueException("The note name " + str + " is not a valid MIDI note name.");
            }
            if (baseIndex == -1) {
                throw new ValueException("The note name " + str + " is not a valid MIDI note name.");
            }
            return ((octaveIndex + 2) * 12) + baseIndex;
        }
        return null;
    }

    public Integer getMIDINoteIndex() {
        return MIDINoteIndex;
    }
}
