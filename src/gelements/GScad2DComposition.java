/*
 * Copyright (C) 2025 moi
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
import gcodeeditor.JarvisMarchHull;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import scad2d.ColoredShape;
import scad2d.Scad2DBaseVisitor;
import scad2d.Scad2DLexer;
import scad2d.Scad2DParser;

/**
 *
 * @author moi
 */
public class GScad2DComposition extends GGroup {

    Point2D origin = new Point2D.Double();

    public static final String HEADER_STRING = "(Start Scad2DComposition-Group-name : ";
    public static final String TRANSLATE_HEADER_STRING = "(Origin : ";

    AffineTransform cumulativeTransform = new AffineTransform();
    
    String scad2DCode = 
"""
/*
 * Exemple de code Scad 2D
 */

module PourBiscuit(dim,r) {
	rect(dim); // le corps

	color("blue") { // les 4 oreilles
		circle(r);
		trans([dim[0],0]) circle(r);
		trans([0,dim[1]]) circle(r);
		trans(dim) circle(r);
	}
	
	n=7; // les cotés hoziontaux
	l=dim[0]-r*2;
	dx=l/n;
	for(i = [0:n]) {
		trans([r+dx*i,0]) circle(r/2);
		trans([r+dx*i, dim[1]]) circle(r/2);
	}
	
	r2=r/2; // les cotés verticaux
	n=5;
	l=dim[1]-r*2;
	dx=l/n;
	color("red") for(i = [0:n]) {
		trans([0,r+dx*i]) circle(r2);
		trans([dim[0], r+dx*i]) circle(r2);
	}
}

module Biscuit()
	union()
		PourBiscuit([40,30],5);


trans([50,0]) PourBiscuit([40,30],5);

color("orange:LeBiscuit")
	Biscuit();

dim2=25;
a=[[0,0,[10,20,30]],[dim2,0,50],[dim2,dim2]];
color("pink") trans(a[2]) poly(a);

rotate(45+90) diff()
{
trans([-2.5,-2]) scale(2) Biscuit();
Biscuit();
}
                        
""";

    public GScad2DComposition(String name0) {
        super(name0);
        scad2DCode = "";
        elements.add(new Code2DElement(this));
        modified = false;
    }

    public String getCode() {
        return scad2DCode;
    }

    public static final String GEN_NAME_HEANDER = "_gen";
    
