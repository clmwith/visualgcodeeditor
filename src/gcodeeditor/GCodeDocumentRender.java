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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gcodeeditor;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import gcodeeditor.gui.JProjectEditorPanel;
import gelements.G1Path;
import gelements.GDrillPoint;
import gelements.GElement;
import gelements.GGroup;
import gelements.GPocket3D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Class used to render the document into GCode to send to GRBL and|or save it in a file.
 * @author Clément
 */
public class GCodeDocumentRender implements Runnable {
    GRBLControler grbl;
    Configuration conf;           
    ParserState state;
    boolean stopThread;
    
    // Read only variables to know what is currently doing.
    GGroup document, currentGroup;
    GElement currentPath, lastBlock;
    String currentGLine;
    int currentBlockNumber, currentBlockLine;
    int currentPass, currentPassCount;
    double currentFeed, currentPower;
    boolean laserMode;   
    double currentZ, currentZStart, currentZEnd, currentZPassDepth;

    private FileWriter outputFile;
    
    
    public GCodeDocumentRender(Configuration conf,  RenderListener l) {
        this.conf = conf;
        listener = l;
    }
    
    /**
     * Set blocks to print.
     * @param document
     * @param grbl
     */
    public void setDocumentToPrint(GGroup document, GRBLControler grbl) {
        this.grbl = grbl;
        currentGroup= this.document = document;
        currentBlockNumber=0;
        currentPath = null;
        currentBlockLine=0;
        currentGLine=null;
        currentPass=0;
        currentZ=currentZStart=currentZEnd=Double.NaN;   
        outputFile = null;
        stopThread = false;
        updateGUI();
    }
    
    /**
     * @param laserMode Use laser (without Z moves) ?
     * @param fileName  Save the job into a file ? (or null)
     * @throws java.io.FileNotFoundException
     */
    public void setParam(boolean laserMode, String fileName) throws FileNotFoundException, IOException {
        this.laserMode = laserMode;
        if ( fileName != null) outputFile = new FileWriter( new File(fileName));
        else {
            if ( outputFile != null) outputFile.close();
            outputFile = null;
        }
    }

    /** Stop as soon as possible the job. (stop sending en exit thread) */
    public void stop() {
        stopThread = true;
    }
    
    /**
     * Execute the Job. (use it into a Thread).
     */
    @Override
    @SuppressWarnings({"CallToPrintStackTrace", "SleepWhileInLoop"})
    public void run() {
        try {     
            long t1 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            
            if (outputFile == null) {        
                // send to GRBL
                state = grbl.getParserState();
            } else
                // save to file
                state = new ParserState();
            
            //grbl.startFileLogger( outputFileName);
            
            currentZ = currentZStart = currentZEnd = currentZPassDepth = Double.NaN;
            sendCmd("; Generated with SimpleGCodeVisualEditor " + JProjectEditorPanel.SVGE_RELEASE);

            if ( laserMode) {
                sendCmd(conf.adaptativePower ? "M4" : "M3");
                sendCmd("G0S0"); 
            } else {
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(conf.safeZHeightForMoving)+"M5S0");
            }
            
            // Execute laser/milling job with defaults values
            sendGroup(document, new EngravingProperties(conf), true);

            if ( ! laserMode) {
                // Return to safe Z position
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(conf.safeZHeightForMoving)+"M5S0");
            } else
                sendCmd("G0M5S0"); 

            sendCmd("M2");
            sendCmd(";End of Job");
            long t2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
            System.out.println("Rendering duration (s) = " + (t2-t1)/1000);
                        
            while (grbl.isConnected() && ! stopThread && 
                    ((grbl.getState() == GRBLControler.GRBL_STATE_RUN)  ||
                     (grbl.getState() == GRBLControler.GRBL_STATE_HOLD) ||
                      ! grbl.isControlerIdle())) {
                updateGUI();
                try { Thread.sleep(330); } catch ( InterruptedException e) { }            
            }

