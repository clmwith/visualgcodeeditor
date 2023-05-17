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
package gcodeeditor.gui;

import java.awt.geom.Point2D;

/**
 *
 * @author Clément
 */
public interface JProjectEditorPanelListenerInterface {
    public void updateGUIAndStatus();
    
    public void updateMouseCoord( int x, int y, double rx, double ry);

    public void updatePropertiesPanel();
    
    public void moveGantry( Point2D newPosition);

    public void setVirtualMachinePosition(Point2D coordSnapPointFor);

    public void inform(String msg);
}
