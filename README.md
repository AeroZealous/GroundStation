# GroundStation
Ground control software for testing, simulating and data analysis.
Primarily meant for quad copters.

![Connect Screen](connect.png "The entry point for connecting to serial devices")
![Sensor Plotter](plot.png "Plots sensor values on graphs and controls")
![View motor outputs](motors.png)
![console](console.png "Receive and transmit raw serial data in the console")
## How to Run 

Simplest way is to open the project in Netbeans and run. 
For cli compiling ensure you include lib folder in `-classpath`.

##Dependencies

All dependencies are included in the lib folder  
You need Java 8 (for lambda support).
UI is done in JavaFX 8u40

- For serial connection to Arduino we use **Java Simple Serial Connector** _2.8_ library  
- For special controls **ControlsFX** _8.40_
- For gauge controls **Medusa** _7.3_

## Entry Point
The `GroundStation.java` is the main class and `start` is the main entry point. 
There is a main but it ultimately calls `start`