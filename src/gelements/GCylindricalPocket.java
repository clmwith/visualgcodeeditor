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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Clément
 */
public final class GCylindricalPocket extends GPocket3D {
    
    public static final String HEADER_STRING = "(CylindricalPocket-name: ";
    
    /** the first GCode line of the bound of the pocket */
    static final int RECTANGLE_GCODE_LINE_NUMBER = 3;
    double radius;
    double inlayDepth;
    double len, rotationAngle;
    private boolean skipInformChanges;
   
    /**
     * Create a cylinder-shaped pocket.
     * @param name0
     * @param origin
     * @param radius
     * @param inlayDepth
     * @param cylinderLen
     * @param rotationAngle 
     */
    public GCylindricalPocket(String name0, Point2D origin, double radius, double inlayDepth, double cylinderLen,  double rotationAngle) {
        super(name0);
        this.radius = radius;
        this.inlayDepth = inlayDepth;
        this.len = cylinderLen;
        this.rotationAngle = Math.toRadians(rotationAngle);
        for( int i = 0; i < 10; i++) lines.add(new GCode((i==0)?0:1,0,0));
        calculate( origin,false);
    }

    /**
     * warning : do not compare Properties !
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof GCylindricalPocket) {
            GCylindricalPocket t = (GCylindricalPocket)obj;
            
            return (t.radius == radius) && 
                   (t.inlayDepth == inlayDepth) &&
                   (t.len == len) &&
                   (t.rotationAngle == rotationAngle) &&
                   (t.lines.get( RECTANGLE_GCODE_LINE_NUMBER).isAtSamePosition(lines.get( RECTANGLE_GCODE_LINE_NUMBER)));
        }  
        return false;
    }
    
    

    /**
     * Create a non initialised GCylindricalPocket.
     * @param name0 name of the new element.
     */
    public GCylindricalPocket(String name0) {
        super(name0);
    }

    @Override
    public void add(int pos, GCode p) { 
        if ( getNbPoints() <6) 
            super.add(pos, p);
        if ( pos <= RECTANGLE_GCODE_LINE_NUMBER) 
            setLine(pos, p);
    }
    
    /**
     * Used to reload a pre-saved cylinder.
     * @param savedParamLine 
     * @return true if the line was appened.
     */
    @Override
    public boolean add( GCode savedParamLine) {
        if ( lines.size() < 7) {
            add( lines.size(), savedParamLine);
            return true;
        }
        return false;
    }
      
