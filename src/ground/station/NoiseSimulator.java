package ground.station;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 *
 * @author Siddhesh Rane
 */
public class NoiseSimulator extends VBox {

    final Slider noiseSlider;
    final DoubleProperty noiseMax = new SimpleDoubleProperty(1);
    final XYChart.Series<Double, Double> series;
    final LineChart chart;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    public final Timeline timeline;

    /*
    public NoiseSimulator() {
    this(new DoubleSupplier() {
    double angle = -RAD_INC;
    
    @Override
    public double getAsDouble() {
    angle += RAD_INC;
    if (angle > Math.PI * 2) {
    angle = 0;
    }
    return Math.sin(angle);
    }
    }, 1);
    }*/
    public NoiseSimulator() {
        noiseSlider = new Slider(0, 1, 0.02);
        noiseSlider.setMajorTickUnit(0.1);
        noiseSlider.setMinorTickCount(2);
        noiseSlider.setBlockIncrement(0.1);
        noiseSlider.setShowTickLabels(true);
        xAxis = new NumberAxis(0, 360, 60);
        xAxis.setLabel("Degrees");
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);

        series = new XYChart.Series<>(FXCollections.observableArrayList());
        chart = new LineChart(xAxis, yAxis, FXCollections.singletonObservableList(series));
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        timeline = new Timeline(new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
            double x;

            @Override
            public void handle(ActionEvent event) {
                double sin = Math.sin(Math.toRadians(x));
                double noise = noiseSlider.getValue();
                sin += -noise + Math.random() * noise * 2;
                series.getData().add(new XYChart.Data<>(x++, sin));
                System.out.println("Point added "+ x);
                if (x > 360) {
                    series.getData().remove(0);
                    xAxis.setLowerBound(x - 360);
                    xAxis.setUpperBound(x);
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        getChildren().addAll(chart, noiseSlider);
        VBox.setVgrow(chart, Priority.ALWAYS);
    }

}
