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
import gcodeeditor.JBlocksViewer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
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

    G90.1 - Mode de déplacement absolu pour les offsets I, J et K. 
            Quand G90.1 est actif, I et J doivent être tous les deux spécifiés 
            avec G2/G3 pour le plan XY ou J et K pour le plan XZ, sinon c’est une erreur.
    G91.1 - Mode de déplacement relatif pour les offsets I, J et K. G91.1 replace I, J et K à leur fonctionnement normal.


 */
public class GArc extends GElement {

    public static final String HEADER_STRING = "(Arc-name: ";
    
    boolean clockwise = true;
    GCode start;
    GCode end;
    GCode center;
    private Arc2D.Double shape;
    G1Path flatten;
    private GCode gcodeArc23;
    
    private Rectangle2D bounds = null;
    private double arcStart,arcLen,radius;

    
    /**
     * Create a non configured arc.
     * @param name0
     */
    public GArc(String name0) {   
        super(name0);
    }
    
    public GArc(String name0, boolean isG2, Point2D start, Point2D end, Point2D center) {
        super(name0);
        clockwise = isG2;
        this.start=new GCode(0, start.getX(), start.getY());
        this.end=new GCode(0, end.getX(), end.getY());
        this.center=new GCode(center.getX(), center.getY());
        recalculateG23();
    }

    public GArc(String name0, Point2D startPoint, GCode g23code) {
        super(name0);
        start=new GCode(0, startPoint.getX(), startPoint.getY());
        setLine(1, g23code);
    }
    
    /**
     * Create a new G2 (G3 if arcLen &lt; 0) GCode Arc
     * @param name0
     * @param center
     * @param radius
     * @param startAngle in degre
     * @param arcLen  in degre
     */
    public GArc(String name0, Point2D center, double radius, double startAngle, double arcLen) {
        super(name0);
        if ( arcLen == 0) arcLen = 360;
        clockwise = arcLen >= 0;
        this.center = new GCode(center);
        start = GCode.newAngularPoint( center, radius, startAngle, true);
        end = GCode.newAngularPoint( center, radius, startAngle + Math.abs(arcLen), true);
        recalculateG23();
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public GCode getElementAt(int index) {
        if ( index == 0) return start;
        if ( index == 1) return gcodeArc23;
        return null;
    }

    @Override
    public GCode getFirstPoint() {
        return start;
    }

    @Override
    public GCode getLastPoint() {
        return gcodeArc23;
    }
    
    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        if ( (lastPoint==null) || (lastPoint.distance(start) > 0.00000001)) fw.append(start.toString()+"\n");
        fw.append(getElementAt(1)+"\n");
        return end;
    }

