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
package gelements;

import gcodeeditor.GCode;
import gelements.GElement.GElementFactoryInterface;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * An element translatable to one or several lines of GCODE.
 * @author Clément
 */
public abstract class GElement implements ListModel<Object>, Iterable<GCode> {
        
    /** Used to create uniq element ID. */
    protected static int curID = 1;
     
    /** A uniq ID. */
    protected int id = -1;
    protected boolean modified = true;
    
    /** The name of the shape. */
    public String name;
    
    ArrayList<ListDataListener> dataListener = new ArrayList<>();
    
    public EngravingProperties properties = new EngravingProperties();;
    
    /**
     * Create a new GElement with a uniq ID.
     * @param name0 
     */
    public GElement( String name0) {
        id = curID++;
        name = name0;
    }
    
    static GCode getPolarPoint(double radius, double angle) {
        return new GCode(radius * Math.cos(angle), radius * Math.sin(angle));
    }

    public String getName() { return name; }
    
    public void setName(String text) { name = text; }
    
   /** Change my ID to a new uniq ID. */
    public void newID() {
        id = curID++;
    }
    
    /**
     * @return  a uniq ID */
    public static int getUniqID() {
        return curID++;
    }
    
    public int getID() {
        return id;
    }   
   
    /**
     * Used by the GCode editor (JList). Can return size()+1 to allow new gcode line to be appened at the end.
     * @return The number of G-Code lines this element contains.
     */
    @Override
    abstract public int getSize();
    
    /**
     * Used to list all lines in the editor.
     * @param index
     * @return a G-Code line
     */
    @Override
    abstract public Object getElementAt(int index);
    
    
    @Override
    public void addListDataListener(ListDataListener l) {
        dataListener.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        dataListener.remove(l);
    }
    
    /**
     * Warns all listeners that the content of this GElement has changed (for JListViewer)
     */
    protected void informAboutChange() {
        modified=true;
        dataListener.forEach((l) -> { 
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
        });
    } 

    /**
     * To parse all G-line of this element (not only points)
     * @return 
     */
    @Override
    abstract public Iterator<GCode> iterator(); 
    
    /**
     * To parse all points of this elements (editable/real or not)
     * @return 
     */
    abstract public Iterable<GCode> getPointsIterator();
    
    public GElement cloneWithSameID() {
        GElement e = clone();
        e.id = id;
        return e;
    }
    
