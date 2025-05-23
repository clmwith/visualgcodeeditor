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
 * You should have receivedLine a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gcodeeditor.gui;

import gcodeeditor.BackgroundPictureParameters;
import gcodeeditor.Configuration;
import gcodeeditor.GCode;
import gcodeeditor.GWord;
import gcodeeditor.JarvisMarchHull;
import gcodeeditor.Point3D;
import gelements.UndoManager;
import gcodeeditor.EngravingProperties;
import gelements.GArc;
import gelements.G1Path;
import gelements.GMixedPath;
import gelements.GDrillPoint;
import gelements.GElement;
import gelements.GGroup;
import gelements.GPocket3D;
import gelements.GSpline;
import gelements.GTextOnPath;
import gcodeeditor.PaintContext;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.SplinePoint;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

/**
 * A panel the permit to display a GCodeProject, and modify it with mouse and keyboard.
 * 
 * @author Clément
 */
public final class JProjectEditorPanel extends javax.swing.JPanel implements BackgroundPictureParameters.ParameterChangedListenerInterface, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    public static final String CONTENT_HEADER = "(Content-Type: ";
    public static final String SVGE_HEADER = "(Simple G-Code Visual Editor Project: ";
    public static final String SVGE_RELEASE = "0.8.8";

    Configuration conf = new Configuration();
    JProjectEditorPanelListenerInterface listener;
    
    /** List of all elements of the document. */
    GGroup document;
    GGroup editedGroup;
    
    /** Visual Zoom factor of the view. */
    double zoomFactor=Double.POSITIVE_INFINITY;
    
    /** View translation. */
    int ox=0, oy=0, dx=0, dy=0;
    
    /** Position of the cursor. */
    private Point2D coord2DCursor=new Point2D.Double(0,0), coordSnapPosition, coordMouseOrigin;
    
    /** Position of the mouse when buton pressed. */
    private java.awt.Point screen2DCursor, screenMousePressPosition, screenMousePosition = new java.awt.Point(-10,-10);
    
    /** Position of the mouse when a rectangle selection is performed. */
    private java.awt.Point mouseRectangleP2;
    
    /** Current selected paths. */
    private final ArrayList<GElement> selectedElements;
    
    /** The current edited path. If null we are not in "edit mode" */
    private GElement editedElement;
    /** Nearest point of the mouse when editedElement is not null. */
    private GCode highlitedPoint;    
    /** Current selected Points when editedElement not null. */
    ArrayList<GCode>selectedPoints;  
    /** The line selected in the viewList. */

    /** the header/footer of th document (used in add and remove methods). */
    private GElement gcodeHeader, gcodeFooter;
    
    private boolean shiftDown, ctrlDown, altDown;
    
    /** rendering display options */
    private boolean showWorkspace, showObjectSurface, showMoves, showGrid, snapToGrid, snapToPoints;
    
    private double gridStep = 10;
    
    public static final int MOUSE_MODE_NONE = 0;
    public static final int MOUSE_MODE_MOVE_SEL = 1;
    public static final int MOUSE_MODE_ROTATION = 2;
    public static final int MOUSE_MODE_SCALE = 3; 
    public static final int MOUSE_MODE_SET_2D_CURSOR = 5;
    public static final int MOUSE_MODE_MOVE_GANTRY = 6;
    public static final int MOUSE_MODE_SET_MPOS = 7;
    public static final int MOUSE_MODE_CHOOSE_MOVE_ORIGIN = 8;
    public static final int MOUSE_MODE_SHOW_DISTANCE = 9;
    public static final int MOUSE_MODE_ADD_RECTANGLES = 10;
    public static final int MOUSE_MODE_ADD_CIRCLES = 11;
    public static final int MOUSE_MODE_ADD_LINES = 12;
    public static final int MOUSE_MODE_QUICK_MOVE = 13;
    public static final int MOUSE_MODE_DRAG_VIEW = 14;
    public static final int MOUSE_MODE_SHOW_ANGLE = 15;
    public static final int MOUSE_MODE_ADD_OVAL = 16;
    public static final int MOUSE_MODE_CURSOR_AT_CENTER = 17;
    public static final int MOUSE_MODE_FOCUS = 18;
    public static final int MOUSE_MODE_ADD_CURVE = 19;
    public static final int MOUSE_MODE_NONE_AT_RELEASE = 20;
    public static final int MOUSE_MODE_ADD_CIRCLE = 21;
    public static final int MOUSE_MODE_ADD_G23_ARC = 23;
    
    private int mouseMode = MOUSE_MODE_NONE;
    private int modeBeforDragViewMode = MOUSE_MODE_NONE;
    
    /** Used to paint G0 moves */
    public static final Color MOVE_COLOR = new Color(120,60,20);
    
    /** The center of the current translate/rotation/scale. */
    private Point2D transformationOrigin;
    /** used in rotation or to calculate angle with mouse */
    double lastRotationAngle, finalRotationAngle=Double.NaN;
    /** used for scaling selection */
    private double scaleNormaleX, scaleNormaleY;
    
    /** for Zoom */
    private double lastScaleRatioX, lastScaleRatioY;

    /** To know if we must save current state into undoStack. */
    private boolean stateHasChanged;
    
    /** To know if we must remplace/update (JList)editor selection. */
    private boolean selectionHasChanged;
    
    /** To know if we must ask for saving the document on close. */
    private boolean documentHasChanged;
    
    /** The stack of all previous states of the document. */
    private final UndoManager undoManager;
    
    /** For copy/cut/past. */
    private static GElement clipBoard;
    
    /** If true, intercept key events. */
    private boolean keyFocus;
    
    /** Component that show the content of the edited GElements. */
    private JList<Object> gCodeListViewer;
    
    /** The frame used to chose filter composition. */
    private JFilterFrame filterFrame;
    private boolean selectionContainsArc;
    private boolean mousePressed, mouseDragged;
    private boolean snapWithoutSelection;

    private BackgroundPictureParameters backgroundPictureParameter;
    private Point3D grblMPos;
    private final Cursor jogCursor;
    private boolean ignoreClick;
    
    /** Listen for changes at edition of the gcode of the editedElement */
    private final ListDataListener editedElementListener;
   
    /** Class to show all paths in the list when no paths edited. */
    public class DocumentListModel implements ListModel<Object> { 
        @Override
        public int getSize() { return (editedGroup==null) ? 0 : editedGroup.getSize(); }
        @Override
        public Object getElementAt(int index) { return editedGroup.get(index); }
        @Override
        public void addListDataListener(ListDataListener l) { }
        @Override
        public void removeListDataListener(ListDataListener l) { }
    };
    final DocumentListModel documentListModel = new DocumentListModel();

    private boolean showHead, showLaser;
    private boolean showStartPoints;
    private final Cursor crossCursor;
    
    /** Used to change properties of several element selected together */
    EngravingProperties selectionProperties;
            
    /**
     * Creates new form NewJPanel
     * @param newEmptyDoc create an empty document in this window
     */
    public JProjectEditorPanel( boolean newEmptyDoc) {
        
        selectedElements = new ArrayList<>(10);
        selectedPoints = new ArrayList<>(100);
        undoManager = new UndoManager();
        
        editedElementListener = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) { if ( mousePressed) stateHasChanged=true; else saveState(false); }
            @Override
            public void intervalRemoved(ListDataEvent e) { if ( mousePressed) stateHasChanged=true; else saveState(false); }
            @Override
            public void contentsChanged(ListDataEvent e) { 
                if ( mousePressed) stateHasChanged=true; 
                else saveState(false);
            }
        };
        
        initComponents();
        setFocusable( true );
        setKeyFocus(true);
        setPreferredSize(new Dimension(1024,768));     
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        String vs[] = conf.editorSettings.split(",");
        if ( vs.length == 8) {
            snapToGrid = vs[0].equals("true");
            snapToPoints = vs[1].equals("true");
            showGrid = vs[2].equals("true");
            showMoves = vs[3].equals("true");
            showHead = vs[4].equals("true");
            showObjectSurface = vs[5].equals("true");
            showStartPoints = vs[6].equals("true");
            showWorkspace = vs[7].equals("true");
            applyConfiguration();
        }
           
        crossCursor = createCrossCursor(null);
        jogCursor = createCrossCursor("JOG");
        if ( newEmptyDoc) setContent(new GGroup("Document"), true); 
    }
    
    private void focusToRect(Rectangle2D r) {
        r.setRect( r.getX()-10, r.getY()-10, r.getWidth()+10, r.getHeight()+10);
        double zx = ((double)(getWidth()-MARGING*4))/r.getWidth();
        double zy = ((double)(getHeight()-MARGING*4))/r.getHeight();
        zoomFactor = Math.min(zx, zy);
        if ( zoomFactor < 0.01) zoomFactor = 0.01;
        if ( zoomFactor > 200000) zoomFactor = 200000;
        dx = -(int)(r.getX()*zoomFactor)+ (getWidth() - (int)(r.getWidth() * zoomFactor)) /2;
        dy = (int)((r.getY()+r.getHeight())*zoomFactor) + (getHeight() - (int)( r.getHeight()* zoomFactor))/2;
        if ( zoomFactor > 1) inform( String.format("Zoom %.1f : 1",zoomFactor));
        else inform( String.format("Zoom 1 : %.1f",(1/zoomFactor)));
        invalidateWithoutUpdateGCodeListViewer();
    }

    /** Completely clear the undoRecords. */
    public void clearUndoRecords() {
        undoManager.clear();
    }

    public double getSelectionSurfaceValue() {
        if ( ! selectedElements.isEmpty() && (selectedElements.get(0) instanceof G1Path)) {
            return ((G1Path)(selectedElements.get(0))).getSurfaceValue();
        }
        return 0;
    } 

    /**
     * @return true if we are editing some GElement.
     */
    public boolean isInEditMode() {
        return editedElement != null;
    }
    
    public BackgroundPictureParameters getBackgroundPictureParameters() {
        if ( backgroundPictureParameter == null) {
            
            backgroundPictureParameter = new BackgroundPictureParameters();
            backgroundPictureParameter.addionChangeListener(this);
        }
        return backgroundPictureParameter;
    }
    
    public void set2DCursorTo(double x, double y) {
        coord2DCursor = new Point2D.Double(x, y);
        repaint();
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setGRBLMachinePosition(Point3D mPos) {
        grblMPos = mPos;
        if ( showHead) repaint();
    }
    
    @Override
    public void backgroundPictureParameterChanged() {
        repaint();
    }
    
    public static final Cursor createCrossCursor(String info) {
        int m = 12, d= 7;
        Image customImage = new BufferedImage(2*m+1,2*m+1, BufferedImage.TYPE_INT_ARGB);
        Graphics g = customImage.getGraphics();
        if ( info != null) {
            int width = g.getFontMetrics().stringWidth(info);
            int h = g.getFontMetrics().getAscent();
            customImage = new BufferedImage(2*m+1+width,2*m+1+h, BufferedImage.TYPE_INT_ARGB);
            g = customImage.getGraphics();
            g.setColor(Color.yellow);
            g.drawString(info, 2* m - d, 2*m + h - d);
        }
        g.setColor(Color.WHITE);
        g.drawLine(0, m, m-d, m);
        g.drawLine(m+d, m, 2*m+1, m);
        g.drawLine(m, 0, m, m-d);
        g.drawLine(m, m+d, m, 2*m+1);
        
        
        return java.awt.Toolkit.getDefaultToolkit().createCustomCursor(customImage, 
                new java.awt.Point(m, m), "crossCursor");
    }
    
    public void setListener( JProjectEditorPanelListenerInterface l)
    {
        listener = l;
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFocusCycleRoot(true);
        setVerifyInputWhenFocusTarget(false);
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    public GCode screenToCoordPoint(int x, int y)
    {
        return new GCode((x - dx)/ zoomFactor, (dy - y) / zoomFactor);
    }
    
    private GCode screenToCoordPoint(GCode point) {
        return new GCode(((point.getX() - dx)/ zoomFactor), ((dy - point.getY()) / zoomFactor));
    }

    private GCode screenToCoordPoint(java.awt.Point point) {
        return new GCode(((point.getX() - dx)/ zoomFactor), ((dy - point.getY()) / zoomFactor));
    }
    
    public Rectangle coordToScreen( Rectangle2D r) {
        return new Rectangle((int)(r.getX()*zoomFactor), -(int)((r.getY()*zoomFactor)+(zoomFactor * r.getHeight())), (int)(zoomFactor * r.getWidth()), (int)(zoomFactor * r.getHeight()));
    }
    
    public java.awt.Point coordToScreenPoint(Point2D p)
    {
        return new java.awt.Point((int)(p.getX() * zoomFactor) + dx, dy - (int)(p.getY() * zoomFactor));
    }
    
    public java.awt.Point coordToScreenPoint(Point p)
    {
        return new java.awt.Point((int)(p.getX() * zoomFactor) + dx, dy - (int)(p.getY() * zoomFactor));
    }
    
    public java.awt.Point coordToScreenPoint(double x, double y)
    {
        return new java.awt.Point((int)(x * zoomFactor) + dx, dy - (int)(y * zoomFactor));
    }
    
    public static final double MARGING = 10;
    
    static int paintNum = 0;
    @Override
    protected void paintComponent(Graphics g) {
      
        if ( zoomFactor == Double.POSITIVE_INFINITY) {
            if (( Math.abs(conf.workspaceWidth) < 0.1) || (Math.abs(conf.workspaceHeight) < 0.1)) {
                if ( document.isEmpty()) {
                    dx = getWidth() /2;
                    dy = getHeight() /2;
                    zoomFactor = 1;
                } else doAction(ACTION_FOCUS_VIEW, 0, null);
            } else {
                // calculate zoom to see workspace
                double zx = ((double)(getWidth()-MARGING*2))/conf.workspaceWidth;
                double zy = ((double)(getHeight()-MARGING*2))/conf.workspaceHeight;
                zoomFactor = Math.min(zx, zy);
                switch ( conf.workspaceOrigin) {
                    case 0: 
                        dx = (getWidth() - (int)(conf.workspaceWidth * zoomFactor)) /2;
                        dy = getHeight() -(getHeight() - (int)(conf.workspaceHeight * zoomFactor))/2 - (int)(conf.workspaceHeight * zoomFactor); 
                        break;
                    case 1: 
                        dx = (int)(conf.workspaceWidth * zoomFactor) + (getWidth() - (int)(conf.workspaceWidth * zoomFactor)) /2;
                        dy = getHeight() -(getHeight() - (int)(conf.workspaceHeight * zoomFactor))/2 - (int)(conf.workspaceHeight * zoomFactor); 
                        break;
                    case 2: 
                        dx = (getWidth() - (int)(conf.workspaceWidth * zoomFactor)) /2;
                        dy = getHeight() -(getHeight() - (int)(conf.workspaceHeight * zoomFactor))/2; 
                        break;
                    case 3:
                        dx = (int)(conf.workspaceWidth * zoomFactor) + (getWidth() - (int)(conf.workspaceWidth * zoomFactor)) /2;
                        dy = getHeight() -(getHeight() - (int)(conf.workspaceHeight * zoomFactor))/2; 
                        break;
                }
            }         
        }
        g.setColor(Color.black);
        g.fillRect(0,0,getWidth(),getHeight());
        
        if ( (backgroundPictureParameter != null) && (backgroundPictureParameter.getImage() != null) && backgroundPictureParameter.isImageVisible()) {
            Graphics2D g2 = (Graphics2D)g;
            AffineTransform t = g2.getTransform();
            Composite comp = g2.getComposite();
            java.awt.Point o = coordToScreenPoint( getBackgroundPictureParameters().getViewX(), getBackgroundPictureParameters().getViewY());
            g2.translate(o.getX(), o.getY());
            AlphaComposite acomp = AlphaComposite.getInstance(
                                        AlphaComposite.SRC_OVER, getBackgroundPictureParameters().getAlpha());
            g2.setComposite( acomp);
            g2.rotate(Math.toRadians(backgroundPictureParameter.getRotation()));
            
            double w = backgroundPictureParameter.getWidth();
            double h = backgroundPictureParameter.getHeight();
            if ( (w == 0) || (h == 0)) { 
                w = backgroundPictureParameter.getImage().getWidth();
                h = backgroundPictureParameter.getImage().getHeight();
            }
            g2.drawImage(backgroundPictureParameter.getImage(), 0, 0, 
                            (int)(w*zoomFactor),
                            (int)(h*zoomFactor),
                            this);
            g2.setComposite(comp);
            g2.setTransform(t);
        }
        
        // paint axis
        g.setColor(Color.lightGray);
        g.drawLine(0, dy, getWidth(), dy);
        g.drawLine(dx, 0, dx, getHeight());        
        
        // paint workspace
        if ( showWorkspace) {
            g.setColor(Color.yellow);
            int wsW = (int)(conf.workspaceWidth * zoomFactor);
            int wsH = (int) (conf.workspaceHeight * zoomFactor);
            switch ( conf.workspaceOrigin) {
                case 0: g.drawRect(dx, dy, wsW, wsH); break;
                case 1: g.drawRect(dx-wsW, dy, wsW, wsH); break;
                case 2: g.drawRect(dx, dy-wsH, wsW, wsH); break;
                case 3: g.drawRect(dx-wsW, dy-wsH, wsW, wsH); break;
            }
        }
        
        // paint rotating object workspace
        if ( showObjectSurface) {           
            g.setColor(Color.red);
            Point2D o = coordToScreenPoint(0,0);
            Point2D p = coordToScreenPoint(conf.objectLength, conf.objectDiameter * Math.PI);
            int w = (int)(p.getX()-o.getX());
            int h = (int)(o.getY()-p.getY());
            g.drawRect((int)o.getX()-w, (int)o.getY()-h, 2 * w, 2 * h);
        } 
               
        // paint Grid
        if ( showGrid) {
            gridStep = 10;
            while( (gridStep * zoomFactor) < 10) gridStep *= 10;
            while( (gridStep * zoomFactor) > 100) gridStep /= 10;

            Point2D upleft = screenToCoordPoint( 0, 0);
            Point2D downRight = screenToCoordPoint(getWidth(), getHeight());
  //          System.out.println(upleft + "   " + downRight + gridStep / zoomFactor);

            for ( double x = Math.floor((upleft.getX() / gridStep))*gridStep; x < downRight.getX() + gridStep; x += gridStep) {
                //g.drawLine((int)(x * zoomFactor), 0, (int) (x * zoomFactor), getHeight());
                for ( double y = Math.floor((upleft.getY() / gridStep))*gridStep; y > downRight.getY() + gridStep; y -= gridStep) {
                    //g.drawLine(0, (int)(y * zoomFactor), getWidth(), (int) (y * zoomFactor));
                    if ( (Math.abs((x / (10*gridStep))-Math.round(x / (10*gridStep)))<0.00001) ||
                         (Math.abs((y / (10*gridStep))-Math.round(y / (10*gridStep)))<0.00001))
                        g.setColor(Color.white); //Color.lightGray);
                    else 
                        g.setColor(new Color(100,100,100));
                      
                    g.drawRect((int)(x* zoomFactor) + dx,  dy - (int)(y* zoomFactor), 0, 0);
                }
            }
        }
              
        ((Graphics2D)g).translate(dx, dy); 
        
        if ( showMoves && (editedElement==null)) paintMoves(g, document, null);          
        /* Show boudaries ? 
        if ( ! selectedElements.isEmpty()) {
            g.setColor(Color.orange);
            Rectangle r = coordToScreen(getSelectionBoundary(true));
            g.drawRect(r.x, r.y, r.width, r.height);
        }
        /* */    
              
        // paint document without selection
        final PaintContext pc = new PaintContext(g, zoomFactor, showStartPoints, highlitedPoint, selectedPoints, conf.toolDiameter);
        pc.editedElement = editedElement;
        // paintDisabled before
        paintGroup(pc, document, false, true, null);
        // paint non selected
        paintGroup(pc, document, false, false, null);
        // paint selection after
        if ( ! selectedElements.isEmpty()) {
            paintGroup(pc, document, true, true, null);     
            paintGroup(pc, document, true, false, null);      
        }
        // last paint edited element
        if ( editedElement != null) {
            pc.color = PaintContext.EDIT_COLOR;
            editedElement.paint(pc);
        }

        ((Graphics2D)g).translate(-dx, -dy);
        
        // paint mouse repere
        if ( mouseMode != MOUSE_MODE_NONE) {
            g.setColor(Color.LIGHT_GRAY);
            final float dashs[] = {1.f};
            final BasicStroke stroke = new BasicStroke(1.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.f, dashs, 1.f);
            final java.awt.Stroke savedStroke =  ((java.awt.Graphics2D)g).getStroke();
            ((java.awt.Graphics2D)g).setStroke(stroke);
            g.setXORMode(MOVE_COLOR);
            g.drawLine(screenMousePosition.x, 0, screenMousePosition.x, getHeight());
            g.drawLine(0, screenMousePosition.y, getWidth(), screenMousePosition.y );
            g.setPaintMode();
            ((java.awt.Graphics2D)g).setStroke(savedStroke);
        }
  
        // paint current mouse work
        paintMouseWork(g);
        
        // paint snap position if needed
        if (snapToGrid || snapToPoints) {
            g.setColor(Color.pink);
            //java.awt.Point p = getScreenSnapPointFor((int)mousePosition.getViewX(), (int)mousePosition.getViewY());
            //g.drawOval(p.x-2, p.y-2, 5, 5);
            g.drawOval(screenMousePosition.x-3, screenMousePosition.y-3, 6, 6);
        }

        // paint cursor2D
        if ( coord2DCursor != null) {
            screen2DCursor = coordToScreenPoint(coord2DCursor);
            g.setColor(Color.white);
            g.drawLine((int)screen2DCursor.x-10, (int)screen2DCursor.y, (int)screen2DCursor.x+10, (int)screen2DCursor.y);
            g.drawLine((int)screen2DCursor.x, (int)screen2DCursor.y-10, (int)screen2DCursor.x, (int)screen2DCursor.y+10);  
            g.setColor(Color.pink);
            g.drawOval(screen2DCursor.x - 5, screen2DCursor.y -5, 10, 10);
        }
        
        // paint Laser position if needed
        if ( showHead && (grblMPos != null)) {
            g.setColor( Color.PINK);
            java.awt.Point p = coordToScreenPoint(grblMPos.getX(), grblMPos.getY());
            g.drawRect(p.x -10, p.y - 10, 20, 20);
            g.drawLine(p.x -10, p.y - 10, p.x +10, p.y + 10);
            g.drawLine(p.x +10, p.y - 10, p.x -10, p.y + 10);
            g.drawOval(p.x-10, p.y-10, 20, 20);
            int d = (int)(conf.toolDiameter*zoomFactor/2);
            g.drawOval(p.x-d, p.y-d, d*2, d*2);
            
        }
    }
    
    private void paintMouseWork(Graphics g) {
        switch ( mouseMode) {
            case MOUSE_MODE_ADD_CURVE:
                if (coordMouseOrigin!=null) {
                    java.awt.Point p = coordToScreenPoint(coordMouseOrigin);
                    g.setColor((editedElement != null) ? PaintContext.EDIT_COLOR : Color.WHITE); 
                    g.drawLine(p.x, p.y, screenMousePosition.x, screenMousePosition.y);
                }
                break;
            
            case MOUSE_MODE_ADD_OVAL:
                if (screenMousePressPosition!=null) {
                    Rectangle2D.Double r = new Rectangle2D.Double(screenMousePressPosition.x, screenMousePressPosition.y, 0, 0);
                    r.add(screenMousePosition);

                    g.setColor(Color.YELLOW); 
                    if ( shiftDown ) 
                        if ( r.height > r.width) r.width = r.height; 
                        else r.height = r.width;
                    if( ctrlDown) {                
                        int w = Math.abs(screenMousePressPosition.x-mouseRectangleP2.x);
                        int h = Math.abs(screenMousePressPosition.y-mouseRectangleP2.y);
                        if ( shiftDown) { if ( w > h) h=w; else w=h; }
                        if ( altDown)
                            r = new Rectangle2D.Double(mouseRectangleP2.x - w, mouseRectangleP2.y - h, w*2, h*2);
                        else
                            r = new Rectangle2D.Double(screenMousePressPosition.x - w, screenMousePressPosition.y - h, w*2, h*2);
                    } else if ( altDown) {
                        Point2D center = new GCode((screenMousePosition.x-screenMousePressPosition.x)/2,
                                             (screenMousePosition.y-screenMousePressPosition.y)/2);
                        double diam = screenMousePressPosition.distance(screenMousePosition);
                        r = new Rectangle2D.Double(screenMousePressPosition.x+center.getX()-diam/2, screenMousePressPosition.y+center.getY()-diam/2,diam,diam);
                    }
                    drawCross(g, screenMousePressPosition, 3);
                    g.drawOval((int)r.x,(int)r.y, (int)r.width, (int) r.height);  
                }
                break;
            case MOUSE_MODE_ADD_CIRCLES:
                if (screenMousePressPosition!=null) {
                    g.setColor(Color.yellow);
                    int rx, ry;
                    if ( ctrlDown) {
                        rx = ry = (int)screenMousePressPosition.distance(screenMousePosition);
                    }
                    else {
                        rx = (screenMousePosition.x-screenMousePressPosition.x);
                        ry = (screenMousePosition.y-screenMousePressPosition.y);
                        if ( ! shiftDown && ((rx==0) || (ry==0))) {
                            if ( rx > ry) ry = rx;
                            else ry = rx; 
                        }
                    }

                    drawCross(g, screenMousePressPosition, 3);
                    if ( shiftDown && ! (altDown|ctrlDown)) {
                        java.awt.Point center = new java.awt.Point(screenMousePressPosition.x+rx/2, screenMousePressPosition.y+ry/2);
                        int radius = (int)screenMousePressPosition.distance(screenMousePosition)/2;
                        g.drawOval(center.x-radius, center.y-radius, radius*2, radius*2);
                        drawCross(g, center, 3);
                        drawCross(g, screenMousePressPosition, 3);
                      //  drawCross(g, new java.awt.Point(center.x-radius/2, center.y-radius/2), 5);
                    } else {
                        rx = Math.abs(rx); ry = Math.abs(ry);
                        if ( ! altDown) {  
                            g.drawOval((int)(screenMousePosition.x-rx), (int)(screenMousePosition.y-ry), (int)(2*rx), (int)(2*ry));
                        } else {                
                            g.drawOval((int)(screenMousePressPosition.x-rx), (int)(screenMousePressPosition.y-ry), (int)(2*rx), (int)(2*ry));
                        }
                    }
                }
                break;
            case MOUSE_MODE_ADD_G23_ARC:
            case MOUSE_MODE_ADD_LINES:
                if ( editedElement!=null) {
                    // paint new segment in MOUSE_MODE_ADD_POINT
                    if ( editedElement.getLastPoint()!=null) {
                        java.awt.Point p = coordToScreenPoint(editedElement.getLastPoint());
                        g.setColor(PaintContext.EDIT_COLOR);
                        g.drawLine(p.x, p.y, screenMousePosition.x, screenMousePosition.y);
                        inform("distance="+ String.format("%.5f", editedElement.getLastPoint().distance(coordSnapPosition)));
                    }
                }
                break;
            case MOUSE_MODE_SHOW_DISTANCE:
            case MOUSE_MODE_CURSOR_AT_CENTER:
                if (coordMouseOrigin!=null) {
                    java.awt.Point screenTransformationOrigin = coordToScreenPoint(coordMouseOrigin);
                    g.setColor(Color.yellow);
                    g.drawLine((int)screenTransformationOrigin.getX(), (int)screenTransformationOrigin.getY(),
                               (int)screenMousePosition.getX(), (int)screenMousePosition.getY());
                    if ( mouseMode == MOUSE_MODE_CURSOR_AT_CENTER)
                        drawCross(g, new java.awt.Point(
                                            screenTransformationOrigin.x+(screenMousePosition.x-screenTransformationOrigin.x)/2,
                                            screenTransformationOrigin.y+(screenMousePosition.y-screenTransformationOrigin.y)/2), 5);
                }
                break;
            case MOUSE_MODE_SHOW_ANGLE:
                if (coordMouseOrigin!=null) {
                    java.awt.Point screenTransformationOrigin = coordToScreenPoint(coordMouseOrigin);

                    g.setColor(Color.yellow);
                    g.drawLine((int)screen2DCursor.getX(), (int)screen2DCursor.getY(),
                               (int)screenTransformationOrigin.getX(), (int)screenTransformationOrigin.getY());
                    g.drawLine((int)screen2DCursor.getX(), (int)screen2DCursor.getY(),
                               (int)screenMousePosition.getX(), (int)screenMousePosition.getY());
                }
                break;
            case MOUSE_MODE_ROTATION:
                if ( transformationOrigin != null) {
                    java.awt.Point screenTransformationOrigin = coordToScreenPoint(transformationOrigin);
                    g.setColor(Color.red);
                    drawCross(g, screenTransformationOrigin, 10);
                    g.setColor(Color.yellow);
                    if( ! Double.isNaN(finalRotationAngle) && shiftDown) {
                        double l = screenTransformationOrigin.distance(screenMousePosition);
                        g.drawLine((int)screenTransformationOrigin.getX(), (int)screenTransformationOrigin.getY(),
                                (int)(screenTransformationOrigin.getX()+Math.cos(-lastRotationAngle) * l), (int)(screenTransformationOrigin.getY()+Math.sin(-lastRotationAngle)* l));
                    } else
                        g.drawLine((int)screenTransformationOrigin.getX(), (int)screenTransformationOrigin.getY(), 
                            screenMousePosition.x, screenMousePosition.y);
                }
                break;
            case MOUSE_MODE_SCALE:
                if ( transformationOrigin != null) {
                    java.awt.Point screenTransformationOrigin = coordToScreenPoint(transformationOrigin);
                    g.setColor(Color.red);
                    drawCross(g, screenTransformationOrigin, 10);
                    g.setColor(Color.yellow);              
                    if (scaleNormaleX != Double.MAX_VALUE )  {
                        g.setColor(Color.red);
                        int radiusX = (int)Math.abs(scaleNormaleX*zoomFactor);
                        int radiusY = (int)Math.abs(scaleNormaleY*zoomFactor);
                        if ( shiftDown || (radiusX<0.00001) || (radiusY<0.00001)) {
                            if ( radiusY > radiusX) radiusX = radiusY;
                            else radiusY = radiusX;
                        }
                        g.drawOval((int)(screenTransformationOrigin.getX()-radiusX), 
                                    (int)(screenTransformationOrigin.getY()-radiusY), 
                                    (int)(radiusX*2), (int)(radiusY*2));
                    } 
                    else if ( transformationOrigin != null) {
                        g.setColor(Color.yellow);
                        double rx = Math.abs(screenTransformationOrigin.x-screenMousePosition.x);
                        double ry = Math.abs(screenTransformationOrigin.y-screenMousePosition.y);
                         if ( shiftDown || (rx==0) || (ry==0)) {
                             if ( rx > ry) ry = rx;
                             else ry = rx; 
                         }
                        g.drawOval((int)(screenTransformationOrigin.x-rx), (int)(screenTransformationOrigin.y-ry), (int)(2*rx), (int)(2*ry));
                    } 
                }
                break;
        default:
        
            if ((mouseRectangleP2 != null)) {
                // TODO IDEA use right click to select all shaped an left is only for non enabled ?
                Rectangle2D.Double r = new Rectangle2D.Double(screenMousePressPosition.x, screenMousePressPosition.y, 0, 0);
                r.add(mouseRectangleP2);

                if (mouseMode == MOUSE_MODE_ADD_RECTANGLES) {
                    g.setColor(Color.YELLOW); 
                    if (mouseMode == MOUSE_MODE_ADD_RECTANGLES) {
                        if ( shiftDown ) 
                            if ( r.height > r.width) r.width = r.height; 
                            else r.height = r.width;
                        if( ctrlDown) {                
                            int w = Math.abs(screenMousePressPosition.x-mouseRectangleP2.x);
                            int h = Math.abs(screenMousePressPosition.y-mouseRectangleP2.y);
                            if ( shiftDown) { if ( w > h) h=w; else w=h; }
                            if ( altDown)
                                r = new Rectangle2D.Double(mouseRectangleP2.x - w, mouseRectangleP2.y - h, w*2, h*2);
                            else
                                r = new Rectangle2D.Double(screenMousePressPosition.x - w, screenMousePressPosition.y - h, w*2, h*2);
                        }
                    }
                }
                else 
                    g.setColor(Color.MAGENTA); // draw rectangle selection
            
                drawCross(g, screenMousePressPosition, 3);
                g.drawRect( (int)r.x,(int)r.y, (int)r.width,(int) r.height);  
            } 
        }
    }
    
    public void drawCenteredCircle(Graphics g, int x, int y, int r) {
        x = x-(r/2);
        y = y-(r/2);
        g.fillOval(x,y,r,r);
    }
    
    /**
     * Paint a cross with <i>g</i>
     * @param g
     * @param at
     * @param dim 
     */
    public static void drawCross(Graphics g, java.awt.Point at, int dim) {
        g.drawLine(at.x-dim, at.y, at.x+dim, at.y);
        g.drawLine(at.x, at.y-dim, at.x, at.y+dim);
    }
    
    
    public static final int GCREADER_UNKNOW_STATE = 0;
    public static final int GCREADER_WAIT_EOL_STATE = 1;
    public static final int GCREADER_READCMD_STATE = 2;
    public static final int GCREADER_READCOORD_STATE = 3;
    /**
     * Try to import GCode blocks from raw GCODE file, or from a VGE Project file.
     * @param gCodeFileName
     * @param background the background image paramter. use reloadImage() to use it.
     * @return the imported content
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static GGroup importGCODE( String gCodeFileName, BackgroundPictureParameters background) throws FileNotFoundException, IOException {

        int lineno=0;
        String name = (gCodeFileName.lastIndexOf('/') != -1) ? gCodeFileName.substring( gCodeFileName.lastIndexOf('/')+1) : gCodeFileName;    
        if ( name.lastIndexOf('.')!=-1) name = name.substring(0, name.lastIndexOf('.'));
        GGroup currentGroup, doc = new GGroup(name);
        GElement currentBlock = null;
        GCode currentState = new GCode(-1, 0,0);
        
        BufferedReader br = new java.io.BufferedReader( new FileReader(gCodeFileName));
        String line = "";
        
        currentGroup = doc;
        do
        { 
            lineno++;
            line = line.trim();            
            while ( (line != null) && line.isBlank()) line = br.readLine(); // skip empty lines
            if ( line == null) break;
            
            if ( line.startsWith(SVGE_HEADER) || line.startsWith( CONTENT_HEADER)) {
                // Loading properties of document
                line = br.readLine();
                if ( line == null) 
                    break;
                if ( line.startsWith(BackgroundPictureParameters.BACK_PICTURE_HEADER)) {
                    BackgroundPictureParameters b = BackgroundPictureParameters.decode(line);
                    if ((background != null) && (b != null)) background.setAll(b);
                    line = br.readLine();
                }
                line = currentGroup.loadProperties(br, line);
                continue;
            }
                
            if ( line.startsWith(GGroup.END_HEADER_STRING)) {
                if ( (currentBlock!=null) && ! currentBlock.isEmpty())
                    currentGroup.add( currentBlock);          
                currentBlock = null;
                currentGroup = doc.getParent(currentGroup); 
            } else {
                GElement el = GElement.buildGElement(line);
                if ( el != null) {
                    // Load GElement
                    if ( (currentBlock!=null) && ! currentBlock.isEmpty()) currentGroup.add( currentBlock); 
                    currentBlock = null;
                    
                    line = el.loadFromStream(br, currentState);
                    currentGroup.add(el);
                    
                    if ( el instanceof GGroup) currentGroup = (GGroup)el;  
                    else currentState.updateGXYWith(el.getLastPoint());
                    continue;
                } else {
                    // Read GCODE an try to make GElement or append to currentPath
                    GCode gcl = new GCode(line, currentState);
                    if ( gcl.isComment()) {
                        if (currentBlock!=null) {
                            if (currentBlock.getNbPoints() > 0) {
                                currentGroup.add( currentBlock); 
                                currentBlock = new G1Path("path " + name);  
                            }
                        } else
                            currentBlock = new G1Path("path " + name); 

                        currentBlock.add( gcl);

                    } else if ( gcl.isAnArc()) {
                        if ((currentBlock!=null) && ! currentBlock.isEmpty() && ! (currentBlock instanceof GArc )) {
                            currentGroup.add( currentBlock);  
                        }
                        currentGroup.add(currentBlock = new GArc(name, currentState, gcl));
                        currentState = currentBlock.getLastPoint();
                        currentBlock=null;

                    } else if ( gcl.isADrill() ) {
                        if ((currentBlock!=null) && ! currentBlock.isEmpty()) {
                            currentGroup.add( currentBlock);
                        }
                        currentGroup.add( currentBlock = new GDrillPoint("drill", gcl));          
                        currentState = currentBlock.getLastPoint();
                        currentBlock = null;

                    } else if ( gcl.isASpline()) {
                        if ((currentBlock!=null) && ! currentBlock.isEmpty()) {
                            currentGroup.add( currentBlock);
                        }
                        currentBlock = new GSpline("curve", currentState, gcl);
                        currentState.updateGXYWith(currentBlock.getLastPoint());
                        currentBlock = null;

                    } else if (gcl.isAMove() && (gcl.isSet('X') || gcl.isSet('Y'))) {
                        if (gcl.getG() == 0) {               
                            // Have we read an Header ?
                            if ( doc.isEmpty() && (currentBlock != null) && (currentBlock.getClass() == G1Path.class) && ! currentBlock.isEmpty() && (currentBlock.getNbPoints()==0)) {
                                currentBlock.setName("Header");
                                doc.add(currentBlock);
                                currentBlock = new G1Path(name + "-p" + GElement.getUniqID());
                            } else {
                                // No, start a new block
                                if ((currentBlock!=null) && ! currentBlock.isEmpty() && (currentBlock.getNbPoints()>0) &&
                                        (gcl.containsXorYCoordinate() || 
                                            ((currentBlock.getNbPoints()!=0)) && currentBlock.getLastPoint().getG()==0)) {
                                    currentGroup.add(currentBlock);
                                    currentBlock=null;
                                }
                                if ( currentBlock == null)
                                    currentBlock = new G1Path("path " + name); 
                            }
                        } else {
                            // G1 move then add G0 first
                            if ( currentBlock == null) 
                                currentBlock = new G1Path("path " + name);
                            
                            if ( (currentBlock.getFirstPoint() == null) && gcl.isAPoint() && ! currentState.isAtSamePosition(gcl)) // Add G0 start point
                                currentBlock.add(new GCode(0, gcl.getX(), gcl.getY()));
                        }                       
                        currentState.updateGXYWith(gcl); 
                        currentBlock.add(gcl);                       

                    } else {
                        // line is not a G{0,1,2,3,5,8x} mouvment (to X,Y)
                        // start a new block ?
                        // TODO: take care of G54 X0 Y0 line
                        if ( gcl.isPercent() ) {
                            if ( (currentBlock != null) && ! currentBlock.isEmpty()) currentGroup.add(currentBlock);
                            currentBlock = new G1Path(name);
                        } else {                             
                            if ( currentBlock==null) 
                                currentBlock = new G1Path("path " + name);
                            if ( gcl.isAPoint() && (currentBlock.getFirstPoint()==null) && ( ! gcl.isSet('G') || (gcl.getG()!=0))) {
                                // add G0 to start new path
                                currentBlock.add(new GCode(0, gcl.getX(), gcl.getY()));
                            }    
                            
                            currentState.updateGXYWith(gcl);   
                            currentBlock.add(gcl);                       
                        }
                    }
                }
            }
            line = br.readLine();
        } while( line != null);
        
        if ( (currentBlock != null) && ! currentBlock.isEmpty()) currentGroup.add(currentBlock);
        return doc;
    }
        
    public void importDXF(String fileName) throws ParseException
    {
        Parser parser = ParserBuilder.createDefaultParser();
        parser.parse(fileName, DXFParser.DEFAULT_ENCODING);
        //System.out.println("Loading " + fileName);
        String[] l = fileName.split("/");
        String blockName = l[l.length-1];
        
        DXFDocument doc = parser.getDocument();
        
        ArrayList<GElement> newBlocks = new ArrayList<>();
        G1Path lastBlock = null;
        
        for( @SuppressWarnings("unchecked") Iterator<DXFLayer> i = doc.getDXFLayerIterator(); i.hasNext(); )
        {
            DXFLayer layer = i.next();
            
            for( Iterator e = layer.getDXFEntityTypeIterator(); e.hasNext();)
            {
                Object type = e.next();
                @SuppressWarnings("unchecked") List<DXFEntity> le = layer.getDXFEntities( type.toString());
                for( DXFEntity el : le)
                {
                    if ( el instanceof DXFLWPolyline) {
                        DXFLWPolyline poly = (DXFLWPolyline)el;
                        if ( poly.getVertexCount() < 2) continue;
                        
                        G1Path s;
                        if ((lastBlock != null) && G1Path.isEquals(lastBlock.getLastPoint(),poly.getVertex(0).getPoint()))
                            s = lastBlock;
                        else 
                            s = new G1Path(blockName);
                        
                        for( int n = (s==lastBlock)?1:0; n < poly.getVertexCount(); n++)
                            s.add(poly.getVertex(n).getPoint());
                             
                        if ( lastBlock != s) newBlocks.add(lastBlock = s);
                        
                    } else if ( el instanceof DXFLine) {
                        DXFLine line = (DXFLine)el;
                        
                        G1Path s;
                        if ((lastBlock != null) && G1Path.isEquals(lastBlock.getLastPoint(),line.getStartPoint()))
                        {
                            s = lastBlock;
                            s.add( line.getEndPoint());
                        } else {
                            s = new G1Path(blockName);
                            s.add( line.getStartPoint());
                            s.add( line.getEndPoint());
                        }
                        if ( lastBlock != s) newBlocks.add(lastBlock = s);
                        
                    } else if ( el instanceof DXFSpline) {
                        DXFSpline spline = (DXFSpline)el;
                        GCode pts[] = new GCode[spline.getControlPointSize()];
                        
                        @SuppressWarnings("unchecked") Iterator<SplinePoint> spi = spline.getSplinePointIterator();
                        int i2 = 0;
                        while ( spi.hasNext()) {
                            SplinePoint p = spi.next();
                            pts[i2++] = new GCode(p.getX(), p.getY());
                        }
                        
                        GSpline spl = null;
                        switch( pts.length) {
                            case 2: 
                                //spl = new GSpline(blockName+"_curve", pts[0], pts[1]); 
                                break;
                            case 3: 
                                //spl = new GSpline(blockName+"_curve", pts[0], pts[1], pts[2]); 
                                break;
                            case 4: 
                                
                                spl = new GSpline(blockName+"_curve", pts[0], pts[1], pts[2], pts[3]); 
                                break;    
                        }
                            
                        if ( spl != null) newBlocks.add(spl);
                    } else if ( el instanceof DXFCircle) {
                        DXFCircle circle = (DXFCircle)el;
                        Point p = circle.getCenterPoint();
                        double r = circle.getRadius();
                        int nbP = (int)(2*Math.PI*r);
                        double a = 2*Math.PI / nbP;
                        lastBlock = new G1Path(blockName + "_circle");
                        for( int pt = 0; pt < nbP; pt++) {
                            lastBlock.add(new Point(p.getX()+Math.cos(a*pt)*r, p.getY()+Math.sin(a*pt)*r,0));
                            
                        } 
                        newBlocks.add(lastBlock);
                    } else if ( el instanceof DXFPolyline) {
                        DXFPolyline poly = (DXFPolyline)el;
                        
                        G1Path s;
                        if ((lastBlock != null) && G1Path.isEquals(lastBlock.getLastPoint(),poly.getVertex(0).getPoint()))
                        {
                            s = lastBlock;
                        } else {
                            s = new G1Path(blockName);
                        }
                        for( int n = (s==lastBlock)?1:0; n < poly.getVertexCount(); n++)
                        { 
                            Point p = poly.getVertex(n).getPoint();
                            s.add(poly.getVertex(n).getPoint());                          
                        }
                        if ( lastBlock != s) newBlocks.add(lastBlock = s);
                    } else if ( el instanceof DXFArc) {
                        DXFArc arc = (DXFArc)el;
                        Point center = arc.getCenterPoint();
                        //Point endP = arc.getEndPoint();
                        //Point startP = arc.getStartPoint();
                        double radius = arc.getRadius();
                        double startA = 360 - arc.getStartAngle();
                        double arcLen = (arc.isCounterClockwise() ? 360 - arc.getTotalAngle() : arc.getTotalAngle() - 360);                        
                        //boolean cc = arc.isCounterClockwise();
                        GCode c = new GCode(center.getX(), center.getY());
                        //System.out.println("c=("+c.getX()+","+c.getY()+")  s="+startA+"  rad="+Math.toRadians(startA)+"   l="+arcLen);
                        newBlocks.add(new GArc("_arc", c, radius, startA, arcLen));
                    } else
                        System.err.println("Impossible de charger l'entité '" + el.getType() + "'");
                    
                }
            }
            if ( gcodeFooter != null) { //TODO: verify that
                document.remove(gcodeFooter);
                document.add(gcodeFooter);
            }
        }
        
        // clear empty blocks
        Iterator<GElement> i = newBlocks.iterator();
        while ( i.hasNext()) {
            GElement b = i.next();
            if ( b instanceof G1Path && b.isEmpty()) i.remove();
        }
        
        boolean concat;
        do {
            concat = false;
            for( int s1 = 0; s1 < newBlocks.size()-1; s1++ )
                for( int s2 = 0; s2 < newBlocks.size()-1; s2++ )
                {
                    if ((s1 != s2) && (newBlocks.get(s1) instanceof G1Path) && (newBlocks.get(s2) instanceof G1Path)) {
                        if ( newBlocks.get(s1).getFirstPoint() == null || newBlocks.get(s2).getFirstPoint() == null)
                            System.out.println("importDXF: ERROR1");
                        
                        if ( newBlocks.get(s1).getFirstPoint().equals( newBlocks.get(s2).getFirstPoint()) ||
                                newBlocks.get(s1).getLastPoint().equals( newBlocks.get(s2).getFirstPoint()) ||
                                newBlocks.get(s1).getLastPoint().equals( newBlocks.get(s2).getLastPoint())) {
                            //System.out.print("concat ");
                            newBlocks.get(s1).concat(newBlocks.get(s2), 0.00001);
                            newBlocks.remove(s2);
                            concat=true;
                        }
                    }
                }
        } while( concat);
        
        if ( ! newBlocks.isEmpty()) {
            GGroup g = new GGroup(blockName, newBlocks);
            g.sort(null, true);
            g.translate(coord2DCursor.getX(), coord2DCursor.getY());
            add(g);
            inform( newBlocks.size() + " path(s) imported");  
        }
    }
    
    
    private GCode currentPosition;
    /**
     * Read SVG file and return his content
     * 
     * @param svgFileName
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public GGroup readSVGfile( String svgFileName) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {

        String name = (svgFileName.lastIndexOf('/') != -1) ? svgFileName.substring( svgFileName.lastIndexOf('/')+1) : svgFileName;    
        if ( name.lastIndexOf('.')!=-1) name = name.substring(0, name.lastIndexOf('.'));
        GGroup res = new GGroup(name);
       
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File( svgFileName));
        document.getDocumentElement().normalize();  
        DocumentTraversal traversal = (DocumentTraversal) document;
        TreeWalker walker = traversal.createTreeWalker(
                document.getDocumentElement(), NodeFilter.SHOW_ALL, null, true);
                
        Node xx = document.getElementsByTagName("svg").item(0).getAttributes().getNamedItem("viewBox");
        //if ( xx != null) System.out.println("SVG Box= " + xx.getNodeValue());
        
        try {
            readSVGtree(walker, "", res);  
        } catch ( Exception e) {
            EventQueue.invokeLater( new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showInternalMessageDialog(null, 
                            "Error while reading the file : \n\n" + svgFileName + "\n\n" + e.getLocalizedMessage()+
                                    "\n\nTry plain/simple SVG format (with Inkscape)", 
                            "Error", JOptionPane.INFORMATION_MESSAGE);
                    
                    e.printStackTrace();
                } });
        }
        
        res.removeExtraGroups();
        return res;
    }

    private void readSVGtree(TreeWalker walker, String indent, GGroup parent) {
        Node node = walker.getCurrentNode();
        if (node instanceof Element) {
            Element element = (Element)node;
            
            String id = element.getAttribute("id"); 
            String transform = element.getAttribute("transform");
            
            //System.out.println(indent + "- " + element.getTagName() + "(" + id + ") transform="+transform);                      

            switch ( element.getTagName()) {
                case "g":
                    GGroup g = new GGroup( (id == null) ? "g" : id);
                    parent.add(g);
                    parent = g;
                    for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
                        readSVGtree(walker, indent + "  ", parent);
                    }
                    walker.setCurrentNode(node);
                    applySVGTransformation(transform, g);
                    break;
                    
                case "polyline":                        
                case "path": 
                    if ( element.getTagName().equals("polyline")) {
                        String pts = "M " + element.getAttribute("points");
                        g = readSVGPath( id, pts);
                    } else {
                        String d = element.getAttribute("d");
                        g = readSVGPath( id, d);
                    }
                    
                    applySVGTransformation(transform, g);                    
                    if ( g.size() == 1) parent.add( g.get(0));
                    else parent.add( g);
                    break;

                case "rect":
                    double x = Double.parseDouble(element.getAttribute("x"));
                    double y = Double.parseDouble(element.getAttribute("y"));
                    double w = Double.parseDouble(element.getAttribute("width"));
                    double h = Double.parseDouble(element.getAttribute("height"));  
                    
                    double rx = Double.NaN, ry = Double.NaN;
                    if ( !  element.getAttribute("rx").isBlank()) {
                        rx = Double.parseDouble(element.getAttribute("rx"));                        
                    }
                    if ( !  element.getAttribute("ry").isBlank()) {
                        ry = Double.parseDouble(element.getAttribute("ry"));                        
                    }
                    
                    G1Path g1p;
                    if ( ! Double.isNaN(rx) || ! Double.isNaN(ry)) {
                        if ( Double.isNaN(rx)) rx = ry;
                        if ( Double.isNaN(ry)) ry = rx;
                        g1p = G1Path.makeRounRect(w, h, Math.max(rx, ry)); // TODO: use ry !
                        g1p.translate(x, y);
                    } else                    
                        g1p = G1Path.newRectangle(new GCode(x, y), new GCode(x+w, y+h));
                    
                    applySVGTransformation(transform, g1p);
                    parent.add( g1p);
                    break;
                    
                case "ellipse":
                    double cx = Double.parseDouble(element.getAttribute("cx"));
                    double cy = Double.parseDouble(element.getAttribute("cy"));
                    rx = Double.parseDouble(element.getAttribute("rx"));
                    ry = Double.parseDouble(element.getAttribute("ry")); 
                    
                    g1p = G1Path.makeOval(new Point2D.Double(cx, cy), rx, ry, conf.minG1move);
                    applySVGTransformation(transform, g1p);
                    parent.add( g1p );   
                    break;
                case "circle":
                    cx = Double.parseDouble(element.getAttribute("cx"));
                    cy = Double.parseDouble(element.getAttribute("cy"));
                    rx = Double.parseDouble(element.getAttribute("r"));
                    GArc garc = new GArc("circle"+GElement.getUniqID(), new GCode(cx,cy), rx, 0, 360);
                    applySVGTransformation(transform, garc);
                    parent.add( garc);   
                    break;

                case "svg":
                    /*if ( ! element.getAttribute("height").isBlank()) {
                        double height = Double.parseDouble(element.getAttribute("height"));
                        double width = Double.parseDouble(element.getAttribute("width"));
                    }*/
                    for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
                        readSVGtree(walker, indent + "  ", parent);
                    }
                    break;
                default:
                    System.out.println("(info) importSVG: ignoring <"+element.getTagName()+"...");  
                    for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
                        readSVGtree(walker, indent + "  ", parent);
                    }
            }            
        }
        walker.setCurrentNode(node);
    }      
    
    /**
     * Read a SVG Path as described here https://svg-path-visualizer.netlify.app/
     * 
     * @return the group containing all draw of this data
    */
    public GGroup readSVGPath( String name, String dataPathString) {
        GGroup res = new GGroup(name);
        GMixedPath mixPath = new GMixedPath( name);
        
        currentPosition = new GCode(0,0,0);
        
        char currentMode = '?';
        boolean absolute = false;
        GCode p, firstPoint = null, pt[] = new GCode[4], lastBezierPoint2 = null, lastBezierEnd = null;
        int patchNumber = 0, spNumber = 0, idSubPath = 1;
        
        String[] commands = dataPathString.split(" ");
        for( int cn = 0; cn < commands.length; ) {
            
            while ( commands[cn].isBlank() && (cn < commands.length)) cn++;
            
            if ( cn < commands.length) {
                String s = commands[cn];
                //System.out.println("S=["+s+"]");

                // get next command ?
                if ( ! s.isEmpty() ) {  
                    char t = s.toUpperCase().charAt(0);

                    if ( ! (((t>='0') && (t<='9')) || (t=='-'))) {                    
                        currentMode = s.toUpperCase().charAt(0);    
                        absolute = s.charAt(0) == currentMode; // absolute if cmd is UPPERCASE

                        if ( s.length() > 1) commands[cn] = s.substring(1);
                        else cn++;
                    }
                }

                // get next position ?
                switch( currentMode) {
                        //if ( firstPoint != null) System.err.println("SVGImport : moveTo in path");
                    case 'H':                        
                        if ( absolute) currentPosition.setX(Double.parseDouble( commands[cn++]));
                        else currentPosition.translate(Double.parseDouble( commands[cn++]), 0);
                        mixPath.add( currentPosition.clone());
                        break;
                    case 'V':   
                        if ( absolute) currentPosition.setY(Double.parseDouble( commands[cn++]));
                        else currentPosition.translate(0, Double.parseDouble( commands[cn++]));
                        mixPath.add( currentPosition.clone());  
                        break;
                    case 'M':   
                        if ( firstPoint != null) {
                            res.add( mixPath);
                            mixPath = new GMixedPath( name + "." + idSubPath++);
                        }                      
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        mixPath.add( firstPoint = currentPosition.clone()); 
                        currentMode = 'L'; // for polyline implementation
                        break;
                    case 'L':
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);                                   
                        mixPath.add( currentPosition.clone());      
                        break;
                    case 'C': // bezier
                        pt[0] = currentPosition.clone();                        
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        pt[1] = currentPosition.clone();
                        if ( ! absolute) currentPosition = pt[0].clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierPoint2 = pt[2] = currentPosition.clone();
                        if ( ! absolute) currentPosition = pt[0].clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierEnd = pt[3] = currentPosition.clone();
                        GElement el = new GSpline("sp"+spNumber++, pt[0], pt[1], pt[2], pt[3]);
                        if ( ! mixPath.add(el)) {
                            res.add( mixPath);
                            res.add(el);
                            mixPath = new GMixedPath( name + "." + idSubPath++);
                            mixPath.add( currentPosition.clone());
                        }
                        break;

                    case 'S': // bezier chained
                        pt[0] = lastBezierEnd.clone();
                        pt[1] = lastBezierEnd.getMirrorPoint(lastBezierPoint2 );
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierPoint2 = pt[2] = currentPosition.clone();
                        if ( ! absolute) currentPosition = pt[0].clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierEnd = pt[3] = currentPosition.clone();
                        mixPath.add(new GSpline("s"+spNumber++, pt[0], pt[1], pt[2], pt[3]));
                        break;
                    case 'Q': // quad
                        pt[0] = currentPosition.clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierPoint2 = pt[1] = currentPosition.clone();
                        if ( ! absolute) currentPosition = pt[0].clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierEnd = pt[2] = currentPosition.clone();
                        mixPath.add(new GSpline("q"+spNumber++, pt[0], pt[1], pt[2]));
                        break;                        
                    case 'T': // quad chained
                        pt[0] = lastBezierEnd.clone();
                        lastBezierPoint2 = pt[1] = lastBezierEnd.getMirrorPoint(lastBezierPoint2 );
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        lastBezierEnd = pt[2] = currentPosition.clone();
                        el = new GSpline("q"+spNumber++, pt[0], pt[1], pt[2]);
                        if ( ! mixPath.add( el)) {                                
                            res.add( mixPath);
                            res.add(el);
                            mixPath = new GMixedPath( name + "." + idSubPath++);
                            mixPath.add( currentPosition.clone());
                        } 
                        break;
                    case 'A': // elliptical Arc (rx,ry rotate LargeArcFlag SweepFlag fx,fy)    
                        pt[1] = currentPosition.clone();
                        
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, true);
                        pt[0] = currentPosition.clone(); // radiusX,radiusY
                                                        
                        float rotate = Float.parseFloat(commands[cn++]);
                        
                        boolean largeArcFlag,sweepFlag;
                        if ( commands[cn].contains(",")) {
                            String[] vals = commands[cn++].split(",");
                            largeArcFlag= Integer.parseInt(vals[0]) == 1;
                            sweepFlag = Integer.parseInt(vals[1]) == 1;                            
                        } else {
                            largeArcFlag= Integer.parseInt(commands[cn++]) == 1;
                            sweepFlag = Integer.parseInt(commands[cn++]) == 1;
                        }
    
                        if ( ! absolute) currentPosition = pt[1].clone();
                        cn = getNextSVGPosition( cn, commands, currentMode, currentPosition, absolute);
                        pt[2] = currentPosition.clone(); // last point 
                        
                        // radiusx, radiusy, xAxisRotation, boolean largeArcFlag, boolean sweepFlag, x, y                     
                        el = arcTo(pt[1], (float)pt[0].getX(), (float)pt[0].getY(), rotate, largeArcFlag, 
                                        sweepFlag, (float)currentPosition.getX(), (float)currentPosition.getY());
                                                
                        if ( ! mixPath.add( el)) {                                
                            res.add( mixPath);
                            res.add(el);
                            mixPath = new GMixedPath( name + "." + idSubPath++);
                            mixPath.add( currentPosition.clone());
                        }                           
                        break;
                    case 'Z':
                        if ( firstPoint != null) {
                            currentPosition.set(firstPoint.clone());
                            mixPath.add( currentPosition.clone());                           
                        }
                        break;
                    default:
                        System.err.println("importSVG: command '"+currentMode+"' in path is not implemented yet.");
                        cn++;
                }
            }
        }
        res.add( mixPath);
        return res;
    } 
    
        private void applySVGTransformation(String transform, GElement elem) {
        if ( (transform != null) && ! transform.isBlank() ) {
            Pattern pat = Pattern.compile("^([^\\(]+)\\(([^\\)]+)\\)"); 
            Matcher m = pat.matcher(transform);
            if( m.matches()) {
                //System.out.println(m.group(1) + " et " + m.group(2));

                switch( m.group(1)) {
                    case "translate":
                        String vals[] = new String[2];
                        vals[1] = m.group(2);
                        vals = extractNumberFrom(vals[1]);
                        double tx = Double.parseDouble( vals[0]);
                        vals = extractNumberFrom(vals[1]);
                        double ty = Double.parseDouble(vals[0]);
                        elem.translate(tx, ty);
                        break;
                        
                    case "matrix":
                        double matrix[] = new double[6];
                        vals = new String[2];
                        vals[1] = m.group(2);
                        for( int i = 0; i < 6; i++) {
                            vals = extractNumberFrom(vals[1]);
                            matrix[i] = Double.parseDouble(vals[0]);
                        }
                        AffineTransform t = new AffineTransform(matrix);
                        elem.transform( t);
                        break;
                        
                    case "scale":
                        vals = new String[2];
                        vals[1] = m.group(2);
                        vals = extractNumberFrom(vals[1]);
                        double sx = Double.parseDouble( vals[0]);
                        vals = extractNumberFrom(vals[1]);
                        double sy = Double.parseDouble(vals[0]);                                                
                        elem.scale(new Point2D.Double(), sx, sy);
                        break;
                        
                    default: 
                        System.out.println("importSVG: unknow transform="+transform);
                }
            }                                                            
        }    
    }
    
    /**
     * Update the current position accordint to the SVG d:path:element[number]
     * @param number            the current element in path
     * @param commands          the elements of the path
     * @param currentCommand    the current SVG:d:path command
     * @param currentPosition   the current position
     * @param absolute          read an absolute position ?
     * @return the next element position to read after
     */
    private static int getNextSVGPosition(int number, String[] commands, char currentCommand, GCode currentPosition, boolean absolute) {
        int cn = number;
        while( cn < commands.length ) {
            String p = commands[cn++];
            if ( ! p.isBlank()) {
                String[] v2, v1 = extractNumberFrom( p);                
                if ( v1[1].isBlank()) {
                    v2 = extractNumberFrom( commands[cn++]);
                } else {
                    v2 = extractNumberFrom(v1[1]);
                }
                if ( ! v2[1].isBlank()) {
                    cn--;
                    commands[cn] = v2[1];
                }
                
                currentPosition.set(currentCommand=='M'?0:1,
                                    Double.parseDouble(v1[0]) + (absolute ? 0 : currentPosition.getX()),
                                    Double.parseDouble(v2[0]) + (absolute ? 0 : currentPosition.getY()));
               
                return cn;
            }
        }
        return Integer.MAX_VALUE;
    }
    
    
    /**
     * Extract a number from 'input' string.
     * 
     * @param input the string to read the number
     * @return String[0] == the number, String[1] == the rest of 'input'
     */
    public static String[] extractNumberFrom( String input) {
        Pattern pat = Pattern.compile("^[,\\s]*(-?(([\\d]+\\.[\\d]+)|([\\d]+)|(\\.\\d+))([Ee]-?\\d+)?)(.*)$");
        Matcher m = pat.matcher(input);
        String res[] = new String[2];
        if( m.matches()) {
            res[0] = m.group(1).isEmpty() ? m.group(2) : m.group(1);
            res[1] = m.group(7);
        } else {
            res[1] = input;
        }       
        return res;
    }
    
    /**
     * Taken from internet.
     * 
     * @param startPoint
     * @param rx
     * @param ry
     * @param theta
     * @param largeArcFlag
     * @param sweepFlag
     * @param x
     * @param y
     * @return 
     */
    public static final GElement arcTo(GCode startPoint, float rx, float ry, float theta, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            
            // Ensure radii are valid
            if (rx == 0 || ry == 0) return new G1Path("arc"+GElement.getUniqID());
                  
            // Get the current (x, y) coordinates of the path
            Point2D p2d = startPoint;
            float x0 = (float) p2d.getX();
            float y0 = (float) p2d.getY();
            // Compute the half distance between the current and the final point
            float dx2 = (x0 - x) / 2.0f;
            float dy2 = (y0 - y) / 2.0f;
            // Convert theta from degrees to radians
            theta = (float) Math.toRadians(theta % 360f);

            //
            // Step 1 : Compute (x1, y1)
            //
            float x1 = (float) (Math.cos(theta) * (double) dx2 + Math.sin(theta)
                            * (double) dy2);
            float y1 = (float) (-Math.sin(theta) * (double) dx2 + Math.cos(theta)
                            * (double) dy2);
            // Ensure radii are large enough
            rx = Math.abs(rx);
            ry = Math.abs(ry);
            float Prx = rx * rx;
            float Pry = ry * ry;
            float Px1 = x1 * x1;
            float Py1 = y1 * y1;
            double d = Px1 / Prx + Py1 / Pry;
            if (d > 1) {
                    rx = Math.abs((float) (Math.sqrt(d) * (double) rx));
                    ry = Math.abs((float) (Math.sqrt(d) * (double) ry));
                    Prx = rx * rx;
                    Pry = ry * ry;
            }

            //
            // Step 2 : Compute (cx1, cy1)
            //
            double sign = (largeArcFlag == sweepFlag) ? -1d : 1d;
            float coef = (float) (sign * Math
                            .sqrt(((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                                            / ((Prx * Py1) + (Pry * Px1))));
            float cx1 = coef * ((rx * y1) / ry);
            float cy1 = coef * -((ry * x1) / rx);

            //
            // Step 3 : Compute (cx, cy) from (cx1, cy1)
            //
            float sx2 = (x0 + x) / 2.0f;
            float sy2 = (y0 + y) / 2.0f;
            float cx = sx2 + (float) (Math.cos(theta) * (double) cx1 - Math.sin(theta)
                                            * (double) cy1);
            float cy = sy2 + (float) (Math.sin(theta) * (double) cx1 + Math.cos(theta)
                                            * (double) cy1);

            //
            // Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
            //
            float ux = (x1 - cx1) / rx;
            float uy = (y1 - cy1) / ry;
            float vx = (-x1 - cx1) / rx;
            float vy = (-y1 - cy1) / ry;
            float p, n;
            // Compute the angle start
            n = (float) Math.sqrt((ux * ux) + (uy * uy));
            p = ux; // (1 * ux) + (0 * uy)
            sign = (uy < 0) ? -1d : 1d;
            float angleStart = (float) Math.toDegrees(sign * Math.acos(p / n));
            // Compute the angle extent
            n = (float) Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
            p = ux * vx + uy * vy;
            sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
            float angleExtent = (float) Math.toDegrees(sign * Math.acos(p / n));
            if (!sweepFlag && angleExtent > 0) {
                    angleExtent -= 360f;
            } else if (sweepFlag && angleExtent < 0) {
                    angleExtent += 360f;
            }
            angleExtent %= 360f;
            angleStart %= 360f;

            Arc2D.Float arc = new Arc2D.Float();
            arc.x = cx - rx;
            arc.y = cy - ry;
            arc.width = rx * 2.0f;
            arc.height = ry * 2.0f;
            arc.start = -angleStart;
            arc.extent = -angleExtent;
            
            if ( rx == ry) {
                // Create GArc                
                return new GArc("arc", new GCode(cx,cy), rx, angleStart, -angleExtent);
                        
            } else {
                Path2D path = new Path2D.Double();
                path.moveTo((float)startPoint.getX(), (float)startPoint.getY());                        
                path.append(arc, true);
                return G1Path.makeFromShape( "ea"+GElement.getUniqID(), path, "").get(0);
            }
    }
    
    
    public void saveDocument(String filename) throws IOException
    {
        GCode lastPoint = null;
        java.io.FileWriter fw;

        fw = new java.io.FileWriter(filename);
        fw.append(SVGE_HEADER + SVGE_RELEASE + ")\n");
        if ( backgroundPictureParameter != null) 
            fw.append(backgroundPictureParameter.toString()+"\n");
        fw.append(document.properties.toString()+"\n");
        for( GElement e : document.getIterable()) 
            lastPoint = e.saveToStream(fw, lastPoint);
        
        fw.close();
        documentHasChanged=false;
        setName( filename);
    }
        
    @Override
    public void mouseClicked(MouseEvent e) {
        if ( ignoreClick) {
            ignoreClick = false;
            return;
        }
        
        if ( e.getButton() == 1) {
            if (mouseMode==MOUSE_MODE_SET_2D_CURSOR) {
            }
            if ( (e.getClickCount() == 1) ) { // point/block selection
                
                if (editedElement != null) { // point selection
                    GCode sel =  editedElement.getCloserPoint(screenToCoordPoint(e.getX(), e.getY()), 10 / zoomFactor, null, false);
                    if ((e.getModifiersEx() & (MouseEvent.CTRL_DOWN_MASK|MouseEvent.SHIFT_DOWN_MASK)) != 0) // add/remove from/to selection
                    {
                        if (sel != null) {
                            if ( selectedPoints.contains(sel)) { 
                                if ( (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == 0) selectedPoints.remove(sel); 
                                selectionHasChanged=true;
                                repaint();
                            }
                            else 
                                addSelectedPoints(sel); 
                        }
                    } 
                    else // change selection
                        if ( (sel != null) && ! selectedPoints.contains(sel)) {
                            clearSelectedPoints();
                            addSelectedPoints(sel); 
                        }
                }
                else    // block selection
                {
                    if ( mouseMode != MOUSE_MODE_NONE) return;
                    GElement sel = editedGroup.getElementFromPoint(screenToCoordPoint(e.getX(), e.getY()), 10 / zoomFactor, null);

                    if (ctrlDown | shiftDown) // add/remove from/to selection
                    {
                        if (sel != null) {
                            if ( selectedElements.contains(sel)) {
                                if (ctrlDown) {
                                    selectedElements.remove(sel);
                                    selectionHasChanged=true; 
                                } 
                            }
                            else {                               
                                selectedElements.add(sel); selectionHasChanged=true; 
                                selectionHasChanged=true; 
                            }
                            if ( selectionHasChanged) repaint();
                        }
                    } 
                    else // change selection
                        if ( (sel != null) && ! selectedElements.contains(sel)) {
                            selectedElements.clear();
                            selectedElements.add(sel); 
                            selectionHasChanged=true;
                            repaint();
                        }
                }

            } else { // (e.getClickCount() != 1)
                
                if ( (e.getModifiersEx() & (MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK)) == 0)
                    setEditedElement(editedGroup.getElementFromPoint(screenToCoordPoint(e.getX(), e.getY()), 10 / zoomFactor, null));         
                
            }
        } else // button != BUTTON1
            if ( e.getButton() == MouseEvent.BUTTON3) {
                switch ( mouseMode) {                    
                    case MOUSE_MODE_ADD_CURVE:
                    case MOUSE_MODE_ADD_LINES:
                        if ( (editedElement != null) && (editedElement.isEmpty())) editParentOrClearSelection();
                    case MOUSE_MODE_ADD_CIRCLES:
                    case MOUSE_MODE_ADD_G23_ARC:    
                    case MOUSE_MODE_QUICK_MOVE:
                        clearMouseMode();
                        break;
                    default:
                        editParentOrClearSelection();
                }
            }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        ctrlDown  = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
        shiftDown = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
        altDown   = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;
        
        setKeyFocus(true);      
            
        if (e.getButton() == MouseEvent.BUTTON2) {
            // translate view
            ox=dx;
            oy=dy;
            modeBeforDragViewMode = mouseMode;
            mouseMode = MOUSE_MODE_DRAG_VIEW;
            screenMousePressPosition = e.getPoint();              
                
        } else switch (e.getButton()) { 
              
            case MouseEvent.BUTTON3: // quick add+move a point in editElement ?    
                if (mouseMode != MOUSE_MODE_NONE) return;
                
                if ((editedElement != null) && (editedElement.getDistanceTo(screenToCoordPoint(e.getPoint())) < 10 / zoomFactor)) {    
                    clearSelectedPoints();
                    
                    final GCode pt = getCoordSnapPointFor(e.getX(), e.getY());
                    
                    GCode p = editedElement.insertPoint(new GCode( e.isControlDown() ? 5 : 1, pt.getX(),pt.getY()));
                    if ( p != null) {
                        coordMouseOrigin = highlitedPoint = p;
                        snapWithoutSelection = true;
                        mouseMode = MOUSE_MODE_QUICK_MOVE;
                        addSelectedPoints(p);
                        break;
                    }
                }  
                return;
                
            case MouseEvent.BUTTON1:

                switch (mouseMode) {
                    case MOUSE_MODE_FOCUS: // Start the focus (zoom) rectangle
                        snapWithoutSelection=false;
                        screenMousePressPosition = e.getPoint();
                        break;
                        
                    case MOUSE_MODE_ADD_G23_ARC:
                    case MOUSE_MODE_ADD_LINES:  
                        snapWithoutSelection=false;
                        if ( coordMouseOrigin == null)
                            coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()); 
                        
                        if ( editedElement.getFirstPoint() == null)
                            editedElement.add( (GCode)coordMouseOrigin.clone());
                        break;
                        
                    case MOUSE_MODE_ADD_CURVE:
                        snapWithoutSelection=false;
                        if ( coordMouseOrigin == null) {
                            screenMousePressPosition = coordToScreenPoint(coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()));
                            inform("choose dimension");
                        } 
                        break;
                        
                    case MOUSE_MODE_ADD_OVAL:
                    case MOUSE_MODE_ADD_CIRCLES:
                    case MOUSE_MODE_ADD_RECTANGLES:
                        snapWithoutSelection=false;
                        screenMousePressPosition = coordToScreenPoint(coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()));
                        inform("choose dimension (use Alt,Ctrl,Shift)");
                        break;
                        
                    case MOUSE_MODE_SHOW_ANGLE:
                        snapWithoutSelection=false;
                        screenMousePressPosition = coordToScreenPoint(coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()));
                        lastRotationAngle = (360 + Math.toDegrees(-G1Path.getAngleInRadian(coord2DCursor, coordMouseOrigin)))%360;
                        invalidateWithoutUpdateGCodeListViewer();
                        inform("move mouse to show angle");
                        break;
                        
                    case MOUSE_MODE_CURSOR_AT_CENTER:
                        snapWithoutSelection=false;
                        if ( coordMouseOrigin == null) {
                            screenMousePressPosition = coordToScreenPoint(coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()));
                            invalidateWithoutUpdateGCodeListViewer();
                            inform("ckick to 2nd point");
                        } else {
                            GCode dest = getCoordSnapPointFor(e.getX(), e.getY());
                            double rx = Math.abs(coordMouseOrigin.getX()+dest.getX())/2;
                            double ry = Math.abs(coordMouseOrigin.getY()+dest.getY())/2;
                            set2DCursorTo(rx, ry);
                            mouseMode = MOUSE_MODE_NONE_AT_RELEASE;
                        }
                        break;
                        
                    case MOUSE_MODE_SHOW_DISTANCE:
                        snapWithoutSelection=false;
                        screenMousePressPosition = coordToScreenPoint(coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY()));
                        invalidateWithoutUpdateGCodeListViewer();
                        inform("move mouse to show distance");
                        break;
                        
                    case MOUSE_MODE_CHOOSE_MOVE_ORIGIN:                       
                        clearMouseMode();
                        mouseMode=MOUSE_MODE_MOVE_SEL;
                        snapWithoutSelection=false;
                        coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY());
                        inform("");
                        break;
                        
                    case MOUSE_MODE_ROTATION:
                        lastRotationAngle = G1Path.getAngleInRadian(transformationOrigin, getCoordSnapPointFor(e.getX(), e.getY()));
                        if ( shiftDown)
                            lastRotationAngle = Math.toRadians(((int)(Math.toDegrees(lastRotationAngle)/10))*10);
                        finalRotationAngle=0;
                        break;
                        
                    case MOUSE_MODE_SCALE:
                        Point2D mp = getCoordSnapPointFor(e.getX(), e.getY());
                        scaleNormaleX = transformationOrigin.getX()-mp.getX();
                        scaleNormaleY = transformationOrigin.getY()-mp.getY();
                        if ( scaleNormaleX == 0) scaleNormaleX = scaleNormaleY;
                        if ( scaleNormaleY == 0) scaleNormaleY = scaleNormaleX;
                        inform("drag mouse to resize the selection");
                        lastScaleRatioX = lastScaleRatioY = 1;
                        break;
                        
                    case MOUSE_MODE_MOVE_GANTRY:
                        if ( e.getButton() != 1) clearMouseMode();
                    case MOUSE_MODE_SET_2D_CURSOR:
                        break;
                        
                    default:
                        screenMousePressPosition = e.getPoint();
                        // startMove or alter shape selection
                        if (editedElement==null)
                        { 
                            
                            if ( ! (shiftDown | ctrlDown)) {
                                if ( editedElement != null) clearFarEditedPointsFrom(e.getPoint()); // clear selected points
                                else clearFarSelectionFrom(e.getPoint()); // clear selected blocks
                                invalidate();
                            }
                            GElement p;  
                            
                            if ( ! (shiftDown | ctrlDown) &&
                                 ((p=editedGroup.getElementFromPoint(screenToCoordPoint(e.getX(), e.getY()),
                                         7 / zoomFactor, selectedElements))!=null)) {
                                if ( selectedElements.isEmpty()) selectedElements.add(p);
                                coordMouseOrigin = getCoordSnapPointFor(e.getX(), e.getY());
                                selectedPoints.clear(); // set point discarded in translate calculation
                                GCode pt = p.getCloserPoint(screenToCoordPoint(e.getX(), e.getY()), 7 / zoomFactor, null, false);
                                if ( (pt != null) && (p.contains(pt))) // is it a real point ?
                                    selectedPoints.add( pt);
                                snapWithoutSelection = true;
                                mouseMode = MOUSE_MODE_MOVE_SEL;
                                selectionHasChanged=true;
                                invalidate();                              
                            }
                        }                                                
                        else if ((e.getModifiersEx()&(MouseEvent.CTRL_DOWN_MASK|MouseEvent.SHIFT_DOWN_MASK))==0) {
                            // if no Shift nor Ctrl key pressed, try to initiate a translation of selection
                            
                            coordMouseOrigin=null;
                            double d, dmax = 10 /zoomFactor;
                            final Point2D clickP = screenToCoordPoint(e.getPoint());
                            for ( GCode p : selectedPoints) {
                                if ( (d=p.distance(clickP)) <= dmax) {
                                    coordMouseOrigin = p;
                                    dmax = d;
                                }
                            }    
                            if ( coordMouseOrigin != null) {
                                mouseMode = MOUSE_MODE_MOVE_SEL;
                                snapWithoutSelection=true;
                                setCursor(crossCursor);
                            } else {
                                clearSelectedPoints();
                                // translate highlited Point ?
                                if ( highlitedPoint != null ) {
                                    coordMouseOrigin = highlitedPoint;
                                    mouseMode = MOUSE_MODE_QUICK_MOVE;
                                    addSelectedPoints(highlitedPoint);
                                    snapWithoutSelection=true;
                                    setCursor(crossCursor);
                                    break;
                                }
                            }
                        }     
                }
                break;
        default:
            //System.err.println("Unknow button : " + e.getButton());  
        }
    }
    
    
   /* public void exportToDXF()
    {
        DXFDocument d = new DXFDocument();
        
    }*/

    @Override
    public void mouseReleased(MouseEvent e) { 
        boolean dontIgnoreClick = false;
        mousePressed=false;
        snapWithoutSelection=false;
        ignoreClick = false;
        
        if ((e.getButton() != MouseEvent.BUTTON1) && ! mouseDragged) return;
        mouseDragged=false;
        
        if ( e.getClickCount() > 1) return;
       
        switch (mouseMode) {
            
            case MOUSE_MODE_SET_2D_CURSOR:
                snapWithoutSelection=false;
                coord2DCursor = getCoordSnapPointFor(e.getX(), e.getY());
                invalidateWithoutUpdateGCodeListViewer();
                inform(String.format("Cursor at %.5f x %.5f",coord2DCursor.getX(),coord2DCursor.getY()));
                mouseMode = MOUSE_MODE_NONE;
                ignoreClick=true;
                break;
                        
            case MOUSE_MODE_ADD_OVAL:
                GCode dest = getCoordSnapPointFor(e.getX(), e.getY());
                double rx, ry;
                rx = (dest.getX()-coordMouseOrigin.getX());
                ry = (dest.getY()-coordMouseOrigin.getY());
                
                if ( (rx != 0) || (ry != 0)) {
                    if ( shiftDown || (rx==0) || (ry==0)) {
                         if ( Math.abs(rx) > Math.abs(ry)) ry = -rx;
                         else rx = -ry; 
                    }
                
                    if ( ctrlDown) add(altDown ? G1Path.makeOval(dest, rx, ry, 0.5)
                                               : G1Path.makeOval(coordMouseOrigin, rx, ry, 0.5));
                    else {  
                        if ( ! shiftDown & altDown) {
                            GCode center = new GCode((dest.getX()+coordMouseOrigin.getX())/2,
                                                       (dest.getY()+coordMouseOrigin.getY())/2);
                            double radius = dest.distance(coordMouseOrigin)/2;
                            add(G1Path.makeOval(center, radius, radius, 0.5));
                        } else
                        add(G1Path.makeOval(new GCode(coordMouseOrigin.getX()+rx/2, coordMouseOrigin.getY()+ry/2), rx/2, ry/2, 0.5));
                    }
                }
                screenMousePressPosition= mouseRectangleP2 = null;
                break;
            case MOUSE_MODE_ADD_CIRCLES :
                dest = getCoordSnapPointFor(e.getX(), e.getY());
                if ( ctrlDown) {
                    rx = ry = coordMouseOrigin.distance(dest);
                }
                else {
                    rx = Math.abs(coordMouseOrigin.getX()-dest.getX());
                    ry = Math.abs(coordMouseOrigin.getY()-dest.getY());
                }
                if ( (shiftDown & ! ctrlDown) || (rx==0) || (ry==0)) {
                         if ( rx > ry) ry = rx;
                         else ry = rx; 
                }
                
                if ( shiftDown & ! ctrlDown) {
                    rx = coordMouseOrigin.distance(dest)/2;
                    GCode center = new GCode( coordMouseOrigin.getX() + (dest.getX() -coordMouseOrigin.getX())/2,
                                                coordMouseOrigin.getY() + (dest.getY() - coordMouseOrigin.getY())/2);
                    add(G1Path.makeOval(center, rx, rx, 0.5));
                } else if ( (rx != 0) || (ry != 0)) 
                    add(! altDown ? G1Path.makeOval(dest, rx,ry, 0.5)
                                 : G1Path.makeOval(coordMouseOrigin, rx, ry, 0.5));
                    
                screenMousePressPosition=mouseRectangleP2=null;
                inform("Press and drag +[Alt,Ctrl,Shift], (right to quit)");
                break;
            case MOUSE_MODE_ADD_CURVE:
                GSpline curve;
                dest = getCoordSnapPointFor(e.getX(), e.getY());
                if( dest.isAtSamePosition(coordMouseOrigin)) break;
                
                curve = new GSpline("curve", new GCode(coordMouseOrigin), null, dest);
                if ( editedElement instanceof GMixedPath) {
                    ((GMixedPath)editedElement).add(curve);
                    coordMouseOrigin = dest;
                    saveState(true);
                } else {
                    add( curve);
                    clearMouseMode();
                    setEditedElement(curve);
                }            
                break;
                
            case MOUSE_MODE_ADD_RECTANGLES :
                inform("Press and drag, (right to quit)");
                if ( mouseRectangleP2 == null) break;
                Rectangle2D.Double r = new Rectangle2D.Double(coordMouseOrigin.getX(), coordMouseOrigin.getY(), 0, 0);  
                GCode p2 = getCoordSnapPointFor(mouseRectangleP2.x, mouseRectangleP2.y);
                r.add(p2 );
                if ( shiftDown ) 
                    if ( r.height > r.width) r.width = r.height; 
                    else r.height = r.width;
                if( ctrlDown) {                
                    double w = Math.abs(coordMouseOrigin.getX()-p2.getX());
                    double h = Math.abs(coordMouseOrigin.getY()-p2.getY());
                    if ( shiftDown) { if ( w > h) h=w; else w=h; }
                    if ( altDown)
                        r = new Rectangle2D.Double(p2.getX() - w, p2.getY() - h, w*2, h*2);
                    else
                        r = new Rectangle2D.Double(coordMouseOrigin.getX() - w, coordMouseOrigin.getY() - h, w*2, h*2);
                }
                add(G1Path.newRectangle(new GCode(r.x, r.y), new GCode(r.x+r.width, r.y+r.height)));                                      
                mouseRectangleP2 = null;
                break;  
                
            case MOUSE_MODE_NONE_AT_RELEASE: 
                clearMouseMode();
                break;
                
            case MOUSE_MODE_SHOW_ANGLE:
            case MOUSE_MODE_SHOW_DISTANCE :
                if ( e.getButton()!=MouseEvent.BUTTON1) clearMouseMode();
                invalidateWithoutUpdateGCodeListViewer();
                break;           
                
            case MOUSE_MODE_ADD_G23_ARC:
                if ( e.getButton() == 1) {    
                    GCode pt = getCoordSnapPointFor(e.getX(), e.getY());   
                    assert( editedElement instanceof GMixedPath);
                    
                    GCode lp = editedElement.getLastPoint();
                    if ( ! lp.isAtSamePosition(pt)) {                                            
                        double radius = pt.distance(coordMouseOrigin) / 2;
                        GCode center = GCode.getMiddlePoint(coordMouseOrigin, pt);
                        GArc arc = new GArc("", center, radius, 
                                    GCode.getAngleInDegre(center, coordMouseOrigin), 180);
//                        GCode p1 = arc.getFirstPoint();
//                        double dx1 = center.getX() - p1.getX();
//                        double dy1 = center.getY() - p1.getY();
//                        arc.translate(coordMouseOrigin.getX()+ dx1, coordMouseOrigin.getY()+dy1);
                        if (((GMixedPath)editedElement).add(arc)) {
                            saveState(false);
                            coordMouseOrigin = pt;
                        }
                    }                  
                }
                break;
                
            case MOUSE_MODE_ADD_LINES:
                if ( e.getButton() == 1) {                   
                    GCode pt = getCoordSnapPointFor(e.getX(), e.getY());                
                    // add new point if in new position
                    GCode lp = editedElement.getLastPoint();
                    if ( ! lp.isAtSamePosition(pt)) {
                        editedElement.add(new GCode(pt.getX(), pt.getY()));
                        saveState(false);
                    }  
                }
                break;  
                
            case MOUSE_MODE_ROTATION:
                finalRotationAngle=Double.NaN;
                saveState(true);
                clearMouseMode();
                break;  
                
            case MOUSE_MODE_SCALE:
                saveState(true);
                clearMouseMode();
                break; 
                
            case MOUSE_MODE_MOVE_SEL:
                clearMouseMode();
                if ( stateHasChanged) saveState(true);
                else return;
                break;   
                
            case MOUSE_MODE_DRAG_VIEW:
                //clearMouseMode();
                mouseMode = modeBeforDragViewMode;
                repaint();
                break;
                
            case MOUSE_MODE_QUICK_MOVE:
                clearMouseMode();
                if ((selectedPoints.size()==1) && ( highlitedPoint == selectedPoints.get(0))) {
                    clearSelectedPoints();
                    dontIgnoreClick = true;
                }
                if ( stateHasChanged) saveState(false);
                break;
                
            case MOUSE_MODE_FOCUS:
                GCode p = screenToCoordPoint(e.getX(), e.getY());
                r = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
                r.add(screenToCoordPoint(screenMousePressPosition));
                if ((r.getWidth() > 1) && (r.getHeight() > 1)) focusToRect(r);
                clearMouseMode();
                break;
                
            case MOUSE_MODE_SET_MPOS:
                listener.setVirtualMachinePosition(getCoordSnapPointFor(e.getX(), e.getY()));
                clearMouseMode();
                break;
                
            case MOUSE_MODE_MOVE_GANTRY:
                listener.moveGantry(getCoordSnapPointFor(e.getX(), e.getY()));                
                break;
                
            default:
                if ( mouseRectangleP2 != null) 
                {
                    // end of rectangular selection
                    if ( (e.getModifiersEx()&(MouseEvent.CTRL_DOWN_MASK|MouseEvent.SHIFT_DOWN_MASK))==0)
                        selectedElements.clear();

                    p = screenToCoordPoint(e.getX(), e.getY());
                    r = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
                    r.add(screenToCoordPoint(screenMousePressPosition));
                    if ( editedElement != null) {
                        // find new points selected
                        for( GCode pt : editedElement.getPointsIterator()) 
                            if ( r.contains(pt)) {
                                if ( selectedPoints.contains(pt) &&
                                         ((e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK)==MouseEvent.CTRL_DOWN_MASK) ) {
                                            selectedPoints.remove(pt);  selectionHasChanged=true;

                                } else {
                                       selectedPoints.add(pt); 
                                       selectionHasChanged=true;
                                }
                            }

                    } else {
                        // find new elements selected
                        for( GElement el : editedGroup.getIterable()) {
                            Rectangle2D r2 = el.getBounds();
                            if ( r2 == null) continue;
                            //System.out.println(s.getID() + " = " + r2);
                            if ( r.contains(r2) || ((r2.getWidth()==0 && r2.getHeight()==0) && r.contains(new Point2D.Double(r2.getX(), r2.getY())))) {
                                if ( selectedElements.contains(el) &&
                                     ((e.getModifiersEx()&MouseEvent.CTRL_DOWN_MASK)==MouseEvent.CTRL_DOWN_MASK) ) {
                                        selectedElements.remove(el);  selectionHasChanged=true;

                                } else {
                                   selectedElements.add(el);  selectionHasChanged=true;
                                }
                            }
                        }
                    }
                    mouseRectangleP2=null;
                    
                } else {      
                    // unselect all
                    if ((e.getModifiersEx()&(MouseEvent.CTRL_DOWN_MASK|MouseEvent.SHIFT_DOWN_MASK))==0) {
                            if ( editedElement != null) 
                                clearFarEditedPointsFrom(e.getPoint()); // clear selected points
                            else 
                                clearFarSelectionFrom(e.getPoint());   
                        invalidate();
                    }
                    if ( ! selectionHasChanged) return;
                }
        }
        e.consume();
        ignoreClick= ! dontIgnoreClick;
        if (selectionHasChanged) invalidate();
        else repaint();
        if ( mouseMode == MOUSE_MODE_NONE) setCursor(Cursor.getDefaultCursor());
    }
    
    /** Clear selected elements if p is to far of them.
     * @param screenPoint a point of the screen */
    public void clearFarSelectionFrom(java.awt.Point screenPoint)
    {
        if ( selectedElements.isEmpty()) return;
        GCode p = screenToCoordPoint(screenPoint);

        boolean clearSel=true;
        for( GElement s : selectedElements) {
            if ( s.getDistanceTo(p) < (7/zoomFactor))
                clearSel = false;
        }
        if ( clearSel) 
        {
            selectedElements.clear(); selectionHasChanged=true;
        }
    }
    
    public void clearSelectedPoints() {
        if ( selectedPoints.isEmpty()) return;
        selectedPoints.clear();
        selectionHasChanged=true;
    }
    
    /** Clear selected points if p is to far of all of them.
     * @param screenPoint a point on the screen */
    public void clearFarEditedPointsFrom(java.awt.Point screenPoint)
    {
        GCode p = screenToCoordPoint(screenPoint);
        
        boolean clearSel= ! selectedPoints.isEmpty();
        for( GCode sel : editedElement.getPointsIterator()) 
            if ( p.distance(sel) < (7/zoomFactor)) 
                    clearSel = false;
        
        if ( clearSel) clearSelectedPoints();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    
        if ( mouseMode != MOUSE_MODE_NONE) setCursor(crossCursor);
        mouseDragged = true;
        coordSnapPosition = getCoordSnapPointFor(e.getX(), e.getY());
        screenMousePosition = (mouseMode==MOUSE_MODE_NONE) ? e.getPoint() 
                                : coordToScreenPoint(coordSnapPosition);
        
        switch (mouseMode) {
            case MOUSE_MODE_DRAG_VIEW: // translate view position
                dx = ox + (int)((e.getX() - screenMousePressPosition.getX()));
                dy = oy + (int)((e.getY() - screenMousePressPosition.getY()));
            case MOUSE_MODE_ADD_CURVE:
            case MOUSE_MODE_ADD_LINES:
                invalidateWithoutUpdateGCodeListViewer();
                return;
            case MOUSE_MODE_ADD_RECTANGLES:
                mouseRectangleP2 = coordToScreenPoint(getCoordSnapPointFor(e.getX(), e.getY()));
                invalidateWithoutUpdateGCodeListViewer();
                break;
            case MOUSE_MODE_SHOW_ANGLE:                      
                invalidateWithoutUpdateGCodeListViewer();
                inform("Angle = " + computeAngle());
                break;
            case MOUSE_MODE_SHOW_DISTANCE:
                inform("Distance = " + GWord.roundForGCODE(coordMouseOrigin.distance(coordSnapPosition)));
            case MOUSE_MODE_CURSOR_AT_CENTER:   
                invalidateWithoutUpdateGCodeListViewer();  
                break;
            case MOUSE_MODE_ROTATION:
                double a = G1Path.getAngleInRadian(transformationOrigin, coordSnapPosition);
                if ( ( (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) )
                    a = Math.toRadians((Math.round(Math.toDegrees(a))));
                
                if ( editedElement == null) {
                    for ( GElement s : selectedElements) {
                        s.rotate(transformationOrigin, a - lastRotationAngle);
                    } 
                
                } else {                   
                    for ( GCode p : selectedPoints) {
                        final double d = GCode.distance(p.getX(), p.getY(), transformationOrigin.getX(), transformationOrigin.getY());
                        final double an = GCode.getAngle(transformationOrigin, p)- lastRotationAngle;
                        final double x = Math.cos(a+an)*d+transformationOrigin.getX()-p.getX();
                        final double y = Math.sin(a+an)*d+transformationOrigin.getY()-p.getY();
                        if ( (x != 0) && (y != 0))
                            editedElement.movePoint(p, x, y);
                    }
                }
                
                finalRotationAngle += a - lastRotationAngle;
                lastRotationAngle = a;
                invalidate();
                inform("Angle = "+ Math.rint(Math.toDegrees(finalRotationAngle)*100)/100.);
                break;
                
            case MOUSE_MODE_SCALE:
                double rx = transformationOrigin.getX() - coordSnapPosition.getX();
                double ry = transformationOrigin.getY() - coordSnapPosition.getY();
                double ratioX = Math.abs(rx/scaleNormaleX);
                double ratioY = Math.abs(ry/scaleNormaleY);
                if ( selectionContainsArc || ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) || (ratioX<0.0001) || (ratioY<0.0001)) {
                    if ( ratioX > ratioY) ratioY = ratioX;
                    else ratioX = ratioY;
                }   
                if ( Math.abs(ratioX) < 0.0001) ratioX = 0.0001;
                if ( Math.abs(ratioY) < 0.0001) ratioY = 0.0001;
                if ((ratioX==lastScaleRatioX) && (ratioY==lastScaleRatioY)) return;
                
                if ( editedElement == null) {                  
                    for ( GElement s : selectedElements) {
                        s.scale(transformationOrigin, ratioX/lastScaleRatioX, ratioY/lastScaleRatioY);
                    }                        
                } else {
                    // scale points
                    for ( GCode p : selectedPoints) {
                        double d = transformationOrigin.distance(p);
                        double an = GElement.getAngleInRadian(transformationOrigin, p);
                        editedElement.movePoint(p, 
                                transformationOrigin.getX() + Math.cos(an)*d*ratioX/lastScaleRatioX - p.getX(),
                                transformationOrigin.getY() + Math.sin(an)*d*ratioY/lastScaleRatioY - p.getY());
                    }
                }                
                lastScaleRatioX = ratioX;
                lastScaleRatioY = ratioY;        
                invalidateWithoutUpdateGCodeListViewer();
                inform("ratio ( "+String.format("%.5f",ratioX)+" , "+String.format("%.5f",ratioY)+" )");
                break;
            case MOUSE_MODE_QUICK_MOVE:
                if ( selectedPoints.isEmpty())
                    selectedPoints.add(highlitedPoint);
                double len = editedElement.getLenOfSegmentTo(highlitedPoint);
                    inform("Point n° "+ editedElement.getIndexOfPoint(highlitedPoint) + 
                            (Double.isNaN(len)?"":" len="+String.format("%.5f",len)));
            case MOUSE_MODE_MOVE_SEL:
                if ( coordSnapPosition.distance(coordMouseOrigin) > 10e-8) {
                    moveCopySelection(coordSnapPosition.getX() - coordMouseOrigin.getX(),
                            coordSnapPosition.getY() - coordMouseOrigin.getY(), 0, false, false, false);
                                                           
                    coordMouseOrigin = coordSnapPosition;
                }
                break;
            default:
                
                if ( (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    
                    // rectangle selection ?
                    if ( (selectedElements.isEmpty() && (selectedPoints.isEmpty())) ||
                            (e.getModifiersEx()&(MouseEvent.CTRL_DOWN_MASK|MouseEvent.SHIFT_DOWN_MASK))!=0) {
                        mouseRectangleP2 = (mouseMode==MOUSE_MODE_NONE)?e.getPoint() : coordToScreenPoint(getCoordSnapPointFor(e.getX(), e.getY()));
                    }
                    invalidateWithoutUpdateGCodeListViewer();
                }
        }
        if ( listener != null) 
            listener.updateMouseCoord(e.getX(), e.getY(), coordSnapPosition.getX(), coordSnapPosition.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) { 
        screenMousePosition = coordToScreenPoint(coordSnapPosition = getCoordSnapPointFor(e.getX(), e.getY()));

        switch (mouseMode) {
            case MOUSE_MODE_SHOW_ANGLE:
                if ( coordMouseOrigin!=null) inform("Angle = " + computeAngle());
                break;
            case MOUSE_MODE_SHOW_DISTANCE:
                if ( coordMouseOrigin!=null)
                    inform("Distance = " + GWord.roundForGCODE(coordMouseOrigin.distance(coordSnapPosition)));
                break;
            case MOUSE_MODE_ROTATION:
                if ( transformationOrigin != null) {
                    lastRotationAngle = G1Path.getAngleInRadian(transformationOrigin, coordSnapPosition);
                    if ( shiftDown)
                        lastRotationAngle = Math.toRadians((Math.round(Math.toDegrees(lastRotationAngle)/5))*5);
                    inform("Angle = " + Math.rint(Math.toDegrees(lastRotationAngle)*100)/100);
                }
                break;
        }
        
        if (editedElement != null) {
            GCode old = highlitedPoint;
            highlitedPoint = editedElement.getCloserPoint(screenToCoordPoint(e.getPoint()), 10 / zoomFactor, null, false);       
            if ( old != highlitedPoint) {
                if ( highlitedPoint != null)
                {
                    final int no = editedElement.getIndexOfPoint(highlitedPoint);
                    if ( no != -1) {
                        double len = editedElement.getLenOfSegmentTo(highlitedPoint);
                        inform("Move n° "+ no + 
                            (Double.isNaN(len)?"":((editedElement instanceof GArc)?"angle=":" len=")+String.format("%.5f",len)));
                    }
                } else inform("");
            }
        }
        if ( listener != null)
            listener.updateMouseCoord(e.getX(), e.getY(), coordSnapPosition.getX(), coordSnapPosition.getY());
        
        //if ( snapToGrid || snapToPoints || (mouseMode == MOUSE_MODE_ADD_POINTS)) invalidateWithoutUpdateGCodeListViewer();
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {        
        if ( mousePressed)  return;
        
        if( e.getWheelRotation() > 0) 
        {
            GCode center = screenToCoordPoint(getWidth()/2,getHeight()/2);            
            if ( zoomFactor > 0.03) zoomFactor /=1.2;    
            java.awt.Point p = coordToScreenPoint(center);
            dx += getWidth()/2 - p.getX();
            dy += getHeight()/2 - p.getY();
        }
        else { 
            GCode center = screenToCoordPoint(getWidth()/2,getHeight()/2);
            if (zoomFactor <= 200000 )zoomFactor *= 1.2;
            java.awt.Point p = coordToScreenPoint(center);
            dx += getWidth()/2 - p.getX();
            dy += getHeight()/2 - p.getY();
        }
        if ( zoomFactor > 1) inform( String.format("Zoom %.1f : 1",zoomFactor));
        else inform( String.format("Zoom 1 : %.1f",(1/zoomFactor)));
        
        // remplace screen mousePress if needed
        if (mouseMode== MOUSE_MODE_ADD_CURVE) {
            if (coordMouseOrigin!=null)
                screenMousePressPosition=coordToScreenPoint(coordMouseOrigin);
        }
        invalidateWithoutUpdateGCodeListViewer();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { 

        ctrlDown  = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
        shiftDown = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
        altDown   = (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0;
        
                //System.err.println("No key " + e);
                switch (e.getKeyChar()) {
                    case '-': // menu allready do it !
                        //doAction(ACTION_MOVE_UP, 0, null);
                        break;
                    case '+':
                        //(ACTION_MOVE_DOWN, 0, null);
                        break;
                    case '\n':
                        if ( selectedElements.size()==1) setEditedElement(selectedElements.get(0));
                        break;
                    default:
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_EQUALS:
                                    if ( (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK)==0) break;
                            case KeyEvent.VK_ADD: 
                                doAction(ACTION_MOVE_DOWN, 0, null);
                                break;
                            case KeyEvent.VK_SUBTRACT: 
                                doAction(ACTION_MOVE_UP, 0, null);
                                break;
                            default: 
                //                System.err.println("Key unknow : " + e);
                                return;
                        }
                }
        
        e.consume();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT: 
                    shiftDown = true; 
                    break;
            case KeyEvent.VK_ALT: 
            case KeyEvent.VK_ALT_GRAPH: 
                    altDown = true; 
                    break;
            case KeyEvent.VK_CONTROL: 
                    ctrlDown = true; 
                    break;
            default:
                return;
        }    
        invalidateWithoutUpdateGCodeListViewer();
    }
    
    @Override
    public void keyReleased(KeyEvent e) { 
        boolean changed = true;
        switch ( e.getKeyCode()) {
            //case KeyEvent.VK_MINUS:
            //            doAction(ACTION_MOVE_UP, 0, null);
            //            break;       
            case KeyEvent.VK_SHIFT: 
                        shiftDown = false; 
                        break;
            case KeyEvent.VK_ALT: 
            case KeyEvent.VK_ALT_GRAPH: 
                        altDown = false; 
                        inform(""); 
                        break;
            case KeyEvent.VK_CONTROL: 
                        ctrlDown = false; 
                        break;
            case KeyEvent.VK_ESCAPE:
                        editParentOrClearSelection();
                        break;
            default:
                changed = false;
        }
        if ( changed) invalidateWithoutUpdateGCodeListViewer();
        
        if ((e.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK))==KeyEvent.CTRL_DOWN_MASK) {

        } else if ((e.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK))==KeyEvent.SHIFT_DOWN_MASK) {

        } else switch ( e.getKeyCode()) {
            case KeyEvent.VK_X: 
                    doAction(ACTION_CUT, 0, null);
                    break;
            case KeyEvent.VK_V: 
                    doAction(ACTION_PASTE,  ( gCodeListViewer.getSelectedIndex() < 0)?0:gCodeListViewer.getSelectedIndex(), null);
                    break;
            case KeyEvent.VK_ESCAPE: 
                    setEditedElement(null);
                    break;
            case KeyEvent.VK_LEFT:  moveSelection(-1,0);
                    break;
            case KeyEvent.VK_RIGHT: moveSelection(1,0); 
                    break;
            case KeyEvent.VK_UP: moveSelection(0,1); 
                    break;
            case KeyEvent.VK_DOWN: moveSelection(0,-1); 
                    break;
            case KeyEvent.VK_DELETE: // delete point or element
                    doAction(ACTION_DELETE, 0, null);
                    break;       
            default:
                return;
        }
        e.consume();
    }

    private void moveSelection(int screenDx, int screenDy) {
        if ((screenDx == 0) && (screenDy == 0)) return;
        if ( editedElement != null)  {
            if (editedElement.movePoints(selectedPoints, screenDx/zoomFactor, screenDy/zoomFactor)) {
                invalidate();
            }
        } else
            if ( ! selectedElements.isEmpty()) {
                selectedElements.forEach((s) -> { s.translate(screenDx/zoomFactor, screenDy/zoomFactor); });
                invalidate();
            }
    }      
        
    /**
     * Translate selection
     * 
     * @param deltaX where NaN 
     * @param deltaY where NaN
     * @param nbCopies if 0 juste move selection
     * @param packed
     * @param grouped
     * @param saveState 
     */
    public void moveCopySelection( double deltaX, double deltaY, int nbCopies, boolean packed, boolean grouped, boolean saveState) {
        final double tx = Double.isNaN(deltaX) ? 0 : deltaX;
        final double ty = Double.isNaN(deltaY) ? 0 : deltaY;
        
        if ( editedElement != null)  {
            stateHasChanged |= (editedElement.movePoints(selectedPoints, tx, ty));  
        } else { 
            if ( selectedElements.isEmpty()) return;
            
            if ( nbCopies == 0) { // simple move
                selectedElements.forEach((s) -> { s.translate(tx, ty); });
                stateHasChanged=true;
            } else {
                ArrayList<GElement> copies = new ArrayList<>(nbCopies * (packed ? 1 : selectedElements.size()));
                double dX = 0, dY = 0;
                while( nbCopies-- > 0) {
                    dX += tx;
                    dY += ty;
                    GGroup g =null;
                    if ( packed) g = new GGroup("copy"+nbCopies);
                    for ( GElement s : selectedElements) { 
                        GElement c = s.clone();
                        c.translate(dX, dY);
                        if ( packed) g.add(c);
                        else copies.add(c);
                    }
                    if ( packed) copies.add(g);
                }
                if ( grouped) add( new GGroup("copies", copies));
                else addAll(copies);
                stateHasChanged=true;
            }
        }
        if ( stateHasChanged) {
            if ( saveState ) saveState(true);
            else invalidate();
        }
    }

    public static final int ACTION_ADD_AT_CENTER             = 10;
    public static final int ACTION_ADD_CIRCLES               = 20;
    public static final int ACTION_ADD_CURVE                 = 30;
    public static final int ACTION_ADD_G2G3_CIRCLE           = 40;
    public static final int ACTION_ADD_HULL                  = 50;
    public static final int ACTION_ADD_INTERSECTION_POINTS   = 60;
    public static final int ACTION_ADD_LINES                 = 70;
    public static final int ACTION_ADD_LINKED_PATHS          = 80;
    public static final int ACTION_ADD_MIXED_PATH            = 90; 
    public static final int ACTION_ADD_OVAL                  = 100;
    public static final int ACTION_ADD_POINTS_AT_HALF        = 110;
    public static final int ACTION_ADD_RECTANGLES            = 120;
    public static final int ACTION_ALIGN                     = 130;
    public static final int ACTION_CHANGE_START_POINT        = 140;
    public static final int ACTION_CONVERT_TO_MIXEDPATH      = 150;
    public static final int ACTION_COPY                      = 160;
    public static final int ACTION_CURSOR_AT_CENTER          = 170;
    public static final int ACTION_CURSOR_AT_HEAD            = 180;
    public static final int ACTION_CUT                       = 190;
    public static final int ACTION_DELETE                    = 200;
    public static final int ACTION_DISTANCE                  = 210;
    /** Insert a line in the current editedPath. */
    public static final int ACTION_INSERT                    = 220;
    public static final int ACTION_INVERSE_SEL               = 230; 
    public static final int ACTION_EXTRACT                   = 240;
    public static final int ACTION_FILTER                    = 250;
    public static final int ACTION_FLIP_G1GX                 = 260;
    public static final int ACTION_FLIP_H                    = 270;
    public static final int ACTION_FLIP_V                    = 280;
    public static final int ACTION_FOCUS_VIEW                = 290;
    public static final int ACTION_GROUP_UNGROUP             = 300;
    public static final int ACTION_JOIN                      = 310;
    public static final int ACTION_MAKE_FLATTEN              = 320;
    public static final int ACTION_MAKE_OFFSET_CUT           = 330;
    public static final int ACTION_MAKE_POCKET               = 340;
    public static final int ACTION_MAP_TEXT_TO_PATH          = 350;
    public static final int ACTION_MOVE                      = 360;
    public static final int ACTION_MOVE_DOWN                 = 370;
    public static final int ACTION_MOVE_GRBL_HEAD            = 380;
    public static final int ACTION_MOVE_MPOS                 = 390;
    public static final int ACTION_MOVE_UP                   = 400;
    public static final int ACTION_OPTIMIZE_MOVES            = 410;
    public static final int ACTION_PASTE                     = 420;
    public static final int ACTION_REDO                      = 430;
    public static final int ACTION_REMOVE_COMMENT            = 431;
    public static final int ACTION_REVERSE                   = 440;
    public static final int ACTION_ROTATE                    = 450;
    public static final int ACTION_SCALE                     = 460;
    public static final int ACTION_SELECT_ALL                = 470;
    public static final int ACTION_SET_2D_CURSOR             = 480;
    public static final int ACTION_SET_AS_FOOTER             = 490;
    public static final int ACTION_SET_AS_HEADER             = 500;
    public static final int ACTION_SHOW_ANGLE                = 510;
    public static final int ACTION_SHOW_GRID                 = 520;
    public static final int ACTION_SHOW_MOVES                = 530;
    public static final int ACTION_SHOW_OBJECT_SURFACE       = 540;
    public static final int ACTION_SHOW_WORKSPACE            = 550;
    public static final int ACTION_SIMPLIFY                  = 560;
    public static final int ACTION_SIMPLIFY_ANGLE            = 570;
    public static final int ACTION_UNDO                      = 580;

    
    public static final int ACTION_TEST = 10000;
    
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_TOP = 2;
    public static final int ALIGN_BOTTOM = 3;
    public static final int ALIGN_VERTICALY = 4;
    public static final int ALIGN_HORIZONTALY = 5;
    public static final int ALIGN_CENTER = 6;
     
    @SuppressWarnings("unchecked")
    public boolean doAction(int action, double param, Object object) {       
        Point2D center; 
        Point3D pt;  
                
        if (action != ACTION_SHOW_GRID) clearMouseMode();
        setKeyFocus(true);
        
        switch (action) {
            case ACTION_ADD_CIRCLES:
                selectedElements.clear();
                mouseMode=MOUSE_MODE_ADD_CIRCLES;
                inform("Press on first point and drag");
                setCursor(crossCursor);
                screenMousePressPosition=mouseRectangleP2=null;
                break;
                
            case ACTION_ADD_MIXED_PATH:
                clearMouseMode();
                GElement elem;
                add(elem = new GMixedPath("mix"));
                setEditedElement(elem);  
                // don't break here
            case ACTION_ADD_CURVE:
                if ((editedElement != null) && 
                    (editedElement instanceof GMixedPath) && 
                    (editedElement.getLastPoint() != null)) {
                        screenMousePressPosition = coordToScreenPoint(coordMouseOrigin=editedElement.getLastPoint());
                        inform("click at next point");
                } else {
                    screenMousePressPosition=mouseRectangleP2=null;
                    selectedElements.clear();
                    inform("Press on start point");
                }
                mouseMode=MOUSE_MODE_ADD_CURVE; 
                setCursor(crossCursor); 
                break;           
                
            case ACTION_ADD_HULL:
                if ( selectedElements.size() < 2) {
                    inform("Select at least 2 paths");
                    break;
                }
                
                final ArrayList<GCode> points = new ArrayList<>(100);
                GCode lastP=null, curP;
                for( GElement el : selectedElements) {
                    if ( el instanceof GGroup) {
                        ((GGroup)el).addAllPointForHull(points);
                        lastP = points.isEmpty() ? null : points.get(points.size()-1);
                    } else {

                        GElement el2 = el.flatten();
                        for ( GCode p : el2) {
                            if ( p.isAPoint() && (p.getG()==1)) {
                                if ( (lastP == null) || ! lastP.isAtSamePosition(p)) 
                                    points.add( lastP = p);
                            }
                        }
                    }
                }
                G1Path res2 = new G1Path("HullPath");
                
                JarvisMarchHull jm = new JarvisMarchHull((Point2D[])points.toArray(new GCode[points.size()]));
                Point2D[] hullRegion = jm.getHull();
                
                if ( hullRegion.length == 0) {
                    JOptionPane.showMessageDialog(this, "The hull region is empty.");
                    break;
                }
                for ( Point2D pt2 : hullRegion ) {
                    res2.add((GCode)pt2);
                }
                res2.add((GCode)hullRegion[0].clone());  
                
                add(res2);
                break;
                
            case ACTION_ADD_INTERSECTION_POINTS:
                if ( (selectedElements.size()>1) &&
                        (selectedElements.get(0) instanceof G1Path)) {
                    
                    G1Path el = (G1Path)selectedElements.get(0);
                    selectedElements.remove(el);
                    
                    ArrayList<GElement> tmp = new GGroup(selectedElements, false).flatten().toArray();
                    ArrayList<G1Path> others = new ArrayList<>( tmp.size());
                    for ( GElement e : tmp ) if (e instanceof G1Path) others.add((G1Path)e);
                    
                    ArrayList<GCode> pts = ((G1Path)selectedElements.get(0)).addIntersectionPointsWith(others);
                    if ( ! pts.isEmpty()) {                      
                        setEditedElement(selectedElements.get(0));
                        selectedPoints = pts;
                        saveState(true);  
                    }
                    inform(pts.size() + " point(s) added");
                    break;
                } else
                    return false;
                
            case ACTION_ADD_G2G3_CIRCLE:
                if ( (editedElement == null) || ! (editedElement instanceof GMixedPath)) {
                    editedGroup.add( elem = new GMixedPath("mix"+GElement.getUniqID()));
                    setEditedElement(elem);
                }     
                if (editedElement.getLastPoint() != null) {
                    screenMousePressPosition = coordToScreenPoint(coordMouseOrigin=editedElement.getLastPoint());
                    inform("click at end point");
                } else {
                    screenMousePressPosition=mouseRectangleP2=null;
                    selectedElements.clear();
                    inform("Press on first point and drag");
                }
                mouseMode=MOUSE_MODE_ADD_G23_ARC; 
                setCursor(crossCursor); 
                break;    
                
            case ACTION_ADD_LINES:
                if ( (editedElement==null) || !((editedElement instanceof G1Path) || (editedElement instanceof GMixedPath))) {                 
                    editedGroup.add( elem = new G1Path("lines"+GElement.getUniqID()));
                    setEditedElement(elem);
                }
                inform("Click on start point");
                setCursor(crossCursor);
                mouseMode=MOUSE_MODE_ADD_LINES;
                break;
                
            case ACTION_ADD_LINKED_PATHS:
                if ( selectedElements.size() > 1) {
                    add(G1Path.makeLinkedPath("linked-path", selectedElements));
                }
            case ACTION_ADD_OVAL:
                setEditedElement(null);
                mouseMode=MOUSE_MODE_ADD_OVAL;
                inform("Press on first rectangle corner and drag");
                setCursor(crossCursor);
                screenMousePressPosition=mouseRectangleP2=null;
                break;
                    
            case ACTION_ADD_POINTS_AT_HALF:
                if ( ! selectedPoints.isEmpty() && (editedElement != null)) {
            
                    if ( editedElement instanceof G1Path) {
                       selectedPoints = ((G1Path)editedElement).addAtHalf( selectedPoints);
                       
                    }
                    else if ( editedElement instanceof GMixedPath) {
                        selectedPoints = ((GMixedPath)editedElement).addAtHalf( selectedPoints);
                    }                    
                    
                    if ( ! selectedPoints.isEmpty()) {
                        inform( selectedPoints.size() + " point(s) added");
                        saveState(true);
                    }
                }
                break;
                
            case ACTION_ADD_RECTANGLES:
                setEditedElement(null);
                inform("Press on first point and drag");
                mouseMode=MOUSE_MODE_ADD_RECTANGLES;
                setCursor(crossCursor);
                repaint();
                break;
                    
            case ACTION_ALIGN:
                Rectangle2D r = null;
                double dest;
                              
                if ( isInEditMode() && ! selectedPoints.isEmpty()) {
                    // Align points in editMode
                    // get bounds of selected points
                    for ( GCode p : selectedPoints) 
                        if ( r == null) r = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
                        else r.add(p);
             
                    for ( GCode p : selectedPoints) {
                        switch ( (int)param) {
                        case ALIGN_LEFT: 
                            editedElement.movePoint(p, r.getMinX() - p.getX(), 0);
                            break;
                        case ALIGN_RIGHT:
                            editedElement.movePoint(p, r.getMaxX() - p.getX(), 0);
                            break;
                        case ALIGN_TOP:
                            editedElement.movePoint(p, 0, r.getMinY() - p.getY());
                            break;
                        case ALIGN_BOTTOM: 
                            editedElement.movePoint(p, 0, r.getMaxY() - p.getY());
                            break;
                        case ALIGN_HORIZONTALY:   
                            editedElement.movePoint(p, 0, r.getCenterY()- p.getY());
                            break;
                        case ALIGN_VERTICALY:
                            editedElement.movePoint(p, r.getCenterX()- p.getX(), 0);
                            break;
                        case ALIGN_CENTER:
                            editedElement.movePoint(p, r.getCenterX()- p.getX(), r.getCenterY()- p.getY());
                            break;
                        }                        
                    }
                    
                    saveState(false);
                    break;
                }
                
                switch ((int)param) {
                    case ALIGN_CENTER:
                        Point2D c;
                        if( selectedElements.size()==1) { // center on 2D Cursor
                            c = coord2DCursor;
                        } else {
                            c = selectedElements.get(0).getCenter();
                            if ( c == null) return false;
                        }
                        selectedElements.forEach((e) -> {
                            Point2D c2 = e.getCenter();
                            if ( c2 != null)
                                e.translate(c.getX() - c2.getX(), c.getY()- c2.getY());
                        });
                        
                        saveState(false);
                        return true;
                    case ALIGN_HORIZONTALY:
                        r = getSelectionBoundary(false);
                        dest = r.getHeight()/2 + r.getY();
                        break;
                    case ALIGN_VERTICALY:
                        r = getSelectionBoundary(false);
                        dest = r.getWidth()/2 + r.getX();
                        break;
                    default:
                        dest = getAlignPosition( (int)param);
                        break;
                }
 
                switch( (int)param) {
                    case ALIGN_HORIZONTALY:
                        selectedElements.forEach((b) -> {
                            double curPos = b.getBounds().getY() + b.getBounds().getHeight()/2;
                            b.translate(0, dest - curPos);
                        });
                        break;
                    case ALIGN_VERTICALY:
                        selectedElements.forEach((b) -> {
                            double curPos = b.getBounds().getX() + b.getBounds().getWidth()/2;
                            b.translate(dest - curPos, 0);
                        });
                        break;
                    case ALIGN_LEFT:         
                        selectedElements.forEach((b) -> {
                            double curPos = b.getBounds().getX();
                            b.translate(dest - curPos, 0);
                        });
                        break;
                    case ALIGN_TOP:         
                        selectedElements.forEach((b) -> {
                            double curPos = b.getBounds().getY();
                            b.translate(0, dest - curPos);
                        });
                        break;
                    case ALIGN_RIGHT:         
                        for( GElement b : selectedElements) {
                            double curPos = (r=b.getBounds()).getX()+r.getWidth();
                            b.translate(dest - curPos, 0);
                        }
                        break;   
                    case ALIGN_BOTTOM:         
                        for( GElement b : selectedElements) {
                            double curPos = (r=b.getBounds()).getY()+r.getHeight();
                            b.translate(0, dest - curPos);
                        }
                        break;   
                }
                saveState(false);
                break;
                
            case ACTION_CURSOR_AT_CENTER:
                if ( ! selectedElements.isEmpty()) {
                    coord2DCursor = getCenterOfSelection();
                    invalidateWithoutUpdateGCodeListViewer();
                    return true;
                }
                if ( selectedPoints.size() < 2) {
                    inform("Press on first point");
                    setCursor(crossCursor);
                    repaint();
                    mouseMode = MOUSE_MODE_CURSOR_AT_CENTER;
                    mouseRectangleP2 = null;
                    coordMouseOrigin = null;
                    return true;
                }
                GCode p3,p2,p1 = selectedPoints.get(0);
                switch (selectedPoints.size()) {
                    case 2:
                        p2 = selectedPoints.get(1);
                        coord2DCursor.setLocation(p1.getX()+(p2.getX()-p1.getX())/2, p1.getY()+(p2.getY()-p1.getY())/2);
                        break;
                    default:
                        p2 = selectedPoints.get(1);
                        p3 = selectedPoints.get(2);
                        double yDelta_a = p2.getY() - p1.getY();
                        double xDelta_a = p2.getX() - p1.getX();
                        double yDelta_b = p3.getY() - p2.getY();
                        double xDelta_b = p3.getX() - p2.getX();
                        GCode c = new GCode(0,0);
                        double aSlope = yDelta_a/xDelta_a;
                        double bSlope = yDelta_b/xDelta_b;  
                        double cx = (aSlope*bSlope*(p1.getY() - p3.getY()) + bSlope*(p1.getX() + p2.getX())
                                        - aSlope*(p2.getX()+p3.getX()) )/(2* (bSlope-aSlope) );
                        double cy = -1*(cx - (p1.getX()+p2.getX())/2)/aSlope +  (p1.getY()+p2.getY())/2;
                        coord2DCursor.setLocation(cx, cy);
                        invalidateWithoutUpdateGCodeListViewer();
                }
                break;
            case ACTION_CURSOR_AT_HEAD:
                pt = (Point3D)object;
                coord2DCursor.setLocation(pt.getX(), pt.getY());
                break;

            case ACTION_CHANGE_START_POINT:
                if (selectedPoints.size() == 1) {  
                    GCode p = selectedPoints.get(0);
                    if (editedElement instanceof G1Path) {
                        if ( ! ((G1Path)editedElement).changeFirstPoint(p) ) {
                            inform("Path not closed and not the end point");
                        } else
                            stateHasChanged = true;
                    }
                    else if (editedElement instanceof GMixedPath) {
                        if ( ! ((GMixedPath)editedElement).changeFirstPoint(p) ) {
                            inform("Path not closed and not the end point");
                        } else
                            stateHasChanged = true;
                    }
                    else {
                        inform("path not compatible");
                        break;
                    }
                        
                    clearSelectedPoints();
                } else
                    inform("Select one point first");
                
                break;   
                
                
            case ACTION_CONVERT_TO_MIXEDPATH:
                if ( selectedElements.isEmpty()) break;
                
                GElement e = selectedElements.get(0);
                if ((e instanceof GArc) || (e instanceof GSpline)) {
                    if ( param == 0) removeElement(e);
                    GMixedPath m = new GMixedPath( ((param == 0) ? "" : "conv-" ) + e.getName());
                    m.add(e);
                    add(m);
                }
                if ( e instanceof G1Path) {
                    if ( param == 0) removeElement(e);
                    GMixedPath m = new GMixedPath( ((param == 0) ? "" : "conv-" ) + e.getName());
                    for( GCode l : (G1Path)e) {
                        m.add((param == 0) ? l : l.clone());
                    }
                    add( m );
                } else inform("not a possible");     
                break;
                
            case ACTION_COPY:
                if ( editedElement != null) {
                    clipBoard = G1Path.cloneArrayOfGLines( editedElement.getLines(gCodeListViewer.getSelectedIndices()));
                    inform(selectedPoints.size() + " lines(s) copied");
                } else if ( ! selectedElements.isEmpty()) {
                    clipBoard = new GGroup(selectedElements, true);
                    inform(selectedElements.size() + " elements(s) copied");
                }
                else inform("Nothing to copy");
                break;
                
            case ACTION_CUT:
                clipBoard = extractSelection(true);
                
                /*if (editedElement != null) {
                    ArrayList<GCode>lines = getSelectedLines();
                    if ( ! lines.isEmpty()) {
                        clipBoard = G1Path.cloneArrayOfGLines(lines);
                        clearSelectedPoints();
                        editedElement.removeAll(lines);
                    }
                } else if ( ! selectedElements.isEmpty()) {
                    clipBoard = new GGroup(selectedElements);
                    removeAllElements(selectedElements);
                }*/
                saveState(true);
                break;
                
            case ACTION_EXTRACT:
                GElement sub = extractSelection(false);
                if ( sub != null) {
                    if ( editedElement != null) document.getParent(editedElement).add(sub);
                    else document.getParent( editedGroup).add(sub);
                    saveState(true);
                }
                break;
                
            case ACTION_DELETE:
                if ( editedElement != null) {
                    ArrayList<GCode> lines = new ArrayList<>();
                    for ( int i : gCodeListViewer.getSelectedIndices())
                        if ( i < editedElement.size())
                            lines.add(editedElement.getLine(i));

                    editedElement.removeAll(lines);
                    if ( editedElement.isEmpty()) editParentOrClearSelection();                   
                    
                    clearSelectedPoints(); 
                } else {
                    removeAllElements(selectedElements);
                    if ( editedGroup.isEmpty()) editParentOrClearSelection();                         
                }
                saveState(true);                
                break;  
                
            case ACTION_FLIP_G1GX:
                if ( (editedElement != null) && (editedElement instanceof GMixedPath))
                    ((GMixedPath)editedElement).flipG1GXmoves((int)param, selectedPoints);
                else inform("only into artistic path");
                break;      
                
            case ACTION_DISTANCE:
                coordMouseOrigin=null;
                mouseMode=MOUSE_MODE_SHOW_DISTANCE;
                inform("Click on first point");
                setCursor(crossCursor);
                break;
                
            case ACTION_SHOW_ANGLE:
                coordMouseOrigin=null;
                mouseMode=MOUSE_MODE_SHOW_ANGLE;
                inform("Click for first point");
                setCursor(crossCursor);
                break;                
                
            case ACTION_ADD_AT_CENTER:
                if ( object instanceof GElement) {
                    selectedElements.forEach((s) -> { 
                        Point2D c = s.getCenter();
                        if ( c != null) {
                            GElement el = ((GElement) object).clone();
                            el.translate(c);
                            add(el);
                        }
                    });
                    saveState(true);
                }
                break;                
                
            case ACTION_FILTER:
                 if ( filterFrame == null) filterFrame = new JFilterFrame();
                 ArrayList<GElement> li = new ArrayList<>();
                 li.add(editedElement);
                 if ( filterFrame.applyFilterOn((editedElement!=null)?GGroup.toList(li):selectedElements)) {
                     clearSelectedPoints();
                     saveState(true);
                 }
                 break;
            case ACTION_FLIP_H:
                if ( selectedElements.isEmpty()) return false;
                center = getCenterOfSelection();
                selectedElements.forEach((s) -> { s.scale(center, -1, 1); }); 
                saveState(false);
                break;
                
            case ACTION_FLIP_V:
                if ( selectedElements.isEmpty()) return false;
                center = getCenterOfSelection();
                selectedElements.forEach((s) -> { s.scale(center, 1, -1); });
                saveState(false);
                break;

            case ACTION_FOCUS_VIEW:
               // if ( editedElement != null) 
               //     r = editedElement.getBounds();
                //else
                r = getSelectionBoundary(false);
                if ( r == null) {
                    if ( param == 1) {
                        selectedElements.add(editedGroup);
                        r = getSelectionBoundary(false);
                        selectedElements.remove(editedGroup);
                    } else {
                        inform("Select the region to focus to");
                        mouseMode=MOUSE_MODE_FOCUS;
                        setCursor(crossCursor);
                        repaint();
                        break;
                    }
                }
                if ( r != null ) focusToRect(r);
                clearMouseMode();
                break;
        
            case ACTION_GROUP_UNGROUP:
                GGroup g = null;
                if ( param == 0)
                    selectedElements.forEach((el) -> { editedGroup.ungroup(el); });
                else {
                    g = editedGroup.group((String)object, selectedElements);
                    inform(selectedElements.size()+" grouped");
                    if ((editedGroup == document) && (gcodeFooter !=null)) {
                        document.remove(gcodeFooter);
                        document.add(gcodeFooter);
                    }
                }
                selectedElements.clear();
                if ( g != null) selectedElements.add(g);
                saveState(true);
                break;
            
            case ACTION_REVERSE:
                if (editedElement != null) editedElement.reverse();
                else selectedElements.forEach((b) -> { b.reverse(); });
                saveState(true);
                break;               
                
            case ACTION_INSERT:
                if ( (editedElement != null)) {
                    int sel = getSelectedLine();
                    clearSelectedPoints();
                    if ( (editedElement instanceof G1Path) || (editedElement instanceof GMixedPath)) {
                        editedElement.add(sel, new GCode("", null));
                        saveState(true);
                    }
                }
                break;
 
            case ACTION_JOIN:
                if (selectedPoints.size() > 1)  // join points in editMode
                {    
                    doAction( ACTION_ALIGN, ALIGN_CENTER, null);
                    inform(selectedPoints.size() + " points merged");
                    /*if ( editedElement instanceof G1Path) ((G1Path)editedElement).joinPoints(selectedPoints);
                    clearSelectedPoints();
                    saveState(true);*/
                    break;
                }
                else if ( selectedElements.size() > 1)
                {
                    boolean joined;
                    int joinCount=0;
                    do
                    {
                        joined=false;
                        for (GElement s1 : selectedElements) {
                            if ( s1 instanceof GGroup) { 
                                if (((GGroup) s1).joinElements(param)) {
                                    if ( s1.size()==1) {
                                        editedGroup.add(((GGroup) s1).get(0));
                                        editedGroup.remove(s1);                               
                                        joined=true;
                                    } 
                                    joinCount++;
                                    break;
                                }
                            }
                            if ( ! ((s1 instanceof G1Path)||(s1 instanceof GSpline)||(s1 instanceof GMixedPath)) || (s1 instanceof GPocket3D)) continue;
                            if (s1.getFirstPoint() == null) continue;
                            
                            for( GElement s2 : selectedElements) {
                                if ( ! ((s2 instanceof G1Path)||(s1 instanceof GSpline)||(s1 instanceof GMixedPath)) || (s1 instanceof GPocket3D)) continue;
                                if (s2.getFirstPoint() == null) continue;
                                
                                if ( s1 != s2) {
                                    if ( s1.getFirstPoint().distance( s2.getFirstPoint()) < param)
                                        s2.reverse();
                                    
                                    if ( (s1.getLastPoint().distance(s2.getFirstPoint()) < param) ||
                                         (s1.getLastPoint().distance(s2.getLastPoint()) < param)) {       
                                        
                                        if (  (s1 instanceof GSpline) ||
                                             ((s2 instanceof GSpline) && ! (s2 instanceof GMixedPath)) || 
                                             ((s1 instanceof G1Path)  && ! (s2 instanceof G1Path))) {
                                            GMixedPath mp = GMixedPath.makeFromGElement(s1);                                          
                                            GGroup p = document.getParent(s1);
                                            int i = p.indexOf(s1);
                                            p.remove(s1);                                            
                                            p.add(i, mp);
                                            selectedElements.add(selectedElements.indexOf(s1), mp);
                                            selectedElements.remove(s1);
                                            s1=mp;                                                                                     
                                        }
                                        document.getParent(s2).remove(s2);
                                        selectedElements.remove(s2);
                                        if (s1.getLastPoint().distance(s2.getLastPoint()) < param) s2.reverse();                                      
                                        joined=s1.concat(s2, param);
                                        joinCount++;
                                        break;
                                    } 
                                }   
                            }
                            if ( joined) break;
                        }
                    } while ( joined);
                    
                    if ( joinCount > 0) {
                        selectedElements.clear();
                        saveState(true);
                        
                    }
                    inform(joinCount + " block(s) merged");
                }
                break;                 
             
            case ACTION_MAKE_FLATTEN:
                @SuppressWarnings("unchecked") 
                final ArrayList<GElement> selection = (ArrayList<GElement>) selectedElements.clone();
                selectedElements.clear();
                
                selection.forEach((el) -> { addWithoutSaveState(el.flatten()); });
                saveState(true);
                inform(selectedElements.size() + " path(s) created");
                break;
                
                
                
            case ACTION_MAKE_POCKET:
                if ( selectedElements.size() < 1) return false;
                if ( ! selectedElements.get(0).isClosed()) return false;
                
                g = G1Path.makePocket(selectedElements, param);
                if ( ! g.isEmpty()) {
                    editedGroup.add( g);
                    (g.properties=new EngravingProperties()).setAllAtOnce(true);
                }             
                saveState(true);
                inform(g.size() + "path(s) created.");
                break;
                
            case ACTION_MAKE_OFFSET_CUT:
                if ( selectedElements.size() < 1) return false;
                boolean inner = false;
                if ( param < 0) {
                    inner=true;
                    param=-param;
                }
                
                Area area = null;
                for ( GElement b : selectedElements) {
                        Area a = b.getOffsetArea(param);
                        if ( area == null) area = a;
                        else area.add(a);   
                }
                 
                r = getSelectionBoundary(false);
                ArrayList<GElement> offsets = G1Path.makeElementsFromArea( "cut", area);
                @SuppressWarnings("unchecked") ArrayList<GElement> res = (ArrayList<GElement>) offsets.clone();
                for( GElement b : offsets)
                    if ( b.getBounds().contains(r)) { 
                        if ( inner) res.remove(b);
                    } else 
                        if ( ! inner) res.remove(b);

                selectedElements.clear();
                if ( ! res.isEmpty()) {
                    if ( res.size() == 1) add(res.get(0));
                    else add(new GGroup("cutPath", res));
                }
                saveState(true);
                inform(res.size() + " cut path(s) created.");
                break;
                
            case ACTION_MAP_TEXT_TO_PATH:
                if ( selectedElements.isEmpty()) return false;
                e = selectedElements.remove(0);
                
                if ( e instanceof GGroup ) {
                    inform("select a path first");
                    return false;
                } else if ( e instanceof GTextOnPath) {
                    inform("impossible");
                    return false;
                }
                
                document.getParent(e).remove(e);
                ((GTextOnPath)object).setPath(e);
                add( (GTextOnPath)object);
                setEditedElement((GTextOnPath)object);
                break;
                
            case ACTION_MOVE:
                if ( selectedElements.isEmpty()) return false;
                inform("Choose move origin");
                mouseMode=MOUSE_MODE_CHOOSE_MOVE_ORIGIN;
                setCursor(crossCursor);
                break;
                
            case ACTION_MOVE_UP:              
                if ( ! selectedPoints.isEmpty()) {
                    int[] sel = gCodeListViewer.getSelectedIndices();
                    for ( int i : sel) {
                        if ( i > 0) {
                            GCode l = (GCode) editedElement.remove(i);
                            editedElement.add(i-1, l);
                        }
                    }
                    selectedPoints.clear();
                    for ( int i : sel) 
                        if ( i > 0) selectedPoints.add( editedElement.getLine(i-1));
                } else {
                    if ( selectedElements.isEmpty()) break;
                    
                    int[] sel = gCodeListViewer.getSelectedIndices();
                    for ( int i : sel) {
                        if ( i > 0 ) {
                            GElement el = editedGroup.remove(i);         
                            editedGroup.add(i-1, el);
                        }
                    }
                }
                saveState(true);
                break;
            case ACTION_MOVE_DOWN:
                if ( ! selectedPoints.isEmpty()) {
                    if ( gCodeListViewer.getMaxSelectionIndex() >=  editedElement.getSize()-1) break;
                    int[] sel = gCodeListViewer.getSelectedIndices();
                    
                    for (int i = sel.length - 1; i >= 0; i--) {
                        int n = sel[i];
                        if ( n < editedElement.getSize()-1) editedElement.add(n+1, (GCode)editedElement.remove(n));
                    }
                    selectedPoints.clear();
                    
                } else {
                    if ( selectedElements.isEmpty()) break;
                    int[] sel = gCodeListViewer.getSelectedIndices();
                    
                    for (int i = sel.length - 1; i >= 0; i--) {
                        int n = sel[i];
                        if ( n < editedGroup.size()-1)
                            editedGroup.add(n+1, editedGroup.remove(n));
                    }
                }
                saveState(true);
                break;
                
               
            case ACTION_INVERSE_SEL:
                if (editedElement != null) {
                    ArrayList<GCode> newSel = new ArrayList<>();
                    for( GCode p : editedElement.getPointsIterator()) {
                        if ( ! selectedPoints.contains(p)) newSel.add(p);
                    }
                    selectedPoints = newSel;

                } else {
                    ArrayList<GElement> newSel = new ArrayList<>();
                    for( GElement el : editedGroup.getIterable()) {
                        if ( ! selectedElements.contains(el)) newSel.add(el);
                    }
                    selectedElements.clear();
                    selectedElements.addAll(newSel);                    
                }
                
                selectionHasChanged = true;
                invalidate();
                break;
                
            case ACTION_MOVE_GRBL_HEAD:    
                mouseMode = MOUSE_MODE_MOVE_GANTRY;
                showLaser=param==1;
                inform("Click to move gantry");
                setCursor( jogCursor);
                break;
            
            case ACTION_MOVE_MPOS:
                mouseMode = MOUSE_MODE_SET_MPOS;
                showLaser=param==1;
                inform("Click to a position to set WPos according to MPos localisation");
                setCursor( crossCursor);
                break;                
      
                
            case ACTION_PASTE:
                if ((clipBoard != null) && ! clipBoard.isEmpty()) {  
                    if ( clipBoard instanceof GGroup)
                        editedGroup.addAll( ((GGroup)clipBoard).clone().getAll());
                    
                    else {
                        selectedPoints.clear();
                        for( GCode p : clipBoard.getPointsIterator()) {
                            GCode c = p.clone();
                            if ( editedElement.add(c))
                                selectedPoints.add(c);
                        }
                        selectionHasChanged = true;
                        inform(selectedPoints.size()+" point(s) added");
                    }                    

                    saveState(true);
                }                
                break;
                
            case ACTION_ROTATE:
                if ( selectedElements.isEmpty() && selectedPoints.isEmpty()) break;
                setCursor(crossCursor);
                inform("Choose angle origin");
                mouseMode=MOUSE_MODE_ROTATION;
                transformationOrigin= (param==0)?getCenterOfSelection():coord2DCursor; 
                           
                repaint();
                break;
                
            case ACTION_SCALE:
                if ( selectedElements.isEmpty() && selectedPoints.isEmpty()) break;
                selectionContainsArc = selectedElements.stream().anyMatch((e3)-> (e3.isoScaling()));
                inform("Choose scale length references.");
                setCursor( crossCursor);
                scaleNormaleX = Double.MAX_VALUE;
                transformationOrigin= (param==0) ? getCenterOfSelection() : coord2DCursor;
                mouseMode=MOUSE_MODE_SCALE;
                repaint();
                break;                
                
            case ACTION_SELECT_ALL:
                if (editedElement != null) {
                    clearSelectedPoints();                    
                    if ( editedElement instanceof G1Path)
                        selectedPoints.addAll(((G1Path)editedElement).getAll());
                    else
                        for (GCode c : editedElement) {
                            selectedPoints.add(c);
                    }
                    
                } else {
                    selectedElements.clear();
                    selectedElements.addAll(editedGroup.getAll());
                }
                selectionHasChanged=true;
                invalidate();
                break;                
                
            case ACTION_SET_2D_CURSOR:
                mouseMode = MOUSE_MODE_SET_2D_CURSOR;
                inform("Click to set the new position of 2D cursor");
                setCursor( crossCursor);
                break;
                
                
            case ACTION_SET_AS_HEADER:
                if ( (selectedElements.size()==1) && 
                        (document.indexOf(selectedElements.get(0)) != 0)) {
                    editedGroup.remove(gcodeHeader=selectedElements.get(0));
                    document.add(0, gcodeHeader);
                    gcodeHeader.setName("Header-0");
                    saveState(true);
                } else inform("Select a block first");
                break;
            case ACTION_SET_AS_FOOTER:
                if ( (selectedElements.size()==1) &&  
                        (editedGroup.indexOf(selectedElements.get(0)) != 0)) {
                    editedGroup.remove(gcodeFooter=selectedElements.get(0));
                    document.add(gcodeFooter);
                    gcodeFooter.setName("Footer-0");
                    saveState(true);
                } else inform("Select a block");
                break;
                
            case ACTION_SIMPLIFY:
                if ((selectedPoints.size() > 2) && ( editedElement instanceof G1Path))
                {
                    ArrayList<GCode> extraPoints = ((G1Path)editedElement).keepToSimplify(selectedPoints, param);
                    clearSelectedPoints();
                    selectedPoints.addAll(extraPoints); 
                    saveState(true);
                }
                break;
            case ACTION_SHOW_MOVES:
                    showMoves = (param == 1);
                    invalidate();
                    break;
            case ACTION_SHOW_OBJECT_SURFACE:
                showObjectSurface = (param == 1);
                invalidate();
                break;
            case ACTION_SHOW_GRID:
                showGrid = (param == 1);
                invalidate();
                break;
            case ACTION_SHOW_WORKSPACE:
                showWorkspace = (param == 1);
                invalidate();
                break;
            case ACTION_SIMPLIFY_ANGLE:
                if ( (editedElement != null) && (editedElement instanceof G1Path) ) {
                      
                    editedElement.removeByDistance(selectedPoints.isEmpty()?
                                    ((G1Path)editedElement).getPointsByAngle(150) 
                                    : selectedPoints, param);
                    saveState(true);
                 
                } else {
                    for( GElement b : selectedElements) {
                        b.removeByDistance(null, param);
                    }
                  saveState(false);
                 
                }
                break;
            case ACTION_OPTIMIZE_MOVES:
                if ( selectedElements.isEmpty() ) editedGroup.sort(coord2DCursor, true);
                else if ( (selectedElements.size() == 1) && (selectedElements.get(0) instanceof GGroup))
                    ((GGroup)selectedElements.get(0)).sort(coord2DCursor, true);
                else {
                    @SuppressWarnings("unchecked")
                    final ArrayList<GElement> el = (ArrayList<GElement>) selectedElements.clone();
                    GGroup.moveLength=0;
                    GGroup.optimizeMoves(el.isEmpty() ? (ArrayList<GElement>)editedGroup.getAll().clone() : el, coord2DCursor, true);
                    // reorder selection in EditedGroup
                    for(int i = 0; i < el.size(); i++) {
                        int pos1 = editedGroup.indexOf(selectedElements.get(i));
                        int pos2 = editedGroup.indexOf(el.get(i));
                        editedGroup.remove(pos1);
                        editedGroup.add(pos1, el.get(i));
                        editedGroup.remove(pos2);
                        editedGroup.add(pos2, selectedElements.get(i));
                    }
                }
                saveState(true);
                break;
            case ACTION_UNDO:
                if ( undoManager.canUndo()) {
                    setEditedElement(undoManager.undo(document)); 
                    invalidate();
                    stateHasChanged=false;
                }
                documentHasChanged = undoManager.canUndo();
                invalidate();
                /*
                if ( undoStackPosition < undoDataStack.size()-1) {                
                    clearSelectedPoints();
                    selectedElements.clear();
                    UndoRecord rec = undoDataStack.get(++undoStackPosition);      
                    if ( rec.allContent != null) {
                        setContent((GGroup) rec.allContent.cloneWithSameID(), false);                    
                        setEditedElement(document.getElementID(rec.editedGroupId));
                    } else {
                        e = document.getElementID(rec.editedElement.getID());
                        GGroup p = document.findParent(e);
                        if ( p == null)
                            System.out.println("pas bon");
                        p.remove(e);
                        p.add(e=rec.editedElement.cloneWithSameID());
                        setEditedElement(e);
                    }
                    documentHasChanged = ! (undoStackPosition==undoDataStack.size()-1);
                    invalidate();
                }*/
                break;    
            case ACTION_REDO:
                if ( undoManager.canRedo()) {
                    setEditedElement(undoManager.redo(document));
                    invalidate();
                    stateHasChanged=false;
                }
                documentHasChanged = undoManager.canUndo();
                invalidate();
                /*
                if ( undoStackPosition > 0) {                
                    clearSelectedPoints();
                    selectedElements.clear();
                    UndoRecord rec = undoManager.get(--undoStackPosition); 
                    if ( rec.allContent != null) {
                        setContent((GGroup) rec.allContent.cloneWithSameID(), false); 
                        setEditedElement(document.getElementID(rec.editedGroupId));
                    } else {
                        //e = document.getElementID(rec.editedElement.getID());
                        GGroup p = UndoRecord.findParent(document, rec.editedElement.getID());
                        p.remove(e);
                        p.add(e=rec.editedElement.cloneWithSameID());
                        setEditedElement(e);
                    }
                    documentHasChanged= ! undoManager.isEmpty();                      
                    invalidate();
                }
                */
                break;  
                
            case ACTION_REMOVE_COMMENT:
                if ( selectedElements.size() > 1)
                    for ( GElement e3 : selectedElements) e3.removeComments();
                invalidate();
                break;
        }
        return true;
    }
    
    /**
     * Save the current state of the document into undoStack and invalidate()
     * @param updateSelection if true, remplace EditListViewer 
     */
    public void saveState( boolean updateSelection) {      
        selectionHasChanged=updateSelection;
        
        if ( stateHasChanged) {
            documentHasChanged=true;       
            undoManager.saveState(document, (editedElement!=null)?editedElement.getID():editedGroup.getID());
            /*
            while( undoStackPosition>0) {
                undoManager.remove(0);
                undoStackPosition--;
            }
            undoManager.add(0, UndoRecord.createRecord(document, editedElement, editedGroup.getID()));

            System.out.println("SaveState=>stack["+undoManager.size()+" / "+ undoManager.size()+"]");
            System.out.flush();
            StackTraceElement[] stackTrace = Thread.getAllStackTraces().get(Thread.currentThread());
            for(istateHasChanged=false;nt i = 2; i < 5; i++) System.err.println(stackTrace[i]);
            */
            stateHasChanged=false;
        }
        setKeyFocus(true);
        if ( updateSelection) invalidate(); 
        else invalidateWithoutUpdateGCodeListViewer(); 
    }

    public String getSelectedElementsInfo() {
        if ( editedElement != null) return "Edited: " + editedElement +  
                    ((selectedPoints.size()>1)?selectedPoints.size()+" selected":
                        (selectedPoints.size()>0)?" selected n° " + editedElement.getIndexOfPoint(selectedPoints.get(0)):"");
        else 
            if ( selectedElements.isEmpty()) return "";
            else {
                Rectangle2D r = getSelectionBoundary(false);
                String bounds = "";
                if ( r != null) 
                    bounds = String.format(Locale.ROOT, " : dim=( %.3f , %.3f )", r.getWidth(), r.getHeight());
                
                if ( selectedElements.size() == 1) 
                    return "Selected: " + selectedElements.get(0) + bounds; 
                else 
                    return selectedElements.size() + " blocks(s) selected" + bounds;
            }
    }

    private Point2D getCenterOfSelection() {
        Rectangle2D r = null;
        if ( ! selectedPoints.isEmpty())
            for (GCode p : selectedPoints) {
                if ( r == null) r = new Rectangle2D.Double(p.getX(), p.getY(), 10e-9, 10e-9);
                else r.add(p);
        }
        else
            if ( selectedElements.size() == 1) return selectedElements.get(0).getCenter();
            else
                for( GElement s : selectedElements) {  
                    Rectangle2D r2 = (Rectangle2D) s.getBounds();
                    if ( r2 != null) {
                        if ( r == null) r = r2;
                        else r.add( r2);
                    }
                }
        
        if ( r == null) return null;
        else return new Point2D.Double(r.getCenterX(), r.getCenterY());
    }

    /**
     * Update listViewerEditor content and selection, if 'selectionHasChanged'.
     * Inform(null) and call to super.invaliate(); and repaint();
     */
    @Override
    public void invalidate() {
        String newConf =  "" + snapToGrid + "," + snapToPoints + "," + showGrid + "," +
                    showMoves + "," + showHead + "," + showObjectSurface + "," +
                    showStartPoints + "," + showWorkspace;
        if ( ! newConf.equals(conf.editorSettings))
        {
            conf.editorSettings = newConf;
            conf.saveVisualSettings();
        }

        if (selectionHasChanged && (gCodeListViewer != null)) {   // remplace listViewer
            if (editedElement != null) {
                if (gCodeListViewer.getModel() != editedElement) 
                    gCodeListViewer.setModel(editedElement);
                else {// remplace points selection
                    gCodeListViewer.clearSelection();
                
                    int first=-1, i = 0;
                    for( GCode l : editedElement) {
                        if ( selectedPoints.contains(l)) { 
                            if ( first == -1) first = i;
                        } else if ( first != -1) { 
                            gCodeListViewer.addSelectionInterval(first, i-1); first = -1; 
                        }
                        i++;
                    }
                    if ( first != -1) gCodeListViewer.addSelectionInterval(first, i-1);
                    
                }
            } else {
                if (gCodeListViewer.getModel() != documentListModel ) 
                    gCodeListViewer.setModel(documentListModel);
                else {// remplace block selection
                    gCodeListViewer.clearSelection();
                
                    int first=-1, i = 0;
                    for( GElement s : editedGroup.getIterable()) {
                        if ( selectedElements.contains(s)) { 
                            if ( first == -1) first = i;
                        } else if ( first != -1) { 
                            gCodeListViewer.addSelectionInterval(first, i-1);
                            first = -1; 
                        }
                        i++;
                    }
                    if ( first != -1) gCodeListViewer.addSelectionInterval(first, i-1);
                }
            }
            inform(null);
            gCodeListViewer.invalidate();
            gCodeListViewer.repaint();           
        }
        selectionHasChanged=false;
        super.invalidate();
        repaint();
    }
    
    /** call inform(null) and repaint(). */
    protected void invalidateWithoutUpdateGCodeListViewer() {
        if ( selectionHasChanged && (mouseMode != MOUSE_MODE_NONE)) 
            clearMouseMode();
        else {
            inform(null);      
            repaint();
        }
    }


    /**
     * Call listener.updateGUIAndStatus() or inform(msg) 
     * and eventualy updatePropertiesPanel() if 'selectionHasChanged'
     * @param msg 
     */
    private void inform(String msg) {
        if (listener == null) return;
        
        if (selectionHasChanged)
            EventQueue.invokeLater(() -> {listener.updatePropertiesPanel(); });
        if (msg == null)
            EventQueue.invokeLater(() -> {listener.updateGUIAndStatus(); });
        else 
            EventQueue.invokeLater(() -> {listener.inform(msg); });
    }

    public void exportToDXF( String filename, boolean onlySelection, boolean flattenSPline) throws IOException {
        if ( selectedElements.isEmpty() || ! onlySelection)
            GGroup.exportToDXF(filename, document, flattenSPline);
        else
            GGroup.exportToDXF(filename, new GGroup(selectedElements, false), flattenSPline);
    }
    
    public void exportToSVG(String fileName, boolean onlySelection) throws IOException {
        if ( editedGroup.size()==0) return;
        if ( selectedElements.isEmpty() || ! onlySelection)
                GGroup.exportToSVG(fileName, document);
            else
                GGroup.exportToSVG(fileName, new GGroup(selectedElements, false));
    }

    private Rectangle getScreenBlocksBounds( boolean onlySelection) {
        Rectangle res = null;
        for ( Iterable<? extends Object> s : onlySelection ? editedGroup : selectedElements ) {
            if ( s instanceof GElement) {
                Rectangle2D r2 = ((GElement)s).getBounds();
                Rectangle r = null;
                if ( r2 != null) r = coordToScreen(r2);
                if ( res == null) res = r;
                else res.add(r);
            }
        }
        return res;
    }

    /** remove element from current editedElement
     * @param element
     * @return  true if element exist in this object */
    private void removeElement( GElement element) {
        //if ( ! editedGroup.contains(element)) return false;
        if ( gcodeFooter == element) gcodeFooter = null;
        if ( gcodeHeader == element) gcodeHeader = null;
        editedGroup.remove(element);
       // return true;
    }
    
    public void removeAllElements( ArrayList<GElement> list) {
        list.forEach((b) -> {
            removeElement(b);
            if ( b==gcodeHeader) gcodeHeader=null;
            if ( b==gcodeFooter) gcodeFooter=null;
        });     
        clearSelectedPoints();
        selectedElements.clear();
        saveState(true);
    }
    
    /** 
     * Add a new path in the current edited group and replace Footer if necessary.
     * Finaly call saveState() and invalidate()
     * @param path a new path
     */
    public void add(GElement path) {   
        addWithoutSaveState( path);
        saveState(true);
        invalidate();
    }
    
    /** 
     * Add a new path in the current edited group and replace Footer if necessary without calling saveState (for multiple adds).
     * @param e a new path
     */
    public void addWithoutSaveState( GElement e) {
        if (e == null) return;
        editedGroup.add(e);
        if ((editedGroup == document) && ( gcodeFooter != null)) {
            document.remove(gcodeFooter);
            document.add(gcodeFooter);
        }        
        stateHasChanged=true;
    }
    
    private void add(int index, GElement element, boolean toSelectionToo) {
        if ( element == null) return;

        editedGroup.add(index,element);
        if ( editedGroup == document) {
            if ((gcodeHeader!=null) && (index == 0)) {
                document.remove(gcodeHeader);
                document.add(0, gcodeHeader);
            }
            if ( gcodeFooter != null) {
                editedGroup.remove(gcodeFooter);
                editedGroup.add(gcodeFooter);
            }
        }
        if ( toSelectionToo ) selectedElements.add(element);
        setEditedElement(editedGroup);
        saveState(toSelectionToo);
    }

    public static final int STATE_POINT_SELECTED_FLAG  = 1;
    public static final int STATE_2POINTS_SELECTED_FLAG  = 2;    
    public static final int STATE_POINTS_SELECTED_FLAG = 4;
    public static final int STATE_SHAPE_SELECTED_FLAG = 8;
    public static final int STATE_SHAPES_SELECTED_FLAG = 16;
    /** set if a path is currently edited (the points). */
    public static final int STATE_EDIT_MODE_FLAG = 32;
    public static final int STATE_CAN_UNDO_FLAG = 64;
    public static final int STATE_SNAP_TO_GRID_FLAG = 128;
    public static final int STATE_SNAP_TO_POINTS_FLAG = 256;
    public static final int STATE_GRID_FLAG = 512;
    public static final int STATE_EDIT_LINE = 1024;
    public static final int STATE_CLIPBOARD_EMPTY_FLAG = 2048;    
    public static final int STATE_DOCUMENT_MODIFIED = 4096;
    public static final int STATE_LINE_SELECTED = 8192;
    public static final int STATE_SHOW_MOVES_FLAG = 16384;
    public static final int STATE_CAN_REDO_FLAG = 32768;
    public static final int STATE_SHOW_OBJECT_SURFACE = 65536;
    public static final int STATE_SHOW_START = 131072;
    /**
     * Get the state of this editor
     * @return an union of STATE_xxx values
     */
    public int getState() {
        int res = 0;
        if ( documentHasChanged ) res |= STATE_DOCUMENT_MODIFIED;
        if ( editedElement != null) res |= STATE_EDIT_MODE_FLAG;
        if ( selectedPoints.size() == 1) res |= STATE_POINT_SELECTED_FLAG;
        if ( selectedPoints.size() == 2) res |= STATE_2POINTS_SELECTED_FLAG;
        if ( selectedPoints.size() > 1) res |= STATE_POINTS_SELECTED_FLAG;
        if ( gCodeListViewer.getSelectedIndex() != -1) res |= STATE_LINE_SELECTED;
        if ( selectedElements.size() == 1) res |= STATE_SHAPE_SELECTED_FLAG;
        if ( selectedElements.size() > 1) res |= STATE_SHAPES_SELECTED_FLAG;
        if ( clipBoard == null) res |= STATE_CLIPBOARD_EMPTY_FLAG;
        if ( undoManager.canUndo()) res |= STATE_CAN_UNDO_FLAG;
        if ( undoManager.canRedo()) res |= STATE_CAN_REDO_FLAG;
        if ( snapToGrid ) res |= STATE_SNAP_TO_GRID_FLAG;
        if ( snapToPoints ) res |= STATE_SNAP_TO_POINTS_FLAG;
        if ( showGrid) res |= STATE_GRID_FLAG;
        if ( showMoves) res |= STATE_SHOW_MOVES_FLAG;
        if ( showObjectSurface) res |= STATE_SHOW_OBJECT_SURFACE;
        if ( showStartPoints) res |= STATE_SHOW_START;
        if ( ! keyFocus) res |= STATE_EDIT_LINE;
        return res;
    }
    
    public Configuration getConfiguration() {
        return conf;
    }
    
    /**
     * Repaint and adjust workspace with 
     */
    public void applyConfiguration() {
        if ( (conf.workspaceWidth > 1) && (conf.workspaceHeight > 1)) 
            zoomFactor = Double.POSITIVE_INFINITY; // zoom to workspace
        
        invalidate();
    }
    
    public void setSnapToGrid(boolean snap) {
        if (snapToGrid != snap) {
            snapToGrid = snap;       
            screenMousePosition = coordToScreenPoint(
                    coordSnapPosition = getCoordSnapPointFor((int)screenMousePosition.getX(), (int)screenMousePosition.getY()));
            invalidate();
            inform(null);
        }
    }

    public void setSnapToPoints(boolean selected) {
        if ( snapToPoints != selected) {
            snapToPoints = selected;
            invalidate();
            inform(null);
        }
    }

    /**
     * return real coordinate of mouse without selectedPoints.get(0) if snapToPoints is true.
     * @param x
     * @param y
     * @return 
     */
    private GCode getCoordSnapPointFor(int x, int y) {
        GCode p = screenToCoordPoint(x,y);
        GCode pGrid = null, p3, pPath = null;

        if ( coord2DCursor.distance(p.getX(), p.getY()) < 10 / zoomFactor) 
            return new GCode(coord2DCursor.getX(), coord2DCursor.getY());
                
        if ( (grblMPos != null) && grblMPos.distance(p.getX(), p.getY(), grblMPos.getZ()) < 10 / zoomFactor)
            return new GCode(grblMPos.getX(), grblMPos.getY());
                
        if ( snapToGrid | snapToPoints) { 
            if ( snapToGrid) {
                p3 = screenToCoordPoint(x,y);
                pGrid = new GCode((Math.round(p3.getX()/gridStep))*gridStep,  (Math.round(p3.getY()/gridStep))*gridStep );
            }  
            
            if ( snapToPoints) {
                    if ( editedElement != null) {
                        pPath = editedElement.getCloserPoint(p, 10 / zoomFactor, null, false);
                    }
                    if ( pPath == null)
                        pPath = document.getCloserPoint(p, 10 / zoomFactor,  
                                        snapWithoutSelection ? selectedElements : null,
                                         snapWithoutSelection ? selectedPoints : null);
                    
                    if ((pPath != null) /*&& (selectedPoints.isEmpty() || (selectedPoints.indexOf(pPath))==-1)) */ && 
                            ((pGrid==null) || (pGrid.distance(p) >= pPath.distance(p))))
                        return (GCode)pPath.clone();
            }
                      
        }
        
        return (pGrid!=null)?pGrid:p;
    }
    
    public void doRemoveByDistance(double d) {
        int res=0,nbp;
        if ( (editedElement != null)&&(editedElement instanceof G1Path)) {
            nbp = editedElement.getSize();
            ((G1Path)editedElement).removeByDistance(((G1Path)editedElement).getPointsByAngle(100), d);
            selectedPoints.clear();
            saveState(true);
            inform(editedElement.getSize()-nbp + " point(s) removed");
        }
        else for ( GElement s : selectedElements) {
            nbp = s.getSize();
            if (s instanceof G1Path) ((G1Path)s).removeByDistance(null, d);
            res+= nbp - s.getSize();
            saveState(true);
            inform( res + "point(s) removed");
        }
    }

    ListModel<Object> getListModel() {
        if ( editedElement != null) return editedElement;
        else 
            return new ListModel<Object>() {
            @Override
            public int getSize() { return 0; }

            @Override
            public Object getElementAt(int index) { return "";} 

            @Override
            public void addListDataListener(ListDataListener l) { }

            @Override
            public void removeListDataListener(ListDataListener l) { }
        };
    }

    /**
     * Set the current edited element (clear selection).
     * @param element the new element to edit
     */
    public void setEditedElement(GElement element) {
        assert( element != null);
        setKeyFocus(true);
          
        boolean exitingEditing = false;
        
        if ( editedElement != null) {  
            if ( editedElement == element) return;
            
            editedElement.removeListDataListener( editedElementListener);
            clearSelectedPoints();
            editedElement = null;
            highlitedPoint = null;
            exitingEditing = true;
        }
        
        clearMouseMode();       
        selectedElements.clear(); 
        
        if ( element != null) {

            if ( element instanceof GGroup) {
                if ( (editedGroup == element ) && ! exitingEditing)  return;                
                editedGroup = (GGroup) element;
            }
            else {
                editedElement = element;
                editedElement.addListDataListener( editedElementListener);
                highlitedPoint=editedElement.getCloserPoint(coordSnapPosition, 10 / zoomFactor, null, false);
            }    
        } else {
            editedGroup = document;            
        }
        
        selectionHasChanged=true;        
        invalidate();
        if ( listener != null) listener.updatePropertiesPanel();
    }

    public void updateEditedRow(String value, ListModel model, int row) {
        if ( model != editedElement) return;
        setKeyFocus(true);       
        SwingUtilities.invokeLater(() -> {
            editedElement.setLine(row, new GCode(value));
            invalidateWithoutUpdateGCodeListViewer();
            //if ( listener != null) listener.updatePropertiesPanel();
        }); 
    }

    public void setListEditor(JList<Object> jListGCode, JPanel sp) {
        gCodeListViewer = jListGCode;
        gCodeListViewer.setModel(documentListModel);
        //listViewerPanel = sp;
        gCodeListViewer.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {                
                //System.out.println("Selection changed("+e.getFirstIndex()+" to "+e.getLastIndex());
                if ( selectionHasChanged) 
                    return; // that comme from me ... don't remplace
                boolean changed = false;
                for( int index = e.getFirstIndex(); index <= e.getLastIndex(); index++) {
                    if ( editedElement != null) {
                        if ( index < editedElement.getSize()) {
                            Object o = editedElement.getElementAt(index); 
                            if ((o instanceof GCode) && ((GCode)o).isAPoint()) {
                                if ( jListGCode.isSelectedIndex(index)) {
                                    if ( ! selectedPoints.contains((GCode)o)) { 
                                        selectedPoints.add((GCode)o); 
                                        changed = true; 
                                    }
                                }
                                else
                                    if ( selectedPoints.contains((GCode)o)) { 
                                        selectedPoints.remove((GCode)o); changed = true;
                                    }
                            }
                        }
                    }
                    else if ( index < editedGroup.getSize()) {
                        Object o = editedGroup.getElementAt(index);
                        if ( o instanceof GElement) {
                            GElement s = (GElement) o;

                            if ( jListGCode.isSelectedIndex(index)) {
                                if ( ! selectedElements.contains(s)) { 
                                    selectedElements.add(s); 
                                    changed = true;
                                }
                            } else
                                if ( selectedElements.contains(s)) { 
                                    selectedElements.remove(s); 
                                    changed = true; 
                                }
                        }
                    }
                }
                if ( changed) invalidateWithoutUpdateGCodeListViewer();
            }
        });
    }

    /** 
     * @return the line selected in the listViewer (or 0 if none)
     */
    private int getSelectedLine() {
        if ( gCodeListViewer.getSelectedIndex() != -1) return gCodeListViewer.getSelectedIndex();
        return 0;
    }

    /**
     * if requestFocus is tgrue, request keyFocus to this JCompoment.
     * @param requestFocus 
     */
    public void setKeyFocus(boolean requestFocus) {
        if ( requestFocus) this.requestFocus();
        keyFocus = requestFocus;

        inform(null); 
    }

    /**
     * Used by EditListAction.
     * @param selectedIndex 
     */
    public void editElement(int selectedIndex) {
        if ( selectedIndex != -1)
            setEditedElement(editedGroup.get(selectedIndex));
    }
    
    /**
     * Return a group composed of selectedElements or a path composed of selectedPoints
     * @param removeOnly if false the result of the extraction will be inserted in the parent of the editedGroup
     * @return if removeOnly return the extracted content (a group of GElement or G[Mixed|1]Path that contains points extracted from the editedElement
     */
    public GElement extractSelection(boolean removeOnly) {
        if ( editedElement == null) {
            if ( removeOnly) {
                selectedElements.forEach((e) -> {
                    document.getParent(e).remove(e);
                });                
                GGroup g = new GGroup(editedGroup.getName()+GElement.getUniqID(), selectedElements);
                saveState(true);
                return g;                
            
            } else {
                // extract selected paths from curent group
                GGroup parent = document.getParent(editedGroup);
                if ( parent == editedGroup) return null;
                
                for ( GElement e : selectedElements) {
                    editedGroup.remove(e);
                    parent.add(e);
                    stateHasChanged = true;
                }
            }
            if ( stateHasChanged) saveState(true);
            
        } else if (editedElement instanceof G1Path) {
            if ( selectedPoints.size() < 2) return null;
            final ArrayList<GCode> cutPoints = new ArrayList<>(selectedPoints.size());
            ((G1Path)editedElement).sortPoints(selectedPoints);
            cutPoints.addAll(selectedPoints);
            editedElement.removeAll(selectedPoints);
            
            clearSelectedPoints();    
            stateHasChanged=true;
            
            return new G1Path(editedElement.getName()+GElement.getUniqID(), cutPoints);

        } else if (editedElement instanceof GMixedPath) {
            if ( selectedPoints.isEmpty()) return null;
            ArrayList<GCode> lines = ((GMixedPath)editedElement).getLines(selectedPoints);
            if ( ! lines.isEmpty()) {
                editedElement.removeAll(lines);
                final GMixedPath p = new GMixedPath(editedElement.getName()+GElement.getUniqID());
                for( GCode gcode : lines) p.add(gcode);
                editedGroup.add( p);
                clearSelectedPoints();
                stateHasChanged = true;
                return p;
            } 
        }
        return null;
    }

    /** 
     * Change the entire content of the document.
     * 
     * @param content the new document content
     * @param saveState save the new content into undoStack
     */
    public void setContent(GGroup content, boolean saveState) {
        boolean newDoc = (document == null);
        if ( ! newDoc) setEditedElement(null);           
        document = content;
        stateHasChanged = true;
        
        if (content.isEmpty())
            gcodeHeader = gcodeFooter = null;
        else {
            if ( content.get(0).getName().startsWith("Header")) gcodeHeader = content.get(0);
            if ( content.get(content.size()-1).getName().startsWith("Footer")) gcodeFooter = content.get(content.size()-1);
        }
        
        setEditedElement(document);
        if ( ! newDoc && saveState) {
            selectedElements.add(document);
            doAction(ACTION_FOCUS_VIEW, 1, null);
            selectedElements.clear();
        }
        if (saveState) saveState(true);
        else invalidate();
        documentHasChanged=!newDoc;
    }

    public void addDefaultGCodeHeaderFooter() {
        setEditedElement(null);
        if ( gcodeHeader == null)
            document.add( 0, gcodeHeader = parseGCode( "Header", conf.GCODEHeader));
        if ( gcodeFooter == null)
            document.add( gcodeFooter = parseGCode( "Footer", conf.GCODEFooter));
        saveState(true);
    }

    /** Create exactly the GCode lines.
     * @param name
     * @param gcode
     * @return  */
    public G1Path parseGCode( String name, String gcode) {
        G1Path res = new G1Path(name);
        for ( String l : gcode.split("\n")) {
            res.add(new GCode(l, null));
        }
        return res;
    }

    /**
     * Used by editor to show the toolTip of a line.
     * @param row the row higlited of the editlistViewer
     * @return 
     */
    public String getListToolTipForRow(int row) {
        if ( row == -1)
            return "Display the content of the current selected element or of the document.";
        
        if ( editedElement != null) {
            if ( editedElement.size() < row) 
                return "-1???";
            return editedElement.getElementAt(row).toString();
        }
        else {
            if ( (editedGroup==null) || (editedGroup.size() <= row))
                return "";
            return editedGroup.get(row).getSummary();
        }
    }
    
    /**
     * Return the properties of this element according to those of this parent
     * @param e any GElemnt
     * @return the EngravingProperties
     */
    public EngravingProperties getHeritedPropertiesOf( GElement e) {
        if ( e == null) return null;
        
        EngravingProperties ep = document.properties.clone();
        GGroup current = document;
        while( (current.getParent(e) != null) &&(current.getParent(e) != current)) {
            current = current.getFirstParentOf(e, true);
            EngravingProperties p = current.properties;
            ep.setEnabled( ep.isEnabled() & p.isEnabled());
            ep.setFeed( Double.isNaN(p.getFeed()) ? ep.getFeed() : p.getFeed());
            ep.setPower( (p.getPower() == -1) ? ep.getPower() : p.getPower());
            if ( ! ep.isAllAtOnce()) {
                ep.setPassCount( (p.getPassCount() == -1) ? ep.getPassCount() : p.getPassCount());
                ep.setZStart( Double.isNaN(p.getZStart()) ? ep.getZStart() : p.getZStart());
                ep.setZEnd( Double.isNaN(p.getZEnd()) ? ep.getZEnd() : p.getZEnd());
                ep.setPassDepth( Double.isNaN(p.getPassDepth()) ? ep.getPassDepth() : p.getPassDepth());
            }
            ep.setAllAtOnce( ep.isAllAtOnce() | p.isAllAtOnce());        
        }
        return ep;
    }
    
    /**
     * @return the only one Gelement selected/edited or the GGroup curently <i>openned</i> if none or null if more than one thing selected.
     */
    private GElement getEditedElement()
    {
        if ( editedElement != null) return editedElement;
        if ( selectedElements.isEmpty()) return editedGroup;
        if ( selectedElements.size()==1) return selectedElements.get(0);
        return null;
    }
    
    /**
     * Set the name of the currently edited element or the GGroup <i>openned</i> or the document.
     * @param newName 
     */
    public void setEditedElementName(String newName) {
        GElement e = getEditedElement();
        if (e != null) {
            e.setName(newName);        
            saveState(true);
        }
    }
    
    public Class getFirtsSelectedElementClass() {
        if ( selectedElements.isEmpty() ) return null;
        return selectedElements.get(0).getClass();
    }
    
    public Class getSelectedElementClass()
    {
        if ( getEditedElement() == null) return selectedElements.getClass();
        else return getEditedElement().getClass();
    }
    
    /**
     * @return the EngravingProperties of the only one Gelement selected 
     * or the GGroup curently <i>openned</i> 
     * or a new <i>EngravingProperties</i> to modify all the selection.
     */
    public EngravingProperties getSelectionProperties() {
        GElement e = getEditedElement();     
        if (e==null) 
            if ( ! selectedElements.isEmpty()) {   
            // more than one element selected
            selectionProperties = new EngravingProperties();
            GGroup.makeCommonProperties( selectionProperties, selectedElements);
            
            // add listener to remplace selection when needed
            selectionProperties.addChangeListener((int type) -> {
                for (GElement e1 : selectedElements) {
                    switch (type) {
                        case EngravingProperties.PropertieChangeListener.ALL:
                            e1.properties.setAllAtOnce(selectionProperties.isAllAtOnce());
                            break;
                        case EngravingProperties.PropertieChangeListener.COUNT:
                            e1.properties.setPassCount(selectionProperties.getPassCount());
                            break;
                        case EngravingProperties.PropertieChangeListener.FEED:
                            e1.properties.setFeed(selectionProperties.getFeed());
                            break;
                        case EngravingProperties.PropertieChangeListener.DEPTH:
                            e1.properties.setPassDepth(selectionProperties.getPassDepth());
                            break;
                        case EngravingProperties.PropertieChangeListener.ENABLE:
                            e1.properties.setEnabled(selectionProperties.isEnabled());
                            break;
                        case EngravingProperties.PropertieChangeListener.END:
                            e1.properties.setZEnd(selectionProperties.getZEnd());
                            break;
                        case EngravingProperties.PropertieChangeListener.START:
                            e1.properties.setZStart(selectionProperties.getZStart());
                            break;
                        case EngravingProperties.PropertieChangeListener.POWER:
                            e1.properties.setPower(selectionProperties.getPower());
                            break;
                    }
                }
            });
            return selectionProperties;
        } else 
            return null;
        
        selectionProperties = null;
        return e.properties;
    }
    
    /**
     * @return the herited EngravingProperties of the only one Gelement selected or the GGroup curently <i>openned</i>.
     */
    public EngravingProperties getParentHeritedPropertiesOfSelection() {
        if ( getEditedElement() == document) return null;
        return getHeritedPropertiesOf(getEditedElement());
    }
    
    /** Repaint and refresh listViewer content.
     * @param andGetFocus */
    public void update(boolean andGetFocus) {    
        if( andGetFocus) setKeyFocus(andGetFocus);
        saveState(true);
    }

    private void addSelectedPoints(GCode p) {
        if ( ! selectedPoints.contains(p)) {
            selectedPoints.add(p);
            selectionHasChanged=true;
            invalidate();
        }
    }

    public void dispose() {
        if ( filterFrame != null) filterFrame.dispose();
    }

    public void scaleSelection(double sx, double sy, int count, boolean fromCenter, boolean keepOriginal) {
        if ( selectedElements.isEmpty() || (count < 1)) return;
        
        ArrayList<GElement> res = new ArrayList<>( selectedElements.size() * count);
        ArrayList<GElement> bks = new ArrayList<>();
        transformationOrigin = fromCenter?getCenterOfSelection():coord2DCursor;
        //saveState();
        for ( int n = 0 ; n < count; n++) {
            if ( (n>0) || keepOriginal) {
                bks.clear();
                for( GElement b : selectedElements) bks.add((GElement) b.clone()); 
                
            } else { // remove originals
                @SuppressWarnings("unchecked") 
                final ArrayList<GElement> el = (ArrayList<GElement>) selectedElements.clone();
                bks = el;
                removeAllElements(selectedElements);
            }
            
            selectedElements.clear();
            for( GElement s : bks) {
                GElement b = (GElement) s.clone();
                b.scale( transformationOrigin, sx, sy);
                selectedElements.add(b);
                res.add(b);
            }
        }
        addAll(res);
        saveState(true);
    }

    private ArrayList<GCode> getSelectedLines() {
        ArrayList<GCode>sel = new ArrayList<>();
        for ( int i : gCodeListViewer.getSelectedIndices())
            if ( i < editedElement.size() ) sel.add( editedElement.getLine(i));
        return sel;
    }
    
    public Point2D get2DCursor() {
        return coord2DCursor;
    }

    /**
     * Add elements in the current edited group.
     * @param paths 
     */
    public void addAll(ArrayList<GElement> paths) {     
        assert( editedGroup != null);
        
        if ( paths.isEmpty()) return;
        
        editedGroup.addAll(paths);
        if ( (gcodeFooter != null) && (editedGroup == document)) {
            document.remove(gcodeFooter);
            document.add(gcodeFooter); 
        }
    }

    public void renameSelection(String name) {
        //saveState();
        if ( editedElement != null) editedElement.setName(name);
        else {
            int i = 0;
            if ( selectedElements.size() == 1) selectedElements.get(0).setName(name);
            else
                for( GElement b : selectedElements)
                    b.setName(name + ((i++>0)?""+i:""));
        }
        saveState(true);
        setKeyFocus(true);
    }

    public void setShowGRBLHead(boolean selected) {
        showHead = selected;
        invalidateWithoutUpdateGCodeListViewer();
    }

    public boolean isEmpty() {
        return (document==null) || document.isEmpty();
    }

    /** return the boundary of the selection or null
     * @param onlyEnabled
     * @return  */
    public Rectangle2D getSelectionBoundary(boolean onlyEnabled) {
        Rectangle2D res = null;
        
        if ( editedElement != null) { 
            if ( selectedPoints.isEmpty()) return null; //res = (Rectangle2D) editedElement.getBounds();
            else
                for ( GCode p : selectedPoints)
                    if ( p.isAPoint()) {
                        if ( res == null) res = new Rectangle2D.Double(p.getX(), p.getY(), 1, 1);
                        else res.add( p);
                    }
            
        } else
            if ( onlyEnabled) return GGroup.getEnabledBounds(selectedElements);
            else 
                for( GElement b : selectedElements) 
                 {
                     Rectangle2D r2 = (Rectangle2D) b.getBounds();
                     if ( r2 != null) {
                         if ( res == null) res = r2;
                         else res.add(r2);
                     }
                 }
        
        return res;
    }

    
    private double getAlignPosition(int alignTo) {
        double v, res = Double.POSITIVE_INFINITY;
        if ( (alignTo==ALIGN_BOTTOM)||(alignTo==ALIGN_RIGHT)) 
            res = Double.NEGATIVE_INFINITY;                 
        
        for( GElement b : selectedElements) {
            Rectangle2D r = b.getBounds();
            switch ( alignTo) {
                case ALIGN_LEFT: 
                    if ((v=r.getX()) < res) res = v;
                    break;
                case ALIGN_RIGHT: 
                    if ((v=(r.getX()+r.getWidth())) > res) res = v;
                    break;
                case ALIGN_TOP: 
                    if ((v=r.getY()) < res) res = v;
                    break;
                case ALIGN_BOTTOM: 
                    if ((v=(r.getY()+r.getHeight())) > res) res = v;
                    break;                    
            }
        }
        return res;
    }
 
    public void setShowStartPoints(boolean selected) {
        showStartPoints = selected;
        invalidateWithoutUpdateGCodeListViewer();
    }

    public boolean hasHeaderFooter() {
        return (gcodeHeader!=null) || (gcodeFooter!=null);
    }

    /**
     * Rotate or multiply the selection in the current editedGroup
     * @param angle
     * @param copies
     * @param fromCenter
     * @param keepOriginal
     * @param keepOrientation 
     */
    public void multiRotateSelection(double angle, int copies, boolean fromCenter, boolean keepOriginal, boolean keepOrientation ) {
        if ( selectedElements.isEmpty() || (Math.abs(angle) < 10e-6)) return; 
        
        transformationOrigin=fromCenter?getCenterOfSelection():coord2DCursor; 
        double rotAngle = 0;

        if ( copies == 0) {
            copies = 1;
            keepOriginal = true;
        }
        
        ArrayList<GElement> res = new ArrayList<>( selectedElements.size() * (copies+(keepOriginal?1:0)));
        for( int n = 0; n < copies; n++) { 
            rotAngle += angle;
            for ( GElement el : selectedElements) {
                Point2D c = el.getCenter();
                if ( c != null) {   
                    if ( (copies <= 2) && (! keepOriginal)) {
                        if ( keepOrientation) el.rotate( c, -rotAngle);
                        el.rotate(transformationOrigin, rotAngle);   
                    } else {
                        GElement clone = ((GElement)el.clone());
                        if ( keepOrientation) clone.rotate( c, -rotAngle);
                        clone.rotate(transformationOrigin, rotAngle);                  
                        res.add(clone);
                    }
                }
            }
        }
        if ( copies > 1 ) document.removeAllElements(selectedElements);
        
        if ( ! res.isEmpty()) addAll(res);
        saveState(true);
    }
    
    /**
     * Paint the G0 moving path between feedPaths.
     * @param g
     * @param group
     * @param lastPoint
     * @return the last position of the path of this group
     */
    private GCode paintMoves(Graphics g, GGroup group, GCode lastPoint) {
        for ( GElement e : group.getIterable()) {
            if ( ! isGElementEnabled(e) || (e.getFirstPoint() == null)) continue;
            if ( e instanceof GGroup) 
                lastPoint = paintMoves(g, (GGroup) e, lastPoint);
            else {
                if ( (lastPoint != null) && showMoves) {               
                    Point2D p2=e.getFirstPoint();
                    g.setColor(MOVE_COLOR);
                    g.drawLine((int)(lastPoint.getX()*zoomFactor), (int)(-lastPoint.getY()*zoomFactor), (int)(p2.getX()*zoomFactor), (int)(-p2.getY()*zoomFactor));

                    // Calcul du centre du segment
                    final double centerX = (lastPoint.getX() + p2.getX()) / 2;
                    final double centerY = (lastPoint.getY() + p2.getY()) / 2;

                    // Calcul de la direction du segment (vecteur direction)
                    final double dx = lastPoint.getX() - p2.getX();
                    final double dy = lastPoint.getY() - p2.getY() ;
                    
                    // Calcul des coordonnées des segments de la tête de la flèche
                    final double arrowLength = 20/zoomFactor;  // Longueur de la flèche
                    final double arrowAngle = Math.toRadians(30);  // Angle de la flèche par rapport au segment

                    // Calcul des coordonnées des deux segments de la flèche (à partir du centre)
                    final double arrowX1 = centerX + arrowLength * Math.cos(Math.atan2(dy, dx) + arrowAngle);
                    final double arrowY1 = centerY + arrowLength * Math.sin(Math.atan2(dy, dx) + arrowAngle);

                    final double arrowX2 = centerX + arrowLength * Math.cos(Math.atan2(dy, dx) - arrowAngle);
                    final double arrowY2 = centerY + arrowLength * Math.sin(Math.atan2(dy, dx) - arrowAngle);

                    // Dessiner les segments de la tête de flèche
                    g.drawLine((int) (centerX*zoomFactor), (int) (-centerY*zoomFactor), (int)(arrowX1*zoomFactor), (int)( -arrowY1*zoomFactor));
                    g.drawLine((int) (centerX*zoomFactor), (int) (-centerY*zoomFactor), (int)(arrowX2*zoomFactor), (int)( -arrowY2*zoomFactor));

                }
                lastPoint = e.getLastPoint();
            }
        } 
        return lastPoint; 
    }

    private void paintGroup(PaintContext pc, GGroup group, boolean paintSelection, boolean paintDisabled, GCode lastPoint) {
    
        for ( GElement el : group.getIterable()) {
            if ( el.getFirstPoint() == null) continue;
            
            if ( el instanceof GGroup) 
                paintGroup(pc, (GGroup) el, paintSelection, paintDisabled, lastPoint);
            
            else {
                if ( pc.editedElement == el) continue; // dont paint editedElement here
                
                boolean selected = (selectedElements.stream().anyMatch((e) 
                                        -> ((e==el) || ((e instanceof GGroup) && ((GGroup)e).isParentOf(el)))));    
                
                boolean enabled = isGElementEnabled(el);
                
                if ( paintDisabled ^ (enabled && editedGroup.isParentOf(el)))
                    if ( paintSelection ^ ! selected ) {
                        if ( pc.editedElement != null )
                            pc.color = Color.DARK_GRAY;
                        else
                            pc.color = ! editedGroup.isParentOf(el) ? Color.darkGray :
                                            selected ? (enabled ? PaintContext.SEL_COLOR1 : PaintContext.SEL_DISABLED_COLOR) : 
                                                       (enabled ? Color.white : Color.gray);

                        el.paint(pc);
                    }
            }
        } 
    }

    public boolean isGElementEnabled(GElement el) {
        return el.isEnabled() && getHeritedPropertiesOf(el).isEnabled();
    }

    /** 
     * @return a <i>MOUSE_MODE_...</i>
     */
    public int getMouseMode() {
        return mouseMode;
    }
    

    public boolean isEditedRootGroup() {
        return (editedElement==null) && selectedElements.isEmpty() && ((editedGroup == null) || (editedGroup == document));
    }

    /** Set mouseMode to NONE (restore Cursor, clear origin and rect2D_selection, and repaint. */
    public void clearMouseMode() {
        if ( ! mousePressed) screenMousePressPosition = null;
        mouseRectangleP2 = null;
        coordMouseOrigin = null;

        if ((highlitedPoint == null) && ( editedElement == null)) 
            selectedPoints.clear();
        mouseMode = MOUSE_MODE_NONE;

        setCursor( new Cursor(Cursor.DEFAULT_CURSOR));
        inform("");
        repaint();
    }
    
    public String getSelectedElementName() {
        GElement e = getEditedElement();
        return (e != null) ? e.getName() : "";
    }
        
    /**
     * Return to Parent or stay into <i>Document root</i> remove empty elements.
     */
    public void editParentOrClearSelection() {
        clearMouseMode();
        
        if ( editedElement != null) {
            if ( ! selectedPoints.isEmpty()) {
                clearSelectedPoints();
            } else {
                final GElement e = editedElement;
                final GGroup parent = document.getParent(editedElement);
                if ( editedElement.isEmpty()) {
                    parent.remove(e);
                    setEditedElement(parent);
                }
                else {
                    setEditedElement(parent);
                    selectedElements.add( e);
                }
            }
        } else {        
            if ( ! selectedElements.isEmpty()) {
                selectedElements.clear();
                selectionHasChanged=true;              
            } else       
                if ( editedGroup != document) {              
                    if ( editedGroup.isEmpty()) {
                        GGroup tmp = document.getParent(editedGroup);
                        document.remove(editedGroup);
                        setEditedElement(tmp);                    
                    } else {
                        GGroup p;
                        setEditedElement(document.getParent(p=editedGroup));
                        selectedElements.add(p);    
                    }
                }
        }
        
        invalidate();
    }
    
    private double computeAngle() {
        double arcLen = (360+Math.toDegrees(-G1Path.getAngleInRadian(coord2DCursor, coordSnapPosition)) - lastRotationAngle)%360;
        if ( (Math.abs(arcLen) < 0.0001) || (Math.abs(360-arcLen) < 0.0001)) arcLen=360;
        else if ( arcLen < 0) arcLen = 360+arcLen;   
        if ( arcLen > 180) arcLen = 360 - arcLen;
        return GWord.round(arcLen);
    }
    
    /**
     * @param onlySelection execute only selected paths
     * @param withHeaderFooter if onlySelection is true, include header footer of the document to returned gcode
     * @return the real elements to execute (not a copy)
     */
    public GGroup getDocumentToExecute(boolean onlySelection, boolean withHeaderFooter) {
        if ( onlySelection) {
            if ( selectedElements.isEmpty()) return null;
            else {
                GGroup res = new GGroup("<selection>");
                
                if ( withHeaderFooter && (gcodeHeader!=null)) res.add(gcodeHeader);
                res.addAll(selectedElements);
                if ( withHeaderFooter && (gcodeFooter!=null)) res.add(gcodeFooter);
                
                // get herited defaults engravings properties
                res.properties = document.getHeritedEngravingPropreties(editedGroup);                
                res.properties.setAllAtOnce(false);
                return res;
            }
        }
        return document;
    }

    public void copyOpenScadCodeOfSelectedShapeToClipboard() {
        String res = "";
        HashMap<String, Integer> names = new HashMap<>();
        if ( selectedElements.isEmpty()) res =  "// No path to copy";
        else {
            for( GElement path0 : selectedElements) {
                GElement e = path0.flatten();
                if ( ! ((e instanceof G1Path) || (e instanceof GGroup))) continue;
                
                if ( e.getName().startsWith("flatten-"))
                        e.setName( e.getName().substring(8));
                
                final GElement path = e;
                Rectangle2D bounds = path.getBounds();
          
                String name = path.getName().replace('-', '_');
                if ( names.containsKey(name)) {
                    int i;
                    names.put(name, i=(names.get(name)+1));
                    name += i;
                } else
                    names.put(name,0);
                
                res += "\n" + name + "_bounds=[" + bounds.getX() + ", " + bounds.getY() +
                                    ", " + bounds.getWidth() + ", " + 
                                    bounds.getHeight() + "];\n"+ name + " = [\n\t";

                int nbp = path.getNbPoints();
                GCode last=null;
                for ( int p = 0; p < nbp; p++) {
                    GCode l = ((G1Path)path).getPoint(p);
                    if ((last!=null) && l.isAtSamePosition(last)) continue;
                    last = l;
                        res +=  String.format(Locale.ROOT, "[%.6f,%.6f]", l.getX(), l.getY());
                        if ( (p + 1) < nbp) {                  
                            if ( ((p+1) % 3) == 0)
                                res += ",\n\t";
                            else
                                res += ", ";
                        }
                    
                }
                res += "];\n";
            }
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(res), null);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
