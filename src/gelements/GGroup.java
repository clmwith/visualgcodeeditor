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
import gcodeeditor.JProjectEditor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * A group of GElement
 * @author Clément
 */
public class GGroup extends GElement implements Iterator<GElement> {
    
    public static final String HEADER_STRING = "(Start Group-name : ";
    public static final String END_HEADER_STRING = "(End Group-name : ";
    
    public static final double DEFAULT_MERGE_DISTANCE = 0.0002;

    ArrayList<GElement> elements;
    
    public GGroup(String name0) {
        super(name0);
        elements = new ArrayList<>();
    }

    /**
     * Create a group with all <i>elements</i> or this clones
     * @param elements 
     */
    public GGroup(ArrayList<GElement> elements, boolean useClones) {
        this("group");
        elements.forEach((e) -> { 
            this.elements.add( useClones ? e.clone() : e); 
        });
    }
    
    /**
     * Create a group with all clone of <i>elements</i>
     * @param elements 
     */
    public GGroup(String name0, ArrayList<GElement> elements) {
        this(name0);
        elements.forEach((e) -> { this.elements.add( e.clone()); });
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public GElement getElementAt(int index) {
        return elements.get(index);
    }

    public GElement get(int index) {
        return elements.get(index);
    }
    
    public void add( GElement e) {
        elements.add(e);
        informAboutChange();
    }
    
    public void add( int i, GElement e) {
        elements.add(i, e);
        informAboutChange();
    }
    
    public Iterable<GElement> getIterable() {
        return elements;
    }
    
    public GElement getElementFromPoint(GCode pt, double dmin, ArrayList<GElement> intoThis) {
        GElement res = null; 
        ArrayList<GElement> l = (((intoThis == null) || intoThis.isEmpty()) ? elements : intoThis);
        for( GElement s : l) {
            double d = s.getDistanceTo(pt);
            if ( dmin > d) {
                res = s;
                dmin = d;
            }   
        }
        return res;
    }

    @Override
    public GCode getCloserPoint(java.awt.geom.Point2D pt, double dmin, ArrayList<GCode> discareIt, boolean excludeFirst) {
        GCode res = null;
        for( GElement s : elements) {
            GCode l = s.getCloserPoint(pt, dmin, discareIt, false);
            if (l != null) {
                double d = l.distance(pt);
                if ( dmin > d) {
                    res = l;
                    dmin = d;
                }    
            }
        }
        
        return res;
    }
    
    @Override
    public boolean isoScaling() {
        if (elements.stream().anyMatch((e) -> (e.isoScaling()))) {
            return true;
        }
        return false;
    }
    

    @Override
    public GGroup clone() {
        GGroup clone = new GGroup(name);
        if ( properties != null) clone.properties = properties.clone();
        elements.forEach((b) -> { 
            clone.elements.add((GElement) b.clone());
        });
            
        return clone;
    }
    
    @Override
    public GGroup cloneWithSameID() {
        GGroup clone = new GGroup(name);
        clone.id = id;
        if ( properties != null) clone.properties = properties.clone();
        elements.forEach((b) -> { 
            clone.elements.add((GElement) b.cloneWithSameID());
        });
            
        return clone;     
    }


    /**
     * Remove <i>e</e> from this group
     * @param e the element to remove
     * @return true if the element was removed.
     */
    public boolean remove(GElement e) {
        if ( elements.remove(e)) {
            informAboutChange();
            return true;
        }
        return false;
    }
    
    @Override
    public GElement remove( int i) {
        GElement e = elements.remove(i); 
        informAboutChange();
        return e;
    }

    public int indexOf(GElement e) {
        return elements.indexOf(e);
    }

    @Override
    public double getDistanceTo(GCode pt) {
        double dmin = Double.POSITIVE_INFINITY;
        for ( GElement e : elements) {
            double d = e.getDistanceTo(pt);
            if ( dmin > d) dmin = d;
        }
        return dmin;
    }

    @Override
    public void rotate(Point2D transformationOrigin, double d) {
        elements.forEach((e) -> {
            e.rotate(transformationOrigin, d);            
        });
    }

    @Override
    public void scale(Point2D transformationOrigin, double d, double d0) {
        elements.forEach((e) -> {
            e.scale(transformationOrigin, d, d0);        
        });
    }

    @Override
    public void translate(double d, double d0) {
        elements.forEach((e) -> { 
            e.translate(d, d0);
        });
    }
    
    @Override
    public void transform(AffineTransform t) {
        elements.forEach((o) -> { 
            o.transform( t);
        });
    }
    
    @Override
    public void reverse() {
        elements.forEach((e) -> { 
            e.reverse();
        });
    }

    @Override
    public Area getOffsetArea(double param) {
        Area a = new Area();
        elements.forEach((e) -> {
            a.add( e.getOffsetArea(param));
        });
        return a;
    }

    /**
     * Add all elements into this group without cloning anything
     * @param paths 
     */
    public void addAll(ArrayList<GElement> paths) {
        elements.addAll( paths);
        informAboutChange();
    }
    
    /**
     * @param e
     * @return true if <i>e</i> is directly in this group
     */
    public boolean contains(GElement e) {
        return elements.contains( e);
    }

    /** Remove elements from this group recursively.<br>
     *  After the call, <i>selectedElement</i> contains all element that was not in this group.
     * @param toRemove */
    public void removeAllElements(ArrayList<GElement> toRemove) {
        ArrayList<GElement> keeped = new ArrayList<>();
        for( GElement e : toRemove)
            if ( elements.contains(e) ) remove(e);
            else {
                boolean removed = false;
                for ( GElement el : toRemove)
                    if ( (el instanceof GGroup) && ((GGroup)el).remove(e)) {
                        removed = true;
                        break;
                    }
                if ( ! removed) keeped.add(e);
            }
        toRemove.clear();
        toRemove.addAll(keeped);
    }

    @Override
    /**
     * Return number of elements in this group
     */
    public int size() { 
        return elements.size();
    }


    @Override
    public String getSummary() {
        return "<html>Group "+name+"<br>"+elements.size()+" element(s)<br></html>";
    }

    /**
     * @param e the GGroup to ungroup
     * @return true if sth has changed
     */
    public boolean ungroup(GElement e) {
        if ( e instanceof GGroup) {
            int i = elements.indexOf(e);
            
            elements.remove(e);
            ArrayList<GElement> els = ((GGroup) e).clear();
            if ( els != null) elements.addAll(i, els); 
            informAboutChange();
            return true;
        }
        return false;
    }
    
    /**
     * Totally clear this group.
     * @return all the elements of this group
     */
    public ArrayList<GElement> clear() {
        ArrayList<GElement> res = elements;
        elements = new ArrayList<>();
        informAboutChange();
        return res;
    }
    
    public GGroup group( String groupName, ArrayList<GElement> els) {
        if ( els.isEmpty()) return null;
        int pos = elements.indexOf(els.get(0));
        elements.removeAll(els);
        if ( elements.isEmpty()) pos = 0;
        else if ( pos >= elements.size()) pos = elements.size()-1;
        GGroup g;
        elements.add( pos, g = new GGroup(groupName != null ? groupName:"group", els));
        informAboutChange();
        return g;
    }

    public EngravingProperties getHeritedEngravingPropreties( GElement e) {
        if ( elements.contains(e)) {
            EngravingProperties res = properties.clone();
            res.enabled &= e.properties.enabled;
            if ( ! Double.isNaN(e.properties.feed))   res.feed = e.properties.feed;
            if ( ! Double.isNaN(e.properties.zStart)) res.zStart = e.properties.zStart;
            if ( ! Double.isNaN(e.properties.zEnd))   res.zEnd = e.properties.zEnd;
            if ( ! Double.isNaN(e.properties.passDepth)) res.passDepth = e.properties.passDepth;
            if ( e.properties.power != -1 )           res.power = e.properties.power;
            if ( e.properties.passCount != -1 )       res.passCount = e.properties.passCount;
        } else for ( GElement el : elements) 
                    if ( el instanceof GGroup) {
                        if ( ((GGroup)el).getParent(e) != null) 
                            return ((GGroup) el).getHeritedEngravingPropreties(e);
                    }
        return null;
    }
    
    /**
     * @param el
     * @return the direct parent of e in this group or null if e is not found in this group.
     */
    public GGroup getParent(GElement el) {
        
        if ( elements.contains(el)) return this;
        else {
            for( GElement e : elements) 
                if ((e instanceof GGroup) && (((GGroup)e).getParent(el) != null)) 
                    return (GGroup) e;
        }
        return null;
    }

    @Override
    public void simplify( double angleMin, double distanceMax) {
        elements.forEach((e) -> {
            e.simplify(angleMin, distanceMax);
        });
    }

    public static Rectangle2D getBounds(ArrayList<GElement> elements) {
        Rectangle2D r = null;
        for( GElement e : elements) 
            if ( r == null) r = e.getBounds();
            else if ( e.getBounds()!=null)
                    r.add( e.getBounds());
//                else
//                    System.out.println("group empty");
        return r;
    }
   
    @Override
    public Rectangle2D getBounds() {
        return getBounds(elements);
    }
    
    /** 
     * @return  the bounding box of all enabled path of this Group.
     */
    public Rectangle2D getEnabledBounds() {
        return getEnabledBounds(elements);
    }
    
    /**
     * @param elements
     * @return  the bounding box of all enabled path of this list of elements.
     */
    public static Rectangle2D getEnabledBounds(ArrayList<GElement> elements) {
        Rectangle2D r = null;
        
        for ( GElement e : elements) {
            if ( ! e.isEnabled()) continue;
            Rectangle2D r2;
            if ( e instanceof GGroup) r2 = getEnabledBounds(((GGroup) e).elements);
            else r2 = e.getBounds();
            
            if ( r == null) r = r2;
            else if ( r2 != null) r.add(r2);
        }
        return r;        
    }

    @Override
    public GCode getLastPoint() {
        if ( ! elements.isEmpty()) 
            for ( int i = elements.size()-1; i > 0; i--)
                if ( elements.get(i).getLastPoint() != null)
                    return elements.get(i).getLastPoint();
        return null;
    }

    @Override
    public GCode getFirstPoint() {
        if ( ! elements.isEmpty()) 
            for( GElement e : elements)
                if ( e.getFirstPoint() != null) return e.getFirstPoint();
        return null;
    }

    @Override
    public CharSequence toSVG(Rectangle2D origin) {
        String res = "";
        for( GElement e : elements) res += e.toSVG(origin);
        return res;
    }
    
    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {

        fw.append(HEADER_STRING + name + ")\n");
        fw.append( properties.toString()+"\n");
        
        for ( GElement e : elements) 
            lastPoint = e.saveToStream(fw, lastPoint);
        
        fw.append(END_HEADER_STRING + name + ")\n");   
        return lastPoint;
    }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) {
        elements.forEach((e) -> { 
            e.removeByDistance(null, distance);
        });
    }
    