    /**
     * @param center
     * @param p1
     * @param p2
     * @return  angle in degree
     */
    public double getAngle( Point2D center, Point2D p1, Point2D p2)
    {
        double a1, b1, a2, b2, a, b, t, cosinus ;
        a1 = p1.getX() - center.getX() ;
        a2 = p1.getY() - center.getY() ;
        b1 = p2.getX() - center.getX() ;
        b2 = p2.getY() - center.getY() ;
        a = Math.sqrt ( (a1*a1) + (a2*a2) );
        b = Math.sqrt ( (b1*b1) + (b2*b2) );

        if ((Math.abs(a) < 0.0000001) || (Math.abs(b) < 0.0000001)) return 0;
        cosinus = (a1*b1+a2*b2) / (a*b) ;
        t = Math.acos ( (a1*b1+a2*b2) / (a*b) );
        t = t * 180.0 / Math.PI;
        
        if ( Double.isNaN(t)) return 180;
        return t;
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
    
    public boolean hasCustomProperties(boolean withDisable) {
        return properties.isSet(withDisable);
    }
    
    /**
     * Read a double value in a string like "[toto = ]34.5".
     * @param value
     * @return the value or NaN
     */
    public double parseDoubleValue(GCode value) {
        final String s = value.toString();
        int i = s.indexOf('=')+1;
        if ( i > 0) 
            try {
                while ( s.charAt(i) == ' ') i++;
                return Double.parseDouble(s.substring(i));
            } catch ( Exception e) { }
        return Double.NaN;
    }

    public boolean isEnabled() {
        return properties.enabled;
    }
    
    public void translate(Point2D delta) {
        translate( delta.getX(), delta.getY());
    }
    
    public static void drawCoordRect(Graphics g, double zoomFactor, Rectangle2D bounds) {
        int sx = (int)(bounds.getX()*zoomFactor);
        int sy = (int)(bounds.getY()*zoomFactor);
        int ex = (int)(bounds.getWidth()*zoomFactor);
        int ey = (int)(bounds.getHeight()*zoomFactor);
        g.setColor(Color.CYAN);
        g.drawRect(sx, -sy-ey, ex, ey);
    }
    
    @Override
    abstract public GElement clone();
    
    abstract public boolean add(GCode coordSnapPointFor);
    
    abstract public void add(int pos, GCode line);

    abstract public Area getOffsetArea(double param);

    /**
     * Try to append this element to this object
     * @param element the object to append
     * @param tolerance the maximal distance between borderd of both objects to contac them
     * @return true if the element has been concatened into this object
     */
    abstract public boolean concat(GElement element, double tolerance);
    
    abstract public CharSequence toSVG(Rectangle2D origin);
 
    abstract public GCode getFirstPoint();
    
    /**
     * Return the last point of the path.
     * @return A true GCode with G in {1,2,3,8x}, or null if there is no points in this path.
     */
    abstract public GCode getLastPoint();
   
    abstract public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException;

    /**
     * Used to find a point of the shape (real or not)
     * @param from the point to compate to
     * @param dmax the maximal distance to show
     * @param discareIt the points to ignore in the search
     * @param excludeFirst exclude the first point of the shape
     * @return a point or null if none
     */
    abstract public GCode getCloserPoint(Point2D from, double dmax, ArrayList<GCode> discareIt, boolean excludeFirst);
    
    /** Return the patch len of this element or Double.NaN if not calculable. */
    abstract double getLength();
    
    /**
     * @return number of points (real or not)
     */
    abstract public int getNbPoints();
    
    /**
     * Return true if there is a point at same position as <i>point</i>.
     * @param point the point to compare to
     * @return 
     */
    abstract public boolean contains( GCode point);
    
    /**
     * @return the bounds of the shape or null if none.
     */
    abstract public Rectangle2D getBounds();

    /** Return the len of the segment that finish at this point.
     * @param point
     * @return  a positive double value or NaN if not evaluable */
    abstract public double getLenOfSegmentTo(GCode point);
    
    abstract public int getIndexOfPoint(GCode highlitedPoint);
    
    /**
     * Used to select a shape by a click
     * @param pt
     * @return the minimal distance between the point and this shape
     */
    abstract public double getDistanceTo(GCode pt);

    /**
     * Move one point of the element
     * @param point
     * @param dx
     * @param dy
     * @return true if the point belongs to this element
     */
    public boolean movePoint(GCode point, double dx, double dy) {
        throw new UnsupportedOperationException("GElement.movePoint is not supported yet.");
    }
    
    /**
     * Move one point of the element
     * @param selectedPoints a list of points to move
     * @param dx
     * @param dy
     * @return true if at least one point belongs to this element
     */
    abstract public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy); 

    abstract public void translate(double dx, double dy);
    
    abstract public Object remove(int i);

    abstract public void removeAll(ArrayList<GCode> lines);
    
    /**
     * Remove the points tha are closer than an maximal distance.
     * @param points if not null, remove only points of this array
     * @param distance 
     */
    abstract public void removeByDistance(ArrayList<GCode> points, double distance);

    abstract public void reverse();
    
    abstract public void rotate(Point2D origin, double angle);

    abstract public void scale(Point2D origin, double sx, double sy);
    
    abstract public void simplify( double angleMin, double distanceMax);
    
    /**
     * Used by the GCode editor.
     * @param row
     * @param value 
     */
    abstract public void setLine(int row, GCode value);
    
    /**
     * Add a new point in the shape
     * @param point
     * @return the new point created or null if none
     */
    public GCode insertPoint(GCode point) {
        return null;
    }

    /**
     * Return the number of GCode lines that compose this element.
     * @return 
     */
    abstract public int size();
    
    /**
     * Return the i th GCode line.
     * @param i
     * @return 
     */
    abstract public GCode getLine(int i);

    /**
     * @return true if this path is closed (lastPoint is on firstPoint)
     */
    abstract public boolean isClosed();

       
    /**
     * Used in Editor to show content summary of the shape.
     * @return 
     */
    abstract public String getSummary();

    abstract public void paint(PaintContext pc);

