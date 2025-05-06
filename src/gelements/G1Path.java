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

import gcodeeditor.PaintContext;
import gcodeeditor.Segment2D;
import gcodeeditor.EngravingProperties;
import gcodeeditor.GWord;
import gcodeeditor.GCode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import org.kabeja.dxf.helpers.Point;

/**
 *
 * @author Clément
 */
public class G1Path extends GElement implements Iterable<GCode> {

    public static final String HEADER_STRING = "(Path-name: ";

    /** List of all points of the shape. */
    protected ArrayList<GCode> lines;
    
    /** The current bounding box value. */
    protected Rectangle2D.Double bounds;
    private double length = Double.NaN;
    
    GeneralPath.Double renderedShape;
       
    public G1Path(String name) {
        this(name, 10);
    }
     
    public G1Path(String name, int initialCapacity) {
        super(name);
        lines = new ArrayList<>(initialCapacity);
    } 

    /**
     * Create a new GPath with this points.
     * @param name
     * @param points 
     */
    public G1Path(String name, ArrayList<GCode> points) {
        super(name);
        lines = new ArrayList<>(points.size());
        addAll(points);
    }

    /**
     * Create a new Path with on or two points.
     * @param name
     * @param start
     * @param end can be null
     */
    public G1Path(String name, Point2D start, Point2D end) {
        this(name, (end!=null)?2:1);
        lines.add(new GCode(0, start.getX(), start.getY()));
        if ( end != null) lines.add(new GCode(end.getX(), end.getY()));
    }

    /**
     * Only used by JListViewerModel
     * @return the number of gcode lines + 1
     */
    @Override
    public int getSize() {
        return lines.size()+1; // show a line to insert at end of path
    }

    @Override
    public GCode getElementAt(int index) {
        //if ( index == 0) return "(Path: "+name + id+")";
        if ( index == lines.size()) return new GCode(); // show a line to insert at end of path
        return lines.get(index);
    }
    
    @Override
    public GCode getFirstPoint() {
        return getPoint(0);
    }
    
    @Override
    public GCode getLastPoint() {
        for( int i = lines.size()-1; i>=0; i --)
            if ( lines.get(i).isAPoint()) return lines.get(i);
        return null;
    }

    @Override
    public void reverse() {
        int first = indexOf(getFirstPoint());
        int last = indexOf(getLastPoint());
        while( first < last) {
            GCode l2 = remove(last);
            GCode l1 = remove(first);
            add(first++,l2);
            add(last--,l1);
        }
    }
    
    @Override
    public boolean concat(GElement shape, double tolerance) {

        if ( ! (shape instanceof G1Path)) return false;
        
        boolean merged = false;
        if ( getLastPoint().distance(shape.getFirstPoint())< tolerance)
        {
            for( int i = 1; i < shape.size(); i++) {
                add( shape.getLine(i));
            }
            merged=true;
        }
        else if ( getLastPoint().distance(shape.getLastPoint())< tolerance)
        {
            for( int i = shape.size()-2; i >= 0; i--) {
                add( shape.getLine(i));
            }
            merged=true;
        }
        else if ( getFirstPoint().distance(shape.getFirstPoint()) < tolerance)
        {
            for( int i = 1; i < shape.size(); i++) {
                add(0, shape.getLine(i));
            }
            merged=true;
        }
        else if ( getLine(0).distance(shape.getLastPoint())< tolerance)
        {
            for( int i = shape.size()-2; i >= 0; i--) {
                add(0, shape.getLine(i));
            }
            merged=true;
        }
        if( merged) informAboutChange();
        return merged;
    }
    
    public static boolean isEquals(GCode p1, Point p2) {
        return (Math.abs(p1.getX()-p2.getX()) < 0.00001) && (Math.abs(p1.getY()-p2.getY()) < 0.00001);
    }
    
    /**
     * @param point the point to compare to
     * @param dmin discar all point away from <i>dmin</i>
     * @return the closest point of the shape or <i>null</i> if none */
    @Override
    public GCode getCloserPoint(java.awt.geom.Point2D point, double dmin, ArrayList<GCode> discareIt, boolean excludeFirst) {
        if (point == null) return null;
        GCode near = null;
        for( GCode p : lines) {
            if (p.isAPoint() && ! excludeFirst &&
                    //! p.isIn(discareIt)
                    ! ((discareIt!=null) && discareIt.contains(p))
                    ) { // (discareIt==null)||(discareIt.indexOf(p)==-1))) {
                double d = point.distance(p);
                if ( dmin > d) {
                    dmin = d;
                    near = p;
                }
            }
            excludeFirst = false;
        }
        return near;
    }
    
    /**
     * @param point the point to compare to
     * @return the minimal distance between the real shape points and 'point' 
    public double getDistanceToAllPoints(GCLine point) {
        if (point == null) return Double.POSITIVE_INFINITY;
        double mind = Double.POSITIVE_INFINITY;

        for( GCLine p : lines) if ( p.isAPoint()) {
            double d = point.distance(p);
            if ( mind > d) mind = d;
        }
        return mind;
    }
    */
    
    
    /**
     * @param point the point to compare to
     * @return the minimal distance of 'point' to the shape */
    @Override
    public double getDistanceTo(GCode point) {
        if (point == null) return Double.POSITIVE_INFINITY;
        double mind = Double.POSITIVE_INFINITY;

        Segment2D s;
        GCode lastPoint = null;
        //for( GCode p : elements) {
        for ( int i = 0; i < lines.size(); i++) { 
            GCode p = lines.get(i);
            if ( p.isAPoint())  {
                double d;
                if ( lastPoint != null) {
                    s = new Segment2D(lastPoint, p);
                    d = Math.abs(s.distanceTo(point));
                    if ( mind > d) mind = d;
                } else
                    mind = p.distance(point);
                
                lastPoint = p;
            }
        }
        return mind;
    }
    