    public void calculate(Point2D origin, boolean angleToo) {
        if ( lines.size() < 9 ) return; // not initialised
        if ( inlayDepth > radius ) inlayDepth = radius;        
 
        while( lines.size()<10 ) lines.add(new GCode(1,0,0));

        double dx = origin.getX();
        double dy = origin.getY();
        if (angleToo) {
            GCode p2; 
            rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER+1), p2=lines.get(RECTANGLE_GCODE_LINE_NUMBER+2));
            len = GWord.round(lines.get(RECTANGLE_GCODE_LINE_NUMBER+1).distance(p2));
        }
        double h = (radius-inlayDepth);
        double offset = Math.sqrt( radius * radius - h*h);

        lines.set(0,new GCode(";Radius="+radius));
        lines.set(1,new GCode(";Length="+len));
        lines.set(2,new GCode(";InlayDepth="+inlayDepth)); 
        lines.get(3).setLocation(0, 0);
        lines.get(4).setLocation(0, -offset);
        lines.get(5).setLocation(len, -offset);
        lines.get(6).setLocation(len, 0);
        lines.get(7).setLocation(len, offset);
        lines.get(8).setLocation(0, offset);
        lines.set(9, new GCode(1, 0, 0));
        super.rotate(lines.get(3), rotationAngle);
        super.translate(dx, dy);
        properties.zEnd = Double.isNaN(properties.zStart) ? -inlayDepth : properties.zStart - inlayDepth;        
    }
    
    public void recalculate(Point2D origin, boolean angleToo) { 
        skipInformChanges = true;
        GCylindricalPocket old = (GCylindricalPocket) clone();
        calculate(origin == null ? lines.get(RECTANGLE_GCODE_LINE_NUMBER) : origin, angleToo);
        skipInformChanges = false;
        if ( ! old.equals(this))
            informAboutChange();
    }

    @Override
    protected void informAboutChange() {
        if ( ! skipInformChanges) super.informAboutChange();
    }
    
    @Override
    public String getSummary() {
        return "<html>Cylindrical pocket<br><i>Z end</i> is ignored in favor of <i>InlayDepth</i></html>";
    }
    
    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        for( GCode l : lines) 
            fw.append(l.toString()+"\n");
        return getLastPoint();
    }

    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {        
        boolean res = super.movePoints(selectedPoints, dx, dy);
        if ((selectedPoints.size()==1)) {
            int i = lines.indexOf(selectedPoints.get(0));
            if ( i == (RECTANGLE_GCODE_LINE_NUMBER+1))
                rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER), lines.get(RECTANGLE_GCODE_LINE_NUMBER+1)) - 3*Math.PI/2;
            else if ( i==(RECTANGLE_GCODE_LINE_NUMBER+5))
                rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER+5), lines.get(RECTANGLE_GCODE_LINE_NUMBER)) - 3*Math.PI/2;
            else if ( (i == (RECTANGLE_GCODE_LINE_NUMBER+3)) || (i==RECTANGLE_GCODE_LINE_NUMBER)) {
                len = lines.get(RECTANGLE_GCODE_LINE_NUMBER).distance(lines.get(RECTANGLE_GCODE_LINE_NUMBER+3));
                rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER), lines.get(RECTANGLE_GCODE_LINE_NUMBER+3));
            } else if ( (i == (RECTANGLE_GCODE_LINE_NUMBER+1)) || (i==(RECTANGLE_GCODE_LINE_NUMBER+2))) {
                len = lines.get(RECTANGLE_GCODE_LINE_NUMBER+1).distance(lines.get(RECTANGLE_GCODE_LINE_NUMBER+2));
                rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER+1), lines.get(RECTANGLE_GCODE_LINE_NUMBER+2));
            } else if (i == (RECTANGLE_GCODE_LINE_NUMBER+4)) {
                len = lines.get(RECTANGLE_GCODE_LINE_NUMBER+4).distance(lines.get(RECTANGLE_GCODE_LINE_NUMBER+5));
                rotationAngle=getAngleInRadian(lines.get(RECTANGLE_GCODE_LINE_NUMBER+5), lines.get(RECTANGLE_GCODE_LINE_NUMBER+4));
            }
            recalculate(null, false);
        } else
            recalculate(null, true);
        
        return res;
    }

    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy); //To change body of generated methods, choose Tools | Templates.
        recalculate(null, false);
    }

    @Override
    public GElement clone() {
        return new GCylindricalPocket(name, lines.get(RECTANGLE_GCODE_LINE_NUMBER), radius, inlayDepth, len, Math.toDegrees(rotationAngle));
    }
    
    @Override
    public void setLine(int row, GCode l) {
        try {
            double v=0;
            GCode p=null;
            if ( row < RECTANGLE_GCODE_LINE_NUMBER) {
                v= parseParameterDouble(l);
                if ( Double.isNaN(v)) return;
            }
            else p = new GCode(l);
            
            switch (row) {
                case 0: 
                    if( v>0) radius=v;
                    v=inlayDepth; // no break here !
                case 1: 
                    if ( v < 0) v = 0;
                    if ( len != v) {
                        len = v;                                        
                        recalculate(null, false);
                        informAboutChange();
                    }
                    break;
                case 2: 
                    if((v>radius) || ( v <= 0)) return;
                    
                    if ( v != inlayDepth) {
                        inlayDepth = v;
                        recalculate(p, false);
                        informAboutChange();
                    }
                    break;
                case RECTANGLE_GCODE_LINE_NUMBER:
                    if (p.isAPoint()) recalculate(p, true);
                    break;
                case (RECTANGLE_GCODE_LINE_NUMBER+2):         
                    if (p.isAPoint()) {
                        lines.set(5, p);
                        recalculate(p, true);  
                    }
            }
        } catch ( NumberFormatException e) { }
    }


    @Override
    public CharSequence toSVG(Rectangle2D origin) { 
        return "<!-- GCylindricalPocket to SVG is not implemented.-->\n"; 
    }

    @Override
    public GCode remove(int i) { 
        return null;
    }
    
    @Override
    public void removeAll(ArrayList<GCode> lines) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }

    @Override
    public void reverse() { }

    @Override
    public void rotate(Point2D transformationOrigin, double d) {
        super.rotate(transformationOrigin, d);
        recalculate(null, true);
    }

    @Override
    public boolean isoScaling() {
        return true;
    }
    
    @Override
    public void scale(Point2D transformationOrigin, double dx, double unused) {
        super.scale(transformationOrigin, dx, dx);
        recalculate(null, true);
    }

    @Override
    public void simplify(double angleMin, double distanceMax) { }

    
    @Override
    public G1Path flatten() {
        G1Path res = new G1Path("flatten-"+name);
        for( int i = 0; i < 7; i++) 
            res.add((GCode) getLine(i+RECTANGLE_GCODE_LINE_NUMBER).clone());
        if ( properties != null) res.properties = properties.clone();
        return res;
    }   

    @Override
    public boolean concat(GElement get, double d) {
        throw new UnsupportedOperationException("Can't concat this element (GCylindricalPocket)");
    }


    public void setValues(double r, double l, double inlay, double rotation) {
        radius = r;
        len = l;
        inlayDepth = inlay;
        recalculate(null, true);
        rotationAngle = Math.toRadians(rotation);
        recalculate(null, false);
    }

    @Override
    public void paint(PaintContext pc) {
        recalculate(null, true);

        double inD = inlayDepth;
        double pd = properties.getPassDepth();
        if ( ! Double.isNaN(pd)) {
            for(; inlayDepth>=0; inlayDepth -= pd ) {
                recalculate(null, false);
                paintPass(pc, inlayDepth==inD);
            }
        } else
            paintPass(pc, true);
        
        inlayDepth = inD;
        recalculate(null, false);
        super.paint(pc);
    }

    private void paintPass(PaintContext pc, boolean withCylinderBorder) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g; 
        if ( withCylinderBorder) {
            Point2D center, p0 = lines.get(RECTANGLE_GCODE_LINE_NUMBER+1);
            double inlayAngle = Math.acos((radius-inlayDepth)/radius) - rotationAngle;

            center = new Point2D.Double( p0.getX() - Math.cos(inlayAngle)*radius, -p0.getY() - Math.sin(inlayAngle)*radius);
            g.setColor(Color.darkGray);
            g.drawOval((int)((center.getX()-radius)*zoomFactor), 
                       (int)((center.getY()-radius)*zoomFactor), 
                       (int)(2*radius*zoomFactor), (int)(2*radius*zoomFactor));

            p0 = lines.get(RECTANGLE_GCODE_LINE_NUMBER+4);
            center = new Point2D.Double( p0.getX() - Math.cos(inlayAngle+Math.PI)*radius, -p0.getY() - Math.sin(inlayAngle+Math.PI)*radius);        
            g.drawOval((int)((center.getX()-radius)*zoomFactor), 
                       (int)((center.getY()-radius)*zoomFactor), 
                       (int)(2*radius*zoomFactor), (int)(2*radius*zoomFactor));
        } else
            super.paint(pc);
    }

    
    /**
     * Return the bounds of the poket at this depth.
     * @param depth the absolute positive depth of the pass (0 is the surface)
     * @return the pass path or null if none
     */
    @Override
    public G1Path getPassBoundsPath(double depth) {
        if ( (depth < 0) || (depth > inlayDepth)) return null;
        return new GCylindricalPocket(name+"_"+depth, lines.get(RECTANGLE_GCODE_LINE_NUMBER), radius, inlayDepth-depth, len, rotationAngle);
    }

    @Override
    public double getInlayDepth() {
        return inlayDepth;
    }

    @Override
    public boolean joinPoints(ArrayList<GCode> selectedPoints) {
        return false;
    }

    /**
     * @return If initialised return 1 for the First and Last point of the pocket offset, or return 0.
     */
    @Override
    public int getNbPoints() {
        return (lines.size() == 10 ) ? 1 : 0;
    }

    @Override
    public String toString() {
        return name + " (cylinder)";
    }

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        radius = parseParameterDouble( lines.get(0));
        len = parseParameterDouble( lines.get(1));
        inlayDepth = parseParameterDouble( lines.get(2));
        calculate(lines.get(RECTANGLE_GCODE_LINE_NUMBER), true);
        return line;
    }
    
}
