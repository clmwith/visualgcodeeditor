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

import gcodeeditor.GWord;
import gcodeeditor.GCode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Clément
 */
public class GSpline extends GElement {

    public static final String HEADER_STRING = "(Spline-name: ";   
    GCode start, cp1, cp2, end;
    
    private Rectangle2D bounds;
    private Shape shape;
    private G1Path flatten;
    
    public GSpline(String name0) {
        super(name0);
    }
    
    public GSpline(String name0, Point2D p0, GCode g51Line) {
        super(name0);
        start = new GCode(0, p0.getX(), p0.getY());
        setLine(1, g51Line);
    }
    
    public GSpline( String name0, GCode s, GCode cp, GCode e) {
        super(name0);
        start = s;
        start.setG(0);
        cp1 = cp;
        end = e;
        validateG5();
    }
    
    public GSpline( String name0, GCode s, GCode p1, GCode p2, GCode e) {
        super(name0);
        start = s;
        start.setG(0);
        cp1 = p1;
        cp2 = p2;
        end = e;
        validateG5();
    }
    
    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public GCode getElementAt(int index) {
        if ( index == 0) return start;
        else if ( index == 1) { 
            validateG5();
            return end;
        }
        return null;
    }

    @Override
    public Iterator<GCode> iterator() {
        return new Iterator<GCode>() {
            int line = 0;
            @Override
            public boolean hasNext() { 
                return line < 2;
            }

            @Override
            public GCode next() {
                switch ( line++ ) {
                    case 0: return start;
                    case 1: return getElementAt(1);
                    default:
                        return null;
                }       
            }
        };
    }
    
    
    
    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String l = super.loadFromStream(stream, null);
        if ( l == null) return null;
        GCode pt = new GCode(l);
        if ( pt.isASpline()) {
            start = new GCode(0,lastGState.getX(), lastGState.getY());
            setLine( 1, pt);
        } else {
            start = pt;
            setLine(1, new GCode(stream.readLine()));
        }
        lastGState.set(end);
        return stream.readLine();
    }

    @Override
    protected void informAboutChange() {
        shape = null; 
        flatten = null; 
        
        if (cp1 != null && end != null) {
            if ( cp1.isAtSamePosition(start) ||
                 cp1.isAtSamePosition(end)) {
                cp1 = cp2;
                cp2 = null;
            }
            if (cp2!=null) {
                if (cp1.isAtSamePosition(cp2) ||
                    cp2.isAtSamePosition(start) ||
                    cp2.isAtSamePosition(end))
                        cp2 = null;
            }
        }
        validateG5();
        super.informAboutChange();
    }

    @Override
    public Iterable<GCode> getPointsIterator() {
        return () -> new Iterator<GCode>() {
            int n=0;
            @Override
            public boolean hasNext() {
                return n < getNbPoints();
            }
            @Override
            public GCode next() {
                switch ( n++ ) {
                    case 0: 
                        return start;
                    case 1:
                        if ( cp1 != null) return cp1;
                    case 2:
                        if (cp2 != null) return cp2;
                    case 3:
                        return getElementAt(1);
                    default: 
                        return null;
                }  
            }
        };
    }
    
    /**
     * @return number of points (real or not)
     */
    @Override
    public int getNbPoints() {
        return 1 + (cp1!=null?1:0) + (cp2!=null?1:0) + (end!=null?1:0);
    }
    
    @Override
    public GElement clone() {
        GSpline clone = new GSpline(name, start, getElementAt(1)); 
        if ( properties != null) clone.properties = properties.clone();
        return clone;
    }
    
    /**
     * Used to set all 4 points sequentialy: start, cp1, end, and cp2 after.
     * @param curveOrPoint a full G5 curve or just a point
     * @return true if point was added (if the curve was not entirely set)
     */
    @Override
    public boolean add(GCode curveOrPoint) {
        if ( curveOrPoint.isASpline()) 
            setLine(1, curveOrPoint);
        else {            
            if ( start == null) {
               start = curveOrPoint;
            } else 
                if ( cp1==null) cp1=curveOrPoint;
                else if ( end==null) end = curveOrPoint;
                else if ( cp2==null) {
                    if ( start.distance(curveOrPoint) > end.distance(curveOrPoint))
                        cp2=curveOrPoint;
                    else {
                        cp2=cp1;
                        cp1=curveOrPoint;
                    }
                }
                else
                    return false;
        }
        informAboutChange();
        return true;
    }

    @Override
    public void add(int pos, GCode line) {
        add(line);
    }

    @Override
    public Area getOffsetArea(double param) {
        return getFlatten().getOffsetArea(param);
    }

    @Override
    public GCode getFirstPoint() {
        return start;
    }

    @Override
    public GCode getLastPoint() {
        return getElementAt(1);
    }

    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        if ( (lastPoint==null) || ! lastPoint.isAtSamePosition(start)) fw.append(start.toString()+"\n");
        String e;
        fw.append(e=getElementAt(1)+"\n");
        return getElementAt(1);
    }

    @Override
    public GCode getCloserPoint(java.awt.geom.Point2D pt, double dmin, ArrayList<GCode> discareIt, boolean excludeFirst) {
                
        GCode res = null;
        if ( ((discareIt==null)||(discareIt.indexOf(cp1)==-1)) && ((cp1!=null) &&(dmin > cp1.distance(pt)))) {
            res = cp1;
            dmin = cp1.distance(pt);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(cp2)==-1)) && ((cp2!=null) && (dmin > cp2.distance(pt)))) {
            res = cp2;
            dmin = cp2.distance(pt);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(start)==-1)) && (dmin > start.distance(pt))) {
            res = start;
            dmin = start.distance(pt);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(getElementAt(1))==-1)) && (dmin > end.distance(pt))) {
            res =  end;
            dmin = end.distance(pt);
        }
        GCode p;
        if ( res == null) {
            if (((p=getFlatten().getCloserPoint(pt, dmin, discareIt, true))!=null) && ! p.isIn(discareIt)) 
                return p;
        }
        
        return res;
    }



    @Override
    public boolean contains(GCode line) {
        return (line==start) || (line==getElementAt(1)) || (line==cp1) || (line==cp2) || (line==end);
    }

    @Override
    public Rectangle2D getBounds() {
        updateShape();
        return (Rectangle2D) bounds.clone();
    }

    @Override
    double getLength() {
        return getFlatten().getLength();
    }

    @Override
    public double getLenOfSegmentTo(GCode point) { 
        if ( point == end) {
            return getFlatten().getLength();
        }
        return Double.NaN;
    }

    @Override
    public int getIndexOfPoint(GCode pt) {
        if ( pt==start) return 0;
        if ( pt==getElementAt(1)) return 1;
        return -1;
    }

    @Override
    public double getDistanceTo(GCode pt) {
        return getFlatten().getDistanceTo(pt);
    }

    @Override
    public boolean movePoint(GCode point, double dx, double dy) {
        if ((point == start) || (point==cp1) || (point==cp2) || (point==end)) {
            point.translate(dx, dy); 
            informAboutChange();
            return true;
        }
        return false; 
    }
    
    @Override
    public boolean movePoints(ArrayList<GCode> points, double dx, double dy) {
        boolean moved = false;
        for( GCode p : points) {
            if ( contains(p)) {
                p.translate(dx, dy);
                moved = true;
            }
        }
        if ( moved) informAboutChange();
        return moved;
    }

    @Override
    public void translate(double dx, double dy) {
        start.translate(dx, dy);
        end.translate(dx, dy);
        if ( cp1 != null) cp1.translate(dx, dy);
        if ( cp2 != null) cp2.translate(dx, dy);
        informAboutChange();
    }
   
    @Override
    public void transform(AffineTransform t) {
        start.transform(t);
        end.transform(t);
        if ( cp1 != null) cp1.transform(t);
        if ( cp2 != null) cp2.transform(t);
        informAboutChange();
    }
    
    @Override
    public Object remove(int i) { 
        return null; 
    }

    @Override
    public void removeAll(ArrayList<GCode> lines) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }

    @Override
    public void reverse() {
        GCode t = start;
        start = end;
        end = t;
        informAboutChange();
    }

    @Override
    public void rotate(Point2D origin, double angle) {
        start.rotate(origin, angle);
        end.rotate(origin, angle);
        if ( cp1 != null) cp1.rotate(origin, angle);
        if ( cp2 != null) cp2.rotate(origin, angle);
        informAboutChange();
    }

    @Override
    public void scale(Point2D origin, double ratioX, double ratioY) {
        start.scale(origin, ratioX, ratioY);
        end.scale(origin, ratioX, ratioY);
        if ( cp1 != null) cp1.scale(origin, ratioX, ratioY);
        if ( cp2 != null) cp2.scale(origin, ratioX, ratioY);
        informAboutChange();
    }

    @Override
    public void simplify(double angleMin, double distanceMax) { }

    @Override
    public void setLine(int row, GCode gcode) {
        switch (row) {
            case 0:
                start.set(gcode);
                start.setG(0);
                break;
            case 1:
                if (gcode.getG() != 5)  gcode.setG(5);
                GCode l = new GCode(gcode);
                double x = Double.isNaN(l.getX()) ? 0 : l.getX();
                double y = Double.isNaN(l.getY()) ? 0 : l.getY();
                if ( end == null) end = new GCode(5,x,y);
                else end.set(5, x, y);
                x = l.get('I') == null ? 0 : l.get('I').getValue();
                y = l.get('J') == null ? 0 : l.get('J').getValue();
                if ( (x != 0) && (y != 0)) cp1 = new GCode(start.getX() + x, start.getY() + y);
                else cp1 = null;
                
                if ( (l.get('P') != null) || (l.get('Q') != null)) {
                    x = l.get('P') == null ? 0 : l.get('P').getValue();
                    y = l.get('Q') == null ? 0 : l.get('Q').getValue();
                    cp2 = new GCode(end.getX() + x, end.getY() + y);
                } else cp2 = null;
                break;
            default:
                return;
        }
        informAboutChange();
    }

    @Override
    public GCode insertPoint(GCode pt) {
        if (cp1 == null) cp1 = pt;
        else if (cp2 == null) {
            if ( start.distance(pt) > end.distance(pt))
                cp2=pt;
            else {
                cp2=cp1;
                cp1=pt;
            }
        }
        else return null;
        informAboutChange();
        return pt;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public GCode getLine(int i) {
        if ( i == 0) return start;
        else return getElementAt(1);
    }

    @Override
    public boolean isClosed() {
        return start.distance(end) < 0.000001;
    }

    @Override
    public String getSummary() {
        return "G5 Spline, bezier curve";
    }

    @Override
    public void paint(PaintContext pc) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g;       
        final int sx = (int)(start.getX()*zoomFactor);
        final int sy = -(int)(start.getY()*zoomFactor);
        final int ex = (int)(end.getX()*zoomFactor);
        final int ey = -(int)(end.getY()*zoomFactor);  
        
        updateShape();
        if (pc.color == PaintContext.EDIT_COLOR) {
            
            int x1 = 0, y1 = 0;
            if ( cp1 != null) {           
                x1 = (int)(cp1.getX()*zoomFactor);
                y1 = -(int)(cp1.getY()*zoomFactor);
                g.setColor(Color.LIGHT_GRAY);
                drawPoint(g, zoomFactor, cp1);
                g.setColor(Color.DARK_GRAY);
                g.drawLine(sx, sy, x1, y1);
            }
            if ( cp2 != null) {
                    x1 = (int)(cp2.getX()*zoomFactor);
                    y1 = -(int)(cp2.getY()*zoomFactor);
                    g.setColor(Color.LIGHT_GRAY);
                    drawPoint(g, zoomFactor, cp2);
                    g.setColor(Color.DARK_GRAY);
                    g.drawLine(ex, ey, x1, y1);
                    
            } 
            if ((cp1 != null) && (cp2==null)) g.drawLine(ex, ey, x1, y1); 
        }
        
        g.setColor(pc.color);      
        Graphics2D g2 = (Graphics2D)g.create();
        g2.scale(zoomFactor, -zoomFactor);
        g2.setStroke(new BasicStroke((float)(1f / zoomFactor)));
        g2.draw(shape);

        if ( pc.color == PaintContext.EDIT_COLOR) {
            drawPoint(g, zoomFactor, start);
            drawPoint(g, zoomFactor, end);
        }
        
        if ( pc.editedElement == this) {
            if ( pc.highlitedPoint != null) {
                g.setColor(Color.yellow);
                int x = (int)(pc.highlitedPoint.getX()*zoomFactor), y = -(int)(pc.highlitedPoint.getY()*zoomFactor);
                g.fillOval(x-3,y-3, 6, 6);   
            }

            if ( (pc.color!=Color.darkGray) && pc.showStartPoints) {
                if( (pc.color == PaintContext.SEL_COLOR1)|| (pc.color == PaintContext.SEL_DISABLED_COLOR)) g.setColor(pc.color);
                else g.setColor(Color.red); 
                g.drawRect(sx-3, sy-3, 6, 6);
            }
            // draw selectedPoints
            g.setColor(PaintContext.SEL_COLOR1);
            pc.selectedPoints.forEach((p) -> {
                if ( p == start) g.fillOval(sx-3,sy-3, 6, 6);
                else if ( p.isASpline() || (p == end)) g.fillOval(ex-3,ey-3, 6, 6);
            });
        }

        pc.lastPoint = end;
    }
    
    public void drawPoint( Graphics g, double zoomFactor, GCode pt) {
        int x = (int)(pt.getX()*zoomFactor), y = -(int)(pt.getY()*zoomFactor);
        g.drawOval(x-3,y-3, 6, 6);  
    }
    
 

    @Override
    public boolean isoScaling() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return end == null;
    }

    @Override
    public G1Path flatten() {
        G1Path res = (G1Path)getFlatten().clone();
        if ( properties != null) res.properties = properties.clone();
        return res;
    }

    @Override
    public Point2D getCenter() {
        updateShape();
        return new Point2D.Double( bounds.getCenterX(), bounds.getCenterY());
    }
    
    @Override
    public boolean concat(GElement get, double d) {
        throw new UnsupportedOperationException("concat for GSpline is not supported yet.");
    }
    
    @Override
    public String toString() {
        return getName() + "(G5)";
    }


    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        return "<path id=\""+getName()+"\"\n d=\"" +
                "M " +start.toSVGPoint(origin) + 
               " C " + ((cp1==null)?start:cp1).toSVGPoint(origin) +
              ((cp2!=null)? " " + cp2.toSVGPoint(origin) : "") +
               end.toSVGPoint(origin) + "\"";            
    }

    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        out.write("  0\nSPLINE\n");
        out.write("  8\n1\n");  // layer 1 70
        out.write(" 70\n8\n 71\n3\n 72\n8\n 73\n4\n 74\n0\n 40\n0\n 40\n0\n 40\n0\n 40\n0\n 40\n1\n 40\n1\n 40\n1\n 40\n1\n");
        out.write(" 10\n"+start.getX()+"\n");
        out.write(" 20\n"+start.getY()+"\n");
        if ( cp1 == null) {
            out.write(" 10\n"+start.getX()+"\n");
            out.write(" 20\n"+start.getY()+"\n");            
        } else {
            out.write(" 10\n"+cp1.getX()+"\n");
            out.write(" 20\n"+cp1.getY()+"\n");
        }
        if ( cp2 != null) {
            out.write(" 10\n"+cp2.getX()+"\n");
            out.write(" 20\n"+cp2.getY()+"\n");
        }
        out.write(" 10\n"+end.getX()+"\n");
        out.write(" 20\n"+end.getY()+"\n");
    }
    


    private G1Path getFlatten() {
        if ( flatten == null) {
            updateShape();
            ArrayList<GElement> flat = G1Path.makeBlocksFromArea("flatten-spline", shape);
            flatten = flat.isEmpty() ? 
                        new G1Path("flatten-spline", start, end)
                        : (G1Path)flat.get(0);
        } 
        return flatten;
    }

    /**
     * Set I J according to this GCODE G5 curve
     * @param g5curve
     */
    public void setIJInversedWith(GSpline g5curve) {
        GCode ij;
        if ( g5curve.cp2 != null) cp1 = cp2.clone();
        else 
            if ( g5curve.cp1 != null) cp1 = cp1.clone();
            else 
                return;
        informAboutChange();
    }

    void translateFirstPoint(double dx, double dy) {
        start.translate(dx, dy);
        // translate I,J too ???
        informAboutChange();
    }
    
    private void updateShape() {
        if ( shape == null) {
            if ( cp1 == null) {
                shape = new GeneralPath();
                ((GeneralPath)shape).moveTo(start.getX(), start.getY());
                ((GeneralPath)shape).lineTo(end.getX(), end.getY());
            }
            else {
                if ( cp2 == null)
                    shape = new QuadCurve2D.Double(start.getX(), start.getY(), cp1.getX(), cp1.getY(), end.getX(), end.getY());
                else
                    shape = new CubicCurve2D.Double(start.getX(), start.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(), end.getX(), end.getY());
            }
            
            bounds = getFlatten().getBounds();
        }
    }

    boolean isALine() {
        return ((cp1==null)||cp1.isAtSamePosition(start)||cp1.isAtSamePosition(end)) &&
                ((cp2==null)||cp2.isAtSamePosition(start)||cp2.isAtSamePosition(end));
    }

    /**
     * Recalculate I,J, Q,P into G5 code according to cp1 and cp2
     */
    private void validateG5() {
        GCode p = end.clone();
        end.clear();
        end.set(5, p.getX(), p.getY());
        if (cp1 !=null) {
            // Cubic
            end.add( new GWord('I', cp1.getX() - start.getX()));
            end.add( new GWord('J', cp1.getY() - start.getY()));          
            if ( cp2 != null) { 
                // Quadratic
                // l.setG( 5.1);               
                if ( Math.abs(cp2.getX() + end.getX()) > 0.0000001) end.add( new GWord('P', cp2.getX() - end.getX()));
                if ( Math.abs(cp2.getY() + end.getY()) > 0.0000001) end.add( new GWord('Q', cp2.getY() - end.getY()));
            }  
        }
    }
}
