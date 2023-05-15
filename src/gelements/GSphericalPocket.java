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
import gcodeeditor.JProjectEditor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public class GSphericalPocket extends GPocket3D {
    
    public static final String HEADER_STRING = "(SphericalPocket-name: ";
    GCode center;
    GCode startPoint;
    double inlayDepth, radius;
    private boolean clockwise;
    
    /**
     * Create a non initialised GSphericalPocket.
     * @param name name of the new element.
     */
    public GSphericalPocket(String name) {
        super(name);
    }
    
    public GSphericalPocket(String name, double x, double y, double radius, double inlayDepth) {
        super(name);
        this.radius = radius;
        this.inlayDepth = inlayDepth;
        center = new GCode(x, y);
        lines.add( new GCode("; sp"));
        recalculate();
    }

    final void recalculate() {
        if ( startPoint == null) {
            double r0 = Math.sqrt(radius * radius - (radius-inlayDepth) * (radius-inlayDepth));
            startPoint = new GCode(0, center.getX() + r0, center.getY());          
        } else {
            double r = center.distance(startPoint);
            radius = (r*r+inlayDepth*inlayDepth)/(2*inlayDepth);
            if ( radius < inlayDepth) {
                radius = inlayDepth;
                startPoint.setLocation(center.getX() + radius, center.getY());
                recalculate();
            }
        }
        super.informAboutChange();
    }
    
    @Override
    protected void informAboutChange() {
        if ( center != null) recalculate();
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public void reverse() {
        clockwise = ! clockwise;
        informAboutChange();
    }

    @Override
    public double getDistanceTo(GCode point) {       
        return Math.min(
                Math.abs(center.distance(startPoint) - center.distance(point)),
                center.distance(point));
    }
    
    @Override
    public GCode getCloserPoint(java.awt.geom.Point2D from, double dmax, ArrayList<GCode> discareIt, boolean excludeFirst) {
        GCode res = null;
        
        // don't return any point when moving startPoint
        if ((discareIt!=null) && discareIt.contains(startPoint)) return null;
        
        if ( ((discareIt==null)||(discareIt.indexOf(center)==-1)) && (dmax > center.distance(from))) {
            res = center;
            dmax = center.distance(from);
        }
        if ( ((discareIt==null)||(discareIt.indexOf(startPoint)==-1)) && (dmax > startPoint.distance(from))) {
            return startPoint;
        }

        if ( Math.abs(radius - center.distance(from)) < dmax) {
            final double a = GElement.getAngleInRadian(center, from);
            return new GCode( center.getX() + (radius * Math.cos(a)), center.getY() + (radius * Math.sin(a)));
        }
        return res;
    }

    @Override
    public int getNbPoints() {
        return 2;
    }

    @Override
    public GCode getPoint(int n) {
        switch (n) {
            case 0: return startPoint;
            case 1: return center;
        }
        return null;
    }
    
    @Override
    public GCode getElementAt(int index) {
        switch ( index) {
            case 0: return new GCode(";Sphere radius=" + radius);
            case 1: return new GCode(";Inlay depth=" + inlayDepth);
            case 2: return startPoint;
            case 3:
                double i = center.getX() - startPoint.getX();
                double j = center.getY() - startPoint.getY();
                return new GCode("G" + (clockwise?2:3) + // "G91.1 " +  
                        " X"+ GWord.GCODE_NUMBER_FORMAT.format( startPoint.getX()) + 
                        " Y"+ GWord.GCODE_NUMBER_FORMAT.format(startPoint.getY()) + 
                        (Math.abs(i)<0.00001?"":" I"+GWord.GCODE_NUMBER_FORMAT.format(i)) +
                        (Math.abs(j)<0.00001?"":" J"+GWord.GCODE_NUMBER_FORMAT.format(j)), null);
            default:
                return new GCode();
        }
    }
    
    @Override
    public void setLine(int row, GCode paramLine) {
        switch ( row) {
            case 0:
                final double r = parseParameterDouble(paramLine);
                if ( ! Double.isNaN(r)) {
                    radius = r;
                    startPoint=null;
                }
            case 1: 
                final double iD = parseParameterDouble(paramLine);
                if (! Double.isNaN(iD) && (iD > 0) && (iD < radius) && (iD != inlayDepth)) {
                    inlayDepth = iD;
                    startPoint = null;
                }
                break;
            case 2: 
                if ( paramLine.isAPoint()) {
                    startPoint.setLocation(paramLine.getX(), paramLine.getY());                  
                }
            case 3:
                if ( paramLine.isAnArc()) {
                    final GArc t = new GArc("tmp", startPoint,  paramLine);
                    clockwise = t.clockwise;  
                }
            default:
                return;
        }
        informAboutChange();
    }
    
    @Override
    public GCode getLine(int i) {
        return getElementAt(1);
    }
          
    /**
     * Return a flatten path of this bounds of the pocket.
     * @return a G1Path object representing the bound of the pocket.
     */
    @Override
    public G1Path flatten() {
        final G1Path flatten = G1Path.makeCircle((Point2D)center, (int)(2* Math.PI * radius), radius, clockwise, true);
        flatten.add(0, new GCode(";InlayDepth="+inlayDepth));
        if ( properties != null) flatten.properties = properties.clone();
        flatten.rotate(center, GElement.getAngleInRadian(center, startPoint));
        return flatten;
    }
    
    @Override
    public double getInlayDepth() {
        return inlayDepth;
    }

    @Override
    public GElement clone() {
        final GSphericalPocket p = new GSphericalPocket(name, center.getX(), center.getY(), radius, inlayDepth);
        p.startPoint = (GCode) startPoint.clone();
        return p;
    }

    @Override
    public G1Path getPassBoundsPath(double depth) {
        if ( depth >= inlayDepth) return new G1Path(name, center, null);
        double pathRadius = Math.sqrt(radius * radius - (radius-inlayDepth+depth) * (radius-inlayDepth+depth));
        return G1Path.makeCircle(center, (int)(Math.PI*2*pathRadius), pathRadius, clockwise, true);
    }

    @Override
    public void paint(PaintContext pc) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g; 
        g.setColor(Color.yellow);
        if ( pc.highlitedPoint != null) {
            final int x = (int)(pc.highlitedPoint.getX()*zoomFactor), y = -(int)(pc.highlitedPoint.getY()*zoomFactor);
            g.fillOval(x-3,y-3, 6, 6);   
        }
        if ( pc.selectedPoints != null)
            for ( GCode p : pc.selectedPoints) {
                final int x = (int)(p.getX()*zoomFactor), y = -(int)(p.getY()*zoomFactor);
                g.fillOval(x-3,y-3, 6, 6);   
        }       
        
        g.setColor(Color.darkGray);
        g.drawOval((int)((center.getX()-radius)*zoomFactor), (int)((-center.getY()-radius)*zoomFactor), 
                   (int)(zoomFactor*radius*2), (int)(zoomFactor*radius*2));
        double pd;
        if ( Double.isNaN(properties.passDepth)) pd=inlayDepth;
        else pd = properties.passDepth;
        for( double depth = properties.passDepth; depth <= inlayDepth; depth += properties.passDepth) {
            g.setColor((depth==0)?pc.color:Color.darkGray);
            double r = Math.sqrt(radius * radius - (radius-inlayDepth+depth) * (radius-inlayDepth+depth) );

            g.drawOval((int)((center.getX()-r)*zoomFactor), (int)((-center.getY()-r)*zoomFactor), 
               (int)(zoomFactor*r*2), (int)(zoomFactor*r*2));
        }
        
        g.setColor(pc.color);
        double r = Math.sqrt(radius * radius - (radius-inlayDepth) * (radius-inlayDepth) );
        g.drawOval((int)((center.getX()-r)*zoomFactor), (int)((-center.getY()-r)*zoomFactor), 
               (int)(zoomFactor*r*2), (int)(zoomFactor*r*2));
               
        
        if ( (pc.editedElement == this) || pc.showStartPoints) g.setColor(Color.red);     
        else g.setColor(Color.darkGray);
        JProjectEditor.drawCross(g, new Point((int)(center.getX()*zoomFactor),-(int)(center.getY()*zoomFactor)),3);
        if ( (pc.color!=Color.darkGray) && pc.showStartPoints) {
            g.setColor(Color.red); 
            final int sx = (int)(startPoint.getX()*zoomFactor);
            final int sy = -(int)(startPoint.getY()*zoomFactor);
            g.drawRect(sx-3, sy-3, 6, 6);
        }
            
        pc.lastPoint = startPoint;
    }

    @Override
    public GCode getLastPoint() {
        return startPoint;
    }

    @Override
    public GCode getFirstPoint() {
        return startPoint;
    }

    @Override
    public Point2D getCenter() {
        return (Point2D) center.clone();
    }

    @Override
    public int getIndexOfPoint(GCode p) {
        if ( p == startPoint) return 0;
        if ( p == center) return 1;
        return -1;
    } 

    @Override
    public void removeByDistance(ArrayList<GCode> points, double d) { }

    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPosition) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        fw.append("; R="+radius+"\n");
        fw.append("; Depth="+inlayDepth+"\n");
        fw.append(getFirstPoint()+"\n");
        fw.append(getElementAt(3)+"\n");
        return getLastPoint();
    }
    
    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        radius = parseParameterDouble( lines.get(0));
        inlayDepth = parseParameterDouble( lines.get(1));
        startPoint=lines.get(2);
        GCode end = new GCode( line);
        clockwise = (end.getG() == 2);
        center = end.getArcCenter(startPoint);
        recalculate();
        lastGState.set( getLastPoint());
        return stream.readLine();
    }
    
    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void scale(Point2D origin, double ratioX, double ratioY) {

        if ( Math.abs(ratioX) != Math.abs(ratioY)) // keep ratio for a circle ...
            ratioY = Math.signum(ratioY) * Math.abs(ratioX);
        
        if (((ratioX == -1) && (ratioY==1)))   { // ! flip Horizontaly ?
            clockwise = ! clockwise;
        } else if ( ! (((ratioX == 1) && (ratioY==-1))) )
            ratioY = ratioX;     
        
        center.scale(origin, ratioX, ratioY);
        startPoint.scale(origin, ratioX, ratioY);
        informAboutChange();
    }

    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {
        final boolean isCenter, isStart = selectedPoints.contains(startPoint);
        if ( (isCenter = selectedPoints.contains(center)) & isStart) 
            translate(dx, dy);
        else {
            if ( isCenter ) center.translate(dx, dy); 
            else if ( isStart) startPoint.translate(dx, dy);
            else return false;
        }
        recalculate();
        return true; 
    }
    
    @Override
    public Iterable<GCode> getPointsIterator() {
        return () -> new Iterator<GCode>() {
            int n=0;
            @Override
            public boolean hasNext() {
                return n < 2;
            }
            @Override
            public GCode next() {
                switch ( n++ ) {
                    case 1: return center;
                    case 0: return startPoint;
                    default: 
                        return null;
                }  
            }
        };
    }
   
    @Override
    public void add(int i, GCode line) { 
        setLine( i, line);
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(center.getX() - radius, center.getY() - radius, radius*2, radius*2);
    }

    @Override
    public void translate(double dx, double dy) {
        center.translate(dx, dy);
        startPoint.translate(dx, dy);
        informAboutChange();
    }
    
    @Override
    public Area getOffsetArea( double distance) {       
        Area a = new Area();
        double r = Math.sqrt(radius * radius - (radius-inlayDepth) * (radius-inlayDepth) );
        a.add(new Area(new Ellipse2D.Double(center.getX()-r-distance, center.getY()-r-distance,
                        2*(distance+r), 2*(distance+r))));
        a.subtract(new Area(new Ellipse2D.Double(center.getX()-r+distance, center.getY()-r+distance,
                        2*(r-distance), 2*(r-distance))));
        return a;
    }
    
    @Override
    public String getSummary() {
        return "<html>Cylindrical pocket "+name+"<br>Radius="+radius+"<br>InlayDepth="+inlayDepth+"</html>";
    }
    
    @Override
    public String toString() {
        return name + " (sphere)";
    }

    public boolean setValues(Double radius, Double inlayDepth) {
        if ( (radius > 0) && (inlayDepth>0) && (inlayDepth<=radius)) {
            this.radius = radius;
            this.inlayDepth = inlayDepth;
            startPoint=null;
            recalculate();
            return true;
        }
        return false;
    }
      
    public double getSphereRadius() {
        return radius;
    }
}
