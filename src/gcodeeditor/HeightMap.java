/*
 * Copyright (C) 2023 agathe
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
package gcodeeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The height map implementation for bed leveling implementation.
 * 
 *   Y
 *   +---+---+---+
 *   |  /|  /|  /|
 *   | / | / | / |
 *   |/  |/  |/  |
 *   +---+---+---+
 *   |  /|  /|  /|
 *   | / | / | / |
 *   |/  |/  |/  |
 *   +---+---+---+
 *   |  /|  /|  /|
 *   | / | / | / |
 *   |/  |/  |/  |
 *   +---+---+---+ X
 *  0
 * 
 * @author clement
 */
public class HeightMap {
    
    public static final String HEADER_STRING = "(HeightMap: ";
    
    String name;
    
    double delta;
    
    Rectangle2D bounds;
    double points[][]; // points[X][Y]
    
    /**
     * Create a new HeightMap grid
     * @param bounds the surface covered by this map (that will be "rounded to upper delta")
     * @param delta the delta between points in the grid
     */
    public HeightMap(Rectangle2D bounds, double delta) {
        this.delta = delta;
        this.bounds = new Rectangle2D.Double(
                            Math.round(bounds.getMinX()/delta)*delta, 
                            Math.round(bounds.getMinY()/delta)*delta, 
                            Math.round((bounds.getWidth()+delta/2)/delta)*delta,
                            Math.round((bounds.getHeight()+delta/2)/delta)*delta);
        
        points = new double[1+(int)(0.5+bounds.getWidth()/delta)][1+(int)(0.5+bounds.getHeight()/delta)];
    }
    
    
    public HeightMap clone() {
        HeightMap clone = new HeightMap(bounds, delta);
        for(int x=0; x < getGridWidth(); x++)
            for(int y=0; x < getGridHeight(); y++)
                clone.points[x][y] = points[x][y];
        return clone;
    }
     
    int getGridWidth() {
        return points.length;
    }
    
    int getGridHeight() {
        return points[0].length;
    }
    
    /**
     * Return the Triangle composed of 3 segments that contains 'p'.
     * @param p
     * @return null if p is out of the map.
     */
    public Segment2D[] getTriangleFromPoint( GCode p) {
        int px = (int)((p.getX() - bounds.getX())/delta);
        int py = (int)((p.getY() - bounds.getY())/delta);
        if ( (px < 0) || (py < 0) || (px >= points.length) || (py >= points[0].length)) return null;
    
        GCode p1 = new GCode(bounds.getX() + px * delta, bounds.getY() + py * delta);
        GCode p2 = new GCode(bounds.getX() + (px+1) * delta, bounds.getY() + (py+1) * delta);
        GCode o;
        Segment2D t[] = new Segment2D[3];
        if ( (p.getX()-p1.getX()) > (p.getY()-p1.getY())) {
            // lower triangle
            o = new GCode(bounds.getX() + (px+1) * delta, bounds.getY() + (py) * delta);         
        } else {
            // upper triangle
            o = new GCode(bounds.getX() + (px) * delta, bounds.getY() + (py+1) * delta);
        }
        t[0] = new Segment2D(o, p1);
        t[1] = new Segment2D(p1, p2);
        t[2] = new Segment2D( p2, o);
        return t;
    }
    
    /**
     * Return the height of 'p' according to the map
     * @param p the point(x,y)
     * @return 0 if p is out of the map
     */
    public double getHeight( Point2D p) {
        int px = (int)((p.getX() - bounds.getX())/delta);
        int py = (int)((p.getY() - bounds.getY())/delta);
        if ( (px < 0) || (py < 0) || (px >= points.length) || (py >= points[0].length)) return 0;
        boolean maxX = (px+1)>=points.length;
        boolean maxY = (py+1)>=points[0].length;
    
        GCode p1 = new GCode(bounds.getX() + px * delta, bounds.getY() + py * delta);

        GCode o;
        double aX, aY, b;

        if ( (p.getX()-p1.getX()) > (p.getY()-p1.getY())) {
            // lower triangle
            o = new GCode(bounds.getX() + (px+1) * delta, bounds.getY() + (py) * delta);
            b = maxX ? 0 : points[px+1][py]; 
            aX = points[px][py]-b;
            aY = (maxX || maxY) ? -b : points[px+1][py+1]-b;
        } else {
            // upper triangle or middle
            o = new GCode(bounds.getX() + (px) * delta, bounds.getY() + (py+1) * delta);
            b = ((py+1)<points[0].length) ? points[px][py+1] : 0; 
            aX = (maxX || maxY) ? -b : points[px+1][py+1]-b;
            aY = points[px][py]-b;
        }            
            
        return b+(double)(
                    (aX*Math.abs(p.getX()-o.getX())/delta)
                  + (aY*Math.abs(p.getY()-o.getY())/delta));
    }
    
