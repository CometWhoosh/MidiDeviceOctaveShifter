package midideviceoctaveshifter;

import java.util.Arrays;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

/**
 * This class creates a GUI for interacting with the application. 
 * Through the GUI, the user can select which MIDI device and 
 * instrument to use, along with how many octaves to shift the 
 * input by.
 *
 * @author Yousef Bulbulia
 */
public class MidiDeviceOctaveShifter extends Application {
	
    class RetrievableTextFieldEventHandler implements EventHandler<KeyEvent> {

	TextField textField;
	String retrievedText = "";
			
	RetrievableTextFieldEventHandler(TextField textField) {
            this.textField = textField;
	}
	
	public void handle(KeyEvent event) {
		
            if(event.getCode().equals(KeyCode.ENTER)) {
		retrievedText = textField.getText();
            }
			
	}
			
	public String retrieve() {
            return retrievedText;
	}

    }
	
    @Override
    public void start(Stage primaryStage) {
		
        MenuButton selectDeviceButton = new MenuButton("Devices");
        
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        String[] deviceMenuItemLables = Arrays.stream(infos)
                                        .map(e -> e.getName())
                                        .toArray(String[]::new);
								  
		CheckMenuItem[] deviceItems = getExclusiveCheckMenuItems(infos.length, deviceMenuItemLables);
		selectDeviceButton.getItems().setAll(Arrays.asList(deviceItems));
		
		
        Synthesizer tempSynth = null;
	boolean synthIsAvailableTemp = true;
	Text noSynthAvailableText = new Text("The default synthesizer is not available due to either resource restrictions or " +  
											 "the fact that no default synthesizer is installed on the system");
        noSynthAvailableText.setVisible(false);
        try {
            tempSynth = MidiSystem.getSynthesizer(); 
        } catch(MidiUnavailableException e) {
            noSynthAvailableText.setVisible(true);
			e.printStackTrace();
        }
		
	final Synthesizer synth = tempSynth;
        final boolean synthIsAvailable = synthIsAvailableTemp;
		
        MenuButton selectInstrumentButton = new MenuButton("Instruments");
        
        Instrument[] instruments = synth.getAvailableInstruments();
	String[] instrumentMenuItemLables = Arrays.stream(instruments)
                                            .map(e -> e.getName())
                                            .toArray(String[]::new);
		
        CheckMenuItem[] instrumentItems = getExclusiveCheckMenuItems(instruments.length, instrumentMenuItemLables);
	selectInstrumentButton.getItems().setAll(Arrays.asList(instrumentItems));
        
		
        TextField numberOfOctavesField = new TextField("Number of octaves");
        
	final RetrievableTextFieldEventHandler retrievable = new RetrievableTextFieldEventHandler(numberOfOctavesField);
	numberOfOctavesField.setOnKeyPressed(retrievable);
		
		
	Text mustSelectDeviceAndInstrumentText = new Text("A MIDI device and instrument must be selected before connecting");
        mustSelectDeviceAndInstrumentText.setVisible(false);
	mustSelectDeviceAndInstrumentText.setWrappingWidth(190);
	
	Button connectButton = new Button("Connect");
        
	connectButton.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent actionEvent) {
                if(synthIsAvailable) {
                    
                    try {

                        boolean noDeviceSelected = true;
                        boolean noInstrumentSelected = true;
                        
                        
                        MidiDevice device = null;

                        for(int i = 0; i < deviceItems.length; i++) {

                            if(deviceItems[i].isSelected()) {
                                
                                noDeviceSelected = false;
                                device = MidiSystem.getMidiDevice(infos[i]);
                                
                            }

                        }
                        
                        
                        Transmitter transmitter = null;
                        if(device != null) {					
                            transmitter = device.getTransmitter();
                        }
                        
                        
                        for(int i = 0; i < instrumentItems.length; i++) {

                            if(instrumentItems[i].isSelected()) {

                                noInstrumentSelected = false;
                                synth.loadInstrument(instruments[i]);

                            }

                        }
                        
                        
                        int numberOfOctaves = 0;

                        if(!retrievable.retrieve().equals("")) {
                            numberOfOctaves = Integer.parseInt(retrievable.retrieve());
                        }
                        
                        
                        if(noDeviceSelected || noInstrumentSelected) {
                            mustSelectDeviceAndInstrumentText.setVisible(true);
                            
                        } else {
                        
                            synth.open();


                            OctaveShifter octaveShifter = new OctaveShifter(synth, numberOfOctaves);
                            transmitter.setReceiver(octaveShifter);
                            
                        }
                        
                        
                    } catch(MidiUnavailableException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
			
		
        Pane root = new Pane();
        root.getChildren().addAll(numberOfOctavesField, /*selectDeviceText,*/ selectDeviceButton, selectInstrumentButton, connectButton, mustSelectDeviceAndInstrumentText, noSynthAvailableText);
	
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("MidiDeviceOctaveShifter");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        selectDeviceButton.setLayoutX(0);
        selectDeviceButton.setLayoutY(0);
        
        selectInstrumentButton.setLayoutX(selectDeviceButton.getWidth());
        selectInstrumentButton.setLayoutY(0);
        
        numberOfOctavesField.setPrefWidth(250);
        numberOfOctavesField.setLayoutX(selectDeviceButton.getWidth() + selectInstrumentButton.getWidth());
        numberOfOctavesField.setLayoutY(0);
        
        connectButton.setLayoutX(0);
        connectButton.setLayoutY(selectDeviceButton.getHeight() + 10);
        
        noSynthAvailableText.setLayoutX(0);
        noSynthAvailableText.setLayoutY(connectButton.getLayoutY() + 10);
        
        mustSelectDeviceAndInstrumentText.setLayoutX(0);
        mustSelectDeviceAndInstrumentText.setLayoutY(noSynthAvailableText.getLayoutY() + 60);
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * This method returns an array of CheckMenuItems 
     * where only one CheckMenuItem can be selected at 
     * a time.
     * 
     * @param length the length of the CheckMenuItem array
     * @param texts  an array of names for the CheckMenuItems 
     *               in the CheckMenuItem array
     * @return       the CheckMenuItem array
     */
    public CheckMenuItem[] getExclusiveCheckMenuItems(int length, String[] texts) {

	CheckMenuItem[] items = new CheckMenuItem[length];
	
	for(int i = 0; i < items.length; i++) {
		
       	    CheckMenuItem item = new CheckMenuItem(texts[i]);
		
	    //When one CheckMenuItem is selected, the others are all deselected, so that only one CheckMenuItem is ever selected at a time
	    item.setOnAction(new EventHandler<ActionEvent>() {
				
		public void handle(ActionEvent event) {
					
		    for(CheckMenuItem e : items) {
						
			if(e.isSelected()) {
                            e.setSelected(false);
			}
						
                    }
					
                    item.setSelected(true);
					
		}
				
            });
                        
            items[i] = item;
			
        }
		
            return items;
	
	}
    
}
