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

import gelements.GGroup;
import gelements.G1Path;
import gelements.PaintContext;
import java.awt.Font;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public class SystemFont extends GFont {

    Font font;

    public SystemFont(String fontName, boolean bold, boolean italic, int size) {
        this(fontName+((bold|italic)?"-":"")+(bold?"BOLD":"")+(italic?"ITALIC":"")+"-"+size); 
    }

    SystemFont(String fontName) {
        font = Font.decode(fontName);
        name = "System;"+fontName;
    }

    @Override
    public GGroup getTextPaths(String text) {
        return G1Path.makeText(font, text);
    }

    @Override
    public void painText(PaintContext pc, int x, int y, String text) {
        getTextPaths(text).paint(pc);
    }
    
    @Override
    public boolean isBold() {
        return font.isBold();
    }

    @Override
    public boolean isItalic() {
        return font.isItalic();
    }
}
