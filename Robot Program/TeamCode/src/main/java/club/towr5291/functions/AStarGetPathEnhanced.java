package club.towr5291.functions;

import android.util.Log;

import com.qualcomm.robotcore.util.ElapsedTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import club.towr5291.astarpathfinder.A0Star;
import club.towr5291.astarpathfinder.AStarClosed;
import club.towr5291.astarpathfinder.AStarValue;
import club.towr5291.astarpathfinder.sixValues;

/**
 * Created by ianhaden on 5/09/16.
 * //creates a path from (startX,startY) to (endX,endY)
 // param startFieldX = startX : starting x position
 // param startFieldY = startY : starting y position
 // param startFieldZ = startZ : starting Direction
 // param targetFieldX = endX : ending x position
 // param targetFieldY = endY : ending y position
 // param targetFieldZ = endZ : ending direction

 //NOTE : Inputs are in terms of room positions.
 //       All other positions will be in terms of grid

 */

public class AStarGetPathEnhanced {

    private static final String TAG = "AStarGetPathEnhanced";

    private AStarValue AStarValues = new AStarValue();
    private AStarValue AStarClosed = new AStarValue();
    private AStarValue AStarOpen = new AStarValue();
    private HashMap<String,AStarValue> AStarClosedMap = new HashMap<String,AStarValue>();
    private HashMap<String,AStarValue> AStarValueMap = new HashMap<String, AStarValue>();
    A0Star a0Star = new A0Star();
    private sixValues[] pathValues = new sixValues[1000];
    private int pathIndex;
    private int costPenalty;
    private int newDir;

    private int BlueRed = 0;  //0 is not defined, 1 is blue, 2 is red

    //set up the variables for the logger
    private String startDate;
    private ElapsedTime runtime = new ElapsedTime();
    private FileLogger fileLogger;
    private int debug = 0;

    public AStarGetPathEnhanced(){
        for ( int i = 0; i < pathValues.length; i++) {
            pathValues[i] = new sixValues();
        }
        pathIndex = 0;
    }

    public int getKey(int xPos, int yPos, int width, int length) {
        int value;
        value = yPos*length + xPos;
        return value;
    }

    public int getXPos(int key, int width, int length) {
        int value;
        value = (int)key % width;  //modulo
        return value;
    }

    public int getYPos(int key, int width, int length) {
        int value;
        value = (int)key / width;  //integer division
        return value;
    }

