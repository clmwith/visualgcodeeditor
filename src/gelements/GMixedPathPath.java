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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A path composed of G1, G2-3(arc) and G5(spline) paths (no comment allowed)
 * @author Clément Gérardin @ Marseille.fr
 */
public class GMixedPathPath extends GElement {
    
    public static final String HEADER_STRING = "(ComposedPath-name: ";

   /** Contains GCode or GArc or GSpline. Allways start with a GCode or is empty */
    ArrayList<Object> gString; 
    private Rectangle2D bounds;
    private int nbPoints = -1;

    public GMixedPathPath(String name0) {
        super(name0);
        gString = new ArrayList<>();
    }
    
    @Override
    public boolean add(GCode gCode) {
        add( gString.size(), gCode);
        return true;
    }

    @Override
    public void add(int pos, GCode gCode) {
        GElement e = null;
        if ( gCode.isAnArc()) 
            e = new GArc(null, getLastPoint(), gCode);
        if (gCode.isASpline()) 
             e = new GSpline(null, getLastPoint(), gCode);
        if ( gCode.getG()==0) 
            gCode.setG(gString.isEmpty()?0:1);
            
        if ( (pos == 0) && (e!=null)) {
            GCode p = getFirstPoint();
            gString.add(0, (p==null)?new GCode(0,0):new GCode(p.getX(),p.getY()));
            gString.add( 1, e);
        } else {
            if ( e != null) gString.add( pos, e);
            else gString.add(pos, gCode);
        }      
        informAboutChange();
    }
    
    public boolean add(GElement circleOrCurve) {
       
        return concat(circleOrCurve, 0.0000001);
    }
    
    public void addAll(ArrayList<GCode> copy) {
        copy.forEach((l) -> { add(l); });
    }

    @Override
    public GElement clone() {
        GMixedPathPath clone = new GMixedPathPath(name);
        clone.properties = properties.clone();
        gString.forEach((e) -> { 
            Object c;
            if ( e instanceof GArc) c = ((GArc)e).clone();
            else if ( e instanceof GSpline) c = ((GSpline)e).clone();
            else c = ((GCode)e).clone();
            clone.gString.add( c);
        });
        return clone;
    }
    
    @Override
    public boolean concat(GElement gElement, double dmax) {
        if (gElement instanceof GArc) return false;
        if (( gElement instanceof GSpline)) {
            final GCode p = gElement.getFirstPoint();
            if ( gString.isEmpty() || (getLastPoint()==null) || (getLastPoint().distance(p) > dmax)) {
                gString.add(new GCode(p));
            }
            gString.add( gElement);

        } else if ( gElement instanceof GMixedPathPath) {
            for( Object o : ((GMixedPathPath)gElement).gString)
                gString.add(o);
            return true;
            
        } else if ( gElement instanceof G1Path) {
                    boolean first = true;
                    for( GCode l : (G1Path)gElement) {
                        if ( first) {
                            if ( gString.isEmpty() || (getLastPoint()==null) || (getLastPoint().distance(l) > dmax)) add(l);
                            first = false;
                        } else gString.add(l); 
                    }
                }
                else 
                    return false;
        
        informAboutChange();
        return true;
    }
    
    @Override
    public boolean contains(GCode point) {
        for (Object o : gString) {
            if (o instanceof GCode) {
                if (((GCode)o).isAtSamePosition(point)) return true;
            } else 
                if (((GElement)o).contains(point)) return true;
        }        
        return false;
    }
    
    @Override
    public GElement flatten() {
        
        G1Path f = new G1Path("flatten-"+name);
        gString.forEach((o) -> {
            if (o instanceof GCode) f.add((GCode)o);
            else f.concat(((GElement)o).flatten(), 0.0002);
        });      
        return f;
    }
    
