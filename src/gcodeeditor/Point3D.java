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
package gcodeeditor;

/**
 *
 * @author Clément
 */
public class Point3D {
    public double x, y, z;
    
    public Point3D( double x0, double y0, double z0) {
        x = x0;
        y = y0;
        z = z0;
    }
    
    /**
     * Create a new Point3D with a String like "15.5,34,-192e3" (without space).
     * @param coordCommaSeparated 
     */
    public Point3D( String coordCommaSeparated) {
        String[] coords=coordCommaSeparated.split(",");
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }

    public double distance(Point3D p) {
        return Math.sqrt((p.x-x)*(p.x-x)+(p.y-y)*(p.y-y)+(p.z-z)*(p.z-z));
    }
    
    public double distance(double x2, double y2, double z2) {
        return Math.sqrt((x2-x)*(x2-x)+(y2-y)*(y2-y)+(z2-z)*(z2-z));
    }

    @Override
    public String toString() {
        return "("+x+" ,"+y+", "+z+")";
    }
    
    
}
