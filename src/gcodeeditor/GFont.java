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
import gelements.PaintContext;
import java.io.IOException;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public abstract class GFont {
    String name;
    
    public String getName() { 
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    abstract public GGroup getTextPaths( String text);
    abstract public void painText( PaintContext pc, int x, int y, String text);
    
    /** 
     * Return the fort descripted by <i>name</i>.
     * @param encodedFontName
     * @return the new font or <i>null</i> if the font is not found.
     * @throws java.io.IOException
     */
    public static GFont decode(String encodedFontName) throws IOException {
        String f[] = encodedFontName.split(";");
        if ( f[0].equals("Hershey")) return HersheyFont.getFont(GFont.class, HersheyFont.getFontIndex(f[1]));
        if ( f[0].equals("LCAD")) return LibreCadFont.getFont(GFont.class, f[1]);
        if ( f[0].equals("System")) return new SystemFont(f[1]);
        return null;
    }

    public boolean isBold() {
        return false;
    }

    public boolean isItalic() {
        return false;
    }
}
