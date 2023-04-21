/*
 * Copyright (C) 2019 Clément Gérardin @ Marseille.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed serialReader the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have receivedLine a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gcodeeditor;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A GRBL 1.1 Command Controler.
 * @author Clément
 */
public class GRBLControler implements Runnable, SerialPortEventListener {
      
    public static final String GRBL_INIT_STRING_HEADER = "Grbl 1.1";
    
    public static final String[] GRBL_ERROR_MSG = {
        "1", 	"G-code words consist of a letter and a value. Letter was not found.",
        "2", 	"Numeric value format is not valid or missing an expected value.",
        "3", 	"Grbl '$' system command was not recognized or supported.",
        "4", 	"Negative value received for an expected positive value.",
        "5",  	"Homing cycle is not enabled via settings.",
        "6", 	"Minimum step pulse time must be greater than 3usec",
        "7", 	"EEPROM read failed. Reset and restored to default values.",
        "8", 	"Grbl '$' command cannot be used unless Grbl is IDLE. Ensures smooth operation during a job.",
        "9", 	"G-code locked out during alarm or jog state",
        "10", 	"Soft limits cannot be enabled without homing also enabled.",
        "11", 	"Max characters per line exceeded. Line was not processed and executed.",
        "12", 	"(Compile Option) Grbl '$' setting value exceeds the maximum step rate supported.",
        "13", 	"Safety door detected as opened and door state initiated.",
        "14", 	"(Grbl-Mega Only) Build info or startup line exceeded EEPROM line length limit.",
        "15", 	"Jog target exceeds machine travel. Command ignored.",
        "16", 	"Jog command with no '=' or contains prohibited g-code.",
        "17", 	"Laser mode requires PWM output.",
        "20", 	"Unsupported or invalid g-code command found in block.",
        "21", 	"More than one g-code command from same modal group found in block.",
        "22" ,	"Feed rate has not yet been set or is undefined.",
        "23", 	"G-code command in block requires an integer value.",
        "24", 	"Two G-code commands that both require the use of the XYZ axis words were detected in the block.",
        "25", 	"A G-code word was repeated in the block.",
        "26" ,	"A G-code command implicitly or explicitly requires XYZ axis words in the block, but none were detected.",
        "27", 	"N line number value is not within the valid range of 1 - 9,999,999.",
        "28", 	"A G-code command was sent, but is missing some required P or L value words in the line.",
        "29", 	"Grbl supports six work coordinate systems G54-G59. G59.1, G59.2, and G59.3 are not supported.",
        "30" ,	"The G53 G-code command requires either a G0 seek or G1 feed motion mode to be active. A different motion was active.",
        "31", 	"There are unused axis words in the block and G80 motion mode cancel is active.",
        "32", 	"A G2 or G3 arc was commanded but there are no XYZ axis words in the selected plane to trace the arc.",
        "33", 	"The motion command has an invalid target. G2, G3, and G38.2 generates this error, if the arc is impossible to generate or if the probe target is the current position.",
        "34", 	"A G2 or G3 arc, traced with the radius definition, had a mathematical error when computing the arc geometry. Try either breaking up the arc into semi-circles or quadrants, or redefine them with the arc offset definition.",
        "35",	"A G2 or G3 arc, traced with the offset definition, is missing the IJK offset word in the selected plane to trace the arc.",
        "36", 	"There are unused, leftover G-code words that aren't used by any command in the block.",
        "37", 	"The G43.1 dynamic tool length offset command cannot apply an offset to an axis other than its configured axis. The Grbl default axis is the Z-axis.",
    };
    
    public static final String[] GRBL_ALARM_MSG = {
        "1", 	"Hard limit triggered. Machine position is likely lost due to sudden and immediate halt. Re-homing is highly recommended.",
        "2", 	"G-code motion target exceeds machine travel. Machine position safely retained. Alarm may be unlocked.",
        "3", 	"Reset while in motion. Grbl cannot guarantee position. Lost steps are likely. Re-homing is highly recommended.",
        "4", 	"Probe fail. The probe is not in the expected initial state before starting probe cycle, where G38.2 and G38.3 is not triggered and G38.4 and G38.5 is triggered.",
        "5", 	"Probe fail. Probe did not contact the workpiece within the programmed travel for G38.2 and G38.4.",
        "6", 	"Homing fail. Reset during active homing cycle.",
        "7", 	"Homing fail. Safety door was opened during active homing cycle.",
        "8", 	"Homing fail. Cycle failed to clear limit switch when pulling off. Try increasing pull-off setting or check wiring.",
        "9", 	"Homing fail. Could not find limit switch within search distance. Defined as 1.5 * max_travel on search and 5 * pulloff on locate phases."
    };
    
    /**
     * $10 = 0 to report WPos, 1 to report MPos | 2 to report Buf:
     */
    public static final String[] GRBL_SETTING_STR = {
        "0", 	"Step pulse time, microseconds",
        "1", 	"Step idle delay, milliseconds",
        "2", 	"Step pulse invert, mask",
        "3", 	"Step direction invert, mask",
        "4", 	"Invert step enable pin, boolean",
        "5", 	"Invert limit pins, boolean",
        "6", 	"Invert probe pin, boolean",
        "10", 	"Status report options, mask",
        "11", 	"Junction deviation, millimeters",
        "12", 	"Arc tolerance, millimeters",
        "13", 	"Report in inches, boolean",
        "20", 	"Soft limits enable, boolean",
        "21", 	"Hard limits enable, boolean",
        "22", 	"Homing cycle enable, boolean",
        "23", 	"Homing direction invert, mask",
        "24", 	"Homing locate feed rate, mm/min",
        "25", 	"Homing search seek rate, mm/min",
        "26", 	"Homing switch debounce delay, milliseconds",
        "27", 	"Homing switch pull-off distance, millimeters",
        "30", 	"Maximum spindle speed, RPM",
        "31", 	"Minimum spindle speed, RPM",
        "32", 	"Laser-mode enable, boolean",
        "100", 	"X-axis steps per millimeter",
        "101", 	"Y-axis steps per millimeter",
        "102", 	"Z-axis steps per millimeter",
        "110", 	"X-axis maximum rate, mm/min",
        "111", 	"Y-axis maximum rate, mm/min",
        "112", 	"Z-axis maximum rate, mm/min",
        "120", 	"X-axis acceleration, mm/sec^2",
        "121", 	"Y-axis acceleration, mm/sec^2",
        "122", 	"Z-axis acceleration, mm/sec^2",
        "130", 	"X-axis maximum travel, millimeters",
        "131", 	"Y-axis maximum travel, millimeters",
        "132", 	"Z-axis maximum travel, millimeters"
    };
    public static final int LAST_GRBL_SETTING_NUMBER = 132;
    
