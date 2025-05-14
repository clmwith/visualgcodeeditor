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
import gcodeeditor.EngravingProperties;
import gcodeeditor.GWord;
import gcodeeditor.GCode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A shape that imlement GCODE of a spherical pocket.
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
    private boolean doInform;
   
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
        properties.setZEnd( Double.isNaN(properties.getZStart()) ? -inlayDepth : properties.getZStart() - inlayDepth); 
                
        for( int i = 0; i < 10; i++) lines.add(new GCode((i==0)?0:1,0,0));
        properties.addChangeListener(new EngravingProperties.PropertieChangeListener() {
            @Override
            public void propertyChanged(int type) {
                switch ( type) {
                    case EngravingProperties.PropertieChangeListener.START:
                        properties.setZEnd(properties.getZStart() - inlayDepth);
                        break;
                    case EngravingProperties.PropertieChangeListener.END:
                        properties.setZStart(properties.getZEnd() + inlayDepth);
                        break;
                }
            }
        });
        revalidate(origin,false);
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
      
    /**
     * caluclate the bounds of the pocket
     * @param newOrigin the center of the first edge of the cylinder (can be null if unchanged)
     * @param angleToo  reclaculate rotation angle too
     */
    public void revalidate(Point2D newOrigin, boolean angleToo) {
        if ( lines.size() < 9 ) return; // not initialised
        if ( inlayDepth > radius ) inlayDepth = radius;        
        if (newOrigin == null) newOrigin = lines.get(RECTANGLE_GCODE_LINE_NUMBER); 
        
        // fill array of lines
        while( lines.size()<10 ) lines.add(new GCode(1,0,0));

        double dx = newOrigin.getX();
        double dy = newOrigin.getY();
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
        doInform = true;
        super.translate(dx, dy);
    } 
    

    @Override
    protected void informAboutChange() {
        if ( doInform) super.informAboutChange();
        doInform = false;
    }
    
    @Override
    public GElement clone() {
        return new GCylindricalPocket(name, lines.get(RECTANGLE_GCODE_LINE_NUMBER), radius, inlayDepth, len, Math.toDegrees(rotationAngle));
    }
    
       /**
     * warning : does not compare Properties !
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
    
    @Override
    public String getSummary() {
        return "<html>Cylindrical pocket<br><i>Z end</i> is ignored in favor of <i>InlayDepth</i></html>";
    }

    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {        
        boolean res = super.movePoints(selectedPoints, dx, dy);
        if ( ! res) return false;
        
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
            } else {
                revalidate(null, true);
                return true;
            }
        }  
        
        revalidate(null, false);           
        return true;
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
                        revalidate(null, false);
                    }
                    break;
                case 2: 
                    if((v>radius) || ( v <= 0)) return;
                    
                    if ( v != inlayDepth) {
                        inlayDepth = v;
                        revalidate(p, false);
                    }
                    break;
                case RECTANGLE_GCODE_LINE_NUMBER:
                    if (p.isAPoint()) revalidate(p, true);
                    break;
                case (RECTANGLE_GCODE_LINE_NUMBER+2):         
                    if (p.isAPoint()) {
                        lines.set(5, p);
                        revalidate(p, true);  
                    }
            }
        } catch ( NumberFormatException e) { }
    }  
       
    /**
     * Ued by PropertyPanel
     * @param radius
     * @param len
     * @param inlay
     * @param rotation 
     */
    public void setValues(double radius, double len, double inlay, double rotation) {
        this.radius = radius;
        this.len = len;
        inlayDepth = inlay;
        revalidate(null, true);
        rotationAngle = Math.toRadians(rotation);
        revalidate(null, false);
    }

    @Override
    public CharSequence toSVG(Rectangle2D origin) { 
        return "<!-- GCylindricalPocket to SVG is not implemented.-->\n"; 
    }
    
    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy); //To change body of generated methods, choose Tools | Templates.
        revalidate(null, false);
    }

    @Override
    public void reverse() { }

    @Override
    public void rotate(Point2D transformationOrigin, double d) {
        super.rotate(transformationOrigin, d);
        revalidate(null, true);
    }

    @Override
    public boolean isoScaling() {
        return true;
    }
    
    @Override
    public void scale(Point2D transformationOrigin, double dx, double unused) {
        super.scale(transformationOrigin, dx, dx);
        revalidate(null, true);
    }
  
    @Override
    public G1Path flatten() {
        G1Path res = new G1Path("flatten-"+name);
        for( int i = 0; i < 7; i++) 
            res.add((GCode) getLine(i+RECTANGLE_GCODE_LINE_NUMBER).clone());
        if ( properties != null) res.properties = properties.clone();
        return res;
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

    /**
     * @return If initialised return 1 for the First and Last point of the pocket offset, or return 0.
     */
    @Override
    public int getNbPoints() {
        return (lines.size() == 10 ) ? 1 : 0;
    }
    
    @Override
    public void paint(PaintContext pc) {

        Graphics2D g = pc.g;
        g.setColor(Color.darkGray);
        
        Point2D p0 = null, p1 = null;
        double pd = properties.getPassDepth();

        if ( ! Double.isNaN(pd) ) {
            for(double inD = inlayDepth; inD >= 0; inD -= pd ) {
                
                if ( inlayDepth==inD ) {
                    double inlayAngle = Math.acos((radius-inlayDepth)/radius) - rotationAngle;
                    p0 = lines.get(RECTANGLE_GCODE_LINE_NUMBER+1);
                    p1 = lines.get(RECTANGLE_GCODE_LINE_NUMBER+4);
                    Point2D center = new Point2D.Double( p0.getX() - Math.cos(inlayAngle)*radius, -p0.getY() - Math.sin(inlayAngle)*radius);

                    g.drawOval((int)((center.getX()-radius)*pc.zoomFactor), 
                               (int)((center.getY()-radius)*pc.zoomFactor), 
                               (int)(2*radius*pc.zoomFactor), (int)(2*radius*pc.zoomFactor));

                    center = new Point2D.Double( p1.getX() - Math.cos(inlayAngle+Math.PI)*radius, -p1.getY() - Math.sin(inlayAngle+Math.PI)*radius);        
                    g.drawOval((int)((center.getX()-radius)*pc.zoomFactor), 
                               (int)((center.getY()-radius)*pc.zoomFactor), 
                               (int)(2*radius*pc.zoomFactor), (int)(2*radius*pc.zoomFactor));
                    
                    p0 = lines.get(RECTANGLE_GCODE_LINE_NUMBER);
                    p1 = lines.get(RECTANGLE_GCODE_LINE_NUMBER+3);
                }            

                double h = (radius-inD);
                double offset = Math.sqrt( radius * radius - h*h);  
                double x1 = pc.zoomFactor*(p0.getX()-Math.cos(rotationAngle-Math.PI/2)*offset);
                double y1 = -pc.zoomFactor*(p0.getY()-Math.sin(rotationAngle-Math.PI/2)*offset);  
                double x2 = pc.zoomFactor*(p1.getX()+Math.cos(rotationAngle+Math.PI/2)*offset);
                double y2 = -pc.zoomFactor*(p1.getY()+Math.sin(rotationAngle+Math.PI/2)*offset);  
                g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2);
                
                
                 x1 = pc.zoomFactor*(p0.getX()+Math.cos(rotationAngle-Math.PI/2)*offset);
                 y1 = -pc.zoomFactor*(p0.getY()+Math.sin(rotationAngle-Math.PI/2)*offset);  
                 x2 = pc.zoomFactor*(p1.getX()-Math.cos(rotationAngle+Math.PI/2)*offset);
                 y2 = -pc.zoomFactor*(p1.getY()-Math.sin(rotationAngle+Math.PI/2)*offset);
                 g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2);
            }
        }
        super.paint(pc); 
    }
        
    

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        radius = parseParameterDouble( lines.get(0));
        len = parseParameterDouble( lines.get(1));
        inlayDepth = parseParameterDouble( lines.get(2));
        revalidate(lines.get(RECTANGLE_GCODE_LINE_NUMBER), true);
        return line;
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
    public String toString() {
        return name + " (cylinder)";
    }
}
