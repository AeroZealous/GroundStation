package ground.station;

import ground.station.SerialDevice.SensorData;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Siddhesh Rane
 */
public class GroundStation extends Application implements Initializable {

    @FXML
    private Tab connectTab;
    @FXML
    private Tab plotsTab;
    @FXML
    private Tab motorTab;
    @FXML
    private Tab consoleTab;
    @FXML
    private Tab pidTab;

    private final ConnectController connect;
    private SensorPlotter plotter;
    private PIDController pidController;
    private MotorController motorController;
    private SerialConsole console;
    private SerialDevice serialDevice;

    private final AnimationTimer serialDataFetcher = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (serialDevice == null || !serialDevice.getSerialPort().isOpened()) {
                return;
            }
            ConcurrentLinkedQueue<SensorData> q = serialDevice.q;
            for (int i = 0; i < 15; i++) {
                if (q.isEmpty()) {
                    break;
                }
                final SensorData data = q.poll();
                plotter.processSensorData(data);
                pidController.processSensorData(data);
                motorController.processSensorData(data);
            }
        }
    };

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(GroundStation.class.getResource("GroundStation.fxml"));
        loader.setController(this);
        Parent root = new Label("Could not load fxml file");
        try {
            root = loader.load();
        } catch (IOException ex) {
            Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scene scene = new Scene(root);

        primaryStage.setTitle("Ground Station");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (serialDevice != null && serialDevice.getSerialPort().isOpened()) {
            try {
                serialDevice.getSerialPort().closePort();
            } catch (SerialPortException ex) {
                Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    public GroundStation() {
        connect = new ConnectController();
        connect.OPEN_PORTS.addListener((MapChangeListener.Change<? extends String, ? extends SerialPort> change) -> {
            if (change.wasAdded()) {
                SerialPort deviceAdded = change.getValueAdded();
                connectDevice(new SerialDevice(deviceAdded));
            }
        });
    }

    public void connectDevice(SerialDevice device) {
        if (serialDevice != null && serialDevice.getSerialPort().isOpened()) {
            try {
                serialDevice.getSerialPort().closePort();
                serialDataFetcher.stop();
            } catch (SerialPortException ex) {
                Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        serialDevice = device;
        plotter = new SensorPlotter();
        pidController = new PIDController();
        motorController = new MotorController();
        console = new SerialConsole(device);
        plotsTab.setContent(plotter);
        pidTab.setContent(pidController);
        motorTab.setContent(motorController);
        consoleTab.setContent(console);
        serialDataFetcher.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectTab.setContent(connect);
        consoleTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean selected) {
                if (selected && consoleTab.getContent() != null) {
                    consoleTab.getContent().requestFocus();
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