    public static final int GRBL_STATE_DISCONNECTED = 0;
    public static final int GRBL_STATE_IDLE   = 1;
    public static final int GRBL_STATE_RUN    = 2;
    public static final int GRBL_STATE_HOLD   = 3;
    public static final int GRBL_STATE_JOG    = 4;
    public static final int GRBL_STATE_ALARM  = 5;
    public static final int GRBL_STATE_DOOR   = 6;
    public static final int GRBL_STATE_CHECK  = 7;
    public static final int GRBL_STATE_HOME   = 8;
    public static final int GRBL_STATE_SLEEP  = 9;
    public static final int GRBL_STATE_HOLDING = 10;
    public static final int GRBL_STATE_DOOR_CLOSED = 11;
    public static final int GRBL_STATE_DOOR_WAITING_CLOSED = 12;
    public static final int GRBL_STATE_DOOR_OPENNED = 13;
    public static final int GRBL_STATE_DOOR_CLOSING = 14;
    public static final int GRBL_STATE_INIT = 15;
    public static final String[] GRBL_STATE_STR = { 
        "<not connected>", "IDLE", "RUN", "HOLD", "JOG", "ALARM", "DOOR", "CHECK",
        "HOME", "SLEEP", "HOLDING", "DOOR_CLOSED", "DOOR_WAITING_CLOSED", "DOOR_OPENNED",
        "DOOR_CLOSING", "<init>" };


    public static int getIndexOfSetting(int grblSettingNumber) {
        for ( int i = 0; i < GRBL_SETTING_STR.length-1; i+=2)
            if ( Integer.parseInt(GRBL_SETTING_STR[i]) == grblSettingNumber) return i >> 1;
        return -1;
    }
    
    /** Realtime GRBL states. */
    int grblState = GRBL_STATE_DISCONNECTED;
    
    int grblBufferFree = 128, grblBufferSize=128, grblSpindle, grblFeed;
    
    /** Contains all the ligne sended to GRBL that have not been executed (no <i>ok</i> returned). */
    ArrayList<String> grblBufferContent = new ArrayList<>();
    
    /** Realtime GRBL positions. */
    Point3D grblMPos, grblWCO, grblWPos;
    
    /** The current GRBL Work Coordinate Offset. */
    HashMap<Integer,Point3D> grblWCOValues = new HashMap<>(9,1f);
    TreeMap<Integer, Double> grblSettings = new TreeMap<>();
    
    boolean stopThread;
    String grblVersion, grblAccessory = "", grblOptions = "";
    
    /** Realtime GRBL Override values. */
    int grblOverride[] = { 100, 100, 100 };
    
    /** FIFO containing cmd to send to GRBL. */
    Queue<String> grblCmdQueue = new ConcurrentLinkedQueue<>();
    
    /** The current parser states, according to last sended commands. */
    ParserState grblParserState = new ParserState();
    
    /** true if backLash compensation is enabled */
    boolean useBackLash = false;
    /** backLash values to add to GCode moves if direction change to go to the next destination. */
    double backLashValue[] = { 0, 0, 0 };
    /** last directions for all axes, x=false(for left), y=false(for north), z=false(for top). */
    boolean[] lastDirection = { false, false, false };
    /** backLash compensations values for all axes {X,Y,Z}. */
    double backLashCompensation[] = {0,0,0};
    /** For backLash compensations ; Contains the last destination asked (not backLash corrected). */
    GCode lastTrueDestination;
    /** For backLash compensation ; Contains the last true destination. */
    GCode lastCorrectedDestination;

    /** If not null, all command send to GRBL will be written onto this file. */
    private FileWriter gcodeOutputFileLogger;
    
    /** Thread that ask GRBL status each second when connected. */
    private Thread grblUpdateThread;
    /** If not null, a thread is sending commands actualy (or is waiting for GRBL to do it). */
    private Thread senderThread;
    
    /** The serial port for Arduino/GRBL comunication */
    CommPort commPort;
    public InputStream serialIn;
    public OutputStream serialOut;
    BufferedReader serialReader;
    PrintStream serialWriter;
    private String limitSwitchValue;
       
