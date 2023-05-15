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

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Clément
 */
public class BackgroundPictureParameters {
    
    public static final String BACK_PICTURE_HEADER = "(BackPicture: ";
        
    private double x, y;
    private float rotation, alpha;
    private String fileName;
    private boolean visible;
    private final ArrayList<ParameterChangedListenerInterface> listeners;
    
    BufferedImage img;
    double width, height;
    
    public BackgroundPictureParameters() {
        listeners = new ArrayList<>();
        alpha=0.5f;
    }
    
    public void addionChangeListener( ParameterChangedListenerInterface me) {
        listeners.add(me);
    }
    
    public void removeOnChangeListener( ParameterChangedListenerInterface me) {
        listeners.remove(me);
    }
      
    /**
     * Build the string to save this picture in a file (use decode to retrieve ti).
     * @return 
     */
    public String toString() {
        return BACK_PICTURE_HEADER + "["+x+","+y+","+width+","+height+","+rotation+","+alpha+","+visible+"]"+(fileName!=null?fileName:"")+")";
    }
    
    /**
     * Try to load the background picture linked to the document.
     * @param line a string in the format of BackgroundPictureParameters.toString()
     * @return null if line is not in right format
     */
    public static BackgroundPictureParameters decode( String line) {
        BackgroundPictureParameters p = new BackgroundPictureParameters();
        try {
            String params[] = line.substring(line.indexOf('[')+1, line.indexOf(']')).split(",");
            String filename = line.substring(line.indexOf(']')+1, line.length()-1);
            p.setAll(filename, 
                        Double.parseDouble(params[0]),
                        Double.parseDouble(params[1]),
                        Double.parseDouble(params[2]),
                        Double.parseDouble(params[3]),
                        Float.parseFloat(params[4]),
                        Float.parseFloat(params[5]),
                        params[6].equals("true"), false);
            return p;
        } catch ( Exception e) { 
            return null;
        }
    }
    
    /**
     * Call reloadImage() after that.
     * @param param 
     */
    public void setAll( BackgroundPictureParameters param) throws IOException {
        x = param.x;
        y = param.y;
        width = param.width;
        height = param.height;
        visible = param.visible;
        alpha = param.alpha;
        fileName = param.fileName;
        rotation = param.rotation;       
        if ((param.img == null) && (fileName != null) && ! fileName.isEmpty()) reloadImage();
        else img = param.img;
        informChanged();
    }
    
    public void reloadImage() throws IOException {
        boolean oldVisible = visible;
        visible = false;
        img = null;
        img = ImageIO.read(new File(fileName));
        //width = height = 0;
        visible = oldVisible;
    }
    
    public boolean isLoaded() {
        return img != null;
    }
    
    public void setAll( String filePath, double x, double y, double width, double height, float rotation, float alpha, boolean visible, boolean loadImage) throws IOException {
        boolean changed = false;
        
        if ( (img==null) || (fileName==null) || ! fileName.equals(filePath)) {
            
            if ( loadImage && (filePath != null)) {
                img = ImageIO.read(new File(filePath));
                if ( (width == 0) && (height == 0)) {
                    this.width = img.getWidth();
                    this.height = img.getHeight();
                }
            } else {
                img = null;
            }
            fileName = filePath;
            changed = true;
        }
        if ( this.x != x) {
            this.x = x;
            changed = true;
        }
        if ( this.y != y) {
            this.y = y;
            changed = true;
        }
        if ( width<0) width = 0;
        if ( this.width != width) {
            this.width = width;
            changed = true;
        }
        if ( height<0) height = 0;
        if ( this.height != height) {
            this.height = height;
            changed = true;
        }
        rotation %= 360;
        if ( this.rotation != rotation) {
            this.rotation = rotation;
            changed = true;
        }
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        if ( this.alpha != alpha) {
            this.alpha = alpha;
            changed = true;
        }        
        if ( this.visible != visible) {
            this.visible = visible;
            changed = true;
        }
        if ( changed) informChanged();
    }
    
    public boolean isImageVisible() {
        return visible && (getViewRegion() != null);    
    }
    
    public void setVisible( boolean visible) {
        if ( this.visible != visible) {
            this.visible = visible;
            informChanged();
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName( String filename) {
        fileName = filename;
    }
    
    public double getViewX() {
        return x;
    }
    
    public double getViewY() {
        return y;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    private void informChanged() {
        listeners.forEach((l) -> {
            l.backgroundPictureParameterChanged();
        });
    }
    
    @Override
    public BackgroundPictureParameters clone() {
        BackgroundPictureParameters clone = new BackgroundPictureParameters();
        try {
            clone.setAll(fileName, x, y, width, height, rotation, alpha, visible, false);
            clone.img = img;
        } catch (IOException ex) {
            Logger.getLogger(BackgroundPictureParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        return clone;
    }

    public Rectangle2D getViewRegion() {
        if ( img == null) return null;
        return new java.awt.geom.Rectangle2D.Double(x, y,
                        (width!=0)?width:img.getWidth(),
                        (height!=0)?height:img.getHeight());
    }
    
    public int getImageWidth() {
        return (img == null) ? 0 : img.getWidth();
    }
    
    public int getImageHeight() {
        return (img == null) ? 0 : img.getHeight();
    }
    
    public interface ParameterChangedListenerInterface {
        /** Called when a parameter has change. */
        public void backgroundPictureParameterChanged();
    }
}
