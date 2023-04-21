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


import gelements.GArc;
import gelements.GElement;
import gelements.GGroup;
import gelements.G1Path;
import gelements.PaintContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that handle LibreCad's famous unknown author's FONTS.
 * @author Clément Gérardin @ Marseille.fr
 */
public class LibreCadFont extends GFont {
    
    public static String LIBRECAD_FONT_DIR = "/usr/share/librecad/fonts";
    public static final ArrayList<String>LIBRECAD_FONTS = new ArrayList<>();
    public static boolean fontNameLoaded = false;
    static final HashMap<Integer,LibreCadFont> FONTS = new HashMap<>();
    
    public class Glyph {
        public double width;
        public GElement paths;
        public Glyph( double w, GElement p) {
            width = w;
            paths = p;
        }
    }
    
    HashMap<Integer,Glyph> glyphs;
    Pattern ASCII_PAT = Pattern.compile("\\[#?([^\\]]+)\\]\\s*([^\\s])*.*");
    Pattern LETTER_S_PAT = Pattern.compile("#\\s*LetterSpacing\\s*:\\s*([\\d\\.0-9])+.*");
    Pattern WORD_S_PAT = Pattern.compile("#\\s*WordSpacing\\s*:\\s*([\\d\\.0-9])+.*");
    Pattern NAME_PAT = Pattern.compile("#\\s*Name\\s*:\\s*(.*)");
    private double letterSpacing = 3, wordSpacing = 7;
    private double ascent, descent;
    
    /**
     * Load a LibreCadFont
     * @param name the name (without .lff extention) of the font in <i>LIBRECAD_FONT_DIR</i>.
     * @return the new font or null if not found
     * @throws java.io.IOException
     */
    public static LibreCadFont getFont( String name) throws IOException {
        loadAvailableFonts();
        int i=0;
        for( String s : LIBRECAD_FONTS)
            if ( s.equals(name))
                return getFont( i);
            else
                i++;
        return null;
    }
    
    public static LibreCadFont getFont( int fontNumber) throws IOException {    
        loadAvailableFonts();
        if ( fontNumber > LIBRECAD_FONTS.size()) return null;
        if ( FONTS.get(fontNumber) == null)
            FONTS.put( fontNumber, new LibreCadFont(LIBRECAD_FONTS.get(fontNumber), 
                                        new FileInputStream(new File(LIBRECAD_FONT_DIR+"/"+LIBRECAD_FONTS.get(fontNumber)+".lff"))));
        
        return FONTS.get(fontNumber);
    }
    
