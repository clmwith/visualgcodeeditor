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
package gcodeeditor;

import gelements.GElement;
import gelements.GGroup;
import gelements.G1Path;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;


/**
 * @author Clément GERARDIN @ Marseille.fr
 */
public class HersheyGlyph {
    
    public char letter;
    int id, nverts, leftpos, rightpos;
    ArrayList<ArrayList<Point>>paths;
    GGroup ggroup;

    public HersheyGlyph( char letter, String jhfLine ) {
        id = getInt(jhfLine.substring(0, 5));
        nverts = getInt(jhfLine.substring(5, 8));
        leftpos = jhfLine.charAt(8) - 'R';
        rightpos = jhfLine.charAt(9) - 'R';
        paths = new ArrayList<>();
        jhfLine = jhfLine.substring(10);
        ArrayList<Point> curPath = null;
        for( int c = 0; (c+1) < jhfLine.length(); c+=2) {
            if ( (curPath==null) || (jhfLine.substring(c, c+2).equals(" R"))) {
                paths.add( curPath = new ArrayList<>());
                if ( jhfLine.charAt(c) == ' ') continue;
            }
            curPath.add( new Point(jhfLine.charAt(c)-'R', jhfLine.charAt(c+1)-'R'));            
        }
    }  
    
    public GElement toGGroup(String name) {
        if ( ggroup == null) {
            ggroup = new GGroup(name);
            int n = 0;
            for( ArrayList<Point> p : paths) {
                n++;
                G1Path path = new G1Path(name+((n==0)?"":n));
                for( Point pt : p)
                    path.add( new GCode(pt.x - leftpos, (16-7) - pt.y));

                ggroup.add(path);
            }
        }
        if ( ggroup.size()==1) {
            final GElement e = ggroup.get(0).clone();
            e.setName(name);
            return e;
        } else 
             return ggroup.clone();
    }
    
    private static int getInt(String substring) {
        int d = 0;
        while( !Character.isDigit(substring.charAt(d))) d++;
        return Integer.valueOf(substring.substring(d));
    }
    
    public int getWidth() {
        return rightpos - leftpos;
    }
    
    public String toHershey() {
        String res = String.format("%5d %2d%c%c", id, nverts, leftpos + 'R', rightpos + 'R');
        boolean first = true;
        for (  ArrayList<Point> p : paths) {
            if ( ! first ) res += " R"; else first = false;
            
            for( Point v : p) {
                res += ""+(char)(v.x+'R')+(char)(v.y + 'R');
            } 
        }       
        return res;
    }

    void paintGlyph(Graphics g, double zoomFactor, int x, int y) {
        x -= leftpos;
        for( ArrayList<Point> p : paths) {
            Point last = null;
            for ( Point pt : p ) {
                if ( last != null) 
                    g.drawLine((int)(zoomFactor * (x + last.x)), 
                                (int)(zoomFactor * (y + last.y)), 
                                (int)(zoomFactor * (x + pt.x)),
                                (int)(zoomFactor * (y+ pt.y)));
            
                last = pt;
            }
        }
    }
}

