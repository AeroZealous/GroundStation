package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class ConnectController extends GridPane implements Initializable {

    @FXML
    private ComboBox<String> portList;
    @FXML
    private ComboBox<Integer> baudRates;
    @FXML
    private ChoiceBox<Integer> dataBits;
    @FXML
    private ChoiceBox<Parity> parity;
    @FXML
    private ChoiceBox<Integer> stopBits;
    @FXML
    private Button connect;
    @FXML
    private Button scan;
    @FXML
    private TextArea info;

    /**
     * Store the list of opened ports for automatic closing during exit and to
     * change connect button text to disconnect
     */
    private static final ObservableMap<String, SerialPort> openPorts = FXCollections.observableHashMap();
    public static final ObservableMap<String, SerialPort> OPEN_PORTS = FXCollections.unmodifiableObservableMap(openPorts);

    public static final Integer[] BAUD = {
        115200, 19200, 9600
    };
    enum Parity {
        NONE(SerialPort.PARITY_NONE),
        ODD(SerialPort.PARITY_ODD),
        EVEN(SerialPort.PARITY_EVEN);

        int num;

        private Parity(int num) {
            this.num = num;
        }

        public int getParity() {
            return num;
        }
    }

     FXMLLoader loader;
    public ConnectController() {
        loader = new FXMLLoader(getClass().getResource("connect.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        baudRates.getItems().addAll(BAUD);
        baudRates.getSelectionModel().selectFirst();

        dataBits.getItems().addAll(8, 7, 6, 5);
        dataBits.getSelectionModel().selectFirst();

        parity.getItems().setAll(Parity.values());
        parity.getSelectionModel().selectFirst();

        stopBits.getItems().setAll(1, 2, 3);
        stopBits.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer stopbit) {
                if (stopbit == 3) {
                    return "1.5";
                }
                return Integer.toString(stopbit);
            }

            @Override
            public Integer fromString(String string) {
                double stopbits = Double.parseDouble(string);
                if (stopbits == 1.5) {
                    stopbits = 3;
                }
                return (int) stopbits;
            }
        });
        stopBits.getSelectionModel().selectFirst();

        portList.setPlaceholder(new Text("No Ports Available"));
        portList.setEditable(true);

        ObjectBinding<SerialPort> portOpened = Bindings.valueAt(OPEN_PORTS, portList.valueProperty());
        connect.textProperty().bind(Bindings.when(portOpened.isNotNull()).then("Disconnect").otherwise("Connect"));
        connect.disableProperty().bind(portList.valueProperty().isNull());
        scan();
    }

    @FXML
    String[] scan() {
        info.setText("");
        portList.getItems().setAll(OPEN_PORTS.keySet());
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length == 0) {
            info.setText("No ports were found");
//            connect.setDisable(true);
            return null;
        }

        portList.getItems().addAll(portNames);
        System.out.println("OPEN_PORTS.keySet() = " + OPEN_PORTS.keySet());
        portList.getSelectionModel().selectFirst();
//        connect.setDisable(false);
        return portNames;
    }

    @FXML
    void connect() {
        String portName = portList.getSelectionModel().getSelectedItem();
        //Check if the port is already opened
        SerialPort port = openPorts.get(portName);
        if (port != null) {
            disconnect(port);
            portList.getItems().remove(portName);
            info.setText("Disconnected from " + portName);
            return;
        }
        port = port == null ? new SerialPort(portName) : port;

        try {
            port.openPort();
            port.setParams(baudRates.getValue(), dataBits.getValue(), SerialPort.STOPBITS_1, parity.getValue().getParity());
            openPorts.put(portName, port);
            portList.getItems().add(portName);
        } catch (SerialPortException ex) {
            Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
            info.setText(ex.getMessage());
            return;
        }
        info.setText("Connected to " + port.getPortName());
    }

    void disconnect(String portName) {
        SerialPort get = openPorts.get(portName);
        if (get != null) {
            disconnect(get);
        }
    }

    void disconnect(SerialPort port) {
        try {
            port.closePort();
            openPorts.remove(port.getPortName());
        } catch (SerialPortException ex) {
            Logger.getLogger(ConnectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
