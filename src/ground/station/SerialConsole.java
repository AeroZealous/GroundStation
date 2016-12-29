package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class SerialConsole extends BorderPane implements Initializable {

    @FXML
    private TextField message;
    @FXML
    private Button send;
    @FXML
    private TextArea console;

    private SerialDevice serialDevice;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serialDevice.outputProperty().addListener(ob->{
            double originalScroll = console.getScrollTop();
            console.setText(serialDevice.getOutput());
            console.setScrollTop(originalScroll);
        });
        send.disableProperty().bind(message.textProperty().isEmpty());
        send.setOnAction(ae -> serialDevice.sendInput(message.getText()));
        message.setOnAction(send.getOnAction());
    }

    public SerialConsole(SerialDevice device) {
        serialDevice = device;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SerialConsole.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
            getChildren().add(new Label("Could not  load FXML"));
        }

    }

}