            grbl.stopFileLogger();                            
        }
        catch ( Exception e) { 
            e.printStackTrace(); 
            grbl.holdAndReset();
            
            if (outputFile != null) {
                try {
                    outputFile.close();
                } catch (IOException ex) {
                    listener.error("IOError :\n"+ex.getLocalizedMessage());
                }
                outputFile = null;
            } else {
                //grbl.stopFileLogger();
                grbl.softReset();  
            }
            listener.error("Exception in GCode Execution Thread :\n"+e.getLocalizedMessage());
        }    
        listener.executionFinished();
    }

    /**
     * Used to send GCode to GRBL or into outputFile.
     * @param cmd The gcode line to send
     * @throws IOException 
     */
    @SuppressWarnings("SleepWhileInLoop")
    private void sendCmd(String cmd) throws IOException {
        if ( stopThread) return;
        
        if ( outputFile != null) outputFile.append(cmd);
        else {    
            while ( grbl.isConnected() && (grbl.getWaitingCommandQueueSize() > 3)) 
                try { Thread.sleep(20); } catch ( InterruptedException e) { }
            grbl.pushCmd( cmd);
        }
        state.updateContextWith(new GCode(cmd)); // TODO: use grbl.parserState ?
    }

    /**
     * Send the GCode corresponding of this group (with sendCmd function)
     * @param group
     * @param currentProperties
     * @param firstPass
     * @throws IOException 
     */
    private void sendGroup(GGroup group, EngravingProperties currentProperties, boolean firstPass) throws IOException {                                                                                                 
        if ( ! group.isEnabled()) return;
        currentGroup=group;
        
        final EngravingProperties groupProp = group.properties;

        if ( currentProperties.isAllAtOnce()) { 
            // We are into a one time flat execution at fixed Z : parse one time without Pass parameters
            // update only speed and power
            if ( ! Double.isNaN(groupProp.getFeed())) currentProperties.setFeed( groupProp.getFeed());
            if ( groupProp.getPower() != -1) currentProperties.setPower( groupProp.getPower());

            for ( GElement b : group.getAll()) {  
                if ( b instanceof GGroup)
                    sendGroup((GGroup) b, currentProperties.clone(), firstPass);               
                else 
                    sendElement(b, currentProperties.clone(), firstPass);
                
                currentGroup=group;
            }
            return;
        }
            
        EngravingProperties.udateHeritedProps(currentProperties, groupProp);
        if ( groupProp.isAllAtOnce()) {
            // Execute all content as simple(s) flat paths at fixed Z
            
            sendCmd(";START_GROUPED_EXECUTION: group:"+group.getName());
            sendCmd(";PASS_COUNT:"+currentProperties.getPassCount());
            
            Iterator<Double> it = currentProperties.iterator();
            int cP = 0;
            while ( it.hasNext()) {
                currentZ = it.next();
                if ( ! laserMode && Double.isNaN(currentZ)) throw new Error("Can find zLevel for element " + group);
                
                currentPass = ++cP;
                currentZStart = currentProperties.getZStart();     
                currentZEnd   = currentProperties.getZEnd();
                currentZPassDepth = currentProperties.getPassDepth();                 
            
                sendCmd(";PASS:" + currentPass + (Double.isNaN(currentZ)?"": ", at Z="+currentZ));

                EngravingProperties onePassProp = new EngravingProperties();
                onePassProp.allAtOnce = true;
                onePassProp.zStart = currentZ;
                onePassProp.passCount = 1;
                for( GElement e : group.getAll()) {
                    currentGroup=group;
                    if ( e instanceof GGroup) sendGroup((GGroup) e, onePassProp.clone(), (cP==1));
                    else sendElement(e, onePassProp.clone(), (cP==1));        
                }
            }       
            sendCmd(";END_GROUPED_EXECUTION: group:"+group.getName());
            
        } else {
            // Normal sequential/recursive mode, remplace properties and execute content sequentially
            currentProperties = EngravingProperties.udateHeritedProps(currentProperties, group.properties);

            for ( GElement b : group.getAll()) {
                currentGroup=group;
                
                if ( b instanceof GGroup) 
                    sendGroup((GGroup) b, currentProperties.clone(), true);
                else 
                    sendElement(b,  currentProperties.clone(), true);
                if ( stopThread) return;
            }
        }                   
    }

    /** 
     * Send an entire element one time (if onePass mode) or multiple times according to pass values.
     * @param path
     * @param currProps
     * @throws IOException 
     */
    private void sendElement(GElement path, EngravingProperties currProps, boolean firstPass) throws IOException {
        if ( ! path.isEnabled()) return;

        boolean onePass = currProps.isAllAtOnce();

        sendCmd(";BEGIN_ELEMENT: " + path.getName());
        currProps = EngravingProperties.udateHeritedProps(currProps, path.properties);

        // remplace Feed and Spindle if needed
        if ( ! Double.isNaN(currProps.getFeed())) sendCmd("F" + (currentFeed=currProps.getFeed()));
        if ( currProps.getPower() != -1) sendCmd("S" + GWord.GCODE_NUMBER_FORMAT.format(currentPower=currProps.getPower()));

        if ( onePass) {
            // flat execution mode of the path wiout Z positioning
            if ( (path instanceof GDrillPoint) && firstPass) {
                // Special case for Drill point emulation
                sendGDrillPoint((GDrillPoint) path, currProps);

            } else
                sendAllLines(path);

        } else {
            // MultiPass mode : remplace PassParameters and execute the content for each Pass
 
            if ( path instanceof GDrillPoint) {
                // Special case for Drill point emulation
                sendGDrillPoint((GDrillPoint) path, currProps);

            } else {           
                sendCmd(";START_PATH:"+path.getName());
                sendCmd(";PASS_COUNT:"+currProps.getPassCount());
                
                currentPass = 1;
                currentPassCount = currProps.getPassCount();                         
                for( Iterator<Double> passHeight = currProps.iterator(); passHeight.hasNext(); currentPass++) {
                    currentZ = passHeight.next();
                    
                    sendCmd(";PASS:" + currentPass + (Double.isNaN(currentZ)?"": ", at Z="+currentZ));
                    if ( path instanceof GPocket3D) {
                        // Special case for the pocket 3D
                        if ( currentZStart-currentZ < ((GPocket3D)path).getInlayDepth()) {
                            ArrayList<GElement> l = new ArrayList<>();
                            l.add(((GPocket3D)path).getPassBoundsPath(currentZStart-currentZ));
                            EngravingProperties ep = new EngravingProperties();
                            ep.setZStart(currentZ);
                            ep.setPassCount(1);
                            ep.setAllAtOnce(true);
                            sendGroup(G1Path.makePocket(l, conf.toolDiameter/2), ep, false);
                        }
                        
                    } else {                        
                        sendAllLines(path);
                    }
                }  
            }
            lastBlock=path;           
        }
        sendCmd(";END_ELEMENT: "+path.getName());
    }
    
    
    /**
     * Send one time all lines of this path at current zLevel.
     * @param path
     * @throws IOException 
     */
    private void sendAllLines(GElement path) throws IOException {
        if (path==null) return;
        assert( ! (path instanceof GGroup));

        path = path.flatten();
   //     if ( !(path instanceof G1Path))
    //        path = path.flatten();

        for( currentBlockLine = 0; ! stopThread && (currentBlockLine < path.size()); currentBlockLine++) {

            updateGUI();
            GCode l = (GCode) path.getLine(currentBlockLine).clone();
            if ( l.isComment()) continue;
          
            if (l.getG()==0) {
                safeMoveTo(l, currentZ, conf.safeZHeightForMoving);   

            } else
                sendCmd(currentGLine=l.toGRBLString());
        }    
        lastBlock = path;
    }
    
    /**
     * Send Drill point emulation GCode.
     * with G81 the drill is in shot !
     * with G83 X10 Y10 Z-30 R2 Q5 F100 
     * Drill from R=2 to Z=-30 in pecks of 5mm (Q5), retracting between each.
     * 
     * if L is present, the drill i set L time from Zstart to Zend, 
     * and eventualy if R is present, the head go to R height value after each pass
     * 
     * @param path
     * @param herited 
     */
    private void sendGDrillPoint(GDrillPoint path, EngravingProperties herited) throws IOException {
        if ( laserMode ) return;
        EngravingProperties.udateHeritedProps(herited, path.properties);
                
        currentPath = path;
        currentZStart = herited.zStart;
        currentZEnd = herited.zEnd;
 
        GCode l = path.getLine(1);
        double safeZ = Double.isNaN(l.getValue('R')) ? conf.safeZHeightForMoving : l.getValue('R');
        currentZPassDepth = Double.isNaN(l.getValue('Q')) ? path.properties.getPassDepth() : l.getValue('Q');
        
        if ( Double.isNaN(currentZStart) || Double.isNaN(currentZEnd) || Double.isNaN(currentZPassDepth)) {
            currentPassCount = -1;
        } else {
            currentPassCount = (int)Math.ceil((currentZStart - currentZEnd) / currentZPassDepth) + 1;
            currentZ = currentZStart;
        }
        if ( currentPassCount < 1) currentPassCount = 1;

         // Go up if we must translate to destination
        safeMoveTo(l, safeZ, conf.safeZHeightForMoving);                     
        if ( ! Double.isNaN(herited.getFeed())) sendCmd("F"+ GWord.GCODE_NUMBER_FORMAT.format(herited.getFeed()));
        
        if ( Double.isNaN(currentZPassDepth)) { 
            // one shot drill
            sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZEnd));
            if ( l.get('P') != null) // make a pause at botton of hole ?
                    sendCmd("G4 P"+ l.get('P').getIntValue());
            sendCmd("G"+(laserMode?0:1)+"Z"+GWord.GCODE_NUMBER_FORMAT.format(safeZ));
            
        } else { 
            // multi pass drill
            currentZ=currentZStart-currentZPassDepth;
            boolean finished;
            do {
                if ( currentZ < currentZEnd) currentZ = currentZEnd;
                sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZ));
                
                if ( l.get('P') != null) // make a pause at botton of hole ?
                    sendCmd("G4 P"+ l.get('P').getIntValue());

                sendCmd("G"+(laserMode?0:1)+"Z"+GWord.GCODE_NUMBER_FORMAT.format(safeZ));

                finished = Math.abs(currentZ - currentZEnd) < 0.00001;
                currentZ -= currentZPassDepth;
            } while ( ! finished);
        }
    }

    /**
     * Used to go safely to position (X,Y,zLevelDestination). 
     * 
     * - It goes to Z 'moveAtZheight' (if needed),
     * - move to (X,Y) at JOG Speed ! 
     * - then go down to Z 'zLevelDestination' (if needed)
     * 
     * WARNING: FEED RATE IS CHANGED, THEN AFTER IT RE-SET RIGHT FEED RATE
     * 
     * @param moveAtZSafeheight start with a <i>G0 Zxx</i> to go to destination if needed
     * @param destinationXYPoint  the destination (X,Y) to move to
     * @param zLevelDestination after translate, if not NaN, do a G1 to this Z if needed
     * @throws IOException 
     */
    private void safeMoveTo(GCode destinationXYPoint, double zLevelDestination, double moveAtZSafeheight) throws IOException {
        assert( destinationXYPoint.isAPoint());
        destinationXYPoint = new GCode(0, destinationXYPoint.getX(), destinationXYPoint.getY());
        
        // remove potential wrong Z value masked by zLevelDestination
        if ( destinationXYPoint.get('Z') != null) destinationXYPoint.remove('Z');
        
        if ( Double.isNaN(zLevelDestination) || Double.isNaN(moveAtZSafeheight)) {
            // nothing to do just go to destination
            sendCmd(destinationXYPoint.toGRBLString());
            return;
        }        
             
        final GCode curPos = state.getGXYPositon();
        final double curZ = curPos.contains('Z') ?  curPos.get('Z').getValue() : Double.NaN;     
        if ( ! curPos.isAPoint() || ! curPos.isAtSamePosition(destinationXYPoint)) {
            // We have to move to dest (X,Y)                                              
                
            if ( laserMode) {
                // go to destination with Z change at same time
                destinationXYPoint.set('Z', zLevelDestination);                

            } else {
                // goto Z level moveAtZheight before moving
                sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(moveAtZSafeheight));  
                // then move to destination
                sendCmd(destinationXYPoint.toGRBLString());  
            }                
        }
        
        // We are in place now, juste change Z if needed
        if ( Double.isNaN(curZ) || (Math.abs(curZ - zLevelDestination) > 0.00001)) {  
            if (laserMode)
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(zLevelDestination));
            else
                sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(zLevelDestination));

        }                
        updateGUI();
    }
    
    /**
     * Update public variables and call updateGui of listeners.
     */
    private void updateGUI() {
        ExecutionState s = new ExecutionState();
        if ( currentGroup != null) {
            GGroup g, parent;
            String gname = (g=currentGroup).getName();
            
            while( (parent = document.getParent(g)) != null) {
                gname = parent.getName() + "/" + gname;
                g = parent;
            }
            s.currentGroupName = gname + "(" + currentBlockNumber + "/" + currentGroup.size() + ")";
        } else 
            s.currentGroupName = "-";
        
        s.currentElementName = (currentPath!=null)?currentPath.getName():"-";
        s.currentBlockNumber = currentBlockNumber;
        s.currentBlockLine = currentBlockLine;   
        s.currentPass = currentPass;
        s.currentPassCount = currentPassCount;
        s.currentZ = currentZ;
        s.currentZStart = currentZStart;
        s.currentZEnd= currentZEnd;
        s.currentZDepth = currentZPassDepth;
        EventQueue.invokeLater(() -> { listener.updateGUI( s); });
    }   
    
    /**
     * The actual state of content rending, to send to RenderListenners.
     */
    public class ExecutionState {
        public String currentGroupName;
        public String currentElementName;
        public int currentBlockNumber, currentBlockLine; 
        public int currentPass, currentPassCount;
        public double currentZ, currentZStart, currentZEnd, currentZDepth;
    }
    public interface RenderListener {
        void updateGUI(ExecutionState state);
        void error(String error);
        void executionFinished();
    }   
    RenderListener listener;
}
