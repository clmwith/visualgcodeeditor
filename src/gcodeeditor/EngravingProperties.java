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

import gcodeeditor.Configuration;
import java.util.Iterator;

/**
 * Contains GCODE engraving properties
 * @author Clément
 */
public class EngravingProperties {
    public static final String HEADER_STRING = "(Prop:";
    
    boolean enabled;
    /** Used in pocket GGroup */
    boolean allAtOnce;
    double feed;
    
    int passCount, power;
    double zStart, zEnd;
    
    /** always positive */
    double passDepth;
    
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
     * @param defaultConfiguration used as default (for feed, power and passCount)
     */
    public EngravingProperties(Configuration defaultConfiguration) {
        this();
        feed = defaultConfiguration.feedRate;
        power = defaultConfiguration.spindleLaserPower;
        passCount = 1;
    }
    
    public void addChangeListener( PropertieChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Make a clone of this properties <b>without cloning listeners.</b>
     * @return a clone of this properties.
     */
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
                    case 1: ep.power = Integer.parseInt(p[i]); break;
                    case 2: ep.feed = Double.parseDouble(p[i]); break;
                    case 3: ep.passCount = Integer.parseInt(p[i]); break;
                    case 4: ep.zStart =  Double.parseDouble(p[i]); break;
                    case 5: ep.passDepth =  Double.parseDouble(p[i]); break;
                    case 6: ep.zEnd =  Double.parseDouble(p[i]); break;
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
     * the laser power or spindle speed
     * @return -1 if not defined
     */
    public int getPower() {
        return power;
    }
    
    /**
     * the start Z level
     * @return Double.NaN if not defined
     */
    public double getZStart() {
        return zStart;
    }
    
    /**
     * the last Z level
     * @return Double.NaN if not defined
     */
    public double getZEnd() {
        return zEnd;
    }
    
    /**
     * the number of pass
     * @return Double.NaN if not defined
     */
    public double getPassDepth() {
        return passDepth;
    }
    
    /**
     * Return the pass Z height iterator, the priority is passDepth. 
     */
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            double zS, pD, pC, zE, cZ;
            /** true if the number of pass is fixed (without any Z move) */
            boolean fixed;
            
            public Iterator<Double> init() {
                zS = cZ = zStart;
                zE = zEnd;
                pD = passDepth;
                pC = getPassCount();
                fixed = Double.isNaN(zStart + zEnd + passDepth) || (passDepth < 0.0001);
                return this;
            }
            
            @Override
            public boolean hasNext() {
                return fixed ? pC > 0 : cZ > zE;
            }

            /**
             * Return the next passZ height, or NaN if no Z value to change.
             */
            @Override
            public Double next() {
                if ( fixed) {
                    assert (pC > 0);
                    pC--;
                    return cZ;
                } else {
                    assert( cZ > zE);
                    
                    if ( cZ < zE) cZ = zE;
                    double res = cZ;
                    cZ -= pD;
                    return res;
                }
            }
        }.init();
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
        if ((Double.isNaN(this.feed) ^ Double.isNaN(feed)) || (Math.abs(this.feed - feed) > 10e-6 )) {
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
    
    /**
     * Set the passCount, and update passDepth if zStart ans zEnd values existes.
     * @param passCount 
     */
    public void setPassCount(int passCount) {
        if ( this.passCount != passCount) {
            this.passCount = passCount;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.COUNT);         
        }
        validatePass( true);
    }
    
    /**
     * verify/update passCount too.
     * @param passDepth is always positive or will be converted.
     */
    public void setPassDepth(double passDepth) {   
        if ( (Double.isNaN(this.passDepth) ^ Double.isNaN(passDepth)) || (Math.abs(this.passDepth - passDepth) > 10e-6 )) {
            if (passDepth < 0.00001) passDepth = 0; // security round
            this.passDepth = passDepth;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.DEPTH);
            if ( ! Double.isNaN(this.passDepth)) validatePass(false);
        }
    }
    
    public void setZStart(double zStart) {
        if ( ! Double.isNaN(zEnd) && (zStart < zEnd)) zStart = zEnd;
        
        if ( (Double.isNaN(this.zStart) ^ Double.isNaN( zStart)) || (Math.abs(this.zStart - zStart) > 10e-6 )) {     
            this.zStart = zStart;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.START);
            validatePass(false);
        }
    }
    
    public void setZEnd(double zEnd) {
        if ( ! Double.isNaN(zStart) && (zStart < zEnd)) zEnd = zStart;
        
        if ( (Double.isNaN(this.zEnd) ^ Double.isNaN( zEnd)) || (Math.abs(this.zEnd - zEnd) > 10e-6 )) {
            this.zEnd = zEnd;
            if ( listener != null) listener.propertyChanged(PropertieChangeListener.END);
            validatePass(false);
        }
    }
    
    /**
     * Update correctly passDepth or passCount according to zStart en zEnd
     * @param priority2TheCount 
     */
    private void validatePass(boolean priority2TheCount) {
        if ( Double.isNaN(zStart) || Double.isNaN(zEnd)) return;
        if ( zEnd > zStart ) zEnd = zStart;
        
        if ( Double.isNaN(passCount) && Double.isNaN(passDepth) ) return;
                
        if ( (priority2TheCount && (passCount > 0)) || Double.isNaN(passDepth) || ((passDepth==0) && (zStart != zEnd))) {
            if ( passCount == 0) passCount = 1;
            setPassDepth( Math.abs(zStart - zEnd) / passCount);     
            
        } else {
            if ((zStart - zEnd) < 10e-6) {
                setPassDepth(0);
                passCount = 1;
                zEnd = zStart;
            } else if ( passDepth > 0) {
                int pc = (int)((zStart - zEnd) / passDepth);
                if (Math.abs((zStart - (pc * passDepth)) - zEnd) > 0.001) pc++;
                if ( passCount == 0) passCount = 1;
                if ( passCount != pc) setPassCount(pc);
            }
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
     * Update properties props according to the new one, and recalculate passCount if needed.
     * @param props property to update
     * @param newProps new value to use
     * @return Return <i>props</i> parameter (not a clone)
     */
    public static EngravingProperties udateHeritedProps(EngravingProperties props, EngravingProperties newProps) {
        if ( props == null) {
            return newProps.clone();
        } else {
            props.setEnabled( props.isEnabled() && newProps.isEnabled());
            if ( ! Double.isNaN(newProps.getFeed())) props.setFeed( newProps.getFeed());
            if ( newProps.getPower() != -1) props.setPower( newProps.getPower());

            if ( ! props.isAllAtOnce()) {
                props.setAllAtOnce( newProps.isAllAtOnce());
                if ( ! Double.isNaN(newProps.getZStart())) props.zStart = newProps.getZStart();
                if ( ! Double.isNaN(newProps.getZEnd())) props.zEnd = newProps.getZEnd();
                if ( ! Double.isNaN(newProps.getPassDepth())) props.passDepth = newProps.getPassDepth();
                if (newProps.getPassCount() != -1) props.passCount = newProps.getPassCount();
                if ( ! Double.isNaN( props.zStart + props.zEnd + props.passDepth))                                  
                    props.validatePass(false);
            }
        }
        return props;
    }
}