    @Override
    public Rectangle2D getBounds() {    
        if ( bounds == null) {
            if ( getNbPoints() == 0) return null;
            gString.forEach((o) -> {
                if ( (o instanceof GCode) && ((GCode)o).isAPoint()) {
                    if ( bounds == null) 
                        bounds = new Rectangle2D.Double(((GCode)o).getX(), ((GCode)o).getY(), 0.00001, 0.00001);
                    else bounds.add((GCode)o);
                } else
                    if ( bounds == null) bounds = ((GElement)o).getBounds();
                    else bounds.add( ((GElement)o).getBounds());
            });
        }             
        
        return (Rectangle2D) bounds.clone();     
    }

    @Override
    public Point2D getCenter() {
        if ( getBounds() != null)
            return new GCode(bounds.getCenterX(), bounds.getCenterY());
        else 
            return null;
    }

    @Override
    public GCode getCloserPoint(Point2D from, double dmax, ArrayList<GCode> discareIt, boolean excludeFirst) {
        GCode res = null;
        double d;
        for ( Object o : gString) {
            if ( o instanceof GCode) {
                if (((GCode)o).isAPoint()) {
                    if ( ! excludeFirst && ! ((discareIt!=null) && discareIt.contains((GCode)o)) && (d=((GCode)o).distance(from)) < dmax) {
                        res = (GCode)o;
                        dmax = d;
                    }
                    excludeFirst=false;
                }
            } else {
                GCode pt = ((GElement)o).getCloserPoint(from, dmax, discareIt, excludeFirst);
                if ( (pt != null) && (d=(pt.distance(from)))< (dmax+1)) {
                    dmax = d;
                    res = pt;
                }
            }
        }
        return res;
    }
    
