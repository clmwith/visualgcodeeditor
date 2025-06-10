// Generated from src/scad2d/Scad2D.g4 by ANTLR 4.13.1
 package scad2d; 
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class Scad2DParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		T__52=53, T__53=54, T__54=55, T__55=56, T__56=57, T__57=58, T__58=59, 
		WS=60, COMMENT=61, MULTILINE_COMMENT=62, ID=63, NUMBER=64, STRING=65, 
		FN=66;
	public static final int
		RULE_file = 0, RULE_body = 1, RULE_statement = 2, RULE_assignment = 3, 
		RULE_ifStatement = 4, RULE_forStatement = 5, RULE_rangeExpr = 6, RULE_echoCall = 7, 
		RULE_strExpr = 8, RULE_colorCall = 9, RULE_translateExpr = 10, RULE_scaleExpr = 11, 
		RULE_rotateExpr = 12, RULE_unionCall = 13, RULE_differenceCall = 14, RULE_intersectionCall = 15, 
		RULE_hullCall = 16, RULE_mirrorCall = 17, RULE_moduleDef = 18, RULE_moduleCall = 19, 
		RULE_paramList = 20, RULE_circleExpr = 21, RULE_rectangleExpr = 22, RULE_polygonExpr = 23, 
		RULE_list = 24, RULE_exprList = 25, RULE_expr = 26;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "body", "statement", "assignment", "ifStatement", "forStatement", 
			"rangeExpr", "echoCall", "strExpr", "colorCall", "translateExpr", "scaleExpr", 
			"rotateExpr", "unionCall", "differenceCall", "intersectionCall", "hullCall", 
			"mirrorCall", "moduleDef", "moduleCall", "paramList", "circleExpr", "rectangleExpr", 
			"polygonExpr", "list", "exprList", "expr"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'='", "';'", "'if'", "'('", "')'", "'else'", "'for'", 
			"'['", "':'", "']'", "'echo'", "'+'", "'color'", "'translate'", "'trans'", 
			"'scale'", "'rotate'", "'union'", "'difference'", "'diff'", "'intersection'", 
			"'inter'", "'hull'", "'mirror'", "'module'", "','", "'circle'", "'square'", 
			"'rect'", "'rectangle'", "'cube'", "'poly'", "'polygon'", "'-'", "'!'", 
			"'*'", "'/'", "'%'", "'>'", "'>='", "'<'", "'<='", "'=='", "'!='", "'&&'", 
			"'||'", "'true'", "'false'", "'PI'", "'abs'", "'sin'", "'cos'", "'tan'", 
			"'int'", "'min'", "'max'", "'?'", null, null, null, null, null, null, 
			"'$fn'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"WS", "COMMENT", "MULTILINE_COMMENT", "ID", "NUMBER", "STRING", "FN"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Scad2D.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public Scad2DParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(Scad2DParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitFile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitFile(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 5)) & ~0x3f) == 0 && ((1L << (_la - 5)) & 2594073387504499985L) != 0)) {
				{
				{
				setState(54);
				statement();
				}
				}
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(60);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BodyContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_body);
		int _la;
		try {
			setState(71);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case T__8:
			case T__12:
			case T__14:
			case T__15:
			case T__16:
			case T__17:
			case T__18:
			case T__19:
			case T__20:
			case T__21:
			case T__22:
			case T__23:
			case T__24:
			case T__25:
			case T__26:
			case T__28:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
			case ID:
			case FN:
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				statement();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				match(T__0);
				setState(67);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((((_la - 5)) & ~0x3f) == 0 && ((1L << (_la - 5)) & 2594073387504499985L) != 0)) {
					{
					{
					setState(64);
					statement();
					}
					}
					setState(69);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(70);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public IfStatementContext ifStatement() {
			return getRuleContext(IfStatementContext.class,0);
		}
		public ForStatementContext forStatement() {
			return getRuleContext(ForStatementContext.class,0);
		}
		public ModuleDefContext moduleDef() {
			return getRuleContext(ModuleDefContext.class,0);
		}
		public CircleExprContext circleExpr() {
			return getRuleContext(CircleExprContext.class,0);
		}
		public RectangleExprContext rectangleExpr() {
			return getRuleContext(RectangleExprContext.class,0);
		}
		public PolygonExprContext polygonExpr() {
			return getRuleContext(PolygonExprContext.class,0);
		}
		public TranslateExprContext translateExpr() {
			return getRuleContext(TranslateExprContext.class,0);
		}
		public ScaleExprContext scaleExpr() {
			return getRuleContext(ScaleExprContext.class,0);
		}
		public RotateExprContext rotateExpr() {
			return getRuleContext(RotateExprContext.class,0);
		}
		public UnionCallContext unionCall() {
			return getRuleContext(UnionCallContext.class,0);
		}
		public DifferenceCallContext differenceCall() {
			return getRuleContext(DifferenceCallContext.class,0);
		}
		public IntersectionCallContext intersectionCall() {
			return getRuleContext(IntersectionCallContext.class,0);
		}
		public MirrorCallContext mirrorCall() {
			return getRuleContext(MirrorCallContext.class,0);
		}
		public HullCallContext hullCall() {
			return getRuleContext(HullCallContext.class,0);
		}
		public ColorCallContext colorCall() {
			return getRuleContext(ColorCallContext.class,0);
		}
		public EchoCallContext echoCall() {
			return getRuleContext(EchoCallContext.class,0);
		}
		public ModuleCallContext moduleCall() {
			return getRuleContext(ModuleCallContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		try {
			setState(91);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(73);
				assignment();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(74);
				ifStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(75);
				forStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(76);
				moduleDef();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(77);
				circleExpr();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(78);
				rectangleExpr();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(79);
				polygonExpr();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(80);
				translateExpr();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(81);
				scaleExpr();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(82);
				rotateExpr();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(83);
				unionCall();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(84);
				differenceCall();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(85);
				intersectionCall();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(86);
				mirrorCall();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(87);
				hullCall();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(88);
				colorCall();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(89);
				echoCall();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(90);
				moduleCall();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public TerminalNode FN() { return getToken(Scad2DParser.FN, 0); }
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_assignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			_la = _input.LA(1);
			if ( !(_la==ID || _la==FN) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(94);
			match(T__2);
			setState(97);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(95);
				expr(0);
				}
				break;
			case 2:
				{
				setState(96);
				list();
				}
				break;
			}
			setState(99);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfStatementContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<BodyContext> body() {
			return getRuleContexts(BodyContext.class);
		}
		public BodyContext body(int i) {
			return getRuleContext(BodyContext.class,i);
		}
		public IfStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterIfStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitIfStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitIfStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfStatementContext ifStatement() throws RecognitionException {
		IfStatementContext _localctx = new IfStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_ifStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			match(T__4);
			setState(102);
			match(T__5);
			setState(103);
			expr(0);
			setState(104);
			match(T__6);
			setState(105);
			body();
			setState(108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(106);
				match(T__7);
				setState(107);
				body();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForStatementContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public RangeExprContext rangeExpr() {
			return getRuleContext(RangeExprContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ForStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterForStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitForStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitForStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForStatementContext forStatement() throws RecognitionException {
		ForStatementContext _localctx = new ForStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_forStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(T__8);
			setState(111);
			match(T__5);
			setState(112);
			match(ID);
			setState(113);
			match(T__2);
			setState(114);
			rangeExpr();
			setState(115);
			match(T__6);
			setState(116);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeExprContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public RangeExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterRangeExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitRangeExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitRangeExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeExprContext rangeExpr() throws RecognitionException {
		RangeExprContext _localctx = new RangeExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_rangeExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			match(T__9);
			setState(119);
			expr(0);
			setState(120);
			match(T__10);
			setState(121);
			expr(0);
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(122);
				match(T__10);
				setState(123);
				expr(0);
				}
			}

			setState(126);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EchoCallContext extends ParserRuleContext {
		public List<StrExprContext> strExpr() {
			return getRuleContexts(StrExprContext.class);
		}
		public StrExprContext strExpr(int i) {
			return getRuleContext(StrExprContext.class,i);
		}
		public EchoCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_echoCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterEchoCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitEchoCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitEchoCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EchoCallContext echoCall() throws RecognitionException {
		EchoCallContext _localctx = new EchoCallContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_echoCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			match(T__12);
			setState(129);
			match(T__5);
			setState(130);
			strExpr();
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(131);
				match(T__13);
				setState(132);
				strExpr();
				}
				}
				setState(137);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(138);
			match(T__6);
			setState(139);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StrExprContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(Scad2DParser.STRING, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public StrExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterStrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitStrExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitStrExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrExprContext strExpr() throws RecognitionException {
		StrExprContext _localctx = new StrExprContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_strExpr);
		try {
			setState(143);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(141);
				match(STRING);
				}
				break;
			case T__5:
			case T__9:
			case T__35:
			case T__36:
			case T__48:
			case T__49:
			case T__50:
			case T__51:
			case T__52:
			case T__53:
			case T__54:
			case T__55:
			case T__56:
			case T__57:
			case ID:
			case NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				expr(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColorCallContext extends ParserRuleContext {
		public List<StrExprContext> strExpr() {
			return getRuleContexts(StrExprContext.class);
		}
		public StrExprContext strExpr(int i) {
			return getRuleContext(StrExprContext.class,i);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ColorCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colorCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterColorCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitColorCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitColorCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColorCallContext colorCall() throws RecognitionException {
		ColorCallContext _localctx = new ColorCallContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_colorCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(T__14);
			setState(146);
			match(T__5);
			setState(147);
			strExpr();
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(148);
				match(T__13);
				setState(149);
				strExpr();
				}
				}
				setState(154);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(155);
			match(T__6);
			setState(156);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TranslateExprContext extends ParserRuleContext {
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public TranslateExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_translateExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterTranslateExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitTranslateExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitTranslateExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TranslateExprContext translateExpr() throws RecognitionException {
		TranslateExprContext _localctx = new TranslateExprContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_translateExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			_la = _input.LA(1);
			if ( !(_la==T__15 || _la==T__16) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(159);
			match(T__5);
			setState(160);
			exprList();
			setState(161);
			match(T__6);
			setState(162);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScaleExprContext extends ParserRuleContext {
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ScaleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scaleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterScaleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitScaleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitScaleExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScaleExprContext scaleExpr() throws RecognitionException {
		ScaleExprContext _localctx = new ScaleExprContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_scaleExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			match(T__17);
			setState(165);
			match(T__5);
			setState(166);
			exprList();
			setState(167);
			match(T__6);
			setState(168);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RotateExprContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public RotateExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rotateExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterRotateExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitRotateExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitRotateExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RotateExprContext rotateExpr() throws RecognitionException {
		RotateExprContext _localctx = new RotateExprContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_rotateExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			match(T__18);
			setState(171);
			match(T__5);
			setState(172);
			expr(0);
			setState(173);
			match(T__6);
			setState(174);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnionCallContext extends ParserRuleContext {
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public UnionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterUnionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitUnionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitUnionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionCallContext unionCall() throws RecognitionException {
		UnionCallContext _localctx = new UnionCallContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_unionCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			match(T__19);
			setState(177);
			match(T__5);
			setState(178);
			match(T__6);
			setState(179);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DifferenceCallContext extends ParserRuleContext {
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public DifferenceCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_differenceCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterDifferenceCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitDifferenceCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitDifferenceCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DifferenceCallContext differenceCall() throws RecognitionException {
		DifferenceCallContext _localctx = new DifferenceCallContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_differenceCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			_la = _input.LA(1);
			if ( !(_la==T__20 || _la==T__21) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(182);
			match(T__5);
			setState(183);
			match(T__6);
			setState(184);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntersectionCallContext extends ParserRuleContext {
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public IntersectionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intersectionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterIntersectionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitIntersectionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitIntersectionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntersectionCallContext intersectionCall() throws RecognitionException {
		IntersectionCallContext _localctx = new IntersectionCallContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_intersectionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			_la = _input.LA(1);
			if ( !(_la==T__22 || _la==T__23) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(187);
			match(T__5);
			setState(188);
			match(T__6);
			setState(189);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HullCallContext extends ParserRuleContext {
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public HullCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hullCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterHullCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitHullCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitHullCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HullCallContext hullCall() throws RecognitionException {
		HullCallContext _localctx = new HullCallContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_hullCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			match(T__24);
			setState(192);
			match(T__5);
			setState(193);
			match(T__6);
			setState(194);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MirrorCallContext extends ParserRuleContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public MirrorCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mirrorCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterMirrorCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitMirrorCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitMirrorCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MirrorCallContext mirrorCall() throws RecognitionException {
		MirrorCallContext _localctx = new MirrorCallContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_mirrorCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			match(T__25);
			setState(197);
			match(T__5);
			setState(198);
			list();
			setState(199);
			match(T__6);
			setState(200);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModuleDefContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public ModuleDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterModuleDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitModuleDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitModuleDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModuleDefContext moduleDef() throws RecognitionException {
		ModuleDefContext _localctx = new ModuleDefContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_moduleDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(202);
			match(T__26);
			setState(203);
			match(ID);
			setState(204);
			match(T__5);
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(205);
				paramList();
				}
			}

			setState(208);
			match(T__6);
			setState(209);
			body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModuleCallContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ModuleCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterModuleCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitModuleCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitModuleCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModuleCallContext moduleCall() throws RecognitionException {
		ModuleCallContext _localctx = new ModuleCallContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_moduleCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(ID);
			setState(212);
			match(T__5);
			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 6)) & ~0x3f) == 0 && ((1L << (_la - 6)) & 441343970610511889L) != 0)) {
				{
				setState(213);
				expr(0);
				setState(218);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__27) {
					{
					{
					setState(214);
					match(T__27);
					setState(215);
					expr(0);
					}
					}
					setState(220);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(223);
			match(T__6);
			setState(224);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamListContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(Scad2DParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(Scad2DParser.ID, i);
		}
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitParamList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitParamList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParamListContext paramList() throws RecognitionException {
		ParamListContext _localctx = new ParamListContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_paramList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			match(ID);
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__27) {
				{
				{
				setState(227);
				match(T__27);
				setState(228);
				match(ID);
				}
				}
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CircleExprContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public TerminalNode FN() { return getToken(Scad2DParser.FN, 0); }
		public CircleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_circleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterCircleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitCircleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitCircleExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CircleExprContext circleExpr() throws RecognitionException {
		CircleExprContext _localctx = new CircleExprContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_circleExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(T__28);
			setState(235);
			match(T__5);
			setState(238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(236);
				match(ID);
				setState(237);
				match(T__2);
				}
				break;
			}
			setState(240);
			expr(0);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__27) {
				{
				setState(241);
				match(T__27);
				setState(242);
				match(FN);
				setState(243);
				match(T__2);
				setState(244);
				expr(0);
				}
			}

			setState(247);
			match(T__6);
			setState(248);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RectangleExprContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(Scad2DParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(Scad2DParser.ID, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public RectangleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rectangleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterRectangleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitRectangleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitRectangleExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RectangleExprContext rectangleExpr() throws RecognitionException {
		RectangleExprContext _localctx = new RectangleExprContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_rectangleExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 16106127360L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(251);
			match(T__5);
			setState(261);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(252);
				match(ID);
				setState(253);
				match(T__2);
				setState(254);
				expr(0);
				setState(255);
				match(T__27);
				setState(256);
				match(ID);
				setState(257);
				match(T__2);
				setState(258);
				expr(0);
				}
				break;
			case 2:
				{
				setState(260);
				expr(0);
				}
				break;
			}
			setState(263);
			match(T__6);
			setState(264);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PolygonExprContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public PolygonExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_polygonExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterPolygonExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitPolygonExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitPolygonExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PolygonExprContext polygonExpr() throws RecognitionException {
		PolygonExprContext _localctx = new PolygonExprContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_polygonExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			_la = _input.LA(1);
			if ( !(_la==T__33 || _la==T__34) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(267);
			match(T__5);
			setState(270);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				{
				setState(268);
				match(ID);
				}
				break;
			case T__9:
				{
				setState(269);
				list();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(272);
			match(T__6);
			setState(273);
			match(T__3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ListContext extends ParserRuleContext {
		public List<ExprListContext> exprList() {
			return getRuleContexts(ExprListContext.class);
		}
		public ExprListContext exprList(int i) {
			return getRuleContext(ExprListContext.class,i);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			match(T__9);
			setState(276);
			exprList();
			setState(281);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__27) {
				{
				{
				setState(277);
				match(T__27);
				setState(278);
				exprList();
				}
				}
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(284);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprListContext extends ParserRuleContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterExprList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitExprList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprListContext exprList() throws RecognitionException {
		ExprListContext _localctx = new ExprListContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_exprList);
		try {
			setState(288);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(286);
				list();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(287);
				expr(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode NUMBER() { return getToken(Scad2DParser.NUMBER, 0); }
		public TerminalNode ID() { return getToken(Scad2DParser.ID, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof Scad2DListener ) ((Scad2DListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof Scad2DVisitor ) return ((Scad2DVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 52;
		enterRecursionRule(_localctx, 52, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(331);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(291);
				((ExprContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__35 || _la==T__36) ) {
					((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(292);
				expr(15);
				}
				break;
			case 2:
				{
				setState(293);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3940649673949184L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 3:
				{
				setState(294);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 139611588448485376L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(295);
				match(T__5);
				setState(296);
				expr(0);
				setState(297);
				match(T__6);
				}
				break;
			case 4:
				{
				setState(299);
				_la = _input.LA(1);
				if ( !(_la==T__56 || _la==T__57) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(300);
				match(T__5);
				setState(301);
				expr(0);
				setState(306);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__27) {
					{
					{
					setState(302);
					match(T__27);
					setState(303);
					expr(0);
					}
					}
					setState(308);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(309);
				match(T__6);
				}
				break;
			case 5:
				{
				setState(311);
				match(NUMBER);
				}
				break;
			case 6:
				{
				setState(312);
				match(ID);
				setState(313);
				match(T__9);
				setState(314);
				expr(0);
				setState(315);
				match(T__11);
				setState(322);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(316);
						match(T__9);
						setState(317);
						expr(0);
						setState(318);
						match(T__11);
						}
						} 
					}
					setState(324);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				}
				}
				break;
			case 7:
				{
				setState(325);
				match(ID);
				}
				break;
			case 8:
				{
				setState(326);
				match(T__5);
				setState(327);
				expr(0);
				setState(328);
				match(T__6);
				}
				break;
			case 9:
				{
				setState(330);
				list();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(356);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(354);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(333);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(334);
						((ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1924145348608L) != 0)) ) {
							((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(335);
						expr(15);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(336);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(337);
						((ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__13 || _la==T__35) ) {
							((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(338);
						expr(14);
						}
						break;
					case 3:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(339);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(340);
						((ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 32985348833280L) != 0)) ) {
							((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(341);
						expr(13);
						}
						break;
					case 4:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(342);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(343);
						((ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__44 || _la==T__45) ) {
							((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(344);
						expr(12);
						}
						break;
					case 5:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(345);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(346);
						((ExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__46 || _la==T__47) ) {
							((ExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(347);
						expr(11);
						}
						break;
					case 6:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(348);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(349);
						match(T__58);
						setState(350);
						expr(0);
						setState(351);
						match(T__10);
						setState(352);
						expr(2);
						}
						break;
					}
					} 
				}
				setState(358);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 26:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 14);
		case 1:
			return precpred(_ctx, 13);
		case 2:
			return precpred(_ctx, 12);
		case 3:
			return precpred(_ctx, 11);
		case 4:
			return precpred(_ctx, 10);
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001B\u0168\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0001\u0000\u0005\u0000"+
		"8\b\u0000\n\u0000\f\u0000;\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0005\u0001B\b\u0001\n\u0001\f\u0001E\t\u0001"+
		"\u0001\u0001\u0003\u0001H\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\\\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003b\b\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004m\b\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006}\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0086\b\u0007\n\u0007"+
		"\f\u0007\u0089\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0003\b\u0090\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u0097"+
		"\b\t\n\t\f\t\u009a\t\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n"+
		"\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00cf"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00d9\b\u0013\n\u0013\f\u0013"+
		"\u00dc\t\u0013\u0003\u0013\u00de\b\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u00e6\b\u0014\n"+
		"\u0014\f\u0014\u00e9\t\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u00ef\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0003\u0015\u00f6\b\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003"+
		"\u0016\u0106\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u010f\b\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005"+
		"\u0018\u0118\b\u0018\n\u0018\f\u0018\u011b\t\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0003\u0019\u0121\b\u0019\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0005\u001a\u0131\b\u001a\n\u001a\f\u001a\u0134\t\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u0141\b\u001a\n"+
		"\u001a\f\u001a\u0144\t\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u014c\b\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0005\u001a\u0163\b\u001a\n\u001a\f\u001a\u0166\t\u001a"+
		"\u0001\u001a\u0000\u00014\u001b\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.024\u0000\u000f\u0002"+
		"\u0000??BB\u0001\u0000\u0010\u0011\u0001\u0000\u0015\u0016\u0001\u0000"+
		"\u0017\u0018\u0001\u0000\u001e!\u0001\u0000\"#\u0001\u0000$%\u0001\u0000"+
		"13\u0001\u000048\u0001\u00009:\u0001\u0000&(\u0002\u0000\u000e\u000e$"+
		"$\u0001\u0000),\u0001\u0000-.\u0001\u0000/0\u0180\u00009\u0001\u0000\u0000"+
		"\u0000\u0002G\u0001\u0000\u0000\u0000\u0004[\u0001\u0000\u0000\u0000\u0006"+
		"]\u0001\u0000\u0000\u0000\be\u0001\u0000\u0000\u0000\nn\u0001\u0000\u0000"+
		"\u0000\fv\u0001\u0000\u0000\u0000\u000e\u0080\u0001\u0000\u0000\u0000"+
		"\u0010\u008f\u0001\u0000\u0000\u0000\u0012\u0091\u0001\u0000\u0000\u0000"+
		"\u0014\u009e\u0001\u0000\u0000\u0000\u0016\u00a4\u0001\u0000\u0000\u0000"+
		"\u0018\u00aa\u0001\u0000\u0000\u0000\u001a\u00b0\u0001\u0000\u0000\u0000"+
		"\u001c\u00b5\u0001\u0000\u0000\u0000\u001e\u00ba\u0001\u0000\u0000\u0000"+
		" \u00bf\u0001\u0000\u0000\u0000\"\u00c4\u0001\u0000\u0000\u0000$\u00ca"+
		"\u0001\u0000\u0000\u0000&\u00d3\u0001\u0000\u0000\u0000(\u00e2\u0001\u0000"+
		"\u0000\u0000*\u00ea\u0001\u0000\u0000\u0000,\u00fa\u0001\u0000\u0000\u0000"+
		".\u010a\u0001\u0000\u0000\u00000\u0113\u0001\u0000\u0000\u00002\u0120"+
		"\u0001\u0000\u0000\u00004\u014b\u0001\u0000\u0000\u000068\u0003\u0004"+
		"\u0002\u000076\u0001\u0000\u0000\u00008;\u0001\u0000\u0000\u000097\u0001"+
		"\u0000\u0000\u00009:\u0001\u0000\u0000\u0000:<\u0001\u0000\u0000\u0000"+
		";9\u0001\u0000\u0000\u0000<=\u0005\u0000\u0000\u0001=\u0001\u0001\u0000"+
		"\u0000\u0000>H\u0003\u0004\u0002\u0000?C\u0005\u0001\u0000\u0000@B\u0003"+
		"\u0004\u0002\u0000A@\u0001\u0000\u0000\u0000BE\u0001\u0000\u0000\u0000"+
		"CA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DF\u0001\u0000\u0000"+
		"\u0000EC\u0001\u0000\u0000\u0000FH\u0005\u0002\u0000\u0000G>\u0001\u0000"+
		"\u0000\u0000G?\u0001\u0000\u0000\u0000H\u0003\u0001\u0000\u0000\u0000"+
		"I\\\u0003\u0006\u0003\u0000J\\\u0003\b\u0004\u0000K\\\u0003\n\u0005\u0000"+
		"L\\\u0003$\u0012\u0000M\\\u0003*\u0015\u0000N\\\u0003,\u0016\u0000O\\"+
		"\u0003.\u0017\u0000P\\\u0003\u0014\n\u0000Q\\\u0003\u0016\u000b\u0000"+
		"R\\\u0003\u0018\f\u0000S\\\u0003\u001a\r\u0000T\\\u0003\u001c\u000e\u0000"+
		"U\\\u0003\u001e\u000f\u0000V\\\u0003\"\u0011\u0000W\\\u0003 \u0010\u0000"+
		"X\\\u0003\u0012\t\u0000Y\\\u0003\u000e\u0007\u0000Z\\\u0003&\u0013\u0000"+
		"[I\u0001\u0000\u0000\u0000[J\u0001\u0000\u0000\u0000[K\u0001\u0000\u0000"+
		"\u0000[L\u0001\u0000\u0000\u0000[M\u0001\u0000\u0000\u0000[N\u0001\u0000"+
		"\u0000\u0000[O\u0001\u0000\u0000\u0000[P\u0001\u0000\u0000\u0000[Q\u0001"+
		"\u0000\u0000\u0000[R\u0001\u0000\u0000\u0000[S\u0001\u0000\u0000\u0000"+
		"[T\u0001\u0000\u0000\u0000[U\u0001\u0000\u0000\u0000[V\u0001\u0000\u0000"+
		"\u0000[W\u0001\u0000\u0000\u0000[X\u0001\u0000\u0000\u0000[Y\u0001\u0000"+
		"\u0000\u0000[Z\u0001\u0000\u0000\u0000\\\u0005\u0001\u0000\u0000\u0000"+
		"]^\u0007\u0000\u0000\u0000^a\u0005\u0003\u0000\u0000_b\u00034\u001a\u0000"+
		"`b\u00030\u0018\u0000a_\u0001\u0000\u0000\u0000a`\u0001\u0000\u0000\u0000"+
		"bc\u0001\u0000\u0000\u0000cd\u0005\u0004\u0000\u0000d\u0007\u0001\u0000"+
		"\u0000\u0000ef\u0005\u0005\u0000\u0000fg\u0005\u0006\u0000\u0000gh\u0003"+
		"4\u001a\u0000hi\u0005\u0007\u0000\u0000il\u0003\u0002\u0001\u0000jk\u0005"+
		"\b\u0000\u0000km\u0003\u0002\u0001\u0000lj\u0001\u0000\u0000\u0000lm\u0001"+
		"\u0000\u0000\u0000m\t\u0001\u0000\u0000\u0000no\u0005\t\u0000\u0000op"+
		"\u0005\u0006\u0000\u0000pq\u0005?\u0000\u0000qr\u0005\u0003\u0000\u0000"+
		"rs\u0003\f\u0006\u0000st\u0005\u0007\u0000\u0000tu\u0003\u0002\u0001\u0000"+
		"u\u000b\u0001\u0000\u0000\u0000vw\u0005\n\u0000\u0000wx\u00034\u001a\u0000"+
		"xy\u0005\u000b\u0000\u0000y|\u00034\u001a\u0000z{\u0005\u000b\u0000\u0000"+
		"{}\u00034\u001a\u0000|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000"+
		"}~\u0001\u0000\u0000\u0000~\u007f\u0005\f\u0000\u0000\u007f\r\u0001\u0000"+
		"\u0000\u0000\u0080\u0081\u0005\r\u0000\u0000\u0081\u0082\u0005\u0006\u0000"+
		"\u0000\u0082\u0087\u0003\u0010\b\u0000\u0083\u0084\u0005\u000e\u0000\u0000"+
		"\u0084\u0086\u0003\u0010\b\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0086"+
		"\u0089\u0001\u0000\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0087"+
		"\u0088\u0001\u0000\u0000\u0000\u0088\u008a\u0001\u0000\u0000\u0000\u0089"+
		"\u0087\u0001\u0000\u0000\u0000\u008a\u008b\u0005\u0007\u0000\u0000\u008b"+
		"\u008c\u0005\u0004\u0000\u0000\u008c\u000f\u0001\u0000\u0000\u0000\u008d"+
		"\u0090\u0005A\u0000\u0000\u008e\u0090\u00034\u001a\u0000\u008f\u008d\u0001"+
		"\u0000\u0000\u0000\u008f\u008e\u0001\u0000\u0000\u0000\u0090\u0011\u0001"+
		"\u0000\u0000\u0000\u0091\u0092\u0005\u000f\u0000\u0000\u0092\u0093\u0005"+
		"\u0006\u0000\u0000\u0093\u0098\u0003\u0010\b\u0000\u0094\u0095\u0005\u000e"+
		"\u0000\u0000\u0095\u0097\u0003\u0010\b\u0000\u0096\u0094\u0001\u0000\u0000"+
		"\u0000\u0097\u009a\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000"+
		"\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u009b\u0001\u0000\u0000"+
		"\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009b\u009c\u0005\u0007\u0000"+
		"\u0000\u009c\u009d\u0003\u0002\u0001\u0000\u009d\u0013\u0001\u0000\u0000"+
		"\u0000\u009e\u009f\u0007\u0001\u0000\u0000\u009f\u00a0\u0005\u0006\u0000"+
		"\u0000\u00a0\u00a1\u00032\u0019\u0000\u00a1\u00a2\u0005\u0007\u0000\u0000"+
		"\u00a2\u00a3\u0003\u0002\u0001\u0000\u00a3\u0015\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a5\u0005\u0012\u0000\u0000\u00a5\u00a6\u0005\u0006\u0000\u0000"+
		"\u00a6\u00a7\u00032\u0019\u0000\u00a7\u00a8\u0005\u0007\u0000\u0000\u00a8"+
		"\u00a9\u0003\u0002\u0001\u0000\u00a9\u0017\u0001\u0000\u0000\u0000\u00aa"+
		"\u00ab\u0005\u0013\u0000\u0000\u00ab\u00ac\u0005\u0006\u0000\u0000\u00ac"+
		"\u00ad\u00034\u001a\u0000\u00ad\u00ae\u0005\u0007\u0000\u0000\u00ae\u00af"+
		"\u0003\u0002\u0001\u0000\u00af\u0019\u0001\u0000\u0000\u0000\u00b0\u00b1"+
		"\u0005\u0014\u0000\u0000\u00b1\u00b2\u0005\u0006\u0000\u0000\u00b2\u00b3"+
		"\u0005\u0007\u0000\u0000\u00b3\u00b4\u0003\u0002\u0001\u0000\u00b4\u001b"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b6\u0007\u0002\u0000\u0000\u00b6\u00b7"+
		"\u0005\u0006\u0000\u0000\u00b7\u00b8\u0005\u0007\u0000\u0000\u00b8\u00b9"+
		"\u0003\u0002\u0001\u0000\u00b9\u001d\u0001\u0000\u0000\u0000\u00ba\u00bb"+
		"\u0007\u0003\u0000\u0000\u00bb\u00bc\u0005\u0006\u0000\u0000\u00bc\u00bd"+
		"\u0005\u0007\u0000\u0000\u00bd\u00be\u0003\u0002\u0001\u0000\u00be\u001f"+
		"\u0001\u0000\u0000\u0000\u00bf\u00c0\u0005\u0019\u0000\u0000\u00c0\u00c1"+
		"\u0005\u0006\u0000\u0000\u00c1\u00c2\u0005\u0007\u0000\u0000\u00c2\u00c3"+
		"\u0003\u0002\u0001\u0000\u00c3!\u0001\u0000\u0000\u0000\u00c4\u00c5\u0005"+
		"\u001a\u0000\u0000\u00c5\u00c6\u0005\u0006\u0000\u0000\u00c6\u00c7\u0003"+
		"0\u0018\u0000\u00c7\u00c8\u0005\u0007\u0000\u0000\u00c8\u00c9\u0003\u0002"+
		"\u0001\u0000\u00c9#\u0001\u0000\u0000\u0000\u00ca\u00cb\u0005\u001b\u0000"+
		"\u0000\u00cb\u00cc\u0005?\u0000\u0000\u00cc\u00ce\u0005\u0006\u0000\u0000"+
		"\u00cd\u00cf\u0003(\u0014\u0000\u00ce\u00cd\u0001\u0000\u0000\u0000\u00ce"+
		"\u00cf\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0"+
		"\u00d1\u0005\u0007\u0000\u0000\u00d1\u00d2\u0003\u0002\u0001\u0000\u00d2"+
		"%\u0001\u0000\u0000\u0000\u00d3\u00d4\u0005?\u0000\u0000\u00d4\u00dd\u0005"+
		"\u0006\u0000\u0000\u00d5\u00da\u00034\u001a\u0000\u00d6\u00d7\u0005\u001c"+
		"\u0000\u0000\u00d7\u00d9\u00034\u001a\u0000\u00d8\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d9\u00dc\u0001\u0000\u0000\u0000\u00da\u00d8\u0001\u0000\u0000"+
		"\u0000\u00da\u00db\u0001\u0000\u0000\u0000\u00db\u00de\u0001\u0000\u0000"+
		"\u0000\u00dc\u00da\u0001\u0000\u0000\u0000\u00dd\u00d5\u0001\u0000\u0000"+
		"\u0000\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00df\u0001\u0000\u0000"+
		"\u0000\u00df\u00e0\u0005\u0007\u0000\u0000\u00e0\u00e1\u0005\u0004\u0000"+
		"\u0000\u00e1\'\u0001\u0000\u0000\u0000\u00e2\u00e7\u0005?\u0000\u0000"+
		"\u00e3\u00e4\u0005\u001c\u0000\u0000\u00e4\u00e6\u0005?\u0000\u0000\u00e5"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e6\u00e9\u0001\u0000\u0000\u0000\u00e7"+
		"\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8"+
		")\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00ea\u00eb"+
		"\u0005\u001d\u0000\u0000\u00eb\u00ee\u0005\u0006\u0000\u0000\u00ec\u00ed"+
		"\u0005?\u0000\u0000\u00ed\u00ef\u0005\u0003\u0000\u0000\u00ee\u00ec\u0001"+
		"\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001"+
		"\u0000\u0000\u0000\u00f0\u00f5\u00034\u001a\u0000\u00f1\u00f2\u0005\u001c"+
		"\u0000\u0000\u00f2\u00f3\u0005B\u0000\u0000\u00f3\u00f4\u0005\u0003\u0000"+
		"\u0000\u00f4\u00f6\u00034\u001a\u0000\u00f5\u00f1\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000"+
		"\u00f7\u00f8\u0005\u0007\u0000\u0000\u00f8\u00f9\u0005\u0004\u0000\u0000"+
		"\u00f9+\u0001\u0000\u0000\u0000\u00fa\u00fb\u0007\u0004\u0000\u0000\u00fb"+
		"\u0105\u0005\u0006\u0000\u0000\u00fc\u00fd\u0005?\u0000\u0000\u00fd\u00fe"+
		"\u0005\u0003\u0000\u0000\u00fe\u00ff\u00034\u001a\u0000\u00ff\u0100\u0005"+
		"\u001c\u0000\u0000\u0100\u0101\u0005?\u0000\u0000\u0101\u0102\u0005\u0003"+
		"\u0000\u0000\u0102\u0103\u00034\u001a\u0000\u0103\u0106\u0001\u0000\u0000"+
		"\u0000\u0104\u0106\u00034\u001a\u0000\u0105\u00fc\u0001\u0000\u0000\u0000"+
		"\u0105\u0104\u0001\u0000\u0000\u0000\u0106\u0107\u0001\u0000\u0000\u0000"+
		"\u0107\u0108\u0005\u0007\u0000\u0000\u0108\u0109\u0005\u0004\u0000\u0000"+
		"\u0109-\u0001\u0000\u0000\u0000\u010a\u010b\u0007\u0005\u0000\u0000\u010b"+
		"\u010e\u0005\u0006\u0000\u0000\u010c\u010f\u0005?\u0000\u0000\u010d\u010f"+
		"\u00030\u0018\u0000\u010e\u010c\u0001\u0000\u0000\u0000\u010e\u010d\u0001"+
		"\u0000\u0000\u0000\u010f\u0110\u0001\u0000\u0000\u0000\u0110\u0111\u0005"+
		"\u0007\u0000\u0000\u0111\u0112\u0005\u0004\u0000\u0000\u0112/\u0001\u0000"+
		"\u0000\u0000\u0113\u0114\u0005\n\u0000\u0000\u0114\u0119\u00032\u0019"+
		"\u0000\u0115\u0116\u0005\u001c\u0000\u0000\u0116\u0118\u00032\u0019\u0000"+
		"\u0117\u0115\u0001\u0000\u0000\u0000\u0118\u011b\u0001\u0000\u0000\u0000"+
		"\u0119\u0117\u0001\u0000\u0000\u0000\u0119\u011a\u0001\u0000\u0000\u0000"+
		"\u011a\u011c\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000"+
		"\u011c\u011d\u0005\f\u0000\u0000\u011d1\u0001\u0000\u0000\u0000\u011e"+
		"\u0121\u00030\u0018\u0000\u011f\u0121\u00034\u001a\u0000\u0120\u011e\u0001"+
		"\u0000\u0000\u0000\u0120\u011f\u0001\u0000\u0000\u0000\u01213\u0001\u0000"+
		"\u0000\u0000\u0122\u0123\u0006\u001a\uffff\uffff\u0000\u0123\u0124\u0007"+
		"\u0006\u0000\u0000\u0124\u014c\u00034\u001a\u000f\u0125\u014c\u0007\u0007"+
		"\u0000\u0000\u0126\u0127\u0007\b\u0000\u0000\u0127\u0128\u0005\u0006\u0000"+
		"\u0000\u0128\u0129\u00034\u001a\u0000\u0129\u012a\u0005\u0007\u0000\u0000"+
		"\u012a\u014c\u0001\u0000\u0000\u0000\u012b\u012c\u0007\t\u0000\u0000\u012c"+
		"\u012d\u0005\u0006\u0000\u0000\u012d\u0132\u00034\u001a\u0000\u012e\u012f"+
		"\u0005\u001c\u0000\u0000\u012f\u0131\u00034\u001a\u0000\u0130\u012e\u0001"+
		"\u0000\u0000\u0000\u0131\u0134\u0001\u0000\u0000\u0000\u0132\u0130\u0001"+
		"\u0000\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u0135\u0001"+
		"\u0000\u0000\u0000\u0134\u0132\u0001\u0000\u0000\u0000\u0135\u0136\u0005"+
		"\u0007\u0000\u0000\u0136\u014c\u0001\u0000\u0000\u0000\u0137\u014c\u0005"+
		"@\u0000\u0000\u0138\u0139\u0005?\u0000\u0000\u0139\u013a\u0005\n\u0000"+
		"\u0000\u013a\u013b\u00034\u001a\u0000\u013b\u0142\u0005\f\u0000\u0000"+
		"\u013c\u013d\u0005\n\u0000\u0000\u013d\u013e\u00034\u001a\u0000\u013e"+
		"\u013f\u0005\f\u0000\u0000\u013f\u0141\u0001\u0000\u0000\u0000\u0140\u013c"+
		"\u0001\u0000\u0000\u0000\u0141\u0144\u0001\u0000\u0000\u0000\u0142\u0140"+
		"\u0001\u0000\u0000\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u0143\u014c"+
		"\u0001\u0000\u0000\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0145\u014c"+
		"\u0005?\u0000\u0000\u0146\u0147\u0005\u0006\u0000\u0000\u0147\u0148\u0003"+
		"4\u001a\u0000\u0148\u0149\u0005\u0007\u0000\u0000\u0149\u014c\u0001\u0000"+
		"\u0000\u0000\u014a\u014c\u00030\u0018\u0000\u014b\u0122\u0001\u0000\u0000"+
		"\u0000\u014b\u0125\u0001\u0000\u0000\u0000\u014b\u0126\u0001\u0000\u0000"+
		"\u0000\u014b\u012b\u0001\u0000\u0000\u0000\u014b\u0137\u0001\u0000\u0000"+
		"\u0000\u014b\u0138\u0001\u0000\u0000\u0000\u014b\u0145\u0001\u0000\u0000"+
		"\u0000\u014b\u0146\u0001\u0000\u0000\u0000\u014b\u014a\u0001\u0000\u0000"+
		"\u0000\u014c\u0164\u0001\u0000\u0000\u0000\u014d\u014e\n\u000e\u0000\u0000"+
		"\u014e\u014f\u0007\n\u0000\u0000\u014f\u0163\u00034\u001a\u000f\u0150"+
		"\u0151\n\r\u0000\u0000\u0151\u0152\u0007\u000b\u0000\u0000\u0152\u0163"+
		"\u00034\u001a\u000e\u0153\u0154\n\f\u0000\u0000\u0154\u0155\u0007\f\u0000"+
		"\u0000\u0155\u0163\u00034\u001a\r\u0156\u0157\n\u000b\u0000\u0000\u0157"+
		"\u0158\u0007\r\u0000\u0000\u0158\u0163\u00034\u001a\f\u0159\u015a\n\n"+
		"\u0000\u0000\u015a\u015b\u0007\u000e\u0000\u0000\u015b\u0163\u00034\u001a"+
		"\u000b\u015c\u015d\n\u0001\u0000\u0000\u015d\u015e\u0005;\u0000\u0000"+
		"\u015e\u015f\u00034\u001a\u0000\u015f\u0160\u0005\u000b\u0000\u0000\u0160"+
		"\u0161\u00034\u001a\u0002\u0161\u0163\u0001\u0000\u0000\u0000\u0162\u014d"+
		"\u0001\u0000\u0000\u0000\u0162\u0150\u0001\u0000\u0000\u0000\u0162\u0153"+
		"\u0001\u0000\u0000\u0000\u0162\u0156\u0001\u0000\u0000\u0000\u0162\u0159"+
		"\u0001\u0000\u0000\u0000\u0162\u015c\u0001\u0000\u0000\u0000\u0163\u0166"+
		"\u0001\u0000\u0000\u0000\u0164\u0162\u0001\u0000\u0000\u0000\u0164\u0165"+
		"\u0001\u0000\u0000\u0000\u01655\u0001\u0000\u0000\u0000\u0166\u0164\u0001"+
		"\u0000\u0000\u0000\u00199CG[al|\u0087\u008f\u0098\u00ce\u00da\u00dd\u00e7"+
		"\u00ee\u00f5\u0105\u010e\u0119\u0120\u0132\u0142\u014b\u0162\u0164";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}