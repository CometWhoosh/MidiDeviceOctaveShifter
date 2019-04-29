package midideviceoctaveshifter;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

/**
 * This class shifts the incoming MIDI messages a specified number 
 * of octaves up or down. It has fields for the synthesizer and 
 * receiver.
 *
 * @author Yousef Bulbulia
 */
public class OctaveShifter implements Receiver {
    
    Synthesizer synth;
    Receiver receiver;
    int numberOfOctaves;
    
    public OctaveShifter(Synthesizer synth, int numberOfOctaves) {
        
        this.synth = synth;
        this.numberOfOctaves = numberOfOctaves;
        
        try {
        
            receiver = synth.getReceiver();
            synth.open();
            
        } catch(MidiUnavailableException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * {@inheritDoc}
     *
     * The MIDI message is shifter up or down by the number of
     * octaves specified in the constructor.
     * 
     */
    public void send(MidiMessage message, long timestamp) {
            
            ShortMessage shortMessage = (ShortMessage)message;
            ShortMessage shiftedShortMessage = null;
            
            try {
                
                shiftedShortMessage = new ShortMessage(shortMessage.getCommand(), 0, 
                    shortMessage.getData1() + 12 * numberOfOctaves, shortMessage.getData2());
               
            } catch(InvalidMidiDataException e){
                e.printStackTrace();
            }
            
            receiver.send(shiftedShortMessage, -1);
        
    }
    
    public void close() {
        
    }
    
}