    /** 
     * @param list the list of elements
     * @return a flat array of all elements contained in the list
     */
    public static ArrayList<GElement> toList(ArrayList<GElement> list) {
        ArrayList<GElement> res = new ArrayList<>();
        list.forEach((e) -> {
            if ( e instanceof GGroup) res.addAll( ((GGroup)e).toArray());
            else res.add(e);
        });
        return res;
    }

    @Override
    public String toString() {
        return getName() + "[" + elements.size() + "]";
    }

    /** Insert or remplace/remplace the element based on his ID.
     * @param element
     * @return true if the element has been remplaced, false if only inserted. */
    public boolean remplace(GElement element) {
        for( GElement e: elements)
            if ( e.getID() == element.getID()) {
                int i = elements.indexOf(e);
                elements.remove(e);
                elements.add( i, element);
                informAboutChange();
                return true;
            }
        elements.add( element);
        informAboutChange();
        return false;
    }

    /**
     * @return a list of all element contained in this group.
     */
    @SuppressWarnings("unchecked")
    public ArrayList<GElement> getAll() {  
        final Collection<? extends GElement> el = (Collection<? extends GElement>)elements.clone();    
        return (ArrayList<GElement>) el;
    }

    /**
     * @param it *  @return true if this group contains this eleent. */
    public boolean isParentOf(GElement it) {
        if ( elements.contains( it)) return true;
        return elements.stream().filter((e) -> ( e instanceof GGroup)).anyMatch((e) -> (((GGroup)e).isParentOf(it))); 
    } 
    