    /**
     * Make a array of moves that folow the map during move from p1 to p2
     * @param g the 'G' to assign to new moves
     * @param p1
     * @param p2
     * @return a array of points that follow the map
     */
    public ArrayList<GCode> applyMapTo( int g, GCode p1, GCode p2) {
        ArrayList<GCode> res = new ArrayList<>(2);
        System.out.println("app: "+p1+"\n"+p2+"\n");
        
        double h1 = p1.getValue('Z');        
        double h2 = p2.getValue('Z');
        if ( Double.isNaN(h1) || Double.isNaN(h2)) h1 = h2 = 0;
        double dh = h2 - h1;
        
        Segment2D path = new Segment2D(p1, p2);
        double pathLen = path.getLength()-1.0;
        
        res.add(new GCode(g, new Point3D(p1.getX(), p1.getY(), getHeight(p1))));       
   
        Segment2D t[] = null, t2[];
        GCode pT;                
        for( double l = 0; l < pathLen; l+= 1.0)  {
            pT = path.getPointByDistance(l);
            t2 = getTriangleFromPoint(pT);
            // find next triangle crossing
            if ((t2 != null) && ((t == null) || ! tiranglesAreEquals(t, t2))) {
                System.out.println("Found new triangle " + t2[0] + "," + t2[1] + "," + t2[2]);
                GCode p = null;
                for ( int seg = 0; seg < 3; seg++) {
                    if ( (p = t2[seg].intersectionPoint(new Segment2D(pT, p2))) != null ) {
                        Point3D np = new Point3D(p.getX(), p.getY(), getHeight(p));
                        System.out.println("newPoint " + np);
                        if ( res.isEmpty() || ! res.get(res.size()-1).isAtSamePosition(new Point2D.Double(np.x, np.y)))
                            res.add(new GCode(g, np));
                        
                        l = p1.distance( new Point2D.Double(np.getX(), np.getY()));
                        break;
                    }
                }
                //assert( p != null);
                t = t2;
            }
        }

        res.add(new GCode(g, new Point3D(p2.getX(), p2.getY(), getHeight(p2))));
        System.out.println("\n");
        return res;
    }

    private boolean tiranglesAreEquals(Segment2D[] t1, Segment2D[] t2) {
        return t1[0].equals(t2[0]) && t1[1].equals(t2[1]) && t1[2].equals(t2[2]);
    }
    