    /**
     * @param from the point to compate to
     * @param dmax the maximal distance to show
     * @param discareIt the points to ignore in the search
     * @param excludeFirst exclude the first point of the shape
     * @return a point or null if none
     */
    @Override
    public GCode getCloserPoint(java.awt.geom.Point2D from, double dmax, ArrayList<GCode> discareIt, boolean excludeFirst) {
        GCode res = null;
        if ( ((discareIt==null)||(discareIt.indexOf(start)==-1)) && (dmax > start.distance(from))) {
            res = start;
            dmax = start.distance(from);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(end)==-1)) && (dmax > end.distance(from))) {
            res = end;
            dmax = end.distance(from);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(center)==-1)) && ((center!=null) && (dmax > center.distance(from)))) {
            res = center;
            dmax = center.distance(from);
        }

        GCode p;
        if ( res == null) {
            if (((p=flatten().getCloserPoint(from, dmax, discareIt, false))!=null) && (p.distance(from) < dmax)) {
                  
                if (! p.isIn(discareIt))
                        return p;   
            }
                
        }
        return res;
    }

    @Override
    public int getNbPoints() {
        return 3;
    }

    @Override
    public boolean contains(GCode line) {
        return (start==line) || (end==line) || (center.distance(line) < 0.00001);
    }
    
    @Override
    public Rectangle2D getBounds() {
        updateShape();
        return (Rectangle2D) bounds.clone();
    }

    @Override
    public double getLenOfSegmentTo(GCode point) {
        return arcLen;
    }
    
    @Override
    double getLength() {       
        return 2*Math.PI * radius / 360 * arcLen;
    }  

    @Override
    public void simplify(double angleMin, double distance) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }

    @Override
    public int getIndexOfPoint(GCode p) {
        if ( p == start) return 0;
        if ( p == center) return 1;
        if ( p == gcodeArc23) return 2;
        return -1;
    }

    @Override
    public String toString() {
        return name + "(G"+(clockwise?2:3)+")";
    }

    @Override
    public double getDistanceTo(GCode pt) {
        //double decal = ((arcStart+arcLen) > 360) ? (arcStart+arcLen)-360 : 0;
        double d, a = Math.toDegrees(G1Path.getAngle(center, pt));
        a = a < 0 ? -a : 360 - a;
        if ( a < arcStart) a += 360;
        
        boolean into = clockwise ? ((a > arcStart)&&(a<(arcStart+arcLen))) 
                            : ! ((a > arcStart)&&(a<(arcStart+arcLen))) ;
        
        if ( (arcLen>=360) || (clockwise ^ ! into))
            d = Math.abs(radius-center.distance(pt));
        else
            d = Double.POSITIVE_INFINITY;
        
        return Math.min(d,Math.min(center.distance(pt),Math.min( start.distance(pt), end.distance(pt))));
    }

    @Override
    public void rotate(Point2D origin, double angle) {
        start.rotate(origin, angle);
        end.rotate(origin, angle);
        center.rotate(origin, angle);
        informAboutChange();
    }

    @Override
    protected void informAboutChange() {
        shape = null; flatten = null;
        updateShape();
        recalculateG23();   
        super.informAboutChange();
    }

    @Override
    public void scale(Point2D origin, double ratioX, double ratioY) {

        if ( Math.abs(ratioX) != Math.abs(ratioY)) // keep ratio for a circle ...
            ratioY = Math.signum(ratioY) * Math.abs(ratioX);
        
        if (((ratioX == 1) && (ratioY==-1))) { // flip verticaly
            clockwise = ! clockwise;
        } else if (((ratioX == -1) && (ratioY==1)))   { // ! flip Horizontaly ?
            clockwise = ! clockwise;
        } else
            ratioY = ratioX;     
        
        start.scale(origin, ratioX, ratioY);
        end.scale(origin, ratioX, ratioY);
        this.center.scale(origin, ratioX, ratioY);
        informAboutChange();
    }

    @Override
    public boolean movePoint( GCode p, double dx, double dy) {
        boolean moved = false;
        if ( start == p) {
            start.translate(dx, dy);
            moved = true;
        }
        else if ( end == p) {
            end.translate(dx, dy);
            moved = true;
        } else if ( center == p) {
            center.translate(dx, dy);
            moved = true;
        } else 
            return false;
        
        informAboutChange();
        return true;
    }
    
    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {
        boolean moved = false;
        moved = selectedPoints.stream().map((p) -> movePoint(p, dx, dy)).reduce(moved, (accumulator, _item) -> accumulator | _item);
        return moved;
    }

    @Override
    public void translate(double dx, double dy) {
        start.translate(dx, dy);
        end.translate(dx, dy);
        center.setLocation(center.getX()+dx, center.getY()+dy);
        informAboutChange();
    }
    
    @Override
    public Object remove(int i) { 
        return null;
    }

    @Override
    public void removeAll(ArrayList<GCode> lines) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reverse() {
        GCode t = new GCode(start.getX(), start.getY());
        start.set(end);
        end.set(t);
        clockwise=!clockwise;
        informAboutChange();
    }

    @Override
    public void setLine(int row, GCode value) { 
        switch (row) {
            case 0:
                start = value;
                start.setG(0);
                break;
            case 1:
                GCode l = new GCode(value);
                clockwise=l.getG()==2;
                double cx = l.getValue('I');
                if ( Double.isNaN(cx)) cx = 0;
                cx += start.getX();
                double cy = l.getValue('J');
                if ( Double.isNaN(cy)) cy = 0;
                cy += start.getY();
                center=new GCode(cx,cy);
                end = new GCode(0, Double.isNaN(l.getX())?start.getX():l.getX(), Double.isNaN(l.getY())?start.getY():l.getY());
                break;
            default:
                return;
        }
        informAboutChange();
    }

    /**
     * Used to set start position if not set.
     * @param pt 
     * @return  
     */
    @Override
    public boolean add(GCode pt) {
        if ( start == null) {
            start= pt;
            start.setG(0);
            return true;
        }  
        return false;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public GElement clone() {
        GArc clone = new GArc(name, clockwise, start, end, center);
        if ( properties != null) clone.properties = properties.clone();
        return clone;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isClosed() {
        return start.distance(end) < 0.00001;
    }

    @Override
    public String getSummary() {
        return "<HTML>GArc<br>Center = (" + 
                    GWord.GCODE_NUMBER_FORMAT.format(center.getX()) + ", " +
                    GWord.GCODE_NUMBER_FORMAT.format(center.getY())+")<br>Radius = " +
                    GWord.GCODE_NUMBER_FORMAT.format(radius) + "<br>Start angle = " +
                    GWord.GCODE_NUMBER_FORMAT.format(arcStart) + " °<br>Arc lenght = " +
                    GWord.GCODE_NUMBER_FORMAT.format(arcLen) + " °</HTML>";
    }
        
    private void updateShape() {
        if ( shape == null) {
            arcStart = (360 + Math.toDegrees(-G1Path.getAngle(center, clockwise?start:end)))%360;
            arcLen = (360+Math.toDegrees(-G1Path.getAngle(center, clockwise?end:start)) - arcStart)%360;

            if ( (Math.abs(arcLen) < 0.0001) || (Math.abs(360-arcLen) < 0.0001)) arcLen=360;
            else if ( arcLen < 0) arcLen = 360+arcLen;

            radius = center.distance(clockwise?start:end);
            bounds = new Rectangle2D.Double(center.getX()-radius, center.getY()-radius, 2*radius, 2*radius);
            shape = new Arc2D.Double(bounds, arcStart, arcLen, Arc2D.OPEN);
            if ( clockwise)
                end.setLocation(center.getX()+Math.cos(Math.toRadians(arcStart+arcLen))*radius, center.getY()-Math.sin(Math.toRadians(arcStart+arcLen))*radius);
            else
                start.setLocation(center.getX()+Math.cos(Math.toRadians(arcStart+arcLen))*radius, center.getY()-Math.sin(Math.toRadians(arcStart+arcLen))*radius);
            
            bounds = shape.getBounds2D();
        }
    }
    
    @Override
    public void paint(PaintContext pc) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g; 
        updateShape();
        int sx = (int)(start.getX()*zoomFactor);
        int sy = -(int)(start.getY()*zoomFactor);
        int ex = (int)(end.getX()*zoomFactor);
        int ey = -(int)(end.getY()*zoomFactor);
        
        g.setColor(pc.color);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.scale(zoomFactor, -zoomFactor);
        g2.setStroke(new BasicStroke((float)(1f / zoomFactor)));
        g2.draw(shape);
        //drawCoordRect(g, zoomFactor, bounds);
        //g2.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
        
        if ( pc.highlitedPoint != null) {
            g.setColor(Color.yellow);
            final int x = (int)(pc.highlitedPoint.getX()*zoomFactor), y = -(int)(pc.highlitedPoint.getY()*zoomFactor);
            g.fillOval(x-3,y-3, 6, 6);   
        }
        
        // draw selectedPoints
        g.setColor(PaintContext.SEL_COLOR1);
        if ( pc.selectedPoints != null) pc.selectedPoints.forEach((p) -> {
                if ( p == start) g.fillOval(sx-3,sy-3, 6, 6);
                else if ( p.isAnArc() || (p == end)) g.fillOval(ex-3,ey-3, 6, 6);
            });
        
        if ( pc.editedElement == this) {
            g.setColor(PaintContext.EDIT_COLOR);
            g.drawOval(sx-3,sy-3, 6, 6);  
            g.drawOval(ex-3,ey-3, 6, 6);  
        }
        
        if ( (pc.color!=Color.darkGray)) {
            if (pc.showStartPoints) {
                g.setColor(Color.red); 
                g.drawRect(sx-3, sy-3, 6, 6);
            }
            if ( pc.paintReperes ) {
                if ( ( pc.editedElement == this) || pc.showStartPoints) g.setColor(Color.red);     
                else g.setColor(Color.darkGray);
                JBlocksViewer.drawCross(g, new Point((int)(center.getX()*zoomFactor),-(int)(center.getY()*zoomFactor)),3);
            }
        }
        

        pc.lastPoint = end;
    }
    
    
    @Override
    public GCode getLine(int i) {
        if ( i == 0) return start;
        if ( i == 1) return getElementAt(1);
        return new GCode();
    }

    @Override
    public boolean concat(GElement get, double d) { 
        return false;
    }

    @Override
    public Iterable<GCode> getPointsIterator() {
        return () -> new Iterator<GCode>() {
            int n=0;
            @Override
            public boolean hasNext() {
                return n < 3;
            }
            @Override
            public GCode next() {
                switch ( n++ ) {
                    case 0: return start;
                    case 1: return center;
                    default: return end;
                }  
            }
        };
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
    public boolean isoScaling() {
        return true;
    }
    
    
    @Override
    public Area getOffsetArea(double distance) {
        return flatten().getOffsetArea(distance);
    }
    
    /**
     * do nothing for GArc
     * @param pos
     * @param line 
     */
    @Override
    public void add(int pos, GCode line) { }
    
    public boolean isSameAs(GElement obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GArc other = (GArc) obj;
        if (this.clockwise != other.clockwise) {
            return false;
        }
        if (start.distance(other.start) > 0.00001) {
            return false;
        }
        if (end.distance(other.end) > 0.00001) {
            return false;
        }
        return center.distance(other.center) <= 0.00001;
    }
    
    @Override
    public boolean isEmpty() {
        return (start == null) || (end == null) || (center == null);
    }
    
    /**
     * @return Return a G1Path correponding to this arc
     */
    @Override
    public G1Path flatten() { 
        if ( flatten == null) {
            updateShape();
            flatten = new G1Path("flattenArc-"+name);
            int nbp = (int)(((2 * Math.PI * radius * (arcLen/360))/12)+1)*12;
            double angle = arcStart, a = arcLen / nbp;
            while(nbp-- > 0) {
                flatten.add(GCode.newAngularPoint(center, radius, angle, false));
                angle+=a;
            }
        }
        G1Path res =  (G1Path)flatten.clone();
        if ( properties != null) res.properties = properties.clone();
        return res;
    }
    

    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        return this.flatten().toSVG(origin);
    }

    @Override
    public Point2D getCenter() {
        return center;
    }

    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        flatten().toDXF(out);
    }

    public static GArc makeBulge(GCode startpoint, GCode endpoint, double bulge) {
        boolean reversed = (bulge<0.0);
        double alpha = Math.atan(bulge)*4.0;
        GCode middle = startpoint.getMiddlePointTo(endpoint);
        double dist = startpoint.distance(endpoint)/2.0;
        double angle = GElement.getAngle(endpoint, startpoint) + ((bulge>0.0) ? -Math.PI : Math.PI)/2;
	double radius = Math.abs(dist / Math.sin(alpha/2.0)); 
        double h = Math.sqrt(Math.abs(radius*radius - dist*dist));        
        if (Math.abs(alpha)> Math.PI)  h*=-1.0;
        
        GCode center = GElement.getPolarPoint(h, angle);
        center.translate(middle.getX(), middle.getY());
        double a1 = -Math.toDegrees(GElement.getAngle(center, startpoint));
        double a2 = Math.toDegrees(GElement.getAngle(center, endpoint));
        a2 = ( a2 > 180) ? -a2 : 360 - a2;
        return new GArc("_arc", center, radius, a1, reversed ? (a2-a1) : (a1-a2));
    }

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String l =  super.loadFromStream(stream, null);
        if ( l == null) return null;
        start = new GCode(l);
        end = new GCode(stream.readLine());
        clockwise = (end.getG() == 2);
        center = end.getArcCenter(start);
        recalculateG23();
        return stream.readLine();
    }

    private void recalculateG23() {
        if ( gcodeArc23 == null) gcodeArc23 = new GCode();      
        double i = center.getX() - start.getX();
        double j = center.getY() - start.getY();
        GCode l =  new GCode("G" + (clockwise?2:3) + // "G91.1 " +  
                " X"+ GWord.GCODE_NUMBER_FORMAT.format( end.getX()) + 
                " Y"+ GWord.GCODE_NUMBER_FORMAT.format(end.getY()) + 
                (Math.abs(i)<0.00001?"":" I"+GWord.GCODE_NUMBER_FORMAT.format(i)) +
                (Math.abs(j)<0.00001?"":" J"+GWord.GCODE_NUMBER_FORMAT.format(j)), null);
        gcodeArc23.set( l);
    }

    void translateFirstPoint(double dx, double dy) {
        start.translate(dx, dy);
        informAboutChange();
    }
    
    
}
