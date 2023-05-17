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

import gcodeeditor.GCode;
import gelements.GElement;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 * Used to choose how to paint GElements
 * @author Clément Gérardin @ Marseille.fr
 */
public class PaintContext {
    
    public static final Color SEL_COLOR1 = Color.GREEN;
    public static final Color SEL_DISABLED_COLOR = new Color(0,125,0);
    public static final Color EDIT_COLOR = new Color(0,150,255);
    public static final Color DISABLE_COLOR = Color.DARK_GRAY;
    
    public Graphics2D g;
    public double zoomFactor, toolDiameter;
    public boolean showStartPoints, paintReperes;
    public GCode highlitedPoint;
    public ArrayList<GCode> selectedPoints;
    public Color color;
    public GCode lastPoint;
    public GElement editedElement;

    public PaintContext() {
        paintReperes=false;
        color = Color.BLACK;
        zoomFactor = 1;
    }
        
    public PaintContext(Graphics g, double zoomFactor, boolean showStartPoints, GCode highlitedPoint, ArrayList<GCode> selectedPoints, double toolDiameter) {
        paintReperes=true;
        this.g = (Graphics2D)g;
        this.zoomFactor = zoomFactor;
        this.showStartPoints = showStartPoints;
        this.highlitedPoint = highlitedPoint;
        this.selectedPoints = selectedPoints;
        this.toolDiameter = toolDiameter;
    }

    @Override
    public PaintContext clone() {
        return new PaintContext(g, zoomFactor, showStartPoints, highlitedPoint, selectedPoints, toolDiameter);
    }
    
    
}
