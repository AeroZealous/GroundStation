package ground.station;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class ConnectController extends VBox implements Initializable {

    @FXML
    private ComboBox<String> portList;
    @FXML
    private ChoiceBox<Integer> baudRates;
    @FXML
    private Button connect;
    @FXML
    private Button scan;
    @FXML
    private Label error;

    final public static Integer[] BAUD = {
        115200, 19200, 9600,};

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        baudRates.getItems().addAll(BAUD);
        baudRates.getSelectionModel().selectFirst();
        portList.setPlaceholder(new Text("No Ports Available"));
        error.setText("");
        scan();
    }

    @FXML
    String[] scan() {
        error.setText("");
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length == 0) {
            error.setText("No ports were found");
            connect.setDisable(true);
            return null;
        }
        portList.getItems().setAll(portNames);
        portList.getSelectionModel().selectFirst();
        connect.setDisable(false);
        return portNames;
    }

    @FXML
    void connect() {
        String portName = portList.getSelectionModel().getSelectedItem();
        SerialPort port = new SerialPort(portName);
        try {
            port.openPort();
            port.setParams(baudRates.getValue(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (SerialPortException ex) {
            Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
            error.setText(ex.getMessage());
        }
        error.setText("Reading Data");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg;
                do {
                    try {
                        Thread.sleep(100);
                        msg = port.readString();
                        
                    } catch (SerialPortException ex) {
                        Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
                        msg = ex.getMessage();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
                        msg = "interrupted";
                    }
                    final String msgFinal = msg;
                    Platform.runLater(() -> error.setText(msgFinal));
                } while (true);
            }
        }).start();

    }
}
