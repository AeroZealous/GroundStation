package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
    private Tab consoleTab;

    private final ConnectController connect;
    private SensorPlotter plotter;

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
        if (plotter != null && plotter.getSerialDevice() != null) {
            try {
                if (plotter.getSerialDevice().getSerialPort().isOpened()) {
                    plotter.getSerialDevice().getSerialPort().closePort();
                }
            } catch (SerialPortException ex) {
                Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        super.stop(); //To change body of generated methods, choose Tools | Templates.
    }

    public GroundStation() {
        connect = new ConnectController();
        connect.OPEN_PORTS.addListener(new MapChangeListener<String, SerialPort>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends SerialPort> change) {
                if (change.wasAdded()) {
                    SerialPort deviceAdded = change.getValueAdded();
                    connectDevice(new SerialDevice(deviceAdded));
                }
            }
        });
    }

    public void connectDevice(SerialDevice device) {
        if (plotter != null && plotter.getSerialDevice() != null) {
            try {
                if (plotter.getSerialDevice().getSerialPort().isOpened()) {
                    plotter.getSerialDevice().getSerialPort().closePort();
                }
            } catch (SerialPortException ex) {
                Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        plotter = new SensorPlotter(device);
        SerialConsole console = new SerialConsole(device);
        plotsTab.setContent(plotter);
        consoleTab.setContent(console);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectTab.setContent(connect);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
