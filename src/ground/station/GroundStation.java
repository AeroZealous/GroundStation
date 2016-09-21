package ground.station;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Siddhesh Rane
 */
public class GroundStation extends Application {

    @Override
    public void start(Stage primaryStage) {

        //Uncomment any one demo
        
        noiseSimulator(primaryStage);
//        arduinoSerialConnector(primaryStage);
    }
    
    void arduinoSerialConnector(Stage primaryStage){
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 300, 250);
        FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource("connect.fxml"));
        VBox connect = new VBox();
        try {
            connect = fXMLLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(GroundStation.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Couldnt load fxml");
        }
        root.setCenter(connect);
        primaryStage.setTitle("Connect to Arduino");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    void noiseSimulator(Stage primaryStage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 300, 250);
        NoiseSimulator noiseSimulator = new NoiseSimulator();
        root.setCenter(noiseSimulator);

        primaryStage.setTitle("Noise Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
        noiseSimulator.timeline.play();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
