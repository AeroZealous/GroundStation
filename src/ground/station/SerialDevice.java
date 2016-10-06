package ground.station;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.chart.XYChart;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * SerialDevice represents a microcontroller or Arduino device on the embedded
 * system that is connected via a serial port and continuously supplying
 * telemetry data.
 *
 * @author Siddhesh Rane
 */
public class SerialDevice implements SerialPortEventListener {

    final SerialPort port;
    final StringBuilder buffer;
    XYChart.Series<Double, Double> accX, accY, accZ, gyX, gyY, gyZ;
    String jdbcURL;
    private PipedWriter writer = new PipedWriter();
    private PipedReader reader = new PipedReader();

    /**
     * Creates a SerialDevice class for a device connected to the given
     * SerialPort. {@code port} has to be non-null and opened.
     * NullPointerException is thrown if port is null.
     *
     * @param port the SerialPort to which the device is connected
     */
    public SerialDevice(SerialPort port) {
        if (port == null) {
            throw new NullPointerException("Serial port cannot be null");
        }
        this.port = port;
        buffer = new StringBuilder(1024);

        try {
            port.addEventListener(this);
            reader.connect(writer);
        } catch (SerialPortException | IOException ex) {
            Logger.getLogger(SerialDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventValue() > 0) {
            try {
                writer.write(port.readString());
            } catch (SerialPortException | IOException ex) {
                Logger.getLogger(SerialDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    Runnable parser = new Runnable() {

        @Override
        public void run() {
            Scanner scanner = new Scanner(reader);
            double  frame = 0;
            while (scanner.hasNext()) {
                scanner.next("<");
                double gX = scanner.nextDouble();
                double gY = scanner.nextDouble();
                gyX.getData().add(new XYChart.Data<>(frame, gX));
                gyY.getData().add(new XYChart.Data<>(frame, gY));
                scanner.next(">");
                frame++;
            }
        }
    };

   
}
