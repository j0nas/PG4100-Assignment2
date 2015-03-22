package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class Controller {
    @FXML
    private Text actiontarget;

    @FXML
    public void handleSubmitButtonAction(ActionEvent e) {
        actiontarget.setText("'Sign in'-button pressed.");
    }
}
