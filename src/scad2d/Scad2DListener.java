// Generated from src/scad2d/Scad2D.g4 by ANTLR 4.13.1
 package scad2d; 
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Scad2DParser}.
 */
public interface Scad2DListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(Scad2DParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(Scad2DParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(Scad2DParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(Scad2DParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(Scad2DParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(Scad2DParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(Scad2DParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(Scad2DParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(Scad2DParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(Scad2DParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(Scad2DParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(Scad2DParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#rangeExpr}.
	 * @param ctx the parse tree
	 */
	void enterRangeExpr(Scad2DParser.RangeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#rangeExpr}.
	 * @param ctx the parse tree
	 */
	void exitRangeExpr(Scad2DParser.RangeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#echoCall}.
	 * @param ctx the parse tree
	 */
	void enterEchoCall(Scad2DParser.EchoCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#echoCall}.
	 * @param ctx the parse tree
	 */
	void exitEchoCall(Scad2DParser.EchoCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#strExpr}.
	 * @param ctx the parse tree
	 */
	void enterStrExpr(Scad2DParser.StrExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#strExpr}.
	 * @param ctx the parse tree
	 */
	void exitStrExpr(Scad2DParser.StrExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#colorCall}.
	 * @param ctx the parse tree
	 */
	void enterColorCall(Scad2DParser.ColorCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#colorCall}.
	 * @param ctx the parse tree
	 */
	void exitColorCall(Scad2DParser.ColorCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#translateExpr}.
	 * @param ctx the parse tree
	 */
	void enterTranslateExpr(Scad2DParser.TranslateExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#translateExpr}.
	 * @param ctx the parse tree
	 */
	void exitTranslateExpr(Scad2DParser.TranslateExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#scaleExpr}.
	 * @param ctx the parse tree
	 */
	void enterScaleExpr(Scad2DParser.ScaleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#scaleExpr}.
	 * @param ctx the parse tree
	 */
	void exitScaleExpr(Scad2DParser.ScaleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#rotateExpr}.
	 * @param ctx the parse tree
	 */
	void enterRotateExpr(Scad2DParser.RotateExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#rotateExpr}.
	 * @param ctx the parse tree
	 */
	void exitRotateExpr(Scad2DParser.RotateExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#unionCall}.
	 * @param ctx the parse tree
	 */
	void enterUnionCall(Scad2DParser.UnionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#unionCall}.
	 * @param ctx the parse tree
	 */
	void exitUnionCall(Scad2DParser.UnionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#differenceCall}.
	 * @param ctx the parse tree
	 */
	void enterDifferenceCall(Scad2DParser.DifferenceCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#differenceCall}.
	 * @param ctx the parse tree
	 */
	void exitDifferenceCall(Scad2DParser.DifferenceCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#intersectionCall}.
	 * @param ctx the parse tree
	 */
	void enterIntersectionCall(Scad2DParser.IntersectionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#intersectionCall}.
	 * @param ctx the parse tree
	 */
	void exitIntersectionCall(Scad2DParser.IntersectionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#hullCall}.
	 * @param ctx the parse tree
	 */
	void enterHullCall(Scad2DParser.HullCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#hullCall}.
	 * @param ctx the parse tree
	 */
	void exitHullCall(Scad2DParser.HullCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#mirrorCall}.
	 * @param ctx the parse tree
	 */
	void enterMirrorCall(Scad2DParser.MirrorCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#mirrorCall}.
	 * @param ctx the parse tree
	 */
	void exitMirrorCall(Scad2DParser.MirrorCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#moduleDef}.
	 * @param ctx the parse tree
	 */
	void enterModuleDef(Scad2DParser.ModuleDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#moduleDef}.
	 * @param ctx the parse tree
	 */
	void exitModuleDef(Scad2DParser.ModuleDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#moduleCall}.
	 * @param ctx the parse tree
	 */
	void enterModuleCall(Scad2DParser.ModuleCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#moduleCall}.
	 * @param ctx the parse tree
	 */
	void exitModuleCall(Scad2DParser.ModuleCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(Scad2DParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(Scad2DParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#circleExpr}.
	 * @param ctx the parse tree
	 */
	void enterCircleExpr(Scad2DParser.CircleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#circleExpr}.
	 * @param ctx the parse tree
	 */
	void exitCircleExpr(Scad2DParser.CircleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#rectangleExpr}.
	 * @param ctx the parse tree
	 */
	void enterRectangleExpr(Scad2DParser.RectangleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#rectangleExpr}.
	 * @param ctx the parse tree
	 */
	void exitRectangleExpr(Scad2DParser.RectangleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#polygonExpr}.
	 * @param ctx the parse tree
	 */
	void enterPolygonExpr(Scad2DParser.PolygonExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#polygonExpr}.
	 * @param ctx the parse tree
	 */
	void exitPolygonExpr(Scad2DParser.PolygonExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(Scad2DParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(Scad2DParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(Scad2DParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(Scad2DParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link Scad2DParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(Scad2DParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Scad2DParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(Scad2DParser.ExprContext ctx);
}