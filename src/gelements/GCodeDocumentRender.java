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
package gelements;

import gcodeeditor.ParserState;
import gcodeeditor.GWord;
import gcodeeditor.GCode;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import gcodeeditor.Configuration;
import gcodeeditor.JBlocksViewer;
import gcodeeditor.GRBLControler;

/**
 * Class used to render the document into GCode to send to GRBL and|or save it in a file.
 * @author Clément
 */
public class GCodeDocumentRender implements Runnable {
    GRBLControler grbl;
    Configuration conf;           
    ParserState state;
    boolean stopThread;
    
    GGroup document, currentGroup;
    GElement currentPath, lastBlock;
    String currentGLine;
    int currentBlockNumber, currentBlockLine;
    int currentPass, currentPassCount;
    double currentFeed, currentPower;
    boolean laserMode;   
    double currentZ, currentZStart, currentZEnd, currentZPassDepth;

    private String outputFileName;
    
    
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
        outputFileName = null;
        stopThread = false;
        updateGUI();
    }
    
    /**
     * @param laserMode Use laser (without Z moves) ?
     * @param fileName  Save the job into a file ? (or null)
     */
    public void setParam(boolean laserMode, String fileName) {
        this.laserMode = laserMode;
        outputFileName = fileName;
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
            
            if (outputFileName != null) grbl.startFileLogger( outputFileName);

            state = grbl.getParserState();
            
            currentZ = currentZStart = currentZEnd = currentZPassDepth = Double.NaN;
            sendCmd("; Generated with SimpleGCodeVisualEditor " + JBlocksViewer.SVGE_RELEASE);

            if ( laserMode) {
                sendCmd(conf.adaptativePower ? "M4" : "M3");
                sendCmd("G0S0"); 
            } else {
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(conf.safeZHeightForMoving)+"M5S0");
            }
            
            sendCmd("F"+GWord.GCODE_NUMBER_FORMAT.format(conf.feedRate));
            sendCmd("S"+GWord.GCODE_NUMBER_FORMAT.format(conf.spindleLaserPower));
            
            // Start with default engraving values
            sendGroup(document, new EngravingProperties(conf));

            if ( ! laserMode)
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(conf.safeZHeightForMoving)+"M5S0");
            else
                sendCmd("G0M5S0"); 

            updateGUI();
            // try { Thread.sleep(1000); } catch ( InterruptedException e) { } // Small delay
            while (grbl.isConnected() && ! stopThread && 
                    ((grbl.getState() == GRBLControler.GRBL_STATE_RUN)||(grbl.getState() == GRBLControler.GRBL_STATE_HOLD)))
                try { Thread.sleep(100); } catch ( InterruptedException e) { }

            sendCmd("M2");
            sendCmd(";End of Gcode");
            // Wait task finished
            while( ! grbl.isControlerIdle()) try { Thread.sleep(100); } catch ( InterruptedException e) { }
            grbl.stopFileLogger();            
        }
        catch ( Exception e) { 
            e.printStackTrace(); 
            grbl.stopFileLogger();
            grbl.softReset();  
            listener.error("Exception in GCode Execution Thread :\n"+e.getLocalizedMessage());
        }  
        listener.executionFinished();
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void sendCmd(String cmd) throws IOException {
        if ( stopThread) return;
        
        while ( grbl.isConnected() && (grbl.getWaitingCommandQueueSize() > 3)) 
            try { Thread.sleep(20); } catch ( InterruptedException e) { }
        grbl.pushCmd( cmd);
        state.updateContextWith(new GCode(cmd)); // TODO: use grbl.parserState ?
    }

    private void sendGroup(GGroup group, EngravingProperties herited) throws IOException {                                                                                                 
        if ( ! group.isEnabled()) return;
        EngravingProperties p = group.properties;

        if ( herited.isAllAtOnce()) { 
            // We are into a flat execution : parse one time without Pass parameters
            if ( ! Double.isNaN(p.getFeed())) herited.setFeed( p.getFeed());
            if ( p.getPower() != -1) herited.setPower( p.getPower());

            for ( GElement b : group.getAll()) {
                currentGroup=group;
                if ( b instanceof GGroup) sendGroup((GGroup) b, herited.clone());
                else sendElement(b, herited.clone());
            }
            return;
        }

        if ( p.isAllAtOnce()) {
            // Execute all content as simple(s) flat paths
            EngravingProperties.udateHeritedProps(herited, p);
            currentZStart = herited.getZStart();     
            currentZEnd   = herited.getZEnd();
            currentZPassDepth = herited.getPassDepth();    
            if ((currentPassCount < 1) && ((Double.isNaN(currentZStart) || Double.isNaN(currentZEnd) || Double.isNaN(currentZPassDepth)))) {
                currentPassCount = -1;
            } else {
                currentPassCount = (int)Math.ceil((currentZStart - currentZEnd) / currentZPassDepth) + 1;
                currentZ = currentZStart;
            }
            if ( currentPassCount < 1) currentPassCount = 1;
            currentZ = currentZStart;
            if ( ! laserMode && Double.isNaN(currentZ)) throw new Error("Can find zLevel for element " + group);

            sendCmd(";START_GROUPED_EXECUTION: group:"+group.getName());
            sendCmd(";PASS_COUNT:"+currentPassCount);
            for( currentPass = 1; ! stopThread && (currentPass <= currentPassCount); currentPass++) {
                sendCmd(";PASS:" + currentPass + (Double.isNaN(currentZ)?"": ", at Z="+currentZ));

                for( GElement e : group.getAll()) {
                    if ( e instanceof GGroup) sendGroup((GGroup) e, herited.clone());
                    else sendElement(e, herited.clone());
                }

                if ( ! Double.isNaN(currentZ) && ! Double.isNaN(currentZPassDepth)) {
                    currentZ -= currentZPassDepth;
                    if (  currentZ < currentZEnd) currentZ = currentZEnd;
                }
            }       
            sendCmd(";END_GROUPED_EXECUTION: group:"+group.getName());
        } else {
            // Normal sequential mode, remplace properties and execute content sequentially
            herited = EngravingProperties.udateHeritedProps(herited, group.properties);

            for ( GElement b : group.getAll()) {
                currentGroup=group;
                if ( b instanceof GGroup) 
                    sendGroup((GGroup) b, herited.clone());
                else 
                    sendElement(b,  herited.clone());
                if ( stopThread) return;
            }
        }                   
    }

    /** 
     * Send an entire element one time (if onePass mode) or for all pass needed.
     * @param path
     * @param herited
     * @throws IOException 
     */
    private void sendElement(GElement path, EngravingProperties herited) throws IOException {
        if ( ! path.isEnabled()) return;
        currentPath=path;
        boolean onePass = herited.isAllAtOnce();

        sendCmd(";Element: " + path.getName());
        herited = EngravingProperties.udateHeritedProps(herited, path.properties);

        // remplace Feed and Spindle if needed
        if ( ! Double.isNaN(herited.getFeed()) && (state.getFeed() != herited.getFeed())) 
            sendCmd("F" + (currentFeed=herited.getFeed()));
        if ( (herited.getPower() != -1) && (state.getPower() != herited.getPower()))
            sendCmd("S" + GWord.GCODE_NUMBER_FORMAT.format(currentPower=herited.getPower()));

        if ( onePass) {
            // flat execution mode of the path
            sendAllLines(path);

        } else {
            // MultiPass mode : remplace PassParameters and execute the content for each Pass
            // TODO: prendre la plus petite valeur de passe entre PassCount (si fixé) et le calcul normal.

            EngravingProperties.udateHeritedProps(herited, path.properties);
            currentZStart = herited.getZStart();     
            currentZEnd   = herited.getZEnd();
            currentZPassDepth = herited.getPassDepth();
            
            if ( Double.isNaN(currentZEnd) || Double.isNaN(currentZPassDepth)) {
                currentPassCount = herited.getPassCount();
                if ( Double.isNaN(currentZEnd)) currentZEnd = currentZStart - currentZPassDepth * currentPassCount;
                else
                    currentZPassDepth = (currentZStart - currentZEnd) / currentPassCount;
            } else {
                currentPassCount = (int)Math.ceil((currentZStart - currentZEnd) / currentZPassDepth) + 1;
                currentZ = currentZStart;
            }
            if ( currentPassCount < 1) currentPassCount = 1;
            currentZ = currentZStart;
            
            sendCmd(";START_PATH:"+path.getName());
            sendCmd(";PASS_COUNT:"+currentPassCount);

            // Special case for Drill point emulation ?
            if ( path instanceof GDrillPoint) {
                if ( laserMode ) return;
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
                safeMoveTo(l, Double.NaN, safeZ);                     
                updateGUI();
                if ( Double.isNaN(currentZPassDepth)) { 
                    // one shot drill
                    sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZEnd));
                } else { 
                    // multi pass drill
                    currentZ=currentZStart-currentZPassDepth;
                    boolean finished;
                    do {
                        if ( currentZ < currentZEnd) currentZ = currentZEnd;
                        sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZ));

                        finished = Math.abs(currentZ - currentZEnd) < 0.00001;
                        if ( ! finished) 
                            sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(safeZ));

                        currentZ -= currentZPassDepth;
                    } while ( ! finished);
                }
                if ( l.get('P') != null) // make a pause ?
                        sendCmd("G4 P"+ l.get('P').getIntValue());

                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(safeZ));

            } else {
                currentZ = currentZStart;
                for( currentPass = 1; ! stopThread && (currentPass <= currentPassCount); currentPass++) {
                    sendCmd(";PASS:" + currentPass + (Double.isNaN(currentZ)?"": ", at Z="+currentZ));

                    if ( path instanceof GTextOnPath) {
                        EngravingProperties ep = new EngravingProperties();
                        ep.setZStart(currentZ);
                        ep.setPassCount(1);
                        ep.setAllAtOnce(true);
                        sendGroup(((GTextOnPath)path).getGText(), ep);
                    
                    } else if ( path instanceof GPocket3D) {
                        // Special case for the pocket 3D
                        if ( currentZStart-currentZ < ((GPocket3D)path).getInlayDepth()) {
                            ArrayList<GElement> l = new ArrayList<>();
                            l.add(((GPocket3D)path).getPassBoundsPath(currentZStart-currentZ));
                            EngravingProperties ep = new EngravingProperties();
                            ep.setZStart(currentZ);
                            ep.setPassCount(1);
                            ep.setAllAtOnce(true);
                            sendGroup(G1Path.makePocket(l, conf.toolDiameter/2), ep);
                        }
                    } else
                        sendAllLines(path);

                    if ( ! Double.isNaN(currentZ) && ! Double.isNaN(currentZPassDepth)) {
                        currentZ -= currentZPassDepth;
                        if (  currentZ < currentZEnd) currentZ = currentZEnd;
                    }
                }  
            }
            lastBlock=path;
            sendCmd(";END_PATH:"+path.getName());
        }
    }

    /**
     * Send one time all lines of this path at current zLevel.
     * @param path
     * @throws IOException 
     */
    private void sendAllLines(GElement path) throws IOException {
        if (path==null) return;

        if ( !(path instanceof G1Path))
            path = path.flatten();

        //boolean first = true;
        // send all lines
        for( currentBlockLine = 0; ! stopThread && (currentBlockLine < path.size()); currentBlockLine++) {

            updateGUI();
            GCode l = (GCode) path.getLine(currentBlockLine).clone();
            if ( l.isComment()) continue;

            // Go up if we must translate to next point without a Laser
            if ( /* first && */ ! laserMode && (l.getG()==0) && l.isAPoint()) {
                //first = false; 
                double d = l.distance(state.getGXYPositon());
                if ( ! Double.isNaN(currentZ) && (state.getZ() < conf.safeZHeightForMoving) && 
                    (Double.isNaN(d) || (d > 0.0001)))
                        sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(conf.safeZHeightForMoving));
            }

            // Starting engraving ?
            if ( ! state.isEngraving() && (l.getG()>0)) {
                if ( ! Double.isNaN(currentZ) && (Double.isNaN(state.getZ()) || 
                        ((Math.abs(state.getZ() -currentZ) > 0.00001))))
                    if ( laserMode)
                        sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZ));
                    else
                        sendCmd("G1Z"+GWord.GCODE_NUMBER_FORMAT.format(currentZ));
            }

            sendCmd(currentGLine=l.toGRBLString());
        }
        lastBlock = path;
    }

    /**
     * Used to safe start a new path.
     * @param moveAtZheight start with a <i>G0 Zxx</i> to go to destination if needed
     * @param destination   send a G0 <i>destination.getX()</i>  <i>destination.getY()</i> if needed
     * @param zLevelDestination after translate, if not NaN, do a G1 to this Z if needed
     * @throws IOException 
     */
    private void safeMoveTo(GCode destination, double zLevelDestination, double moveAtZheight) throws IOException {
        if (laserMode || Double.isNaN(currentZ)) return;
        double d = destination.distance(state.getGXYPositon());
        int g = (state.get(ParserState.MOTION).getIntValue() != 0) ? 1 : 0;

        // Go UP ?
        if (Double.isNaN(state.getZ()) || Double.isNaN(d) || (d>0.0001)) 
                sendCmd("G0Z"+GWord.GCODE_NUMBER_FORMAT.format(moveAtZheight));
        // translate to destination
        if ( Double.isNaN(d) || (d>0.0001))
                sendCmd(new GCode(0,destination.getX(), destination.getY()).toGRBLString());

        // Z (engraving) translate to start level ?
        if ( Double.isNaN(zLevelDestination)) return;
        if( Double.isNaN(state.getZ()) || Math.abs(state.getZ() - zLevelDestination) > 0.00001)
                sendCmd("G1"+GWord.GCODE_NUMBER_FORMAT.format(zLevelDestination));
    }
    
    
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

    private Exception Error(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
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
