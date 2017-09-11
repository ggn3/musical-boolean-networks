package Parser;

import javafx.scene.control.Alert;

public class DialogMaker {


    static Alert errorDialog = new Alert(Alert.AlertType.ERROR);
    static Alert infoDialog = new Alert(Alert.AlertType.INFORMATION);

    public static void showErrorDialog(String header, String message){
        errorDialog.setTitle("Error");
        errorDialog.setHeaderText(header);
        errorDialog.setContentText(message);
        errorDialog.showAndWait();
    }

    public static void showInfoDialog(String header, String message){
        infoDialog.setTitle("Information");
        infoDialog.setHeaderText(header);
        infoDialog.setContentText(message);
        infoDialog.showAndWait();
    }


    public static void showMidiUnavailableDialog(){
        showErrorDialog("MIDI Unavailable", "Could not access the system's MIDI resources. Playback will not function.");
    }


}
