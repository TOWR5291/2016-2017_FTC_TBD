package club.towr5291.opmodes;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.text.SimpleDateFormat;
import java.util.Date;

import club.towr5291.astarpathfinder.A0Star;
import club.towr5291.astarpathfinder.sixValues;
import club.towr5291.functions.AStarGetPathBasic;
import club.towr5291.functions.AStarGetPathEnhanced;
import club.towr5291.functions.FileLogger;


/**
 * Created by ianhaden on 2/09/16.
 */

@Autonomous(name="Concept: A Star Path Finder 2", group="5291Concept")
//@Disabled
public class ConceptAStarPathFinder2 extends OpMode {


    private static final String TAG = "ConceptAStarPathFinder2";

    //set up the variables for the logger
    private String startDate;
    private ElapsedTime runtime = new ElapsedTime();
    private FileLogger fileLogger;

    public sixValues[] pathValues = new sixValues[1000];

    A0Star a0Star = new A0Star();
    String fieldOutput = "";

    //public AStarGetPathBasic pathValues2 = new AStarGetPathBasic();
    public AStarGetPathEnhanced pathValues2 = new AStarGetPathEnhanced();

    public int pathIndex = 0;

    /*
    * Code to run ONCE when the driver hits INIT
    */
    @Override
    public void init() {

        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        startDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        runtime.reset();
        telemetry.addData("FileLogger: ", runtime.toString());
        fileLogger = new FileLogger(runtime);
        fileLogger.open();
        telemetry.addData("FileLogger Op Out File: ", fileLogger.getFilename());
        fileLogger.write("Time,SysMS,Thread,Event,Desc");
        fileLogger.writeEvent("init()","Log Started");

        // Send telemetry message to signify robot waiting;
        telemetry.update();

        fileLogger.writeEvent("init()","Field");
        //outputing field
        fileLogger.writeEvent("init()", "Writing Field Array");


        //load start point
        int startX = 121;  //122
        int startY = 122;  //122
        int startZ = 90;
        int endX = 48;     //12
        int endY = 2;     //80
        int endDir = 270;

        //pathValues = pathValues2.findPathAStar(startX, startY, endX, endY);  //for basic
        pathValues = pathValues2.findPathAStar(startX, startY, startZ, endX, endY, endDir);  //for enhanced


        //plot out path..
//        for (int i = 0; i < pathValues.length; i++) {
//            fileLogger.writeEvent("init()","Path " + pathValues[i].val1 + " " + pathValues[i].val2 + " " + pathValues[i].val3 + " Dir:= " + pathValues[i].val4 );
//            if ((pathValues[i].val2 == endX) && (pathValues[i].val3 == endY)) {
//                break;
//            }
//        }

        String[][] mapComplete = new String[A0Star.FIELDWIDTH][A0Star.FIELDWIDTH];
        fieldOutput = "";

        for (int loopRow = 0; loopRow < a0Star.fieldWidth; loopRow++) {
            for (int loopColumn = 0; loopColumn < a0Star.fieldLength; loopColumn++) {
                if (a0Star.walkableRed[loopRow][loopColumn]) {
                    if ((loopRow == startX) && (loopColumn == startY)) {
                        mapComplete[loopRow][loopColumn] = "S";
                        fieldOutput = fieldOutput + "S";
                    }  else {
                        mapComplete[loopRow][loopColumn] = "1";
                        fieldOutput = fieldOutput + "1";
                    }
                } else {
                    if ((loopRow == startX) && (loopColumn == startY)) {
                        mapComplete[loopRow][loopColumn] = "1";
                        fieldOutput = fieldOutput + "S";
                    } else {
                        mapComplete[loopRow][loopColumn] = "0";
                        fieldOutput = fieldOutput + "0";
                    }
                }
            }
            //fileLogger.writeEvent("loop()", fieldOutput);
            fieldOutput = "";
        }

        fieldOutput = "MAP INIT COMPLETE";

        fileLogger.writeEvent("loop()", "Plotting results");

//        for (int i = 0; i < pathValues.length; i++) {
//            fileLogger.writeEvent("init()","Path " + pathValues[i].val1 + " " + pathValues[i].val2 + " " + pathValues[i].val3 + " Dir:= " + pathValues[i].val4 );
//            if ((pathValues[i].val2 == endX) && (pathValues[i].val3 == endY)) {
//                break;
//            }
//        }

        //plot out path..
        for (int i = 0; i < pathValues.length; i++) {
            //fileLogger.writeEvent("init()","Path " + pathValues[i].val1 + " " + pathValues[i].val2 + " " + pathValues[i].val3 + " Dir:= " + pathValues[i].val4 );
            mapComplete[(int)pathValues[i].val2][(int)pathValues[i].val3] = "P";
            if ((pathValues[i].val2 == startX) && (pathValues[i].val3 == startY)) {
                mapComplete[(int)pathValues[i].val2][(int)pathValues[i].val3] = "S";
            }
            if ((pathValues[i].val2 == endX) && (pathValues[i].val3 == endY)) {
                mapComplete[(int)pathValues[i].val2][(int)pathValues[i].val3] = "E";
                break;
            }
        }
        fieldOutput ="";
        for (int loopRow = 0; loopRow < a0Star.fieldWidth; loopRow++) {
            for (int loopColumn = 0; loopColumn < a0Star.fieldLength; loopColumn++) {
                fieldOutput = "" + fieldOutput + mapComplete[loopRow][loopColumn];
            }
            fileLogger.writeEvent("loop()", fieldOutput);
            fieldOutput = "";
        }


        fileLogger.writeEvent("init()","Init Complete");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {

    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        fileLogger.writeEvent("start()","START PRESSED: ");
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {

        telemetry.update();
    }

    /*
    * Code to run ONCE after the driver hits STOP
    */
    @Override
    public void stop()
    {
        telemetry.addData("FileLogger Op Stop: ", runtime.toString());
        if (fileLogger != null) {
            fileLogger.writeEvent("stop()","Stopped");
            fileLogger.close();
            fileLogger = null;
        }
    }




    //--------------------------------------------------------------------------
    // User Defined Utility functions here....
    //--------------------------------------------------------------------------



}
