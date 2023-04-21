/*
 * Copyright (C) 2023 clement
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
import java.awt.geom.Point2D;
import org.kabeja.dxf.helpers.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author clement
 */
public class G1Rectangle extends G1Path {
    

    double angle = 0;
    
    public G1Rectangle(String name, double w, double h) {
        super(name);
        add( new Point(0, 0, 0));
        add( new Point(w, 0, 0));
        add( new Point(w, h, 0));
        add( new Point(0, h, 0));
        add( new Point(0, 0, 0));
    }

    /**
     * Insert line until end of initialisation (5 points)
     * @param i
     * @param line 
     */
    @Override
    protected void insertLine(int i, GCode line) {
        if ( lines.size() < 5) super.insertLine(i, line);
    }

    @Override
    public void rotate(Point2D center, double angle) {
        
    }
    
    

    @Override
    public GElement clone() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public GCode remove(int i) { return null; }

    @Override
    public void removeAll(ArrayList<GCode> lines) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }

    @Override
    public String getSummary() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
