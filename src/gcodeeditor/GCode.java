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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simplified G-Code line implementation :
 * 
 * Possibles formats:
 * - ( comment1 )
 * - ; comment2
 * - Ann.nnn B Cnnn ...
 * 
 * Restrictions :
 * - only one 'G' per line (multiple G line like "G90 G81 ..." must be splitted)
 * - only absolutes moves (relatives moves 'A{+/-}nn.nn' must be converted)
 * 
 * @author Clément
 */
public class GCode extends Point2D implements Iterable<GWord> {
          
    /** List of Gword that compose the line. */
    ArrayList<GWord> words;  
    /** The current X (or null) */
    GWord x;
    /** The current Y (or null) */
    GWord y;
    
    /** Create an empty line. */
    public GCode() {
        words = new ArrayList<>();
        x = y = null;
    }
    /**
     * Create a copy of l.
     * @param l the line to copy. */
    public GCode( GCode l) {
        this();
        set(l);
    }
    
    /** 
     * Create a G-Code Line from a string (add G,X,Y words if not present, according to lastGState)
     * 
     * @param line the line to decode
     * @param defaultValues */
    public GCode( String line, GCode defaultValues) {
        words = new ArrayList<>();
        set(line);
        if ( ! isSet('G') && ((x!=null)||(y!=null)||isSet('Z')) && defaultValues.isSet('G'))
            words.add(0, new GWord('G', defaultValues.getG()));
        
        if ( isAMove() && ((x==null)^(y==null)))
        {
            assert(((x==null) || (y==null)) && (defaultValues != null));               
            
            if (! isSet('X')) setX( defaultValues.getX());
            if (! isSet('Y')) setY( defaultValues.getY());
        }
    }
    
    /** Create a G-Code Line from a string
     * @param line the line to decode */
    public GCode( String line) {
        words = new ArrayList<>();
        set(line);
    }

    
    public GCode(java.awt.geom.Point2D p) {
        this( p.getX(), p.getY());
    }
    
    /** 
     * Create an new G-Code <b>G1 (one)</b> with 2D position.
     * 
     * @param x
     * @param y
     */
    public GCode( double x, double y) {
        this(1, x ,y);
    }
    
    /** Create an new G-Code Gn 2D position.
     * @param gNumber
     * @param x
     * @param y */
    public GCode(int gNumber, double x, double y) {
        words = new ArrayList<>(3);
        words.add( new GWord('G', gNumber));
        words.add( this.x = new GWord('X', x));
        words.add( this.y = new GWord('Y', y));
    }
    
    public GCode(int gNumber, Point3D p) {
        words = new ArrayList<>(3);
        words.add( new GWord('G', gNumber));
        if ( ! java.lang.Double.isNaN(p.x)) words.add( this.x = new GWord('X', p.x));
        if ( ! java.lang.Double.isNaN(p.y)) words.add( this.y = new GWord('Y', p.y));
        if ( ! java.lang.Double.isNaN(p.z)) words.add( new GWord('Z', p.z));
    }

    @Override
    public int hashCode() { 
        int val = 7;
        for( GWord w : words) val += w.letter + (int)w.value;
        return val;
    }
    
    /**
     * Add or remplace GWord 'nw'
     * @param nw 
     */
    public void add( GWord nw) {        
        final GWord w = get(nw.letter);
        if ( w != null) w.set(nw);
        else {
            if ( nw.letter == 'X') x = nw;
            else if ( nw.letter == 'Y') y = nw;
            words.add(nw);
        }
    }
    
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public GCode clone() {
        GCode c = new GCode();
        words.forEach((w) -> {
            c.add((GWord) w.clone());
        });
        return c;
    }
    
    public boolean containsXorYCoordinate() {
        return (x != null) || (y!=null);
    }

