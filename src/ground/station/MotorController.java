package ground.station;

import eu.hansolo.medusa.Gauge;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class MotorController extends HBox implements Initializable {

    @FXML
    private Gauge flMotor;
    @FXML
    private Gauge rlMotor;
    @FXML
    private Gauge throttle;
    @FXML
    private Gauge frMotor;
    @FXML
    private Gauge rrMotor;

    public MotorController() {
        FXMLLoader loader = new FXMLLoader(SensorPlotter.class.getResource("Motor.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(SensorPlotter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ComboBox<Gauge.SkinType> skinType = new ComboBox<>(FXCollections.observableArrayList(Gauge.SkinType.values()));
skinType.setOnAction(ae->{throttle.setSkinType(skinType.getValue());});
    }

    public void processSensorData(SerialDevice.SensorData sensorData) {
        flMotor.setValue(sensorData.motorFL - 1000);
        frMotor.setValue(sensorData.motorFR - 1000);
        rrMotor.setValue(sensorData.motorRR - 1000);
        rlMotor.setValue(sensorData.motorRL - 1000);
        throttle.setValue(sensorData.rcThrottle);
        throttle.setSubTitle(sensorData.rcMode < 1500 ? "OFF" : "ON");
    }
}