    public void setCode(String newCode, boolean resetTransformations) {
        if ( resetTransformations) cumulativeTransform = new AffineTransform();
        
        int id = 0;
        @SuppressWarnings("unchecked")
        ArrayList<GElement> newElements = new ArrayList<>();

        scad2DCode = newCode;
        newElements.add(new Code2DElement(this));

        try {
            List<ColoredShape> result = buildShapeFrom(newCode);
            for (ColoredShape e : result) {
                String name;
                String color = e.getColor();
                if (color != null) {
                    if (color.contains(":")) {
                        name = color.substring(color.indexOf(":") + 1);
                    } else {
                        name = GEN_NAME_HEANDER + (id++) + "_" + color;
                    }
                } else {
                    name = GEN_NAME_HEANDER + (id++);
                }

                // keep original properties if same name as old ones
                ArrayList<GElement> p = G1Path.makeFromShape(name, e, "auto generated");
                for (GElement el : p) {
                    GElement orig = getElementName(el.getName());
                    if (orig != null) {
                        el.properties = orig.properties;
                    } else
                        if ( el.getName().startsWith(GEN_NAME_HEANDER))
                            el.properties.setEnabled(false);
                }
                if (p.size() == 1) {
                    newElements.add(p.get(0));
                } else {
                    GGroup g = new GGroup("grp_"+name);
                    // append uniqID to each elements of the group     
                    int i = 0;
                    for ( GElement el : p ) {
                        el.setName(name + i++);
                        GElement orig = getElementName(el.getName());
                        if (orig != null) el.properties = orig.properties;
                        if ( el.getName().startsWith(GEN_NAME_HEANDER))
                            el.properties.setEnabled(false);
                        g.add(el);
                    }
                    newElements.add(g);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        elements.clear();
        elements.addAll(newElements);
        super.transform(cumulativeTransform);
        informAboutChange();
    }

    public static List<ColoredShape> buildShapeFrom(String code) {
        CharStream cs = CharStreams.fromString(code);
        Scad2DLexer lexer = new Scad2DLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Scad2DParser parser = new Scad2DParser(tokens);
        ParseTree tree = parser.file();
        GScad2DComposition.ShapeBuilder shapeBuilder = new GScad2DComposition.ShapeBuilder((String message) -> {
        });
        return shapeBuilder.visit(tree);
    }

    public static double evaluate(String expression) {
        CharStream cs = CharStreams.fromString(expression);
        Scad2DLexer lexer = new Scad2DLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Scad2DParser parser = new Scad2DParser(tokens);
        Scad2DParser.ExprContext exprCtx = parser.expr();
        GScad2DComposition.ShapeBuilder shapeBuilder = new GScad2DComposition.ShapeBuilder((String message) -> {
        });
        Object res = shapeBuilder.evalExpr(exprCtx);
        if (res instanceof Double) {
            return ((Double) res).doubleValue();
        } else {
            return Double.NaN;
        }
    }

    /**
     * @return a list of all element contained in this group.
     */
    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<GElement> getAll() {
        final Collection<? extends GElement> el = (Collection<? extends GElement>) elements.clone();
        return (ArrayList<GElement>) el;
    }

    public static class ShapeBuilder extends Scad2DBaseVisitor<java.util.List<ColoredShape>> {

        private final Map<String, Object> globalVariables = new HashMap<>();
        private final Deque<Map<String, Object>> scopeStack = new ArrayDeque<>();
        private final Map<String, ModuleDef> modules = new HashMap<>();
        //private final int maxRecursionDepth = 10;
        //private Map<String, Integer> moduleCallDepth = new HashMap<>();
        LoggerInterface log;

        public ShapeBuilder(LoggerInterface log) {
            this.log = log;
            enterScope();
        }

        public interface LoggerInterface {

            public void log(String message);
        }

        @Override
        public java.util.List<ColoredShape> visitFile(Scad2DParser.FileContext ctx) {
            java.util.List<ColoredShape> shapes = new ArrayList<>();
            for (Scad2DParser.StatementContext stmt : ctx.statement()) {
                java.util.List<ColoredShape> shapeList = visit(stmt);
                if (shapeList != null) {
                    shapes.addAll(shapeList);
                }
            }
            exitScope();
            return shapes;
        }

        @Override
        public ArrayList<ColoredShape> visitForStatement(Scad2DParser.ForStatementContext ctx) {
            String varName = ctx.ID().getText();
            Scad2DParser.RangeExprContext rangeExpr = ctx.rangeExpr();
            ArrayList<ColoredShape> shapes = new ArrayList<>();

            try {
                // Évaluer les expressions de la plage
                double start = evalExprScalar(rangeExpr.expr(0));
                double end = evalExprScalar(rangeExpr.expr(rangeExpr.expr().size() > 2 ? 2 : 1));
                double step = (rangeExpr.expr().size() > 2) ? evalExprScalar(rangeExpr.expr(1)) : 1;

                // Itérer sur la plage
                for (double i = start; (step > 0) ? i <= end : i >= end; i += step) {
                    // Définir la variable d'itération dans la nouvelle portée
                    enterScope();
                    defineVar(varName, i);

                    // Visiter le corps de la boucle et ajouter la géométrie résultante
                    java.util.List<ColoredShape> otherShapes = visit(ctx.body());
                    if ((shapes != null) && (otherShapes != null)) {
                        shapes.addAll(otherShapes);
                    }

                    // Restaurer l'état précédent des variables
                    exitScope();
                }
            } catch (RuntimeException e) {
                int line = ctx.start.getLine();
                int column = ctx.start.getCharPositionInLine();
                throw new RuntimeException(String.format("Error in for loop at line %d, column %d: %s", line, column, e.getMessage()));
            }

            // Retourner les géométries résultantes
            return shapes;
        }

        @Override
        public java.util.List<ColoredShape> visitBody(Scad2DParser.BodyContext ctx) {
            java.util.List<Scad2DParser.StatementContext> statements = ctx.statement();
            if (ctx.getStart().getText().equals("{") == false && statements.size() == 1) {
                return visit(statements.get(0));
            }

            java.util.List<ColoredShape> shapes = new ArrayList<>();
            enterScope();
            for (Scad2DParser.StatementContext stmtCtx : statements) {
                java.util.List<ColoredShape> shapeList = visit(stmtCtx);
                if (shapeList != null) {
                    shapes.addAll(shapeList);
                }
            }
            exitScope();
            return shapes;
        }

        @Override
        public java.util.List<ColoredShape> visitColorCall(Scad2DParser.ColorCallContext ctx) {
            String color = evalStrExpr(ctx.strExpr());
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());

            if (shapes != null) {
                // Créer une nouvelle liste mutable à partir de la liste immuable
                ArrayList<ColoredShape> mutableShapes = new ArrayList<>(shapes);
                for (int i = 0; i < mutableShapes.size(); i++) {
                    ColoredShape shape = mutableShapes.get(i);
                    mutableShapes.set(i, new ColoredShape(shape.getShape(), color));
                }
                return mutableShapes;
            }
            return shapes;
        }

        @Override
        public ArrayList<ColoredShape> visitCircleExpr(Scad2DParser.CircleExprContext ctx) {
            double radius = evalExprScalar(ctx.expr(0));
            int segments = ctx.expr().size() > 1 ? (int) evalExprScalar(ctx.expr(1)) : 64;
            Shape circle = new Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2);

            ArrayList<ColoredShape> result = new ArrayList<>();
            result.add(new ColoredShape(circle, null));
            return result;
        }

        @Override
        public java.util.List<ColoredShape> visitRectangleExpr(Scad2DParser.RectangleExprContext ctx) {
            double width, height;
            if (ctx.ID().size() == 2) {
                double firstValue = evalExprScalar(ctx.expr(0));
                double secondValue = evalExprScalar(ctx.expr(1));
                width = ctx.ID(0).getText().equals("w") ? firstValue : secondValue;
                height = ctx.ID(0).getText().equals("w") ? secondValue : firstValue;
            } else {
                Object exprValue = evalExpr(ctx.expr(0));
                if (exprValue instanceof Double) {
                    width = height = (Double) exprValue;
                } else if (exprValue instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Double> dimensions = (java.util.List<Double>) exprValue;
                    width = dimensions.get(0);
                    height = dimensions.get(1);
                } else {
                    throw new RuntimeException("Line " + ctx.start.getLine() + ": Unsupported expression type for rectangle.");
                }
            }
            Shape rectangle = new Rectangle2D.Double(0, 0, width, height);
            ArrayList<ColoredShape> result = new ArrayList<>();
            result.add(new ColoredShape(rectangle, null));
            return result;
        }

        @Override
        public java.util.List<ColoredShape> visitPolygonExpr(Scad2DParser.PolygonExprContext ctx) {
            Path2D.Double path = new Path2D.Double();
            boolean first = true;
            
            // Vérifier si l'argument est un identifiant ou une liste de coordonnées
            if (ctx.ID() != null) {
                String varName = ctx.ID().getText();
                Object value = resolveVarRaw(varName, ctx.start.getLine(), ctx.start.getCharPositionInLine());

                if (value instanceof List l) {
                    for (Object e : l) {
                        if (e instanceof List l2) {
                            if (first) {
                                path.moveTo((Double) l2.getFirst(), (Double) l2.get(1));
                                first = false;
                            } else {
                                path.lineTo((Double) l2.getFirst(), (Double) l2.get(1));
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Line " + ctx.start.getLine() + ": Expected a list of coordinates for polygon.");
                }
            } else if (ctx.list() != null) {
                Object l = evalList(ctx.list());

                if (l instanceof List l2) {
                    for (Object e : l2) {
                        if (e instanceof List l3) {
                            if (l3.size() < 2) {
                                throw new RuntimeException("line " + ctx.getStart().getLine() + " : Wrong coordinates in list for polygon.");
                            }
                            double x = (Double) l3.get(0);
                            double y = (Double) l3.get(1);
                            if (first) {
                                path.moveTo(x, y);
                                first = false;
                            } else {
                                path.lineTo(x, y);
                            }
                        }
                    }
                }
            }

            if ( ! ctx.func.getText().equals("path") ) path.closePath();
            ArrayList<ColoredShape> res = new ArrayList<>();
            res.add(new ColoredShape(path, null));
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitTranslateExpr(Scad2DParser.TranslateExprContext ctx) {
            Object coord = evalExprList(ctx.exprList());
            if (coord instanceof List l) {
                java.util.List<ColoredShape> shapes = visitBody(ctx.body());
                if (shapes != null) {
                    if ((l.size() < 2) || (l.getFirst() instanceof List) || (l.get(1) instanceof List)) {
                        throw new RuntimeException("Line " + ctx.start.getLine() + ": unexpected value for translate.");
                    }
                    AffineTransform transform = AffineTransform.getTranslateInstance((Double) l.getFirst(), (Double) l.get(1));

                    for (int i = 0; i < shapes.size(); i++) {
                        ColoredShape shape = shapes.get(i);
                        shapes.set(i, new ColoredShape(transform.createTransformedShape(shape.getShape()), shape.getColor()));
                    }
                }
                return shapes;
            } else {
                throw new RuntimeException("Line " + ctx.start.getLine() + ": Expected 2D array for translate.");
            }
        }

        @Override
        public java.util.List<ColoredShape> visitScaleExpr(Scad2DParser.ScaleExprContext ctx) {
            Object coord = evalExprList(ctx.exprList());
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            if ((shapes == null) || shapes.isEmpty()) {
                return shapes;
            }

            if (coord instanceof Double) {
                AffineTransform transform = AffineTransform.getScaleInstance((Double) coord, (Double) coord);

                for (int i = 0; i < shapes.size(); i++) {
                    ColoredShape shape = shapes.get(i);
                    shapes.set(i, new ColoredShape(transform.createTransformedShape(shape.getShape()), shape.getColor()));
                }
            } else if (coord instanceof List) {

                @SuppressWarnings("unchecked")
                double[] d = ((ArrayList<double[]>) coord).getFirst();
                if (d.length < 2) {
                    throw new RuntimeException("Line " + ctx.start.getLine() + ": Expected 2D array for translate.");
                }
                if (d.length != 2) {
                    log.log("Warning line " + ctx.getStart().getLine() + " : Expected 2D array for translate.");
                }
                AffineTransform transform = AffineTransform.getScaleInstance(d[0], d[1]);

                for (int i = 0; i < shapes.size(); i++) {
                    ColoredShape shape = shapes.get(i);
                    shapes.set(i, new ColoredShape(transform.createTransformedShape(shape.getShape()), shape.getColor()));
                }

            } else {
                throw new RuntimeException("line" + ctx.getStart().getLine() + ": Expected single value or 2D array for translate.");
            }
            return shapes;
        }

        @Override
        public java.util.List<ColoredShape> visitRotateExpr(Scad2DParser.RotateExprContext ctx) {
            double angle = Math.toRadians(evalExprScalar(ctx.expr()));
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            AffineTransform transform = AffineTransform.getRotateInstance(angle);
            if (shapes != null) {
                for (int i = 0; i < shapes.size(); i++) {
                    ColoredShape shape = shapes.get(i);
                    shapes.set(i, new ColoredShape(transform.createTransformedShape(shape.getShape()), shape.getColor()));
                }
            }
            return shapes;
        }

        @Override
        public java.util.List<ColoredShape> visitUnionCall(Scad2DParser.UnionCallContext ctx) {
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            if (shapes == null || shapes.isEmpty()) {
                return shapes;
            }

            Area unionArea = new Area(shapes.get(0).getShape());
            for (int i = 1; i < shapes.size(); i++) {
                unionArea.add(new Area(shapes.get(i).getShape()));
            }
            
            ArrayList<ColoredShape> res = new ArrayList<>();
            res.add(new ColoredShape(unionArea, shapes.get(0).getColor()));
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitDifferenceCall(Scad2DParser.DifferenceCallContext ctx) {
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            if (shapes == null || shapes.isEmpty()) {
                return shapes;
            }

            Area differenceArea = new Area(shapes.get(0).getShape());
            for (int i = 1; i < shapes.size(); i++) {
                differenceArea.subtract(new Area(shapes.get(i).getShape()));
            }

            ArrayList<ColoredShape> res = new ArrayList<>();
            res.add(new ColoredShape(differenceArea, shapes.get(0).getColor()));
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitIfStatement(Scad2DParser.IfStatementContext ctx) {
            // Évaluer la condition
            double condition = evalExprScalar(ctx.expr());

            // Visiter le bloc 'if' si la condition est vraie
            if (condition != 0) {
                return visit(ctx.body(0));
            } // Visiter le bloc 'else' s'il existe et si la condition est fausse
            else if (ctx.body().size() > 1) {
                return visit(ctx.body(1));
            }

            // Retourner une liste vide si aucun bloc n'est exécuté
            return new ArrayList<>();
        }

        @Override
        public java.util.List<ColoredShape> visitIntersectionCall(Scad2DParser.IntersectionCallContext ctx) {
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            if (shapes == null || shapes.isEmpty()) {
                return shapes;
            }

            Area intersectionArea = new Area(shapes.get(0).getShape());
            for (int i = 1; i < shapes.size(); i++) {
                intersectionArea.intersect(new Area(shapes.get(i).getShape()));
            }

            ArrayList<ColoredShape> res = new ArrayList<>();
            res.add(new ColoredShape(intersectionArea, shapes.get(0).getColor()));
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitHullCall(Scad2DParser.HullCallContext ctx) {
            java.util.List<ColoredShape> shapes = visitBody(ctx.body());
            if (shapes == null || shapes.isEmpty()) {
                return shapes;
            }

            // Simplified convex hull calculation
            Area hullArea = new Area();
            for (ColoredShape shape : shapes) {
                hullArea.add(new Area(shape.getShape()));
            }
            ArrayList<GElement> r = G1Path.makeFromShape("hullArea", hullArea, "");
            GGroup g = new GGroup(r, false);

            ArrayList<GCode> pts = g.addAllPointForHull(null);
            G1Path hullPath = new G1Path("HullPath");

            JarvisMarchHull jm = new JarvisMarchHull((Point2D[]) pts.toArray(new GCode[pts.size()]));
            Point2D[] hullRegion = jm.getHull();

            ArrayList<ColoredShape> res = new ArrayList<>();
            if (hullRegion.length > 0) {
                for (Point2D pt2 : hullRegion) {
                    hullPath.add((GCode) pt2);
                }
                hullPath.add((GCode) hullRegion[0].clone());
                hullPath.updateRenderShape();
                res.add(new ColoredShape(hullPath.renderedShape, shapes.get(0).getColor()));
            }
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitEchoCall(Scad2DParser.EchoCallContext ctx) {
            log.log(evalStrExpr(ctx.strExpr()));
            return null;
        }

        public String evalStrExpr(List<Scad2DParser.StrExprContext> ctx) {
            String res = "";
            for (Scad2DParser.StrExprContext s : ctx) {
                if (s.expr() != null) {
                    Object v = evalExpr(s.expr());
                    
                    if ((v instanceof Double d) && (Math.abs(d - d.intValue()) < 10e-10)) {
                        res += d.intValue();
                    } else {
                        res += v.toString();
                    }
                } else {
                    String stringValue = s.getText();
                    res += stringValue.substring(1, stringValue.length() - 1);
                }
            }
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitAssignment(Scad2DParser.AssignmentContext ctx) {
            if (ctx.ID().getText().equals("$fn")) {
                defineVar("$fn", evalExprScalar(ctx.expr()));
            } else {
                if (ctx.expr() != null || ctx.list() != null) {
                    defineVar(ctx.ID().getText(), (ctx.expr() != null) ? evalExpr(ctx.expr()) : evalList(ctx.list()));
                }
            }
            return null;
        }

        public Object evalExprList(Scad2DParser.ExprListContext ctx) {
            if (ctx.expr() != null) {
                return evalExpr(ctx.expr());
            } else {
                return evalList(ctx.list());
            }
        }

        public ArrayList<Object> evalList(Scad2DParser.ListContext list) {
            ArrayList<Object> res = new ArrayList<>(list.exprList().size());

            for (Scad2DParser.ExprListContext c : list.exprList()) {
                res.add(evalExprList(c));
            }
            return res;
        }

        @Override
        public java.util.List<ColoredShape> visitModuleDef(Scad2DParser.ModuleDefContext ctx) {
            String name = ctx.ID().getText();
            java.util.List<String> paramNames = new ArrayList<>();
            if (ctx.paramList() != null) {
                for (TerminalNode id : ctx.paramList().ID()) {
                    paramNames.add(id.getText());
                }
            }
            modules.put(name, new ModuleDef(name, paramNames, ctx.body()));
            return null;
        }

        @Override
        public List<ColoredShape> visitMirrorCall(Scad2DParser.MirrorCallContext ctx) {
            // Evaluate the expression list to get mirror parameters
            List<Object> mirrorParams = (List<Object>) evalList(ctx.list());

            // Ensure there are enough parameters
            if ((mirrorParams.size() < 2) || !(mirrorParams.get(0) instanceof Double) || !(mirrorParams.get(1) instanceof Double)) {
                throw new RuntimeException("Line " + ctx.start.getLine() + ": Mirror requires at least two parameters: x and y coordinates for the mirror line.");
            }

            double mirrorX = (Double) mirrorParams.get(0) * -1;
            double mirrorY = (Double) mirrorParams.get(1) * -1;
            if (mirrorX == 0) {
                mirrorX = 1;
            }
            if (mirrorY == 0) {
                mirrorY = 1;
            }
            AffineTransform transform = new AffineTransform();
            transform.scale(mirrorX, mirrorY);

            // Visit the body to get the shapes to be mirrored
            List<ColoredShape> originalShapes = (List<ColoredShape>) visit(ctx.body());

            // Create a list to hold the mirrored shapes
            List<ColoredShape> mirroredShapes = new ArrayList<>();

            // Apply mirror transformation to each shape
            for (ColoredShape shape : originalShapes) {
                Shape original = shape.getShape();
                Shape mirroredShape = transform.createTransformedShape(original);
                mirroredShapes.add(new ColoredShape(mirroredShape, shape.getColor()));
            }

            return mirroredShapes;
        }

        @Override
        public java.util.List<ColoredShape> visitModuleCall(Scad2DParser.ModuleCallContext ctx) {
            String moduleName = ctx.ID().getText();
            ModuleDef moduleDef = modules.get(moduleName);
            if (moduleDef == null) {
                throw new RuntimeException("Module not found: " + moduleName);
            }
            enterScope();
            java.util.List<String> paramNames = moduleDef.getParameters();
            java.util.List<Scad2DParser.ExprContext> exprs = ctx.expr();
            if (exprs.size() != paramNames.size()) {
                throw new RuntimeException("Line "+ctx.getStart().getLine()+": Incorrect number of parameters for module: " + moduleName);
            }
            for (int i = 0; i < paramNames.size(); i++) {
                String paramName = paramNames.get(i);
                Object value = evalExpr(exprs.get(i));
                defineVar(paramName, value);
            }
            java.util.List<ColoredShape> result = visit(moduleDef.getBodyContext());
            exitScope();
            return result;
        }

        private void defineVar(String name, Object value) {
            if ( Character.isUpperCase(name.charAt(0)))
                globalVariables.put( name, value);
            else
                scopeStack.peek().put(name, value);
        }

        private Object resolveVarRaw(String name, int line, int column) {
            if ( Character.isUpperCase(name.charAt(0))) {
                if ( globalVariables.containsKey(name)) return globalVariables.get(name);
                else throw new RuntimeException("line " + line + ":" + column + ", Global variable not found: " + name);

            }              
            for (Map<String, Object> scope : scopeStack) {
                if (scope.containsKey(name)) {
                    return scope.get(name);
                }
            }
            throw new RuntimeException("line " + line + ":" + column + ", Variable not found: " + name);
        }

        private double evalExprScalar(Scad2DParser.ExprContext ctx) {
            Object value = evalExpr(ctx);
            if (value instanceof Boolean b) {
                return b ? 1 : 0;
            }
            if (value instanceof Double) {
                return (Double) value;
            } else {
                throw new RuntimeException("Line " + ctx.start.getLine() + ": Expected scalar value");
            }
        }

        private Object evalExpr(Scad2DParser.ExprContext ctx) {
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();

            if (ctx.ID() != null) {
                String varName = ctx.ID().getText();
                if (varName.equals("$fn")) {
                    throw new RuntimeException("Line " + line + ": $fn cannot be used as a regular variable.");
                }
                Object v = resolveVarRaw(varName, line, column);
                if (ctx.expr() != null) {
                    for (Scad2DParser.ExprContext e : ctx.expr()) {
                        double i = evalExprScalar(e);
                        if (v instanceof List<?> l) {
                            v = l.get((int) i);
                        } else {
                            throw new RuntimeException("Line " + line + ": Cannot index non-list object.");
                        }
                    }
                }
                return v;

            } else if (ctx.NUMBER() != null) {
                return Double.valueOf(ctx.NUMBER().getText());

            } else if (ctx.getText().equalsIgnoreCase("pi")) {
                return Math.PI;

            } else if (ctx.getText().equals("true")) {
                return true;

            } else if (ctx.getText().equals("false")) {
                return false;

            } else if (ctx.list() != null) {
                return evalList(ctx.list());

            } else if (ctx.expr() != null && !ctx.expr().isEmpty()) {

                // Handle unary operators
                if (ctx.op != null && ctx.expr().size() == 1) {
                    Object value = evalExpr(ctx.expr(0));
                    switch (ctx.op.getText()) {
                        case "-":
                            return -((Double) value);
                        case "!":
                            return !(Boolean) value;
                        default:
                            throw new RuntimeException("Line " + line + ": Unknown unary operator '" + ctx.op.getText() + "'.");
                    }
                }

                // Handle binary and ternary operations
                if (ctx.op != null && ctx.expr().size() == 2) {
                    Object left = evalExpr(ctx.expr(0));
                    Object right = evalExpr(ctx.expr(1));
                    String op = ctx.op.getText();

                    if (left instanceof Double l && right instanceof Double r) {
                        return switch (op) {
                            case "%" ->
                                l % r;
                            case "*" ->
                                l * r;
                            case "/" ->
                                l / r;
                            case "+" ->
                                l + r;
                            case "-" ->
                                l - r;
                            case ">" ->
                                l > r;
                            case ">=" ->
                                l >= r;
                            case "<" ->
                                l < r;
                            case "<=" ->
                                l <= r;
                            case "==" ->
                                l.equals(r);
                            case "!=" ->
                                !l.equals(r);
                            default ->
                                throw new RuntimeException("Line " + line + ": Unknown numeric operator '" + op + "'.");
                        };
                    } else if (left instanceof Boolean l && right instanceof Boolean r) {
                        return switch (op) {
                            case "&&" ->
                                l && r;
                            case "||" ->
                                l || r;
                            case "==" ->
                                l == r;
                            case "!=" ->
                                l != r;
                            default ->
                                throw new RuntimeException("Line " + line + ": Unknown boolean operator '" + op + "'.");
                        };
                    } else if ( left instanceof List l1 && right instanceof List l2) {
                        if ( op.equals("+")) {
                            List res = new ArrayList<Object>(l1);
                            res.addAll(l2);
                            return res;
                        }
                        else throw new RuntimeException("Line " + line + ": Only '+' operator is allowed with lists" + op + "'.");
                    } else {
                        throw new RuntimeException("Line " + line + ": Incompatible types for operator '" + op + "'.");
                    }
                }

                if (ctx.expr().size() == 3 && ctx.children.get(1).getText().equals("?")) {
                    Object cond = evalExpr(ctx.expr(0));
                    boolean result;
                    if (cond instanceof Boolean b) {
                        result = b;
                    } else if (cond instanceof Double d) {
                        result = Math.abs(d) > 1e-10;
                    } else {
                        throw new RuntimeException("Line " + line + ": Invalid condition type for ternary expression.");
                    }
                    return result ? evalExpr(ctx.expr(1)) : evalExpr(ctx.expr(2));
                }

                // Handle functions
                if (ctx.getText().matches("^(abs|sin|cos|tan|asin|acos|atan|int|min|max|len)\\(.*\\)$")) {
                    String func = ctx.getText().split("\\(")[0];
                    List<Scad2DParser.ExprContext> args = ctx.expr();
                    return switch (func) {
                        case "abs" ->
                            Math.abs(evalExprScalar(args.get(0)));
                        case "sin" ->
                            Math.sin(Math.toRadians(evalExprScalar(args.get(0))));
                        case "cos" ->
                            Math.cos(Math.toRadians(evalExprScalar(args.get(0))));
                        case "tan" ->
                            Math.tan(Math.toRadians(evalExprScalar(args.get(0))));
                        case "asin" ->
                            Math.asin(Math.toRadians(evalExprScalar(args.get(0))));
                        case "acos" ->
                            Math.acos(Math.toRadians(evalExprScalar(args.get(0))));
                        case "atan" ->
                            Math.atan(Math.toRadians(evalExprScalar(args.get(0))));
                        case "int" ->
                            (int) evalExprScalar(args.get(0));
                        case "min" ->
                            args.stream().mapToDouble(this::evalExprScalar).min().orElseThrow();
                        case "max" ->
                            args.stream().mapToDouble(this::evalExprScalar).max().orElseThrow();
                        case "len" -> 
                            evalExpr(args.get(0)) instanceof List l ? l.size() : 0;
                        default ->
                            throw new RuntimeException("Line " + line + ": Unknown function '" + func + "'.");
                    };
                }

                // Handle parenthesis
                if (ctx.getText().startsWith("(") && ctx.getText().endsWith(")")) {
                    return evalExpr(ctx.expr(0));
                }

                // Handle vector literals
                if (ctx.getText().startsWith("[") && ctx.getText().endsWith("]")) {
                    List<Double> values = new ArrayList<>();
                    for (Scad2DParser.ExprContext e : ctx.expr()) {
                        values.add(evalExprScalar(e));
                    }
                    return values;
                }
            }

            throw new RuntimeException("Line " + line + ": Unsupported expression type: " + ctx.getText());
        }

        private void enterScope() {
            scopeStack.push(new HashMap<>());
        }

        private void exitScope() {
            scopeStack.pop();
        }
    }

    static class ModuleDef {

        private final String name;
        private final java.util.List<String> parameters;
        private final Scad2DParser.BodyContext bodyContext;

        public ModuleDef(String name, java.util.List<String> parameters, Scad2DParser.BodyContext bodyContext) {
            this.name = name;
            this.parameters = parameters;
            this.bodyContext = bodyContext;
        }

        public String getName() {
            return name;
        }

        public java.util.List<String> getParameters() {
            return parameters;
        }

        public Scad2DParser.BodyContext getBodyContext() {
            return bodyContext;
        }
    }

    /**
     * This virtual Element it used : - in EditListAction to detect and activate
     * Scad2D code edition, - to save and restore the code in GCODE file.
     */
    public class Code2DElement extends G1Path {

        public static final String HEADER_STRING = "(Scad2DCode-name: ";

        public static final String SCAD2D_CODE_HEADER = "; Scad2D code (Base64 encoded) : ";
        GScad2DComposition compo;

        Code2DElement(GScad2DComposition elem) {
            super("Scad2D Code");
            compo = elem;
            String b64str = Base64.getEncoder().encodeToString(elem.getCode().getBytes(StandardCharsets.UTF_8));
            add(new GCode(SCAD2D_CODE_HEADER + b64str));
        }

        /*
        @Override
        public GCode getElementAt(int index) {
            return (index == 0) ? new GCode("; Scad2D code") : new GCode();
        }*/
        /**
         * @return the GScad2DComposition associated to this Scad2D code
         */
        public GScad2DComposition getCompositionElement() {
            return compo;
        }

        @Override
        public String toString() {
            return "<Scad2D Code>";
        }

        @Override
        public String getSummary() {
            return "<html>Code that (re)generate all content of this 2D composition.<br><br>The properties are preserved only for paths that have been named with color(\"color:name\")";
        }
    }

    @Override
    public String toString() {
        return name + " (Scad2D Composition)";
    }

    @Override
    public String getSummary() {
        return "Scad2D composition";
    }

    @Override
    public void translate(double dx, double dy) {
        AffineTransform t = AffineTransform.getTranslateInstance(dx, dy);
        cumulativeTransform.concatenate(t);  // Accumule la transformation
        super.translate(dx, dy);
    }
    
    @Override
    public void rotate(Point2D origin, double angle) {
        AffineTransform t = AffineTransform.getRotateInstance(angle, origin.getX(), origin.getY());
        cumulativeTransform.concatenate(t);  // Accumule la transformation
        super.rotate(origin, angle);
    }
    
    @Override
    public void transform(AffineTransform t) {
        cumulativeTransform.concatenate(t);  // Accumule la transformation
        super.transform(t);
    }

    /*
    @Override
    public int getSize() {
        return 1;
    }
    @Override
    public GElement getElementFromPoint(GCode pt, double dmin, ArrayList<GElement> intoThis) {
        return null;
    }*/

    @Override
    public String loadFromStream(BufferedReader stream, GCode lastGState) throws IOException {
        String l = super.loadFromStream(stream, null);
        if (l == null) {
            return null;
        }

        if (l.startsWith(Code2DElement.SCAD2D_CODE_HEADER)) {
            scad2DCode = new String(Base64.getDecoder().decode(l.substring(Code2DElement.SCAD2D_CODE_HEADER.length())), StandardCharsets.UTF_8);
        }

        l = stream.readLine();
        if (l.startsWith(TRANSLATE_HEADER_STRING)) {
            double ox = Double.parseDouble(l.substring(TRANSLATE_HEADER_STRING.length(), l.indexOf(",")));
            double oy = Double.parseDouble(l.substring(l.indexOf(',') + 1, l.indexOf(")")));
            origin = new Point2D.Double(ox, oy);
        }

        Code2DElement c2d;
        elements.set(0, c2d = new Code2DElement(this));

        if (c2d.getSummary().equals(scad2DCode)) System.out.println("c2d.getSummary().equals(scad2DCode) is false");
        return stream.readLine();
    }

    @Override
    public GCode saveToStream(FileWriter fw, GCode lastPoint) throws IOException {
        fw.append(HEADER_STRING + name + ")\n");
        fw.append(properties.toString() + "\n");
        fw.append(((GScad2DComposition.Code2DElement) elements.get(0)).getLine(0).toString() + "\n");
        fw.append(TRANSLATE_HEADER_STRING + origin.getX() + " ," + origin.getY() + " )\n");
        for (GElement e : elements) {
            if (!(e instanceof GScad2DComposition.Code2DElement)) {
                lastPoint = e.saveToStream(fw, lastPoint);
            }
        }

        fw.append(END_HEADER_STRING + name + ")\n");
        return lastPoint;
    }

    @Override
    public GElement remove(int i) {
        if (i != 0) return super.remove(i);
        return null;
    }

    @Override
    public boolean remove(GElement e) {
        if (!(e instanceof GScad2DComposition.Code2DElement))
            return super.remove(e);
        return false;
    }
        
}