    public Object[] getSerialPorts() {
        Enumeration portsEnumaration = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> ports = new ArrayList<>();
        while( portsEnumaration.hasMoreElements()) {
            ports.add(((CommPortIdentifier)portsEnumaration.nextElement()).getName());
        }
        return ports.toArray();
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public boolean connect(String portName, int rate) throws NoSuchPortException, PortInUseException, TooManyListenersException, UnsupportedCommOperationException, IOException {
        
        try {
            if (commPort != null) disconnect(true);
            
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                listeners.forEach((li) -> { li.receivedMessage("Port '" + portName + "' currently in use."); });
           
            } else {
                commPort = portIdentifier.open(this.getClass().getName(), 2000);
                
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
                serialPort.notifyOnBreakInterrupt(true);
                serialPort.notifyOnFramingError(true);
                serialPort.notifyOnOverrunError(true);
                serialPort.notifyOnParityError(true);
                serialPort.notifyOnRingIndicator(true);
                
                serialPort.setSerialPortParams(rate, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                serialIn = serialPort.getInputStream();
                serialOut = serialPort.getOutputStream();
                serialWriter = new PrintStream(serialOut);
                serialReader = new BufferedReader(new InputStreamReader(serialIn));

                grblUpdateThread = new Thread( () -> {
                        do {
                            sendRTCmd('?');
                            try { Thread.sleep(1000); } catch (InterruptedException ex) { }
                            
                        } while( serialWriter != null );
                    }, "grblUpdateStatusThread");
                grblUpdateThread.start();

                setState(GRBL_STATE_INIT);
                return true; 
            }
            
        } catch (NoSuchPortException | PortInUseException | TooManyListenersException | 
                 UnsupportedCommOperationException | IOException ex) {
            
            listeners.forEach((li) -> {
                li.receivedMessage("COM: Can't connect to '" + portName + "' (" + ex.toString() + ")");
            });
            
            commPort = null;
            throw ex;
        }
        return false;
    }
    
    /** Send immediately a char to GRBL. */
    private void sendRTCmd( char c) {
        if ( isComOpen()) {
            serialWriter.print(c);
            serialWriter.flush();
        }
    }
    
    /**
     * Add a command to process.
     * @param cmd the command without '\n'
     */
    public void pushCmd( String cmd) {
        if ( cmd.endsWith("\n"))
            throw new Error("pushCmd() : ERROR '\\n' detected");
        grblCmdQueue.add(cmd);
        restartSenderThread(); 
    }
    
    /**
     * Eventualy start Thread to send new commands to GRBL if it was stopped.
     */
    private synchronized void restartSenderThread() {
        if ( senderThread == null ) { // start sender
            stopThread = false;
            senderThread = new Thread( this , "GRBLSenderThread");
            senderThread.start();
        }
    }
    
    /** 
     * Realy send a command to GRBL through serial port and/or logFile, 
     * update grblBuffers values and warn listeners.
     * @param cmd a GRBL command <b>without '\n'</b>
     */
    private void sendCmd( GCode cmd, String comment) throws IOException { 
        
        final String s = ((grblParserState!=null)?grblParserState.getCleanForGRBL(cmd):cmd.toGRBLString())+"\n";
        if ( (s.length()>1) && isComOpen()) {
            if ( s.length() > grblBufferFree) {
                //System.err.print("WAITING GRBL");
                while ( ! stopThread && (s.length() > grblBufferFree)) {
                    try { Thread.sleep(10); } catch (InterruptedException ex) { }
                    //System.err.print(".");
                }
               // System.err.println();
            } 
            if ( stopThread) return;
            serialWriter.print(s);
            System.err.print(s);
            serialWriter.flush();
            grblBufferFree -= s.length();
            grblBufferContent.add(s);
        }
        
        // Update Gx states.
        grblParserState.updateContextWith(cmd);
        final String line = cmd + comment;
        if ( gcodeOutputFileLogger != null) gcodeOutputFileLogger.write(line + "\n");
        else listeners.forEach((li) -> { li.sendedLine(line+"; => " + s); });        
    }
    
    /** 
     * Used to send commands through serial port to GRBL if any.<br>
     * Dont call it directly nor start a Thread with it, all is automatic !
     */
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void run() {
        try {
            while( ! (stopThread || grblCmdQueue.isEmpty()) ) {
                switch ( grblState) {
                    case GRBL_STATE_ALARM:
                        if ( grblCmdQueue.peek().equals("$X") || 
                             grblCmdQueue.peek().equals("$H")) break;
                    case GRBL_STATE_SLEEP: // GRBL ignore commands serialReader these states
                        grblCmdQueue.poll(); 
                        break;
                }
                if (grblCmdQueue.isEmpty()) break;       
                GCode cmd = applyBackLash(grblCmdQueue.poll());
                sendCmd(cmd, "");
            }          
        } catch ( Exception ex) {
            softReset();
            listeners.forEach((li) -> { li.exceptionInGRBLComThread(ex); });
        }
        senderThread = null;
    }
    
    /**
     * Eventualy insert a pre-backLash correction then return the rectified <i>cmd</i>.
     * @param cmd
     * @return the eventualy modified <i>cmd</i>
     * @throws IOException 
     */
    private GCode applyBackLash(String cmd) throws IOException {
        GCode dest = new GCode(cmd);
            
        if ( useBackLash & ((dest.isAMove() && dest.containsXorYCoordinate()) || dest.containsOnlyXorYCoordinate())) {
            
            if ((lastTrueDestination == null) && dest.isAMove()) { 
                // First move, after GRBL initialisation or abrupt stop/reset
                if ( isConnected() && isIdle() && (grblWPos != null)) 
                    lastTrueDestination = new GCode(dest.getG(), grblWPos);
                else if ( dest.isAPoint() ) {
                    // update backLash values according to this move
                    if ( isConnected() && isIdle() && (grblWPos != null)) {
                        if ( ! Double.isNaN(dest.getX())) lastDirection[0] = dest.getX() > grblWPos.getX();
                        if ( ! Double.isNaN(dest.getY())) lastDirection[1] = dest.getY() > grblWPos.getY();
                        if ( dest.get('Z') != null) lastDirection[2] = false; // assume last Z direction was UP
                        else lastDirection[2] = dest.get('Z').getValue() < grblWPos.getZ();
                    } else
                        lastTrueDestination = dest;
                }
                lastCorrectedDestination=null;
                return dest;
            }
            
            
            // update backLash values and directions
            boolean addBackLashCommand = false;
            if ( ! Double.isNaN(dest.getX())) {
                if ( lastDirection[0] ) { 
                    // was going right
                    if (dest.getX() < lastTrueDestination.getX()) { // reversal of X direction, adding backLashCmd
                        backLashCompensation[0] -= backLashValue[0];
                        lastDirection[0]=false;
                        addBackLashCommand=true;
                    }
                } else
                    if (dest.getX() > lastTrueDestination.getX()) { // reversal of X direction, adding backLashCmd
                        backLashCompensation[0] += backLashValue[0];
                        lastDirection[0]=true;
                        addBackLashCommand=true;
                    }
            }

            if ( ! Double.isNaN(dest.getY())) {
                if ( lastDirection[1] ) {
                    // was going north
                    if (dest.getY()< lastTrueDestination.getY()) { // reversal of X direction, adding backLashCmd
                        backLashCompensation[1] -= backLashValue[1];
                        lastDirection[1]=false;
                        addBackLashCommand=true;
                    }
                } else
                    if (dest.getY() > lastTrueDestination.getY()) { // reversal of X direction, adding backLashCmd
                        backLashCompensation[1] += backLashValue[1];
                        lastDirection[1]=true;
                        addBackLashCommand=true;
                    }
            }

            // add backLash move to return to the good start point for the next move if next move is G1
            if ( (dest.getG()==1) && addBackLashCommand && (lastCorrectedDestination != null)) { 
                //GCLine backLashAdjustment = (GCode)dest.clone();
                if ( ! Double.isNaN(dest.getX())) 
                    lastTrueDestination.setX(lastTrueDestination.getX()+backLashCompensation[0]);
                if ( ! Double.isNaN(dest.getY())) 
                    lastTrueDestination.setY(lastTrueDestination.getY()+backLashCompensation[1]);

                sendCmd(lastTrueDestination, " ; backLash");          
            }
            
            lastCorrectedDestination = (GCode) dest.clone();
            if ( ! Double.isNaN(dest.getX())) lastCorrectedDestination.setX( dest.getX()+backLashCompensation[0]);
            if ( ! Double.isNaN(dest.getY())) lastCorrectedDestination.setY( dest.getY()+backLashCompensation[1]);
            //if ( ! Double.isNaN(dest.getZ())) dest.setZ( dest.getZ()+backLashCompensation[2]);

            // and update the destination
            lastTrueDestination = dest;
            return (lastCorrectedDestination != null) ? lastCorrectedDestination : dest;
        }          
        return dest;
    }
                
    
    /** Is GRBL buffer enough free to send this new command ?
     * @param cmd the GRBL command to test with the current free size of the GRBL buffer
     * @return true if the command can be send without delay
     */
    public boolean canSend(String cmd) {
        int s = 0;
        for ( String c : grblCmdQueue) s += c.length();
                
        return (grblBufferFree > (s + cmd.length()));
    }
    
    public int getWaitingCommandQueueSize() {
        return grblCmdQueue.size();
    }
    
    /**
     * @return thue if the controler and GRBL do nothing.
     */
    public boolean isControlerIdle() {
        return grblCmdQueue.isEmpty() && (isConnected() ? isIdle() && grblBufferContent.isEmpty() : true );
    }
    
    /** Update grblState and call listeners if changed. */
    private void setState(int newState) {
        if ( grblState != newState) {
            grblState = newState;
            listeners.forEach((li) -> { li.stateChanged(); });
        }
    }
    
    
    
    /** Clear grblCmdQueue and restore values. */
    private void clearCmdQueue() {
        grblCmdQueue.clear();
        grblBufferContent.clear();
        grblBufferFree = grblBufferSize;
    }
    
    /** Update grblSettings map.
     * @param oneGRBLSetting a string like "$10=3"
     */
    private void updateGRBLSetting( String oneGRBLSetting) {
        String v[] = oneGRBLSetting.substring(1).split("=");
        grblSettings.put(Integer.valueOf(v[0]), Double.valueOf(v[1]));
        if ( Integer.parseInt(v[0]) == LAST_GRBL_SETTING_NUMBER)
            listeners.forEach((t) -> { t.settingsReady(); });
    }
    
    /** Update status.
     * @param grblStatusString as string like "<Idle|MPos:0.000,0.000,0.000|FS:0,0|WCO:-100.000,-100.000,0.000>"
     */
    private void updateGRBLStatus(String grblStatusString) {
        String[] args = grblStatusString.substring(1, grblStatusString.length()-1).split("\\|");
        String newLimitSwitchValues = "";
        
        // Read states
        switch ( args[0].toUpperCase()) {
            case "IDLE": 
                    if ((grblState != GRBL_STATE_IDLE) && (grblVersion == null)) { // First time IDLE, ask GRBL stettings
                        pushCmd("$I");
                        pushCmd("$#");
                        pushCmd("$G");
                        pushCmd("$$");
                    }
                    if ( grblState == GRBL_STATE_HOME) { 
                        // Homming just finished, initialise backLashCompensations
                        lastTrueDestination = new GCode(0, grblWPos); // reset last position
                        lastCorrectedDestination = null;                         
                        // initialise compensations according to home directions
                        final int bits =  grblSettings.get(23).intValue(); // homming direction invert mask:0b00000ZYX (1=>negDir)
                        for(int i = 0; i < 3; i++) {
                            lastDirection[i] = ((bits&(1<<i)) != 0);
                            backLashCompensation[0] = 0;
                        }
                    }      
                    setState(GRBL_STATE_IDLE); 
                    break;
            case "RUN": setState(GRBL_STATE_RUN); break;
            case "HOLD:0": setState(GRBL_STATE_HOLD); break;
            case "HOLD:1": setState(GRBL_STATE_HOLDING); break;
            case "JOG": setState(GRBL_STATE_JOG); break;
            case "DOOR:0": setState(GRBL_STATE_DOOR_CLOSED); break;
            case "DOOR:1": setState(GRBL_STATE_DOOR_WAITING_CLOSED); break;
            case "DOOR:2": setState(GRBL_STATE_DOOR_OPENNED); break;
            case "DOOR:3": setState(GRBL_STATE_DOOR_CLOSING); break;
            case "CHECK": setState(GRBL_STATE_CHECK); break;
            case "HOME": setState(GRBL_STATE_HOME); break;
            case "SLEEP": setState(GRBL_STATE_SLEEP); break;
            case "ALARM": setState(GRBL_STATE_ALARM); break;
            default:
                    System.err.println("unknow GRBL state : " + args[0]);
        }
        
        // Read others values
        for ( int n = 1; n < args.length; n++) {
            String c[], p[] = args[n].toUpperCase().split(":");
            Point3D pt;
            switch( p[0]) {
                case "MPOS": // Machine position
                    //System.err.println("GRBL Machine Pos : " + p[1]);
                            c = p[1].split(",");
                            pt = new Point3D(Double.parseDouble(c[0]),Double.parseDouble(c[1]),Double.parseDouble(c[2]));
                            if ( (grblMPos == null) || (pt.distance(grblMPos) > 0.00001)) {
                                grblMPos = pt;
                                // WPos = MPos - WCO
                                if ( grblWCO != null) {
                                    grblWPos = new Point3D(
                                                    grblMPos.getX() - grblWCO.getX(),
                                                    grblMPos.getY() - grblWCO.getY(),
                                                    grblMPos.getZ() - grblWCO.getZ());
                                }
                                listeners.forEach(GRBLCommListennerInterface::wPosChanged);
                            }
                            break;            
                case "WPOS": // Work position
                            //System.err.println("GRBL workpos : " + p[1]);
                            c = p[1].split(",");
                            pt = new Point3D(Double.parseDouble(c[0]),Double.parseDouble(c[1]),Double.parseDouble(c[2]));
                            if ( (grblWPos == null) || (pt.distance(grblWPos) > 0.00001)) {
                                grblWPos = pt;                                
                                listeners.forEach(GRBLCommListennerInterface::wPosChanged);
                            } 
                            break;
                case "WCO":  // Current Work coordinate
                            updateWCO(p[1]);
                            //System.err.println("GRBL workpos="+grblWPos+", calculated from WCO=" + p[1]);
                            break;
                case "BF":  // Buffer states (BF:15,128)
                            String v[] = p[1].split(",");
                            //if ( grblCmdQueue.isEmpty() && (Integer.parseInt(v[1]) != 128))     
                            //    grblBufferFree = Integer.parseInt(v[1]);                           
                            //System.serialWriter.println(String.format("GRBL Buffer %s blocks, %s bytes free", v[0], v[1]));                                  
                            break;
                case "LN":  // Line number currently being executed
                            System.out.println("GRBL line number : " + args[n]);
                            break;
                case "F":   // Current feed
                            grblFeed = Integer.parseInt(p[1]);
                            listeners.forEach(GRBLCommListennerInterface::feedSpindleChanged);
                            break;
                case "FS":  // Current feed and speed
                            String v2[] = p[1].split(",");
                            if (( grblFeed != Integer.parseInt(v2[0])) || (grblSpindle != Integer.parseInt(v2[1]))) {
                                grblFeed = Integer.parseInt(v2[0]);
                                grblSpindle = Integer.parseInt(v2[1]);
                                listeners.forEach(GRBLCommListennerInterface::feedSpindleChanged);
                            }
                            break;
                case "OV":
                            //System.serialWriter.println("GRBL override values : " + args[n]);
                            boolean changed = false;
                            String[] ov = p[1].split(",");                           
                            for(int i = 0; i < ov.length; i++) {
                                int val = Integer.parseInt(ov[i]);
                                if ( val != grblOverride[i]) {
                                    grblOverride[i] = val;
                                    changed = true;
                                }
                            }
                            if ( changed)
                                listeners.forEach(GRBLCommListennerInterface::overrideChanged);
                            
                            break;
                case "A":   if ( grblAccessory.equals(p[1])) {
                                grblAccessory = p[1];
                                listeners.forEach(GRBLCommListennerInterface::accessoryStateChanged);
                            }
                            break;
                case "PN":  
                            newLimitSwitchValues = p[1];
                            //System.out.println("GRBL stopPin : " + args[n]);
                            break;

                default:
                            System.out.println("GRBL other param : " + args[n]);
            }
        }
        if ( ! newLimitSwitchValues.equals(limitSwitchValue)) {
            limitSwitchValue = newLimitSwitchValues;
            listeners.forEach(GRBLCommListennerInterface::limitSwitchChanged);
        }
    }
    
    private void updateWCO(String wco) {
        String c[] = wco.split(",");
        Point3D oldWCO = grblWCO;
        grblWCO = new Point3D(Double.parseDouble(c[0]),Double.parseDouble(c[1]),Double.parseDouble(c[2]));
        
        // if WCO has changed, reset lastTrueDestination
        if ( (oldWCO != null) && (oldWCO.distance(grblWCO) > 0.00001) && (lastTrueDestination != null)) {
            lastTrueDestination.translate(grblWCO.getX()-oldWCO.getX(), grblWCO.getY()-oldWCO.getY());
            if ( lastCorrectedDestination != null)
                lastCorrectedDestination.translate(grblWCO.getX()-oldWCO.getX(), grblWCO.getY()-oldWCO.getY());
        }
        
        // WPos = MPos - WCO
        if ( grblMPos != null) {
            Point3D pt = new Point3D(
                    grblMPos.getX() - grblWCO.getX(),
                    grblMPos.getY() - grblWCO.getY(),
                    grblMPos.getZ() - grblWCO.getZ());
            if ( (grblWPos == null) || (pt.distance(grblWPos) > 0.00001)) {
                grblWPos = pt;
                listeners.forEach(GRBLCommListennerInterface::wPosChanged);
            }
        }
    }
    
    /**
     * @return true if serial port is open.
     */
    public boolean isConnected() {
        return (commPort != null) && (grblState != GRBL_STATE_INIT) && (grblState != GRBL_STATE_DISCONNECTED);
    }
    
    /**
     * @return true if serial port is open.
     */
    public boolean isComOpen() {
        return (commPort != null);
    }
    
    /**
     * @return true if GRBL is serialReader IDLE states
     */
    public boolean isIdle() {
        return (grblState == GRBL_STATE_IDLE);
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void disconnect(boolean forceClose) {
        stopThread = true;
        if ( commPort == null) return;
        clearCmdQueue();
        while ( senderThread != null ) 
            try { Thread.sleep(250); } catch (InterruptedException ex) { }
        
        try { 
            if ( forceClose) serialReader.close();  // hangs sometimes
            serialIn.close();
            serialOut.close();
        } catch (IOException ex) { }    
        serialWriter.close();
        serialWriter = null;
        if ( forceClose) commPort.close();
        commPort = null;
        grblVersion = null;
        grblWCO = grblMPos = null;
        lastCorrectedDestination = lastTrueDestination = null;
        grblSettings.clear();
        setState(GRBL_STATE_DISCONNECTED);
    }
    
    public static final Pattern WCO_PATTERN = Pattern.compile("^\\[G(5[4-9]|28|30|92):([^\\]]+)\\].*");
    @Override
    public void serialEvent(SerialPortEvent spe) {
        try {
            switch( spe.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                while ( serialReader.ready()) {
                    Matcher m;
                    // Read ACK
                    String l = serialReader.readLine();
                    for( GRBLCommListennerInterface li : listeners) 
                                li.receivedLine(l);  
            
                    if( l.startsWith("<")) updateGRBLStatus( l);
                    else if( l.startsWith(GRBL_INIT_STRING_HEADER)) {
                        grblCmdQueue.clear();
                        grblBufferFree = grblBufferSize;
                        grblParserState = new ParserState();    // reset parserState
                        System.out.println("reset");
                    } else if( l.startsWith("[VER:")) {
                        if ( (grblVersion==null) || ! grblVersion.equals(l.substring(1, l.length()-1))) {
                            grblVersion = l.substring(1, l.length()-1);
                            if ( isSettingsReady())
                                for( GRBLCommListennerInterface li : listeners) 
                                    li.settingsReady();
                        }
                        
                    } else if( l.startsWith("$")) updateGRBLSetting(l);
                    
                    else if (l.startsWith("[MSG:")) 
                                for( GRBLCommListennerInterface li : listeners) 
                                    li.receivedMessage(l.substring(5, l.length()-1));  
                    
                    else if ( l.startsWith("[OPT:")) {
                        String f[] = (grblOptions=l.substring(1, l.length()-1).split(":")[1]).split(",");
                        if ( f.length == 3) 
                            grblBufferSize = Integer.parseInt(f[2])-3; // TODO: why -3 ??
                        
                    } else if ( l.startsWith("[GC:")) {
                        String ps = l.substring(4, l.length()-1);
                        grblParserState.updateContextWith(new GCode(ps));
                        listeners.forEach(GRBLCommListennerInterface::stateChanged);
                        
                    } else if ( (m=WCO_PATTERN.matcher(l)).matches()) {
                        final int gNum = Integer.parseInt(m.group(1));
                        final Point3D p3d = new Point3D(m.group(2));
                        grblWCOValues.put( gNum, p3d);
                        // Use this as WCO ... is it good idea ?
                        if ( (gNum==54) && (grblParserState.get(ParserState.COORDINATE).getIntValue() == 54))
                            updateWCO( l.substring(1, l.length()-1).split(":")[1]);    
                        
                    } else if ( l.startsWith("[TLO:"))
                        grblParserState.updateTLO(l.substring(1, l.length()-1).split(":")[1] );
                    
                    else if ( l.startsWith("[PRB:"))
                        listeners.forEach( (li) -> { li.probFinished(l.substring(1, l.length()-1)); });
                        
                    else if ( l.startsWith("ALARM:")) {
                        if ( grblState != GRBL_STATE_IDLE)
                            lastCorrectedDestination = lastTrueDestination = null;
                        
                        setState( GRBL_STATE_ALARM);
                        listeners.forEach((li) -> {
                            li.receivedAlarm(Integer.parseInt(l.split(":")[1])); 
                        });

                        clearCmdQueue(); 
                    } 
                    else if (l.startsWith("error:")) {
                        // TODO verify backLash states !!
                        String wrongLine = grblBufferContent.remove(0);
                        grblBufferFree += wrongLine.length(); 
                        listeners.forEach((li) -> {
                            li.receivedError(Integer.parseInt(l.split(":")[1]), wrongLine);
                        });
                           
                        if ( ! grblCmdQueue.isEmpty()) restartSenderThread();
                    }

                    else if ( l.equals("ok")) {
                        if ( ! grblBufferContent.isEmpty()) {
                            String s = grblBufferContent.remove(0);
                            grblBufferFree += s.length(); 
                            //s = s.substring(0, s.length()-1);
                            //System.err.println("OK for (" + s + ")  =>  free " + grblBufferFree);
                        } else
                            throw new Error("GRBLComm.serialEvent() : 'ok' received but grblBufferContent is empty !!");
                        
                        if ( ! grblCmdQueue.isEmpty()) restartSenderThread();
                    } else {
                        System.err.println("serialEvent() : Unknow input [" + l + ']');
                    }
                }
                break;
            default:
                System.err.println("SerialEvent("+spe.getEventType()+")");
        }
        
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }  
   
    public Point3D getMPos() {
        if ( grblMPos == null) return null;
        return new Point3D(grblMPos.getX(), grblMPos.getY(), grblMPos.getZ());
    }
    
    public Point3D getWPos() {
        if ( grblWPos == null) return null;
        return new Point3D(grblWPos.getX(), grblWPos.getY(), grblWPos.getZ());
    }
    
    public Point3D getWCO() {
        if ( grblWCO == null) return null;
        return new Point3D(grblWCO.getX(), grblWCO.getY(), grblWCO.getZ());
    }
    
    public TreeMap<Integer,Double> getGRBLSettings() {
        // refresh settings
        if ( isConnected() && (grblSettings.isEmpty())) {
            pushCmd("$$");
            try { Thread.sleep(1000); } catch (InterruptedException e) { }          
        }
        @SuppressWarnings("unchecked") 
        TreeMap<Integer,Double> settings = new TreeMap<>();
        grblSettings.keySet().forEach((k) -> {
            settings.put(k, grblSettings.get(k));
        });
        return settings;
    }
    
    /**
     * Ask GRBL to reset his settings to factory defaults.
     */
    public void resetSettings() {
        pushCmd("$RST=$");
        grblSettings.clear();
        pushCmd("$$");
    }


    /** Send new settings to GRBL if changed.
     * @param settings the new GRBL settings to use
     */
    public void setGRBLSettings(Map<Integer, Double> settings) {
        settings.keySet().forEach((k) -> {
            final Double val = settings.get(k);
            final Double val2 = grblSettings.get(k);
            if ( (val2 != null) && (val != null) && ! val2.equals(val)) {
                pushCmd("$"+k+"="+val);
            }
        });
        grblSettings.clear();
        pushCmd("$$");
    }
    
    public void setBackLashEnabled( boolean useBackLash) {
        this.useBackLash = useBackLash;
    }
    
    /** Set BackLash value used at execution of G0/G1 moves.
     * @param bsX
     * @param bsY
     * @param bsZ 
     */
    public void setBackLashValues( double bsX, double bsY, double bsZ) {
        backLashValue[0] = bsX;
        backLashValue[1] = bsY;
        backLashValue[2] = bsZ;
    }
    
    /** Send JOG command to GRBL.
     * @param dx can be NaN
     * @param dy can be NaN
     * @param dz can be NaN
     * @param rate the JOG Feed rate used
     * @param relative choose relative or abolute translate 
     */
    public void jog( double dx, double dy, double dz, int rate, boolean relative) {
        if ( useBackLash) {
            if (relative) {
                if ( ! Double.isNaN(dx) && (Math.abs(dx) > 0.00000001)) if ( dx > 0) lastDirection[0]=(dx>0);
                if ( ! Double.isNaN(dy) && (Math.abs(dy) > 0.00000001)) if ( dy > 0) lastDirection[1]=(dy>0);
                if ( ! Double.isNaN(dz) && (Math.abs(dz) > 0.00000001)) if ( dz > 0) lastDirection[2]=(dz>0);
            } else {
                // TODO compare with current WPOS
                System.err.println("Warning: non relative JOG with backLash enabled");
            }
        }
        GCode l = new GCode((relative?"G91":"") +
                               (Double.isNaN(dx)?"":(relative && (dx==0))?"":String.format(Locale.ROOT, "X%.3f",dx)) +
                               (Double.isNaN(dy)?"":(relative && (dy==0))?"":String.format(Locale.ROOT, "Y%.3f",dy)) +
                               (Double.isNaN(dz)?"":(relative && (dz==0))?"":String.format(Locale.ROOT, "Z%.3f",dz)) + "F" + rate);
        pushCmd("$J=" + l.toGRBLString());
        lastCorrectedDestination = l;
    }
    
    public void sleepMode() throws IOException {
        pushCmd("$SLP");
    }
    
    public void killAlarm() {
        pushCmd("$X");
    }

    /**
     * Immediately halts and safely resets Grbl without a power-cycle.
     */
    public void softReset() {
        sendRTCmd((char)0x18);
        clearCmdQueue();
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public void holdAndReset() {
        hold();
        // wait hold 
        int i = 0;
        while( isComOpen() && (grblState != GRBL_STATE_HOLD) && (grblState != GRBL_STATE_ALARM))
            try { 
                Thread.sleep(100); 
                if ( i++ > 30) break;
            } catch (InterruptedException e) { }     
        softReset();
    }
    
    /** 
     * Resumes a feed hold, a safety door/parking states when the door is closed, and the M0 program pause states.
     */
    public void cycleStartResume() {
        sendRTCmd('~');
    }
    
    /** 
     * Places Grbl into a suspend or HOLD state. If serialReader motion, the machine will decelerate to a stop and then be suspended.
     */
    public void hold() {
        sendRTCmd('!');
    }
    
    /**
     * Immediately cancels the current jog states by a feed hold and automatically flushing any remaining jog commands serialReader the buffer.
     */
    public void jogCancel() {
        sendRTCmd((char)0x85);
        clearCmdQueue();
    }
    
    public void feed100Percent() {
        sendRTCmd((char)0x90);
    }
    
    public void feedIncrease10p() { 
        sendRTCmd((char)0x91);
    }
    
    public void feedDecrease10p() { 
        sendRTCmd((char)0x92);
    }
    
    public void feedIncrease1p() { 
        sendRTCmd((char)0x93);
    }
    
    public void feedDecrease1p() { 
        sendRTCmd((char)0x94);
    }
    
    public void setRapid100p() {
        sendRTCmd((char)0x95);
    }
    public void setRapid50p() {
        sendRTCmd((char)0x96);
    }
    public void setRapid25p() {
        sendRTCmd((char)0x97);
    }
    
    public void spindle100Percent() {
        sendRTCmd((char)0x99);
    }
    
    public void spindleIncrease10p() { 
        sendRTCmd((char)0x9A);
    }
    
    public void spindleDecrease10p() { 
        sendRTCmd((char)0x9B);
    }
    
    public void spindleIncrease1p() { 
        sendRTCmd((char)0x9C);
    }
    
    public void spindleDecrease1p() { 
        sendRTCmd((char)0x9D);
    }
    
    /**
     * Toggles spindle enable or disable states immediately, but only while serialReader the HOLD states.
     */
    public void toggleSpindleStop() {
        sendRTCmd((char)0x9E);
    }

    /**
     * Toggles flood coolant state and output pin until the next toggle or g-code command alters it.
     * This override directly changes the coolant modal state serialReader the g-code parser. 
     * Grbl will continue to operate normally like it receivedLine and executed an M8 or M9 g-code command.
     */
    public void toggleFloodCoolant() {
        sendRTCmd((char)0xA0);
    }
    
    /**
     * Enabled by ENABLE_M7 compile-time option. Default is disabled.
     * Toggles mist coolant state and output pin until the next toggle or g-code command alters it.
     */
    public void toggleMistAccessory() {
        sendRTCmd((char)0xA1);
    }
    
    ArrayList<GRBLCommListennerInterface> listeners = new ArrayList<>();
    public void addListenner(GRBLCommListennerInterface listener) {
        listeners.add(listener);
    }
    
    /** Remove this listener.
     * @param listener The listener to remove */
    public void removeListenner(GRBLCommListennerInterface listener) {
        listeners.remove(listener);
        //if ( listeners.isEmpty()) disconnect(); // disconnect if no more to listen GRBLControler events.
    }

    public int getState() {
        return grblState;
    }

    public boolean isCoolantEnabled() {
        return grblAccessory.contains("F");
    }

    public boolean isMistEnabled() {
        return grblAccessory.contains("M");
    }
    
    /**
     *  Return GRBL 1.1 Parser State.<br>
  Possibles values :
  Motion Mode - (G0), G1, G2, G3, G38.2, G38.3, G38.4, G38.5, G80
  Coordinate System Select - (G54), G55, G56, G57, G58, G59
  Plane Select - (G17 =XY), G18, G19
  Distance Mode - (G90 =absolute), G91
  Arc IJK Distance Mode - (G91.1 =IJ relative) not returned
  Feed Rate Mode : G93, (G94)
  Units Mode : G20, (G21)
  Cutter Radius Compensation : (G40) not returned
  Tool Length Offset : G43.1, (G49)
  Program Mode : (M0), M1, M2, M30 not returned
  Spindle State : M3, M4, (M5)
  Coolant State : M7, M8, (M9)
 
 Supported Non-Modal Commands (because they only affect the current line they are commanded serialReader) :
 G4, G10 L2, G10 L20, G28, G30, G28.1, G30.1, G53, G92, G92.1
     * 
     * @return a string like "G2 G54 G17 G21 G90 G94 M5 M9 T0 F100 S0"
     */
    public String getParserStateStr() {
        return grblParserState.toString();
    }
    
    // Return current GrblParserState (estimated)
    public ParserState getParserState() {
        return grblParserState.clone();
    }

    /**
     * @return an string representing GRBL states.
     */
    public String getStateStr() {
        return GRBL_STATE_STR[grblState];
    }
    
    /**
     * @param errorno
     * @return a human readable string of GRBL receivedError.
     */
    public static String getErrorMsg(int errorno) {
        for( int i = 0; i < GRBL_ERROR_MSG.length; i+= 2)
            if ( errorno == Integer.parseInt(GRBL_ERROR_MSG[i]))
                return GRBL_ERROR_MSG[i+1];
        return null;
    }
    
    /**
     * @param alarmno
     * @return a human readable string of GRBL receivedAlarm.
     */
    public static String getAlarmMsg(int alarmno) {
        for( int i = 0; i < GRBL_ALARM_MSG.length; i+= 2)
            if ( alarmno == Integer.parseInt(GRBL_ALARM_MSG[i]))
                return GRBL_ALARM_MSG[i+1];
        return null;
    }
    
    /**
     * @param settingNumber
     * @return a human readable string of GRBL setting number.
     */
    public static String getSettingDescription(int settingNumber) {
        for( int i = 0; i < GRBL_SETTING_STR.length; i+= 2)
            if ( settingNumber == Integer.parseInt(GRBL_SETTING_STR[i]))
                return GRBL_SETTING_STR[i+1];
        return null;
    }

    /** send G{0|1} command to GRBL.
     * @param p destination point (Z is ignored)
     * @param engrave will be G1 ?
     * @param feedRate 0 if not set
     */
    public void moveHeadTo(Point2D p, boolean engrave, int feedRate) {
        if ( feedRate <= 0)
            pushCmd(String.format(Locale.ROOT, "G"+(engrave?1:0)+"X%.5fY%.5f", p.getX(), p.getY()));
        else
            pushCmd(String.format(Locale.ROOT, "G"+(engrave?1:0)+"X%.5fY%.5fF%d", p.getX(), p.getY(), feedRate));
    }

    public int getFeedRate() {
        return grblFeed;
    }

    public int getSpindleSpeed() {
        return grblSpindle;
    }

    public void goHome() {
        pushCmd("$H");
    }

    /**
     * @return true if GRBL is configured to perform homing cycle.
     */
    public boolean canHome() {
        Double canHome = grblSettings.get(22);
        return (canHome!=null) && (canHome.intValue()==1);
    }
    
    /**
     * @return true if GRBL setting $32 == 1.
     */
    public boolean isLaserMode() {
        return grblSettings.get(32).intValue() == 1;
    }
    
    /** 
     * @return true if all GRBL Settings values are read.
     */
    public boolean isSettingsReady() {
        return grblSettings.get(LAST_GRBL_SETTING_NUMBER) != null;
    }

    /**
     * @return the total of lines serialReader buffer that GRBL has not parsed.
     */
    public int getCmdQueueSize() {
        return grblCmdQueue.size() + grblBufferContent.size();
    }

    /** Return GRBL Override values.
     * @return  An array of percents corresponding to Feed, Speed and Spindle. */
    public int[] getOverrideValues() {
        return grblOverride;
    }

    /** Return GRBL options.
     * @return  */
    public String getOPT() {
        return grblOptions;
    }
    
    public String getVersion() {
        return grblVersion;
    }

    /**
     * Create a file to write all next GCODE command to send to GRBL (whenever GRBL connected or not).<br>
     * The commands written into this file are not optimised.
     * @param outputFileName
     * @throws java.io.IOException
     */
    public void startFileLogger(String outputFileName) throws IOException {
        gcodeOutputFileLogger = new java.io.FileWriter(outputFileName);
    }

    /**
     * Stop current file logging.
     */
    public void stopFileLogger() {
        if ( gcodeOutputFileLogger != null)
            try {
                gcodeOutputFileLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(GRBLControler.class.getName()).log(Level.SEVERE, null, ex);
            }
        gcodeOutputFileLogger = null;
    }

    
    public void setVMPos(Point2D pos) {
        pushCmd("G10L20P1X"+pos.getX()+"Y"+pos.getY());
    }

    public String getMachineName() {
        if ( grblVersion != null) return grblVersion.split(":")[2];
        return "";
    }

    public String getLimitSwitchValues() {
        return limitSwitchValue;
    }

    public interface GRBLCommListennerInterface {
        
        public void wPosChanged();
        /**
         * Called when GRBL states or parser states had changed.
         */
        public void stateChanged();
        public void settingsReady();
        public void receivedError(int errono, String line);
        public void receivedAlarm(int alarmno);
        public void receivedMessage(String substring);        
        public void accessoryStateChanged();
        public void feedSpindleChanged();
        public void sendedLine(String cmd);
        public void receivedLine(String l);
        public void overrideChanged();
        public void exceptionInGRBLComThread(Exception ex);
        public void probFinished(String substring);
        public void limitSwitchChanged();
    }
}

