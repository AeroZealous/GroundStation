# GroundStation
Ground control software for testing, simulating and data analysis


## How to Run 

Simplest way is to open the project in Netbeans and run. 
For cli compiling ensure you include lib folder in `-classpath`.

##Dependacies

For serial connection to Arduino we use Java Simple Serial Connector library  

- jar located in `lib/jssc-2.8.0.jar`  
- javadoc in `lib/jssc-2.8.0-javadoc.jar`

You need Java 7+ though Java 8 is recommended.
UI is done in JavaFX

## Entry Point
The `GroundStation.java` is the main class and `start` is the main entry point. 
There is a main but it ultimately calls `start`

