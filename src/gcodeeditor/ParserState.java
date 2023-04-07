package gcodeeditor;

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


import gcodeeditor.GWord;
import gcodeeditor.GCode;

/**
 *  Not fully implemented !!
 * 
 *  GRBL 1.1 Parser State.<br>
 *  Possibles values :
 *  Motion Mode - (G0), G1, G2, G3, G38.2, G38.3, G38.4, G38.5, G80
 *  Coordinate System Select - (G54), G55, G56, G57, G58, G59
 *  Plane Select - (G17 =XY), G18, G19
 *  Units Mode : G20, (G21)
 *  Distance Mode - (G90 =absolute), G91
 *  Arc IJK Distance Mode - (G91.1 =IJ relative) not returned
 *  Feed Rate Mode : G93, (G94)
 *  Cutter Radius Compensation : (G40) not returned
 *  Tool Length Offset : G43.1, (G49)
 *  Program Mode : (M0), M1, M2, M30 not returned
 *  Spindle State : M3, M4, (M5)
 *  Coolant State : M7, M8, (M9)
 * 
 * Supported Non-Modal Commands (because they only affect the current line they are commanded in) :
 * G4, G10 L2, G10 L20, G28, G30, G28.1, G30.1, G53, G92, G92.1
 * 
 * @author Clément
 */
public class ParserState {
    
    /** The type is unknow. */
    public static final int UNKNOW = -1;
    /** Use it with get() to know current G{0,1,2,3} value. */
    public static final int MOTION = 0;
    /** Use it with get() to know current G5n value. */
    public static final int COORDINATE = 1;
    /** Use it with get() to know current G1{7,8,9} value. */
    public static final int PLANE = 2;
    /** Use it with get() to know current G2{0,1} value. */
    public static final int UNIT = 3;
    /** Use it with get() to know current G9{0,1} value. */
    public static final int DISTANCE = 4;
    /** Use it with get() to know current G9{3,4} value. */
    public static final int FEED = 5;
    /** Use it with get() to know current G43.1 or G49 value. */
    public static final int TOOL_LENGTH = 6;
    //** Use it with get() to know current M{0,1,2,30} value. */
    //public static final int PROGRAM = 7;
    /** Use it with get() to know current M{3,4,5} value. */
    public static final int SPINDLE = 8;
    /** Use it with get() to know current M{7,8,9} value. */
    public static final int COOLANT = 9;
    /** Use it with get() to know current tool number. */
    public static final int TOOL_NUMBER = 10;
    /** Use it with get() to know current feed speed value. */
    public static final int FEED_SPEED = 11;
    /** Use it with get() to know current spindle/power value. */
    public static final int SPINDLE_SPEED = 12;
    public static final int X = 13;
    public static final int Y = 14;
    public static final int Z = 15;
    public static final int E = 16;
    public static final int A = 17;
    
    public static final String DEFAULT_GRBL_PARSER_STATE[] = { 
        "G-1",   // 0  - Motion Mode - (G0), G1, G2, G3, G38.2, G38.3, G38.4, G38.5, G80
        "G54",  // 1  - Coordinate System Select - (G54), G55, G56, G57, G58, G59
        "G17",  // 2  - Plane Select - (G17 =XY), G18, G19
        "G21",  // 3  - Units Mode : G20, (G21)
        "G90",  // 4  - Distance Mode - (G90 =absolute), G91
        "G94",  // 5  - Feed Rate Mode : G93, (G94)
        "G49",  // 6  - Tool Length Offset : G43.1, (G49)
        "M0",   // 7  - Program Mode : (M0), M1, M2, M30 not returned
        "M5",   // 8  - Spindle State : M3, M4, (M5)
        "M9",   // 9  - Coolant State : M7, M8, (M9)
        "T0",   // 10  - Tool
        "F-1",   // 11 - Feed speed
        "S-1",   // 12 - Spindle speed
        "X", "Y", "Z", "E", "A"
    };
    
    GWord[] states;
    
    /**
     * Create a GRBL default parser states.
     */
    public ParserState() {
        states = new GWord[DEFAULT_GRBL_PARSER_STATE.length];
        for( int i = 0; i < DEFAULT_GRBL_PARSER_STATE.length; i++) {
            GWord w = new GWord(DEFAULT_GRBL_PARSER_STATE[i]);
            if ( w.getLetter() != GWord.UNDEF) states[i] = w;
        }
    }
    
