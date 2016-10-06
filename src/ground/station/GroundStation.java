package ground.station;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author Siddhesh Rane
 */
public class GroundStation extends Application {

    @Override
    public void start(Stage primaryStage) {

        //Uncomment any one demo
//        noiseSimulatorTest(primaryStage);
//        arduinoSerialConnectorTest(primaryStage);
//        serialMonitorTest(primaryStage);
        serialConnectionTest(primaryStage);
    }

    void arduinoSerialConnectorTest(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("connect.fxml"));
        GridPane gridRoot = new GridPane();
        fXMLLoader.setRoot(gridRoot);
        fXMLLoader.setController(new ConnectController());
        Parent parent = new StackPane(new Label("Could not load view from FXML"));
        try {
            parent = fXMLLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Couldnt load fxml");
        }
        root.setCenter(parent);
        primaryStage.setTitle("Connect to Arduino");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void noiseSimulatorTest(Stage primaryStage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 300, 250);
        NoiseSimulator noiseSimulator = new NoiseSimulator();
        root.setCenter(noiseSimulator);

        primaryStage.setTitle("Noise Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
        noiseSimulator.timeline.play();
    }

    SerialPort monitorPort;

    void serialMonitorTest(Stage primaryStage) {

        ConnectController connector = new ConnectController();

        TextArea content = new TextArea();
        TextField portBox = new TextField();
        portBox.setPromptText("/dev/ttyACM0 or /dev/pts/1");
        Button connect = new Button("Connect");

        VBox root = new VBox(new HBox(portBox, connect), content);
        VBox.setVgrow(content, Priority.ALWAYS);
        HBox.setHgrow(portBox, Priority.ALWAYS);

        final StringBuilder buffer = new StringBuilder();
        final SerialPortEventListener serialPortEventListener = new SerialPortEventListener() {
            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                if (serialPortEvent.getEventValue() > 0) {
                    try {
                        buffer.append(monitorPort.readString());
                        content.setText(buffer.toString());
                    } catch (SerialPortException ex) {
                        Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
                        content.setText(ex.getMessage());
                    }
                }
            }
        };

        connect.setOnAction(ae -> {
            try {
                content.setText("");
                if (monitorPort != null) {
                    monitorPort.closePort();
                }
                connect.setText("Connect");
                SerialPort newPort = new SerialPort(portBox.getText());
                newPort.openPort();
                monitorPort = newPort;
                monitorPort.addEventListener(serialPortEventListener);
                connect.setText("Disconnect");
            } catch (SerialPortException ex) {
                Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
                content.setText(ex.getMessage());
            }
        });

        Scene scene = new Scene(root);
        primaryStage.setOnCloseRequest(v -> {
            if (monitorPort != null && monitorPort.isOpened()) {
                try {
                    monitorPort.closePort();
                } catch (SerialPortException ex) {
                    Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        primaryStage.setTitle("Serial monitor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void serialConnectionTest(Stage primaryStage) {
        SerialMonitorController smc = new SerialMonitorController();

        Scene scene = new Scene(smc);

        primaryStage.setTitle("Serial monitor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
