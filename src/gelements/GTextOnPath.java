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
import gcodeeditor.GFont;
import gcodeeditor.GWord;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Clément
 */
public class GTextOnPath extends GElement implements ListDataListener {
    
    public static final String HEADER_STRING = "(TextOnPath-name: ";
    public static final String PATH_GUIDE_NAME_HEADER = "[PathGuide]";
    public static final String FONT_NAME_HEADER = "(Font: ";
    public static final String TEXT_HEADER = "(Text: ";
    public static final String HEIGHT_HEADER = "(Height: ";
    
    public static final String LINE_FONT_HEADER = ";Font=";
    public static final String LINE_TEXT_HEADER = ";Text=";
    public static final String LINE_HEIGHT_HEADER = ";Height=";
    
    private String text;
    private GFont font;
    private GElement pathGuide;
    /** The rawTextPaths to map to path */
    private GGroup rawTextPaths;
    private GGroup mappedText;
    private double textHeight;
    
    /**
     * Create a new Text on Path element.
     * @param name the name of this element
     * @param text the text to map
     * @param textHeight the height of the text
     * @param font the font to render the text
     * @param textPaths the representation of the <i>text</i> with the <i>fontName</i> (can be null)
     */
    public GTextOnPath(String name, GFont font, double textHeight, String text, GGroup textPaths) {
        super(name);
        this.text = text;
        this.font = font;
        if ( textPaths != null) rawTextPaths = textPaths;           
        else rawTextPaths = font.getTextPaths(text); 
        setTextHeight( textHeight);
    }
    

    /**
     * Create an unitialised element
     * @param name 
     */
    public GTextOnPath(String name) {
        super(name);
    }
    
    public final void setTextHeight(double textHeight) {
        if ( Double.isNaN(textHeight) || (textHeight < 0.1)) return;
        Rectangle2D b = rawTextPaths.getBounds();
        if ( ! GWord.equals(b.getHeight(), textHeight)) {
            double s = textHeight/b.getHeight();
            rawTextPaths.scale(new Point2D.Double(0,0), s, s);
            this.textHeight = textHeight;
            informAboutChange();
        }
    }
    
    /**
     * Set the path the text will run on.
     * @param pathGuide inserted in the element that has to be removed from the document.
     */
    public void setPath( GElement pathGuide) {
        this.pathGuide = pathGuide;
        pathGuide.setName(PATH_GUIDE_NAME_HEADER + getName());
        pathGuide.addListDataListener(this);
        informAboutChange();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        informAboutChange();
    }

    @Override
    public void paint(PaintContext pc) {
        remapText();

        if ( pc.editedElement == this) {
            PaintContext pc2 = pc.clone();
            pc2.paintReperes = pc2.showStartPoints = false;
            pc2.color = Color.lightGray;
            mappedText.paint(pc2);
            if ( pc.editedElement == this) pc.editedElement = pathGuide;
            pathGuide.paint(pc);
        } else {
            Color c = pc.color;
            pc.color = Color.DARK_GRAY;
            pathGuide.paint(pc);
            pc.color = c;
            pc.paintReperes = pc.showStartPoints= false;
            mappedText.paint(pc);
        }
    }
    