    public sixValues ProcessCurrentNode (int currentX, int currentY, int currentDir, int endX, int endY, int endDir) {
        boolean closed = false;
        boolean diagonal = false;
        boolean canWalk = false;
        int searching = 1;
        int found = 0;
        boolean empty = false;
        double distFromCurrentToIJ = 0, distFromStartToCurrent = 0;
        double tempF, tempG, tempH;
        int minF;
        double lowestF = -1;
        int lowestFKey = 0;

        HashMap<String,AStarValue> AStarCurrentProcess = new HashMap<String, AStarValue>();

        sixValues returnValue = new sixValues(0,0,0,0,0,0);

        AStarValue AStarValueCurrentXY = new AStarValue();
        AStarValue AStarValueCurrentIJ = new AStarValue();

        AStarValueCurrentIJ.xvalue = 0;
        AStarValueCurrentIJ.yvalue = 0;
        AStarValueCurrentIJ.zvalue = 0;
        AStarValueCurrentIJ.FValue = 0;
        AStarValueCurrentIJ.GValue = 0;
        AStarValueCurrentIJ.HValue = 0;
        AStarValueCurrentIJ.ID = 0;
        AStarValueCurrentIJ.Parent = 0;

        //add point to be process to the closed list
        AStarClosed.xvalue = currentX;
        AStarClosed.yvalue = currentY;
        AStarClosed.ID = getKey(AStarClosed.xvalue, AStarClosed.yvalue, a0Star.fieldWidth, a0Star.fieldLength);
        AStarClosedMap.put(String.valueOf(AStarClosed.ID), new AStarValue(AStarClosed.ID, 0, 0, 0, 0, AStarClosed.xvalue, AStarClosed.yvalue, 0));

        if (debug >= 3) {
            fileLogger.writeEvent(TAG, "Added to closed list point (" + currentX + "," + currentY + ") Key " + AStarClosed.ID);
            Log.d(TAG, "Added to closed list point(" + currentX + "," + currentY + ") Key " + AStarClosed.ID);
        }
        //analyze adjacent blocks/grid locations
        //key exists so get the values
        int nodeKey = getKey(currentX, currentY, a0Star.fieldWidth, a0Star.fieldLength);
        if (AStarValueMap.containsKey(String.valueOf(nodeKey))){
            AStarValueCurrentXY = AStarValueMap.get(String.valueOf(nodeKey));
            if (debug >= 3) {
                fileLogger.writeEvent(TAG, "Getting Key - point (" + currentX + "," + currentY + ") Key " + nodeKey);
                Log.d(TAG, "Getting Key - point (" + currentX + "," + currentY +") Key " + nodeKey);
            }
        } else {
            AStarValueCurrentXY.xvalue = currentX;
            AStarValueCurrentXY.yvalue = currentY;
            AStarValueCurrentXY.zvalue = currentDir;
            AStarValueCurrentXY.FValue = 0;
            AStarValueCurrentXY.GValue = 0;
            AStarValueCurrentXY.HValue = 0;
            AStarValueCurrentXY.ID = 0;
            if (debug >= 3) {
                fileLogger.writeEvent(TAG, "Key Doesn't Exist - point (" + currentX + "," + currentY + ") Key " + nodeKey);
                Log.d(TAG, "Key Doesn't Exist - point (" + currentX + "," + currentY + ") Key " + nodeKey);
            }
        }

        for (int i = Math.max(0, (currentX - 1)); i <= Math.min(a0Star.fieldWidth - 1, currentX + 1); i++ ) {
            for (int j = Math.max(0, (currentY - 1)); j <= Math.min(a0Star.fieldLength - 1, currentY + 1); j++ ) {
                if ((i == currentX) && (j == currentY)) {
                    if (debug >= 3) {
                        fileLogger.writeEvent(TAG, "i=x and j=y - nothing to do point (" + i + "," + j + ")");
                        Log.d(TAG, "i=x and j=y - nothing to do point (" + i + "," + j + ")");
                    }
                } else {
                    //check if its on the closed list
                    if (debug >= 3) {
                        fileLogger.writeEvent(TAG, "checking if on closed list - point (" + i + "," + j + ")");
                        Log.d(TAG, "checking if on closed list - point (" + i + "," + j + ")");
                    }
                    if (AStarClosedMap.containsKey(String.valueOf(getKey(i, j, a0Star.fieldWidth, a0Star.fieldLength))))
                        closed = true;      //need to check if this returns null it doesn't error out
                    else
                        closed = false;                                                                 //not on closed list, must be on open list

                    if (debug >= 3) {
                        fileLogger.writeEvent(TAG, "on closed list " + closed + " - point (" + i + "," + j + ") Checking if Diagonal ");
                        Log.d(TAG, "on closed list " + closed + " - point (" + i + "," + j + ")  Checking if Diagonal ");
                    }

                    if ((i + j) % 2 == (currentX + currentY) % 2)
                        diagonal = true;
                    else
                        diagonal = false;

                    //check direction change
                    switch (currentDir) {
                        case 0:
                            if ((i == currentX) && (j == (currentY - 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 45:
                            if ((i == (currentX + 1)) && (j == (currentY - 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 90:
                            if ((i == (currentX + 1)) && (j == (currentY))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 135:
                            if ((i == (currentX - 1)) && (j == (currentY + 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 180:
                            if ((i == (currentX)) && (j == (currentY + 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 225:
                            if ((i == (currentX - 1)) && (j == (currentY + 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 270:
                            if ((i == (currentX - 1)) && (j == (currentY))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                        case 315:
                            if ((i == (currentX - 1)) && (j == (currentY - 1))) {
                                costPenalty = 0;  //no turning
                            } else {
                                costPenalty = 300;  //cost for turning
                            }
                            break;
                    }
                    costPenalty = 0;

                    if ((i == currentX) && (j == (currentY - 1))) {  //0 degrees
                        newDir = 0;
                    } else if ((i == (currentX + 1)) && (j == (currentY - 1))) {  //45 degrees
                        newDir = 45;
                    } else if ((i == (currentX + 1)) && (j == (currentY))) {  // 90 degrees
                        newDir = 90;
                    } else if ((i == (currentX - 1)) && (j == (currentY + 1))) {  // 135 degrees
                        newDir = 135;
                    } else if ((i == (currentX)) && (j == (currentY + 1))) {  // 180 degrees
                        newDir = 180;
                    } else if ((i == (currentX - 1)) && (j == (currentY + 1))) {  // 225 degrees
                        newDir = 225;
                    } else if ((i == (currentX - 1)) && (j == (currentY))) {  //  270 degrees
                        newDir = 270;
                    } else if ((i == (currentX - 1)) && (j == (currentY - 1))) {  //  315 degrees
                        newDir = 315;
                    }

                    if (debug >= 3) {
                        fileLogger.writeEvent(TAG, "point (" + i + "," + j + ") costPenalty " + costPenalty + " newDir " + newDir + " Is Diagonal " + diagonal);
                        Log.d(TAG, "point (" + i + "," + j + ") costPenalty " + costPenalty + " newDir " + newDir + " Is Diagonal " + diagonal);
                    }

                    if (diagonal) {
                        if (BlueRed == 2) {
                            canWalk = a0Star.walkableRed[i][j] && a0Star.walkableRed[currentX][j] && a0Star.walkableRed[i][currentY];
                            if (debug >= 3) {
                                fileLogger.writeEvent(TAG, "Checking Diagonal RED Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                                Log.d(TAG, "Checking Diagonal RED Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                            }
                        } else {
                            canWalk = a0Star.walkableBlue[i][j] && a0Star.walkableBlue[currentX][j] && a0Star.walkableBlue[i][currentY];
                            if (debug >= 3) {
                                fileLogger.writeEvent(TAG, "Checking Diagonal BLUE Canwalk " + canWalk + " point (" + i + "," + j + ")");
                                Log.d(TAG, "Checking Diagonal BLUE Canwalk " + canWalk + " point (" + i + "," + j + ")");
                            }
                        }
                        //canWalk = a0Star.walkable[i][j] && a0Star.walkable[currentX][j] && a0Star.walkable[i][currentY];
                        distFromCurrentToIJ = 1410 + costPenalty;                                                      //G Value
                    } else {
                        if (BlueRed == 2) {
                            canWalk = a0Star.walkableRed[i][j];
                            if (debug >= 3) {
                                fileLogger.writeEvent(TAG, "Checking RED Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                                Log.d(TAG, "Checking RED Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                            }
                        } else {
                            canWalk = a0Star.walkableBlue[i][j];
                            if (debug >= 3) {
                                fileLogger.writeEvent(TAG, "Checking BLUE Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                                Log.d(TAG, "Checking BLUE Canwalk " + canWalk + " - point (" + i + "," + j + ")");
                            }
                        }
                        //canWalk = a0Star.walkable[i][j];
                        distFromCurrentToIJ = 1000 + costPenalty;                                                      //G Value
                    }


                    if (debug >= 3) {
                        fileLogger.writeEvent(TAG, "Canwalk: " + canWalk + " closed: " + closed);
                        Log.d(TAG, "Canwalk: " + canWalk + " closed: " + closed);
                    }
                    if (!closed && canWalk) {
                        //calculated G,H,and F
                        tempG = AStarValueCurrentXY.GValue + distFromCurrentToIJ;                     // distance from starttocurrent + value just calculated
                        tempH = ((Math.abs(i - endX) * 1000 + Math.abs(j - endY) * 1000));                            //insert heuristic of choice (we use manhattan)
                        //NOTE : you could also use point_distance(i,j,endX,endY);
                        tempF = tempG + tempH;
                        //update if necessary
                        if (AStarValueMap.containsKey(String.valueOf(getKey(i, j, a0Star.fieldWidth, a0Star.fieldLength))))   //see if key is in G Map, means already processed
                        {
                            //var oldG=;
                            //show_debug_message(string(tempG)+" compare to "+string(oldG));
                            AStarValueCurrentIJ = AStarValueMap.get(String.valueOf(getKey(i, j, a0Star.fieldWidth, a0Star.fieldLength)));
                            AStarValues.xvalue = i;
                            AStarValues.yvalue = j;
                            AStarValues.zvalue = newDir;
                            AStarValues.FValue = tempF;
                            AStarValues.GValue = tempG;
                            AStarValues.HValue = tempH;
                            AStarValues.Parent = getKey(currentX, currentY, a0Star.fieldWidth, a0Star.fieldLength);
                            AStarValues.ID = getKey(AStarValues.xvalue, AStarValues.yvalue, a0Star.fieldWidth, a0Star.fieldLength);
                            if (tempG < AStarValueCurrentIJ.GValue)
                            {
                                AStarValueMap.remove(String.valueOf(AStarValues.ID));
                                if (debug >= 3) {
                                    fileLogger.writeEvent(TAG, "Removed OLD point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                                    Log.d(TAG, "Removed OLD point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                                }
                                AStarValueMap.put(String.valueOf(AStarValues.ID), new AStarValue(AStarValues.ID, AStarValues.FValue, AStarValues.GValue, AStarValues.HValue, AStarValues.Parent, AStarValues.xvalue, AStarValues.yvalue, AStarValues.zvalue));

                                if (debug >= 3)
                                {
                                    fileLogger.writeEvent(TAG, "Updating point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                                    Log.d(TAG, "Updating point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                                }
                            }
                            AStarCurrentProcess.put(String.valueOf(AStarValues.ID), new AStarValue(AStarValues.ID, AStarValues.FValue, AStarValues.GValue, AStarValues.HValue, AStarValues.Parent, AStarValues.xvalue, AStarValues.yvalue, AStarValues.zvalue));
                        }
                        else
                        {
                            AStarValues.xvalue = i;
                            AStarValues.yvalue = j;
                            AStarValues.zvalue = newDir;
                            AStarValues.FValue = tempF;
                            AStarValues.GValue = tempG;
                            AStarValues.HValue = tempH;
                            AStarValues.Parent = getKey(currentX, currentY, a0Star.fieldWidth, a0Star.fieldLength);
                            AStarValues.ID = getKey(AStarValues.xvalue, AStarValues.yvalue, a0Star.fieldWidth, a0Star.fieldLength);
                            AStarValueMap.put(String.valueOf(AStarValues.ID), new AStarValue(AStarValues.ID, AStarValues.FValue, AStarValues.GValue, AStarValues.HValue, AStarValues.Parent, AStarValues.xvalue, AStarValues.yvalue, AStarValues.zvalue));
                            AStarCurrentProcess.put(String.valueOf(AStarValues.ID), new AStarValue(AStarValues.ID, AStarValues.FValue, AStarValues.GValue, AStarValues.HValue, AStarValues.Parent, AStarValues.xvalue, AStarValues.yvalue, AStarValues.zvalue));
                            if (debug >= 3) {
                                fileLogger.writeEvent(TAG, "Adding point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                                Log.d(TAG, "Adding point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                            }
                        }
                        if (AStarValueMap.containsKey(String.valueOf(getKey(i, j, a0Star.fieldWidth, a0Star.fieldLength)))) {
                            //if (debug >= 3) {
                            //    fileLogger.writeEvent(TAG, "Key Exists point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                            //    Log.d(TAG, "Key Exists point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                            //}
                        }
                        else
                        {
                            //if (debug >= 3) {
                            //    fileLogger.writeEvent(TAG, "Key Doesn't Exist and should - point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                            //    Log.d(TAG, "Key Doesn't Exist and should - point (" + i + "," + j + ") Key " + AStarValues.ID + "    G:" + tempG + "     H:" + tempH + "     F:" + tempF);
                            //}
                        }
                    }
                }
            }
        }
        lowestF = 99999999;
        //find best option
        //
        // for (String key: AStarValueMap.keySet()) {
        for (String key: AStarCurrentProcess.keySet()){
            AStarValueCurrentIJ = AStarValueMap.get(key);
            if (debug >= 3) {
                fileLogger.writeEvent(TAG, "Valid key " + key + " ID " + AStarValueCurrentIJ.ID + " FValue " + AStarValueCurrentIJ.FValue + " LowestF " + lowestF);
                Log.d(TAG, "Valid key " + key + " ID " + AStarValueCurrentIJ.ID + " FValue " + AStarValueCurrentIJ.FValue + " LowestF " + lowestF);
            }
            if (AStarValueCurrentIJ.FValue < lowestF) {
                lowestF = AStarValueCurrentIJ.FValue;
                lowestFKey = AStarValueCurrentIJ.ID;
                if (debug >= 3) {
                    fileLogger.writeEvent(TAG, "Found LowerF " + lowestF);
                    Log.d(TAG, "Found LowerF " + lowestF);
                }
            }
        }
        if (lowestF != 99999999) {
            if (pathIndex == 999) {
                if (debug >= 3) {
                    fileLogger.writeEvent(TAG, "Can't find path, exiting ");
                    Log.d(TAG, "Can't find path, exiting ");
                }
                // break out of loop, can't find path....
                searching = 0;
                found = 1;
            } else {

                Log.d(TAG, "Found Lowest F, now getting next point ");
                //found low values in map, remove it from map
                pathValues[pathIndex].val1 = (double) pathIndex;
                pathValues[pathIndex].val2 = currentX;
                pathValues[pathIndex].val3 = currentY;  //path_add_point (path, getXPos(curNode, a0Star.fieldWidth, a0Star.fieldLength), getYPos(curNode, a0Star.fieldWidth, a0Star.fieldLength);
                pathValues[pathIndex].val4 = currentDir;

                pathIndex++;
                if (debug >= 3) {
                    fileLogger.writeEvent(TAG, "Lowest FKey " + lowestFKey);
                    Log.d(TAG, "Lowest FKey " + lowestFKey);
                }
                AStarValueCurrentIJ = AStarValueMap.get(String.valueOf(lowestFKey));
                currentX = AStarValueCurrentIJ.xvalue;
                currentY = AStarValueCurrentIJ.yvalue;
                currentDir = AStarValueCurrentIJ.zvalue;

                //delete the fValue - make it real big so its not lowest anymore
                AStarValueMap.put(String.valueOf(lowestFKey), new AStarValue(AStarValueCurrentIJ.ID, 99999999, AStarValueCurrentIJ.GValue, AStarValueCurrentIJ.HValue, AStarValueCurrentIJ.Parent, AStarValueCurrentIJ.xvalue, AStarValueCurrentIJ.yvalue, AStarValueCurrentIJ.zvalue));

                if (debug >= 3) {
                    fileLogger.writeEvent(TAG, "Made F Realy Big, Trying Key " + getKey(currentX, currentY, a0Star.fieldWidth, a0Star.fieldLength) + " - point (" + currentX + "," + currentY + ")");
                    Log.d(TAG, "Made F Realy Big, Trying Key " + getKey(currentX, currentY, a0Star.fieldWidth, a0Star.fieldLength) + " - point (" + currentX + "," + currentY + ")");
                }
                searching = 1;
                found = 0;
            }
        } else {
            searching = 0;
            found = 0;
            if (debug >= 3) {
                fileLogger.writeEvent(TAG, "No More Nodes Left");
                Log.d(TAG, "No More Nodes Left");
            }
        }
        //check whether we're at the end
        if ((currentX == endX) && (currentY == endY)) {
            pathValues[pathIndex].val1 = (double)pathIndex;
            pathValues[pathIndex].val2 = currentX;
            pathValues[pathIndex].val3 = currentY;  //path_add_point (path, getXPos(curNode, a0Star.fieldWidth, a0Star.fieldLength), getYPos(curNode, a0Star.fieldWidth, a0Star.fieldLength);
            pathValues[pathIndex].val4 = currentDir;

            searching = 0;
            found = 1;
            if (debug >= 1) {
                fileLogger.writeEvent(TAG, "You found me I'm the final block");
                Log.d(TAG, "You found me I'm the final block");
            }
        }
        returnValue.val1 = searching;
        returnValue.val2 = found;
        returnValue.val3 = currentX;
        returnValue.val4 = currentY;
        returnValue.val5 = currentDir;
        if (debug >= 3) {
            fileLogger.writeEvent(TAG, "Returning 1- " + searching + " 2- " + found + " 3- " + currentX + " 4- " + currentY + " 5- " + currentDir + "point (" + currentX + "," + currentY + ")");
            Log.d(TAG, "Returning 1- " + searching + " 2- " + found + " 3- " + currentX + " 4- " + currentY + " 5- " + currentDir + "point (" + currentX + "," + currentY + ")");
        }
        return returnValue;
    }

    public sixValues[] findPathAStar (int startX, int startY, int startZ, int endX, int endY, int endDir) {
        boolean searching = true;

        if (debug >= 1) {
            startDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            fileLogger = new FileLogger(runtime);
            fileLogger.open();
            fileLogger.write("Time,SysMS,Thread,Event,Desc");
            fileLogger.writeEvent(TAG, "Log Started");
        }
        AStarValues.xvalue = startX;
        AStarValues.yvalue = startY;
        AStarValues.zvalue = startZ;
        AStarValues.GValue = 0;
        AStarValues.HValue = 0;
        AStarValues.FValue = 0;
        AStarValues.Parent = 0;
        AStarValues.ID = getKey(AStarValues.xvalue, AStarValues.yvalue, a0Star.fieldWidth, a0Star.fieldLength);
        AStarValueMap.put(String.valueOf(AStarValues.ID), new AStarValue(AStarValues.ID, AStarValues.FValue, AStarValues.GValue, AStarValues.HValue, AStarValues.Parent, AStarValues.xvalue, AStarValues.yvalue, AStarValues.zvalue));

        if (startX < startY)
        {
            BlueRed = 2;  //RED
        } else {
            BlueRed = 1;  //BLUE
        }

        for (String key: AStarValueMap.keySet()) {
            if (debug >= 3) {
                fileLogger.writeEvent(TAG, "Keys " + key + ": x:" + AStarValueMap.get(key).xvalue + " y:" + AStarValueMap.get(key).yvalue);
                Log.d(TAG, "Keys " + key + ": x:" + AStarValueMap.get(key).xvalue + " y:" + AStarValueMap.get(key).yvalue);
            }
        }
        //process map
        sixValues currentResult = new sixValues(1,0,AStarValues.xvalue,AStarValues.yvalue, AStarValues.zvalue, 0);

        while (searching) {
            if (debug >= 1) {
                fileLogger.writeEvent(TAG, "Searching ");
                Log.d(TAG, "Searching...");
            }
            currentResult = (ProcessCurrentNode((int)currentResult.val3, (int)currentResult.val4, (int)currentResult.val5 , endX, endY, endDir));
            searching = (currentResult.val1 == 1);
        }

        boolean found = (currentResult.val2 == 1);

//        if (found) {
//
//            for ( int i = 0; i < pathValues.length; i++) {
//                //fileLogger.writeEvent("init()","Path " + pathValues[i].val1 + " " + pathValues[i].val2 + " " + pathValues[i].val3 );
//                Log.d(TAG, "Path Step:= " + pathValues[i].val1 + " x:- " + pathValues[i].val2 + " y:= " + pathValues[i].val3  + " Dir:= " + pathValues[i].val4);
//                if ((pathValues[i].val2 == endX) && (pathValues[i].val3 == endY)) {
//                    break;
//                }
//            }
//        }
        if (debug >= 1) {
            if (fileLogger != null) {
                fileLogger.writeEvent(TAG, "Stopped");
                fileLogger.close();
                fileLogger = null;
            }
        }
        return pathValues;
    }
}