    /**
     * @return a flat list of ALL elements (recursively) contained in this group (not cloned)
     */
    public ArrayList<GElement> toArray() {
        ArrayList<GElement> res = new ArrayList<>(elements.size());
        elements.forEach((e) -> {
            if ( e instanceof GGroup)
                res.addAll(((GGroup) e).toArray());
            else
                res.add(e);
        });
        return res;
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    
    /**
     * flat all GElement of this group
     * @return a new group composed of clones of G1Paths and GGroups
     */
    @Override
    public GGroup flatten() {
        GGroup res = new GGroup("flatten-"+name);
        elements.forEach((e) -> { 
            if ( e instanceof GGroup) res.add(((GGroup)e).flatten());
            else res.add(e.flatten());
        });
        if ( properties != null) res.properties = properties.clone();
        return res;
    }
    
    /** Join elements recursivemly.
     * @param tolerance the maximal distance to consider two elements joinable.
     * @return true if at least two elements have been joined. 
     */
    public boolean joinElements(double tolerance) {
        boolean joined;
        boolean modif = false;
        do
        {
            joined=false;
            for (GElement s1 : elements) {;
                if ( s1 instanceof GGroup) 
                    if (joined |= ((GGroup) s1).joinElements(tolerance)) {
                        if ( s1.size()==1) {
                            elements.add(((GGroup) s1).get(0));
                            elements.remove(s1);
                        }             
                        break;
                    }
                if ( ! (s1 instanceof G1Path) ) continue;

                for( GElement s2 : elements) {
                    if ( ! (s2 instanceof G1Path) || (s1 instanceof GPocket3D)) continue;

                    if ( s1 != s2) {
                        if ((s1.getFirstPoint() == null) || (s2.getFirstPoint() == null)) continue;
                        if ( s1.getFirstPoint().distance(s2.getLastPoint()) < tolerance) {
                            elements.remove(s1);
                            s2.concat(s1, tolerance);
                            joined=true;
                            break;
                        } else if ( (s1.getFirstPoint().distance(s2.getFirstPoint()) < tolerance) ||
                                    (s1.getLastPoint().distance(s2.getLastPoint()) < tolerance) )
                        {  
                            elements.remove(s2);
                            s2.reverse();
                            s1.concat(s2, tolerance);
                            joined=true;
                            break;
                        }
                    }   
                }
                if ( joined) break;
            }
            modif |= joined;
        } while ( joined);
        if ( modif) informAboutChange();
        return modif;
    }
    
    public GCode getCloserPoint(GCode pt, double dmax, ArrayList<GElement> discareElement, ArrayList<GCode> discarePoints) {
        GCode closer, closest = null;
        double d;
        for( GElement s : elements) {
            closer=null;
            if ( (discareElement!=null) && discareElement.contains(s)) continue;
            if ( s instanceof GGroup) closer = ((GGroup)s).getCloserPoint(pt, dmax, discareElement, discarePoints);
            else closer = s.getCloserPoint(pt, dmax, discarePoints, false);
            if ((closer != null) && ((d = pt.distance(closer)) < dmax) && ((discarePoints == null) || (discarePoints.indexOf(closer)==-1))) {
                closest = closer;
                dmax = d;
            }
        }
        return closest;
    }    

    /**
     * Find element or sub-element of this group.
     * @param id
     * @return the GElement with same 'id' or null
     */
    public GElement getElementID(int id) {
        if ( this.id == id) return this;
        GElement res;
        for( GElement e : elements)
            if ( e.getID() == id) return e;
            else if ( (e instanceof GGroup) && 
                      ((res=((GGroup)e).getElementID(id))!=null)) 
                        return res;
        return null;
    }
    
    public static double moveLength;
    /**
     * Sort this array of GElement to optimise CNC move between each.
     * @param selection the elements to optimizeMoves
     * @param lastPosition the last position of the head at start. (can be null)
     * @param recursive to optimizeMoves GGroup recursively
     * @return last position of sorted paths
     */
    public static GCode optimizeMoves(ArrayList<GElement> selection, Point2D lastPosition, boolean recursive) {
        //GCode lastPointOfCloser;
        if ( selection.isEmpty()) return (lastPosition != null) ? new GCode(lastPosition) : null;
        
        // optimizeMoves childs first
        /*
        if ( recursive)
            selection.stream().filter((e) -> 
                    ( e instanceof GGroup)).forEach((e) ->
                        { ((GGroup) e).sort(recursive); });
        */
        
        /*if ( (selection.size() == 1) ) {
            lastPoint = selection.get(0).getLastPoint();
            return lastPoint;
        }   */    
        
        GElement closer;
        for( int i = 0; i < selection.size(); i++) {
            GElement e = selection.get(i);
            
            //System.out.println(i + "=" + e);
            if ( e.getFirstPoint() == null) continue;           
            if (lastPosition==null) lastPosition = e.getFirstPoint();
            double dmin = Double.POSITIVE_INFINITY;
            
            closer=null;
            for( int j = i; j < selection.size(); j++) {
                e = selection.get(j);
                final GCode firstPoint = e.getFirstPoint();
                if ( firstPoint == null) continue;
                final double d=lastPosition.distance(e.getFirstPoint());
                
                // TODO in the betters closed next paths : try choosing the first that have endPoint near all others
                if ( (d<dmin) /*&& ((lastPointOfCloser==null) ||
                        (e.getLastPoint().distance(lastPosition) < lastPointOfCloser.distance(lastPosition)))*/) {
                    closer = e;
                    //lastPointOfCloser=closer.getLastPoint();
                    dmin=d;
                }
            }
            if ( closer == null) 
                break; // no more elements to optimizeMoves 
            
            selection.remove(closer);
            selection.add(i,closer); 
            moveLength += dmin;
            if ( recursive && (closer instanceof GGroup)) ((GGroup)closer).sort( lastPosition, true);
            lastPosition=closer.getLastPoint();
        }
        return selection.get(selection.size()-1).getLastPoint();
    } 
    
    /** Reorder elements of this group.
     * @param lastPosition
     * @param recursive
     * @return the last point of the last element of this group. */
    public GCode sort( Point2D lastPosition, boolean recursive) {
        GCode c = optimizeMoves(elements, lastPosition, recursive);
        informAboutChange();
        return c;
    }
    
    public static void exportToDXF(String DXFfileName, GElement element, boolean flattenSpline) throws FileNotFoundException, IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(DXFfileName));       
	// header
        out.write("999\nDXF created by SimpleGCodeVisualEditor v"+JProjectEditor.SVGE_RELEASE+"\n");
        out.write("  0\nSECTION\n");
        out.write("  2\nHEADER\n");
        out.write("  9\n$ACADVER\n1\nAC1006\n");
        out.write("  9\n$INSBASE\n");
        out.write(" 10\n1000\n");
        out.write(" 20\n1000\n");
        out.write(" 30\n0.0\n");
        out.write("  9\n$EXTMIN\n");
        out.write(" 10\n0\n"); // "10\n"+robot.getSettings().getPaperLeft()+"\n");
        out.write(" 20\n0\n");  // "20\n"+robot.getSettings().getPaperBottom()+"\n");
        out.write(" 30\n0.0\n");
        out.write("  9\n$EXTMAX\n");
        out.write(" 10\n1000\n"); // 10\n"+robot.getSettings().getPaperRight()+"\n");
        out.write(" 20\n1000\n"); // 20\n"+robot.getSettings().getPaperTop()+"\n");
        out.write(" 30\n0.0\n");
        out.write("  0\nENDSEC\n");
        // tables section
        out.write("  0\nSECTION\n");
        out.write("  2\nTABLES\n");
        // line type
        out.write("  0\nTABLE\n");
        out.write("  2\nLTYPE\n");
        out.write(" 70\n1\n");
        out.write("  0\nLTYPE\n");
        out.write("  2\nCONTINUOUS\n");
        out.write(" 70\n64\n");
        out.write("  3\nSolid line\n");
        out.write(" 72\n65\n");
        out.write(" 73\n0\n");
        out.write(" 40\n0.000\n");
        out.write("  0\nENDTAB\n");
        // layers
        out.write("  0\nTABLE\n");
        out.write("  2\nLAYER\n");
        out.write(" 70\n6\n");
        out.write("  0\nLAYER\n");
        out.write("  2\n1\n");
        out.write(" 70\n64\n");
        out.write(" 62\n7\n");
        out.write("  6\nCONTINUOUS\n");
        out.write("  0\nLAYER\n");
        out.write("  2\n2\n");
        out.write(" 70\n64\n");
        out.write(" 62\n7\n");
        out.write("  6\nCONTINUOUS\n");
        out.write("  0\nENDTAB\n");
        out.write("  0\nTABLE\n");
        out.write("  2\nSTYLE\n");
        out.write(" 70\n0\n");
        out.write("  0\nENDTAB\n");
        // end tables
        out.write("  0\nENDSEC\n");
        // empty blocks section (good form?)
        out.write("  0\nSECTION\n");
        out.write("  0\nBLOCKS\n");
        out.write("  0\nENDSEC\n");
        // now the lines
        out.write("  0\nSECTION\n");
        out.write("  2\nENTITIES\n");        
        //if ( element instanceof GGroup) exportToDXF(out, (GGroup)element);
        //else exportToDXF( out, element);  
        ((GGroup)element).toDXF(out, flattenSpline);
        out.write("  0\nENDSEC\n");
        out.write("  0\nEOF\n");
        out.flush();
    }
    
    public void toDXF(OutputStreamWriter out, boolean flattenSPline) throws IOException {
        for( GElement e : elements) 
            if ( e instanceof GGroup) ((GGroup)e).toDXF(out, flattenSPline);
            else 
                if ( flattenSPline && ((e instanceof GMixedPath) ||
                        (e instanceof GSpline)))
                            e.flatten().toDXF(out);
                else e.toDXF(out);
    }
    
    @Override
    public void toDXF(OutputStreamWriter out) throws IOException {
        for( GElement e : elements) e.toDXF(out);
    }
    
    public static void exportToSVG(String fileName, GGroup document) throws FileNotFoundException, IOException {
        Rectangle2D r = document.getBounds();
        if ( r == null) return;
        
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName));     
        out.write("<svg width=\"" + (int)r.getWidth() + "\" height=\"" + (int)r.getHeight() + "\">\n");
        exportToSVG(out, document, r);       
        out.write("</svg>\n");
        out.close();
    }
    
    private static void exportToSVG(OutputStreamWriter out, GGroup gGroup, Rectangle2D origin) throws IOException {
        for(GElement element : gGroup.elements) {
            if ( element instanceof GGroup) exportToSVG(out, (GGroup)element, origin);
            else out.write(element.flatten().toSVG(origin).toString());
        }
    }

    @Override
    public boolean add(GCode coordSnapPointFor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean movePoints(ArrayList<GCode> selectedPoints, double d, double d0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean movePoint(GCode point, double dx, double dy) {
        ArrayList<GCode> l = new ArrayList<>();
        l.add(point);
        return movePoints(l, dx, dy);
    }

    @Override
    public void removeAll(ArrayList<GCode> lines) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLine(int row, GCode value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean concat(GElement get, double d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GCode getLine(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean contains(GCode line) {
        return elements.stream().anyMatch((e) -> ( e.contains(line)));
    }
    
    
    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GElement next() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void paint(PaintContext pc) {
        for ( GElement e : elements) e.paint(pc);
    }
    
    @Override
    public int getIndexOfPoint(GCode highlitedPoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNbPoints() {
        int res = 0;
        res = elements.stream().map((e) -> e.getNbPoints()).reduce(res, Integer::sum);
        return res;
    }

    /*@Override
    public GCode getPoint(int p) {
        for( GElement e : elements)
            if ( p < e.getNbPoints()) return e.getPoint(p);
            else p -= e.getNbPoints();
        return null;
    }*/

    @Override
    public double getLenOfSegmentTo(GCode highlitedPoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<GCode> iterator() {
        return new Iterator<GCode>() {
            GElement current = null;
            Iterator<GCode> curLine;
            boolean finished = false;
            
            @Override
            public boolean hasNext() {
                if ( current==null) {
                    current = get(0);
                    curLine = current.iterator();
                } else 
                    if ( ! curLine.hasNext()) nextShape();
                return ! finished;
            }

            @Override
            public GCode next() {
                if ( current==null) {
                    if ( finished) return null;
                    current = get(0);
                    curLine = current.iterator();
                }
                if ( ! curLine.hasNext()) nextShape();
                if ( finished) return null;
                return curLine.next();
            }
            
            public void nextShape() {
                do {
                    if ( indexOf(current) == (size()-1)) finished = true;
                    else curLine = (current = get(indexOf(current)+1)).iterator();
                } while ( ! finished && ! curLine.hasNext());
            }
        };
    }

    @Override
    public void add(int pos, GCode line) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable<GCode> getPointsIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @param el the element to find in
     * @param startWithMe if true, returns the child group instead of this object
     * @return the furhest parent of el or null if el is not in this obkect or his hierarchi
     */
    public GGroup getFirstParentOf(GElement el, boolean startWithMe) {
        for (GElement e : elements) {
            if ( el == e) return this;
            else if ( e instanceof GGroup) {
                GGroup res = ((GGroup) e).getFirstParentOf(el, false);
                if (res != null) {
                    if ( startWithMe) return (GGroup) e;
                    else return this;
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean hasCustomProperties(boolean withEnable) {
        for( GElement e : elements)
            if ( e.isEnabled() && e.hasCustomProperties(withEnable)) return true;

        return super.hasCustomProperties(withEnable);
    }

    public boolean containsDisabledElement() {
        for (GElement e : elements)
            if ( ! e.isEnabled() ) return true;
            else 
                if ( e instanceof GGroup)
                    return ((GGroup) e).containsDisabledElement();
                
        return false;
    }
    
    /**
     * Clean <i>commonProps</i> of properties that are not common of all element of <i>list</i>.
     * @param commonProps
     * @param list 
     */
    public static void makeCommonProperties(EngravingProperties commonProps, ArrayList<GElement> list) {
        boolean first = true;
        for( GElement e : list)
            if ( first) {
                EngravingProperties.udateHeritedProps(commonProps, e.properties);
                first = false;
            } else {         
                EngravingProperties p = e.properties;               
                if ( commonProps.getFeed() != p.getFeed()) commonProps.setFeed(Double.NaN);
                if ( p.getPower() != commonProps.getPower()) commonProps.setPower(-1);
                if ( p.getZStart() != commonProps.getZStart()) commonProps.setZStart(Double.NaN);
                if ( p.getZEnd() != commonProps.getZEnd()) commonProps.setZEnd(Double.NaN);
                if ( p.getPassDepth() != commonProps.getPassDepth()) commonProps.setPassDepth(Double.NaN);
                if (p.getPassCount() != commonProps.getPassCount()) commonProps.setPassCount(-1); 
            }
    }

    @Override
    public Point2D getCenter() {        
        Rectangle2D bounds = null;        
        for ( GElement e : elements) {
            if ( e.getBounds() != null)
                if ( bounds == null) bounds = e.getBounds();
                else bounds.add(e.getBounds());
        }
        if ( bounds == null) return null;
        return new Point2D.Double( bounds.getCenterX(), bounds.getCenterY());  
    }

    @Override
    double getLength() {
        return Double.NaN;
    }

    /** 
     * Remove empty group or group that contains only one element
     */
    public void removeExtraGroups() {
        GElement g;
        boolean restart;
        do {
            restart = false;
            for( GElement e : elements) {
                if ( e instanceof GGroup) {  
                    ((GGroup)e).removeExtraGroups();
                    
                    if (((GGroup)e).size() == 1) elements.add(((GGroup)e).remove(0));
                    
                    if (((GGroup)e).isEmpty()) {
                        remove(e);
                        restart = true;
                        break;
                    }
                }
            }
        } while ( restart);
        
        while ( (elements.size() == 1) && ((g=elements.get(0)) instanceof GGroup)) {
            remove(0);
            addAll(((GGroup)g).elements);
        }
    }

    /**
     * Put all points of all shap in this group into the array <i>points</i>
     * 
     * @param points the array to put real points (do not modify it directly !)
     */
    public void addAllPointForHull(ArrayList<GCode> points) {
        for( GElement el : elements) {
            if ( el instanceof GGroup) {
                ((GGroup)el).addAllPointForHull(points);
            } else {
                GElement el2 = el.flatten();
                for ( GCode p : el2) {
                    if ( p.isAPoint() && (p.getG()==1)) {
                        if ( points.isEmpty() || ! points.get(points.size()-1).isAtSamePosition(p)) 
                            points.add( p);
                    }
                }
            }
        }
    }


}