    /**
     * Return a Segment2D or null if on a GArc or GSPline (can add point on them)
     * @param point
     * @return 
     */
    private Object getClosestSegmentTo(GCode point) {
        GCode lastPoint = null;
        double d, dmin = Double.MAX_VALUE;
        Segment2D s;
        Object res = null;
        for ( Object o : gString) {
            if ( (o instanceof GCode) && ((GCode)o).isAPoint()) { 
                if (( lastPoint != null) && (d=(s=new Segment2D(lastPoint, (GCode)o)).distanceTo(point)) < dmin ) {
                    dmin = d;
                    res = s;
                }
                lastPoint = (GCode)o;
            } else {
                if ( (d=((GElement)o).getDistanceTo(point)) < dmin) {
                    dmin = d;
                    res = o;
                }
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
        return res;
    }
    
    @Override
    public double getDistanceTo(GCode point) {
        //if (point == null) return Double.POSITIVE_INFINITY;
        double d, mind = Double.POSITIVE_INFINITY;

        Segment2D s;
        GCode lastPoint = null;
        for( Object o : gString) {
            if (o instanceof GCode) {
                if (((GCode)o).isAPoint()) {
                    if ( lastPoint != null) {
                        s = new Segment2D(lastPoint, (GCode)o);
                        d = Math.abs(s.distanceTo(point));
                        if ( mind > d) mind = d;
                    }
                    lastPoint = (GCode)o;
                }
            } else {
                if ( (d=((GElement)o).getDistanceTo(point)) < mind) {
                    mind = d;
                }
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
        return mind;
    }


    @Override
    public GCode getElementAt(int index) {
        if ( index < gString.size()) {         
            Object o = gString.get(index);
            if ( o instanceof GCode) return (GCode)o;
            else return (GCode)((GElement)o).getElementAt(1);
        } else 
            return new GCode();
    }

    @Override
    public GCode getFirstPoint() {
        for( Object o : gString) {
            if ( (o instanceof GCode)  && ((GCode)o).isAPoint()) 
                return (GCode)o;
            else {
                GCode p = ((GElement)o).getFirstPoint();
                if ( p != null) 
                    return p;
            }
        }
        return null;
    }
    
    private Point2D getFirstPointFor(Object o) {
        for(int i = gString.indexOf(o)-1 ; i > 0; i -- ) {
            o = gString.get(i);
            if ((o instanceof GCode)) {
                if (((GCode)o).isAPoint()) return (GCode)o;
            } else return ((GElement)o).getLastPoint();
        }
        return null;
    }

    
    private int getIndexOfLine(GCode l) {
        final int size = size();
        for( int i = 0; i < size; i++)
            if( getElementAt(i).equals(l)) return i;
        return -1;
    }
    
    @Override
    public int getIndexOfPoint(GCode point) {
        final int size = getSize();
        for(int i = 0; i < size; i++ )
            if ( getElementAt(i) == point) 
                return i;
        return -1;
    }
    
    @Override
    public GCode getLastPoint() {
        GCode l;
        for(int i = gString.size(); i > 0; ) {
            final Object o = gString.get(--i);
            if (o instanceof GCode) {
                if (((GCode)o).isAPoint()) return (GCode)o;
            } else
                return ((GElement)o).getLastPoint();
        }
        return null;
    }

    @Override
    double getLength() {
        double res = 0;
        GCode lastPoint = null;
        for(Object o : gString) {
            if ( o instanceof GCode) {
                if (((GCode) o).isAPoint()) {
                    if ( lastPoint != null) res += lastPoint.distance((GCode)o);
                    lastPoint = (GCode)o;
                }
            } 
            else {
                res += ((GElement)o).getLength();
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
        return res;
    }
    
    
    
    @Override
    public double getLenOfSegmentTo(GCode point) {
        GCode p2;
        GCode lastPoint = null;
        for ( Object o : gString) {
            if ( (o instanceof GCode)) {
                    if (o == point) {
                        if ( lastPoint == null) return 0;
                        return lastPoint.distance(point);
                    }
                    if ( ((GCode)o).isAPoint()) lastPoint = (GCode)o;
            } else {
                final GElement e = (GElement)o;
                if ( (e.getLastPoint()) == point) 
                    return e.getLength();
                
                lastPoint = e.getLastPoint();
            }
        }
        return 0;
    }    
    
    @Override
    public GCode getLine(int i) {
        return getElementAt(i);
    }

    @Override
    public ArrayList<GCode> getLines(int[] indices) {
        ArrayList<GCode> res = new ArrayList<>(indices.length);
        int lastIndice = -1;
        for ( int i : indices) {
            Object o;
            if ( (o=gString.get(i)) instanceof GCode) {
                res.add((GCode)o);
                lastIndice = i;
            }
            else {
                if ( res.isEmpty() || (lastIndice != i-1)) {
                    final GCode pt = ((GElement)o).getFirstPoint();
                    res.add(new GCode(0,pt.getX(), pt.getY()));
                }
                res.add((GCode)((GElement)o).getElementAt(1));
            }
        }
        return res;
    }
    
    public ArrayList<GCode> getLines(ArrayList<GCode> lines) {
        int indices[] = new int[lines.size()];
        int i = 0;
        for( GCode l : lines) indices[i++] = getIndexOfLine(l);
        return getLines(indices);
    }
    
    @Override
    public int getNbPoints() {
        if ( nbPoints != -1) return nbPoints;
        else {
            nbPoints = 0;
            for( int i = 0; i < size(); i++)
                if ( getElementAt(i).isAPoint()) nbPoints++;
        }
        return nbPoints;
    }
    
    @Override
    public Area getOffsetArea(double distance) {
        if ( getNbPoints() < 2) return null;
        final Area a = new Area();
        GCode lastPoint = null;
        boolean lastIsAPoint = false;

        for ( Object o : gString)
            if ( o instanceof GCode) {
                if ( lastPoint != null) {
                    a.add( new Area(new Ellipse2D.Double(lastPoint.getX()-distance, lastPoint.getY()-distance, 2*distance, 2*distance)));
                    a.add(new Area(G1Path.getLineOffset(lastPoint, (GCode)o, distance)));
                    
                }
                lastPoint = (GCode)o;
                lastIsAPoint = true;
            } else {
                if ( (lastPoint != null) && lastIsAPoint) {
                    a.add( new Area(new Ellipse2D.Double(lastPoint.getX()-distance, lastPoint.getY()-distance, 2*distance, 2*distance)));
                    a.add( new Area(G1Path.getLineOffset(lastPoint, ((GElement)o).getFirstPoint(), distance)));
                }
                a.add( ((GElement)o).getOffsetArea(distance));
                lastPoint = ((GElement)o).getLastPoint();
                lastIsAPoint = false;
            }
       
        if ( ! isClosed())
            a.add( new Area(new Ellipse2D.Double(lastPoint.getX()-distance, lastPoint.getY()-distance, 2*distance, 2*distance)));
        
        return a;        
    }

    @Override
    public Iterable<GCode> getPointsIterator() {
        return () -> new Iterator<GCode>() {
            int nbl=gString.size(), n=0;
            private Iterator<GCode> i;
            
            @Override
            public boolean hasNext() {
                return (n < nbl) || ((i != null) && i.hasNext());
            }
            @Override
            public GCode next() {
                if ( (i != null) && (i.hasNext())) return i.next();
                i=null;
                GCode p = null;
                while( n < nbl ) {
                    Object o = gString.get(n++);
                    if ( o instanceof GCode) {
                        if( ((GCode)o).isAPoint()) return (GCode)o;
                    } else {
                        i = ((GElement)o).getPointsIterator().iterator();
                        return i.next();
                    }
                }
                return null;
            }
        };
    }
    
    @Override
    public int getSize() {
        return size() + 1;
    }
    
    @Override
    public String getSummary() {
        String res = "<html><b>ComposedPath</b><br>";
        for( int nb = 0; nb < size(); nb++) {
            res += getElementAt(nb++) + "<br>";
            if ( nb>=20) {
                res += "...</html>";
                break;
            }
        }
        return res;
    }

    @Override
    protected void informAboutChange() {
        nbPoints=-1;
        bounds = null;
        validatePath();
        super.informAboutChange();
    }  
    
    /** Verify the coherance of the path. */
    protected void validatePath() {
        GCode lastPoint, firstPoint;
        boolean verified;
        do {
            verified = true;
            lastPoint = null;
            firstPoint = null;
            for ( Object o : gString) {
                if ( o instanceof GCode) {
                    if (((GCode)o).isAPoint()) {
                        if ((lastPoint!=null) && ((GCode)o).isAtSamePosition(lastPoint)) {
                            gString.remove(o);
                            verified=false;
                            break;
                        }
                        if ( firstPoint == null)
                            (firstPoint=(GCode)o).setG(0);
                        else if ( firstPoint != o) ((GCode)o).setG(1);
                        lastPoint = (GCode)o;
                    }                   
                } else {
                    if ( (o instanceof GSpline) && ((GSpline)o).isALine()) {
                        // transform G5 into G1
                        gString.set( gString.indexOf(o), new GCode(((GSpline)o).getLastPoint()));
                        verified=false;
                        break;
                    } else if ( (lastPoint == null) || 
                            ! ((GElement)o).getFirstPoint().isAtSamePosition(lastPoint)) {
                        if (lastPoint == null)  // insert a G0 line at start of the path
                            gString.add(0, ((GElement)o).getFirstPoint().clone());
                        else {
                            if (((GElement)o).getFirstPoint().distance(lastPoint) < 0.2)
                                ((GElement)o).getFirstPoint().set(lastPoint);
                            else {
                                // add a G1 path to go to the start of 'o'
                                final GCode pt = ((GElement)o).getFirstPoint();
                                gString.add(gString.indexOf(o), new GCode(1, pt.getX(), pt.getY()));
                            }
                            
                        }
                        
                        verified = false;
                        break;
                    } 
                    lastPoint = ((GElement)o).getLastPoint();
                }         
            }
        } while ( ! verified);
    }
    
    @Override
    public GCode insertPoint(GCode point) {
        Object bestSegment = getClosestSegmentTo(point);

        if ( bestSegment != null) {
            GCode newP = new GCode(point); 
            if (bestSegment instanceof Segment2D) {
                int i = getIndexOfPoint(((Segment2D)bestSegment).p2);
                final GSpline newC = new GSpline("", ((Segment2D)bestSegment).p1.clone(), point, ((Segment2D)bestSegment).p2.clone());
                gString.set( i, newC);
                informAboutChange();
                return point;
            } else 
                if (((GElement)bestSegment).add(point)) 
                    return point;
                else if ( bestSegment instanceof GSpline) {
                    // Split SPLine
                    final GSpline sp = ((GSpline)bestSegment);
                    final GSpline newC = new GSpline("", new GCode(sp.getFirstPoint()), new GCode(sp.cp1), new GCode(point));
                    int i = gString.indexOf(bestSegment);
                    gString.add( i, newC);
                    if ( sp.cp2 != null) {
                        sp.cp1 = sp.cp2;
                        sp.cp2 = null;
                    }
                    sp.setLine(0, point);
                    informAboutChange();
                    return newC.getLastPoint();
                }
        }
        return null;
    }
    
    @Override
    public boolean isClosed() {
        return getFirstPoint().isAtSamePosition(getLastPoint());
    }

    @Override
    public boolean isEmpty() {
        return gString.isEmpty();
    }

    @Override
    public Iterator<GCode> iterator() {
        Iterator<GCode> it = new Iterator<GCode>() {
            private GMixedPathPath path;
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < gString.size();
            }
            @Override
            public GCode next() {
                return getElementAt(currentIndex++);
            }
            @Override
            public void remove() {
                path.remove(currentIndex);
            }
            public Iterator<GCode> setPath( GMixedPathPath p) {
                path=p;
                return this;
            }
        }.setPath(this);
        return it;
    }

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        GCode lastPoint=lastGState.clone();
        GElement e;
        while( ( line != null)  && ! GElement.isGElementHeader(line)) {
            GCode gcl = new GCode( line);
            switch ( gcl.getG()) {
                case 0:
                    if ( ! gString.isEmpty()) return line;
                case 1: 
                    if ( gcl.isAPoint()) lastPoint = gcl;
                    add( gcl);
                    break;
                case 2:
                case 3: 
                    add(e = new GArc("", lastPoint, gcl));
                    lastPoint = e.getLastPoint();
                    break; 
                case 5:
                    add(e = new GSpline("", lastPoint, gcl));
                    lastPoint = e.getLastPoint();
                    break;
                default:
                    return line;
            }
            line = stream.readLine();
        }
        return line;
    }
    
    
    /**
     * Make a new GMixedPath from a GSPline, G1Path or GMixedPath
     * @param s1
     * @return 
     */
    public static GMixedPathPath makeFromGElement(GElement s1) {
        if ( s1 instanceof GMixedPathPath) return (GMixedPathPath)s1.clone();
        GMixedPathPath res = new GMixedPathPath((s1.getName()));
        res.properties = s1.properties.clone();
        if ( s1 instanceof GSpline) {
            res.add( ((GCode)s1.getElementAt(0)).clone());
            res.add( ((GCode)s1.getElementAt(1)).clone());
        }
        else if ( s1 instanceof G1Path)
                for( GCode p : s1)
                    res.add(p.clone());
        else 
            return null;
        
        return res;
    }

 
    
    @Override
    public boolean movePoints(ArrayList<GCode> points, double dx, double dy) {
        boolean moved = false;
        for ( GCode p : points) {
            boolean moveNext = false; // move start of next object
            for( Object o : gString) {
                if (o instanceof GCode) {
                    
                    if ( o == p) {
                        p.translate(dx, dy);
                        moveNext = true;
                        moved = true;
                    } else 
                        if ( ((GCode) o).isAPoint()) 
                            moveNext = false;
                } else {
                    if ( ! moveNext ) {
                        final GElement e = (GElement)o;
                        if (((GElement)o).contains(p)) {
                            if (((GElement)o).movePoint(p, dx, dy)) {
                            moved = true;    
                            if ( ((GElement)o).getLastPoint() == p) 
                                moveNext=true; // if next is not a point
                            }
                        }
                    } else {
                        if ( points.contains(((GElement)o).getFirstPoint())) {
                            moveNext=false;
                            break;
                        }
                        if ( o instanceof GSpline) {
                            ((GSpline)o).translateFirstPoint(dx, dy);
                           
                        } else if ( o instanceof GArc) {
                            ((GArc)o).translateFirstPoint(dx, dy);
                        }
                        moved = true;
                        moveNext = false;
                        break;
                    }
                }
            }
        }
        if ( moved) informAboutChange();
        return moved;
    }
    
    @Override
    public void paint(PaintContext pc) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g; 
        int oldx=Integer.MAX_VALUE, oldy=Integer.MAX_VALUE;
        boolean oldShowStartPoint = pc.showStartPoints;
        
        if ( pc.editedElement == this) {      
            if ( !pc.selectedPoints.isEmpty()) {
                g.setColor(Color.yellow);
                for ( GCode p : pc.selectedPoints) {
                    int x = (int)(p.getX()*zoomFactor);
                    int y = -(int)(p.getY()*zoomFactor);
                    g.fillOval(x-3,y-3, 6, 6);
                }
            }
            if ( pc.highlitedPoint != null) {
                g.setColor(Color.white);
                final int x = (int)(pc.highlitedPoint.getX()*zoomFactor);
                final int y = -(int)(pc.highlitedPoint.getY()*zoomFactor);
                g.fillOval(x-3,y-3, 6, 6);
            }
        }
        
        for( int n = 0; n < size(); n++) {
            Object o = gString.get(n);
            if (o instanceof GCode) {
                if ( ((GCode)o).isAPoint()) {
           
                    int x = (int)(((GCode)o).getX()*zoomFactor);
                    int y = -(int)(((GCode)o).getY()*zoomFactor);

                    g.setColor(pc.color);
                    // draw segment
                    if ( oldx==Integer.MAX_VALUE) {
                        oldx=x;
                        oldy=y;
                        // first point
                        if ( (pc.color != Color.darkGray) && pc.showStartPoints) {
                            g.setColor(Color.red); 
                            g.drawRect(x-3, y-3, 6, 6);
                            pc.showStartPoints = false;
                        }
                    } 
                    else {
                        if ( pc.selectedPoints.contains((GCode)o)) g.setColor(Color.WHITE);
                        else g.setColor(pc.color);
                        g.drawLine(oldx, oldy, oldx=x, oldy=y);
                    }
                    pc.lastPoint = (GCode)o;
                }
            } else {
                pc.lastPoint = ((GElement)o).getLastPoint();
                Color old = pc.color;
                if ( pc.selectedPoints.contains(pc.lastPoint)) pc.color=Color.YELLOW;
                ((GElement)o).paint(pc);
                pc.color = old;
                oldx = (int)(pc.lastPoint.getX()*zoomFactor);
                oldy = -(int)(pc.lastPoint.getY()*zoomFactor);
            }
        }
        pc.showStartPoints = oldShowStartPoint;
    }

    @Override
    public Object remove(int i) {
        if ( i == -1) return null;
        
        Object res = gString.remove(i);
        informAboutChange();
        return res;
    }

    @Override
    public void removeAll(ArrayList<GCode> lines) {
        for ( GCode l : lines) {
            for( int i = 0; i < gString.size(); i++) {
                Object o;
                if ( (o=gString.get(i)) == l) {
                    gString.remove(i);
                    break;
                }
                else if ( ! (o instanceof GCode))
                    if ( ((GElement)o).getLastPoint() == l) {
                        gString.remove(i);
                        break;
                    }
            }
        }
        informAboutChange();
    }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reverse() {
        ArrayList<Object> inv = new ArrayList<>(gString.size());
        for( int i = size(); i > 0; ) {
            inv.add( gString.get(--i));
        }
        gString = inv;
        informAboutChange();
    }

    @Override
    public void rotate(Point2D origin, double angle) {
        gString.forEach((o) -> {
            if ( o instanceof GCode) ((GCode)o).rotate(origin, angle);
            else ((GElement)o).rotate(origin, angle);
        });
        informAboutChange();
    }

    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        for ( Object o : gString)  {
            if ( o instanceof GCode) {
                fw.append(o.toString()+"\n");
                if ( ((GCode)o).isAPoint()) lastPoint = (GCode)o;
            }
            else {
                fw.append(((GElement)o).getElementAt(1)+"\n");
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
        return lastPoint;
    }


    @Override
    public void scale(Point2D origin, double sx, double sy) {
        gString.forEach((o) -> {           
            if ( o instanceof GCode) {
                final GCode p = (GCode)o;
                if ( p.isAPoint()) {
                    double d = origin.distance(p);
                    double a = getAngle(origin, p);
                    p.setLocation( origin.getX() + Math.cos(a)*d*sx,
                                    origin.getY() + Math.sin(a)*d*sy);
                }
            } else ((GElement)o).scale(origin, sx, sy);
        });
        informAboutChange();
    }

    @Override
    public void simplify(double angleMin, double distanceMax) { }

    @Override
    public void setLine(int row, GCode value) {
        Object o = gString.get(row);
        
        if ((o instanceof GCode) && ! (value.isASpline() | value.isAnArc()) ) {
            if ( value.isAPoint()) value.setG(1);
            ((GCode)o).set(value);
        }
        else {
            final Point2D p = getFirstPointFor(o);
            if ( o == null) // no first point => convert to a simple final point
                gString.set( row, new GCode(1, p.getX(), p.getX()));
            else 
            if ( value.isASpline()) {
                if (o instanceof GSpline) ((GElement)o).setLine(1, value);
                else {
                    gString.set(row, new GSpline("", p, value));
                }
            } else if ( value.isAnArc()) {
                if (o instanceof GArc) ((GElement)o).setLine(1, value);
                else {                   
                    gString.set(row, new GArc("", p, value));
                }
            }
        }
        informAboutChange();
    }

    @Override
    public int size() {
        return gString.size();
    }

    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        GCode lastPoint = null;
        for( Object o : gString ) {
            if ( o instanceof GCode) {
                if ( ((GCode)o).isAPoint()) {
                    final GCode p = (GCode)o;
                    if ( lastPoint != null) {
                        out.write("0\nLINE\n");
                        out.write("8\n1\n");  // layer 1
                        out.write("10\n"+lastPoint.getX()+"\n");
                        out.write("20\n"+-lastPoint.getY()+"\n");
                        out.write("11\n"+p.getX()+"\n");
                        out.write("21\n"+-p.getY()+"\n");
                    }
                    lastPoint = (GCode)o;
                }
            } else {
                ((GElement)o).toDXF(out);
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
    }

    @Override
    public String toString() {
        return getName() + "(Mixed)";
    }

        
    @Override
    public void translate(double dx, double dy) {
        gString.forEach((o) -> {
            if ( o instanceof GCode) ((GCode)o).translate(dx, dy);
            else ((GElement)o).translate(dx, dy);
        });
        informAboutChange();
    }

    public boolean changeFirstPoint(GCode p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void transform(AffineTransform t) {
        gString.forEach((o) -> {
            if ( o instanceof GCode) ((GCode)o).transform( t);
            else if ( o instanceof GElement) ((GElement)o).transform( t);
            else
                 throw new AbstractMethodError( "" + o.getClass() + ".transform(t) not implemented");
        });
    }

}
