package ground.station;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class OrientationController extends StackPane implements Initializable {

    @FXML
    private StackPane square;
    @FXML
    private StackPane graphic;
    @FXML
    private Text angleText;

    private final DoubleProperty angle;

    public double getAngle() {
        return angle.get();
    }

    public void setAngle(double value) {
        angle.set(value);
    }

    public DoubleProperty angleProperty() {
        return angle;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        graphic.rotateProperty().bind(angle);
        angleText.textProperty().bind(angle.asString("%.2f°"));
    }

    public OrientationController() {
        this(0);
    }

    public OrientationController(double angle) {
        this.angle = new SimpleDoubleProperty(angle);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("orientation.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(OrientationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double width = square.getWidth();
        double height = square.getHeight();
        double size = Math.min(width, height);
        square.resizeRelocate((width-size)/2,(height-size)/2,size, size);
    }
}