    /**
     * Create LibreCad (.lff) font from a stream.
     * @param  name the name without "LCAD;" nor extension ".lff"
     * @param fromStream the stream to load the font from.
     * @throws IOException 
     */
    LibreCadFont( String name, InputStream fromStream) throws IOException {
        Matcher m;
        G1Path p;
        GGroup g;
        ArrayList<String> includes = new ArrayList<>();
        this.name = "LCAD;" + name;
        
        glyphs = new HashMap<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(fromStream));
        while ( in.ready()) {
            String l = in.readLine();
            if ( (m=NAME_PAT.matcher(l)).matches()) {
                name = m.group(1);
                while ( ! name.isEmpty() && Character.isWhitespace(name.charAt(0))) name = name.substring(1);
            } else if ( (m=LETTER_S_PAT.matcher(l)).matches()) {
                letterSpacing = Double.parseDouble(m.group(1));
            } else if ( (m=WORD_S_PAT.matcher(l)).matches()) {
                wordSpacing = Double.parseDouble(m.group(1));
            } else if ( (m=ASCII_PAT.matcher(l)).matches()) {
                int n, code = hex2decimal(m.group(1));
                g = new GGroup("" +(char)code);
                
                n=0;
                double width = 0;
                while( ((l=in.readLine()) != null) && (l.length() > 2)) {
                    p = new G1Path("" + (char)code + "" + n++);

                    if ( l.charAt(0) == 'C') {
                        //int ptr = hex2decimal(l.substring(1));
                        includes.add( "" + (char)code + l); 
                        l=in.readLine();
                        if (l.isEmpty()) break;
                    }
                    GCode currpoint, lastpoint = null;
                    final String pts[] = l.split(";");
                    for (String pt : pts) {
                        String[] xy = pt.split(",");
                        if ( xy.length < 2) continue;
                        try {
                            int id;
                            if ( (id=xy[1].indexOf('A')) != -1) {
                                final String[] ts = new String[3];
                                ts[0] = xy[0];
                                ts[1] = xy[1].substring(0, id);
                                ts[2] = xy[1].substring(id);
                                xy = ts;
                            }
                            final double x = Double.parseDouble( xy[0]);
                            final double y = Double.parseDouble( xy[1]);
                            if ( x > width) width = x;
                            if ( y > ascent) ascent = y;
                            if ( y < descent) descent = y;
                            currpoint=new GCode( x, y);
                            if ( xy.length==3) { // X,Y<A
                                if ( p.size() > 0 ) g.add(p); // close current path
                                else
                                    System.out.println("LibreCardFont(): prepend !");
                                double a = Double.parseDouble(xy[2].substring(1));
                                g.add( GArc.makeBulge(lastpoint, currpoint , a));
                                p = new G1Path("" + (char)code + "" + n++);
                            }
                            p.add( lastpoint = currpoint);
                        } catch ( NumberFormatException e) {
                            System.out.println("LibreCardFont(): wrong number for ("+code+")[" + l + "]");
                        } 
                    }
                    if ( ! p.isEmpty()) g.add(p);          
                }
                
                GElement glyph;
                if ( g.size()==1) {
                    glyph = g.get(0);
                    glyph.setName(g.getName());
                } else {
                    glyph = g;
                    g.joinElements(0.0002);
                }
                
                Glyph gl = new Glyph(width, glyph);
                if ( gl.paths.name.charAt(0) == 'e') wordSpacing = gl.width;
                glyphs.put(code, gl);     
            }     
        }
        in.close();
        
        includes.forEach((inc) -> {
            ArrayList<GElement> els = new ArrayList<>();
            GElement link = glyphs.get( hex2decimal(inc.substring(2))).paths;
            Glyph glyph = glyphs.get( (int)inc.charAt(0));
            els.add(link);
            els.add(glyph.paths);         
            glyphs.put( (int)inc.charAt(0), new Glyph( glyph.width, new GGroup(""+ inc.charAt(0), els)));
        });
    }
    
    static void loadAvailableFonts()
    {
        if ( ! fontNameLoaded) {
            File dir = new File(LIBRECAD_FONT_DIR);
            if ( dir.isDirectory()) {

                for ( File f : dir.listFiles())
                    if ( f.getName().endsWith(".lff"))
                        LIBRECAD_FONTS.add( f.getName().split("\\.")[0]);
            }
            fontNameLoaded = true;
            LIBRECAD_FONTS.sort((String o1, String o2) -> o1.compareToIgnoreCase(o2));
        }
    }
    
    public LibreCadFont(File fromFile) throws FileNotFoundException, IOException {
        this( "file|"+fromFile.getAbsolutePath(), new FileInputStream( fromFile));
    }
    
    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
    
    @Override
    public GGroup getTextPaths( String text) {
        GGroup res = new GGroup(text);
        int x = 0;
        for( int i = 0; i < text.length(); i++) {
            int ch = text.charAt(i);
            Glyph g = glyphs.get(ch);
            if (g != null) {
                GElement pth = g.paths.clone();
                if (! pth.isEmpty()) {
                    pth.translate(x, 0);
                    res.add( pth);
                }
                x += g.width + letterSpacing;
                
            }
            if ( ch == ' ') 
                    x += wordSpacing;
        }
        return res;
    }
    
    @Override
    public void painText( PaintContext pc, int x, int y, String text) {
        GElement e = getTextPaths(text);
        e.translate(new GCode(x, -y));
        e.paint(pc);
    }
}
