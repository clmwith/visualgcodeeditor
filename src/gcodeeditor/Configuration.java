/*
 * Copyright (C) 2019 Clément Gérardin @ Marseille.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gcodeeditor;

import gcodeeditor.gui.JProjectEditorPanel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class used to store and restore several permanent application and GRBL machine settings.
 * @author Clément
 */
public class Configuration  {

    public static final String DEFAULT = "<default>";
    private static final String SAVE_HEADER = "savedø";
    private static final String SAVE_VALUE = "øvalues";
    public static final String RECENT_FILE_SEPARATOR = "µ";
    
    public static final int DEFAULT_SHOW_LASER_POWER = 8;
    
    public String configurationFileName = null;
    public int workspaceWidth, workspaceHeight;
    public int workspaceOrigin;
    public double feedRate;
    public int spindleLaserPower;
    public int axeAIs; // axe Z
    public double pulseByUnit;
    public double objectDiameter;
    public double objectLength;
    public String GCODEHeader, GCODEFooter;
    public double unitForATurn;
    public String recentFiles;
    public double engravingHeight, safeZHeightForMoving;
    public boolean adaptativePower, useBackLash;
    public double backLashX, backLashY, backLashZ;
    public int jogSpeed;
    public double toolDiameter;
    public String editorSettings;
    public int showLaserPowerValue;
    public double minG1move;
    public String CNCnotes;
    public String guiTheme;

    public Configuration() {  
        getDefault();
    }
    
    public final void getDefault() {
        Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
        editorSettings = prefs.get("guiSettings", "");
        workspaceWidth = prefs.getInt("WorkspaceWidth", 0);
        workspaceHeight = prefs.getInt("WorkspaceHeight", 0);
        workspaceOrigin = prefs.getInt("workspaceOrigin", 0);
        feedRate = prefs.getDouble("FeedRate", 100);
        spindleLaserPower = prefs.getInt("SpindleLaserPower", 2);
        axeAIs = prefs.getInt("axeAIs", 2); // axe Z
        pulseByUnit = prefs.getDouble("pulseByUnit", 250);
        objectDiameter = prefs.getDouble("objectDiameter", 10);
        unitForATurn = prefs.getDouble("unitForATurn", 10);
        objectLength = prefs.getDouble("objectLength", 100);
        GCODEHeader  = prefs.get("GCODEHeader", "");
        GCODEFooter  = prefs.get("GCODEFooter", "");
        recentFiles  = prefs.get("recentFiles", "");
        engravingHeight = prefs.getDouble("engravingHeight", 0);
        jogSpeed = prefs.getInt("jogSpeed", 1000);
        safeZHeightForMoving = prefs.getDouble("safeZHeightForMoving", 10);
        adaptativePower = prefs.getBoolean("adaptativePower", false);
        useBackLash = prefs.getBoolean("useBackLash", false);
        backLashX = prefs.getDouble("backLashX", 0.2);
        backLashY = prefs.getDouble("backLashY", 0.2);
        backLashZ = prefs.getDouble("backLashZ", 0.2); 
        toolDiameter = prefs.getDouble("toolDiameter", 3);
        showLaserPowerValue = prefs.getInt("showLaserPowerValue", DEFAULT_SHOW_LASER_POWER);             
        minG1move = prefs.getDouble("minG1move", 1);
        guiTheme  = prefs.get("guiTheme", "");
        CNCnotes = prefs.get("CNCnote", "");
    }
    