    /**
     * Used by ArrayList.removeAll. We must compare pointer here.
     * @param obj
     * @return only true if this object <b> == </b> obj
     */
    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }
    
    
    /**
     * Return the GWord associate to this <i>letter</i>
     * @param letter the letter to find in the line
     * @return null if none in this line
     */
    public GWord get( char letter) {
        for( GWord w : words) if ( w.letter == letter) return w;
        return null;
    }
    
    /**
     * Warning: It return only integer value of G number. 
     * Use get('G') to get real value (for G38<b>.4</b> for example).
     * @return return G number, or -1 if no Gnn in this line.
     */
    public int getG() {
        for( GWord w : words) {
            if ( w.letter == 'G') return w.getIntValue();
        }
        return -1;
    }
    
    public double getValue( char letter) {
        for( GWord w : words) {
            if ( w.letter == letter) return w.value;
        }
        return java.lang.Double.NaN;
    }
    
    public boolean contains(char letter) {
        for( GWord w : words) {
            if ( w.letter == letter) return true;
        }
        return false;
    }
    
    /**
     * Return the double value of Xxxx word.
     * @return NaN if the is no Xxxx word in this GCode line
     */
    @Override
    public double getX() {
        if ( x == null) return java.lang.Double.NaN;
        return x.value;
    }
   
    
    /**
     * Return the value of Mmmm word.
     * @return an integer or -1 if the is no Mmmm word in this line.
     */
    public int getM() {
        for( GWord w : words) {
            if ( w.letter == 'M') return w.getIntValue();
        }
        return -1;
    }
     
    /**
     * Returns the word at the specified position in this line.
     * @return
     */
    public GWord get(int i) {
        return words.get(i);
    }
    
    /**
     * Return the double value of Yxxx word.
     * @return NaN if the is no Yxxx word in this GCode line
     */
    @Override
    public double getY() {
        if ( y == null) return java.lang.Double.NaN;
        return y.value;
    }
    
    /**
     * Return the GWord position that has letter <i>letter</i>.
     * @param letter
     * @return -1 if letter not found
     */
    public int indexOf( char letter) {
        int i = 0;
        for( GWord w : words) 
            if ( w.letter == letter) return i; else i++;
        return -1;
    }
    
    /**
     * @return true if this line contains Xxxx and Yyyy words
     */
    public boolean isAPoint() {
        return (x!=null) && (y!=null);
    }
    
    /**
     * @return true if this GCODE is a G4 code
     */
    public boolean isAPause() {
        return getG() == 4; 
    }
    
    /**
     * @return true if G2 or G3 GCode
     */
    public boolean isAnArc() {
        return (getG() == 2) || (getG()==3);
    }
    
   
    /**
     * Return true if this line is a GO,1,2,3,5 move (with or without X,Y,Z).
     * @return true if G is O,1,2,3,5
     */
    public final boolean isAMove() {
        final int g = getG();
        return (g>=0) && (g<6) && (g!=4);
    }

    /**
     * @return  true if this line start with a %
     */
    public boolean isPercent() {
        return words.get(0).isPercent();
    }
    
    /**
     * @return true if this line contains no word.
     */
    public boolean isEmpty() {
        return words.isEmpty();
    }
    /**
     * @param point
     * @return true is pos <b>X and Y</b>  are "same" (near 10e-7) with <i>point</i> (<b>warning:</b> it doesn't compare others words like Z). 
     *         the result unsure if not a point (without X and Y) !
     */
    public boolean isAtSamePosition(Point2D point) {
        if ( point == null) return false;
        
        final double px = point.getX();
        final double py = point.getY();
        return (java.lang.Double.isNaN(px) || (Math.abs(getX() - px) < 0.00000001)) &&
               (java.lang.Double.isNaN(py) || (Math.abs(getY() - py) < 0.00000001));      
    }
    
    public boolean isComment() {
        return (words.size() == 1) && (words.get(0).isComment());
    }

    /**
     * @return true if the is a Gnnn; ant nnn is in [80:84] interval.
     */
    public boolean isADrill() {
        return (getG()>80) && (getG() < 84);
    }

    /**
     * @return true if there is a G5 word in this line.
     */
    public boolean isASpline() {
        return (getG()==5);
    }
    
    /**
     * @return true if G in {1,2,3,5,8x}
     */
    public boolean isAGFeedWork() {
        final int g = getG();
        return (g>0) && ((g<4)||(g==5)||((g>80)&&(g<84)));
    }

    /**
     * @param groupOfPoints
     * @return true if one element of disscareIt is at smae posiion ((x==x) && (y==y)) of this point
     */
    public boolean isIn(ArrayList<GCode> groupOfPoints) {
        if ( groupOfPoints == null) return false;
        for ( GCode l : groupOfPoints)
            if ( (l.getX() == getX()) && (l.getY() == getY())) return true;
        return false;
    }

    /**
     * Parse all word of this GCode line.
     * @return 
     */
    @Override
    public Iterator<GWord> iterator() {
        return words.iterator();
    }
   
    
    /**
     * Return a G0 <i>Xnn Ynn</i> around center, calculated by angle.
     * @param center
     * @param radius
     * @param degre  in degre ( -360 to 360 )
     * @param rounded round coordinate <i>lower than at 10e-9</i>
     * @return 
     */
    public static GCode newAngularPoint(Point2D center, double radius, double degre, boolean rounded) {
        double rad = Math.toRadians(degre % 360);
        double dx, dy;
        if( rounded) {                                                      
            dx = ((double)Math.round(Math.cos(rad) * radius * 10e9))/10e9;
            dy = ((double)Math.round(Math.sin(rad) * radius * 10e9))/10e9;
        } else {
            dx = Math.cos(rad) * radius;
            dy = Math.sin(rad) * radius;
        }
        return new GCode( 0, center.getX() + dx, center.getY() + dy);
    }
    
    public void setG( int gValue) {
        GWord g = get('G');
        if ( g != null) g.value = gValue;
        else words.add(0, new GWord('G', gValue));
    }
    
    public final void setX( double value) {
        if ( java.lang.Double.isNaN(value)) remove('X');
        else {
            if ( x != null) x.value = value;
            else words.add(indexOf('G') + 1, x = new GWord('X', value));
        }
        
    }
    
    public final void setY( double value) {
        if ( java.lang.Double.isNaN(value)) remove('Y');
        else {
            if ( y != null) y.value = value;
            else {
                int i = indexOf('X');
                words.add((i==-1)? indexOf('G') + 1 : (i + 1), y = new GWord('Y', value));
            }
        }
    }
    
    /** Set this object with a new GCode line. (all old words are cleared)
     * @param gcode a g-code line */
    public void set( String gcode) {
        clear();
        while( ! gcode.isBlank()) {
            GWord word = new GWord();
            gcode = word.grabGWord( gcode);
            //GWord word = GWord.grabGWord( gcode);
            switch( word.letter) {
                case '\n':
                case GWord.UNDEF:
                    break;
                default:
                    add(word);
            }
        }
    }
        
    public void set(char c, double value) {
        add( new GWord(c, value));
    }
    
    /**
     * Set Xxxx and Yyyy words.
     * @param x0
     * @param y0 
     */
    @Override
    public void setLocation(double x0, double y0) {
        setX(x0);
        setY(y0);
    }
   
    
    /** Return the GCODE line optimised for GRBL transfer.
     * @return an optimized string (without text, space and extra digits) to send to GRBL. */
    public String toGRBLString() {
        String res = null;
        for( GWord w : words) {
            if ( w.isComment()) continue;
            if ( res==null) res = w.toGRBLString();
            else res += w.toGRBLString();
        }
        return (res==null)?"":res;
    }
    
    /**
     * Used to convert this line to the string equivalent string.
     * @return 
     */
    @Override
    public String toString() {
        String res = null;
        for( GWord w : words) {
            if ( res==null) res = w.toString();
            else res += " " + w.toString();
        }
        return (res==null)?"":res;
    }
    
    
    public String toSVGPoint( Rectangle2D origin) {
        return "" + (getX()-origin.getX()) + "," + (origin.getY()+origin.getHeight()-getY());
    }
    
    /**
     * Remove the word <i>w</i> from this line.
     * @param w the word to remove
     * @return true if <i>w</i> was removed
     */
    public boolean remove(GWord w) {
        return words.remove(w);
    }

    /**
     * @return the number of words <i>Xnn</i> in the line.
     */
    public int size() {
        return words.size();
    }

    /**
     * Move the XY coords according delta(X,Y)
     * @param delta 
     */
    public void translate(java.awt.geom.Point2D delta) {
        x.value += delta.getX();
        y.value += delta.getY();
    }
    
    public void transform(AffineTransform t) {
        if ( isAPoint()) {            
            Point2D r = t.transform(new Point2D.Double(getX(), getY()), null);
            setX( r.getX());
            setY( r.getY());
        }
    }

    public void remove(char wordLetter) {
        if ( wordLetter == 'X') x = null;
        if ( wordLetter == 'Y') y = null;
        for( GWord w : words) {
            if ( w.letter == wordLetter) {
                words.remove(w);
                return;
            }
        }
    }

    public void translate(double dx, double dy) {
        if (x == null) setX(dx);
        else x.value += dx;
        if (y==null) setY(dy);
        else y.value += dy;
    }
    
    /** 
     * Rotate this point around origin
     * @param origin
     * @param radianAngle (in radian)
     */
    public void rotate(java.awt.geom.Point2D origin, double radianAngle) {
        final double d = distance(origin);
        final double a = getAngle(origin, this);
        x.value = (origin.getX() + Math.cos(a+radianAngle)*d);
        y.value = (origin.getY() + Math.sin(a+radianAngle)*d);
    }
    
    /**
     * @param target
     * @param origin
     * @return angle in radian
     */
    public static double getAngle(Point2D origin, Point2D target) {
        double angle = Math.atan2(target.getY() - origin.getY(), target.getX() - origin.getX());
        return angle;
    }
    
    /**
     * @param target
     * @param origin
     * @return angle in degre into [0:360]°
     */
    public static double getAngleInDegre(Point2D origin, Point2D target) {
        double angle = Math.atan2(target.getY() - origin.getY(), target.getX() - origin.getX());
        return (360+(360*angle)/(2*Math.PI))%360;
    }
    
    /** scale the distance between this point and origin
     * @param origin
     * @param ratioX
     * @param ratioY */
    public void scale(Point2D origin, double ratioX, double ratioY) {
        final double d = distance(origin);
        final double a = getAngle(origin, this);
        x.value = (origin.getX() + Math.cos(a)*d*ratioX);
        y.value = (origin.getY() + Math.sin(a)*d*ratioY);
    }

    /**
     * Return (I,J) or throw an Error !!
     */
    public GCode getArcCenter(GCode startPoint) {
        if ( ! isAnArc()) 
            throw new Error("GCLine.getArcCenter() : "+this.toString()+" is not an arc");
        
        final GCode res = (GCode) startPoint.clone();
        res.translate(java.lang.Double.isNaN(getValue('I')) ? 0 : getValue('I'), 
                 java.lang.Double.isNaN(getValue('J')) ? 0 : getValue('J'));
        return res;
    }
    
    
    /**
     * Used to keep Gn state and and (X,Y) coordinates
     * @param newState the new state with eventualy G,X,Y words defined
     */
    public void updateGXYWith(GCode newState) {
        if ( newState == null) return;
        // TODO use ParserContext to handle relative moves, etc...
        if ( ! java.lang.Double.isNaN(newState.getX())) setX(newState.getX());
        if ( ! java.lang.Double.isNaN(newState.getY())) setY(newState.getY());
        if ( newState.isSet('G')) setG(newState.getG());
    }

    /**
     * Return the getHalfPointTo point between this point and <i>pt</i>
     * @param pt
     * @return 
     */
    public GCode getHalfPointTo(GCode pt) {
        return new GCode((getX()+pt.getX())/2.,(getY()+pt.getY())/2.);
    }
    
    public static GCode getMiddlePoint(Point2D p1, Point2D p2) {
        return new GCode((p1.getX()+p2.getX())/2.,(p1.getY()+p2.getY())/2.0);
    }
    
    public static GCode getOppositPointFrom(Point2D center, Point2D point) {
        if (point == null) return null;
        return new GCode(center.getX()*2-point.getX(), center.getY()*2 - point.getY());
    }

    /**
     * Change or add G, X, and Y words
     * @param g
     * @param x
     * @param y 
     */
    public void set(int g, double x, double y) {
        setG(g);
        setX(x);
        setY(y);
    }

    public void set(GCode gcode) {
        clear();
        for( GWord w : gcode) add( w.clone());
    }

    /** Remove all GWords of this line. */
    public void clear() {
        words.clear();
        x = y = null;
    }

    /**
     * Return the mirror of pt according to this object as origin.
     * @param pt the point to calculate the mirror
     * @return 
     */
    public GCode getMirrorPoint(GCode pt) {
        return new GCode( getG(), pt.getX() - getX() * 2, pt.getY() - getY() * 2);
    }

    /**
     * Verify tha a Word exist and is defined with 'non nul' value. 
     * @param letter the word to search
     * @return true if GCode 'letter' exist and is not NaN, or if it is a comment.
     */
    public boolean isSet(char letter) {
        GWord w = get(letter); 
        
        if ( w != null) {
            if ( w.isComment()) return w.text != null;
            switch ( letter) {
                case 'M':
                case 'G':
                    return w.getIntValue() != -1;
                default:
                    return ! java.lang.Double.isNaN(w.value);
            }
        }
        return false;        
    }

}