    /**
     * @param point the point to compare to
     * @return the the closest distanceTo of 'point' to the shape */
    public Segment2D getClosestSegmentTo(GCode point) {
        if (point == null) return null;
        double mind = Double.POSITIVE_INFINITY;

        Segment2D res = null;
        Segment2D s;
        GCode lastPoint = null;
        //for( GCode p : elements) {
        for ( int i = 0; i < lines.size(); i++) { 
            GCode p = lines.get(i);
            if ( p.isAPoint()) {
                if ( lastPoint != null) {
                    s = new Segment2D(lastPoint, p);
                    double d = Math.abs(s.distanceTo(point));
                    if ( mind > d) {
                        mind = d;
                        res = s;
                    }
                }
                lastPoint = p;
            }
        }
        return res;
    }
    
    /** Move all points of this shape with vector [dx,dy].
     * @param dx
     * @param dy */
    @Override
    public void translate(double dx, double dy) {
       lines.forEach((p) -> {
           if ( p.isAPoint()) p.setLocation(p.getX() + dx, p.getY() + dy);
        });
        informAboutChange();
    }
   
    @Override
   public void transform(AffineTransform t) {
        lines.forEach((p) -> {
           if ( p.isAPoint()) p.transform( t);
        });
        informAboutChange();    
    }    
   
    /** 
     * @return  the bounding box of this shape.*/
    @Override
    public Rectangle2D getBounds() {
        if ( bounds == null) {
            if ( getNbPoints() == 0) return null;
            lines.forEach((p) -> {
                if ( p.isAPoint()) {
                    if ( bounds == null) 
                        bounds = new Rectangle2D.Double(p.getX(), p.getY(), 10e-6, 10e-6);
                    else bounds.add(p);
                }
            });
        }
        return (Rectangle2D) bounds.clone();        
    }

    /**
     * @return true if no gcode lines (and comments) in this element
     */
    @Override
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * @return the number of gcode lines in this element
     */
    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public GCode getLine(int i) {
        return lines.get(i);
    }
    
    @Override
    public ArrayList<GCode> getLines(int[] indices) {
        ArrayList<GCode> res = new ArrayList<>();
        for( int i : indices)
            res.add(getElementAt(i));
        return res;
    }

    @Override
    public void add(int i, GCode line) {
        insertLine(i, line);
    }
    
    @Override
    public boolean add(GCode e) {
        insertLine( lines.size(), e);
        return true;
    }
    
    public void add( Point p) {
        insertLine( lines.size(), new GCode(lines.isEmpty()?0:1, p.getX(), p.getY()));
    }

