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
import java.awt.geom.Point2D;

/**
 * A 2D segment used in GPaths
 * @author Clément
 */
public class Segment2D {
    public GCode p1;
    public GCode p2;
    double a, b;    // graph : y=ax+b
    
    public Segment2D( GCode p1, GCode p2) {
        if ( p1.getX() == p2.getX()) {
            a=Double.POSITIVE_INFINITY;
            b=0;
        }
        else {
            if ( p1.getX() > p2.getX()) {
                a=(p2.getY()-p1.getY())/(p2.getX()-p1.getX());
            } else {
                a=(p1.getY()-p2.getY())/(p1.getX()-p2.getX());
            }
            b=p1.getY() -(a*p1.getX());
        }
        this.p1 = p1;
        this.p2 = p2;
    }
    
    /**
     * @param x
     * @return y=f(x)=ax+b
     */
    public double getY( double x) {
        return a * x + b;
    }
    
    /**
     * @param p
     * @return the distanceTo of the line represented by this segment and p.
     */
    public double distanceLineAnd(Point2D p) {
        if ( (a == Double.POSITIVE_INFINITY) || (a==Double.NEGATIVE_INFINITY))
            return Math.abs(p.getX()-p1.getX());
        return p.getY() - getY(p.getX());
    }
    
    /**
     * @param p
     * @return the distantce of this segment with p. */
    public double distanceTo( Point2D p) {
        if ( ((a == Double.POSITIVE_INFINITY) || (a==Double.NEGATIVE_INFINITY)) &&
                (((p.getY()>=p1.getY())&&(p.getY()<=p2.getY())) ||
                 ((p.getY()>=p2.getY())&&(p.getY()<=p1.getY()))))
                 return Math.abs(p.getX()-p1.getX());
        else {
            final double len = p1.distance(p2);
            double l1, l2=Double.NaN; 
            if ( ((l1=p.distance(p1)) < len) && ((l2=p.distance(p2)) < len)) 
                return Math.abs( a * p.getX() - p.getY() + b)/ Math.sqrt( a*a + 1);
            else {
                if ( Double.isNaN(l2)) l2=p.distance(p2);
                return Math.min(l1, l2);
            }
        }
    }
    
    /**
     * Calculate the intersection of two segments
     * @param s
     * @return null if none or the two segments are parallel.
     */
    public GCode intersectionPoint(Segment2D s) {
        
        if ( Double.compare(a,s.a) == 0) return null;
        
        final double sp1y = s.p1.getY();
        final double sp2y = s.p2.getY();
        final double p1y = p1.getY();
        final double p2y = p2.getY();     
        final double sp1x = s.p1.getX();
        final double sp2x = s.p2.getX();
        final double p1x = p1.getX();
        final double p2x = p2.getX();
        if (s.a == Double.POSITIVE_INFINITY) {
                if ( ((p1x < sp1x) && (p2x < sp1x)) ||
                     ((p1x > sp1x) && (p2x > sp1x))) return null;
                else {
                    final double y = a* sp1x + b;                 
                    if ((y >= sp1y) && (y<=sp2y)) return new GCode(sp1x, y);
                    else return null;
                }
                
        } else if (a == Double.POSITIVE_INFINITY) {
                if ( ((sp1x < p1x) && (sp2x < p1x)) ||
                     ((sp1x > p1x) && (sp2x > p1x))) return null;
                else {
                    final double y = s.a* p1x + s.b;
                    if ((y >= Math.min(p1y, p2y)) && (y<= Math.max(p1y,p2y))) return new GCode(p1x, y);
                    else return null;
                }         
        } else { 
                // any two segments
                final double cx = (s.b - b) / (a - s.a);
                if ((cx < Math.min(p1x, p2x)) || (cx > Math.max(p1x, p2x))) return null;
                if ((cx < Math.min(sp1x, sp2x)) || (cx > Math.max(sp1x, sp2x))) return null;
                final double y = a*cx+b;
                if ( (Math.abs(a) < 1e-12 ?   (Math.abs(y-p1y)>1e-12)  : (y<Math.min(p1y, p2y)) || (y>Math.max(p1y,p2y))) ||
                     (Math.abs(s.a)<1e-12 ? (Math.abs(y-sp1y)>1e-12) : (y<Math.min(sp1y, sp2y)) || (y>Math.max(sp1y,sp2y)))) return null;     
                return new GCode(cx, y);
        }
    }
    
