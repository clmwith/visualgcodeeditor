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
package gelements;

import gcodeeditor.Configuration;

/**
 *
 * @author Clément
 */
public class EngravingProperties {
    public static final String HEADER_STRING = "(Prop:";
    
    boolean enabled;
    /** Used in pocket GGroup */
    boolean allAtOnce;
    double feed;
    int passCount, power;
    double zStart, zEnd, passDepth;
    
    PropertieChangeListener listener;
    
    public interface PropertieChangeListener {
        public static final int ENABLE = 0;
        public static final int ALL = 1;
        public static final int FEED = 2;
        public static final int COUNT = 3;
        public static final int POWER = 4;
        public static final int START = 5;
        public static final int END = 6;
        public static final int DEPTH = 7;
        public void propertyChanged(int type);
    }

    /** Create enabled EngravingProperties without any parameter set. */
    public EngravingProperties() {
        enabled = true;
        allAtOnce = false;
        passCount = power = -1;
        feed = zStart = zEnd = passDepth = Double.NaN;
    }
    
    /** Create enabled EngravinvProperties with defaults values taken from 'c'.
     * @param defaultConfiguration */
    public EngravingProperties(Configuration defaultConfiguration) {
        this();
        feed = defaultConfiguration.feedRate;
        power = defaultConfiguration.spindleLaserPower;
        passCount = 1;
    }
    
    public void setChangeListener( PropertieChangeListener listener) {
        this.listener = listener;
    }
    
    @Override
    public EngravingProperties clone() {
        EngravingProperties clone = new EngravingProperties();
        clone.enabled = enabled;
        clone.feed = feed;
        clone.power = power;
        clone.zStart = zStart;
        clone.zEnd = zEnd;
        clone.passCount = passCount;
        clone.passDepth = passDepth;
        clone.allAtOnce = allAtOnce;
        return clone;
    }
    
    /** 
     * Create new EngravingProperties decoded from 'props'.
     * @param props An EngravingProperties.toString() result
     * @return a new EngravingProperties corresponding to props
     */
    public static EngravingProperties decode( String props) {
        EngravingProperties ep = new EngravingProperties();
        String p[] = props.substring(HEADER_STRING.length(), props.indexOf(')')).split(",");
        for( int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
            if ( p[i].length()!=0)
                switch(i) {
                    case 0: ep.enabled = p[i].equals("1"); break;
                    case 1: ep.power = Integer.valueOf(p[i]); break;
                    case 2: ep.feed = Double.valueOf(p[i]); break;
                    case 3: ep.passCount = Integer.valueOf(p[i]); break;
                    case 4: ep.zStart =  Double.valueOf(p[i]); break;
                    case 5: ep.passDepth =  Double.valueOf(p[i]); break;
                    case 6: ep.zEnd =  Double.valueOf(p[i]); break;
                    case 7: ep.allAtOnce = p[i].equals("1"); break;
                }
        }
        return ep;
    }
    
    /**
     * @param s a G-Code parenthesis comment line
     * @return true if this line is an valide encoded EngravingProperties string.
     */
    public boolean containsEngravingProperties( String s) {
        try {
            decode(s);           
        } catch ( Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Verify if at least one property is defined.
     * @param withDisable verify <i>enable</i> property too
     * @return true if at least on parameter is set or false if all are undefined.
     */
    public boolean isSet(boolean withDisable) {
        return ! ((enabled|!withDisable) && ! allAtOnce && Double.isNaN(feed) && Double.isNaN(zStart) && Double.isNaN(zEnd) && Double.isNaN(passDepth) &&
                  (power==-1) && (passCount == -1));
    }  
    public boolean isEnabled() {
        return enabled;
    }
    public boolean isAllAtOnce() {
        return allAtOnce;
    }
    /**
     * @return Double.NaN if not defined
     */
    public double getFeed() {
        return feed;
    }
    /**
     * @return -1 if not defined
     */
    public int getPassCount() {
        return passCount;
    }
    /**
     * @return -1 if not defined
     */
    public int getPower() {
        return power;
    }
    /**
     * @return Double.NaN if not defined
     */
    public double getZStart() {
        return zStart;
    }
    /**
     * @return Double.NaN if not defined
     */
    public double getZEnd() {
        return zEnd;
    }
    /**
     * @return Double.NaN if not defined
     */
    public double getPassDepth() {
        return passDepth;
    }
    
    public void setEnabled(boolean enabled) {
        if ( this.enabled != enabled) {
            this.enabled = enabled;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.ENABLE);
        }
    }
    
    public void setAllAtOnce(boolean allAtOnce) {
        if ( this.allAtOnce != allAtOnce) {
            this.allAtOnce = allAtOnce;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.ALL);
        }
    }
    public void setFeed(double feed) {
        if ( this.feed != feed) {
            this.feed = feed;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.FEED);
        }
    }
    public void setPower(int power) {
        if ( this.power != power) {
            this.power = power;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.POWER);
        }
    }
    public void setPassCount(int passCount) {
        if ( this.passCount != passCount) {
            this.passCount = passCount;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.COUNT);
        }
    }
    public void setPassDepth(double passDepth) {
        if ( this.passDepth != passDepth) {
            this.passDepth = passDepth;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.DEPTH);
        }
    }
    public void setZStart(double zStart) {
        if ( this.zStart != zStart) {
            this.zStart = zStart;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.START);
        }
    }
    public void setZEnd(double zEnd) {
        if ( this.zEnd != zEnd) {
            this.zEnd = zEnd;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.END);
        }
    }
   
    /**
     * @return a G-Code prenthesis comment representing these values.
     */
    @Override
    public String toString() {
        return HEADER_STRING +  
                (enabled              ?  1 : 0) + ", " +
                (power==-1            ? "" : power) + ", " +
                (Double.isNaN(feed)   ? "" : feed) + ", " +
                (passCount == -1      ? "" : passCount) + ", " +
                (Double.isNaN(zStart) ? "" : zStart) + ", " +
                (Double.isNaN(passDepth) ? "" : passDepth) + ", " +
                (Double.isNaN(zEnd)   ? "" : zEnd) + ", " +
                (allAtOnce            ?  1 : 0) + ")";
    }
    
    /**
     * Update herited properties according to new one
     * @param herited
     * @param newProps
     * @return Return <i>herited</i> parameter (not a clone)
     */
    public static EngravingProperties udateHeritedProps(EngravingProperties herited, EngravingProperties newProps) {
        if ( herited == null) {
            return newProps.clone();
        } else {
            herited.setEnabled( herited.isEnabled() && newProps.isEnabled());
            if ( ! Double.isNaN(newProps.getFeed())) herited.setFeed( newProps.getFeed());
            if ( newProps.getPower() != -1) herited.setPower( newProps.getPower());

            if ( ! herited.isAllAtOnce()) {
                herited.setAllAtOnce( herited.isAllAtOnce() | newProps.isAllAtOnce());
                if ( ! Double.isNaN(newProps.getZStart())) herited.setZStart( newProps.getZStart());
                if ( ! Double.isNaN(newProps.getZEnd())) herited.setZEnd( newProps.getZEnd());
                if ( ! Double.isNaN(newProps.getPassDepth())) herited.setPassDepth(  newProps.getPassDepth());
                if (newProps.getPassCount() != -1) herited.setPassCount( newProps.getPassCount()); 
            }
        }
        return herited;
    }
}
