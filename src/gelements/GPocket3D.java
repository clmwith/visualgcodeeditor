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

/**
 *
 * @author Clément GERARDIN @ Marseille.fr
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
}