    /**
     * @return The centered perpendicular of this Segment.
     */
    public Segment2D getTangentSegment() {        
        Point2D c = center();      
        GCode pt1 = p1.clone();        
        GCode pt2 = p2.clone();
        pt1.rotate(c, Math.PI/2);
        pt2.rotate(c, Math.PI/2);
        return new Segment2D(pt1, pt2);
    }
    
    public double getCenterOfXIntersectionInterval( Segment2D s) {
        final double p1x = Math.min(p1.getX(), p2.getX());
        final double p2x = Math.max(p1.getX(), p2.getX());
        final double sp1x = Math.min(s.p1.getX(), s.p2.getX());
        final double sp2x = Math.max(s.p1.getX(), s.p2.getX());
        if ( (p2x < sp1x) || (p1x > sp2x)) return Double.NaN;
        else return ((p1x>sp1x)?p1x:sp1x)+(((p2x>sp2x)?sp2x:p2x)-((p1x>sp1x)?p1x:sp1x))/2;
    }
    
    /**
     * @return the center of this segment
     */
    public Point2D center() {
        final double c = (p1.getX() + p2.getX())/2;
        if ( a == Double.POSITIVE_INFINITY) return new Point2D.Double(p1.getX(), (p1.getY()+ p2.getY())/2);
        return new Point2D.Double( c, getY(c));
    }

    public double getLength() {
        return p1.distance(p2);
    }

    @Override
    public String toString() {
        return "Seg{y="+a +"X +" + b + " (" + p1.getX() + "," + p1.getY() + ")-(" + p2.getX() + "," + p2.getY() + ")}";
    }

    /**
     * sort Y for the calculation of the intersectionPoint
     */
    public void sortPointsByY() {
        if ( p2.getY() < p1.getY()) {
            final GCode t = p1;
            p1 = p2;
            p2 = t;
        }
    }
    
    /**
     * @return angle in radian
     */
    public double getAngle() {
        return GElement.getAngleInRadian(p1, p2);
    }

    /**
     * Return the point at distance <i>d</i> from p1 on this segment (or in his prolongation)
     * @param d
     * @return 
     */
    public Point2D getPointAt(double d) {
        double a = getAngle();
        return new GCode(p1.getX() + Math.cos(a) * d, p1.getY() + Math.sin(a) * d);
    }

    
    public GCode getClosestPointFrom(GCode p) {
        if ( a == Double.POSITIVE_INFINITY) return new GCode( p1.getX(), p.getY());
      
        double u = ((p.getX()-p1.getX())*(p2.getX()-p1.getX())+(p.getY()-p1.getY())*(p2.getY()-p1.getY()))/(sqr(p2.getX()-p1.getX())+sqr(p2.getY()-p1.getY()));
        /*if (u > 1.0)
            return (GCode)p2.clone();
        else if (u <= 0.0)
            return (GCode)p1.clone();
        else*/
            return new GCode(p2.getX()*u+p1.getX()*(1.0-u), p2.getY()*u+p1.getY()*(1.0-u));
    }
    
    public static double sqr( double v) {
        return v*v;
    }

    /**
     * Return the point at 'len' from p1.
     * @param len
     * @return 
     */
    public GCode getPointByDistance(double len) {
        double ratio = len / getLength();
        return new GCode(p1.getX()+ (p2.getX()-p1.getX())*ratio, p1.getY() + (p2.getY()-p1.getY())*ratio);
    }
    
    /**
     * Compare segments from distance.
     * @param s
     * @return true if s is at same position than it.
     */
    public boolean equals( Segment2D s) {
        return p1.isAtSamePosition(s.p1) && p2.isAtSamePosition(s.p2);
    }
}