    @Override
    public Iterator<GCode> iterator() { 
        Iterator<GCode> it = new Iterator<GCode>() {
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < lines.size();
            }
            @Override
            public GCode next() {
                return lines.get(currentIndex++);
            }
            @Override
            public void remove() {
                path.remove(currentIndex);
            }
            G1Path path;
            public Iterator<GCode> setPath( G1Path p) {
                path=p;
                return this;
            }
        }.setPath(this);
        return it;
    }

    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {
        boolean moved = false;
        if ( ! selectedPoints.isEmpty()) {
                for( GCode p : selectedPoints) {
                    if (lines.contains(p)) {     
                            p.setLocation( p.getX() + dx, p.getY() + dy);
                            moved=true;
                    }
                }
        }
        if ( moved ) informAboutChange();
        return moved;       
    }
    
    @Override
    public boolean movePoint(GCode p, double dx, double dy) {
        if (lines.contains(p)) {     
            p.setLocation( p.getX() + dx, p.getY() + dy);
            informAboutChange();
            return true;
        }
        return false;
    }
    
    public boolean joinPoints( ArrayList<GCode> selectedPoints) {
        if ( selectedPoints.size() < 2) return false;
        
        double sx=0, sy=0;
        int n=0, pos=-1;
        //calculate center
        for( GCode p : selectedPoints) {
            sx+=p.getX();
            sy+=p.getY();
            n++;
        }
        
        boolean changed = false;
        for( GCode p : selectedPoints) if ( lines.contains(p)) {
            p.setLocation(sx/n, sy/n);        
            changed= true;
        }
        
        if ( changed) {
            removeByDistance(null, 10e-9);
            informAboutChange();
        }
        return changed;
    }
    
    @Override
    protected void informAboutChange() {
        modified=true;
        renderedShape=null;
        bounds=null;
        length = Double.NaN;
        super.informAboutChange();
    } 

    public void remove( GCode l) {
        int i = lines.indexOf(l);
        if ( i != -1) remove(i);
    }

    @Override
    public GCode remove( int index) {
        GCode res = lines.remove(index);
        GCode l = getFirstPoint();
        if ( (l!=null) && (l.getG() != 0)) l.setG(0);
        informAboutChange();
        return res;
    }
    
    @Override
    public void removeComments() {
        for ( GCode gc : lines)
            if ( gc.isComment()) 
                lines.remove(gc);
    }
    
    @Override
    public void removeAll(ArrayList<GCode> points) {
        lines.removeAll(points);
        GCode l = getFirstPoint();
        if ( (l!=null) && (l.getG() != 0)) l.setG(0);
        informAboutChange();
    }

    @Override
    public GElement clone() {
        G1Path clone = new G1Path(name, lines.size());
        clone.id =  id;
        if ( properties != null) clone.properties = (EngravingProperties) properties.clone();
        lines.forEach((l) -> { clone.add(new GCode(l)); });
        if ( bounds != null) clone.bounds = (Rectangle2D.Double) bounds.clone();
        return clone;
    }
    
    @Override
    public G1Path flatten() {
        G1Path clone = (G1Path)clone();
        clone.newID();
        return clone;
    }

       
    
    public static G1Path cloneArrayOfGLines(ArrayList<GCode> points) {
        ArrayList<GCode> clone = new ArrayList<>(points.size());
        points.forEach((l) -> { clone.add(new GCode(l)); });
        return new G1Path("past"+getUniqID(), clone);
    }

    /**
     * @param pts
     * @param tolerance
     * @return the list of extra points we can delete.
     */
    public ArrayList<GCode>keepToSimplify(ArrayList<GCode> pts, double tolerance) {
        ArrayList<GCode> res = new ArrayList<>();
        GCode lastPoint = null;
        Segment2D lastSegment = null;
        
        for( GCode p : pts) {
            if ( p.isAPoint()) {
                if ( lastPoint != null) {
                    if ( lastSegment == null) {
                        lastSegment = new Segment2D(lastPoint, p);
                    }
                    else {
                        if ( Math.abs(lastSegment.distanceLineAnd(p)) <= tolerance) res.add(lastPoint);
                        lastSegment = new Segment2D(lastPoint, p);
                    }
                }
                lastPoint = p;
            }
        }
        informAboutChange();
        return res;
    }
    
    /**
     * @param angleMin
     * @return a list of points that make an angle with prev and next points where that angles is gretter than angleMin
     */
    public ArrayList<GCode> getPointsByAngle(double angleMin) {
        ArrayList<GCode> res = new ArrayList<>();
        GCode lastPoint = null;
        GCode center = null;
        for( GCode p : getPointsIterator()) {
            if ((lastPoint!=null) && (center!=null) && (getAngleInDegre(center, lastPoint, p) > angleMin))
                res.add(center);
            lastPoint=center;
            center=p;
        }
        return res;
    }

    
    @Override
    public void simplify( double angleMin, double distanceMax) {
        lines.removeAll( keepToSimplify(getPointsByAngle(angleMin), distanceMax));
    }

    /**
     * @param n
     * @return then n th true point (with x,y coordinate) of the path.
     */
    public GCode getPoint(int n) {
        int i = 0, cur = 0;
        for( GCode l : lines) {
            if ( l.isAPoint()) {
                if (cur==n) return lines.get(i);
                cur++;
            }
            i++;
        }
        return null;
    }    
    
    /** 
     * Insert a new point in the shape.
     * @param newPoint
     * @return <i>point</i> if realy inserted or null if not.
     */
    @Override
    public GCode insertPoint( GCode newPoint) {
        Segment2D bestSegment = getClosestSegmentTo(newPoint);

        if ( bestSegment != null) {
            if ( bestSegment.p1.isAtSamePosition(newPoint) ||
                 bestSegment.p1.isAtSamePosition(newPoint))
                return null;
            
            add(lines.indexOf(bestSegment.p1)+1, newPoint);
            informAboutChange();
            return newPoint;
        }
        return null;
    }

    @Override
    public String toString() {
        return getName() + " (" + lines.size() + ")";
    }

    @Override
    public void rotate(Point2D center, double angle) {
        lines.stream().filter((p) -> ( p .isAPoint())).forEach((p) -> {
            double d = center.distance(p);
            double a = getAngleInRadian(center, p);
            p.setLocation( center.getX() + Math.cos(a+angle)*d,
                    center.getY() + Math.sin(a+angle)*d);
        });
        informAboutChange(); 
    }

    @Override
    public void scale(Point2D center, double ratioX, double ratioY) {
        lines.stream().filter((p) -> ( p .isAPoint())).forEach((p) -> {      
            double d = center.distance(p);
            double a = getAngleInRadian(center, p);
            p.setLocation( center.getX() + Math.cos(a)*d*ratioX,
                    center.getY() + Math.sin(a)*d*ratioY);
        });
        informAboutChange();
    }

    /** Color used to paint line and points in editionMode */
    public static Color editionColor = new Color(127, 127, 255);
    
    @Override
    public void paint(PaintContext pc) {
        if ( renderedShape == null) {
            // recalculate visualisation shape
            
            // TODO verrify wind
            renderedShape = new GeneralPath.Double(GeneralPath.WIND_EVEN_ODD, lines.size());
            final Iterable<GCode> pi = getPointsIterator();
            GCode g0 = null;
            
            for( GCode l : lines) {
                if ( l.isAPoint()) {
                    if (g0 == null) {
                        assert l.getG() == 0;
                        g0 = l;
                        renderedShape.moveTo(g0.getX(), g0.getY());
                        continue;
                    } else {
                        assert l.getG() == 1;
                        renderedShape.lineTo(l.getX(), l.getY());
                    }

                }
            }
            if ( bounds == null) {
                bounds = (Rectangle2D.Double)renderedShape.getBounds2D();
                if (bounds.width == 0) bounds.width = 10e-6;
                if (bounds.height == 0) bounds.height = 10e-6;
            }
        }      
        
        final double zoomFactor = pc.zoomFactor;
        final Graphics2D g = pc.g; 
        final Graphics2D g2 = (Graphics2D)pc.g.create();

        // paint path
        g2.scale(zoomFactor, -zoomFactor);
        g2.setStroke(new BasicStroke((float)(1f / zoomFactor)));
        if ( pc.editedElement == this) g2.setColor(PaintContext.EDIT_COLOR);
        else g2.setColor(pc.color);
        g2.draw(renderedShape);
        
        // paint first point
        if ( (pc.color != Color.darkGray) && pc.showStartPoints) {
            GCode p = getFirstPoint();
            if ( p != null) {
                g.setColor(Color.red); 
                g.drawRect((int)(p.getX()*zoomFactor)-3, (int)(p.getY()*-zoomFactor)-3, 6, 6);
            }
        }
                
        if ( pc.editedElement != this) return;
        
        // paint selected moves
        int oldx=Integer.MAX_VALUE, oldy=Integer.MAX_VALUE;
        for( GCode p : lines) {
            if ( p.isAPoint()) {
                int x = (int)(p.getX()*zoomFactor);
                int y = (int)(p.getY()*-zoomFactor);
                
                if ( pc.selectedPoints.contains(p)) {
                    g.setColor(PaintContext.SEL_COLOR1); 
                    if (oldx != Integer.MAX_VALUE)
                        g.drawLine(oldx, oldy, x, y);
                    
                    g.fillOval((int)x-3,(int)y-3, 6, 6);                   
                } else {
                    g.setColor(PaintContext.EDIT_COLOR);
                    g.drawOval((int)x-3,(int)y-3, 6, 6);  
                }  
                
                if ( pc.highlitedPoint == p ) { 
                    g.setColor(Color.white); 
                    g.fillOval((int)x-3,(int)y-3, 6, 6);
                }
                
                oldx=x;
                oldy=y;
                pc.lastPoint = p;
            }
        }

/*        if (bounds != null) {
            g.setColor(Color.red);
            if ( bounds.getWidth() < 10)
                g.drawRect((int)(bounds.getX()*zoomFactor), -(int)((bounds.getY()+bounds.getHeight())*zoomFactor), (int)(bounds.getWidth()*zoomFactor), (int)(bounds.getHeight()*zoomFactor));
        }*/
    }

    @Override
    public int getNbPoints() {
        int r = 0;
        for( GCode l : lines) if ( l.isAPoint()) r++;
        //r = lines.stream().filter((l) -> ( l.isAPoint())).map((_item) -> 1).reduce(r, Integer::sum);
        return r;
    }
    
    /**
     * Try to change de starting point of this path if it is closed or changing last point.
     * @param p
     * @return false if not sucess (no change made)
     */
    public boolean changeFirstPoint(GCode p) {
        if ( (getNbPoints() < 3) || ! lines.contains(p)) return false;
        
        GCode fp; 
        boolean closed = (fp=getFirstPoint()).distance(getLastPoint()) < 0.00001;
        if ( closed) {
            if ( lines.get(lines.size()-1) != p) remove(lines.size()-1);
            fp.setG(1);
            while( (fp=getFirstPoint()) != p) {
                lines.remove(fp);
                lines.add(fp); 
            }
            add((GCode) p.clone());
        } else {
            if ( getLastPoint() == p) {// invert direction
                getFirstPoint().setG(1);
                int i = lines.indexOf(getFirstPoint());
                int nb = (lines.size()-i)/2;
                
                for(int n = 0;  n < nb; n++) {
                    GCode t = lines.get(n+i);
                    lines.set(n+i, (lines.get(lines.size()-n-1)));
                    lines.set(lines.size()-n-1, t);
                }
            } else 
                return false; // do nothing
        }
        // set first move to 
        for( GCode l : lines) if ( l.isAMove()) {
            l.setG(0);
            break;
        }
        informAboutChange();
        return true;
    }

    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        String svg;
        if ( getFirstPoint() == null) return "";
                
        if ( isClosed()) {
            svg = "<polygon id=\""+ name+"-"+id +"\" points=\"";
            for ( int i = 0; i < lines.size()-1; i++) {
                GCode p = lines.get(i);
                svg += (p.getX()-origin.getX()) + "," + (origin.getY()+origin.getHeight()-p.getY()) + " ";
            }
        } else {
            svg = "<polyline id=\""+ name+"-"+id +"\" points=\"";
        
            for ( GCode l : lines) {
                if ( l.isAPoint())
                    svg += (l.getX()-origin.getX()) + "," + (origin.getY()+origin.getHeight()-l.getY()) + " ";
            }
        }
        return svg + "\"\n style=\"fill:none;stroke:black;stroke-width:1px\" />\n";
    } 

    @Override
    public boolean isClosed() {
        if ( getNbPoints() < 1) return false;
        return getFirstPoint().distance(getLastPoint()) < 0.00001;
    }

    /**
     * Cut the path at a line.
     * @param at cut line
     * @return the block after <i>at</i> or null if nothing to cut
     */
    public G1Path cutAt(GCode at) {
        final int pos = getLineof(at)+1;
        if ((lines.size() < 2) || (pos <=1) || (pos>=lines.size()))  return null;
        G1Path newShape = new G1Path(name);
        newShape.add((GCode) at.clone());
        while( lines.size() > pos) newShape.add(remove(pos));
        informAboutChange();
        return newShape;
    }
    
    @Override
    public GCode saveToStream(java.io.FileWriter w, GCode lastPosition) throws IOException {
        w.append(HEADER_STRING + name + ")\n");
        w.append(properties.toString()+"\n");
        
        boolean first = true;
        for ( GCode l : lines) {
            if ( l.isAPoint()) { 
                // TODO simplify that !!
                if ( first && (lastPosition!=null) && (l.size()==lastPosition.size()) && lastPosition.isAtSamePosition(l)) {
                    lastPosition=l;
                    first=false;
                    continue;
                }
                lastPosition = l;
                first=false;
            }    
            w.append( l.toString() + "\n");
        }
        return lastPosition;
    }

    /** 
     * @param line
     * @return the index of this line
     */
    public int indexOf(GCode line) {
        return lines.indexOf(line);
    }
    
    @Override
    public int getIndexOfPoint(GCode point) {
        assert point.isAPoint();

        int pos = 0;
        for ( GCode l : lines) {
            if ( l==point) return pos;
            if ( l.isAPoint()) pos++;
        }
        return -1;
    }

    @Override
    public double getLenOfSegmentTo(GCode p) {
        if ( ! p.isAPoint()) return Double.NaN;

        final int i = getIndexOfPoint(p);
        if (i < 1) return Double.NaN;
        
        return getPoint(i-1).distance(p);
    }
    
    @Override
    double getLength() {
        if ( Double.isNaN(length)) {
            GCode lastPoint = null;
            double len = 0;
            for ( GCode l : lines)
                if ( l.isAPoint()) {
                    if ( lastPoint != null) len += lastPoint.distance(l);
                    lastPoint = l;
                }
            length = len;
        }
        return length;
    }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double d) {
        if ( getNbPoints() < 3) return;
        if ( points == null) points = lines;
        
        final ArrayList<GCode> toRemove = new ArrayList<>();
        GCode lastPoint = getLastPoint();
        GCode prevPoint = null;
        for ( GCode p : points) {
            if ( p.isAPoint()) {
                if ( prevPoint != null)
                    if ( prevPoint.distance(p) <= d) {
                        if ( p != lastPoint) toRemove.add(p);
                        continue;
                    }
                prevPoint = p;
            }
        }
        lines.removeAll(toRemove);
    }
    
    /** Change line value.
     * @param row
     * @param value */
    @Override
    public void setLine(int row, GCode value) {
        if ( row == lines.size()) lines.add( value);
        else lines.get(row).set(value);
        informAboutChange();
    }

    /** Add lines into path.
     * @param position
     * @param lines */
    public void addAllAt(int position, ArrayList<GCode> lines) {
        for( int l = lines.size()-1; l >= 0; l--) {
            add(position, lines.get(l));
        }
    }
    
    /** Add lines into path.
     * @param lines */
    public void addAll(ArrayList<GCode> lines) {
        lines.forEach((l) -> {
            add(l);
        });
    }

    /**
     * @param line
     * @return The line number of this line
     */
    public int getLineof(GCode line) {
        for( int l =0; l < lines.size(); l++) 
            if ( lines.get(l) == line) return l;
        return -1;
    }

    @Override
    public String getSummary() {
        String res = "<html>";
        int nb = 0;
        for ( GCode l : lines) if (nb++ < 20) res += l.toString() + "<br>";
        if ( nb>=20) res += "...</html>";
        return res;
    }

    /** 
     * Returns all the lines, not a copy !
     * @return return a ArrayList&lt;GCode&gt;
     */
    public Collection<? extends GCode> getAll() {
        return lines;
    }
    
    public static GGroup makeText(java.awt.Font font, String text) {
        final FontRenderContext context = new FontRenderContext(null, false, false);
        final GeneralPath shape = new GeneralPath();
        final TextLayout layout = new TextLayout(text, font, context);
        final Shape outline = layout.getOutline(new AffineTransform(1, 0, 0, -1, 0, 0));
        shape.append(outline, true);
        final GGroup g = new GGroup(text, makeFromShape("ch", shape, "Font: " + font.getName()+'-'+font.getSize()));
        g.joinElements(0.0002);
        return g;
    }
    
    public static ArrayList<GElement> makeFromShape(String name, Shape shape, String rem) {
        final ArrayList<GElement> res = new ArrayList<>();      
        G1Path currentBlock = null;
        FlatteningPathIterator iter = new FlatteningPathIterator(shape.getPathIterator(new AffineTransform()), 0.1);
        final float[] coords=new float[6];
        float lastX = 0, lastY = 0;
        while (!iter.isDone()) {
            switch ( iter.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    //System.out.println("M("+param[0]+" , "+ param[1]);
                    res.add(currentBlock = new G1Path(name));
                    if ( rem != null) currentBlock.add(new GCode('('+rem+')'));
                    currentBlock.add(new GCode(0, lastX = coords[0], lastY = coords[1]));
                    break;
            case PathIterator.SEG_LINETO:
               // System.out.println("   L("+param[0]+" , "+ param[1]);
                    currentBlock.add(new GCode(lastX = coords[0], lastY = coords[1]));
                    break;
            case PathIterator.SEG_QUADTO:
                iter=new FlatteningPathIterator(new QuadCurve2D.Double(coords[0], coords[1], coords[2], coords[3], lastX = coords[4], lastY = coords[5]).getPathIterator(new AffineTransform()), 0.1);
                res.add(currentBlock = new G1Path("quad"));
                while (!iter.isDone()) {
                    iter.currentSegment(coords);
                    currentBlock.add(new GCode(coords[0], coords[1]));
                    iter.next();
                }
                break;
            case PathIterator.SEG_CUBICTO:
                iter=new FlatteningPathIterator(new CubicCurve2D.Double(lastX, lastY, coords[0], coords[1], coords[2], coords[3], lastX = coords[4], lastY = coords[5]).getPathIterator(new AffineTransform()), 0.1);
                res.add(currentBlock = new G1Path("cubic"));
                while (!iter.isDone()) {
                    iter.currentSegment(coords);
                    currentBlock.add(new GCode(coords[0], coords[1]));
                    iter.next();
                }
                break;
            case PathIterator.SEG_CLOSE:
                break;
            }
            iter.next();
        }
     
        return res;
    }
    
    public static ArrayList<G1Path> getPathFor() {
        final ArrayList<G1Path> res = new ArrayList<>();
        G1Path currentBlock = null;
        // test récupération de chmion sur texte
        final FontRenderContext context = new FontRenderContext(null, false, false);
        final GeneralPath shape = new GeneralPath();
        final TextLayout layout = new TextLayout("Bonjour", new java.awt.Font("Arial-10", 0, 30), context);
        final Shape outline = layout.getOutline(null);
        shape.append(outline, true);
        final PathIterator path = shape.getPathIterator(null);        
        while( !path.isDone()) {
            final double[] param = new double[6];
            switch (path.currentSegment(param)) {
                case PathIterator.SEG_MOVETO:
                        System.out.println("M("+param[0]+" , "+ param[1]);
                        res.add(currentBlock = new G1Path("text"));
                        currentBlock.add(new GCode(0, param[0], -param[1]));
                        break;
                case PathIterator.SEG_LINETO:
                    System.out.println(" L("+param[0]+" , "+ param[1]);
                        currentBlock.add(new GCode(param[0], -param[1]));
                        break;
                case PathIterator.SEG_CUBICTO:
                        break;
                case PathIterator.SEG_CLOSE:
                        System.out.println("close");
                        break;
                default:    
                    System.out.println("other");
            }    
            path.next();
        }
        return res;
    }
    
    public static G1Path makePolygon(Point2D center, int nbEdges, double edgeLen, double radius, boolean clockwise) {
        G1Path res = new G1Path( "poly"+nbEdges+"-"+getUniqID());
        final double da = 2* Math.PI/nbEdges;
        double angle=0;
        for( int e = 0; e < nbEdges; e++) {
            double x = Math.cos(angle);
            if ( Math.abs(x) < 0.000000001) 
                x = 0;
            double y = Math.sin(angle);
            if ( Math.abs(y) < 0.000000001)
                y = 0;

            res.add(new Point(radius * x, radius * y, 0));
            angle += (clockwise ? da : -da);
        }
        res.add(new Point(radius * Math.cos(0), radius * Math.sin(0),0));
        if ( center != null) res.translate(center);
        return res;
    }
    
    public static G1Path makeCircle( Point2D center, int nbPoints, double radius, boolean clockwise, boolean adjustNbPoints) {   
            double angle=0;
            if ( adjustNbPoints ) nbPoints = (nbPoints/4+((4*(nbPoints/4)==nbPoints)?0:1))*4;
            final double a = 2* Math.PI/nbPoints;
            
            final G1Path s = new G1Path("circle");
            for(int n = 0; n < nbPoints; n++) {
                
                double x = Math.cos(angle);
                if ( Math.abs(x) < 0.000000001) 
                    x = 0;
                double y = Math.sin(angle);
                if ( Math.abs(y) < 0.000000001)
                    y = 0;
         
                s.add(new Point(radius * x, radius * y, 0));
                angle += (clockwise ? a : -a);
            }
            s.add(new Point(radius * Math.cos(0), radius * Math.sin(0),0));
            if ( center != null) s.translate(center);
            return s;
    }
    
    public static GGroup makeCross(Point2D location, double len) {
        final GGroup c = new GGroup("cross");
        final G1Path l = new G1Path("cross-hLine");
        len/=2;
        l.add( new Point(-len, 0, 0));
        l.add( new Point(0, 0, 0));
        l.add( new Point(len, 0, 0)); 
        c.add(l);
        final G1Path l2 = new G1Path("cross-vLine");
        c.add( l2 );
        l2.add( new Point(0, -len, 0));
        l2.add( new Point(0, 0, 0));
        l2.add( new Point(0, len, 0)); 
        if ( location != null) c.translate(location);
        return c;
    }
    
    /**
     * Create a new path with all paths linked together.
     * @param name the name of the new path
     * @param paths the paths to be linked
     * @return 
     */
    public static G1Path makeLinkedPath(String name, ArrayList<GElement> paths) {
        GGroup.optimizeMoves(paths, null, true);
        final G1Path res = new G1Path(name);
        for ( GElement e : paths) 
            if ( e instanceof GGroup) 
                res.append( makeLinkedPath("sub", ((GGroup)e).getAll()));
            else
                res.append((G1Path)e.flatten());
            
        return res;
    }
    
    public static G1Path makeOval(Point2D center, double xRadius, double yRadius, double segLen) {
        final G1Path res = makeCircle(center, (int)(Math.PI*2*Math.sqrt((xRadius*xRadius+yRadius*yRadius)/2)/segLen), 1, true, true);
        res.scale(center, xRadius, yRadius);
        if ( xRadius != yRadius) res.setName("oval");
        return res;
    }
    
    public static GGroup makePocket( ArrayList<GElement>elements, double offset) {
        final GElement biggerBlock = elements.get(0); // pocket of the first of the list, not the biggest     
        final Rectangle2D rMax  = biggerBlock.getBounds();
               
        ArrayList<GElement> offsets = new ArrayList<>(), newBlocks;             
        final ArrayList<GElement> originalBlocks = GGroup.toList(elements);
        originalBlocks.remove(biggerBlock);
        boolean added;
        Area area = null;
        do {
            for ( GElement b : elements) {
                Area a = b.getOffsetArea(offset);
                if ( a==null) {
                    a = b.getOffsetArea(offset);
                    System.out.println("a==null");
                }
                if ( area == null) area = a;
                else area.add(a);   
            }

            newBlocks = G1Path.makeElementsFromArea( "pocket", area);
            elements.clear();
            added = false;
            for ( GElement b : newBlocks) {
                boolean discare = (b.getNbPoints() < 2);
                if ( ! discare && rMax.contains(b.getBounds())) {
                  for( GElement ob : originalBlocks) 
                      if ( ob.getBounds().contains(b.getBounds()) && ob.contains(b.getFirstPoint())) {
                          discare = true;
                          break;
                      }
                } else discare = true;
                if ( ! discare && ((b.getNbPoints()>2)||(b.getFirstPoint().distance(b.getLastPoint()))>offset))  {
                    offsets.add(b);
                    elements.add(b);
                    added= true;
                }
            }                
        } while ( added );

        return new GGroup("pocket-"+biggerBlock.getName(), offsets);
    }
    

    public static G1Path makeRounRect(double w, double h, double radius) {
        
        final int nbPoints = (int)((2* Math.PI*radius)/12)*12; 
        double angle=-Math.PI/2;
        final double a = 2* Math.PI/nbPoints;
         
        G1Path s = new G1Path("roundRect");
        
        Point2D center = new Point2D.Double(w-radius,radius);
        for(int n = 0; n < nbPoints; n++) {
            if ( n == 0) s.add( new Point(radius,0,0));
            else if ( n ==  nbPoints/4) {
                s.add( new Point(w,radius,0));
                center = new Point2D.Double(w-radius,h-radius);
            } else if ( n ==  2* nbPoints/4) {
                s.add( new Point(w-radius,h,0));
                center = new Point2D.Double(radius,h-radius);
            } else if ( n ==  3* nbPoints/4) {
                s.add( new Point(0,h-radius,0));
                center = new Point2D.Double(radius,radius);
            }
            double x = Math.cos(angle);
            if ( Math.abs(x) < 0.000000001) x = 0;
            double y = Math.sin(angle);
            if ( Math.abs(y) < 0.000000001) y = 0;
            s.add( new Point(center.getX() + radius * x, center.getY() + radius * y, 0));
            angle +=a;
        }
        s.add( new Point(radius,0,0));
        return s;
    }
    
    /**
     * Order a list of point of this path based on their line numbers.
     * @param points 
     */
    public void sortPoints( ArrayList<GCode> points) {

        boolean sorted, changed=false;
        do {
            sorted = true;
            for( int i = 0; i < points.size()-1; i++) {
                if ( (lines.indexOf(points.get(i))) >  (lines.indexOf(points.get(i+1)))) {
                    points.add(i+1, points.remove(i));
                    sorted = false;
                    changed=true;
                }
            }
        } while ( ! sorted);
        if ( changed) informAboutChange();
    }

    public void removeZ() {
        final ArrayList<GWord> toRemove = new ArrayList<>();
        final ArrayList<GCode> lineToRemove = new ArrayList<>();
        for ( GCode l : lines) {
            toRemove.clear();
            for( GWord w : l) 
                if ( w.getLetter() == 'Z') toRemove.add(w);
            for( GWord w : toRemove) 
                l.remove(w);
            if ( l.isAMove() && l.size()==1) lineToRemove.add(l);
        }
        lineToRemove.forEach((o) -> lines.remove(o));
        if ( ! lineToRemove.isEmpty()) informAboutChange();
    }
    
    @Override
    public Area getOffsetArea( double distance) {
        if ( getNbPoints() < 2) return null;
        final Area a = new Area();
        GCode lastPoint = null;

        for( GCode p : lines) {
            if ( p.isAPoint()) {
                if ( lastPoint != null) {
                    a.add( new Area(new Ellipse2D.Double(lastPoint.getX()-distance, lastPoint.getY()-distance, 2*distance, 2*distance)));
                    a.add( new Area(getLineOffset(lastPoint, p, distance)));
                    
                }
                lastPoint = p;
            }
        }
        if ( ! isClosed())
            a.add( new Area(new Ellipse2D.Double(lastPoint.getX()-distance, lastPoint.getY()-distance, 2*distance, 2*distance)));
        
        return a;
    }
        
    public static ArrayList<GElement> makeElementsFromArea( String name, Shape a) {
        //GGroup res = new GGroup(name);
        final ArrayList<GElement> res = new ArrayList<>();
        G1Path currentBlock = null;
        final PathIterator pi = a.getPathIterator(null, 0.1);
        int number=0;
        while (pi.isDone() == false) {
            final double[] coordinates = new double[6];
            final int type = pi.currentSegment(coordinates);
            switch (type) {
            case PathIterator.SEG_MOVETO:
                    final GCode p = new GCode(coordinates[0], coordinates[1]);
                    if ( (currentBlock != null) &&  ! currentBlock.isEmpty() && (currentBlock.getLastPoint().distance(p) > 0.00001)) {
                        
                        currentBlock.simplify(100, 0.1);
                        currentBlock.removeByDistance(null, 0.01);
                        if ( currentBlock.size() > 0) {
                            res.add(currentBlock);
                            currentBlock=null;
                        } 
                    }
                    if ( currentBlock==null) currentBlock = new G1Path(name+(number++));
                    currentBlock.add( p);
                    break;
            case PathIterator.SEG_LINETO:
                    currentBlock.add( new Point(coordinates[0], coordinates[1], 0));
                    break;
            case PathIterator.SEG_QUADTO:
              System.out.println("quadratic to " + coordinates[0] + ", " + coordinates[1] + ", "
                  + coordinates[2] + ", " + coordinates[3]);
              break;
            case PathIterator.SEG_CUBICTO:
              System.out.println("cubic to " + coordinates[0] + ", " + coordinates[1] + ", "
                  + coordinates[2] + ", " + coordinates[3] + ", " + coordinates[4] + ", " + coordinates[5]);
              break;
            case PathIterator.SEG_CLOSE:
                if ( currentBlock != null) {
                    currentBlock.add((GCode) currentBlock.lines.get(0).clone());
                    currentBlock.removeByDistance(null, 0.1);
                    if ( currentBlock.size() > 0) res.add(currentBlock);
                }
                currentBlock = new G1Path(name+(number++));
                break;
            default:
              break;
            }
            pi.next();
        }  
        /*if ( res.size() == 1) {
            res.get(0).setName(name);
            return res.get(0);
        } */
        if ( ! currentBlock.isEmpty()) res.add(currentBlock);
        return res;
    }
    
    /**
     * Create a Shape that correspond to the offset path ot a line between <i>p1</i> and <i>p2</i>.
     * @param p1
     * @param p2
     * @param radius
     * @return 
     */
    public static Shape getLineOffset(Point2D p1, Point2D p2, double radius) {
        final GeneralPath result = new GeneralPath();
        final double x1 = p1.getX();
        final double x2 = p2.getX();
        final double y1 = p1.getY();
        final double y2 = p2.getY();
        if ((x2 - x1) != 0.0) {
            final double theta = Math.atan((y2 - y1) / (x2 - x1));
            final double dx = (float) Math.sin(theta) * radius;
            final double dy = (float) Math.cos(theta) * radius;
            result.moveTo(x1 - dx, y1 + dy);
            result.lineTo(x1 + dx, y1 - dy);
            result.lineTo(x2 + dx, y2 - dy);
            result.lineTo(x2 - dx, y2 + dy);
            result.closePath();
        }
        else {
            // special case, vertical line
            result.moveTo(x1 - radius , y1);
            result.lineTo(x1 + radius , y1);
            result.lineTo(x2 + radius, y2);
            result.lineTo(x2 - radius, y2);
            result.closePath();
        }
        return result;
    }


    @Override
    public boolean contains(GCode point) {
        if (lines.stream().filter((l) -> ( l.isAPoint())).anyMatch((l) -> (l.isAtSamePosition(point)))) {
            return true;
        }
        return false;
    }
    
    public static G1Path newRectangle(GCode p1, GCode p2) {
        final double w = p2.getX()-p1.getX();
        final double h = p2.getY()-p1.getY();
        
        G1Path res = new G1Path("rectangle");  
        res.add(p1);
        res.add(new GCode(p2.getX(), p1.getY()));
        res.add(p2);
        res.add(new GCode(p1.getX(), p2.getY()));
        res.add((GCode) p1.clone());
        return res;
    }
    
    private Iterable<Segment2D> getSegmentIterator() {
        return () -> new Iterator<Segment2D>() {
            int nbp= getNbPoints(), n=0, curLine=0;
            GCode lastPoint;
            @Override
            public boolean hasNext() {
                return (n < nbp) && (nbp > 1);
            }
            @Override
            public Segment2D next() {
                GCode p;
                Segment2D s = null;
                do {
                    while( ! (p=lines.get(curLine++)).isAPoint());
                    n++;
                    if ( lastPoint == null ) lastPoint = p;
                    else s = new Segment2D(lastPoint, p);
                } while (s==null);
                lastPoint = p;
                return s;
            }
        };
    }

    @Override
    public Iterable<GCode> getPointsIterator() {
        // TODO not optimized !!
        return () -> new Iterator<GCode>() {
            int nbp=getNbPoints(), n=0; // wron if
            @Override
            public boolean hasNext() {
                return n < nbp;
            }
            @Override
            public GCode next() {
                return getPoint(n++);
            }
            @Override
            public void forEachRemaining(Consumer<? super GCode> action) {
                throw new UnsupportedOperationException("g1path.iterator");
                //Iterator.super.forEachRemaining(action); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("g1path.iterator");
                //Iterator.super.remove(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
            }
        };
    }

    @Override
    public Point2D getCenter() {
        if ( getNbPoints() == 3 + (isClosed() ? 1 : 0)) {
            GCode p1 = getFirstPoint();
            GCode p2 = getPoint(1);
            GCode p3 = getPoint(2);
            double yDelta_a = p2.getY() - p1.getY();
            double xDelta_a = p2.getX() - p1.getX();
            double yDelta_b = p3.getY() - p2.getY();
            double xDelta_b = p3.getX() - p2.getX();
            GCode c = new GCode(0,0);
            double aSlope = yDelta_a/xDelta_a;
            double bSlope = yDelta_b/xDelta_b;  
            double cx = (aSlope*bSlope*(p1.getY() - p3.getY()) + bSlope*(p1.getX() + p2.getX())
                            - aSlope*(p2.getX()+p3.getX()) )/(2* (bSlope-aSlope) );
            double cy = -1*(cx - (p1.getX()+p2.getX())/2)/aSlope +  (p1.getY()+p2.getY())/2;
            return new Point2D.Double(cx, cy);
        }
        if ( bounds != null) getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }
    
    public double getSurfaceValue() {
        double res = 0;
        int nbp = getNbPoints();
        if ( isClosed()) nbp--;
        if( nbp < 3) return 0;
        else {             
            Point2D orig = getPoint(0);
            for( int i = 0; i < nbp - 2; i++) {
                double a = orig.distance( getPoint(i+1));
                double c = orig.distance( getPoint(i+2));
                double b = getPoint(i+1).distance( getPoint(i+2));
                double p = (a+b+c)/2.;
                double s =  Math.sqrt(p * (p-a) * (p-b) * (p-c));
                System.out.println("Surface " + i + " =" + s + " => " + (res +s));
                res += s;
            }
        }
        return res;
    }

    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        GCode lastPoint = null;
        for( GCode p : getPointsIterator()) {
            //GCLine p = element.getLine(i);
            if ( p.isAPoint()) {
                if ( lastPoint != null) {
                    out.write("0\nLINE\n");
                    out.write("8\n1\n");  // layer 1
                    out.write("10\n"+lastPoint.getX()+"\n");
                    out.write("20\n"+-lastPoint.getY()+"\n");
                    out.write("11\n"+p.getX()+"\n");
                    out.write("21\n"+-p.getY()+"\n");
                }
                lastPoint = p;
            }           
        }
    }

    /**
     * Append (move) all line of the path at the end of this path.
     * @param losingPath the path to be cleared
     */
    private void append(G1Path losingPath) {
        losingPath.lines.forEach((l) -> {
            insertLine(lines.size(), l);
        });
        losingPath.lines.clear();
    }
    
    /** 
     * Add points at each intersection between this path.
     * @param shapes
     * @return the G1 points added
     */
    public ArrayList<GCode> addIntersectionPointsWith(ArrayList<G1Path> shapes) {
        final ArrayList<GCode> res = new ArrayList<>();
        GCode last=null;
           
        // Find points by segments intersections
        for( int lineNumber=0; lineNumber < lines.size();) {
            GCode pt = lines.get(lineNumber);
            lineNumber++;
            if ( ! pt.isAPoint()) continue;
            if ( last != null) {
                for ( G1Path shape : shapes)
                    if (shape != this) {    
                        for( Segment2D s : ((G1Path)shape).getSegmentIterator()) {
                            s.sortPointsByY();
                            GCode iP = new Segment2D(last, pt).intersectionPoint(s);
                            if ( (iP != null) && ! contains(iP)) {
                                iP.setG(1);
                                add(lineNumber-1, pt = iP);
                                res.add(iP);
                            }
                        }
                    }
            }
            last = pt;
        }
        return res;
    }

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        
        while( line != null) {
            final GCode gcl = new GCode(line, lastGState); 
            final boolean hasPoint = getFirstPoint() != null;
                
            // is it end of this path ?
            if ( ((gcl.getG()!=0) && ((gcl.getG()!=1) && ! gcl.isComment()) || 
                 ((gcl.isComment() || (gcl.getG() != 1)) && hasPoint) || 
                 GElement.isGElementHeader(line)))
                        break;
            
            if ( ! hasPoint && ! gcl.isComment() && (gcl.getG()!=0)) {
                // add the G0 first
                if ( lastGState == null) lastGState = gcl;
                lines.add( new GCode(0, lastGState.getX(), lastGState.getY()));
            }
            
            if ( ! gcl.isComment()) {
                if ( ! gcl.isAMove() || ! gcl.isAtSamePosition( lastGState)) lines.add( gcl);
                if ( lastGState == null) lastGState = gcl.clone();
                else lastGState.updateGXYWith(gcl);
            } else
                lines.add( gcl);
            
            line = stream.readLine();
        }
        informAboutChange();
        return line;
    }

    protected void insertLine(int i, GCode line) {
        GCode p;
        // Change first G0 to G1 if exist and needed
        if ( line.isAMove() ) {
            if ((p=getFirstPoint()) != null) {
                if (p.isAMove() && (lines.indexOf(p) >= i)) {
                    p.setG(1);
                    line.setG(0);
                }
                else line.setG(1);
            }
            else line.setG(0);
        }
        lines.add(i, line); 
        informAboutChange();
    }

    /**
     * Add point at canter of each G1 move of selectedPoints
     * @param selectedPoints
     * @return the new points insered
     */
    public ArrayList<GCode> addAtHalf(ArrayList<GCode> selectedPoints) {
        ArrayList<GCode> res = new ArrayList<>(selectedPoints.size());
        
        GCode lastPoint = null;
        for ( int i = 0; i < lines.size(); i++) { 
            GCode p = lines.get(i);
            if ( selectedPoints.contains(p) && p.isAPoint() && (lastPoint != null)) {

                final Segment2D s = new Segment2D(lastPoint, p);
                lastPoint = (GCode)s.getPointAt(s.getLength()/2);
                add(i, lastPoint);
                res.add(lastPoint);
                i++;
            }
            lastPoint = p;
        }
 
        return res;
    }
   
}

 