    public ArrayList<String> getSavedNames() {
        Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
        String[] k = {};
        try {
            k = prefs.keys();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<String> res = new ArrayList<>();
        for ( String s : k)
            if ( s.startsWith(SAVE_HEADER) && (s.endsWith(SAVE_VALUE)))
                res.add(s.split("ø")[1]);        
        return res;
    }
    
    public boolean exist(String nodeName) {        
        Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
        String vals = prefs.get(SAVE_HEADER+nodeName+SAVE_VALUE, null);
        return ( vals != null);
    }
    
    public boolean restore( String nodeName) {
        Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
        String vals = prefs.get(SAVE_HEADER+nodeName+SAVE_VALUE, null);
        if ( vals != null) {
            GCODEHeader  = prefs.get(SAVE_HEADER+nodeName+"øheader", "");
            GCODEFooter  = prefs.get(SAVE_HEADER+nodeName+"øfooter", "");  
            CNCnotes = prefs.get(SAVE_HEADER+nodeName+"ønotes", "");  
            String v[] = vals.split(",");
            try  {
            workspaceWidth = Integer.parseInt(v[0]);
            workspaceHeight = Integer.parseInt(v[1]);
            workspaceOrigin = Integer.parseInt(v[2]);
            feedRate        = Double.parseDouble(v[3]);
            spindleLaserPower = Integer.parseInt(v[4]);
            axeAIs          = Integer.parseInt(v[5]);
            pulseByUnit     = Double.parseDouble(v[6]);
            objectDiameter = Double.parseDouble(v[7]);
            unitForATurn = Double.parseDouble(v[8]);
            objectLength = Double.parseDouble(v[9]);
            engravingHeight = Double.parseDouble(v[10]);
            jogSpeed = Integer.parseInt(v[11]);
            safeZHeightForMoving = Double.parseDouble(v[12]);
            adaptativePower = v[13].equals("true");
            useBackLash = v[14].equals("true");
            backLashX = Double.parseDouble(v[15]);
            backLashY = Double.parseDouble(v[16]);
            backLashZ = Double.parseDouble(v[17]);
            toolDiameter = Double.parseDouble(v[18]);                  
            showLaserPowerValue = Integer.parseInt(v[19]);
            minG1move = Double.parseDouble(v[20]);
            guiTheme  = v[21];
            } catch ( IndexOutOfBoundsException e) {
                
            }
        }
        return vals != null;
    }
    
    public void save( String nodeName) {
        if ( nodeName.equals(DEFAULT)) saveDefault();
        else try {
            Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
            prefs.put(SAVE_HEADER+nodeName+SAVE_VALUE, "" +
            workspaceWidth + "," +
            workspaceHeight + "," + 
            workspaceOrigin + "," +
            feedRate + "," + 
            spindleLaserPower + "," + 
            axeAIs + "," +
            pulseByUnit + "," +
            objectDiameter + "," +
            unitForATurn + "," +
            objectLength + "," +
            engravingHeight + "," +
            jogSpeed + "," +
            safeZHeightForMoving + "," +
            adaptativePower + "," +
            useBackLash + "," +
            backLashX+ "," +
            backLashY + "," +
            backLashZ + "," +
            toolDiameter+ "," +
            showLaserPowerValue+ "," +
            minG1move);
            prefs.put(SAVE_HEADER+nodeName+"øheader", GCODEHeader);
            prefs.put(SAVE_HEADER+nodeName+"øfooter", GCODEFooter);
            prefs.put(SAVE_HEADER+nodeName+"ønotes", CNCnotes);
            prefs.sync();
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JProjectEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveVisualSettings() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
            prefs.put("guiSettings", editorSettings);
            prefs.sync();
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JProjectEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveDefault() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
            prefs.put("GCODEHeader", GCODEHeader);
            prefs.put("GCODEFooter", GCODEFooter);            
            prefs.putInt("WorkspaceWidth", workspaceWidth);
            prefs.putInt("WorkspaceHeight", workspaceHeight);
            prefs.putInt("workspaceOrigin", workspaceOrigin);
            prefs.putDouble("FeedRate", feedRate);
            prefs.putInt("SpindleLaserPower", spindleLaserPower);
            prefs.putInt("axeAIs", axeAIs); // axe Z
            prefs.putDouble("pulseByUnit", pulseByUnit);
            prefs.putDouble("objectDiameter", objectDiameter);
            prefs.putDouble("objectLength", objectLength);
            prefs.putDouble("unitForATurn", unitForATurn);
            prefs.putDouble("engravingHeight", engravingHeight);
            prefs.putDouble("safeZHeightForMoving", safeZHeightForMoving);
            prefs.putInt("jogSpeed", jogSpeed);
            prefs.putBoolean("adaptativePower", adaptativePower);
            prefs.putBoolean("useBackLash", useBackLash);
            prefs.putDouble("backLashX", backLashX);
            prefs.putDouble("backLashY", backLashY);
            prefs.putDouble("backLashZ", backLashZ);
            prefs.put("recentFiles", recentFiles);
            prefs.put("guiSettings", editorSettings);
            prefs.putDouble("toolDiameter", toolDiameter);
            prefs.putInt( "showLaserPowerValue", showLaserPowerValue);
            prefs.putDouble("minG1move", minG1move);
            prefs.put("guiTheme", guiTheme);
            prefs.put("CNCnotes", CNCnotes);
            prefs.sync();
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JProjectEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(String selected) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
            prefs.remove(SAVE_HEADER+selected+SAVE_VALUE);
            prefs.remove(SAVE_HEADER+selected+"øheader");
            prefs.remove(SAVE_HEADER+selected+"øfooter");
            prefs.sync();
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JProjectEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveConfiguration() throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(configurationFileName,false));
        writer.append("# A Simple GCODE VisualEditor configuration File.\n");
        writer.append("# Don't modify values order !\n");
        writer.append("#\n");
        writer.append("#Workspace width (integer)\n");
        writer.append(Integer.toString(workspaceWidth) + "\n");
        writer.append("#Workspace height (integer)\n");
        writer.append(Integer.toString(workspaceHeight) + "\n");
        writer.append("#Feed rate (integer)\n");
        writer.append(Double.toString(feedRate) + "\n");
        writer.append("#Spindle Laser/Power (integer)\n");
        writer.append(Integer.toString(spindleLaserPower) + "\n");
        writer.append("#Workspace origin (0=upLeft,1=upRight,2=downLeft,3=downRight)\n");
        writer.append(Integer.toString(workspaceOrigin) + "\n");
        writer.append("#Axe A is (0=X, 1=Y, 2=Z, 3=A)\n");
        writer.append(Integer.toString(axeAIs) + "\n");
        writer.append("#Pulse by Unit (float) (Unit in mm or inch, whatever)\n");
        writer.append(Double.toString(pulseByUnit) + "\n");
        writer.append("#Objet to work radius (float)\n");
        writer.append(Double.toString(objectDiameter) + "\n");
        writer.append("#Objet to work length (float)\n");
        writer.append(Double.toString(objectLength) + "\n");
        writer.append("#nb A-axis unit to turn object one time (float)\n");
        writer.append(Double.toString(unitForATurn) + "\n");
        writer.append("#Engravind height (float)\n");
        writer.append(Double.toString(engravingHeight) + "\n");
        writer.append("#Safe moving height (float)\n");
        writer.append(Double.toString(safeZHeightForMoving) + "\n");
        writer.append("#Adaptative laser power (boolean = 0|1)\n");
        writer.append(""+ (adaptativePower?1:0) + "\n");
        writer.append("# JOG speed (float)\n");
        writer.append(Integer.toString(jogSpeed) + "\n");
        writer.append("# X BackLash (float)\n");
        writer.append(Double.toString(backLashX) + "\n");
        writer.append("# Y BackLash (float)\n");
        writer.append(Double.toString(backLashY) + "\n");
        writer.append("# Z BackLash (float)\n");
        writer.append(Double.toString(backLashZ) + "\n");
        writer.append("# Tool diameter (float)\n");
        writer.append(Double.toString(toolDiameter) + "\n");
        writer.append("# ShowLaser power value (int)\n");
        writer.append(Double.toString(showLaserPowerValue) + "\n");
        writer.append("# Minimal G1 move (int)\n");
        writer.append(Double.toString(minG1move) + "\n");
        writer.append("#GCODE Header\n");
        writer.append(GCODEHeader);
        writer.append("\n> (don't touch this line)\n#GCODE Footer\n");
        writer.append(GCODEFooter);
        writer.append("\n");
        writer.close();
    }
    
    public void load( String confFile) throws FileNotFoundException, IOException {
        if ( confFile == null) return;
        
        BufferedReader br = new BufferedReader(new FileReader(new File(confFile)));
        String l, buf="";
        int line=0;
        boolean readFooter = false;
        while((l = br.readLine()) != null)
        {
            if ( ! l.startsWith("#")) {
                switch ( line) {
                    case 0: workspaceWidth = Integer.parseInt(l);                 
                        break;
                    case 1: workspaceHeight = Integer.parseInt(l);                               
                        break;
                    case 2: feedRate = Double.parseDouble(l);                              
                        break;
                    case 3: spindleLaserPower = Integer.parseInt(l);
                        break;
                    case 4: workspaceOrigin = Integer.parseInt(l);
                        break;
                    case 5: axeAIs = Integer.parseInt(l);
                        break;
                    case 6: pulseByUnit = Double.parseDouble(l);
                        break;
                    case 7: objectDiameter = Double.parseDouble(l); 
                        break;
                    case 8: objectLength = Double.parseDouble(l);
                        break;
                    case 9: unitForATurn = Double.parseDouble(l);
                        break;
                    case 10: engravingHeight = Double.parseDouble(l);
                        break;
                    case 11: safeZHeightForMoving = Double.parseDouble(l);
                        break;
                    case 12: adaptativePower = (Integer.parseInt(l) == 1);
                        break;
                    case 13: jogSpeed = Integer.parseInt(l);
                        break;
                    case 14: backLashX = Double.parseDouble(l);
                        break;
                    case 15: backLashY = Double.parseDouble(l);
                        break;
                    case 16: backLashZ = Double.parseDouble(l);
                        break;
                    case 17: toolDiameter = Double.parseDouble(l);
                        break;
                    case 18: showLaserPowerValue = Integer.parseInt(l);
                        break;
                    case 19: minG1move = Double.parseDouble(l);
                        break;
                    default: // load GCODE                              
                       if ( l.startsWith(">")) { 
                           GCODEHeader=buf;
                           readFooter=true;  
                           buf = ""; 
                           break;
                       }
                       buf += l + "\n";
                       break;
                }
                line++;
            }
        }
        GCODEFooter=buf;
        br.close();
        configurationFileName = confFile;
    }

    public void removeRecentFile(String fileName) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Configuration.class);
            String recents[] = recentFiles.split(RECENT_FILE_SEPARATOR);
            String newRecent = null;
            for( String s : recents)
                if ( ! s.equals(fileName))
                    if ( newRecent==null) newRecent = s;
                    else newRecent += RECENT_FILE_SEPARATOR + s;
            
            if ( newRecent == null) newRecent = "";
            prefs.put("recentFiles", recentFiles = newRecent);
            prefs.put("guiSettings", editorSettings);
            prefs.sync();
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
