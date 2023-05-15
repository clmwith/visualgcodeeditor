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
import java.util.ArrayList;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public abstract class GPocket3D extends G1Path {

    public GPocket3D(String name) {
        super(name);
    }
    
    abstract public double getInlayDepth();
    
    /**
     * Return the bounds of the poket at this depth.
     * @param depth the absolute positive depth of the pass (0 is the surface)
     * @return the pass path or null if none
     */
    abstract public G1Path getPassBoundsPath(double depth);
    
    //
    // Disable some G1Path functions below
    //
    @Override
    public boolean concat(GElement get, double d) {
        // return false;
        throw new UnsupportedOperationException("Can't concat GPocket3D");
    }
        
    @Override
    protected void insertLine(int i, GCode line) { }

    @Override
    public GCode insertPoint(GCode newPoint) { return null; } 
    
    @Override
    public boolean joinPoints(ArrayList<GCode> selectedPoints) { return false; }
   
    @Override
    public GCode remove(int i) { return null; }
    
    @Override
    public void remove(GCode line) { }    
    
    @Override
    public void removeAll(ArrayList<GCode> lines) { }

    @Override
    public void removeByDistance(ArrayList<GCode> points, double distance) { }
    
    @Override
    public void simplify(double angleMin, double distanceMax) { }
}