    public void saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append("; bounds=" + bounds.getX()+","+bounds.getY()+","+bounds.getWidth()+","+bounds.getHeight()+"\n");
        fw.append("; points=");
        boolean first = true;
        for( int x = 0; x < getGridWidth(); x++)
            for( int y = 0; y < getGridHeight(); y++) {
                if ( first) first = false; else fw.append(',');            
                fw.append(""+points[x][y]);   
            }
    }
    
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String line = stream.readLine();
        if ( line.startsWith("; bounds=")) {
            String v[] = line.substring(9).split(",");
            bounds = new Rectangle2D.Double(Double.valueOf(v[0]),Double.valueOf(v[1]),Double.valueOf(v[2]),Double.valueOf(v[3]));
        } else return line;
        
        line = stream.readLine();
        
        return stream.readLine();
    }       
       
    
    /**
     * set height value to the closest point of the grid.
     * @param heightPoint 
     */
    public void setHeight( Point3D heightPoint) {
        int px = (int)(0.5 + ((heightPoint.getX() - bounds.getX())/delta));
        int py = (int)(0.5 + ((heightPoint.getY() - bounds.getY())/delta));
        if ( (px < 0) || (py < 0) || (px >= points.length) || (py >= points[0].length)) return;
        points[px][py] = (double)heightPoint.getZ();
    }
    
    /**
     * Set an height at grid[x,y]
     * @param x
     * @param y
     * @param h 
     */
    public void setHeight(int x, int y, double h) {
        points[x][y] = h;
    }
    
    
    /**
     * Test Class used only to visualy test HeightMap class.
     */
    private static class JHeightMapTester extends JPanel {

        HeightMap hm;
        int mouseX, mouseY;
        double scalingFactor = 2;
        ArrayList<GCode> path;
        private boolean setP1 = true;
        private GCode p1, p2;
        
        /**
        * Test the grid :
        * 
        *      3    4
        * -5  10   -3
        *  0   5
        * 
        */
        public JHeightMapTester() {
            hm = new HeightMap( new Rectangle2D.Double(-156, -156, 320, 370), 100);                     
            
            setPreferredSize(new Dimension( (int)(scalingFactor*(hm.bounds.getWidth()+hm.delta)+20), 
                                            (int)(scalingFactor*(hm.bounds.getHeight()+hm.delta)+20)));
            setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor( 
                    new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB), 
                    new java.awt.Point(0, 0), "blankCursor"));
            
            addMouseMotionListener( new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent me) { 
                    int oy = (int)(hm.getGridHeight()*hm.delta);
                    int mX = (int)(me.getX() / scalingFactor);
                    int mY = (int)(me.getY() / scalingFactor); 
                    if ( setP1) p1 = new GCode(mX, oy - mY);
                    else p2 = new GCode(mX, oy - mY);
                    path = hm.applyMapTo(0, p1, p2);
                    repaint();
                }
                @Override
                public void mouseMoved(MouseEvent me) {
                    mouseX = (int)(hm.bounds.getX()+me.getX()+hm.bounds.getX()/ scalingFactor);
                    mouseY = (int)(me.getY() / scalingFactor); 
                    repaint();
                }                
            });
            
            addMouseListener( new MouseListener() {               
                @Override
                public void mouseClicked(MouseEvent me) {
                    int oy = (int)(hm.getGridHeight()*hm.delta);
                    if ( setP1) p1 = new GCode(mouseX, oy - mouseY);
                    else p2 = new GCode(mouseX, oy - mouseY);
                    setP1 = ! setP1;
                    path = hm.applyMapTo(0, p1, p2);
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent me) { }
                @Override
                public void mouseReleased(MouseEvent me) { }
                @Override
                public void mouseEntered(MouseEvent me) { }
                @Override
                public void mouseExited(MouseEvent me) { }
            });
            
            hm.setHeight(1, 2, 3);
            hm.setHeight(2, 2, 4);
            hm.setHeight(0, 1, -5);
            hm.setHeight(1, 1, 10);
            hm.setHeight(2, 1, -3);
            hm.setHeight(1, 0, 5);        
            System.out.println( hm.getHeight( new Point2D.Double(5, 5)));
            System.out.println( hm.getHeight( new Point2D.Double(6, 5)));
            System.out.println( hm.getHeight( new Point2D.Double(5, 6)));
            System.out.println();
            System.out.println( hm.getHeight( new Point2D.Double(12, 12)));
            System.out.println( hm.getHeight( new Point2D.Double(12, 15)));
            System.out.println( hm.getHeight( new Point2D.Double(15, 12)));   
            
            path = hm.applyMapTo(0, p1 = new GCode(140,-100), p2 = new GCode(290, 110));
        }
        
        public static final int MARGE = 10;
        @Override
        protected void paintComponent(Graphics g) {   
            ((Graphics2D)g).scale(scalingFactor, scalingFactor);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());            
            g.translate(MARGE-(int)hm.bounds.getX(), MARGE);
            int oy = (int)(hm.getGridHeight()*hm.delta)+ (int)hm.bounds.getY();
            
            g.setColor(Color.yellow);
            for( int x = 0; x < hm.points.length; x++) {
                int dx = (int)(hm.bounds.getX()+x*hm.delta);
                if ( Math.abs(dx) < 10e-10) g.setColor(Color.white);
                g.drawLine(dx, MARGE, dx, (int)(hm.getGridHeight()*hm.delta));
                
                for( int y = 0; y < hm.points[0].length; y++) {
                    int dy = (int)(hm.bounds.getMinY()+y*hm.delta);
                    if ( Math.abs(dy) < 10e-10) g.setColor(Color.white);
                    g.drawLine((int)hm.bounds.getX(), oy - dy, (int)(hm.getGridWidth()*hm.delta), oy - dy);
                    
                    g.setColor(Color.lightGray);
                    g.drawLine(dx, oy - dy, (int)(dx+hm.delta), oy - dy - (int)hm.delta);
                    g.setColor(Color.yellow);
                    g.drawString("("+x+","+y+")="+hm.getHeight(new Point(dx, dy)), dx+3, oy - dy-3);
                }
            }  
            
            g.setColor(Color.red);
            g.drawLine ((int)path.get(0).getX(), oy - (int)path.get(0).getY(),
                    (int)path.get(path.size()-1).getX(), oy - (int)path.get(path.size()-1).getY());
            
            g.setColor(Color.white);
            g.drawLine(mouseX-3, mouseY-3, mouseX+3, mouseY+3);
            g.drawLine(mouseX-3, mouseY+3, mouseX+3, mouseY-3);
            int mx, my;
            mx = mouseX;
            my = oy - mouseY;
            //System.out.println(new Point(mx, my));
            g.drawString(""+Math.floor(hm.getHeight(new Point(mx, my))*1000)/1000f, mouseX+MARGE, mouseY+MARGE);
            
            g.setColor(Color.white);
            if ( path != null) {
                int x=0, y=0;
                for( GCode p : path) {
                    x = (int)p.getX();
                    y = oy - (int)p.getY();
                    g.drawLine(x-3, y-3, x+3, y+3);
                    g.drawLine(x-3, y+3, x+3, y-3);
                }
                
                g.drawString("1", 5 + (int)path.get(0).getX(), oy - (int)path.get(0).getY()+ 10);
                g.drawString("2", 5 + x, y +10);
            }     
        }
        
    }
    
    public static void main( String args[]) {                                   
        JHeightMapTester hmp = new JHeightMapTester();
        JFrame f = new JFrame();
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
        f.setContentPane(hmp);
        f.pack();
        f.setVisible(true);
    }
    
}
