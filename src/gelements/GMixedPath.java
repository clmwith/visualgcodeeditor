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
 * @author Clément
 */
public class GMixedPath extends GElement {
    
    public static final String HEADER_STRING = "(ComposedPath-name: ";

   /** Contains GCode or GArc or GSpline. Always start with a GCode or is empty */
    ArrayList<Object> gContent; 
    
    private Rectangle2D bounds;
    private int nbPoints = -1;

    public GMixedPath(String name0) {
        super(name0);
        gContent = new ArrayList<>();
    }
    
    @Override
    public boolean add(GCode gCode) {
        add(gContent.size(), gCode);
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
            gCode.setG(gContent.isEmpty()?0:1);
            
        if ( (pos == 0) && (e!=null)) {
            GCode p = getFirstPoint();
            gContent.add(0, (p==null)?new GCode(0,0):new GCode(p.getX(),p.getY()));
            gContent.add( 1, e);
        } else {
            if ( e != null) gContent.add( pos, e);
            else gContent.add(pos, gCode);
        }      
        informAboutChange();
    }
    
    public boolean add(GElement circleOrCurve) {
       
        return concat(circleOrCurve, 0.0000001);
    }
    
    public void addAll(ArrayList<GCode> copy) {
        copy.forEach((l) -> { add(l); });
    }

    /**
     * Generate a clone of this path
     * @return
     */
    @Override
    public GElement clone() {
        GMixedPath clone = new GMixedPath(name);
        clone.properties = properties.clone();
        gContent.forEach((e) -> { 
            Object c;
            if ( e instanceof GArc) c = ((GArc)e).clone();
            else if ( e instanceof GSpline) c = ((GSpline)e).clone();
            else c = ((GCode)e).clone();
            clone.gContent.add( c);
        });
        return clone;
    }
    
    @Override
    public boolean concat(GElement gElement, double dmax) {
        final GCode p = gElement.getFirstPoint();
        if (p == null) return false;
        
        GCode lp = getLastPoint();
        if (lp == null) gContent.add( lp = new GCode(p.getX(), p.getY()));
        else if ( lp.distance( p) > dmax) return false;
        
        if (gElement instanceof GArc) {
            gContent.add( gElement);
            
        } else if (( gElement instanceof GSpline)) {
            if ( gContent.isEmpty() || (getLastPoint()==null) || (getLastPoint().distance(p) > dmax)) {
                gContent.add(new GCode(p));
            }
            gContent.add( gElement);

        } else if ( gElement instanceof GMixedPath && (gElement.getFirstPoint().distance(getLastPoint()) < dmax)) {
            for( Object o : ((GMixedPath)gElement).gContent)
                gContent.add(o);
            
        } else if ( gElement instanceof G1Path) {
            boolean first = true;
            for( GCode l : (G1Path)gElement) {
                if ( first) first = false;
                else gContent.add(l); 
            }
        } else 
            return false;
        
        informAboutChange();
        return true;
    }
    
    @Override
    public boolean contains(GCode point) {
        for (Object o : gContent) {
            if (o instanceof GCode) {
                if (((GCode)o).isAtSamePosition(point)) return true;
            } else 
                if (((GElement)o).contains(point)) return true;
        }        
        return false;
    }
    
    @Override
    public G1Path flatten() {
        
        G1Path f = new G1Path("flatten-"+name);
        gContent.forEach((o) -> {
            if (o instanceof GCode) f.add((GCode)o);
            else f.concat(((GElement)o).flatten(), 0.0002);
        });      
        return f;
    }
    
