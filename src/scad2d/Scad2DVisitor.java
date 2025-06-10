// Generated from src/scad2d/Scad2D.g4 by ANTLR 4.13.1
 package scad2d; 
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Scad2DParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Scad2DVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile(Scad2DParser.FileContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(Scad2DParser.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(Scad2DParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(Scad2DParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(Scad2DParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(Scad2DParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#rangeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeExpr(Scad2DParser.RangeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#echoCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEchoCall(Scad2DParser.EchoCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#strExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrExpr(Scad2DParser.StrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#colorCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColorCall(Scad2DParser.ColorCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#translateExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTranslateExpr(Scad2DParser.TranslateExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#scaleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScaleExpr(Scad2DParser.ScaleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#rotateExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRotateExpr(Scad2DParser.RotateExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#unionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionCall(Scad2DParser.UnionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#differenceCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDifferenceCall(Scad2DParser.DifferenceCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#intersectionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntersectionCall(Scad2DParser.IntersectionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#hullCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHullCall(Scad2DParser.HullCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#mirrorCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMirrorCall(Scad2DParser.MirrorCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#moduleDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModuleDef(Scad2DParser.ModuleDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#moduleCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModuleCall(Scad2DParser.ModuleCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(Scad2DParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#circleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCircleExpr(Scad2DParser.CircleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#rectangleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRectangleExpr(Scad2DParser.RectangleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#polygonExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolygonExpr(Scad2DParser.PolygonExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(Scad2DParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(Scad2DParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link Scad2DParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(Scad2DParser.ExprContext ctx);
}