    private void remapText() {
        if ( mappedText != null) return;
        
        mappedText = new GGroup("mappedText");
        if ( pathGuide == null) return;
        
        Segment2D s = null;
        double angle=0, len = -1, curLen = 0;
        GCode lastP = null;
        
        GElement p = (pathGuide instanceof G1Path) ? pathGuide : pathGuide.flatten();
        
        Iterator<GCode> pts = p.getPointsIterator().iterator();
        boolean pathFinished = ! pts.hasNext();
        for ( GElement e : rawTextPaths.getAll()) {         
            double middle = e.getBounds().getCenterX();
            GElement clone = e.clone();
             
            while( ! pathFinished && (len < middle)) {                
                GCode p2 = pts.next();
                
                if ( lastP != null) {
                    s = new Segment2D(lastP, p2);
                    curLen = len;
                    len += s.getLength();
                    angle = s.getAngle();
                }
                lastP = p2;
                pathFinished |= ! pts.hasNext();
            }
                     
            if ( s != null) {
                Point2D pt = s.getPointAt(middle - curLen);
                clone.translate( pt.getX() - middle, pt.getY());
                clone.rotate(pt, angle);
            }
            mappedText.add(clone);
        }
    }
    
    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString()+")\n");
        fw.append(FONT_NAME_HEADER + font.getName() + ")\n");
        fw.append(HEIGHT_HEADER + rawTextPaths.getBounds().getHeight() + ")\n");
        fw.append(TEXT_HEADER + text + ")\n");
        return pathGuide.saveToStream(fw, lastPoint);
    }
    
    /**
     * Load parameters from a stream. 
     * @param stream
     * @param lastGState
     * @return
     * @throws IOException 
     */
    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = super.loadFromStream(stream, null);
        font = GFont.decode(line.substring(FONT_NAME_HEADER.length(), line.length()-1));
        line = stream.readLine();
        double height = Double.parseDouble(line.substring(HEIGHT_HEADER.length(), line.length()-1));
        line = stream.readLine();
        text = line.substring( TEXT_HEADER.length(), line.length()-1);
        rawTextPaths = font.getTextPaths(text);
        // read the path
        pathGuide = buildGElement(stream.readLine());    
        line = pathGuide.loadFromStream(stream, lastGState);
        pathGuide.addListDataListener(this);
        setTextHeight(height);
        return line;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public GElement clone() {
        try {
            GTextOnPath clone = new GTextOnPath(name);
            clone.properties = properties.clone();
            clone.text = ""+text;
            clone.textHeight = textHeight;
            clone.font = GFont.decode(font.getName());
            clone.pathGuide = pathGuide.clone();
            clone.rawTextPaths = rawTextPaths.clone();
            return clone;
        } catch (IOException ex) {
            Logger.getLogger(GTextOnPath.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return getName() + "(MText)";
    }
    
    @Override
    public Object getElementAt(int index) {
        if ( index == 0) return new GCode(";Font=" + font);
        if ( index == 1) return new GCode(";Text=" + text);
        if ( index == 2) return new GCode(";Height=" + GWord.roundForGCODE(rawTextPaths.getBounds().getHeight()));
        return pathGuide.getElementAt(index-3);
    }
    @Override
    public GCode getLine(int i) {
        if ( i == 0) return new GCode(";Font=" + font);
        if ( i == 1) return new GCode(";Text="+text);
        if ( i == 2) return new GCode(";Height=" + GWord.roundForGCODE(rawTextPaths.getBounds().getHeight()));
        return pathGuide.getLine(i-3);
    }
    @Override
    public void setLine(int row, GCode value) {
        String s;
        switch (row) {
            case 0:
                //throw AssertionError("Can't modify this line");
                break;
            case 1:
                if ((s=value.toString()).startsWith(LINE_TEXT_HEADER)) {
                    rawTextPaths = font.getTextPaths(text = s.split("=",2)[1]);
                    setTextHeight(textHeight);
                    informAboutChange();
                } else {
                    // Assume all is the text
                  rawTextPaths = font.getTextPaths(text = s);
                    //informAboutChange();
                    break;
                }
                
            case 2:
                if ((s=value.toString()).contains("=")) {
                    try {
                        double h = Double.parseDouble( s.split("=")[1]);
                        setTextHeight(h);
                    } catch ( NumberFormatException e) { }
                }
                break;
            default:
                pathGuide.setLine(row-3, value);
                break;
        }
    }
    @Override
    public int getSize() {
        return pathGuide.getSize()+3;
    }
    @Override
    public Iterator<GCode> iterator() {
        Iterator<GCode> it = new Iterator<GCode>() {
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < path.size();
            }
            @Override
            public GCode next() {
                return path.getLine(currentIndex++);
            }
            @Override
            public void remove() {
                path.remove(currentIndex);
            }
            GTextOnPath path;
            public Iterator<GCode> setPath( GTextOnPath p) {
                path=p;
                return this;
            }
        }.setPath(this);
        return it;
    }
    @Override
    public Iterable<GCode> getPointsIterator() {
        return pathGuide.getPointsIterator();
    }
    @Override
    public boolean add(GCode line) {
        return pathGuide.add(line);
    }
    @Override
    public void add(int pos, GCode line) {
        if ( pos < 3) pos=3;
        pathGuide.add(pos, line);
    }
    @Override
    public boolean concat(GElement it, double tolerance) {
        return pathGuide.concat(it, tolerance);
    }
    @Override
    public GCode getFirstPoint() {
        return pathGuide.getFirstPoint();
    }
    @Override
    public GCode getLastPoint() {
        if ( pathGuide != null) 
            return pathGuide.getLastPoint();
        else 
            return null;
    }
    @Override
    public GCode getCloserPoint(Point2D from, double dmax, ArrayList<GCode> discareIt, boolean excludeFirst) {
        GCode p = pathGuide.getCloserPoint(from, dmax, discareIt, excludeFirst);
        if ( p == null) {
            remapText();
            return mappedText.getCloserPoint(from, dmax, discareIt, excludeFirst);
        }
        return p;
    }
    @Override
    public int getNbPoints() {
        return pathGuide.getNbPoints();
    }
    @Override
    public boolean contains(GCode point) {
        return pathGuide.contains(point);
    }
    @Override
    public Rectangle2D getBounds() {
        return pathGuide.getBounds();
    }
    @Override
    public double getLenOfSegmentTo(GCode point) {
        return pathGuide.getLenOfSegmentTo(point);
    }
    @Override
    public int getIndexOfPoint(GCode point) {
        return pathGuide.getIndexOfPoint(point) + 3;
    }
    @Override
    public double getDistanceTo(GCode pt) {
        return pathGuide.getDistanceTo(pt);
    }
    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double dx, double dy) {
        return pathGuide.movePoints(selectedPoints, dx, dy);
    }
    @Override
    public boolean movePoint(GCode point, double dx, double dy) {
        return pathGuide.movePoint(point, dx, dy);
    }
    @Override
    public void translate(double dx, double dy) {
        pathGuide.translate(dx, dy);
    }
    @Override
    public Object remove(int i) {
        if ( i < 3) return null;
        return pathGuide.remove(i-3);
    }
    @Override
    public void removeAll(ArrayList<GCode> lines) {        
        pathGuide.removeAll(lines);
    }
    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) {
        pathGuide.removeByDistance(points, distance);
    }
    @Override
    public void reverse() {
        pathGuide.reverse();
    }
    @Override
    public void rotate(Point2D origin, double angle) {
        pathGuide.rotate(origin, angle);
    }
    @Override
    public void scale(Point2D origin, double sx, double sy) {
        setTextHeight(textHeight * sy);
        pathGuide.scale(origin, sx, sy);
    }
    @Override
    public void simplify(double angleMin, double distanceMax) {
        pathGuide.simplify(angleMin, distanceMax);
    }

    
    public void changeFont(GFont choosedFont, double height) {
        font = choosedFont;
        rawTextPaths = font.getTextPaths(text);      
        setTextHeight(textHeight = ((height == Double.NaN) ? textHeight : height));
        informAboutChange();  
    }


    @Override
    public GCode insertPoint(GCode pt) {
        return pathGuide.insertPoint(pt);
    }
    @Override
    public int size() {
        return pathGuide.size()+3;
    }

    @Override
    public boolean isoScaling() {
        return true;
    }
    
    @Override
    public boolean isClosed() {
        return pathGuide.isClosed();
    }
    
    @Override
    public String getSummary() {
        return "<html>Text on path:<br>"+text+"</html>";
    }
    
    @Override
    public boolean isEmpty() {
        return pathGuide.isEmpty();
    }

    @Override
    public GElement flatten() {
        remapText();
        return mappedText.flatten();
    }
    @Override
    public Point2D getCenter() {
        remapText();
        return mappedText.getCenter();
    }
    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        remapText();
        mappedText.toDXF(out);
    }
    
    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        remapText();
        return mappedText.toSVG(origin);
    }
    
    @Override
    public Area getOffsetArea(double param) {
        remapText();
        return mappedText.getOffsetArea(param);
    }

    @Override
    protected void informAboutChange() {
        mappedText = null;
        super.informAboutChange();
    }
    @Override
    public void intervalAdded(ListDataEvent e) { }
    @Override
    public void intervalRemoved(ListDataEvent e) { }

    /**
     * Return the true GElements corresponding to the text to render.
     * @return
     */
    GGroup getGText() {
        remapText();
        return mappedText;
    }

    @Override
    double getLength() {
        return Double.NaN;
    }

    public GFont getFont() {
        return font;
    }

    public double getFontSize() {
        return textHeight;
    }

    /**
     * @return the text to paint
     */
    public String getText() {
        return "" + text;
    }

    /** Change the text to paint on path */
    public void setText(String choosedText) {
        text = "" + choosedText;
        rawTextPaths = font.getTextPaths(text);
        informAboutChange();
    }
}
