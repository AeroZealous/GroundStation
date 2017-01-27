package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class PIDController extends AnchorPane implements Initializable {

    @FXML
    private StackedBarChart<String, Double> chart;
    @FXML
    private PieChart rollPID;
    @FXML
    private PieChart pitchPID;
    @FXML
    private PieChart yawPD;
    @FXML
    private CheckBox showLegend;
    @FXML
    private CheckBox showLabels;
    @FXML
    private ToggleButton showRoll;
    @FXML
    private ToggleButton showPitch;
    @FXML
    private ToggleButton showYaw;
    @FXML
    private HBox pieChartHBox;
    XYChart.Series<String, Double> pitchPSeries;
    XYChart.Series<String, Double> pitchISeries;
    XYChart.Series<String, Double> pitchDSeries;
    XYChart.Series<String, Double> rollPSeries;
    XYChart.Series<String, Double> rollISeries;
    XYChart.Series<String, Double> rollDSeries;
    XYChart.Series<String, Double> yawPSeries;
    XYChart.Series<String, Double> yawISeries;
    XYChart.Series<String, Double> yawDSeries;

    XYChart.Data<String, Double> flPitchP;
    XYChart.Data<String, Double> flPitchI;
    XYChart.Data<String, Double> flPitchD;
    XYChart.Data<String, Double> flRollP;
    XYChart.Data<String, Double> flRollI;
    XYChart.Data<String, Double> flRollD;

    XYChart.Data<String, Double> frPitchP;
    XYChart.Data<String, Double> frPitchI;
    XYChart.Data<String, Double> frPitchD;
    XYChart.Data<String, Double> frRollP;
    XYChart.Data<String, Double> frRollI;
    XYChart.Data<String, Double> frRollD;

    XYChart.Data<String, Double> rrPitchP;
    XYChart.Data<String, Double> rrPitchI;
    XYChart.Data<String, Double> rrPitchD;
    XYChart.Data<String, Double> rrRollP;
    XYChart.Data<String, Double> rrRollI;
    XYChart.Data<String, Double> rrRollD;

    XYChart.Data<String, Double> rlPitchP;
    XYChart.Data<String, Double> rlPitchI;
    XYChart.Data<String, Double> rlPitchD;
    XYChart.Data<String, Double> rlRollP;
    XYChart.Data<String, Double> rlRollI;
    XYChart.Data<String, Double> rlRollD;

    PieChart.Data rollP = new PieChart.Data("P", 10);
    PieChart.Data rollI = new PieChart.Data("I", 10);
    PieChart.Data rollD = new PieChart.Data("D", 10);
    PieChart.Data pitchP = new PieChart.Data("P", 10);
    PieChart.Data pitchI = new PieChart.Data("I", 10);
    PieChart.Data pitchD = new PieChart.Data("D", 10);
    PieChart.Data yawP = new PieChart.Data("P", 10);
    PieChart.Data yawD = new PieChart.Data("D", 10);
    
    public PIDController() {

        flPitchP = new XYChart.Data<>("Front Left", Math.random());
        flPitchI = new XYChart.Data<>("Front Left", Math.random());
        flPitchD = new XYChart.Data<>("Front Left", Math.random());
        flRollP = new XYChart.Data<>("Front Left", Math.random());
        flRollI = new XYChart.Data<>("Front Left", Math.random());
        flRollD = new XYChart.Data<>("Front Left", Math.random());

        frPitchP = new XYChart.Data<>("Front Right", Math.random());
        frPitchI = new XYChart.Data<>("Front Right", Math.random());
        frPitchD = new XYChart.Data<>("Front Right", Math.random());
        frRollP = new XYChart.Data<>("Front Right", Math.random());
        frRollI = new XYChart.Data<>("Front Right", Math.random());
        frRollD = new XYChart.Data<>("Front Right", Math.random());

        rrPitchP = new XYChart.Data<>("Rear Right", Math.random());
        rrPitchI = new XYChart.Data<>("Rear Right", Math.random());
        rrPitchD = new XYChart.Data<>("Rear Right", Math.random());
        rrRollP = new XYChart.Data<>("Rear Right", Math.random());
        rrRollI = new XYChart.Data<>("Rear Right", Math.random());
        rrRollD = new XYChart.Data<>("Rear Right", Math.random());

        rlPitchP = new XYChart.Data<>("Rear Left", Math.random());
        rlPitchI = new XYChart.Data<>("Rear Left", Math.random());
        rlPitchD = new XYChart.Data<>("Rear Left", Math.random());
        rlRollP = new XYChart.Data<>("Rear Left", Math.random());
        rlRollI = new XYChart.Data<>("Rear Left", Math.random());
        rlRollD = new XYChart.Data<>("Rear Left", Math.random());

        pitchPSeries = new XYChart.Series<>("Pitch P", FXCollections.observableArrayList(flPitchP, frPitchP, rrPitchP, rlPitchP));
        pitchISeries = new XYChart.Series<>("Pitch I", FXCollections.observableArrayList(flPitchI, frPitchI, rrPitchI, rlPitchI));
        pitchDSeries = new XYChart.Series<>("Pitch D", FXCollections.observableArrayList(flPitchD, frPitchD, rrPitchD, rlPitchD));

        rollPSeries = new XYChart.Series<>("Roll P", FXCollections.observableArrayList(flRollP, frRollP, rrRollP, rlRollP));
        rollISeries = new XYChart.Series<>("Roll I", FXCollections.observableArrayList(flRollI, frRollI, rrRollI, rlRollI));
        rollDSeries = new XYChart.Series<>("Roll D", FXCollections.observableArrayList(flRollD, frRollD, rrRollD, rlRollD));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("PID.fxml"));
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
        chart.setData(FXCollections.observableArrayList(pitchPSeries, pitchISeries, pitchDSeries, rollPSeries, rollISeries, rollDSeries));
        rollPID.setData(FXCollections.observableArrayList(rollP, rollI, rollD));
        pitchPID.setData(FXCollections.observableArrayList(pitchP, pitchI, pitchD));
        yawPD.setData(FXCollections.observableArrayList(yawP, yawD));
        pitchPID.labelsVisibleProperty().bind(showLabels.selectedProperty());
        rollPID.labelsVisibleProperty().bind(showLabels.selectedProperty());
        yawPD.labelsVisibleProperty().bind(showLabels.selectedProperty());
        pitchPID.legendVisibleProperty().bind(showLegend.selectedProperty());
        rollPID.legendVisibleProperty().bind(showLegend.selectedProperty());
        yawPD.legendVisibleProperty().bind(showLegend.selectedProperty());

        showPitch.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pieChartHBox.getChildren().add(pitchPID);
                } else {
                    pieChartHBox.getChildren().remove(pitchPID);
                }
            }
        });
        showYaw.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pieChartHBox.getChildren().add(yawPD);
                } else {
                    pieChartHBox.getChildren().remove(yawPD);
                }
            }
        });
        showRoll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pieChartHBox.getChildren().add(rollPID);
                } else {
                    pieChartHBox.getChildren().remove(rollPID);
                }
            }
        });
    }

    public void processSensorData(SerialDevice.SensorData sensorData) {
        pitchP.setPieValue(sensorData.pitchP);
        pitchI.setPieValue(sensorData.pitchI);
        pitchD.setPieValue(sensorData.pitchD);
        rollP.setPieValue(sensorData.rollP);
        rollI.setPieValue(sensorData.rollI);
        rollD.setPieValue(sensorData.rollD);
        yawP.setPieValue(sensorData.yawP);
        yawD.setPieValue(sensorData.yawD);

        flPitchP.setYValue(sensorData.pitchP);
        flPitchI.setYValue(sensorData.pitchI);
        flPitchD.setYValue(sensorData.pitchD);
        frPitchP.setYValue(sensorData.pitchP);
        frPitchI.setYValue(sensorData.pitchI);
        frPitchD.setYValue(sensorData.pitchD);
        
        rrPitchP.setYValue(-sensorData.pitchP);
        rrPitchI.setYValue(-sensorData.pitchI);
        rrPitchD.setYValue(-sensorData.pitchD);
        rlPitchP.setYValue(-sensorData.pitchP);
        rlPitchI.setYValue(-sensorData.pitchI);
        rlPitchD.setYValue(-sensorData.pitchD);
        
        flRollP.setYValue(sensorData.rollP);
        flRollI.setYValue(sensorData.rollI);
        flRollD.setYValue(sensorData.rollD);
        rlRollP.setYValue(sensorData.rollP);
        rlRollI.setYValue(sensorData.rollI);
        rlRollD.setYValue(sensorData.rollD);
        
        frRollP.setYValue(-sensorData.rollP);
        frRollI.setYValue(-sensorData.rollI);
        frRollD.setYValue(-sensorData.rollD);
        rrRollP.setYValue(-sensorData.rollP);
        rrRollI.setYValue(-sensorData.rollI);
        rrRollD.setYValue(-sensorData.rollD);
    }
}
