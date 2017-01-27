package ground.station;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import ground.station.SerialDevice.SensorData;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.CheckListView;

/**
 *
 * @author Siddhesh Rane
 */
public class SensorPlotter extends VBox implements Initializable {

    //Sensor DATA
    XYChart.Series<Double, Double> xGyro;
    XYChart.Series<Double, Double> yGyro;
    XYChart.Series<Double, Double> zGyro;
    XYChart.Series<Double, Double> xAcc;
    XYChart.Series<Double, Double> yAcc;
    XYChart.Series<Double, Double> zAcc;
    XYChart.Series<Double, Double> xAccFil;
    XYChart.Series<Double, Double> yAccFil;
    XYChart.Series<Double, Double> zAccFil;
    ObservableList<XYChart.Series<Double, Double>> data;

    @FXML
    LineChart<Double, Double> chart;
    @FXML
    NumberAxis time;
    @FXML
    NumberAxis magnitude;
    @FXML
    CheckBox includeZeroOnChart;
    @FXML
    private OrientationController xAngle;
    @FXML
    private OrientationController yAngle;
    @FXML
    private OrientationController zAngle;
    @FXML
    private OrientationController xAngle1;
    @FXML
    private OrientationController yAngle1;
    @FXML
    private OrientationController zAngle1;
    @FXML
    private Gauge deltaTime;
    @FXML
    private CheckListView<XYChart.Series<Double, Double>> filterList;
    private ObservableList<XYChart.Series<Double, Double>> filteredSeries;

    //Computed Sensor data
    private double xGyInt, yGyInt, zGyInt;

    public SensorPlotter() {
        xGyro = new XYChart.Series<>();
        xGyro.setName("Gyro X");
        yGyro = new XYChart.Series<>();
        yGyro.setName("Gyro Y");
        zGyro = new XYChart.Series<>();
        zGyro.setName("Gyro Z");
        xAcc = new XYChart.Series<>();
        xAcc.setName("Accelerometer X");
        yAcc = new XYChart.Series<>();
        yAcc.setName("Accelerometer Y");
        zAcc = new XYChart.Series<>();
        zAcc.setName("Accelerometer Z");
        xAccFil = new XYChart.Series<>();
        xAccFil.setName("Accelerometer X Filtered");
        yAccFil = new XYChart.Series<>();
        yAccFil.setName("Accelerometer Y Filtered");
        zAccFil = new XYChart.Series<>();
        zAccFil.setName("Accelerometer Z Filtered");
        data = FXCollections.observableArrayList(xGyro, yGyro, zGyro, xAcc, yAcc, zAcc, xAccFil, yAccFil, zAccFil);
        FXMLLoader loader = new FXMLLoader(SensorPlotter.class.getResource("Sensor.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(SensorPlotter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void processSensorData(SensorData sensorData) {
        if (sensorData == null) {
            return;
        }
        double pitch = Math.atan2(sensorData.filaX, Math.sqrt(sensorData.filaY * sensorData.filaY + sensorData.filaZ * sensorData.filaZ));
        double roll = -Math.atan2(sensorData.filaY, Math.sqrt(sensorData.filaX * sensorData.filaX + sensorData.filaZ * sensorData.filaZ));
        xAngle1.setAngle(Math.toDegrees(roll));
        yAngle1.setAngle(Math.toDegrees(pitch));
        xAngle.setAngle(sensorData.absX);
        yAngle.setAngle(sensorData.absY);
        zAngle.setAngle(sensorData.absZ);
        deltaTime.setValue(sensorData.delta);
       
        xGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.gX));
        yGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.gY));
        zGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.gZ));

        xAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.aX));
        yAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.aY));
        zAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.aZ));

        xAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.filaX));
        yAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.filaY));
        zAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.filaZ));
        double diff = sensorData.frame - time.getUpperBound();

        if (diff > 0) {
            time.setLowerBound(diff + time.getLowerBound());
            time.setUpperBound(diff + time.getUpperBound());
            xGyro.getData().remove(0);
            yGyro.getData().remove(0);
            zGyro.getData().remove(0);
            xAcc.getData().remove(0);
            yAcc.getData().remove(0);
            zAcc.getData().remove(0);
            xAccFil.getData().remove(0);
            yAccFil.getData().remove(0);
            zAccFil.getData().remove(0);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterList.setItems(data);
        filterList.getCheckModel().checkAll();
//        filteredSeries = data.filtered(filterList.getCheckModel().getSelectedItems()::contains);
        filteredSeries = FXCollections.observableArrayList(data);
        filterList.getCheckModel().getCheckedItems().addListener((Observable ob) -> {
            filteredSeries.setAll(filterList.getCheckModel().getCheckedItems());
        });
        chart.setData(filteredSeries);
        magnitude.forceZeroInRangeProperty().bind(includeZeroOnChart.selectedProperty());
        xAngle.setAngle(0);
        yAngle.setAngle(0);
        zAngle.setAngle(0);
        deltaTime.setSections(
                new Section(0, 5, Color.LIGHTGREEN),
                new Section(5, 10, Color.GREENYELLOW),
                new Section(10, 15, Color.ORANGE),
                new Section(15, 20, Color.RED));
        deltaTime.setSectionsVisible(true);
    }

}
