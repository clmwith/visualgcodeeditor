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

import gcodeeditor.GCode;
import java.awt.geom.Point2D;


/**
 * Translated from bCnC : gear.py file
 * @author Clément
 */
public class GearHelper {
    
    static double involute_intersect_angle( double Rb, double R) {
        return Math.sqrt(R*R - Rb*Rb) / Rb - Math.acos(Rb/R);
    }
    
    static GCode point_on_circle(double radius, double angle) {
		return new GCode(radius * Math.cos(angle), radius * Math.sin(angle));
    }

    /**
     * @param numberOfTeeth  number of teeth
     * @param pressureAngle  pressure angle
     * @param circularPitch  Circular Pitch
     * @return a gear
     */
    public static GGroup makeGear(double numberOfTeeth, double pressureAngle, double circularPitch) {
		pressureAngle = Math.toRadians(pressureAngle);
                numberOfTeeth = Math.abs(numberOfTeeth);
		//# Pitch Circle
		double D = numberOfTeeth * circularPitch / Math.PI;
		double R = D / 2.0;

		//# Diametrical pitch
		double Pd = numberOfTeeth / D;

		//# Base Circle
		double Db = D * Math.cos(pressureAngle);
		double Rb = Db / 2.0;

		//# Addendum
		double a = 1.0 / Pd;

		//# Outside Circle
		double Ro = R + a;
		//double Do = 2.0 * Ro;

		//# Tooth thickness
		//double T = Math.PI*D / (2*numberOfTeeth);

		//# undercut?
		//double U = 2.0 / (Math.sin(pressureAngle) * (Math.sin(pressureAngle)));
		//boolean needs_undercut = numberOfTeeth < U;
		//# sys.stderr.write("N:%s R:%s Rb:%s\n" % (N,R,Rb))

		//# Clearance
		double c = 0.0;
		//# Dedendum
		double b = a + c;

		//# Root Circle
		double Rr = R - b;
		//double Dr = 2.0*Rr;

		double two_pi = 2.0*Math.PI;
		double half_thick_angle = two_pi / (4.0*numberOfTeeth);
		double pitch_to_base_angle = involute_intersect_angle(Rb, R);
		double pitch_to_outer_angle = involute_intersect_angle(Rb, Ro); // # pitch_to_base_angle

		G1Path points = new G1Path("gear");
		for( int x = 1; x < numberOfTeeth+1; x++) {
			c = x * two_pi / numberOfTeeth;

			//# angles
			double pitch1 = c - half_thick_angle;
			double base1  = pitch1 - pitch_to_base_angle;
			double outer1 = pitch1 + pitch_to_outer_angle;

			double pitch2 = c + half_thick_angle;
			double base2  = pitch2 + pitch_to_base_angle;
			double outer2 = pitch2 - pitch_to_outer_angle;

			//# points
			GCode b1 = point_on_circle(Rb, base1);
			GCode p1 = point_on_circle(R,  pitch1);
			GCode o1 = point_on_circle(Ro, outer1);
			GCode o2 = point_on_circle(Ro, outer2);
			GCode p2 = point_on_circle(R,  pitch2);
			GCode b2 = point_on_circle(Rb, base2);

			if (Rr >= Rb) {
				double pitch_to_root_angle = pitch_to_base_angle - involute_intersect_angle(Rb, Rr);
				double root1 = pitch1 - pitch_to_root_angle;
				double root2 = pitch2 + pitch_to_root_angle;
				GCode r1 = point_on_circle(Rr, root1);
				GCode r2 = point_on_circle(Rr, root2);

				points.add(r1);
				points.add(p1);
				points.add(o1);
				points.add(o2);
				points.add(p2);
				points.add(r2);
                         } else {
				GCode r1 = point_on_circle(Rr, base1);
				GCode r2 = point_on_circle(Rr, base2);
				points.add(r1);
				points.add(b1);
				points.add(p1);
				points.add(o1);
				points.add(o2);
				points.add(p2);
				points.add(b2);
				points.add(r2);
                        }
                }
                points.add((GCode) points.getFirstPoint().clone());
                GGroup gear = new GGroup("gear");
                gear.add(points);
                GArc arc;
                gear.add(arc = new GArc("circle", new Point2D.Double(), R, 0., 360.));
                arc.properties.setEnabled(false);
                return gear;
    }
}