/*
 * Copyright (C) 2019 Clément GERARDIN @ Marseille.fr
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
public final class GDrillPoint extends G1Path {
    
    public static final String HEADER_STRING = "(DrillPoint-name: ";
    public static final GCode DRILL_COMMENT = new GCode("; A drill point");
    static final int DRILL_GCODE_LINE_NUMBER = 1;
    
    /**
     * Create a Drill point
     * @param name0
     * @param position the drill position
     */
    public GDrillPoint(String name0, Point2D position) {
        super(name0);
        lines.add(DRILL_COMMENT);
        if ( position != null) 
            lines.add(new GCode(81, position.getX(), position.getY()));
        else 
            lines.add(new GCode(81, 0, 0));
    }

    /**
     * Create a non initialised GCylindricalPocket.
     * @param name0 name of the new element.
     */
    public GDrillPoint(String name0) {
        super(name0);
    }


    @Override
    public void add(int pos, GCode p) { 
        if ( lines.size() < DRILL_GCODE_LINE_NUMBER+1) {
            if ( p.isADrill() && lines.isEmpty()) lines.add(DRILL_COMMENT);
            super.add(lines.size(), p);
        }
    }
    
    @Override
    public void setLine(int row, GCode l) {
        String line = l.toString();
        if ( row==DRILL_GCODE_LINE_NUMBER) {
            boolean hasP=false, hasQ=false, ignore;
            String ignoredWords=";";
            GCode ok = new GCode();
            for( GWord w : new GCode(line)) {
                ignore=false;
                switch( w.getLetter()) {
                    case 'G':
                    case 'X': 
                    case 'Y':
                    case 'R': break;
                    case 'P': hasP=true; break;
                    case 'Q': hasQ=true; break;
                    case 'Z': properties.zEnd = w.getValue(); ignore=true; break;
                    default:
                        ignore=true;
                }
                if ( ignore) {
                    if ( w.getLetter() != ';') ignoredWords += " " + w;
                }
                else ok.add(w);
            }
            if ( hasQ) ok.setG(83);
            else if ( hasP) ok.setG(82);
            else ok.setG(81);
            if ( ignoredWords.length() > 1) ok.add( new GWord(ignoredWords));
            
            if ( getFirstPoint() == null) add(ok);
            else lines.set(lines.indexOf(getFirstPoint()), ok);
            informAboutChange();
        }
    }
    
    @Override
    public String getSummary() {
        return "<html>A drill point<br>It simulate a G8{1,2,3} GCodes on XY plan.<br>"+
                "Only P, Q and R words are used.<br>"+
                "<i>Pass depth</i> is used like the Q word<br>"+
                "<i>Z End</i> is used as <b>absolute</b> value and replace Z word"+
                "</html>";
    }
    
    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+"\n");
        fw.append(lines.get(DRILL_GCODE_LINE_NUMBER).toString()+"\n");
        return lines.get(DRILL_GCODE_LINE_NUMBER);
    }
    
    @Override
    public GElement flatten() {
        G1Path res = new G1Path("flatten-"+name);
        res.add((GCode) lines.get(DRILL_GCODE_LINE_NUMBER).clone());
        if ( properties != null) res.properties = properties.clone();
        return res;
    }   

    @Override
    public void paint(PaintContext pc) {
        final double zoomFactor = pc.zoomFactor;
        final Graphics g = pc.g; 
        int x = (int)(lines.get(DRILL_GCODE_LINE_NUMBER).getX() * zoomFactor);
        int y = -(int)(lines.get(DRILL_GCODE_LINE_NUMBER).getY() * zoomFactor);
        //int diam = (int)Math.ceil(toolDiameter*zoomFactor/2), l2= diam+5; 
        int diam=4, l2=10;
        if ( pc.color != Color.darkGray) {
            g.setColor(Color.red);
            g.drawLine((int)(x-l2), (int)(y - l2), (int)(x+l2), (int)(y + l2));
            g.drawLine((int)(x+l2), (int)(y - l2), (int)(x-l2), (int)(y + l2));
            g.setColor(pc.color);
        }
        g.drawOval((int)(x-diam), (int)(y - diam), 2*diam, 2*diam);
        super.paint(pc);
    }

    @Override
    public GElement clone() {
        return new GDrillPoint(name, lines.get(DRILL_GCODE_LINE_NUMBER));
    }


    @Override
    public CharSequence toSVG(Rectangle2D origin) { 
        return "<!-- GDrillPoint to SVG is not implemented.-->\n";
    }

    @Override
    public GCode remove(int i) { 
        return null;
    }
    
    @Override
    public boolean contains(GCode line) {
        return lines.get(DRILL_GCODE_LINE_NUMBER).distance(line) < 0.000001;
    }
    
    @Override
    public void removeAll(ArrayList<GCode> lines) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }

    @Override
    public void reverse() { }

    @Override
    public void rotate(Point2D transformationOrigin, double d) { }

    @Override
    public void scale(Point2D transformationOrigin, double dx, double unused) { }

    @Override
    public void simplify(double angleMin, double distanceMax) { }
 
    
    @Override
    public boolean concat(GElement get, double d) {
        throw new UnsupportedOperationException("Can't concat this element (GDrillPoint)");
    }

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String l = super.loadProperties(stream, null);
        if ( l == null) return null;
        GCode gcl = new GCode(l);
        if ( gcl.isComment()) {
            lines.add(gcl);
            gcl = new GCode(stream.readLine());
        } else
            lines.add(new GCode(DRILL_COMMENT));
        
        if ( gcl.isADrill()) // TODO use getFirstPoint to find drill GCODE
            setLine(DRILL_GCODE_LINE_NUMBER, gcl);
        else 
            throw new Error("No drill point read");
        
        return stream.readLine();
    }

    @Override
    public String toString() {
        return name + "(drilling)";
    }
    
    
}