    @Override
    public Rectangle2D getBounds() {    
        if ( bounds == null) {
            if ( getNbPoints() == 0) return null;
            gContent.forEach((o) -> {
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
        for ( Object o : gContent) {
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
        for ( Object o : gContent) {
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
        for( Object o : gContent) {
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
    public GCode getElementAt(int line) {
        if ( line < gContent.size()) {        
        Object o = gContent.get(line);
        if ( o instanceof GCode) return (GCode)o;
        else return (GCode)((GElement)o).getElementAt(1);        
        }
        return new GCode(); // for the last line of the editor
    }

    @Override
    public GCode getFirstPoint() {
        for( Object o : gContent) {
            if (o instanceof GCode) {
                if (((GCode)o).isAPoint()) return (GCode)o;
            } else {
                GCode p = ((GElement)o).getFirstPoint();
                if ( p != null) 
                    return p;
            }
        }
        return null;
    }
    
    private Point2D getFirstPointFor(Object o) {
        for(int i = gContent.indexOf(o)-1 ; i > 0; i -- ) {
            o = gContent.get(i);
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
        for(int i = gContent.size(); i > 0; ) {
            final Object o = gContent.get(--i);
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
        for(Object o : gContent) {
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
        for ( Object o : gContent) {
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
            if ( (o=gContent.get(i)) instanceof GCode) {
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

        for ( Object o : gContent)
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
        return new Iterable<GCode>() {
                        GMixedPath me;
                        public Iterable<GCode> setMe( GMixedPath m) { return me = m; }            
                        @Override
                        public Iterator<GCode> iterator() { return me.iterator(); }            
                    }.setMe(this);
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
        revalidate();
        super.informAboutChange();
    }  
    
    /** Verify the coherance of the path. */
    public void revalidate() {
        GCode lastPoint, firstPoint;
        boolean verified;
        do {
            verified = true;
            lastPoint = null;
            firstPoint = null;
            for ( Object o : gContent) {
                if ( o instanceof GCode) {
                    if (((GCode)o).isAPoint()) {
                        if ((lastPoint!=null) && ((GCode)o).isAtSamePosition(lastPoint)) {
                            gContent.remove(o);
                            verified=false;
                            break;
                        }
                        if ( firstPoint == null)
                            (firstPoint=(GCode)o).setG(0);
                        else if ( firstPoint != o) ((GCode)o).setG(1);
                        lastPoint = (GCode)o;
                    }                   
                } else { 
                    final GCode fp = ((GElement)o).getFirstPoint();
                    if (lastPoint == null)  // insert a G0 line at start of the path
                            gContent.add(0, lastPoint = fp.clone());
                    
                    if ( ! lastPoint.isAtSamePosition(fp)) {
                        gContent.add(gContent.indexOf(o), new GCode(1, fp.getX(), fp.getY()));
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
                gContent.add(i, new GCode(1, point.getX(), point.getY()));
                /*
                final GSpline newC = new GSpline("", ((Segment2D)bestSegment).p1.clone(), point, ((Segment2D)bestSegment).p2.clone());
                gContent.set( i, newC);
                gContent.get(i);*/
                informAboutChange();
                return point;
            } else 
                if (((GElement)bestSegment).add(point)) 
                    return point;
                else if ( bestSegment instanceof GSpline) {
                    // Split SPLine
                    final GSpline sp = ((GSpline)bestSegment);
                    final GSpline newC = new GSpline("", new GCode(sp.getFirstPoint()), new GCode(sp.cp1), new GCode(point));
                    int i = gContent.indexOf(bestSegment);
                    gContent.add( i, newC);
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
        return gContent.isEmpty();
    }

    @Override
    public Iterator<GCode> iterator() {
        Iterator<GCode> it = new Iterator<GCode>() {
            private GMixedPath path;
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < gContent.size();
            }
            @Override
            public GCode next() {
                return getElementAt(currentIndex++);
            }
            @Override
            public void remove() {
                path.remove(currentIndex);
            }
            public Iterator<GCode> setPath( GMixedPath p) {
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
                    if ( ! gContent.isEmpty()) return line;
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
    public static GMixedPath makeFromGElement(GElement s1) {
        if ( s1 instanceof GMixedPath) return (GMixedPath)s1.clone();
        GMixedPath res = new GMixedPath((s1.getName()));
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
    public boolean movePoint(GCode point, double dx, double dy) {
        ArrayList<GCode> l = new ArrayList<>();
        l.add(point);
        return movePoints(l, dx, dy);
    }

    
    @Override
    public boolean movePoints(ArrayList<GCode> points, double dx, double dy) {
        boolean moved = false;
        
        // if not null, contains the element where getLastPoint == lastPoint, else lastPoint is a G1 code
        GElement last = null; 
        
        for ( GCode p : points) {
            boolean moveNext = false; // used to move start of next object
            GCode lastPoint = null; 
            GCode lastRealPoint = null;
            
            for( Object o : gContent) {
                if (o instanceof GCode) {
                    
                    if ( moveNext) break; // point already moved and no linked point, goto next
                        
                    if ( o == p) {
                        GCode old = p.clone();
                        p.translate(dx, dy);
                        moved = true;
                        
                        // move linked point ?
                        if ( (lastPoint != null) && lastPoint.isAtSamePosition(p)) {
                            if ( last != null) last.movePoint(lastPoint, dx, dy);
                            else lastPoint.translate(dx, dy);
                            break;
                        }      
                        moveNext = true;
                        lastPoint = old;
                    } else 
                        if ( ((GCode) o).isAPoint())  {
                            moveNext = false;
                            last = null;
                            lastPoint = (GCode)o;
                        }
                } else {
                    if ( ! moveNext ) {
                        if (((GElement)o).contains(p)) {
                            GCode old = ((GElement)o).getLastPoint().clone();
                            
                            if (((GElement)o).movePoint(p, dx, dy)) {
                                moved = true; 
                                
                                if ( ((GElement)o).getFirstPoint() == p) {                             
                                    // try to move linked point
                                    if ( (lastPoint != null) && lastPoint.isAtSamePosition(old)) {
                                        if ( last == null) lastPoint.translate(dx, dy);
                                        else last.movePoint( lastPoint, dx, dy);
                                    }
                                    break;
                                    
                                } else if ( (o instanceof GArc) || ( ((GElement)o).getLastPoint() == p)) {
                                    moveNext=true; // move linked point
                                    lastPoint = old;
                                    if ( o instanceof GArc) lastRealPoint = ((GElement)o).getLastPoint();
                                    else lastRealPoint = null;
                                    
                                }
                                
                            } else {
                                // not the end point of this element, moving next time
                                old = ((GElement)o).getLastPoint();
                                if ( old.isAtSamePosition( p)) {
                                    last = (GElement)o;
                                    lastPoint = old;
                                    moveNext = false;
                                }
                            }
                        }
                    } else {
                        // move the point linked to the current already moved point
                        if ( !((GElement)o).getFirstPoint().isAtSamePosition(lastPoint)) break;
                        
                        double tdx = (lastRealPoint==null) ? dx : lastRealPoint.getX() - lastPoint.getX();
                        double tdy = (lastRealPoint==null) ? dy : lastRealPoint.getY() - lastPoint.getY();
                        if ( o instanceof GSpline) {
                            ((GSpline)o).translateFirstPoint(tdx, tdy);                        
                        } else if ( o instanceof GArc) {
                            ((GArc)o).translateFirstPoint(tdx, tdy);
                        } else
                            throw new UnsupportedOperationException("GMixed: can't move first point of class" + o.getClass());
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
            Object o = gContent.get(n);
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
                        
                        if ( pc.editedElement == this)
                            g.drawOval((int)x-3,(int)y-3, 6, 6);  
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
        return remove( null, i);        
    }

    
    GCode remove( GCode thisLine, int orThisPosition) {
        GCode res = null;
        GCode lastPoint = null;
        boolean moveNext = false;
        
        for( int pos = 0; pos < gContent.size(); pos++) {
            Object o = gContent.get(pos);
            if ( moveNext) {
                if ( lastPoint == null) {
                    // set new G0
                    if ( o instanceof GCode) ((GCode)o).setG(0);
                    else {
                        // remplace GArc/GSpline by his destination
                        GCode tmp = ((GElement)o).getLastPoint();
                        gContent.set(0, new GCode(0, tmp.getX(), tmp.getY()));
                    }                    
                } else {
                    // move start of the move after deleted one
                    if ( ! (o instanceof GCode)) {
                        final GCode p = ((GElement)o).getFirstPoint();                    
                        double dx = lastPoint.getX()- p.getX() ;                    
                        double dy = lastPoint.getY() - p.getY();               
                        ((GElement)o).movePoint(p, dx, dy);
                    }
                }  
                break;
            }
            
            if (o instanceof GCode) {
                if ((o == thisLine) || (pos == orThisPosition)) {
                    if ( gContent.remove(o)) {                        
                        res = (GCode)o;
                        moveNext = true;
                        pos--;
                        continue;
                    } 
                }
                lastPoint = (GCode)o;
                
            } else {
                if ((pos == orThisPosition) || (((GElement)o).getLastPoint() == thisLine)) {
                    gContent.remove(pos);
                    moveNext = true;
                    pos--;
                    continue;
                }
                lastPoint = ((GElement)o).getLastPoint();
            }
        }
            
        if ( res != null) informAboutChange();
        return res;
    }
    
    
    @Override
    public void removeAll(ArrayList<GCode> lines) {
        for ( GCode l : lines) remove(l, -1);
        
        informAboutChange();
    }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reverse() {
        ArrayList<Object> inv = new ArrayList<>(gContent.size());
        for( int i = size(); i > 0; ) {
            inv.add(gContent.get(--i));
        }
        gContent = inv;
        informAboutChange();
    }

    @Override
    public void rotate(Point2D origin, double angle) {
        gContent.forEach((o) -> {
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
        for ( Object o : gContent)  {
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
        gContent.forEach((o) -> {           
            if ( o instanceof GCode) {
                final GCode p = (GCode)o;
                if ( p.isAPoint()) {
                    double d = origin.distance(p);
                    double a = getAngleInRadian(origin, p);
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
       
        if (row == 0) {
            // first point must be G0 point with (X,Y)
            GCode v = new GCode(value);
            if (v.isAPoint() && (v.getG()==0)||(v.getG()==1)) gContent.set(0, new GCode(0,v.getX(), v.getY()));
            return;
        }
        
        Object o = gContent.get(row);      
        if (o instanceof GCode) {
            if ( ! (value.isASpline() || value.isAnArc() || value.isComment()))
                gContent.set(row, value);
            
        } else if ( value.isASpline()) {          
            gContent.set(row, new GSpline("", getFirstPointFor( o), value));       
        } else if ( value.isAnArc()) {
            gContent.set(row, new GArc("", getFirstPointFor(o), value));                
        }       
        informAboutChange();
    }

    @Override
    public int size() {
        return gContent.size();
    }

    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        GCode lastPoint = null;
        for( Object o : gContent ) {
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
        return getName() + " (mix "+gContent.size()+")";
    }

        
    @Override
    public void translate(double dx, double dy) {
        gContent.forEach((o) -> {
            if ( o instanceof GCode) ((GCode)o).translate(dx, dy);
            else ((GElement)o).translate(dx, dy);
        });
        informAboutChange();
    }

    public boolean changeFirstPoint(GCode p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transform(AffineTransform t) {
        gContent.forEach((o) -> {
            if ( o instanceof GCode) ((GCode)o).transform( t);
            else if ( o instanceof GElement) ((GElement)o).transform( t);
            else
                 throw new AbstractMethodError( "" + o.getClass() + ".transform(t) not implemented");
        });
    }

    @Override
    public GCode getPoint(int p) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
     * Insert G1 point at center of each selected G1 moves of this path
     * @param selectedPoints
     * @return the new point inserted
     */
    public ArrayList<GCode> addAtCenter(ArrayList<GCode> selectedPoints) {      
        ArrayList<GCode> res = new ArrayList<>(selectedPoints.size());
        GCode lastPoint = null;
        
        if ( ! selectedPoints.isEmpty() )
            for( int i = 0 ; i <  gContent.size(); i++) {
                Object o = gContent.get(i);

                if ( o instanceof GCode) {
                    final GCode p = (GCode) o;

                    if ( p.isAPoint()) {
                        if ((lastPoint != null) && (p.getG() == 1)) {
                            final Segment2D s = new Segment2D(lastPoint, p);
                            lastPoint = (GCode)s.getPointAt(s.getLength()/2);
                            add(i, lastPoint);
                            res.add(lastPoint);
                            i++;
                        }
                        lastPoint = (GCode)o;
                    }
                }                        
            } 
        
        return res;
    }

    
}