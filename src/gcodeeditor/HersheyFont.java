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

import gelements.GElement;
import gelements.GGroup;
import gelements.PaintContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public class HersheyFont extends GFont {
    
    public static final String[] FONTS = {
        "astrology", "cursive", "cyrilc_1", "cyrillic", "futural", "futuram",
        "gothgbt", "gothgrt", "gothiceng", "gothicger", "gothicita", "gothitt",
        "greekc", "greek", "greeks", "japanese", "markers", "mathlow", 
        "mathupp", "meteorology", "music", "rowmand", "rowmans", "rowmant",
        "scriptc", "scripts", "symbolic", "timesg", "timesib", "timesi",
        "timesrb", "timesr" };
    
    static final HersheyFont HFONTS[] = new HersheyFont[FONTS.length]; 
    
    /**
     * Load a JAR included Hershey font.
     * @param aClass a Class to use getRessource()
     * @param index the number of the font in FONTS array (use getFontIndex()
     * @return the new font
     * @throws IOException 
     */
    public static HersheyFont getFont( Class<?> aClass, int index) throws IOException {
        if ( HFONTS[index] == null) HFONTS[index] =  new HersheyFont(aClass, index);
        return HFONTS[index];
    }
    
    /**
     * @param hersheyFontName
     * @return the index in the <i>FONTS</i> array of this <i>hersheyFontName</i> or -1 if not found.
     */
    public static int getFontIndex(String hersheyFontName) {
        for( int i = 0; i < FONTS.length; i++) 
            if ( FONTS[i].equals(hersheyFontName)) return i;
        return -1;
    }
   
    ArrayList<HersheyGlyph> glyphs;
    
    public HersheyFont( Class<?> me, int fontNumber) throws IOException {
        this(me.getResourceAsStream("/hershey-fonts/" + FONTS[fontNumber] + ".jhf"));
        name = "Hershey;" + FONTS[fontNumber];
    }
    
    HersheyFont( InputStream fromStream) throws IOException {
        glyphs = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(fromStream));
        char letter = ' ';
        while ( in.ready()) {
            String l = in.readLine();
            glyphs.add(new HersheyGlyph(letter++, l));
        }
        in.close();
    }
    
    public HersheyFont(File fromFile) throws FileNotFoundException, IOException {
        this( new FileInputStream(fromFile));
    }
    
    @Override
    public GGroup getTextPaths( String text) {
        GGroup res = new GGroup(text);
        int x = 0;
        for( int i = 0; i < text.length(); i++) {
            int ch = text.charAt(i) - 32;
            if ( (ch >=0) && (glyphs.size() > ch)) {
                HersheyGlyph g = glyphs.get(ch);
                GElement gg = g.toGGroup(""+(char)(' '+ch));
                if ( ! gg.isEmpty()) {
                    gg.translate(x, 0);
                    res.add( gg);
                }
                x += g.getWidth();
            }
        }
        return res;
    }
   
    @Override
    public void painText( PaintContext pc, int x, int y, String text) {
        for( int i = 0; i < text.length(); i++) {
            int ch = text.charAt(i) - 32;
            if ( (ch >=0) && (glyphs.size() > ch)) {
                HersheyGlyph hg = glyphs.get(ch); 
                hg.paintGlyph(pc.g, pc.zoomFactor, x, y);
                x += hg.getWidth();
            }
        }
    }
}
