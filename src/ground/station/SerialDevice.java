package ground.station;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.HashMap;
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
        double absX;
        double absY;
        double absZ;
        double timedelta;
        double motorFL, motorFR, motorRR, motorRL;
        double rcRoll, rcPitch, rcYaw, rcThrottle, rcMode;
        double pitchP, pitchI, pitchD;
        double rollP, rollI, rollD;
        double yawP, yawD;

        @Override
        public void run() {
            Scanner scanner = new Scanner(reader);
            scanner.useDelimiter("\\s+");
            double frame = 0;
            boolean inPacket = false;
            while (scanner.hasNext()) {
                String token = scanner.next();
                switch (token) {
                    case "{":   //start new packet
                        inPacket = true;
                        break;
                    case "}":   //packet ended; commit
                        if (!inPacket) {
                            break;
                        }
                        inPacket = false;
                        SensorData sensorData = new SensorData(frame, 0, timedelta, gX, gY, gZ, aX, aY, aZ, rcRoll, rcPitch, rcYaw, rcThrottle, rcMode, aFilX, aFilY, aFilZ, absX, absY, absZ, motorFL, motorFR, motorRR, motorRL, pitchP, pitchI, pitchD, rollP, rollI, rollD, yawP, yawD);
                        q.add(sensorData);
                        frame++;
                        break;
                    case "ms":
                        if (scanner.hasNextDouble()) {
                            timedelta = scanner.nextDouble();
                        }
                        break;
                    case "gy":
                        if (scanner.hasNextDouble()) {
                            gX = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            gY = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            gZ = scanner.nextDouble();
                        }
                        break;
                    case "ac":
                        if (scanner.hasNextDouble()) {
                            aX = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            aY = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            aZ = scanner.nextDouble();
                        }
                        break;
                    case "acfil":
                        if (scanner.hasNextDouble()) {
                            aFilX = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            aFilY = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            aFilZ = scanner.nextDouble();
                        }
                        break;
                    case "ang":
                        if (scanner.hasNextDouble()) {
                            absX = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            absY = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            absZ = scanner.nextDouble();
                        }
                        break;
                    case "rc6":
                        if (scanner.hasNextDouble()) {
                            rcRoll = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rcPitch = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rcYaw = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rcThrottle = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rcMode = scanner.nextDouble();
                        }
                        break;
                    case "motor":
                        if (scanner.hasNextDouble()) {
                            motorFL = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            motorFR = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            motorRR = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            motorRL = scanner.nextDouble();
                        }
                        break;
                    case "pid":
                        if (scanner.hasNextDouble()) {
                            pitchP = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            pitchI = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            pitchD = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rollP = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rollI = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            rollD = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            yawP = scanner.nextDouble();
                        }
                        if (scanner.hasNextDouble()) {
                            yawD = scanner.nextDouble();
                        }
                        break;
                }
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
                if (buffer.length() > 64 * 1024) {
                    buffer.delete(0, msg.length());
                }
                Platform.runLater(output::fireValueChangedEvent);

            } catch (SerialPortException | IOException ex) {
                Logger.getLogger(SerialDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("port  < 0");
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

        //internal
        public double frame;
        //time
        public double timestamp, delta;
        //raw gyro+accelerometer
        public double gX, gY, gZ, aX, aY, aZ;
        //raw receiver
        public double rcRoll, rcPitch, rcYaw, rcThrottle, rcMode;
        //computed filtered accelerometer + absolute angle
        public double filaX, filaY, filaZ;
        public double absX, absY, absZ;
        //motor commands
        public double motorFL, motorFR, motorRR, motorRL;
        //PID
        double pitchP, pitchI, pitchD;
        double rollP, rollI, rollD;
        double yawP, yawD;
        HashMap<String, Double> extra;
        
        public SensorData(double frame, double timestamp, double delta, double gX, double gY, double gZ, double aX, double aY, double aZ, double rcRoll, double rcPitch, double rcYaw, double rcThrottle, double rcMode, double filaX, double filaY, double filaZ, double absX, double absY, double absZ, double motorFL, double motorFR, double motorRR, double motorRL) {
            this.frame = frame;
            this.timestamp = timestamp;
            this.delta = delta;
            this.gX = gX;
            this.gY = gY;
            this.gZ = gZ;
            this.aX = aX;
            this.aY = aY;
            this.aZ = aZ;
            this.rcRoll = rcRoll;
            this.rcPitch = rcPitch;
            this.rcYaw = rcYaw;
            this.rcThrottle = rcThrottle;
            this.rcMode = rcMode;
            this.filaX = filaX;
            this.filaY = filaY;
            this.filaZ = filaZ;
            this.absX = absX;
            this.absY = absY;
            this.absZ = absZ;
            this.motorFL = motorFL;
            this.motorFR = motorFR;
            this.motorRR = motorRR;
            this.motorRL = motorRL;
        }

        public SensorData(double frame, double timestamp, double delta, double gX, double gY, double gZ, double aX, double aY, double aZ, double rcRoll, double rcPitch, double rcYaw, double rcThrottle, double rcMode, double filaX, double filaY, double filaZ, double absX, double absY, double absZ, double motorFL, double motorFR, double motorRR, double motorRL, double pitchP, double pitchI, double pitchD, double rollP, double rollI, double rollD, double yawP, double yawD) {
            this.frame = frame;
            this.timestamp = timestamp;
            this.delta = delta;
            this.gX = gX;
            this.gY = gY;
            this.gZ = gZ;
            this.aX = aX;
            this.aY = aY;
            this.aZ = aZ;
            this.rcRoll = rcRoll;
            this.rcPitch = rcPitch;
            this.rcYaw = rcYaw;
            this.rcThrottle = rcThrottle;
            this.rcMode = rcMode;
            this.filaX = filaX;
            this.filaY = filaY;
            this.filaZ = filaZ;
            this.absX = absX;
            this.absY = absY;
            this.absZ = absZ;
            this.motorFL = motorFL;
            this.motorFR = motorFR;
            this.motorRR = motorRR;
            this.motorRL = motorRL;
            this.pitchP = pitchP;
            this.pitchI = pitchI;
            this.pitchD = pitchD;
            this.rollP = rollP;
            this.rollI = rollI;
            this.rollD = rollD;
            this.yawP = yawP;
            this.yawD = yawD;
        }

        private SensorData() {
        }

    }
}