    public ParserState clone()
    {
        ParserState clone = new ParserState();
        clone.states = new GWord[DEFAULT_GRBL_PARSER_STATE.length];
        for( int i = 0; i < DEFAULT_GRBL_PARSER_STATE.length; i++) {
            if ( states[i] != null) clone.states[i] = states[i].clone();
        }
        return clone;
    }
    
    /**
     * @param w
     * @return true if states has changed.
     */
    public boolean set( GWord w) {
        final int type = getValueType(w);
        if ( type == UNKNOW) return false;
        
        if ( states[type] == null ) states[type] = w.clone();
        else if ( states[type].value !=  w.value) {
            if ((type >= X) && (states[DISTANCE].value == 91)) // G91 relative coordinate ?
                    states[type].value += w.value;
            else
                states[type].value =  w.value;
            return true;
        } 
        return false;
    }
    
    private int getValueType( GWord w) {
        switch ( w.letter) {
            case 'X': return X;
            case 'Y': return Y;
            case 'Z': return Z;
            case 'A': return A;
            case 'E': return E;
            case 'T': return TOOL_NUMBER;
            case 'F': return FEED_SPEED;
            case 'S': return SPINDLE_SPEED;
            case 'M': 
                switch ( w.getIntValue()) {
                    case 3: case 4: case 5: return SPINDLE;
                    case 7: case 8: case 9: return COOLANT;
                }
                break;
            case 'G': 
                switch( w.getIntValue()) {
                    case 0: case 1: case 2: case 3: case 38: case 80:
                        return MOTION;
                    case 17: case 18: case 19:
                        return PLANE;
                    case 20: case 21:
                        return UNIT;
                    case 90: case 91: 
                        return DISTANCE; 
                }
        }
        return UNKNOW;
    }
    
    public boolean updateContextWith( GCode l) {
        if ( l.isComment()) return false;
        boolean changed = false;
        for( GWord w : l.words) changed |= set(w);
        return changed;
    }
    
    public GWord get(int type) {
        return states[type].clone();
    }

    public GCode getGXYPositon() {
        return new GCode(states[MOTION]+" "+states[X]+" "+states[Y]);
    }
    
    private double getValue(int type) {
        if ( states[type] == null) return Double.NaN;
        else return states[type].value;
    }
    
    public double getX() {
        return getValue(X);
    }
    public double getY() {
        return getValue(Y);
    }
    public double getZ() {
        return getValue(Z);
    }
    
    private String getStringValue(int type) {
        if ( states[type] == null) return "";
        else return states[type].toString();
    }
    
    @Override
    public String toString() {
        String res = "";
        for( int i = 0 ; i< DEFAULT_GRBL_PARSER_STATE.length; i++)
            res += ((i==0) ? "" : " ") + getStringValue(i);
        return res;
    }

    public double getFeed() {
        return states[FEED_SPEED].value;
    }
    
    public int getPower() {
        return states[SPINDLE_SPEED].getIntValue();
    }

    public boolean isEngraving() {
        return states[MOTION].getIntValue() > 0;
    }
    
    /** 
     * Remove unnecessary common GWord of this line according to this parser states.
     * @param line the GCode line parse.
     * @return a string optimised to send to GRBL.
     */
    public String getCleanForGRBL(GCode line) {
        
        String res = "";
        for( GWord w : line.words) {
            
            if ( w.isComment()) continue;
            if ( w.letter == '$') return w.toString();
            
            switch( w.letter) {
                case 'G': int val = w.getIntValue();
                          if ((val < 4) && (val == states[MOTION].getIntValue())) continue;
                          else 
                              return line.toGRBLString(); // don't remove any word in special Gnn commands
                          
                          
                case 'T': if ( w.getIntValue() == states[TOOL_NUMBER].getIntValue()) continue;
                          break;
                case 'S': if ( w.getIntValue() == states[SPINDLE_SPEED].getIntValue()) continue;
                          break;  
                case 'F': if (Math.abs(w.value - states[FEED_SPEED].value) < 0.0000001) continue;
                          break;
                case 'X': if ((states[X] != null) &&
                              (Math.abs(w.value - states[X].value) < 0.0000001)) 
                                continue;
                          break;
                case 'Y': if ((states[Y] != null) &&
                              (Math.abs(w.value - states[Y].value) < 0.0000001)) 
                                continue;
                          break;
                case 'Z': if ((states[Z] != null) &&
                              (Math.abs(w.value - states[Z].value) < 0.0000001)) 
                                continue;
                          break;
            }
            res += w.toGRBLString();
        }
        return res;
    }

    
    public void updateTLO(String newTLO) {
        states[TOOL_LENGTH].value = Double.valueOf(newTLO);
    }

}
