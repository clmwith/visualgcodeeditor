/*
 * Copyright (C) 2019 Clément GERARDIN @ Marseille.fr
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * A word [char][numeric value] that compose a line of G-Code
 * @author Clément
 */
public class GWord {
    public static final char UNDEF = (char)255;
    /** Format used to send numerical values to GRBL. */
    public static final DecimalFormat GCODE_NUMBER_FORMAT;
    static {
        GCODE_NUMBER_FORMAT = (DecimalFormat)DecimalFormat.getInstance(Locale.ROOT);
        GCODE_NUMBER_FORMAT.applyLocalizedPattern("#########.####");
        GCODE_NUMBER_FORMAT.setRoundingMode(RoundingMode.HALF_DOWN);
    }
    public static final DecimalFormat NOEXP_NUMBER_FORMAT;
    static {
        NOEXP_NUMBER_FORMAT = (DecimalFormat)DecimalFormat.getInstance(Locale.ROOT);
        NOEXP_NUMBER_FORMAT.applyLocalizedPattern("########0.####");
        NOEXP_NUMBER_FORMAT.setRoundingMode(RoundingMode.HALF_DOWN);
    }
    
    /** can be ';' or '(' for text, or any other normal uppercase G-Code cmd letter */
    char letter = UNDEF;
    double value = Double.NaN;
    String text = null;

    public GWord() { }
    
    public GWord(char c, double val) {
        letter = c;
        value = val;
    }

    /**
     * Creat a new GWord
     * @param s must start with a letter.
     */
    public GWord(String s) {
        extractGWord(s);
    }
    
    @Override
    public GWord clone() {
        GWord clone = new GWord(letter, value);
        if ( text != null) clone.text = "" + text;
        return clone;
    }
    
    public final String extractGWord(String line) {
        boolean readLetter = true;
        if ( line.length() > 0) do {
            switch ( line.charAt(0)) {
                case '$':
                case ';': 
                    letter=line.charAt(0);
                    text = line.substring(1);
                    line="";
                    break;
                case '(': 
                    letter ='(';
                    if ( line.length()>1)
                        text = line.substring(1, (line.indexOf(')')!=-1)?line.indexOf(')'):line.length()-1);
                    else 
                        text="";
                   
                    line="";
                    break;
                case '%': 
                    letter='%'; // must be alone in the line
                    line="";
                    break;
                case ' ':
                case '\t':
                case '\r': line = line.substring(1); letter=UNDEF; break;
                default:
                    if ( readLetter) {
                        letter = Character.toUpperCase(line.charAt(0));
                        line = line.substring(1);
                        readLetter = false;
                    }
                    else { // read value
                        int i;
                        for( i = 0; (i < line.length()) && ((line.charAt(i)=='-')||(line.charAt(i)=='.')||(Character.isDigit(line.charAt(i)))); ) i++;
                        try {
                            value = Double.parseDouble(line.substring(0,i));
                        } catch (Exception e) {
                            value = Double.NEGATIVE_INFINITY; 
                            readLetter=true;
                            //letter=UNDEF; 
                        }
                        line = line.substring(i);
                        return (line.length()>0)?line:null;
                    }        
            }
        } while ( line.length() > 0);
        if ( ! readLetter) letter = UNDEF; // we have read a letter without any value
        return null;
    }
    
    public boolean isComment() {
        return (letter == '(') || (letter == ';') || (letter == '%');
    }
    
    public boolean isPercent() {
        return letter == '%';
    }
    
    public boolean isMotion() {
        return (letter == 'G') && (value>=0) && (value<=5);
    }
    public char getLetter() {
        return letter;
    }
    
    @Override
    public String toString()
    {
        switch ( letter) {
            case '$': return "$" + text;
            case ';': return ";" + text;
            case '(': return "(" + text + ")";
            case '%': return "%\n";
            default:
                if ( Double.isNaN(value)) return "" + letter;
                else if ( value==Double.POSITIVE_INFINITY) return "" + letter;
                else if ( value==Double.NEGATIVE_INFINITY) return "" + letter;
                else return "" + letter + (isIntValue(value)?Integer.toString((int)value):NOEXP_NUMBER_FORMAT.format(round(value)));
        }
    }
    
    /**
     * Return an optimisez GRBL GWord.
     * @return empty string if text or minimal GWord value.
     */
    public String toGRBLString() {
        switch ( letter) {
            case '$': return "$" + text;
            case ';': return "";
            case '(': return "";
            case '%': return "";
            default:
                if ( Double.isNaN(value)) return "" + letter;
                else if ( value==Double.POSITIVE_INFINITY) return "" + letter;
                else if ( value==Double.NEGATIVE_INFINITY) return "" + letter;
                else {
                    // round value to avoid 10En output
                    double d = Math.pow(10, GCODE_NUMBER_FORMAT.getMaximumFractionDigits());
                    double v = (Math.round(round(value) * d))/d;
                    return "" + letter + 
                        (isIntValue(value)?Integer.toString((int)round(value)):
                            GCODE_NUMBER_FORMAT.format(v));
                }               
        }
    }
    
    /**
     * @param val
     * @return rounded value to 10e-9
     */
    public static double round( double val) {
        return ((double)Math.round( val * 1000000000))/1000000000;
    }
    
    
    public static String roundForGCODE( double val) {
        // round value to avoid 10En output
        double d = Math.pow(10, GCODE_NUMBER_FORMAT.getMaximumFractionDigits());
        double v = (Math.round(val * d))/d;
        return GCODE_NUMBER_FORMAT.format(v);
    }
    
    public int getIntValue() {
        return Double.isNaN(value)?-1:(int)value;
    }
    
    public double getValue() {
        return value;
    }
    
    boolean isIntValue( double d) {
        return Math.abs(d-(int)d) < 0.00000001;
    }
}