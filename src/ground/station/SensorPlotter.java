package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ListExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.CheckListView;

/**
 *
 * @author Siddhesh Rane
 */
public class SensorPlotter extends AnchorPane implements Initializable {

    private SerialDevice serialDevice;
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
    private OrientationController xAngle;
    @FXML
    private OrientationController yAngle;
    @FXML
    private OrientationController zAngle;
    @FXML
    private CheckListView<XYChart.Series<Double, Double>> filterList;
    private ObservableList<XYChart.Series<Double, Double>> filteredSeries;

    //Computed Sensor data
    private double xGyInt, yGyInt, zGyInt;

    public SensorPlotter(SerialDevice device) {
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
        FXMLLoader loader = new FXMLLoader(SensorPlotter.class.getResource("SensorPlotter.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(SensorPlotter.class.getName()).log(Level.SEVERE, null, ex);
        }
        setSerialDevice(device);
    }

    public void setSerialDevice(SerialDevice device) {
        if (device == null) {
            throw new NullPointerException("SerialDevice cannot be null");
        }
        serialDevice = device;
        timer.start();
    }

    public SerialDevice getSerialDevice() {
        return serialDevice;
    }

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (serialDevice.getSerialPort().isOpened()) {
                double diff = 0;
                ConcurrentLinkedQueue<SerialDevice.SensorData> q = serialDevice.q;
                for (int i = 0; i < 30; i++) {
                    if (q.isEmpty()) {
                        break;
                    }
                    SerialDevice.SensorData sensorData = q.poll();
                    if (sensorData != null) {
                        xGyInt += sensorData.xG * 0.02;
                        yGyInt += sensorData.yG * 0.02;
                        zGyInt += sensorData.zG * 0.02;
                        xAngle.setAngle(sensorData.xAbs);
                        yAngle.setAngle(sensorData.yAbs);
                        zAngle.setAngle(sensorData.zAbs);
                        xGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.xG));
                        yGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.yG));
                        zGyro.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.zG));

                        xAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.xA));
                        yAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.yA));
                        zAcc.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.zA));

                        xAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.xFilA));
                        yAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.yFilA));
                        zAccFil.getData().add(new XYChart.Data<>(sensorData.frame, sensorData.zFilA));
                        diff = sensorData.frame - time.getUpperBound();
                    }
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
            }
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterList.setItems(data);
        filterList.getCheckModel().checkAll();
        filterList.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends XYChart.Series<Double, Double>> c) -> {
            System.out.println(filterList.getSelectionModel().getSelectedItems());
        });
//        filteredSeries = data.filtered(filterList.getCheckModel().getSelectedItems()::contains);
        filteredSeries = FXCollections.observableArrayList(data);
        filterList.getCheckModel().getCheckedItems().addListener((Observable ob) -> {
            filteredSeries.setAll(filterList.getCheckModel().getCheckedItems());
        });
        chart.setData(filteredSeries);
        xAngle.setAngle(0);
        yAngle.setAngle(0);
        zAngle.setAngle(0);
    }

}
