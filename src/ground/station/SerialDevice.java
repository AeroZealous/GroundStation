package ground.station;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
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

    private final SerialPort port;
    private final StringBuilder buffer;
    private final PipedWriter writer = new PipedWriter();
    private final PipedReader reader = new PipedReader();
    private final ReadOnlyStringPropertyBaseImpl output = new ReadOnlyStringPropertyBaseImpl();
    public final ConcurrentLinkedQueue<SensorData> q = new ConcurrentLinkedQueue<>();

    private Thread parserThread;
    Runnable parser = new Runnable() {
        double gX = 0;
        double gY = 0;
        double gZ = 0;
        double aX = 0;
        double aY = 0;
        double aZ = 0;
        double aFilX = 0;
        double aFilY = 0;
        double aFilZ = 0;
        double xAbs;
        double yAbs;
        double zAbs;

        @Override
        public void run() {
            Scanner scanner = new Scanner(reader);
            scanner.useDelimiter("\\s+");
            double frame = 0;
            while (scanner.hasNext()) {
                if (!scanner.hasNext(";")) {
                    String skipped = scanner.next();
                    System.out.println("skipped = '" + skipped + '\'');
                    continue;
                }
                scanner.next(); //consume ';' 
                if (scanner.hasNextDouble()) {
                    gY = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    gX = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    gZ = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aX = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aY = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aZ = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aFilX = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aFilY = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    aFilZ = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    xAbs = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    yAbs = scanner.nextDouble();
                }
                if (scanner.hasNextDouble()) {
                    zAbs = scanner.nextDouble();
                }

//                System.out.print(frame % 20 == 0 ? '|' : '.');
                final double fr = frame;
                SensorData sensorData = new SensorData(frame, gX, gY, gZ, aX, aY, aZ, aFilX, aFilY, aFilZ, xAbs, yAbs, zAbs);
                q.add(sensorData);
                frame++;
            }
        }
    };

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

    public ReadOnlyStringProperty outputProperty() {
        return output;
    }

    public String getOutput() {
        return output.get();
    }

    public void sendInput(String msg) {
        try {
            port.writeString(msg);
        } catch (SerialPortException ex) {
            Logger.getLogger(SerialDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventValue() > 0) {
            try {
                if (parserThread == null) {
                    parserThread = new Thread(parser, "Serial Data Parser");
                    parserThread.start();
                }
                final String msg = port.readString();
                writer.write(msg);
                writer.flush();
                buffer.append(msg);
                if (buffer.length() > 2000) {
                    buffer.delete(0, msg.length());
                }
                Platform.runLater(output::fireValueChangedEvent);

            } catch (SerialPortException | IOException ex) {
                Logger.getLogger(SerialDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public SerialPort getSerialPort() {
        return port;
    }

    private class ReadOnlyStringPropertyBaseImpl extends ReadOnlyStringPropertyBase {

        public ReadOnlyStringPropertyBaseImpl() {
        }

        @Override
        public String get() {
            return buffer.toString();
        }

        @Override
        public Object getBean() {
            return SerialDevice.this;
        }

        @Override
        public String getName() {
            return "output";
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public static final class SensorData {

        public final double frame;
        public final double xG, yG, zG, xA, yA, zA;
        public final double xFilA, yFilA, zFilA;
        public double xAbs, yAbs, zAbs;

        public SensorData(double frame, double xG, double yG, double zG, double xA, double yA, double zA) {
            this.frame = frame;
            this.xG = xG;
            this.yG = yG;
            this.zG = zG;
            this.xA = xA;
            this.yA = yA;
            this.zA = zA;
            this.xFilA = 0;
            this.yFilA = 0;
            this.zFilA = 0;
        }

        public SensorData(double frame, double xG, double yG, double zG, double xA, double yA, double zA, double xFilA, double yFilA, double zFilA) {
            this.frame = frame;
            this.xG = xG;
            this.yG = yG;
            this.zG = zG;
            this.xA = xA;
            this.yA = yA;
            this.zA = zA;
            this.xFilA = xFilA;
            this.yFilA = yFilA;
            this.zFilA = zFilA;
        }

        public SensorData(double frame, double xG, double yG, double zG, double xA, double yA, double zA, double xFilA, double yFilA, double zFilA, double xAbs, double yAbs, double zAbs) {
            this.frame = frame;
            this.xG = xG;
            this.yG = yG;
            this.zG = zG;
            this.xA = xA;
            this.yA = yA;
            this.zA = zA;
            this.xFilA = xFilA;
            this.yFilA = yFilA;
            this.zFilA = zFilA;
            this.xAbs = xAbs;
            this.yAbs = yAbs;
            this.zAbs = zAbs;
        }

    }
}
