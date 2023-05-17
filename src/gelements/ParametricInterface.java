/*
 * Copyright (C) 2023 clement
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
 * Add parametric properties to the Element (not actualy used)
 * @author clement
 */
public interface ParametricInterface {
    public static final String ID_START_POINT = "StartAt";
    public static final String ID_WIDTH = "Width";
    public static final String ID_HEIGHT = "Height";
    
    /** Used to know if the Element contains modifiable font */
    public static final String ID_FONT = "Font";
        
    /**
     * Get all parameters keys
     * @return The name of all parameters
     */
    public String[] getParams();
    
    /**
     * Get the value of a parameter
     * @param name
     * @return 
     */
    public Object getParam( String key);
    
    /**
     * Set the value of a parameter
     * @param name
     * @param value
     * @return true if the value is accepted (valid)
     */
    public boolean setParam( String key, String value);
}
