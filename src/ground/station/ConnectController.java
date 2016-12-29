package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
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
    private ChoiceBox<StopBits> stopBits;
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

        private final int num;

        private Parity(int num) {
            this.num = num;
        }

        public int getParity() {
            return num;
        }
    }

    enum StopBits {
        ONE(SerialPort.STOPBITS_1, "1"),
        TWO(SerialPort.STOPBITS_2, "2"),
        ONE_HALF(SerialPort.STOPBITS_1_5, "1.5");

        private final int bits;
        private final String text;

        private StopBits(int bits, String text) {
            this.bits = bits;
            this.text = text;
        }

        public int getBits() {
            return bits;
        }

        @Override
        public String toString() {
            return text;
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
            getChildren().add(new Label("Could not  load FXML"));
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

        stopBits.getItems().setAll(StopBits.values());
        stopBits.getSelectionModel().selectFirst();

        portList.setPlaceholder(new Text("No Ports Available"));
        portList.setEditable(true);

        ObjectBinding<SerialPort> portOpened = Bindings.valueAt(OPEN_PORTS, portList.valueProperty());
        connect.textProperty().bind(Bindings.when(portOpened.isNotNull()).then("Disconnect").otherwise("Connect"));
        connect.disableProperty().bind(portList.valueProperty().isNull());
        scan();
    }

    @FXML
    public String[] scan() {
        portList.getItems().setAll(OPEN_PORTS.keySet());
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length == 0) {
            info.setText("No ports were found");
            return null;
        } else {
            info.setText("");
        }

        portList.getItems().addAll(portNames);
        portList.getSelectionModel().selectFirst();
        return portNames;
    }

    @FXML
    private void connect() {
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
            port.setParams(baudRates.getValue(), dataBits.getValue(), stopBits.getValue().getBits(), parity.getValue().getParity());
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

    public ObservableList<SerialPort> getOpenPorts() {
        return null;
    }
}