    /**
     * @return true if this element must be scaled with isometric ratio.
     */
    public boolean isoScaling() { return false; }

    abstract public boolean isEmpty();

    /**
     * @return a flatten G1 path representation of this element (or a clone of it).
     */
    abstract public GElement flatten();
    
    /**
     * @return  The center of the path or null if none (contains no feed code)
     */
    abstract public Point2D getCenter();

    abstract public void toDXF(OutputStreamWriter out) throws IOException;
    
    /**
     * Initialise this element in reading from a stream.<br>
     * (The <i>GElement.loadFromStream()</i>  methode read only properties line and return the line read or null if EOF).
     * @param stream
     * @param lastGState to know the current Gx state (this method must update it : at least G,X,Y)
     * @return the last line readden from the <i>stream</i> but not included in this element.
     * @throws IOException 
     */
    public String loadFromStream( BufferedReader stream, GCode lastGState) throws IOException {
        modified = true;
        return loadProperties(stream, null);
    }
    
    /**
     * Read properties from a string.
     * @param stream
     * @param line the line to read from, or can be null then it will use the stream
     * @return the next line
     * @throws IOException 
     */
    public String loadProperties( BufferedReader stream, String line) throws IOException {
        if ( line == null) line = stream.readLine();
        if ( line == null) return null;
        if ( line.startsWith(EngravingProperties.HEADER_STRING))
            properties = EngravingProperties.decode(line);
        else return line;
        return stream.readLine();
    }

    /**
     * Return the gcode lines corresponding of <i>indices</i>
     * @param indices
     * @return 
     */
    public ArrayList<GCode> getLines(int[] indices) {
        return new ArrayList<>();
    }

    public GCode getPoint(int p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void transform(AffineTransform t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
    /**
     * Interface to manage GElement factory (with setGElementBuilder() ).
     */
    public interface GElementFactoryInterface {
        public Class getClasseFromHeader( String gElementHeader);
    }
    
    private static GElementFactoryInterface gElementFactory = (String line) -> {
        if ( line.startsWith(GGroup.HEADER_STRING)) return GGroup.class;
        if ( line.startsWith(G1Path.HEADER_STRING)) return G1Path.class;
        if ( line.startsWith(GMixedPathPath.HEADER_STRING)) return GMixedPathPath.class;
        if ( line.startsWith(GArc.HEADER_STRING)) return GArc.class;
        if ( line.startsWith(GCylindricalPocket.HEADER_STRING)) return GCylindricalPocket.class;
        if ( line.startsWith(GSphericalPocket.HEADER_STRING)) return GSphericalPocket.class;
        if ( line.startsWith(GDrillPoint.HEADER_STRING)) return GDrillPoint.class;
        if ( line.startsWith(GSpline.HEADER_STRING)) return GSpline.class;
        if ( line.startsWith(GTextOnPath.HEADER_STRING)) return GTextOnPath.class;
        return null;
    };  
    
    /**
     * Set the GElement builder
     * @param newBuilder
     * @return the old defined (to use in new builder to handle all actual GElement)
     */
    public static GElementFactoryInterface setGElementBuilder( GElementFactoryInterface newBuilder) {
        GElementFactoryInterface r = gElementFactory;
        gElementFactory = newBuilder;
        return r;
    }   
    
    public static GElementFactoryInterface getGElementFactoryInterface() {
        return gElementFactory;
    }
    
    static boolean isGElementHeader(String line) {
        return getGElementFactoryInterface().getClasseFromHeader(line) != null;
    }
    
    /**
     * Use GElementFactory to make a new GElement based on a <i>GElement.HEADER_STRING</i> string.<br>
     * use setGElementBuilder() to add your new GElement.
     * @param gElementHeader
     * @return the new GElement according to header string or null if not known.
     */
    @SuppressWarnings("unchecked")
    public static GElement buildGElement(String gElementHeader) {      
        Class c = gElementFactory.getClasseFromHeader(gElementHeader);
        if ( c == null) return null;
        try {
            Field f = c.getField("HEADER_STRING");
            String headerStr = (String)f.get(null);    
            
            return (GElement)c.getConstructor(String.class).newInstance(gElementHeader.substring(headerStr.length(), gElementHeader.length()-1));                
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
            Logger.getLogger(GElement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
