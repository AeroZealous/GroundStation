package ground.station;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class SerialMonitorController extends AnchorPane implements Initializable, SerialPortEventListener {

    @FXML
    private Tab connect;
    @FXML
    private Tab monitor;
    @FXML
    private ChoiceBox<SerialPort> portChooser;
    @FXML
    private TextField message;
    @FXML
    private Button send;
    @FXML
    private TextArea console;
    @FXML
    private BorderPane borderpane;
    private LineChart<Double, Double> chart;

    private final ConnectController connectController;
    XYChart.Series<Double, Double> gyXseries;

    PipedReader reader;
    PipedWriter writer;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        connect.setContent(connectController);
        monitor.disableProperty().bind(Bindings.isEmpty(connectController.OPEN_PORTS));
        connectController.OPEN_PORTS.addListener((Observable observable) -> {
            portChooser.getItems().setAll(connectController.OPEN_PORTS.values());
            if (portChooser.valueProperty().get() == null) {
                portChooser.getSelectionModel().selectFirst();
            }
        });
        portChooser.setConverter(new StringConverter<SerialPort>() {
            @Override
            public String toString(SerialPort port) {
                return port.getPortName();
            }

            @Override
            public SerialPort fromString(String string) {
                return new SerialPort(string);
            }
        });

        send.setOnAction(ae -> {
            try {
                portChooser.getValue().writeString(message.getText());
            } catch (SerialPortException ex) {
                Logger.getLogger(SerialMonitorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        portChooser.valueProperty().addListener(new ChangeListener<SerialPort>() {
            @Override
            public void changed(ObservableValue<? extends SerialPort> observable, SerialPort oldValue, SerialPort newValue) {
                try {
                    if (oldValue != null) {
                        oldValue.removeEventListener();
                    }
                    newValue.addEventListener(SerialMonitorController.this);
                    console.clear();
                } catch (SerialPortException ex) {
                    Logger.getLogger(SerialMonitorController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        borderpane.setCenter(chart);
    }

    public SerialMonitorController() {
        NumberAxis yAxis = new NumberAxis();
        NumberAxis axis = new NumberAxis();
        chart = new LineChart(axis, yAxis);
        gyXseries = new XYChart.Series<>("gyro X", FXCollections.observableArrayList());
        ObservableList<XYChart.Series<Double, Double>> series = chart.getData();
        series.add(gyXseries);
        
        writer = new PipedWriter();
        reader = new PipedReader();
        connectController = new ConnectController();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SerialMonitor.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            reader.connect(writer);
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(SerialMonitorController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Runnable parser = new Runnable() {
            @Override
            public void run() {
                Scanner sc = new Scanner(reader);
                sc.useDelimiter("[ \t\n\r,;]");
                double frame = 0;
                while (sc.hasNext()) {
                    if (!sc.hasNextDouble()) {
                        sc.next();
                        continue;
                    }
                    final double gyx = sc.nextDouble();
                    final double fr = frame;
                    frame++;
                    System.out.println("gyx = " + gyx);
                    Platform.runLater(() -> gyXseries.getData().add(new XYChart.Data<>(fr, gyx)));
                }
            }
        };
        Executors.newSingleThreadExecutor().execute(parser);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventValue() > 0) {
            try {
                final String msg = portChooser.getValue().readString();
                console.appendText(msg);
                writer.write(msg);
            } catch (SerialPortException ex) {
                Logger.getLogger(SerialMonitorController.class.getName()).log(Level.SEVERE, null, ex);
                console.setText(ex.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(SerialMonitorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
