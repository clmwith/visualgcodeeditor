/*
 * Copyright (C) 2019 Clément Gérardin @ Marseille.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed serialReader the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have receivedLine a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gcodeeditor.gui;

import gcodeeditor.gui.dialogs.CylindricalPocketInputPanel;
import gcodeeditor.gui.dialogs.SphericalPocketInputPanel;
import gcodeeditor.gui.dialogs.JScalePanel;
import gcodeeditor.gui.dialogs.JRotationPanel;
import gcodeeditor.gui.dialogs.JPolygonPanel;
import gcodeeditor.gui.dialogs.JDuplicatePanel;
import gcodeeditor.BackgroundPictureParameters;
import gelements.GSpline;
import gelements.EngravingProperties;
import gelements.GElement;
import gelements.G1Path;
import gcodeeditor.GCode;
import gelements.GGroup;
import gelements.GArc;
import gelements.GCylindricalPocket;
import gelements.GDrillPoint;
import gelements.GearHelper;
import gcodeeditor.GWord;
import gcodeeditor.Configuration;
import gcodeeditor.JBlockViewerListenerInterface;
import gcodeeditor.JBlocksViewer;
import gelements.GSphericalPocket;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import gcodeeditor.GRBLControler;
import gcodeeditor.gui.dialogs.DialogManager;
import gcodeeditor.gui.dialogs.JMovePanel;
import gelements.GTextOnPath;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TooManyListenersException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.ParseException;
import org.xml.sax.SAXException;

/**
 * The main frame of the application.
 * @author Clément
 */
public class JEditorFrame extends javax.swing.JFrame implements JBlockViewerListenerInterface, JConfigurationFrame.JConfigurationChangeListener {

    public JBlocksViewer blocksviewer;
    private File curDir = null;
    private JConfigurationFrame confFrame;

    private static int numberOfWindowsOpenned=0;
    private String savedFileName;
    
    private static Window activeWindow; // the active window display GRBL receivedMessage
    private final DialogManager dialogManager = new DialogManager(this);
    
    private static GRBLControler grbl;
    private final GRBLControler.GRBLCommListennerInterface grblListenner;
    private BackgroundPicturePanel backgroundPicturePanel;
    private SphericalPocketInputPanel spherePocketEditor;
    
    private JLogFrame jLogFrame;
    private JJoGFrame jogWindow;
    
    private boolean showLaserPosition;
    
    private EngravingProperties curentEditedProperties;
    
    public static File lastImportDir = null;
    
    /**
     * Creates new form NewJFrame
     * @param addHeaderFooter
     * @param newEmptyDoc   create document without header/footer blocks
     */
    public JEditorFrame( boolean addHeaderFooter, boolean newEmptyDoc) {
        if ( grbl == null) grbl = new GRBLControler();
        blocksviewer = new JBlocksViewer(newEmptyDoc);
        numberOfWindowsOpenned++;
        confFrame = new JConfigurationFrame(blocksviewer.getConfiguration(), this);

        initComponents();
        if ( blocksviewer.getConfiguration().visualSettings.equals("")) {
            blocksviewer.setSnapToGrid( jCheckBoxMenuItemSnapGrid.isSelected());
            blocksviewer.setSnapToPoints(jCheckBoxMenuItemSnapPoints.isSelected());
            blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_GRID, jCheckBoxMenuItemShowGrid.isSelected()?1:0, null);
        }       
        remove(jPanelEditor);
        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, blocksviewer, jPanelEditor);
        p.setDividerSize(2);
        add(p, java.awt.BorderLayout.CENTER);
        //add(blocksviewer, java.awt.BorderLayout.CENTER);
        pack();
        p.setDividerLocation(0.85);
        p.setResizeWeight(1);
        
        blocksviewer.setListener(this);
        blocksviewer.setListEditor(jListGCode, jPanelEditor);
        
        jListGCode.setPrototypeCellValue("                    ");
        jListGCode.setCellRenderer( new DefaultListCellRenderer() { 
            @Override
            public Component getListCellRendererComponent(JList list, 
                                              Object value, 
                                              int index, boolean isSelected, 
                                              boolean cellHasFocus) {     
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
                
                if ( value instanceof GElement) {
                    if( value instanceof GGroup)  
                        setFont(getFont().deriveFont(Font.BOLD | (((GGroup)value).containsDisabledElement() ? Font.ITALIC : 0)));
                            
                    if ( ! blocksviewer.isGElementEnabled(((GElement)value)))
                            setForeground(Color.lightGray);
                    else if ( ((GElement)value).hasCustomProperties(false))
                            setForeground(Color.blue);
                         else
                            setForeground(Color.black);

                }
                return this;  
            }
        });
        
        ListAction la = new ListAction(jListGCode);   
        la.setAction(ListAction.ENTER, new EditListAction(blocksviewer));
        la.setAction(ListAction.INSERT, new AbstractAction() { // called when INSERT key on the jList
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( (blocksviewer.getState() & JBlocksViewer.STATE_EDIT_MODE_FLAG) == 0) return;
                if ( jListGCode.getMinSelectionIndex() != -1 ) {
                    int pos = jListGCode.getMinSelectionIndex();
                    if ( pos == -1) pos = 0;
                    blocksviewer.doAction(JBlocksViewer.ACTION_INSERT, 0, null);
                    Action action = jListGCode.getActionMap().get(ListAction.ENTER);
                    if (action != null)
                    {
                        jListGCode.requestFocusInWindow();
                        jListGCode.setSelectedIndex(pos);
                        ActionEvent event = new ActionEvent(
                                jListGCode,
                                ActionEvent.ACTION_PERFORMED,
                                "");
                        action.actionPerformed(event);
                    }
                }
            }
        });
        la.setAction(ListAction.ESCAPE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.editParentOrClearSelection();
            }
        });
        la.setAction(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_CUT, 0, null);
            }
        });
        la.setAction(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_PASTE, 0, null);
            }
        });
        la.setAction(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_COPY, 0, null);
            }
        });
        la.setAction(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_DOWN, 0, null);
            }
        });
        la.setAction(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_UP, 0, null);
            }
        });
        la.setAction(ListAction.DELETE1, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_DELETE, 0, null);
            }
        });
        la.setAction(ListAction.DELETE2, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blocksviewer.doAction(JBlocksViewer.ACTION_DELETE, 0, null);
            }
        });
        la.setAction(ListAction.F2, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {               
                String name = JOptionPane.showInputDialog(blocksviewer, "New name", blocksviewer.getSelectedBlockName());
                if ( name != null) blocksviewer.renameSelection(name);
            }
        });
        if ( addHeaderFooter) {
            blocksviewer.addDefaultGCodeHeaderFooter();
        }
        
        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { closeWindows(); }   
        });
        
        final Window thisFrame = this;
        grbl.addListenner(grblListenner = new GRBLControler.GRBLCommListennerInterface() {
            @Override
            public void wPosChanged() { 
                blocksviewer.setGRBLMachinePosition( grbl.getWPos());
            }
            @Override
            public void stateChanged() {
                jLabelGRBLState.setText(GRBLControler.GRBL_STATE_STR[grbl.getState()]);
                switch( grbl.getState()) {
                    case GRBLControler.GRBL_STATE_ALARM : 
                        jLabelGRBLState.setForeground(new Color(220,0,0));
                        break;
                    case GRBLControler.GRBL_STATE_HOME:
                    case GRBLControler.GRBL_STATE_JOG:
                    case GRBLControler.GRBL_STATE_RUN:
                        jLabelGRBLState.setForeground(new Color(240, 195, 0));
                        break;
                    case GRBLControler.GRBL_STATE_IDLE:
                        jLabelGRBLState.setForeground(new Color(24, 157, 30));
                        break;
                    case GRBLControler.GRBL_STATE_DISCONNECTED:
                        updateTitle();                       
                    default:
                        jLabelGRBLState.setForeground(Color.black);
                }
                SwingUtilities.invokeLater(() -> { updateGUIAndStatus(null); });
            }
            @Override
            public void receivedError(int errorno, String line) {
                stopShowBoundariesThread=true;
                if ((activeWindow == thisFrame) && (! printDialog.isVisible()))
                    showGRBLError(errorno, line);
            }
            @Override
            public void receivedAlarm(int alarmno) {
                stopShowBoundariesThread=true;
                if ( activeWindow == thisFrame) 
                    showGRBLAlarm(alarmno);
                stateChanged();
            }
            @Override
            public void sendedLine(String grblCmd) { 
                if ( jLogFrame != null) jLogFrame.addLog( grblCmd, true);
            }
            @Override
            public void receivedLine(String grblResponse) { 
                if ( jLogFrame != null) jLogFrame.addLog( grblResponse, false);
            }
            @Override
            public void overrideChanged() { }
            @Override
            public void receivedMessage(String grblMessage) {
                if ( activeWindow == thisFrame) {
                    showGRBLMessage(grblMessage);
                }
            }
            @Override
            public void exceptionInGRBLComThread(Exception ex) {
                SwingUtilities.invokeLater(() -> { 
                    JOptionPane.showMessageDialog(thisFrame, "Exception in sender, GRBL connexion closed : \n" + ex.toString(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
                if ( grbl.isConnected() && ! grbl.isIdle()) grbl.softReset();
            }
            @Override
            public void settingsReady() {
                if ( confFrame.getConfiguration(grbl.getVersion().split(":")[2])) {
                    confFrame.updateGUI();
                    blocksviewer.applyConfiguration();
                } else {
                    EventQueue.invokeLater(() -> {
                        JOptionPane.showMessageDialog(thisFrame, "The configuration for GRBL \"" + grbl.getVersion().split(":")[2] + "\" is not found.", 
                                "Configuration not found...", JOptionPane.INFORMATION_MESSAGE);
                    });
                } 
                updateTitle();
            }
            @Override
            public void accessoryStateChanged() { }
            @Override
            public void feedSpindleChanged() { }
            @Override
            public void probFinished(String substring) { }

            @Override
            public void limitSwitchChanged() {
                String lsv = grbl.getLimitSwitchValues();
                EventQueue.invokeLater(() -> {
                    if ( lsv.isEmpty() ) jLabelMessage.setText("");
                    else jLabelMessage.setText("LimitSwitch: " + lsv);
                    jLabelMessage.invalidate();                    
                });
            }
        });
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if ( e.getWindow() instanceof JEditorFrame) 
                    activeWindow = e.getWindow();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if ( e.getWindow() instanceof JEditorFrame) 
                    activeWindow = e.getWindow();
            }
        });

        configurationChanged();
        updateRecentFilesMenu();
        updateGUIAndStatus("");
        updateTitle();
    }
    
    
    /**
     * Creates new form NewJFrame
     * @param gcodeFileName the GCode file to load into.
     * @throws IOException 
     */
    public JEditorFrame( String gcodeFileName) throws IOException {
        this(false, false);
        
        if ( gcodeFileName.lastIndexOf('/') != -1 )
            curDir = new File(gcodeFileName.substring(0, gcodeFileName.lastIndexOf('/')));
        else 
            curDir = new File(".");
        
        
        blocksviewer.setContent(JBlocksViewer.importGCODE(gcodeFileName, blocksviewer.getBackgroundPictureParameters()), true);
        savedFileName = gcodeFileName;
        updateTitle();
        blocksviewer.doAction(JBlocksViewer.ACTION_FOCUS_VIEW, 1, null);
    }
    
    private void closeWindows() { 
        
        if ( ((blocksviewer.getState() & JBlocksViewer.STATE_DOCUMENT_MODIFIED) != 0)) {
            switch ( JOptionPane.showConfirmDialog(this, "Save this document before closing ?", 
                    "Save document ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                        jMenuItemSaveGCodeActionPerformed(null);
                case JOptionPane.NO_OPTION:
                        break;
                case JOptionPane.CANCEL_OPTION:
                default:
                        return;
            }
        }  
        grbl.removeListenner(grblListenner);
        
        numberOfWindowsOpenned--;
        if ( numberOfWindowsOpenned == 0)  
            java.awt.EventQueue.invokeLater(() -> { 
                grbl.disconnect(false);
                System.exit(0);
            });
        else java.awt.EventQueue.invokeLater(() -> {
                    blocksviewer.dispose();
                    dispose();
             });
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelStatus = new javax.swing.JPanel();
        jLabelFormInfo = new javax.swing.JLabel();
        jLabelMousePosition = new javax.swing.JLabel();
        jLabelMessage = new javax.swing.JLabel();
        jPanelEditor = new javax.swing.JPanel();
        jPanelEditedInfo = new javax.swing.JPanel();
        jPanelProperties = new javax.swing.JPanel();
        jCheckBoxDisabled = new javax.swing.JCheckBox();
        jCheckBoxAllAtOnce = new javax.swing.JCheckBox();
        jLabelFeed = new javax.swing.JLabel();
        jTextFieldFeed = new javax.swing.JTextField();
        jLabelPower = new javax.swing.JLabel();
        jTextFieldPower = new javax.swing.JTextField();
        jLabelPass = new javax.swing.JLabel();
        jTextFieldPass = new javax.swing.JTextField();
        jLabelStart = new javax.swing.JLabel();
        jTextFieldZStart = new javax.swing.JTextField();
        jLabelDepth = new javax.swing.JLabel();
        jTextFieldPassDepht = new javax.swing.JTextField();
        jLabelEnd = new javax.swing.JLabel();
        jTextFieldZEnd = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabelContent = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabelEditType = new javax.swing.JLabel();
        jTextFieldEditedBlock = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListGCode = new javax.swing.JList<Object>() {
            public String getToolTipText( MouseEvent e )
            {
                int row = locationToIndex( e.getPoint() );
                return blocksviewer.getListToolTipForRow(row);
            }

        }
        ;
        jToolBar1 = new javax.swing.JToolBar();
        jButtonNew = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jSeparator21 = new javax.swing.JToolBar.Separator();
        jButtonCopy = new javax.swing.JButton();
        jButtonCut = new javax.swing.JButton();
        jButtonPaste = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonUndo = new javax.swing.JButton();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        jToggleButtonAddLines = new javax.swing.JToggleButton();
        jToggleButtonAddRects = new javax.swing.JToggleButton();
        jToggleButtonAddCircles = new javax.swing.JToggleButton();
        jButtonText = new javax.swing.JButton();
        jToggleButtonZoom = new javax.swing.JToggleButton();
        jToggleButtonShowDistance = new javax.swing.JToggleButton();
        jToggleButtonShowAngle = new javax.swing.JToggleButton();
        jSeparator22 = new javax.swing.JToolBar.Separator();
        jButtonExecute = new javax.swing.JButton();
        jButtonExecSelection = new javax.swing.JButton();
        jButtonGRBLHome = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jButtonKillAlarm = new javax.swing.JButton();
        jToggleButtonHold = new javax.swing.JToggleButton();
        jToggleButtonMoveHead = new javax.swing.JToggleButton();
        jToggleButtonShowLaser = new javax.swing.JToggleButton();
        jSeparator23 = new javax.swing.JToolBar.Separator();
        jLabelGRBLState = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemNew = new javax.swing.JMenuItem();
        jMenuItemOpenGCode = new javax.swing.JMenuItem();
        jMenuRecent = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSaveGCode = new javax.swing.JMenuItem();
        jMenuItemSaveAs = new javax.swing.JMenuItem();
        jMenuItemExportSVG = new javax.swing.JMenuItem();
        jMenuItemExportDXF = new javax.swing.JMenuItem();
        jSeparator31 = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItemConf = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItemQuit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemSelectAll = new javax.swing.JMenuItem();
        jMenuItemInvertSelection = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        jMenuItemUndo = new javax.swing.JMenuItem();
        jMenuItemRedo = new javax.swing.JMenuItem();
        jSeparator27 = new javax.swing.JPopupMenu.Separator();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExtract = new javax.swing.JMenuItem();
        jMenuItemRemoveSelection = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSetCursor = new javax.swing.JMenuItem();
        jMenuItemSetCursorTo = new javax.swing.JMenuItem();
        jMenuItemCursorAtCenter = new javax.swing.JMenuItem();
        jMenuItemCursorAtHead = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuItemSnapGrid = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemSnapPoints = new javax.swing.JCheckBoxMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jMenuItemGroup = new javax.swing.JMenuItem();
        jMenuItemUngroup = new javax.swing.JMenuItem();
        jSeparator25 = new javax.swing.JPopupMenu.Separator();
        jMenuItemMoveUp = new javax.swing.JMenuItem();
        jMenuItemMoveDown = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        jCheckBoxMenuItenItemShowPicture = new javax.swing.JCheckBoxMenuItem();
        jMenuItemDistance = new javax.swing.JMenuItem();
        jCheckBoxMenuItemShowEditor = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowHeadPosition = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowGrid = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowMoves = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowObjectSurface = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowStart = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowToolBar = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowWorkspace = new javax.swing.JCheckBoxMenuItem();
        jSeparator28 = new javax.swing.JPopupMenu.Separator();
        jMenuItemFocus = new javax.swing.JMenuItem();
        jMenuBlocks = new javax.swing.JMenu();
        jMenuAdds = new javax.swing.JMenu();
        jMenuItemAddMixedPath = new javax.swing.JMenuItem();
        jMenuItemAddcurve = new javax.swing.JMenuItem();
        jMenuItemAddCustom = new javax.swing.JMenuItem();
        jSeparator33 = new javax.swing.JPopupMenu.Separator();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemAddArc = new javax.swing.JMenuItem();
        jMenuItemAddCircle = new javax.swing.JMenuItem();
        jMenuItemAddCurvesCircle = new javax.swing.JMenuItem();
        jMenuItemAddOval = new javax.swing.JMenuItem();
        jMenuItemAddDrill = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItemAddCross = new javax.swing.JMenuItem();
        jMenuItemAddGear = new javax.swing.JMenuItem();
        jMenuItemAddRipple = new javax.swing.JMenuItem();
        jMenuItemAddRoubndRect = new javax.swing.JMenuItem();
        jMenuItemAddSpiral = new javax.swing.JMenuItem();
        jMenuItemAddStar = new javax.swing.JMenuItem();
        jMenuItemAddRectangle = new javax.swing.JMenuItem();
        jMenuItemAddPolygon = new javax.swing.JMenuItem();
        jMenuItemAddText = new javax.swing.JMenuItem();
        jMenuItemAddTextOnPath = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAddBounds = new javax.swing.JMenuItem();
        jMenuMakeCutPath = new javax.swing.JMenu();
        jMenuItemMakeCutPathI = new javax.swing.JMenuItem();
        jMenuItemMakeCutPathO = new javax.swing.JMenuItem();
        jMenuItemMakeFlatten = new javax.swing.JMenuItem();
        jMenuItemAddHull = new javax.swing.JMenuItem();
        jMenuItemAddLinkedPath = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItemAddPocket = new javax.swing.JMenuItem();
        jSeparator29 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAddCylindricalPocket = new javax.swing.JMenuItem();
        jMenuItemAddSphericalPocket = new javax.swing.JMenuItem();
        jMenuItemImport = new javax.swing.JMenuItem();
        jSeparator32 = new javax.swing.JPopupMenu.Separator();
        jMenuAlign = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItemAlignHorizontaly = new javax.swing.JMenuItem();
        jMenuItemAlignVertical = new javax.swing.JMenuItem();
        jSeparator26 = new javax.swing.JPopupMenu.Separator();
        jMenuItemAlignBottom = new javax.swing.JMenuItem();
        jMenuItemAlignLeft = new javax.swing.JMenuItem();
        jMenuItemAlignRight = new javax.swing.JMenuItem();
        jMenuItemAlignTop = new javax.swing.JMenuItem();
        jMenuItemDuplicate = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItemFlipH = new javax.swing.JMenuItem();
        jMenuItemFlipV = new javax.swing.JMenuItem();
        jMenuItemReverse = new javax.swing.JMenuItem();
        jMenuItemJoin = new javax.swing.JMenuItem();
        jMenuItemMoveWithMouse = new javax.swing.JMenuItem();
        jMenuItemMove = new javax.swing.JMenuItem();
        jMenuItemMoveCenter = new javax.swing.JMenuItem();
        jMenuItemRename = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemRotate = new javax.swing.JMenuItem();
        jMenuItemRotateCenter = new javax.swing.JMenuItem();
        jMenuItemRotateP = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItemScale = new javax.swing.JMenuItem();
        jMenuItemScaleCenter = new javax.swing.JMenuItem();
        jMenuItemScaleP = new javax.swing.JMenuItem();
        jSeparator24 = new javax.swing.JPopupMenu.Separator();
        jMenuGCODE = new javax.swing.JMenu();
        jMenuItemAddHeaderFooter = new javax.swing.JMenuItem();
        jMenuItemSort = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSetAsHeader = new javax.swing.JMenuItem();
        jMenuItemSetAsFooter = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        jMenuItemFilter = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemCopyOpenScadPolygon = new javax.swing.JMenuItem();
        jMenuPoints = new javax.swing.JMenu();
        jMenuItemAddPoints = new javax.swing.JMenuItem();
        jMenuItemAddFromHeadPos = new javax.swing.JMenuItem();
        jMenuItemAddIntersectionPoints = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        jMenuItemChStartPos = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSimplifyP = new javax.swing.JMenuItem();
        jMenuItemSimplifyByDistance = new javax.swing.JMenuItem();
        jMenuItemSimplify = new javax.swing.JMenuItem();
        jMenuGRBL = new javax.swing.JMenu();
        jMenuItemGRBLConnect = new javax.swing.JMenuItem();
        jMenuItemGRBLDisconnect = new javax.swing.JMenuItem();
        jMenuItemGRBLSettings = new javax.swing.JMenuItem();
        jMenuItemGRBLKillAlarm = new javax.swing.JMenuItem();
        jMenuItemGRBLSoftReset = new javax.swing.JMenuItem();
        jSeparator30 = new javax.swing.JPopupMenu.Separator();
        jMenuItemGRBLHome = new javax.swing.JMenuItem();
        jMenuItemGRBLSetMPos = new javax.swing.JMenuItem();
        jMenuItemGRBLWPosAsMPos = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        jMenuItemGRBLJogWindow = new javax.swing.JMenuItem();
        jMenuItemGRBLMoveHead = new javax.swing.JMenuItem();
        jMenuItemGRBLCmd = new javax.swing.JMenuItem();
        jCheckBoxMenuItemGRBLShowLaserPosition = new javax.swing.JCheckBoxMenuItem();
        jMenuItemGRBLShowLogWindow = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMenuItemGRBLShowBoundaries = new javax.swing.JMenuItem();
        jMenuItemExecuteAll = new javax.swing.JMenuItem();
        jMenuItemExecuteSelected = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("VGEditor");

        jPanelStatus.setLayout(new java.awt.BorderLayout());

        jLabelFormInfo.setText(" ");
        jLabelFormInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabelFormInfo.setPreferredSize(new java.awt.Dimension(400, 15));
        jPanelStatus.add(jLabelFormInfo, java.awt.BorderLayout.CENTER);

        jLabelMousePosition.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelMousePosition.setText("(0,0)");
        jLabelMousePosition.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabelMousePosition.setInheritsPopupMenu(false);
        jLabelMousePosition.setPreferredSize(new java.awt.Dimension(200, 15));
        jPanelStatus.add(jLabelMousePosition, java.awt.BorderLayout.WEST);

        jLabelMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelMessage.setText(" ");
        jLabelMessage.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabelMessage.setPreferredSize(new java.awt.Dimension(300, 15));
        jPanelStatus.add(jLabelMessage, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanelStatus, java.awt.BorderLayout.SOUTH);

        jPanelEditor.setLayout(new java.awt.BorderLayout());

        jPanelEditedInfo.setPreferredSize(new java.awt.Dimension(180, 240));
        jPanelEditedInfo.setLayout(new java.awt.BorderLayout());

        jPanelProperties.setLayout(new java.awt.GridLayout(0, 2));

        jCheckBoxDisabled.setText("Disable");
        jCheckBoxDisabled.setToolTipText("<html>Exclude this element of the execution.<br>Can be used to set drawing reperes</html>");
        jCheckBoxDisabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisabledActionPerformed(evt);
            }
        });
        jPanelProperties.add(jCheckBoxDisabled);

        jCheckBoxAllAtOnce.setText("All at once");
        jCheckBoxAllAtOnce.setToolTipText("<html>All element of this group will be executed together for each pass</html>");
        jCheckBoxAllAtOnce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAllAtOnceActionPerformed(evt);
            }
        });
        jPanelProperties.add(jCheckBoxAllAtOnce);

        jLabelFeed.setText("Feed Rate");
        jLabelFeed.setToolTipText("Set custom feed rate for this element or leave empty to use herited value");
        jPanelProperties.add(jLabelFeed);

        jTextFieldFeed.setToolTipText("Feed rate for engraving moves");
        jTextFieldFeed.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldFeedFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldFeedFocusLost(evt);
            }
        });
        jTextFieldFeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFeedActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldFeed);

        jLabelPower.setText("Power (spindle)");
        jLabelPower.setToolTipText("Laser power or Spindle speed");
        jPanelProperties.add(jLabelPower);

        jTextFieldPower.setToolTipText("The power of the laser or spin for this element (and his childs)");
        jTextFieldPower.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldPowerFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldPowerFocusLost(evt);
            }
        });
        jTextFieldPower.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPowerActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldPower);

        jLabelPass.setText("Pass count");
        jLabelPass.setToolTipText("");
        jPanelProperties.add(jLabelPass);

        jTextFieldPass.setToolTipText("Ignored if Z parameters are set");
        jTextFieldPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldPassFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldPassFocusLost(evt);
            }
        });
        jTextFieldPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPassActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldPass);

        jLabelStart.setText("Z start");
        jPanelProperties.add(jLabelStart);

        jTextFieldZStart.setToolTipText("Start feeding at this level");
        jTextFieldZStart.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldZStartFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldZStartFocusLost(evt);
            }
        });
        jTextFieldZStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldZStartActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldZStart);

        jLabelDepth.setText("Pass depth");
        jPanelProperties.add(jLabelDepth);

        jTextFieldPassDepht.setToolTipText("Maximum vertical distance between each pass");
        jTextFieldPassDepht.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldPassDephtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldPassDephtFocusLost(evt);
            }
        });
        jTextFieldPassDepht.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPassDephtActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldPassDepht);

        jLabelEnd.setText("Z end");
        jPanelProperties.add(jLabelEnd);

        jTextFieldZEnd.setToolTipText("Feed up to this Z level");
        jTextFieldZEnd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldZEndFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldZEndFocusLost(evt);
            }
        });
        jTextFieldZEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldZEndActionPerformed(evt);
            }
        });
        jPanelProperties.add(jTextFieldZEnd);

        jPanelEditedInfo.add(jPanelProperties, java.awt.BorderLayout.CENTER);

        jLabelContent.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabelContent.setText("GCode");
        jPanel1.add(jLabelContent);

        jPanelEditedInfo.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabelEditType.setText("Name ");
        jPanel2.add(jLabelEditType, java.awt.BorderLayout.WEST);

        jTextFieldEditedBlock.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldEditedBlockFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldEditedBlockFocusLost(evt);
            }
        });
        jTextFieldEditedBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldEditedBlockActionPerformed(evt);
            }
        });
        jPanel2.add(jTextFieldEditedBlock, java.awt.BorderLayout.CENTER);

        jPanelEditedInfo.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanelEditor.add(jPanelEditedInfo, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jListGCode.setFont(new java.awt.Font("Courier 10 Pitch", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(jListGCode);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanelEditor.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelEditor, java.awt.BorderLayout.LINE_END);

        jToolBar1.setRollover(true);

        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/New24.gif"))); // NOI18N
        jButtonNew.setFocusable(false);
        jButtonNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNew);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Save24.gif"))); // NOI18N
        jButtonSave.setFocusable(false);
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSave);
        jToolBar1.add(jSeparator21);

        jButtonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Copy24.gif"))); // NOI18N
        jButtonCopy.setToolTipText("Copy selection");
        jButtonCopy.setFocusable(false);
        jButtonCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonCopy);

        jButtonCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Cut24.gif"))); // NOI18N
        jButtonCut.setToolTipText("Cut selection");
        jButtonCut.setFocusable(false);
        jButtonCut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCutActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonCut);

        jButtonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Paste24.gif"))); // NOI18N
        jButtonPaste.setToolTipText("Paste");
        jButtonPaste.setFocusable(false);
        jButtonPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPasteActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonPaste);

        jButtonDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Delete24.gif"))); // NOI18N
        jButtonDelete.setToolTipText("Delete selection");
        jButtonDelete.setFocusable(false);
        jButtonDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonDelete);

        jButtonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Undo24.gif"))); // NOI18N
        jButtonUndo.setToolTipText("Undo");
        jButtonUndo.setFocusable(false);
        jButtonUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUndoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonUndo);
        jToolBar1.add(jSeparator15);

        jToggleButtonAddLines.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Line.gif"))); // NOI18N
        jToggleButtonAddLines.setToolTipText("<html>Add a path in the current group or a line in edited element.<br>Insert new point with right buton<p>Shortcut key : <b>L</b></html>");
        jToggleButtonAddLines.setFocusable(false);
        jToggleButtonAddLines.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonAddLines.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonAddLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonAddLinesActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonAddLines);

        jToggleButtonAddRects.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Rectangle.gif"))); // NOI18N
        jToggleButtonAddRects.setToolTipText("<html>Add Rectangle with mouse<br>\nUse Shift, Ctrl and Alt keys and Snap.\n</html>");
        jToggleButtonAddRects.setFocusable(false);
        jToggleButtonAddRects.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonAddRects.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonAddRects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonAddRectsActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonAddRects);

        jToggleButtonAddCircles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/CCircle.gif"))); // NOI18N
        jToggleButtonAddCircles.setToolTipText("<html>Add oval with mouse<br>\nUse <b>Shift</b> and <b>Ctrl</b> and <b>Alt</b> keys combinaisons to Snap the way you want !\n</html>");
        jToggleButtonAddCircles.setFocusable(false);
        jToggleButtonAddCircles.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonAddCircles.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonAddCircles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonAddCirclesActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonAddCircles);

        jButtonText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Text.gif"))); // NOI18N
        jButtonText.setToolTipText("Add text");
        jButtonText.setFocusable(false);
        jButtonText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTextActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonText);

        jToggleButtonZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ZoomIn24.gif"))); // NOI18N
        jToggleButtonZoom.setToolTipText("Focus to the selection or with mouse.");
        jToggleButtonZoom.setFocusable(false);
        jToggleButtonZoom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonZoom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonZoomActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonZoom);

        jToggleButtonShowDistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Distance.gif"))); // NOI18N
        jToggleButtonShowDistance.setToolTipText("Show distance between two points");
        jToggleButtonShowDistance.setFocusable(false);
        jToggleButtonShowDistance.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonShowDistance.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonShowDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonShowDistanceActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonShowDistance);

        jToggleButtonShowAngle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Angle.gif"))); // NOI18N
        jToggleButtonShowAngle.setToolTipText("Show 2D cursor angle with two points");
        jToggleButtonShowAngle.setFocusable(false);
        jToggleButtonShowAngle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonShowAngle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonShowAngle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonShowAngleActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonShowAngle);
        jToolBar1.add(jSeparator22);

        jButtonExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Execute.gif"))); // NOI18N
        jButtonExecute.setToolTipText("Execute document");
        jButtonExecute.setFocusable(false);
        jButtonExecute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExecute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonExecute);

        jButtonExecSelection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ExecuteDocument.gif"))); // NOI18N
        jButtonExecSelection.setToolTipText("Execute selection");
        jButtonExecSelection.setFocusable(false);
        jButtonExecSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExecSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExecSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecSelectionActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonExecSelection);

        jButtonGRBLHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Home24.gif"))); // NOI18N
        jButtonGRBLHome.setToolTipText("GRBL Home");
        jButtonGRBLHome.setFocusable(false);
        jButtonGRBLHome.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGRBLHome.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGRBLHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGRBLHomeActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonGRBLHome);

        jButtonStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Stop24.gif"))); // NOI18N
        jButtonStop.setToolTipText("GRBL SoftReset");
        jButtonStop.setFocusable(false);
        jButtonStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonStop);

        jButtonKillAlarm.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/KillAlarm.gif"))); // NOI18N
        jButtonKillAlarm.setToolTipText("Kill GRBL alarm");
        jButtonKillAlarm.setFocusable(false);
        jButtonKillAlarm.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonKillAlarm.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonKillAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKillAlarmActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonKillAlarm);

        jToggleButtonHold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/Hold.gif"))); // NOI18N
        jToggleButtonHold.setToolTipText("Hold or resume GRBL operation.");
        jToggleButtonHold.setFocusable(false);
        jToggleButtonHold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonHold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonHoldActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonHold);

        jToggleButtonMoveHead.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/MoveHead.gif"))); // NOI18N
        jToggleButtonMoveHead.setToolTipText("Move Gantry with mouse click on document");
        jToggleButtonMoveHead.setFocusable(false);
        jToggleButtonMoveHead.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonMoveHead.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonMoveHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonMoveHeadActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonMoveHead);

        jToggleButtonShowLaser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/LaserOff.gif"))); // NOI18N
        jToggleButtonShowLaser.setToolTipText("<html>Enable laser <i>(at minimum power)</i> to show <b>real</b> position in reality !</html>");
        jToggleButtonShowLaser.setFocusable(false);
        jToggleButtonShowLaser.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButtonShowLaser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleButtonShowLaser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonShowLaserActionPerformed(evt);
            }
        });
        jToolBar1.add(jToggleButtonShowLaser);
        jToolBar1.add(jSeparator23);

        jLabelGRBLState.setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        jLabelGRBLState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelGRBLState.setText(grbl.isConnected() ? grbl.getStateStr() : "");
        jToolBar1.add(jLabelGRBLState);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        jMenuFile.setText("Document");

        jMenuItemNew.setText("New");
        jMenuItemNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemNew);

        jMenuItemOpenGCode.setText("Open");
        jMenuItemOpenGCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenGCodeActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOpenGCode);

        jMenuRecent.setText("Open recent");
        jMenuFile.add(jMenuRecent);
        jMenuFile.add(jSeparator1);

        jMenuItemSaveGCode.setText("Save");
        jMenuItemSaveGCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveGCodeActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveGCode);

        jMenuItemSaveAs.setText("Save as ...");
        jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAsActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveAs);

        jMenuItemExportSVG.setText("Export as SVG ...");
        jMenuItemExportSVG.setToolTipText("Export the document or selection to a SVG file");
        jMenuItemExportSVG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportSVGActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExportSVG);

        jMenuItemExportDXF.setText("Export to DXF ...");
        jMenuItemExportDXF.setToolTipText("Export document or selection to DXF file");
        jMenuItemExportDXF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportDXFActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExportDXF);
        jMenuFile.add(jSeparator31);

        jMenuItem2.setText("Print ...");
        jMenuFile.add(jMenuItem2);
        jMenuFile.add(jSeparator6);

        jMenuItemConf.setText("Configuration ...");
        jMenuItemConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConfActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemConf);
        jMenuFile.add(jSeparator7);

        jMenuItemQuit.setText("Close");
        jMenuItemQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemQuitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemQuit);

        jMenuBar.add(jMenuFile);

        jMenuEdit.setText("Edit");

        jMenuItemSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemSelectAll.setText("Select all");
        jMenuItemSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSelectAllActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemSelectAll);

        jMenuItemInvertSelection.setText("Invert selection");
        jMenuItemInvertSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInvertSelectionActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemInvertSelection);
        jMenuEdit.add(jSeparator16);

        jMenuItemUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemUndo.setText("Undo");
        jMenuItemUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUndoActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemUndo);

        jMenuItemRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemRedo.setText("Redo");
        jMenuItemRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRedoActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemRedo);
        jMenuEdit.add(jSeparator27);

        jMenuItemCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemCut.setText("Cut");
        jMenuItemCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCutActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCut);

        jMenuItemCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemCopy.setText("Copy");
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCopy);

        jMenuItemPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemPaste.setText("Paste");
        jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPasteActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemPaste);
        jMenuEdit.add(jSeparator4);

        jMenuItemExtract.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
        jMenuItemExtract.setText("Extract");
        jMenuItemExtract.setToolTipText("Split selection");
        jMenuItemExtract.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExtractActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemExtract);

        jMenuItemRemoveSelection.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jMenuItemRemoveSelection.setText("Remove");
        jMenuEdit.add(jMenuItemRemoveSelection);
        jMenuEdit.add(jSeparator12);

        jMenuItemSetCursor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0));
        jMenuItemSetCursor.setText("Set cursor with mouse");
        jMenuItemSetCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetCursorActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemSetCursor);

        jMenuItemSetCursorTo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemSetCursorTo.setText("Set cursot to ...");
        jMenuItemSetCursorTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetCursorToActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemSetCursorTo);

        jMenuItemCursorAtCenter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItemCursorAtCenter.setText("Cursor to center");
        jMenuItemCursorAtCenter.setToolTipText("Set 2D cursor at center of selection or 2 or 3 points or any mouse segment.");
        jMenuItemCursorAtCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCursorAtCenterActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCursorAtCenter);

        jMenuItemCursorAtHead.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.ALT_DOWN_MASK));
        jMenuItemCursorAtHead.setText("Cursor to machine head");
        jMenuItemCursorAtHead.setToolTipText("Set 2D Cursor to GRBL Head Position");
        jMenuItemCursorAtHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCursorAtHeadActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemCursorAtHead);
        jMenuEdit.add(jSeparator5);

        jCheckBoxMenuItemSnapGrid.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, 0));
        jCheckBoxMenuItemSnapGrid.setText("Snap to grid");
        jCheckBoxMenuItemSnapGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemSnapGridActionPerformed(evt);
            }
        });
        jMenuEdit.add(jCheckBoxMenuItemSnapGrid);

        jCheckBoxMenuItemSnapPoints.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, 0));
        jCheckBoxMenuItemSnapPoints.setSelected(true);
        jCheckBoxMenuItemSnapPoints.setText("Snap to point(s)");
        jCheckBoxMenuItemSnapPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemSnapPointsActionPerformed(evt);
            }
        });
        jMenuEdit.add(jCheckBoxMenuItemSnapPoints);
        jMenuEdit.add(jSeparator11);

        jMenuItemGroup.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemGroup.setText("Group");
        jMenuItemGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGroupActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemGroup);

        jMenuItemUngroup.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemUngroup.setText("Ungroup");
        jMenuItemUngroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUngroupActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemUngroup);
        jMenuEdit.add(jSeparator25);

        jMenuItemMoveUp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, 0));
        jMenuItemMoveUp.setText("Move up");
        jMenuItemMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMoveUpActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemMoveUp);

        jMenuItemMoveDown.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PLUS, 0));
        jMenuItemMoveDown.setText("Move down");
        jMenuItemMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMoveDownActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemMoveDown);

        jMenuBar.add(jMenuEdit);

        jMenuView.setText("Show");

        jCheckBoxMenuItenItemShowPicture.setText("Background picture");
        jCheckBoxMenuItenItemShowPicture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItenItemShowPictureActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItenItemShowPicture);

        jMenuItemDistance.setText("Distances");
        jMenuItemDistance.setToolTipText("Show the distance between two points");
        jMenuItemDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDistanceActionPerformed(evt);
            }
        });
        jMenuView.add(jMenuItemDistance);

        jCheckBoxMenuItemShowEditor.setSelected(true);
        jCheckBoxMenuItemShowEditor.setText("Editor");
        jCheckBoxMenuItemShowEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowEditorActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowEditor);

        jCheckBoxMenuItemShowHeadPosition.setText("GRBL Head position");
        jCheckBoxMenuItemShowHeadPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowHeadPositionActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowHeadPosition);

        jCheckBoxMenuItemShowGrid.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jCheckBoxMenuItemShowGrid.setSelected(true);
        jCheckBoxMenuItemShowGrid.setText("Grid");
        jCheckBoxMenuItemShowGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowGridActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowGrid);

        jCheckBoxMenuItemShowMoves.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jCheckBoxMenuItemShowMoves.setText("Moves");
        jCheckBoxMenuItemShowMoves.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowMovesActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowMoves);

        jCheckBoxMenuItemShowObjectSurface.setText("Rotating object surface (x4)");
        jCheckBoxMenuItemShowObjectSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowObjectSurfaceActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowObjectSurface);

        jCheckBoxMenuItemShowStart.setSelected(true);
        jCheckBoxMenuItemShowStart.setText("Start positions");
        jCheckBoxMenuItemShowStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowStartActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowStart);

        jCheckBoxMenuItemShowToolBar.setSelected(true);
        jCheckBoxMenuItemShowToolBar.setText("ToolBar");
        jCheckBoxMenuItemShowToolBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowToolBarActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowToolBar);

        jCheckBoxMenuItemShowWorkspace.setSelected(true);
        jCheckBoxMenuItemShowWorkspace.setText("Workspace surface");
        jCheckBoxMenuItemShowWorkspace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowWorkspaceActionPerformed(evt);
            }
        });
        jMenuView.add(jCheckBoxMenuItemShowWorkspace);
        jMenuView.add(jSeparator28);

        jMenuItemFocus.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
        jMenuItemFocus.setText("Focus");
        jMenuItemFocus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFocusActionPerformed(evt);
            }
        });
        jMenuView.add(jMenuItemFocus);

        jMenuBar.add(jMenuView);

        jMenuBlocks.setText("Element");

        jMenuAdds.setText("Add");

        jMenuItemAddMixedPath.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0));
        jMenuItemAddMixedPath.setText("Artistic path");
        jMenuItemAddMixedPath.setToolTipText("<html>Create a path composed of curves (G5) and lines (G1)<br>\nUse right mouse button to insert points.</html>");
        jMenuItemAddMixedPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddMixedPathActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddMixedPath);

        jMenuItemAddcurve.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, 0));
        jMenuItemAddcurve.setText("Besier curve");
        jMenuItemAddcurve.setToolTipText("Create a besier curve (G5)");
        jMenuItemAddcurve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddcurveActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddcurve);

        jMenuItemAddCustom.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0));
        jMenuItemAddCustom.setText("Lines");
        jMenuItemAddCustom.setToolTipText("<html>Create a custom G1 path<br>Use right mouse button to insert points.</html>");
        jMenuItemAddCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddCustomActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddCustom);
        jMenuAdds.add(jSeparator33);

        jMenu1.setText("Circle");

        jMenuItemAddArc.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, 0));
        jMenuItemAddArc.setText("G2|3 Arc");
        jMenuItemAddArc.setToolTipText("");
        jMenuItemAddArc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddArcActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemAddArc);

        jMenuItemAddCircle.setText("Circle ...");
        jMenuItemAddCircle.setToolTipText("Add a circle composed by G1 segments");
        jMenuItemAddCircle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddCircleActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemAddCircle);

        jMenuItemAddCurvesCircle.setText("Curves circle");
        jMenuItemAddCurvesCircle.setToolTipText("A circle composed by four curves");
        jMenuItemAddCurvesCircle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddCurvesCircleActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemAddCurvesCircle);

        jMenuItemAddOval.setText("With mouse 2");
        jMenuItemAddOval.setToolTipText("<html>Create a G1 circle with mouse and <i>Alt</i>, <i>Ctrl</i> and <i>Shift</i> combinaisons keys.</html>");
        jMenuItemAddOval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddOvalActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemAddOval);

        jMenuAdds.add(jMenu1);

        jMenuItemAddDrill.setText("Drill point");
        jMenuItemAddDrill.setToolTipText("Want to drill somewhere ?");
        jMenuItemAddDrill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddDrillActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddDrill);

        jMenu2.setText("Misc");

        jMenuItemAddCross.setText("Cross");
        jMenuItemAddCross.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddCrossActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddCross);

        jMenuItemAddGear.setText("Gear");
        jMenuItemAddGear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddGearActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddGear);

        jMenuItemAddRipple.setText("Ripple");
        jMenuItemAddRipple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddRippleActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddRipple);

        jMenuItemAddRoubndRect.setText("Rounded rectangle");
        jMenuItemAddRoubndRect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddRoubndRectActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddRoubndRect);

        jMenuItemAddSpiral.setText("Spiral");
        jMenuItemAddSpiral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddSpiralActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddSpiral);

        jMenuItemAddStar.setText("Star");
        jMenuItemAddStar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddStarActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItemAddStar);

        jMenuAdds.add(jMenu2);

        jMenuItemAddRectangle.setText("Rectangle");
        jMenuItemAddRectangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddRectangleActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddRectangle);

        jMenuItemAddPolygon.setText("Polygon");
        jMenuItemAddPolygon.setToolTipText("Add a polygon centered around 2DCursor");
        jMenuItemAddPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddPolygonActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddPolygon);

        jMenuItemAddText.setText("Text");
        jMenuItemAddText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddTextActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddText);

        jMenuItemAddTextOnPath.setText("Text on path");
        jMenuItemAddTextOnPath.setToolTipText("Map a text on the first selected path");
        jMenuItemAddTextOnPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddTextOnPathActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddTextOnPath);
        jMenuAdds.add(jSeparator8);

        jMenuItemAddBounds.setText("Bounds");
        jMenuItemAddBounds.setToolTipText("Add the bounding box of the selection");
        jMenuItemAddBounds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddBoundsActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddBounds);

        jMenuMakeCutPath.setText("Offset Cut path");
        jMenuMakeCutPath.setToolTipText("To cut around selection");

        jMenuItemMakeCutPathI.setText("Inner");
        jMenuItemMakeCutPathI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMakeCutPathIActionPerformed(evt);
            }
        });
        jMenuMakeCutPath.add(jMenuItemMakeCutPathI);

        jMenuItemMakeCutPathO.setText("Outer");
        jMenuItemMakeCutPathO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMakeCutPathOActionPerformed(evt);
            }
        });
        jMenuMakeCutPath.add(jMenuItemMakeCutPathO);

        jMenuAdds.add(jMenuMakeCutPath);

        jMenuItemMakeFlatten.setText("Flatten path");
        jMenuItemMakeFlatten.setToolTipText("<html>Create a flat copy of selection<br>(Composed only of G0/G1 moves).</html>");
        jMenuItemMakeFlatten.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMakeFlattenActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemMakeFlatten);

        jMenuItemAddHull.setText("Hull path");
        jMenuItemAddHull.setToolTipText("Add hull path around selection");
        jMenuItemAddHull.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddHullActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddHull);

        jMenuItemAddLinkedPath.setText("Linked paths");
        jMenuItemAddLinkedPath.setToolTipText("Create a unique new path corresponding to selection.");
        jMenuItemAddLinkedPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddLinkedPathActionPerformed(evt);
            }
        });
        jMenuAdds.add(jMenuItemAddLinkedPath);

        jMenu5.setText("Pocket");

        jMenuItemAddPocket.setText("Pocket path");
        jMenuItemAddPocket.setToolTipText("Make pocket paths into the first selected path");
        jMenuItemAddPocket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddPocketActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemAddPocket);
        jMenu5.add(jSeparator29);

        jMenuItemAddCylindricalPocket.setText("Cylindrical pocket");
        jMenuItemAddCylindricalPocket.setToolTipText("Create a cylinder-shaped pocket");
        jMenuItemAddCylindricalPocket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddCylindricalPocketActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemAddCylindricalPocket);

        jMenuItemAddSphericalPocket.setText("Spherical pocket");
        jMenuItemAddSphericalPocket.setToolTipText("Create a sphere-shaped pocket");
        jMenuItemAddSphericalPocket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddSphericalPocketActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemAddSphericalPocket);

        jMenuAdds.add(jMenu5);

        jMenuBlocks.add(jMenuAdds);

        jMenuItemImport.setText("Import...");
        jMenuItemImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImportActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemImport);
        jMenuBlocks.add(jSeparator32);

        jMenuAlign.setText("Align");

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignCenter24.gif"))); // NOI18N
        jMenuItem1.setText("Center");
        jMenuItem1.setToolTipText("Center selection arount first selected or 2D Cursor");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItem1);

        jMenuItemAlignHorizontaly.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignJustifyHorizontal24.gif"))); // NOI18N
        jMenuItemAlignHorizontaly.setText("Horizontaly");
        jMenuItemAlignHorizontaly.setToolTipText("Align selection with center of fisrt selected");
        jMenuItemAlignHorizontaly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignHorizontalyActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignHorizontaly);

        jMenuItemAlignVertical.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignJustifyVertical24.gif"))); // NOI18N
        jMenuItemAlignVertical.setText("Verticaly");
        jMenuItemAlignVertical.setToolTipText("Align selection with center of first selected");
        jMenuItemAlignVertical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignVerticalActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignVertical);
        jMenuAlign.add(jSeparator26);

        jMenuItemAlignBottom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignBottom24.gif"))); // NOI18N
        jMenuItemAlignBottom.setText("Bottom");
        jMenuItemAlignBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignBottomActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignBottom);

        jMenuItemAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignLeft24.gif"))); // NOI18N
        jMenuItemAlignLeft.setText("Left");
        jMenuItemAlignLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignLeftActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignLeft);

        jMenuItemAlignRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignRight24.gif"))); // NOI18N
        jMenuItemAlignRight.setText("Right");
        jMenuItemAlignRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignRightActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignRight);

        jMenuItemAlignTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/AlignTop24.gif"))); // NOI18N
        jMenuItemAlignTop.setText("Top");
        jMenuItemAlignTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAlignTopActionPerformed(evt);
            }
        });
        jMenuAlign.add(jMenuItemAlignTop);

        jMenuBlocks.add(jMenuAlign);

        jMenuItemDuplicate.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemDuplicate.setText("Duplicate ...");
        jMenuItemDuplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDuplicateActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemDuplicate);

        jMenu4.setText("Flip");

        jMenuItemFlipH.setText("Flip horizontally");
        jMenuItemFlipH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFlipHActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemFlipH);

        jMenuItemFlipV.setText("Flip vertically");
        jMenuItemFlipV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFlipVActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemFlipV);

        jMenuBlocks.add(jMenu4);

        jMenuItemReverse.setText("Inverse");
        jMenuItemReverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReverseActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemReverse);

        jMenuItemJoin.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, 0));
        jMenuItemJoin.setText("Join");
        jMenuItemJoin.setToolTipText("Try to join paths");
        jMenuItemJoin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemJoinActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemJoin);

        jMenuItemMoveWithMouse.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, 0));
        jMenuItemMoveWithMouse.setText("Move");
        jMenuItemMoveWithMouse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMoveWithMouseActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemMoveWithMouse);

        jMenuItemMove.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemMove.setText("Move ...");
        jMenuItemMove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMoveActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemMove);

        jMenuItemMoveCenter.setText("Move to 2D Cursor");
        jMenuItemMoveCenter.setToolTipText("Move the center of the selection on 2D cursor.");
        jMenuItemMoveCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMoveCenterActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemMoveCenter);

        jMenuItemRename.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        jMenuItemRename.setText("Rename");
        jMenuItemRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRenameActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemRename);
        jMenuBlocks.add(jSeparator2);

        jMenuItemRotate.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemRotate.setText("Rotate ...");
        jMenuItemRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRotateActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemRotate);

        jMenuItemRotateCenter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
        jMenuItemRotateCenter.setText("Rotate around center");
        jMenuItemRotateCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRotateCenterActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemRotateCenter);

        jMenuItemRotateP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItemRotateP.setText("Rotate around 2D cursor");
        jMenuItemRotateP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRotatePActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemRotateP);
        jMenuBlocks.add(jSeparator10);

        jMenuItemScale.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemScale.setText("Scale ...");
        jMenuItemScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScaleActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemScale);

        jMenuItemScaleCenter.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
        jMenuItemScaleCenter.setText("Scale from center");
        jMenuItemScaleCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScaleCenterActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemScaleCenter);

        jMenuItemScaleP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItemScaleP.setText("Scale from 2D cursor");
        jMenuItemScaleP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemScalePActionPerformed(evt);
            }
        });
        jMenuBlocks.add(jMenuItemScaleP);
        jMenuBlocks.add(jSeparator24);

        jMenuGCODE.setText("GCODE");

        jMenuItemAddHeaderFooter.setText("Add Header/Footer");
        jMenuItemAddHeaderFooter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddHeaderFooterActionPerformed(evt);
            }
        });
        jMenuGCODE.add(jMenuItemAddHeaderFooter);

        jMenuItemSort.setText("Optimize moves");
        jMenuItemSort.setToolTipText("Reorder path to minimize machine  move, starting with first selected.");
        jMenuItemSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSortActionPerformed(evt);
            }
        });
        jMenuGCODE.add(jMenuItemSort);
        jMenuGCODE.add(jSeparator3);

        jMenuItemSetAsHeader.setText("Set as G-Code Header");
        jMenuItemSetAsHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetAsHeaderActionPerformed(evt);
            }
        });
        jMenuGCODE.add(jMenuItemSetAsHeader);

        jMenuItemSetAsFooter.setText("Set as G-Code Footer");
        jMenuItemSetAsFooter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetAsFooterActionPerformed(evt);
            }
        });
        jMenuGCODE.add(jMenuItemSetAsFooter);
        jMenuGCODE.add(jSeparator17);

        jMenuItemFilter.setText("Apply filter ...");
        jMenuItemFilter.setToolTipText("Clean selected GCode");
        jMenuItemFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFilterActionPerformed(evt);
            }
        });
        jMenuGCODE.add(jMenuItemFilter);

        jMenuBlocks.add(jMenuGCODE);

        jMenu3.setText("OpenScad");

        jMenuItemCopyOpenScadPolygon.setText("<html>Copy OpenScad polygon code to clipboard</html>");
        jMenuItemCopyOpenScadPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyOpenScadPolygonActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemCopyOpenScadPolygon);

        jMenuBlocks.add(jMenu3);

        jMenuBar.add(jMenuBlocks);

        jMenuPoints.setText("Point");

        jMenuItemAddPoints.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, 0));
        jMenuItemAddPoints.setText("Add with mouse");
        jMenuItemAddPoints.setToolTipText("Append new points with mouse");
        jMenuItemAddPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddPointsActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemAddPoints);

        jMenuItemAddFromHeadPos.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        jMenuItemAddFromHeadPos.setText("Add from current head position");
        jMenuItemAddFromHeadPos.setToolTipText("");
        jMenuItemAddFromHeadPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddFromHeadPosActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemAddFromHeadPos);

        jMenuItemAddIntersectionPoints.setText("Add from intersection");
        jMenuItemAddIntersectionPoints.setToolTipText("<html>Insert <b>in the first selected path</b> all the intersection points with all others paths.<br> The first path selected must be a flat path.(G1)");
        jMenuItemAddIntersectionPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddIntersectionPointsActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemAddIntersectionPoints);
        jMenuPoints.add(jSeparator13);

        jMenuItemChStartPos.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
        jMenuItemChStartPos.setText("Set as first point");
        jMenuItemChStartPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemChStartPosActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemChStartPos);
        jMenuPoints.add(jSeparator20);

        jMenuItemSimplifyP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, 0));
        jMenuItemSimplifyP.setText("Keep to simplify");
        jMenuItemSimplifyP.setToolTipText("<html>Unselect key points based on angles<br>Then adjust with mouse and delete</html>");
        jMenuItemSimplifyP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSimplifyPActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemSimplifyP);

        jMenuItemSimplifyByDistance.setText("Simplify by distance ...");
        jMenuItemSimplifyByDistance.setToolTipText("Remove closed points");
        jMenuItemSimplifyByDistance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSimplifyByDistanceActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemSimplifyByDistance);

        jMenuItemSimplify.setText("Simplify by divergence ...");
        jMenuItemSimplify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSimplifyActionPerformed(evt);
            }
        });
        jMenuPoints.add(jMenuItemSimplify);

        jMenuBar.add(jMenuPoints);

        jMenuGRBL.setText("GRBL");

        jMenuItemGRBLConnect.setText("Connect");
        jMenuItemGRBLConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLConnectActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLConnect);

        jMenuItemGRBLDisconnect.setText("Disconnect");
        jMenuItemGRBLDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLDisconnectActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLDisconnect);

        jMenuItemGRBLSettings.setText("Settings");
        jMenuItemGRBLSettings.setToolTipText("Edit GRBL settings");
        jMenuItemGRBLSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLSettingsActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLSettings);

        jMenuItemGRBLKillAlarm.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemGRBLKillAlarm.setText("Kill Alarm");
        jMenuItemGRBLKillAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLKillAlarmActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLKillAlarm);

        jMenuItemGRBLSoftReset.setText("Soft Reset");
        jMenuItemGRBLSoftReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLSoftResetActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLSoftReset);
        jMenuGRBL.add(jSeparator30);

        jMenuItemGRBLHome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        jMenuItemGRBLHome.setText("Home");
        jMenuItemGRBLHome.setToolTipText("Execute an homming cycle");
        jMenuItemGRBLHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLHomeActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLHome);

        jMenuItemGRBLSetMPos.setText("Set MPos");
        jMenuItemGRBLSetMPos.setToolTipText("Virtualy move machine position on the screen");
        jMenuItemGRBLSetMPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLSetMPosActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLSetMPos);

        jMenuItemGRBLWPosAsMPos.setText("Set MPos to origine");
        jMenuItemGRBLWPosAsMPos.setToolTipText("Assume machine position is at origine.");
        jMenuItemGRBLWPosAsMPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLWPosAsMPosActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLWPosAsMPos);
        jMenuGRBL.add(jSeparator19);

        jMenuItemGRBLJogWindow.setText("JOG Window");
        jMenuItemGRBLJogWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLJogWindowActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLJogWindow);

        jMenuItemGRBLMoveHead.setText("Move Gantry");
        jMenuItemGRBLMoveHead.setToolTipText("Move machine gantry with the mouse on the screen");
        jMenuItemGRBLMoveHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLMoveHeadActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLMoveHead);

        jMenuItemGRBLCmd.setText("Send command");
        jMenuItemGRBLCmd.setToolTipText("Send a comand to GRBL");
        jMenuItemGRBLCmd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLCmdActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLCmd);

        jCheckBoxMenuItemGRBLShowLaserPosition.setText("Show Laser position");
        jCheckBoxMenuItemGRBLShowLaserPosition.setToolTipText("<html>Put GRBL to G1 mode with spindle set to 1<br>(warning if tool if not a laser)</html>");
        jCheckBoxMenuItemGRBLShowLaserPosition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemGRBLShowLaserPositionActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jCheckBoxMenuItemGRBLShowLaserPosition);

        jMenuItemGRBLShowLogWindow.setText("LOG Window");
        jMenuItemGRBLShowLogWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLShowLogWindowActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLShowLogWindow);
        jMenuGRBL.add(jSeparator18);

        jMenuItemGRBLShowBoundaries.setText("Show boundaries");
        jMenuItemGRBLShowBoundaries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGRBLShowBoundariesActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemGRBLShowBoundaries);

        jMenuItemExecuteAll.setText("Execute all ...");
        jMenuItemExecuteAll.setToolTipText("Execute the document");
        jMenuItemExecuteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExecuteAllActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemExecuteAll);

        jMenuItemExecuteSelected.setText("Excute selected ...");
        jMenuItemExecuteSelected.setToolTipText("Execute only the selected blocks");
        jMenuItemExecuteSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExecuteSelectedActionPerformed(evt);
            }
        });
        jMenuGRBL.add(jMenuItemExecuteSelected);

        jMenuBar.add(jMenuGRBL);

        jMenuHelp.setText("Help");

        jMenuItemAbout.setText("About...");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    void showGRBLError(int errorno, String line) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Error "+errorno+" :\n"+GRBLControler.getErrorMsg(errorno)+ "\n\nLine: " + line, "GRBL Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    void showGRBLAlarm(int alarmno) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Alarm "+alarmno+" :\n"+GRBLControler.getAlarmMsg(alarmno), "GRBL Alarm", JOptionPane.ERROR_MESSAGE);
        });
    }
    void showGRBLMessage(String grblMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Message :\n"+grblMessage, "GRBL Message", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    private void jMenuItemImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImportActionPerformed
        JFileChooser fc = new JFileChooser(".");
        if ( lastImportDir != null) fc.setCurrentDirectory(lastImportDir);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Drawing Exchange File (*.dxf)", "dxf"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("G-Code (*.ngc,*.nc,*.tap,*.gcode)", "ngc", "nc", "tap","gcode"));
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Scalable Vector Graphics (*.svg)", "svg"));        
        if ( curDir != null) fc.setCurrentDirectory(curDir);

        if(fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            curDir = fc.getCurrentDirectory();
     
            try {
                if ( lastImportDir == null) 
                    lastImportDir = fc.getSelectedFile().getParentFile();
                
                if ( fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".svg")) 
                        addGElement(blocksviewer.importSVG(fc.getSelectedFile().getAbsolutePath()));
                else 
                    if ( fc.getSelectedFile().getAbsolutePath().toLowerCase().endsWith(".dxf"))
                        blocksviewer.importDXF(fc.getSelectedFile().getAbsolutePath());
                    else 
                        addGElement(JBlocksViewer.importGCODE(fc.getSelectedFile().getAbsolutePath(), null));
                
            } catch ( ParserConfigurationException | SAXException | IOException | ParseException ex) {
                JOptionPane.showMessageDialog(this, "Error reading file : \n\n" + ex.toString(), "Import error", JOptionPane.ERROR_MESSAGE);
               // Logger.getLogger(JEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
               ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_jMenuItemImportActionPerformed

    private void jMenuItemNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewActionPerformed
        java.awt.EventQueue.invokeLater(() -> {
            new JEditorFrame(true, true).setVisible(true);
        });
    }//GEN-LAST:event_jMenuItemNewActionPerformed

    private void jMenuItemQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemQuitActionPerformed
        closeWindows();
    }//GEN-LAST:event_jMenuItemQuitActionPerformed

    private void jMenuItemJoinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemJoinActionPerformed
        try {
            double tolerance = 0;
            if (! blocksviewer.isInEditMode()) // Not in Edit mode
                tolerance = Double.valueOf( JOptionPane.showInputDialog(this, "Maximal distance", "0.0002"));
            blocksviewer.doAction(JBlocksViewer.ACTION_JOIN, tolerance, null);
        } catch (NumberFormatException e) { 
            updateGUIAndStatus("wrong distance");
        }
    }//GEN-LAST:event_jMenuItemJoinActionPerformed

    private void jMenuItemUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUndoActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_UNDO, 0, null);
    }//GEN-LAST:event_jMenuItemUndoActionPerformed

    private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPasteActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_PASTE, 0, null);
    }//GEN-LAST:event_jMenuItemPasteActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_COPY, 0, null);
    }//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jMenuItemCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCutActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_CUT, 0, null);
    }//GEN-LAST:event_jMenuItemCutActionPerformed

    private void jMenuItemSimplifyPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSimplifyPActionPerformed
        try {
            double d = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter the tolerance (0=no change)", "1.0"));
            blocksviewer.doAction(JBlocksViewer.ACTION_SIMPLIFY, d, null);
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_jMenuItemSimplifyPActionPerformed

    private void jMenuItemRotatePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRotatePActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ROTATE_POINT, 0, null);
    }//GEN-LAST:event_jMenuItemRotatePActionPerformed

    private void jMenuItemRotateCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRotateCenterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ROTATE, 0, null);
    }//GEN-LAST:event_jMenuItemRotateCenterActionPerformed

    private void jMenuItemScaleCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScaleCenterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SCALE, 0, null);
    }//GEN-LAST:event_jMenuItemScaleCenterActionPerformed

    private void jMenuItemFlipHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFlipHActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_FLIP_H, 0, null);
    }//GEN-LAST:event_jMenuItemFlipHActionPerformed

    private void jMenuItemFlipVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFlipVActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_FLIP_V, 0, null);
    }//GEN-LAST:event_jMenuItemFlipVActionPerformed

    private void jMenuItemChStartPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemChStartPosActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_CHANGE_START_POINT, 0, null);
    }//GEN-LAST:event_jMenuItemChStartPosActionPerformed

    private void jMenuItemExportSVGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportSVGActionPerformed
        int res = 1;
        
        if ( blocksviewer.getSelectionBoundary(false) != null)
            res = JOptionPane.showConfirmDialog(this, "Export ony selection ?", "Export to SVG...", 
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if ( res == 2) return;
        JFileChooser f = new JFileChooser();
        if ( lastImportDir != null) f.setCurrentDirectory(lastImportDir);
        f.setFileFilter(new FileNameExtensionFilter("Scalable Vector Graphics (.svg)", "svg"));
        int rVal = f.showSaveDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) {
            if ( lastImportDir == null) 
                    lastImportDir = f.getSelectedFile().getParentFile();
            String fname = f.getSelectedFile().getAbsolutePath();
            if ( fname.indexOf('.') == -1) fname = fname.concat(".svg");
            if (new File( fname).exists())
                if ( JOptionPane.showConfirmDialog(this, fname+"\nFile exits, overwrite it ?", "File Exist", JOptionPane.WARNING_MESSAGE)== JOptionPane.CANCEL_OPTION)
                        return;    
            try {
                blocksviewer.exportToSVG( fname, res == 0);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error at export:\n"+ex.getLocalizedMessage(), "SVG Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemExportSVGActionPerformed

    private void jMenuItemExtractActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExtractActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_EXTRACT, 0, null);
    }//GEN-LAST:event_jMenuItemExtractActionPerformed

    private void jMenuItemConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfActionPerformed
        if ( confFrame == null) confFrame = new JConfigurationFrame(blocksviewer.getConfiguration(), this);
        confFrame.setLocationRelativeTo(this);
        confFrame.setVisible(true);
    }//GEN-LAST:event_jMenuItemConfActionPerformed

    private void jCheckBoxMenuItemShowGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowGridActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_GRID, jCheckBoxMenuItemShowGrid.isSelected()?1:0, null);
    }//GEN-LAST:event_jCheckBoxMenuItemShowGridActionPerformed

    private void jCheckBoxMenuItemSnapGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemSnapGridActionPerformed
        blocksviewer.setSnapToGrid( jCheckBoxMenuItemSnapGrid.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemSnapGridActionPerformed

    private void jCheckBoxMenuItemSnapPointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemSnapPointsActionPerformed
        blocksviewer.setSnapToPoints( jCheckBoxMenuItemSnapPoints.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemSnapPointsActionPerformed

    private void jMenuItemSimplifyByDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSimplifyByDistanceActionPerformed
        String r = JOptionPane.showInputDialog("Enter the minimal distance between 2 adjacent points.", "2.0");
        if ( r == null) return;
        double d;
        try { d = Double.parseDouble(r); } catch ( NumberFormatException e) { 
            JOptionPane.showMessageDialog(this, "Distance invalid.");
            return; 
        }
        blocksviewer.doRemoveByDistance(d);
    }//GEN-LAST:event_jMenuItemSimplifyByDistanceActionPerformed

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        JOptionPane.showMessageDialog(this, "A Simple 2D G-Code Visual Editor\n\n"+
                "For laser engraving and simple milling projects\nInclude a realtime GRBL 1.1 controler\n\nVersion: "+
                JBlocksViewer.SGVE_RELEASE+" - 2023\nAuthor: Clément Gérardin @ Marseille.fr", "About this software"+
                        "\n\nWithout any guarantee !!!", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemSaveGCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveGCodeActionPerformed
        if ( savedFileName == null) jMenuItemSaveAsActionPerformed(null);
        else try {
            blocksviewer.saveDocument(savedFileName);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error:\n"+ex.getLocalizedMessage(), "Saving...", JOptionPane.ERROR_MESSAGE);
            }
    }//GEN-LAST:event_jMenuItemSaveGCodeActionPerformed

    private void jMenuItemOpenGCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenGCodeActionPerformed
        openGCODE(null);
    }//GEN-LAST:event_jMenuItemOpenGCodeActionPerformed

    private void jMenuItemAddPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddPolygonActionPerformed
      
        JPolygonPanel polyPanel = (JPolygonPanel)dialogManager.showDialogFor( JPolygonPanel.class, null);                 
        if ( polyPanel != null) {
            
            G1Path p;
            if ( polyPanel.radius > 0)
                p = G1Path.makeCircle(blocksviewer.get2DCursor(),
                         (polyPanel.nbEdge > 0) ? polyPanel.nbEdge : (int)(2 * Math.PI* polyPanel.radius), polyPanel.radius, polyPanel.clockwise, false);
            else {              
                p = G1Path.makeCircle(blocksviewer.get2DCursor(),
                         polyPanel.nbEdge, Math.abs((double)polyPanel.edgeLen / (2. * Math.sin(180/polyPanel.nbEdge))), polyPanel.clockwise, false);
                double d = 100. / p.getFirstPoint().distance( p.getPoint(1));
                p.scale( blocksviewer.get2DCursor(), d, d);
            }
             
            p.name = "poly"+polyPanel.nbEdge+"-"+GElement.getUniqID();
            if ( polyPanel.rotate && (polyPanel.nbEdge > 0)) {
                double da = 360. / polyPanel.nbEdge;
                double maxY = Double.NEGATIVE_INFINITY;
                int point = 0;
                
                // TODO: find the right way for any points
                double angle = 90;
                double a1 = da * (polyPanel.nbEdge/2);
                double a2 = a1 + da;
                double r = (a1+a2)/2;
                if ( Math.abs(r - 180.) < 10e-8) {
                    // equals, just rotate 90°
                    angle = 90;
                }
                else
                    angle = 180 - r;

                p.rotate( blocksviewer.get2DCursor(), Math.toRadians(angle));
            }           
            addGElement( p);
        }
            
       /* String res = JOptionPane.showInputDialog(this, "Enter length", "50");
        double len;
        try {
            len = Double.parseDouble(res);
            G1Path.makePolygon( );
            G1Path s = new G1Path("square"+GElement.getUniqID());
            s.add( new Point(0, 0, 0));
            s.add( new Point(len, 0, 0));
            s.add( new Point(len, len, 0));
            s.add( new Point(0, len, 0));
            s.add( new Point(0, 0, 0));
            s.translate(blocksviewer.get2DCursor());
            addGElement( s);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }*/
    }//GEN-LAST:event_jMenuItemAddPolygonActionPerformed

    private void jMenuItemAddCircleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddCircleActionPerformed
        try {    
            double diameter = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter diameter", "10.0"));
            int np = Integer.valueOf(JOptionPane.showInputDialog(this, "<html>Number of point in the circle ?<br><i>(Choose a multiple of 2,3,4,5,6,8,9 for best accuray)</i></html>", 12*(int)(2*Math.PI*diameter/6)));         
            addGElement(G1Path.makeCircle(blocksviewer.get2DCursor(), np, diameter/2, true, false));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemAddCircleActionPerformed
    
    private void jMenuItemAddRippleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddRippleActionPerformed
        try {
            int p = Integer.valueOf(JOptionPane.showInputDialog(this, "Number of points by periode", "48"));
            int nbR = Integer.valueOf(JOptionPane.showInputDialog(this, "Enter number of ripple", "10"));
            double scaleFactor = Double.valueOf(JOptionPane.showInputDialog(this, "Scale factor", "10"));
            double angle=0, a = Math.PI * 2 /p, stepX = (double)2/p, x= 0;
            G1Path s = new G1Path("ripple"+GElement.getUniqID());
            for ( int ripple = 0; ripple < nbR; ripple++)
                for(int n = 0; n < ((ripple==(nbR-1))?p+1:p); n++) {
                    double y = Math.sin(angle);
                    if ( Math.abs(y) < 0.000000001) y = 0;
                    s.add( new Point( scaleFactor*x, scaleFactor*y, 0));
                    angle +=a;
                    x += stepX;
                }
            for ( GCode pt : s) pt.translate(blocksviewer.get2DCursor());
            addGElement( s);
        } catch (NumberFormatException e) { }
    }//GEN-LAST:event_jMenuItemAddRippleActionPerformed

    private void jTextFieldEditedBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldEditedBlockActionPerformed
        jTextFieldEditedBlock.setEnabled(false);
        blocksviewer.setEditedBlockName(jTextFieldEditedBlock.getText());  
    }//GEN-LAST:event_jTextFieldEditedBlockActionPerformed

    private void jMenuItemSetAsHeaderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetAsHeaderActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SET_AS_HEADER, 0, null);
    }//GEN-LAST:event_jMenuItemSetAsHeaderActionPerformed

    private void jMenuItemMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMoveUpActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_UP, 0, null);
    }//GEN-LAST:event_jMenuItemMoveUpActionPerformed

    private void jMenuItemMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMoveDownActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_DOWN, 0, null);
    }//GEN-LAST:event_jMenuItemMoveDownActionPerformed

    private void jMenuItemSetAsFooterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetAsFooterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SET_AS_FOOTER, 0, null);
    }//GEN-LAST:event_jMenuItemSetAsFooterActionPerformed

    private void jMenuItemFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFilterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_FILTER, 0, null);
    }//GEN-LAST:event_jMenuItemFilterActionPerformed

    private void jCheckBoxMenuItemShowObjectSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowObjectSurfaceActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_OBJECT_SURFACE, jCheckBoxMenuItemShowObjectSurface.isSelected()?1:0, null);
    }//GEN-LAST:event_jCheckBoxMenuItemShowObjectSurfaceActionPerformed

    private void jCheckBoxMenuItemShowMovesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowMovesActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_MOVES, jCheckBoxMenuItemShowMoves.isSelected()?1:0, null);
    }//GEN-LAST:event_jCheckBoxMenuItemShowMovesActionPerformed

    private void jMenuItemAddStarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddStarActionPerformed
        try {
            int b = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of branches ", "5"));
            double br = Double.parseDouble(JOptionPane.showInputDialog(this, "Branches radius", "20"));
            double r = Double.parseDouble(JOptionPane.showInputDialog(this, "Center radius", "6.66666666"));
            double angle=Math.PI/2, a = 2* Math.PI/b, x, y;

            G1Path s = new G1Path("star"+GElement.getUniqID());
            int brn;
            for ( brn = 0; brn < b; brn++) {
                    x = Math.cos(angle);
                    if ( Math.abs(x) < 0.000000001) x = 0;
                    y = Math.sin(angle);
                    if ( Math.abs(y) < 0.000000001) y = 0;
                    s.add( new Point( br * x, br * y, 0));
                    angle +=a/2;
                    x = Math.cos(angle);
                    if ( Math.abs(x) < 0.000000001) x = 0;
                    y = Math.sin(angle);
                    if ( Math.abs(y) < 0.000000001) y = 0;
                    s.add( new Point( r * x, r * y, 0));
                    angle +=a/2;
            }
            x = Math.cos(angle);
            if ( Math.abs(x) < 0.000000001) x = 0;
            y = Math.sin(angle);
            if ( Math.abs(y) < 0.000000001) y = 0;
            s.add( new Point( br * x, br * y, 0));
            angle +=a/2;
            for ( GCode pt : s) pt.translate(blocksviewer.get2DCursor());
            addGElement( s);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemAddStarActionPerformed

    private void jMenuItemScalePActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScalePActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SCALE_POINT, 1, null);
    }//GEN-LAST:event_jMenuItemScalePActionPerformed

    private void jMenuItemScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemScaleActionPerformed
        JScalePanel scalePanel = (JScalePanel)dialogManager.showDialogFor(
                JScalePanel.class, blocksviewer.getSelectionBoundary(false));
                     
        if (scalePanel != null) {
                blocksviewer.scaleSelection( scalePanel.xScale, scalePanel.yScale, scalePanel.copies,
                                             scalePanel.fromCenter, scalePanel.keepOriginal);
        }           
        
    }//GEN-LAST:event_jMenuItemScaleActionPerformed

    private void jMenuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAsActionPerformed
        JFileChooser f = new JFileChooser();
        f.setFileFilter(new FileNameExtensionFilter("G-Code (*.ngc,*.nc,*.tap,*.gcode)", "ngc", "nc", "tap","gcode"));
        if ( lastImportDir == null) {
            if ( blocksviewer.getName() != null) {
                lastImportDir = new File(blocksviewer.getName()).getParentFile();
            } else {
                lastImportDir = new File(".");
            }
        }
        f.setCurrentDirectory(lastImportDir);
        
        int rVal = f.showSaveDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) {
            lastImportDir = f.getSelectedFile().getParentFile();
            String fname = f.getSelectedFile().getAbsolutePath();
            if ( fname.indexOf('.') == -1) 
                fname = fname.concat(".gcode");
            if (new File( fname).exists())
                if ( JOptionPane.showConfirmDialog(this, fname + "\nFile exits, overwrite it ?", "File Exist", JOptionPane.WARNING_MESSAGE)== JOptionPane.CANCEL_OPTION)
                        return;
            
            try {
                blocksviewer.saveDocument(fname);
                savedFileName = fname;
                addToRecentFile(savedFileName);
                updateTitle();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error:\n"+ex.getLocalizedMessage(), "Saving...", JOptionPane.ERROR_MESSAGE);
            }
                
        }
    }//GEN-LAST:event_jMenuItemSaveAsActionPerformed

    private void jMenuItemAddPointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddPointsActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_LINES, 0, null);
    }//GEN-LAST:event_jMenuItemAddPointsActionPerformed

    private void jMenuItemRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRotateActionPerformed
        JRotationPanel rotatePanel = (JRotationPanel)dialogManager.showDialogFor(JRotationPanel.class, null);
     
        if ( rotatePanel != null) {
            blocksviewer.rotateSelection( Math.toRadians(rotatePanel.angle), 
                    rotatePanel.copies, rotatePanel.fromCenter, rotatePanel.keepOriginal, rotatePanel.keepOrientation);
        }
        
    }//GEN-LAST:event_jMenuItemRotateActionPerformed

    private void jMenuItemSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectAllActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SELECT_ALL, 0, null);
    }//GEN-LAST:event_jMenuItemSelectAllActionPerformed

    private void jMenuItemSetCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetCursorActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SET_2D_CURSOR, 0, null);
    }//GEN-LAST:event_jMenuItemSetCursorActionPerformed

    private void jCheckBoxMenuItemShowWorkspaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowWorkspaceActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_WORKSPACE, jCheckBoxMenuItemShowWorkspace.isSelected()?1:0, null);
    }//GEN-LAST:event_jCheckBoxMenuItemShowWorkspaceActionPerformed

    private void jMenuItemCursorAtCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCursorAtCenterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_CURSOR_AT_CENTER, 0, null);
    }//GEN-LAST:event_jMenuItemCursorAtCenterActionPerformed

    private void jMenuItemAddSpiralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddSpiralActionPerformed
        try {          
            double radius = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter start radius", "5.0"));
            int np = Integer.valueOf(JOptionPane.showInputDialog(this, "Number of points per turn", 24));
            int nt = Integer.valueOf(JOptionPane.showInputDialog(this, "Number of turns", 10));
            double gd = Double.parseDouble(JOptionPane.showInputDialog(this, "Distance between each turns", "5"));
            gd /= np;
            
            double angle=0, a = 2* Math.PI/np;            
            G1Path s = new G1Path("spiral");
            for(int n = 0; n < (np*nt); n++) {
                double x = Math.cos(angle);
                if ( Math.abs(x) < 0.000000001) x = 0;
                double y = Math.sin(angle);
                if ( Math.abs(y) < 0.000000001) y = 0;
                s.add( new Point(radius * x, radius * y, 0));
                
                radius += gd;
                System.out.println(radius);
                
                angle +=a;
            }
            s.add( new Point(radius * Math.cos(0), radius * Math.sin(0),0));
            for ( GCode pt : s) pt.translate(blocksviewer.get2DCursor());
            addGElement( s);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemAddSpiralActionPerformed

    private void jMenuItemRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRenameActionPerformed
        blocksviewer.renameSelection( JOptionPane.showInputDialog(this, "New name ?", blocksviewer.getSelectedBlockName()));
    }//GEN-LAST:event_jMenuItemRenameActionPerformed

    private void jMenuItemAddCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddCustomActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_LINES, 0, null);
    }//GEN-LAST:event_jMenuItemAddCustomActionPerformed

    private void jMenuItemAddRectangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddRectangleActionPerformed
        try {          
            double w = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter width", "50"));
            double h = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter height", "50"));
             
            G1Path s = new G1Path("rectangle"+GElement.getUniqID());
            s.add( new Point(0, 0, 0));
            s.add( new Point(w, 0, 0));
            s.add( new Point(w, h, 0));
            s.add( new Point(0, h, 0));
            s.add( new Point(0, 0, 0));
            for ( GCode pt : s) pt.translate(blocksviewer.get2DCursor());
            addGElement( s);
            blocksviewer.setEditedElement(s);
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemAddRectangleActionPerformed

    private void jMenuItemDuplicateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDuplicateActionPerformed
        JDuplicatePanel moveDialog = (JDuplicatePanel)dialogManager.showDialogFor( JDuplicatePanel.class, null);
        if ( moveDialog != null) {
            blocksviewer.moveCopySelection(moveDialog.deltaX == Double.POSITIVE_INFINITY ? 0 : moveDialog.deltaX,
                                           moveDialog.deltaY == Double.POSITIVE_INFINITY ? 0 : moveDialog.deltaY,
                                           moveDialog.nbCopies, moveDialog.packed, moveDialog.grouped, true);
        }        
    }//GEN-LAST:event_jMenuItemDuplicateActionPerformed

    private void jCheckBoxMenuItemShowHeadPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowHeadPositionActionPerformed
        blocksviewer.setShowGRBLHead( jCheckBoxMenuItemShowHeadPosition.isSelected());       
    }//GEN-LAST:event_jCheckBoxMenuItemShowHeadPositionActionPerformed

    private void jMenuItemGRBLConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLConnectActionPerformed
        String port = null;
        
        if ( ! grbl.isConnected()) try {
            port = (String) JOptionPane.showInputDialog(this, "GRBL Serial port ?", "Connection", JOptionPane.PLAIN_MESSAGE, null, grbl.getSerialPorts(), null);
            if ( port != null) {
                grbl.connect(port, 115200);
                jMenuItemGRBLConnect.setEnabled(false);
                jMenuItemGRBLDisconnect.setEnabled(true);
                jCheckBoxMenuItemShowHeadPosition.setSelected(true);
                jCheckBoxMenuItemShowHeadPositionActionPerformed(null);      
            }
        } catch (IOException | NoSuchPortException | PortInUseException | TooManyListenersException | UnsupportedCommOperationException e) {
            JOptionPane.showMessageDialog(this, port + "\n" + e.toString(), "Can't connect to GRBL", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemGRBLConnectActionPerformed

    private void jMenuItemGRBLDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLDisconnectActionPerformed
        if ( grbl.isConnected()) {
            grbl.disconnect(true);
        }
        //jMenuItemGRBLConnect.setEnabled(true);
        //jMenuItemGRBLDisconnect.setEnabled(false);
    }//GEN-LAST:event_jMenuItemGRBLDisconnectActionPerformed

    private void jMenuItemGRBLCmdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLCmdActionPerformed
        grbl.pushCmd(JOptionPane.showInputDialog(this, "GRBL Command ?", ""));
    }//GEN-LAST:event_jMenuItemGRBLCmdActionPerformed

    private void jMenuItemGRBLSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLSettingsActionPerformed
        TreeMap<Integer, Double> settings = grbl.getGRBLSettings();
        if ( ! grbl.isSettingsReady()) {
            if ( grbl.isIdle()) grbl.pushCmd("$$");
            JOptionPane.showMessageDialog(this, "GRBL Settings not ready.\nIs GRBL in IDLE state ?");
            return;
        }
        JDialog d = new JDialog(this, "GRBL Settings", true);
        TableModel tm;
        JTable t = new JTable( tm = new TableModel() {
            @Override
            public int getRowCount() { return settings.size(); }

            @Override
            public int getColumnCount() { return 3; }
            @Override
            public String getColumnName(int columnIndex) {
                switch( columnIndex) {
                    case 0: return "ID";
                    case 1: return "Value";
                    case 2: return "Description";
                }
                return null;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return JTextField.class;
            }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                switch( columnIndex) {
                    case 0: return("$" + GRBLControler.GRBL_SETTING_STR[rowIndex*2]);
                    case 1: return (settings.get(Integer.valueOf(GRBLControler.GRBL_SETTING_STR[rowIndex*2])));
                    case 2: return (GRBLControler.GRBL_SETTING_STR[rowIndex*2+1]);
                    default: return ("??unknow??");
                }
            }
            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                try {
                    double val = ( aValue instanceof JTextField) ? Double.valueOf(((JTextField) aValue).getText()) : (Double)aValue;
                    settings.put(Integer.valueOf(GRBLControler.GRBL_SETTING_STR[rowIndex*2]), val);
                    if (aValue instanceof JTextField) 
                        ((JTextField)aValue).setBackground(Color.white);
                } catch ( NumberFormatException e) {
                    if (aValue instanceof JTextField) 
                        ((JTextField)aValue).setBackground(Color.red);
                }
            }
            @Override
            public void addTableModelListener(TableModelListener l) { }
            @Override
            public void removeTableModelListener(TableModelListener l) { }
        });
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        t.getColumnModel().getColumn(0).setMaxWidth(50);
        t.getColumnModel().getColumn(1).setMaxWidth(200);
        d.setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
        d.getContentPane().add(t);
        d.getContentPane().add(new JLabel(" "));
        JPanel p = new JPanel();
        JButton b;
        p.add( b = new JButton("Reset"));
        b.setToolTipText("Restore to factory default.");
        b.addActionListener((ActionEvent e) -> {
            grbl.resetSettings();
            d.dispose();
        });
        p.add( new JLabel("   "));
        p.add( b= new JButton("Load"));
        b.addActionListener((ActionEvent e) -> {
            String file = askForFile(false, null, new FileNameExtensionFilter("Text file (*.txt)", "txt"));
            if (file != null) {
                Pattern pat = Pattern.compile("^\\$(\\d+)=([\\-0-9\\.]+)");
                try {
                    BufferedReader in = new BufferedReader( new FileReader(new  File(file)));
                    boolean error = false;
                    t.clearSelection();
                    while( in.ready()) {
                        String line = in.readLine(); 
                        if ( line.startsWith(";")) continue;
                        Matcher m = pat.matcher(line);
                        if( m.matches()) {
                            try {
                                int i = GRBLControler.getIndexOfSetting(Integer.parseInt(m.group(1)));
                                if ( i >= 0) {
                                    if ( ! settings.get(Integer.parseInt(m.group(1))).equals(Double.valueOf(m.group(2)))) {
                                        t.setValueAt(Double.valueOf(m.group(2)), i, 1);
                                        t.addRowSelectionInterval(i, i);
                                    }
                                } else
                                    error = true;
                            } catch ( NumberFormatException nfe) {
                                error = true;
                            }
                        } else error = true;
                    }
                    in.close();
                    if ( error)
                        JOptionPane.showMessageDialog(this, "Wrong setting or numeric format detected", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Reading error :\n" + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
                }
            }
        });
        p.add( b= new JButton("Save"));
        b.addActionListener((ActionEvent e) -> {
            String file = askForFile(true, null, new FileNameExtensionFilter("Text file (*.txt)", "txt"));
            if (file != null) {
                try {
                    FileWriter fw = new FileWriter(new  File(file));
                    fw.append("; GRBL "+ grbl.getMachineName() + " settings values files\n");
                    for( Integer s : settings.keySet())
                        fw.append("$"+s+"="+settings.get(s)+"\n");
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(JEditorFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        p.add( new JLabel("   "));
        p.add(b = new JButton("Cancel"));
        b.addActionListener((ActionEvent e) -> { d.dispose(); });               
        p.add( b= new JButton("Update"));
        b.addActionListener((ActionEvent e) -> { 
            grbl.setGRBLSettings(settings); 
            d.dispose();
        });
        d.getContentPane().add(p);
        d.pack();
        //d.setSize(new Dimension(460, d.getHeight()));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }//GEN-LAST:event_jMenuItemGRBLSettingsActionPerformed

    private void jMenuItemExecuteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExecuteAllActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_FOCUS_VIEW, 1, null);
        executeGCODE(blocksviewer.getDocumentToExecute(false, false));
        updateLaserPosition();
    }//GEN-LAST:event_jMenuItemExecuteAllActionPerformed

    private void jMenuItemExecuteSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExecuteSelectedActionPerformed
        int r;
        blocksviewer.doAction(JBlocksViewer.ACTION_FOCUS_VIEW, 0, null);
        
        if ( blocksviewer.hasHeaderFooter())
            r = JOptionPane.showConfirmDialog(this, "Use Header/Footer with selected blocks?", "Execute selection", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        else r = 1;
        
        if ( r != 2) {      
            executeGCODE(blocksviewer.getDocumentToExecute(true,(r==0)));
        }
        updateLaserPosition();
    }//GEN-LAST:event_jMenuItemExecuteSelectedActionPerformed

    private void jMenuItemGRBLSoftResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLSoftResetActionPerformed
        grbl.softReset();
    }//GEN-LAST:event_jMenuItemGRBLSoftResetActionPerformed

    private void jMenuItemGRBLKillAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLKillAlarmActionPerformed
        grbl.killAlarm();
    }//GEN-LAST:event_jMenuItemGRBLKillAlarmActionPerformed

    private void jMenuItemGRBLMoveHeadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLMoveHeadActionPerformed
        if ( grbl.isIdle())
            blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_GRBL_HEAD, showLaserPosition?1:0, null);
    }//GEN-LAST:event_jMenuItemGRBLMoveHeadActionPerformed

    private void jMenuItemGRBLHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLHomeActionPerformed
        grbl.goHome();
    }//GEN-LAST:event_jMenuItemGRBLHomeActionPerformed

    private void jMenuItemGRBLJogWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLJogWindowActionPerformed
        if ( jogWindow == null) jogWindow = new JJoGFrame(grbl);
        if ( ! jogWindow.isVisible()) jogWindow.setLocationRelativeTo(this);
        jogWindow.setVisible(true);
    }//GEN-LAST:event_jMenuItemGRBLJogWindowActionPerformed

    private void jCheckBoxMenuItemShowEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowEditorActionPerformed
        jPanelEditor.setVisible(jCheckBoxMenuItemShowEditor.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemShowEditorActionPerformed

    private void jTextFieldEditedBlockFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldEditedBlockFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldEditedBlockFocusGained

    private void jMenuItemAddFromHeadPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddFromHeadPosActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_POINT_FROM_HEAD_POSITION, 0, grbl.getWPos());
    }//GEN-LAST:event_jMenuItemAddFromHeadPosActionPerformed

    private void jMenuItemGRBLWPosAsMPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLWPosAsMPosActionPerformed
        grbl.pushCmd("G10L20P1X0Y0Z0");
    }//GEN-LAST:event_jMenuItemGRBLWPosAsMPosActionPerformed

    boolean threadFinished, stopShowBoundariesThread = false;
    @SuppressWarnings("SleepWhileInLoop")
    private void jMenuItemGRBLShowBoundariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLShowBoundariesActionPerformed
        Rectangle2D bounds = blocksviewer.getSelectionBoundary(false);
        if ( bounds != null) {
            double height = Double.POSITIVE_INFINITY;
            if ( ! grbl.isLaserMode()) {
                try {
                    String zh = JOptionPane.showInputDialog(this, "Z height ?", confFrame.conf.safeZHeightForMoving);
                    if ( zh == null) return;
                    else height = Double.valueOf(zh);
                } catch ( NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalide height", "Show boundaries", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
              
            String res;
            int speed;
            try {
                res = JOptionPane.showInputDialog(this, "Speed ?", confFrame.conf.jogSpeed);
                if ( res == null) return;
                speed = Integer.parseInt(res);
            } catch ( NumberFormatException e) { 
                JOptionPane.showMessageDialog(this, "Invalide height", "Show boundaries", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            
            Thread t = new Thread( new Runnable() {
                int border, speed;
                double height;
                public Runnable setValue(double height, int speed) {
                    this.height = height;
                    this.speed= speed;
                    return this;
                }
                @Override
                @SuppressWarnings("SleepWhileInLoop")
                public void run() {           
                    if ( height != Double.POSITIVE_INFINITY) grbl.pushCmd("G0Z"+height);
                    grbl.moveHeadTo(new Point2D.Double(bounds.getX(), bounds.getY()),false, confFrame.conf.jogSpeed);
                    grbl.pushCmd("G1F"+speed+"M3S1");
                    while( ! stopShowBoundariesThread ) { 
                        switch ( grbl.getState() ){
                            case GRBLControler.GRBL_STATE_IDLE:
                                //System.serialWriter.println("change...");
                                switch ( border) {
                                    case 0: grbl.moveHeadTo(new Point2D.Double(bounds.getX(), bounds.getY()),true, 0); break;
                                    case 1: grbl.moveHeadTo(new Point2D.Double(bounds.getX()+bounds.getWidth(), bounds.getY()),true, 0); break;
                                    case 2: grbl.moveHeadTo(new Point2D.Double(bounds.getX()+bounds.getWidth(), bounds.getY()+bounds.getHeight()),true, 0); break;
                                    case 3: grbl.moveHeadTo(new Point2D.Double(bounds.getX(), bounds.getY()+bounds.getHeight()),true, 0); break;
                                }
                                border = (border+1)%4;
                            case GRBLControler.GRBL_STATE_RUN:
                                try { Thread.sleep(100); } catch (InterruptedException ex) { }
                                break;
                            default:
                                // receivedError or receivedAlarm !
                                grbl.holdAndReset();
                                threadFinished = true;
                                //System.serialWriter.println("finished on receivedError.");
                                return;
                        }
                       
                    }
                    grbl.holdAndReset();
                    threadFinished = true;
                    //System.serialWriter.println("finished.");
                }
            }.setValue(height, speed), "ShowBoundsThread");
            stopShowBoundariesThread=false;
            t.start();
            JOptionPane.showMessageDialog(this, "Running on boundaries ...", "GRBL is running", JOptionPane.INFORMATION_MESSAGE);
            stopShowBoundariesThread = true;
            while( ! threadFinished)
                try { Thread.sleep(1000); } catch (InterruptedException ex) { }
            
            System.out.println("Boundary Thread finished.");
            if ( showLaserPosition) grbl.pushCmd("G1F100M3S1");
        } else
            JOptionPane.showMessageDialog(this, "Select a path first.", "Show boundaries", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemGRBLShowBoundariesActionPerformed

    private void jMenuItemGRBLShowLogWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLShowLogWindowActionPerformed
        if ( jLogFrame == null) {
            jLogFrame = new JLogFrame(grbl);
            jLogFrame.addLog("Starting log...\n", false);
        }
        if ( ! jLogFrame.isVisible()) jLogFrame.setLocationRelativeTo(this);
        jLogFrame.setVisible(true);
    }//GEN-LAST:event_jMenuItemGRBLShowLogWindowActionPerformed

    private void jCheckBoxMenuItemGRBLShowLaserPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemGRBLShowLaserPositionActionPerformed
        showLaserPosition = jCheckBoxMenuItemGRBLShowLaserPosition.isSelected();
        updateLaserPosition();
    }//GEN-LAST:event_jCheckBoxMenuItemGRBLShowLaserPositionActionPerformed

    private void jMenuItemCursorAtHeadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCursorAtHeadActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_CURSOR_AT_HEAD, 0, null);
    }//GEN-LAST:event_jMenuItemCursorAtHeadActionPerformed

    private void jMenuItemAddHeaderFooterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddHeaderFooterActionPerformed
        blocksviewer.addDefaultGCodeHeaderFooter();
    }//GEN-LAST:event_jMenuItemAddHeaderFooterActionPerformed

    private void jMenuItemSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSortActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SORT_STARTS, 0, null);
    }//GEN-LAST:event_jMenuItemSortActionPerformed

    private void jMenuItemReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReverseActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_REVERSE_SELECTION, 0, null);
    }//GEN-LAST:event_jMenuItemReverseActionPerformed

    private void jMenuItemAlignLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignLeftActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_LEFT, null);
    }//GEN-LAST:event_jMenuItemAlignLeftActionPerformed

    private void jCheckBoxMenuItemShowStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowStartActionPerformed
        blocksviewer.setShowStartPoints( jCheckBoxMenuItemShowStart.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemShowStartActionPerformed

    private void jMenuItemAlignRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignRightActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_RIGHT, null);
    }//GEN-LAST:event_jMenuItemAlignRightActionPerformed

    private void jMenuItemAlignBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignBottomActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_TOP, null);
    }//GEN-LAST:event_jMenuItemAlignBottomActionPerformed

    private void jMenuItemAlignTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignTopActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_BOTTOM, null);
    }//GEN-LAST:event_jMenuItemAlignTopActionPerformed

    private void jMenuItemGRBLSetMPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGRBLSetMPosActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_MPOS, 0, null);
    }//GEN-LAST:event_jMenuItemGRBLSetMPosActionPerformed

    private void jMenuItemAddPocketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddPocketActionPerformed
        String l = JOptionPane.showInputDialog(this, "Offset distance (near tool diameter) ?", blocksviewer.getConfiguration().toolDiameter/2);
        if ( l != null)
            blocksviewer.doAction(JBlocksViewer.ACTION_MAKE_POCKET, Double.valueOf(l), null);
    }//GEN-LAST:event_jMenuItemAddPocketActionPerformed

    private void jMenuItemSimplifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSimplifyActionPerformed
        String l = JOptionPane.showInputDialog(this, "Distance min between points ?", blocksviewer.getConfiguration().toolDiameter/2);
        double value;
        if ( l != null) {
            try {
                value = Double.parseDouble(l);
            } catch ( NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Must be >= to 0.1");
                return;
            }
            blocksviewer.doAction(JBlocksViewer.ACTION_SIMPLIFY_ANGLE, value, null);
        }
    }//GEN-LAST:event_jMenuItemSimplifyActionPerformed

    private void jMenuItemMakeCutPathIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMakeCutPathIActionPerformed
        String l = JOptionPane.showInputDialog(this, "Offset distance (normaly half of tool diameter) ?", blocksviewer.getConfiguration().toolDiameter/2);
        double value;
        if ( l != null) {
            try {
                value = Double.parseDouble(l);
            } catch ( NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Must be >= to 0.1");
                return;
            }
            blocksviewer.doAction(JBlocksViewer.ACTION_MAKE_OFFSET_CUT, -value, null);
        }
    }//GEN-LAST:event_jMenuItemMakeCutPathIActionPerformed

    private void jMenuItemMoveWithMouseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMoveWithMouseActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MOVE, 0, null);
    }//GEN-LAST:event_jMenuItemMoveWithMouseActionPerformed

    private void jMenuItemDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDistanceActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_DISTANCE, 0, null);
    }//GEN-LAST:event_jMenuItemDistanceActionPerformed

    private void jMenuItemMakeCutPathOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMakeCutPathOActionPerformed
        String l = JOptionPane.showInputDialog(this, "Offset distance (normaly half of tool diameter) ?", blocksviewer.getConfiguration().toolDiameter/2);
        double value;
        if ( l != null) {
            try {
                value = Double.parseDouble(l);
            } catch ( NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Must be >= to 0.1");
                return;
            }
            blocksviewer.doAction(JBlocksViewer.ACTION_MAKE_OFFSET_CUT, value, null);
        }
    }//GEN-LAST:event_jMenuItemMakeCutPathOActionPerformed

    private void jMenuItemGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGroupActionPerformed
        String name = JOptionPane.showInputDialog(this, "Group name ?", "group");
        if ( name != null)
            blocksviewer.doAction(JBlocksViewer.ACTION_GROUP_UNGROUP, 1, name);
    }//GEN-LAST:event_jMenuItemGroupActionPerformed

    private void jMenuItemUngroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUngroupActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_GROUP_UNGROUP, 0, null);
    }//GEN-LAST:event_jMenuItemUngroupActionPerformed
    
    private void goNextField(JTextField tf) {
        blocksviewer.update(false);
        updatePropertiesPanel();
        tf.requestFocusInWindow();
        tf.setSelectionStart(0);
        tf.setSelectionEnd(100);
    }
    
    private void jTextFieldFeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFeedActionPerformed
        try {
            if ( jTextFieldFeed.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setFeed(Double.NaN);
                jTextFieldFeed.setBackground(Color.white);
                if (evt != null) goNextField(jTextFieldPower);   
            } else {
                
                double feedR = Double.valueOf(jTextFieldFeed.getText());
                if ( feedR > 0) {
                    curentEditedProperties.setFeed( feedR);
                    jTextFieldFeed.setBackground(Color.white);     
                    if (evt != null) goNextField(jTextFieldPower);     
                } else 
                    jTextFieldFeed.setBackground(Color.red);          
            }
            
        } catch ( NumberFormatException e) {
            jTextFieldFeed.setBackground(Color.red);
        }
    }//GEN-LAST:event_jTextFieldFeedActionPerformed

    private void jTextFieldPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPassActionPerformed
        try {
            if ( jTextFieldPass.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setPassCount( -1);
            } else {
                int pC = Integer.valueOf(jTextFieldPass.getText());
                if ( pC > 0) curentEditedProperties.setPassCount( pC);
                else {
                    jTextFieldPass.setBackground(Color.red);
                    return;
                }
            }
            jTextFieldPass.setBackground(Color.white);
            if (evt != null) goNextField(jTextFieldZStart);
        } catch ( NumberFormatException e) {
            jTextFieldPass.setBackground(Color.red);
        }
    }//GEN-LAST:event_jTextFieldPassActionPerformed

    private void jTextFieldPassDephtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPassDephtActionPerformed
        try {
            if ( jTextFieldPassDepht.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setPassDepth(Double.NaN);
            } else {
                try {
                    double pD = Double.valueOf(jTextFieldPassDepht.getText());        
                    if ( pD <= 0) {
                        jTextFieldPassDepht.setBackground(Color.red);
                        return;
                    }
                    curentEditedProperties.setPassDepth( pD);
                } catch ( NumberFormatException e) {
                    jTextFieldPassDepht.setBackground(Color.red);
                    return;
                }
            }
            jTextFieldPassDepht.setBackground(Color.white);
            if (evt != null) goNextField( jTextFieldZEnd );
        } catch ( NumberFormatException e) { }
    }//GEN-LAST:event_jTextFieldPassDephtActionPerformed

    private void jTextFieldZStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldZStartActionPerformed
        try {
            if ( jTextFieldZStart.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setZStart(Double.NaN);
                jTextFieldZStart.setBackground(Color.white);
            } else {
                try {
                    double zStart = Double.valueOf(jTextFieldZStart.getText());
                    curentEditedProperties.setZStart( zStart);
                    if ( ! Double.isNaN(curentEditedProperties.getZStart()) && (curentEditedProperties.getZEnd() > zStart)) {
                        jTextFieldZStart.setBackground(Color.red);
                        return;
                    } else
                        if ( zStart < 0)
                            jTextFieldZStart.setBackground(Color.orange);
                        else
                            jTextFieldZStart.setBackground(Color.white);
                } catch ( NumberFormatException e) {
                    jTextFieldZStart.setBackground(Color.red);
                    return;
                } 
            }
            if (evt != null) goNextField(jTextFieldPassDepht);
        } catch ( NumberFormatException e) { }
    }//GEN-LAST:event_jTextFieldZStartActionPerformed

    private void jTextFieldZEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldZEndActionPerformed
        try {
            if ( jTextFieldZEnd.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setZEnd( Double.NaN);
                jTextFieldZEnd.setBackground(Color.white);
            } else {
                double zEnd = Double.valueOf(jTextFieldZEnd.getText());
                curentEditedProperties.setZEnd( zEnd);
                if ( ! Double.isNaN(curentEditedProperties.getZStart()) && (curentEditedProperties.getZStart() < zEnd)) {
                    jTextFieldZEnd.setBackground(Color.red);
                    return;
                }
            }
            jTextFieldZEnd.setBackground(Color.white);
            if (evt != null) goNextField( jTextFieldFeed);
            
         } catch ( NumberFormatException e) {
                jTextFieldZEnd.setBackground(Color.red);
        }
    }//GEN-LAST:event_jTextFieldZEndActionPerformed

    private void jCheckBoxDisabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisabledActionPerformed
        curentEditedProperties.setEnabled(  ! jCheckBoxDisabled.isSelected());
        blocksviewer.update(true);
        updatePropertiesPanel();
    }//GEN-LAST:event_jCheckBoxDisabledActionPerformed

    private void jCheckBoxMenuItemShowToolBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowToolBarActionPerformed
        jToolBar1.setVisible( jCheckBoxMenuItemShowToolBar.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemShowToolBarActionPerformed

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        jMenuItemNewActionPerformed(evt);
    }//GEN-LAST:event_jButtonNewActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        jMenuItemSaveGCodeActionPerformed(evt);
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCutActionPerformed
        jMenuItemCutActionPerformed(evt);
    }//GEN-LAST:event_jButtonCutActionPerformed

    private void jButtonCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyActionPerformed
        jMenuItemCopyActionPerformed(evt);
    }//GEN-LAST:event_jButtonCopyActionPerformed

    private void jButtonPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPasteActionPerformed
        jMenuItemPasteActionPerformed(evt);
    }//GEN-LAST:event_jButtonPasteActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_DELETE, 0, null);
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStopActionPerformed
        if ( grbl.isConnected()) grbl.softReset();
    }//GEN-LAST:event_jButtonStopActionPerformed

    private void jButtonExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteActionPerformed
        jMenuItemExecuteAllActionPerformed(evt);
    }//GEN-LAST:event_jButtonExecuteActionPerformed

    private void jButtonExecSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecSelectionActionPerformed
        jMenuItemExecuteSelectedActionPerformed(evt);
    }//GEN-LAST:event_jButtonExecSelectionActionPerformed

    private void jButtonTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTextActionPerformed
        jMenuItemAddTextActionPerformed(evt);
    }//GEN-LAST:event_jButtonTextActionPerformed

    private void jToggleButtonShowLaserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonShowLaserActionPerformed
        showLaserPosition = jToggleButtonShowLaser.isSelected();
        jToggleButtonShowLaser.setIcon(
                new javax.swing.ImageIcon(getClass().getResource("/icons/LaserO"+(showLaserPosition?"n":"ff")+".gif"))); // NOI18N;
        updateLaserPosition();
    }//GEN-LAST:event_jToggleButtonShowLaserActionPerformed

    private void jButtonGRBLHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGRBLHomeActionPerformed
        jMenuItemGRBLHomeActionPerformed(evt);
    }//GEN-LAST:event_jButtonGRBLHomeActionPerformed

    private void jToggleButtonMoveHeadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonMoveHeadActionPerformed
        jMenuItemGRBLMoveHeadActionPerformed(evt);
        jToggleButtonMoveHead.setSelected(true);
    }//GEN-LAST:event_jToggleButtonMoveHeadActionPerformed

    private void jButtonUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUndoActionPerformed
        jMenuItemUndoActionPerformed(evt);
    }//GEN-LAST:event_jButtonUndoActionPerformed

    private void jTextFieldPowerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPowerActionPerformed
        try {
            if ( jTextFieldPower.getText().length() == 0) {
                if (evt != null) curentEditedProperties.setPower( -1);
                jTextFieldPower.setBackground(Color.white);
                if (evt != null) goNextField( jTextFieldPass);
            } else {
                int power = Integer.valueOf(jTextFieldPower.getText());
                if ( power >= 0) {
                    curentEditedProperties.setPower(power);
                    jTextFieldPower.setBackground(Color.white);
                    if (evt != null) goNextField( jTextFieldPass);
                } else {
                    jTextFieldPower.setBackground(Color.red);
                }
            }
        } catch ( NumberFormatException e) {
            jTextFieldPower.setBackground(Color.red);
        }
    }//GEN-LAST:event_jTextFieldPowerActionPerformed

    private void jToggleButtonShowDistanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonShowDistanceActionPerformed
        jMenuItemDistanceActionPerformed(evt);
    }//GEN-LAST:event_jToggleButtonShowDistanceActionPerformed

    private void jToggleButtonAddRectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonAddRectsActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_RECTANGLES, 0, null);
    }//GEN-LAST:event_jToggleButtonAddRectsActionPerformed

    private void jToggleButtonAddCirclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonAddCirclesActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_OVAL, 0, null);                                           
    }//GEN-LAST:event_jToggleButtonAddCirclesActionPerformed

    private void jToggleButtonAddLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonAddLinesActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_LINES, 0, null);  
    }//GEN-LAST:event_jToggleButtonAddLinesActionPerformed

    private void jMenuItemAddRoubndRectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddRoubndRectActionPerformed
        try {
            double w = Double.valueOf(JOptionPane.showInputDialog(this, "Width ?", ""));
            double h = Double.valueOf(JOptionPane.showInputDialog(this, "Height ?", ""));
            double r = Double.valueOf(JOptionPane.showInputDialog(this, "Corner radius ?", Math.min(w,h)/10));
            G1Path p = G1Path.makeRounRect(w,h,r);
            p.translate(blocksviewer.get2DCursor());
            addGElement(p);
        } catch ( Exception e) { }
    }//GEN-LAST:event_jMenuItemAddRoubndRectActionPerformed

    private void jMenuItemAddArcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddArcActionPerformed
        try {
            double diameter = Double.valueOf(JOptionPane.showInputDialog(this, "Diameter ?", ""));
            double startAngle = Double.valueOf(JOptionPane.showInputDialog(this, "Start angle (clockwise start at right) ?", "0"));
            double arcLen = Double.valueOf(JOptionPane.showInputDialog(this, "Angular arc length (negative for G3) ?", "360"));
            if ( arcLen == 0) return;
            while ( Math.abs(arcLen) > 360) arcLen = (arcLen < 0) ? 360+arcLen : arcLen-360;
            addGElement( new GArc("arc", blocksviewer.get2DCursor(), diameter/2, startAngle, arcLen));
        } catch ( Exception e) { }
    }//GEN-LAST:event_jMenuItemAddArcActionPerformed

    private void jMenuItemMakeFlattenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMakeFlattenActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MAKE_FLATTEN, 0, null);
    }//GEN-LAST:event_jMenuItemMakeFlattenActionPerformed

    private void jMenuItemFocusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFocusActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_FOCUS_VIEW, 0, null);
    }//GEN-LAST:event_jMenuItemFocusActionPerformed

    private void jMenuItemExportDXFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportDXFActionPerformed
        int res = -1;
        if ( blocksviewer.getSelectionBoundary(false) != null)
            res = JOptionPane.showConfirmDialog(this, "Export ony selection ?", "Export to DXF...", 
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if ( res == 2) return;
        
        JFileChooser f = new JFileChooser();
        f.setFileFilter(new FileNameExtensionFilter("AutoCAD Drawing Exchange Format (*.dxf)", "dxf"));
        if ( lastImportDir == null) {
            if ( blocksviewer.getName() != null) {
                lastImportDir = new File(blocksviewer.getName()).getParentFile();
            } else {
                lastImportDir = new File(".");
            }
        }
        f.setCurrentDirectory(lastImportDir);
        
        int rVal = f.showSaveDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) {
            lastImportDir = f.getSelectedFile().getParentFile();
            String fname = f.getSelectedFile().getAbsolutePath();
            if ( fname.indexOf('.') == -1) fname = fname.concat(".dxf");
            if (new File( fname).exists())
                if ( JOptionPane.showConfirmDialog(this, fname + " \nFile exits, overwrite it ?", "File Exist", JOptionPane.WARNING_MESSAGE)== JOptionPane.CANCEL_OPTION)
                        return;           
            try {
                blocksviewer.exportToDXF(fname, res == 0, 
                        JOptionPane.showConfirmDialog(this, "Use SPLine ?", "DXF Export...", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error:\n"+ex.getLocalizedMessage(), "DXF Export error", JOptionPane.ERROR_MESSAGE);
            }      
        }
    }//GEN-LAST:event_jMenuItemExportDXFActionPerformed

    private void jButtonKillAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKillAlarmActionPerformed
        grbl.killAlarm();
    }//GEN-LAST:event_jButtonKillAlarmActionPerformed

    private void jTextFieldFeedFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldFeedFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldFeedFocusGained

    private void jTextFieldPowerFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPowerFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldPowerFocusGained

    private void jTextFieldPassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPassFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldPassFocusGained

    private void jTextFieldZStartFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldZStartFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldZStartFocusGained

    private void jTextFieldPassDephtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPassDephtFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldPassDephtFocusGained

    private void jTextFieldZEndFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldZEndFocusGained
        blocksviewer.setKeyFocus(false);
    }//GEN-LAST:event_jTextFieldZEndFocusGained

    private void jCheckBoxAllAtOnceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAllAtOnceActionPerformed
        curentEditedProperties.setAllAtOnce(jCheckBoxAllAtOnce.isSelected());
    }//GEN-LAST:event_jCheckBoxAllAtOnceActionPerformed

    private void jTextFieldFeedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldFeedFocusLost
        jTextFieldFeedActionPerformed(null);
    }//GEN-LAST:event_jTextFieldFeedFocusLost

    private void jTextFieldPowerFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPowerFocusLost
        jTextFieldPowerActionPerformed(null);
    }//GEN-LAST:event_jTextFieldPowerFocusLost

    private void jTextFieldPassFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPassFocusLost
        jTextFieldPassActionPerformed(null);
    }//GEN-LAST:event_jTextFieldPassFocusLost

    private void jTextFieldZStartFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldZStartFocusLost
        jTextFieldZStartActionPerformed(null);
    }//GEN-LAST:event_jTextFieldZStartFocusLost

    private void jTextFieldPassDephtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldPassDephtFocusLost
        jTextFieldPassDephtActionPerformed(null);
    }//GEN-LAST:event_jTextFieldPassDephtFocusLost

    private void jTextFieldZEndFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldZEndFocusLost
        jTextFieldZEndActionPerformed(null);
    }//GEN-LAST:event_jTextFieldZEndFocusLost

    private void jTextFieldEditedBlockFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldEditedBlockFocusLost
        jTextFieldEditedBlockActionPerformed(null);
    }//GEN-LAST:event_jTextFieldEditedBlockFocusLost

    private void jMenuItemAddCylindricalPocketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddCylindricalPocketActionPerformed
        GCylindricalPocket cp = new GCylindricalPocket("cylinder-"+ GElement.getUniqID(), blocksviewer.get2DCursor(), 10, 5, 100, 30);
        
        if ( dialogManager.showDialogFor( CylindricalPocketInputPanel.class, cp) != null) {
            addGElement( cp);
        }
    }//GEN-LAST:event_jMenuItemAddCylindricalPocketActionPerformed

    private void jMenuItemAddDrillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddDrillActionPerformed
        if ( ((blocksviewer.getState() & (JBlocksViewer.STATE_SHAPE_SELECTED_FLAG|JBlocksViewer.STATE_SHAPES_SELECTED_FLAG)) != 0) &&
             (JOptionPane.showConfirmDialog(this, "Drill on center of each element selected ?", 
                     "New drill", JOptionPane.YES_NO_OPTION)== JOptionPane.OK_OPTION))
                blocksviewer.doAction(JBlocksViewer.ACTION_ADD_AT_CENTER, 0 , new GDrillPoint("drill", (Point2D)null));
        else {
            addGElement( new GDrillPoint("drill", blocksviewer.get2DCursor()));
        }
    }//GEN-LAST:event_jMenuItemAddDrillActionPerformed

    private void jMenuItemAddCrossActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddCrossActionPerformed
        try {
            double len = Double.valueOf(JOptionPane.showInputDialog(this, "Diameter ?", "10"));
            if ( ((blocksviewer.getState() & (JBlocksViewer.STATE_SHAPE_SELECTED_FLAG|JBlocksViewer.STATE_SHAPES_SELECTED_FLAG)) != 0) &&
             (JOptionPane.showConfirmDialog(this, "Put a cross on center of each element selected ?", 
                     "New cross", JOptionPane.YES_NO_OPTION)== JOptionPane.OK_OPTION))
                blocksviewer.doAction(JBlocksViewer.ACTION_ADD_AT_CENTER, 0 , 
                        G1Path.makeCross(null, len));
            else {
                addGElement(G1Path.makeCross(blocksviewer.get2DCursor(), len));
            }
            
        } catch (Exception e) { }
    }//GEN-LAST:event_jMenuItemAddCrossActionPerformed

    private void jMenuItemAddcurveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddcurveActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_CURVE, 0 , null);
    }//GEN-LAST:event_jMenuItemAddcurveActionPerformed

    private void jMenuItemAddCurvesCircleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddCurvesCircleActionPerformed
        try {
            double len = Double.valueOf(JOptionPane.showInputDialog(this, "Diameter ?", "10"));
            Point2D center = new Point2D.Double();
        
            final double delta = 0.552284749831; // (4/3)*tan(pi/8) = 4*(sqrt(2)-1)/3 = 0.552284749831
            GGroup g = new GGroup("curveCircle");
            for(int i =0; i < 4; i++) {
                GCode p1 = GCode.newAngularPoint(center, 1, 90*i, true);
                GCode p2 = GCode.newAngularPoint(center, 1, 90*(i+1), true);
                GCode cp1;
                GCode cp2;
                switch (i) {
                    case 0: 
                        cp1=new GCode(p1.getX(),p1.getY()-delta);
                        cp2=new GCode(p2.getX()+delta,p2.getY());
                        break;
                    case 1: 
                        cp1=new GCode(p1.getX()-delta,p1.getY());
                        cp2=new GCode(p2.getX(),p2.getY()-delta);
                        break;
                    case 2: 
                        cp1=new GCode(p1.getX(),p1.getY()+delta);
                        cp2=new GCode(p2.getX()-delta,p2.getY());
                        break;
                    default:
                        cp1=new GCode(p1.getX()+delta,p1.getY());
                        cp2=new GCode(p2.getX(),p2.getY()+delta);
                }
                g.add(new GSpline("curve", p1, cp1, cp2, p2));              
            }
            g.scale(center, len/2, len/2);
            g.translate(blocksviewer.get2DCursor());
            addGElement(g);
        } catch (Exception e) { }
    }//GEN-LAST:event_jMenuItemAddCurvesCircleActionPerformed

    private void jToggleButtonShowAngleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonShowAngleActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_SHOW_ANGLE, 0, null);
    }//GEN-LAST:event_jToggleButtonShowAngleActionPerformed

    private void jMenuItemAddGearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddGearActionPerformed
        try {
            int nbTeeth = Integer.valueOf(JOptionPane.showInputDialog(this, "Number of teeth ?", 12));
            double pressureA = Double.valueOf(JOptionPane.showInputDialog(this, "Pressure angle (> 0) ?", 14));
            double circularPitch = Double.valueOf(JOptionPane.showInputDialog(this, "Circular pitch ?", 10));
            GGroup g = GearHelper.makeGear(nbTeeth, pressureA, circularPitch);
            g.translate(blocksviewer.get2DCursor());
            addGElement( g);
        } catch ( Exception e) { }
    }//GEN-LAST:event_jMenuItemAddGearActionPerformed

    private void jMenuItemAlignHorizontalyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignHorizontalyActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_HORIZONTALY, null);
    }//GEN-LAST:event_jMenuItemAlignHorizontalyActionPerformed

    private void jMenuItemAlignVerticalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAlignVerticalActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_VERTICALY, null);
    }//GEN-LAST:event_jMenuItemAlignVerticalActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ALIGN, JBlocksViewer.ALIGN_CENTER, null);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jCheckBoxMenuItenItemShowPictureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItenItemShowPictureActionPerformed
        if ( backgroundPicturePanel == null ) {
            backgroundPicturePanel = new BackgroundPicturePanel(this, blocksviewer.getBackgroundPictureParameters(), "Background picture");
        }
        backgroundPicturePanel.showDialog(this);
        jCheckBoxMenuItenItemShowPicture.setSelected(backgroundPicturePanel.isImageVisible());
    }//GEN-LAST:event_jCheckBoxMenuItenItemShowPictureActionPerformed

    private void jMenuItemCopyOpenScadPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyOpenScadPolygonActionPerformed
        /*String code = blocksviewer.copyOpenScadCodeOfSelectedShapeToClipboard();
        if ( code != null) {
            JFrame f = new JFrame();
            f.getContentPane().addGElement( new JTextArea(code));
            f.pack();
            f.setVisible(true);
        }*/
        blocksviewer.copyOpenScadCodeOfSelectedShapeToClipboard();
    }//GEN-LAST:event_jMenuItemCopyOpenScadPolygonActionPerformed

    private SetCursorToPanel setCursorPositionPanel;
    JTextField setCursorPositionDialogTx, setCursorPositionDialogTy;
    
    private void jMenuItemSetCursorToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetCursorToActionPerformed
        
        if ( setCursorPositionPanel == null) {
            setCursorPositionPanel = new SetCursorToPanel(this);
           
        }
        SetCursorToPanel.Choice res = setCursorPositionPanel.showDialog();
        double x = setCursorPositionPanel.x;
        double y = setCursorPositionPanel.y;
        switch ( res) {
            case CURSOR: 
                x += blocksviewer.get2DCursor().getX();
                y += blocksviewer.get2DCursor().getY();
            case ORIGIN:  
                blocksviewer.set2DCursorTo(x, y);
            case CANCEL: 
                return;
                
            default:
                Rectangle2D bounds = blocksviewer.getSelectionBoundary(false);
                if ( bounds == null) break;
                switch ( res) {
                    case SEL_UL:
                    case SEL_L:
                    case SEL_DL:
                        x = bounds.getX(); 
                        break;
                    case SEL_UR:
                    case SEL_R:
                    case SEL_DR:
                        x = bounds.getX()+ bounds.getWidth(); 
                        break;
                    default:
                        x = bounds.getCenterX();
                }
                switch ( res) {
                    case SEL_UL:
                    case SEL_U:
                    case SEL_UR:
                        y = bounds.getY()+ bounds.getHeight(); 
                        break;
                    case SEL_DL:
                    case SEL_D:
                    case SEL_DR:
                        y = bounds.getY(); 
                        break;
                    default:
                        y = bounds.getCenterY();
                }
                blocksviewer.set2DCursorTo(x, y);
        }
    }//GEN-LAST:event_jMenuItemSetCursorToActionPerformed

    private void jMenuItemAddOvalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddOvalActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_CIRCLES, 0, null);
    }//GEN-LAST:event_jMenuItemAddOvalActionPerformed

    private void jMenuItemAddHullActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddHullActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_HULL, 0, null);
    }//GEN-LAST:event_jMenuItemAddHullActionPerformed

    private void jMenuItemRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRedoActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_REDO, 0, null);
    }//GEN-LAST:event_jMenuItemRedoActionPerformed

    private void jMenuItemAddBoundsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddBoundsActionPerformed
        
        Rectangle2D bounds = blocksviewer.getSelectionBoundary(false);
        String m = JOptionPane.showInputDialog(this, "Marge", "0");
        try {
            double marge = Double.valueOf(m);
            addGElement(G1Path.newRectangle(new GCode(bounds.getX()-marge, bounds.getY()-marge), 
                    new GCode(bounds.getX()+bounds.getWidth()+marge, bounds.getY()+bounds.getHeight()+marge)));
        } catch ( NumberFormatException e) { }
    }//GEN-LAST:event_jMenuItemAddBoundsActionPerformed

    JFontChooserPanel hFontChooser;
    private void jMenuItemAddTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddTextActionPerformed
        if ( hFontChooser == null)
            hFontChooser = new JFontChooserPanel();
        
        GGroup g = hFontChooser.showFontChooserWindow();
        if ( g != null) {
            
            g.translate( blocksviewer.get2DCursor());
            addGElement(g);
        }
    }//GEN-LAST:event_jMenuItemAddTextActionPerformed
    
    private void jToggleButtonHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonHoldActionPerformed
        if ( grbl.getState() != GRBLControler.GRBL_STATE_HOLD) grbl.hold();
        else grbl.cycleStartResume();    
    }//GEN-LAST:event_jToggleButtonHoldActionPerformed

    private void jMenuItemAddLinkedPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddLinkedPathActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_LINKED_PATHS, 0, null);
    }//GEN-LAST:event_jMenuItemAddLinkedPathActionPerformed

    private void jMenuItemAddSphericalPocketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddSphericalPocketActionPerformed
        final Point2D p = blocksviewer.get2DCursor();
        GSphericalPocket sp = new GSphericalPocket("sp-"+GElement.getUniqID(), p.getX(), p.getY(), 20, 10);
        
        spherePocketEditor = (SphericalPocketInputPanel)dialogManager.showDialogFor( SphericalPocketInputPanel.class, sp);
        if ( spherePocketEditor != null) addGElement(sp);
    }//GEN-LAST:event_jMenuItemAddSphericalPocketActionPerformed

    private void jMenuItemAddIntersectionPointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddIntersectionPointsActionPerformed
        if ( ! blocksviewer.doAction(JBlocksViewer.ACTION_ADD_INTERSECTION_POINTS, 0, null)) {
            JOptionPane.showMessageDialog(this, "Select only two flat shapes.");
        }
    }//GEN-LAST:event_jMenuItemAddIntersectionPointsActionPerformed

    private void jToggleButtonZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonZoomActionPerformed
        if ( blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_FOCUS) blocksviewer.clearMouseMode();
        else jMenuItemFocusActionPerformed(evt);
    }//GEN-LAST:event_jToggleButtonZoomActionPerformed

    private void jMenuItemAddTextOnPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddTextOnPathActionPerformed
        Class c = blocksviewer.getFirtsSelectedElementClass();
        if ( (c != null) && (c != GGroup.class)) {
            if ( hFontChooser == null) hFontChooser = new JFontChooserPanel();
            GGroup g = hFontChooser.showFontChooserWindow();
            if ( g != null) 
                blocksviewer.doAction(JBlocksViewer.ACTION_MAP_TEXT_TO_PATH, 0, 
                        new GTextOnPath(g.getName(), hFontChooser.getChoosedFont(), hFontChooser.getChoosedSize(), hFontChooser.getChoosedText(), g));
        }
    }//GEN-LAST:event_jMenuItemAddTextOnPathActionPerformed

    public void editFontOf(GElement gElement) {
        if ( gElement instanceof GTextOnPath) {
            GTextOnPath e = ((GTextOnPath)gElement);
            
            if ( hFontChooser == null)
            hFontChooser = new JFontChooserPanel();
        
            hFontChooser.setText( e.getText());
            hFontChooser.setFont(e.getFont(), e.getFontSize());
            
            if ( hFontChooser.showFontChooserWindow() != null) {
                e.setText( hFontChooser.getChoosedText());
                e.changeFont( hFontChooser.getChoosedFont(), hFontChooser.getChoosedSize());
            }
        }
    }
    
    private void jMenuItemAddMixedPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddMixedPathActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_ADD_MIXED_PATH, 0, null);               
    }//GEN-LAST:event_jMenuItemAddMixedPathActionPerformed

    private void jMenuItemMoveCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMoveCenterActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_MOVE_CENTER, 0, null);
    }//GEN-LAST:event_jMenuItemMoveCenterActionPerformed

    private void jMenuItemInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInvertSelectionActionPerformed
        blocksviewer.doAction(JBlocksViewer.ACTION_INVERT_SELECTION, 0, null);   
    }//GEN-LAST:event_jMenuItemInvertSelectionActionPerformed

    private void jMenuItemMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMoveActionPerformed
        JMovePanel p = (JMovePanel)dialogManager.showDialogFor( JMovePanel.class, null);
        if ( p != null) {
            blocksviewer.moveCopySelection(p.deltaX == Double.POSITIVE_INFINITY ? 0 : p.deltaX,
                                           p.deltaY == Double.POSITIVE_INFINITY ? 0 : p.deltaY,
                                           0, false, false, false);
        }
    }//GEN-LAST:event_jMenuItemMoveActionPerformed

    /** 
     * Called by BlockViewer to change GRBL gantry position.
     * @param position tell GRBL to move gantry at this position.
     */
    @Override
    public void moveGantry(Point2D position) {
        if ( grbl.isIdle()) {          
            grbl.pushCmd("G0X"+position.getX()+"Y"+position.getY());
        }
    }

    @Override
    public void setVirtualMachinePosition(Point2D pos) {
        SwingUtilities.invokeLater(()->{ grbl.setVMPos(pos); });
    }

    @Override
    public void updatePropertiesPanel() {
        //if ( changed) blocksviewer.remplace();
        
        if ( (curentEditedProperties = blocksviewer.getSelectionProperties()) != null) {
            EngravingProperties herited = blocksviewer.getParentHeritedPropertiesOfSelection();
            if ( herited == null) herited = new EngravingProperties();
            
            if ( blocksviewer.isEditedRootGroup()) {
                jCheckBoxDisabled.setSelected(false);
                jCheckBoxDisabled.setEnabled(false);
            } else {
                jCheckBoxDisabled.setSelected( ! ( herited.isEnabled() & curentEditedProperties.isEnabled()));
                jCheckBoxDisabled.setEnabled( herited.isEnabled());
            }
            if ( ! herited.isEnabled()) setPropertiesEnabled(false, false);    
            else {
                setPropertiesEnabled(curentEditedProperties.isEnabled(), herited.isAllAtOnce()); 
                
                if ( herited.isAllAtOnce()) {
                    jCheckBoxAllAtOnce.setSelected(true);
                    jCheckBoxAllAtOnce.setEnabled(false);
                    jLabelPass.setForeground(Color.lightGray);
                    jLabelStart.setForeground(Color.lightGray);
                    jLabelEnd.setForeground(Color.lightGray);
                    jLabelDepth.setForeground(Color.lightGray);
                    
                    jTextFieldPass.setForeground(Color.lightGray);
                    jTextFieldZStart.setForeground(Color.lightGray);
                    jTextFieldZEnd.setForeground(Color.lightGray);
                    jTextFieldPassDepht.setForeground(Color.lightGray);
                    jLabelPass.setFont( jLabelEnd.getFont().deriveFont(Font.ITALIC));
                    jLabelStart.setFont( jLabelEnd.getFont().deriveFont(Font.ITALIC));
                    jLabelEnd.setFont( jLabelEnd.getFont().deriveFont(Font.ITALIC));
                    jLabelDepth.setFont( jLabelDepth.getFont().deriveFont(Font.ITALIC));
                } else {
                    if ((herited.getPassCount() < 2) && (curentEditedProperties.getPassCount() < 2)) {
                        jLabelEnd.setFont( jLabelEnd.getFont().deriveFont(Font.ITALIC));
                        jLabelDepth.setFont( jLabelDepth.getFont().deriveFont(Font.ITALIC));
                    } else  {
                        jLabelDepth.setFont( jLabelDepth.getFont().deriveFont(Font.PLAIN));
                        jLabelEnd.setFont( jLabelEnd.getFont().deriveFont(Font.PLAIN));
                    }  
                }
                
                jLabelPass.setForeground(curentEditedProperties.getPassCount() == -1 ? Color.black : Color.blue);
                jLabelFeed.setForeground(Double.isNaN(curentEditedProperties.getFeed()) ? Color.black : Color.blue);
                jLabelStart.setForeground( Double.isNaN(curentEditedProperties.getZStart()) ? 
                        (curentEditedProperties.getZStart() < 0 ? Color.orange : Color.black) : Color.blue);
                jLabelEnd.setForeground( Double.isNaN(curentEditedProperties.getZEnd()) ? Color.black : Color.blue);
                jLabelDepth.setForeground( Double.isNaN(curentEditedProperties.getPassDepth()) ? Color.black : Color.blue );
                jLabelPower.setForeground( curentEditedProperties.getPower() != -1 ? Color.blue : Color.black);
                    
                jCheckBoxAllAtOnce.setSelected( herited.isAllAtOnce() | curentEditedProperties.isAllAtOnce());         
                jTextFieldFeed.setText( Double.isNaN(curentEditedProperties.getFeed()) ? "" : GWord.GCODE_NUMBER_FORMAT.format(curentEditedProperties.getFeed()));
                jTextFieldPower.setText( curentEditedProperties.getPower() != -1 ? "" + curentEditedProperties.getPower() : "");              
                jTextFieldPass.setText(curentEditedProperties.getPassCount() != -1 ? "" + curentEditedProperties.getPassCount() : "");
                jTextFieldZStart.setText( Double.isNaN(curentEditedProperties.getZStart()) ? "" : GWord.GCODE_NUMBER_FORMAT.format(curentEditedProperties.getZStart()));
                jTextFieldPassDepht.setText( Double.isNaN(curentEditedProperties.getPassDepth()) ? "" : GWord.GCODE_NUMBER_FORMAT.format(curentEditedProperties.getPassDepth()));
                jTextFieldZEnd.setText( Double.isNaN(curentEditedProperties.getZEnd()) ? "" : GWord.GCODE_NUMBER_FORMAT.format(curentEditedProperties.getZEnd()));
            }
            jPanelProperties.setVisible( curentEditedProperties != null);
        }
    }
        
    private void setPropertiesEnabled( boolean enabled, boolean onlyFeedPower) {
        if ( onlyFeedPower) setPropertiesEnabled(false, false);
        
        jTextFieldFeed.setForeground( enabled ? Color.BLACK : Color.GRAY);
        jTextFieldPower.setForeground( enabled ? Color.BLACK : Color.GRAY);
        jLabelFeed.setForeground( enabled ? Color.BLACK : Color.GRAY);
        jLabelPower.setForeground( enabled ? Color.BLACK : Color.GRAY);
        
        if ( ! onlyFeedPower) {
            jCheckBoxAllAtOnce.setEnabled(enabled);                           
            jTextFieldPass.setForeground( enabled ? Color.BLACK : Color.GRAY);
            jTextFieldZStart.setForeground( enabled ? Color.BLACK : Color.GRAY);
            jTextFieldZEnd.setForeground( enabled ? Color.BLACK : Color.GRAY);
            jTextFieldPassDepht.setForeground( enabled ? Color.BLACK : Color.GRAY);
            jLabelPass.setEnabled( enabled);
            jLabelStart.setEnabled( enabled);
            jLabelEnd.setEnabled( enabled);
            jLabelPass.setEnabled( enabled);
            jLabelDepth.setEnabled( enabled);
        }
    }
    
    @Override
    public final void updateGUIAndStatus(String msg) {
               
        if ( msg != null) {
            jLabelMessage.setText(msg);
            jLabelMessage.invalidate();
        }
        
        if (blocksviewer.isEditedRootGroup()) jLabelEditType.setText("Doc ");
        else {
            Class c = blocksviewer.getSelectedElementClass();
            if ( c == ArrayList.class ) {
                jLabelEditType.setText("Selection");
                jTextFieldEditedBlock.setEnabled(false);
            }
            else jTextFieldEditedBlock.setEnabled(true);
            if ( c == GGroup.class) jLabelEditType.setText("Group ");
            else jLabelEditType.setText("Elem ");
        }
        
        jLabelFormInfo.setText(blocksviewer.getSelectedBlocksInfo());
        //jLabelFormInfo.invalidate();
        //jLabelFormInfo.repaint();

        boolean noEdition = ((getFocusOwner() == null) || ! (getFocusOwner() instanceof JTextField) || (blocksviewer.hasFocus())) ;

        if (noEdition) updatePropertiesPanel();
        
        if ( ! jTextFieldEditedBlock.isFocusOwner())  {
            jTextFieldEditedBlock.setText( blocksviewer.getSelectedBlockName());
            jTextFieldEditedBlock.setEnabled(true);
        }
        
        int s = blocksviewer.getState();
        boolean empty = blocksviewer.isEmpty();
        boolean edit = (s & JBlocksViewer.STATE_EDIT_MODE_FLAG) != 0;
        boolean point = (s & JBlocksViewer.STATE_POINT_SELECTED_FLAG) != 0;
        boolean points = (s & JBlocksViewer.STATE_POINTS_SELECTED_FLAG) != 0;
        boolean block = (s & JBlocksViewer.STATE_SHAPE_SELECTED_FLAG) != 0;
        boolean blocks = (s & JBlocksViewer.STATE_SHAPES_SELECTED_FLAG) != 0;
        boolean CBempyt = (s & JBlocksViewer.STATE_CLIPBOARD_EMPTY_FLAG) != 0;
        boolean canUndo = (s & JBlocksViewer.STATE_CAN_UNDO_FLAG) != 0;
        boolean canRedo = (s & JBlocksViewer.STATE_CAN_REDO_FLAG) != 0;
        //boolean noEdition = (s & JBlocksViewer.STATE_EDIT_LINE) == 0;
        boolean lines = (s & JBlocksViewer.STATE_LINE_SELECTED) != 0;
        boolean modified = (s & JBlocksViewer.STATE_DOCUMENT_MODIFIED) != 0;
        boolean grbl_ok = grbl.isConnected();
        boolean grbl_open = grbl.isComOpen();
        boolean grbl_idle = grbl_ok && grbl.getState() == GRBLControler.GRBL_STATE_IDLE;
        boolean grbl_laser = grbl_ok && grbl.isSettingsReady() && grbl.isLaserMode();
        Configuration conf = blocksviewer.getConfiguration();
        jButtonStop.setEnabled( grbl_ok);
        jButtonKillAlarm.setEnabled(grbl_ok && (grbl.getState()==GRBLControler.GRBL_STATE_ALARM));
        jButtonExecute.setEnabled( ! empty);
        jButtonExecSelection.setEnabled(block|blocks);
        jToggleButtonZoom.setSelected(blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_FOCUS);
        jButtonSave.setEnabled(modified);
        jButtonCopy.setEnabled((lines|point|points|block|blocks));
        jButtonDelete.setEnabled((lines|point|points|block|blocks));
        jButtonGRBLHome.setEnabled( grbl_ok && (grbl.isSettingsReady()?grbl.canHome():true));  
        jButtonPaste.setEnabled(! CBempyt);
        jButtonCut.setEnabled((lines|point|points|block|blocks));
        jButtonUndo.setEnabled( canUndo);
        jCheckBoxMenuItemGRBLShowLaserPosition.setEnabled( grbl_idle & grbl_laser);
        jCheckBoxMenuItemShowHeadPosition.setEnabled(grbl_ok);
        jCheckBoxMenuItemShowGrid.setEnabled(noEdition);
        jCheckBoxMenuItemSnapGrid.setEnabled(noEdition);
        jCheckBoxMenuItemSnapPoints.setEnabled(noEdition);
        jCheckBoxMenuItemShowMoves.setEnabled(noEdition);  
        jCheckBoxMenuItemShowWorkspace.setEnabled( (conf.workspaceWidth != 0) &&(conf.workspaceHeight != 0));
        jCheckBoxMenuItemShowGrid.setSelected(noEdition && ((s & JBlocksViewer.STATE_GRID_FLAG) != 0));
        jCheckBoxMenuItemSnapGrid.setSelected(noEdition && ((s & JBlocksViewer.STATE_SNAP_TO_GRID_FLAG) != 0));
        jCheckBoxMenuItemSnapPoints.setSelected(noEdition && ((s & JBlocksViewer.STATE_SNAP_TO_POINTS_FLAG) != 0));
        jCheckBoxMenuItemShowMoves.setSelected(((s & JBlocksViewer.STATE_SHOW_MOVES_FLAG) !=0));
        jMenuAdds.setEnabled(noEdition);
        jMenuAlign.setEnabled( (block|blocks| (edit & lines))& noEdition);
        jMenuMakeCutPath.setEnabled( (block|blocks)& noEdition);        
        jMenuItemGroup.setEnabled(blocks && noEdition);
        jMenuItemUngroup.setEnabled((block|blocks) && noEdition);
        jMenuItemGRBLShowLogWindow.setEnabled( noEdition);
        jMenuItemGRBLShowBoundaries.setEnabled( grbl_ok & grbl_idle & (block|blocks) & noEdition);
        jMenuItemAddFromHeadPos.setEnabled( grbl_ok & edit & noEdition);
        jMenuItemAddPoints.setEnabled(noEdition);
        jMenuItemAddBounds.setEnabled((block|blocks)&& noEdition);
        jMenuItemAddIntersectionPoints.setEnabled(blocks & noEdition);
        jMenuItemAddHull.setEnabled(block|blocks);
        jMenuItemAddLinkedPath.setEnabled( blocks & noEdition);
        jMenuItemAddMixedPath.setEnabled( noEdition);
        jMenuItemAddPocket.setEnabled( (block|blocks)& noEdition);
        jMenuItemAddTextOnPath.setEnabled( block & noEdition);
        jMenuItemExecuteSelected.setEnabled(  (block| blocks) & noEdition);
        jMenuItemExecuteAll.setEnabled(  ! blocksviewer.isEmpty() & noEdition);
        jMenuItemGRBLSettings.setEnabled( grbl_ok);
        jMenuItemGRBLCmd.setEnabled( grbl_ok & noEdition);
        jMenuItemGRBLSoftReset.setEnabled( grbl_ok);
        jMenuItemGRBLKillAlarm.setEnabled(grbl.getState()==GRBLControler.GRBL_STATE_ALARM);
        jMenuItemMakeFlatten.setEnabled( (block|blocks)& noEdition);
        jMenuItemSimplify.setEnabled( (point|points|block|blocks)& noEdition);
        jMenuItemFocus.setEnabled(noEdition);
        jMenuItemGRBLMoveHead.setEnabled( grbl_idle);
        jMenuItemGRBLHome.setEnabled( grbl_ok && grbl.canHome());
        jMenuItemGRBLConnect.setEnabled(!grbl_open);
        jMenuItemGRBLDisconnect.setEnabled(grbl_open);
        jMenuItemGRBLSetMPos.setEnabled(grbl_ok);
        jMenuItemGRBLWPosAsMPos.setEnabled(grbl_ok);
        jMenuItemUndo.setEnabled(canUndo && noEdition);
        jMenuItemRedo.setEnabled(canRedo && noEdition);
        jMenuItemSetCursor.setEnabled(noEdition);
        jMenuItemRename.setEnabled((block|blocks)&& noEdition);
        jMenuItemChStartPos.setEnabled( point && noEdition);
        jMenuItemExtract.setEnabled((point|points) && noEdition);
        jMenuItemCopy.setEnabled((lines|point|points|block|blocks)&& noEdition);
        jMenuItemPaste.setEnabled(! CBempyt && noEdition);
        jMenuItemCut.setEnabled((lines|point|points|block|blocks)&& noEdition);
        jMenuItemFlipH.setEnabled((block|blocks)&& noEdition);
        jMenuItemFlipV.setEnabled((block|blocks)&& noEdition);
        jMenuItemSort.setEnabled((block|blocks)&& noEdition);
        jMenuItemReverse.setEnabled((block|blocks|edit)&& noEdition);
        jMenuItemDuplicate.setEnabled((point|points|block|blocks)&& noEdition);
        jMenuItemMoveCenter.setEnabled((block|blocks)&& noEdition);
        jMenuItemMove.setEnabled((block|blocks)&& noEdition);
        jMenuItemMoveWithMouse.setEnabled((point|points|block|blocks)&& noEdition);
        jMenuItemJoin.setEnabled((points|blocks|block)&& noEdition);
        jMenuItemRotateCenter.setEnabled((block|blocks)&& noEdition);
        jMenuItemRotateP.setEnabled((block|blocks)&& noEdition);
        jMenuItemRotate.setEnabled((block|blocks)&& noEdition);
        jMenuItemScaleCenter.setEnabled((block|blocks)&& noEdition);
        jMenuItemScale.setEnabled((block|blocks)&& noEdition);
        jMenuItemScaleP.setEnabled((block|blocks)&& noEdition);
        jMenuItemSimplifyP.setEnabled(points&& noEdition);
        jMenuItemSimplifyByDistance.setEnabled((points|block|blocks) && noEdition);
        jMenuItemUndo.setEnabled(canUndo&& noEdition);
        jMenuItemFilter.setEnabled((edit|block|blocks)&& noEdition);
        jMenuItemSetAsFooter.setEnabled(block&& noEdition);
        jMenuItemSetAsHeader.setEnabled(block&& noEdition);
        jMenuItemMoveUp.setEnabled((lines|point|points|block|blocks)&& noEdition);
        jMenuItemMoveDown.setEnabled((lines|point|points|block|blocks)&& noEdition);
        jMenuItemCursorAtCenter.setEnabled( noEdition);
        jMenuItemCursorAtHead.setEnabled(grbl_ok && noEdition);
        jMenuItemCopyOpenScadPolygon.setEnabled(block|blocks);
        jToggleButtonShowLaser.setEnabled(grbl_idle && grbl_laser);
        jToggleButtonHold.setEnabled( grbl_ok);
        jToggleButtonHold.setSelected(grbl_ok && (grbl.getState() == GRBLControler.GRBL_STATE_HOLD));        
        jToggleButtonMoveHead.setEnabled( grbl_idle);
        jToggleButtonShowDistance.setSelected(blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_SHOW_DISTANCE);
        jToggleButtonShowAngle.setSelected(blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_SHOW_ANGLE);
        jToggleButtonMoveHead.setSelected(blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_MOVE_GANTRY);
        jToggleButtonAddRects.setSelected(! edit && (blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_ADD_RECTANGLES));
        jToggleButtonAddCircles.setSelected(! edit && blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_ADD_OVAL); 
        jToggleButtonAddLines.setSelected(blocksviewer.getMouseMode() == JBlocksViewer.MOUSE_MODE_ADD_LINES);  
    }

    @Override
    public void updateMouseCoord(int x, int y, double rx, double ry) {
        updateGUIAndStatus(null);
        jLabelMousePosition.setText("("+String.format(Locale.ROOT,"%.5f",rx)+", "+String.format(Locale.ROOT,"%.5f",ry)+")");
    }
    
    private void updateTitle() {
        final String title = "VGEditor - " + 
                ((savedFileName != null) ? savedFileName : "<new>") +
                (grbl.isSettingsReady() && confFrame.conf.exist(grbl.getVersion().split(":")[2])?" - (on " + grbl.getVersion().split(":")[2]+")":"");

        if ( ! getTitle().equals(title)) setTitle(title);
    }

    /**
     * Add a element into the document.
     * @param e the document to add
     */
    public void addGElement(GElement e) {
        blocksviewer.add(e);
        blocksviewer.clearMouseMode();
    }

    @Override
    public final void configurationChanged() {
        blocksviewer.applyConfiguration();
        grbl.setBackLashEnabled( confFrame.conf.useBackLash);
        grbl.setBackLashValues(confFrame.conf.backLashX, confFrame.conf.backLashY, confFrame.conf.backLashZ);
        updateTitle();
    }

    private void addToRecentFile(String file) {
        String[] recents = confFrame.conf.recentFiles.split("µ");
        String newR = "";
        boolean first = true;
        int n = 0;
        for (  String r : recents) {
            if ( ! r.equals(file) && (n++ < 20)) {
                newR += (first?r:"µ" + r);
                first = false;
            }
        }
        confFrame.conf.recentFiles = file + (first?"" : "µ" + newR);      
        confFrame.conf.saveDefault();
        updateRecentFilesMenu();
    }

    private void updateRecentFilesMenu() {
        jMenuRecent.removeAll();
        for( String r : confFrame.conf.recentFiles.split(Configuration.RECENT_FILE_SEPARATOR)) {
            if (lastImportDir==null ) lastImportDir = new File(r).getParentFile();
            
            if( ! r.isEmpty()) {
                JMenuItem m = new JMenuItem(r);
                jMenuRecent.add( m);
                m.addActionListener((ActionEvent e) -> {
                    Object o = e.getSource();
                    if ( o instanceof JMenuItem) openGCODE( ((JMenuItem) o).getText());
                });
            }
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void openGCODE(String fileName) {
        if ( fileName == null) {
            JFileChooser f = new JFileChooser();
            f.setDialogTitle("Ouvrir un fichier G-Code");
            f.setFileFilter(new FileNameExtensionFilter("G-Code", "gcode"));
            if ( lastImportDir != null) f.setCurrentDirectory(lastImportDir);
            int rVal = f.showOpenDialog(this);
            if ( rVal == JFileChooser.APPROVE_OPTION) {   
                String gcodeFileName = f.getSelectedFile().toString();

                lastImportDir = f.getSelectedFile().getParentFile();
                try {
                    if ( blocksviewer.isEmpty()) {
                        blocksviewer.setContent(JBlocksViewer.importGCODE(
                                gcodeFileName, blocksviewer.getBackgroundPictureParameters()), true);
                        savedFileName = gcodeFileName;
                        updateTitle();
                    } else 
                        new JEditorFrame(f.getSelectedFile().getAbsolutePath()).setVisible(true);
                    addToRecentFile(f.getSelectedFile().getAbsolutePath());
                } catch (IOException e) {
                    if ( e instanceof FileNotFoundException ) {
                        confFrame.conf.removeRecentFile(gcodeFileName);
                        updateRecentFilesMenu();
                    }
                    JOptionPane.showMessageDialog(this, "Error reading file : \n\n" + e.toString(),
                            "Import error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        } else {
            try {
                if ( blocksviewer.isEmpty()) {
                    blocksviewer.clearUndoRecords();
                    blocksviewer.setContent(JBlocksViewer.importGCODE(fileName, blocksviewer.getBackgroundPictureParameters()), true);
                    savedFileName = fileName;
                    updateTitle();
                } else 
                    new JEditorFrame(fileName).setVisible(true);
                
                addToRecentFile(fileName);
                lastImportDir = new File(fileName).getParentFile();
            } catch (IOException e) {
                if ( e instanceof FileNotFoundException ) {
                    confFrame.conf.removeRecentFile(fileName);
                    updateRecentFilesMenu();
                }
                JOptionPane.showMessageDialog(this, "Error reading file : \n\n" + e.toString(),
                        "Import error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        
        BackgroundPictureParameters p = blocksviewer.getBackgroundPictureParameters();
        if ( p.isVisible() && ! p.isLoaded() ) 
            try {
                p.reloadImage();
            } catch (Exception e) {
                if ( JOptionPane.showConfirmDialog(this, "The attached background image of this document is not found.\nWould you like to find it manualy ?", 
                        "Background Image ...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            jCheckBoxMenuItenItemShowPictureActionPerformed(null);
                        }
                    });
                }
            }
    }
    
    /**
     * Reactivate Laser (send M3S1G1F100) if GRBL is IDLE.
     */
    private void updateLaserPosition() {
        if (grbl.getState() != GRBLControler.GRBL_STATE_IDLE) return;
        if ( showLaserPosition)
            grbl.pushCmd("M3S8G1F100");
        else
            grbl.pushCmd("G0");
    }
    
    
    static JDialog printDialog = null;
    static JRunningPanel senderPanel;
    public void executeGCODE(GGroup document) {
        if ( document.size()==0) return;
        if ( printDialog == null) {
            printDialog = new JDialog(this, "GCODE Sender to GRBL", true);
            printDialog.setLayout(new BoxLayout(printDialog.getContentPane(), BoxLayout.Y_AXIS));           
            printDialog.getContentPane().add(senderPanel = new JRunningPanel(printDialog, grbl, confFrame.conf));
            printDialog.pack();
            printDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            printDialog.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent e) {
                                    if ( senderPanel.isPrinting()) {
                                        if (JOptionPane.showConfirmDialog(printDialog, "Stop the job and close Window ?") 
                                                                            != JOptionPane.OK_OPTION) 
                                            return;
                                        else 
                                            senderPanel.stopPrint();
                                    }
                                    printDialog.setVisible(false);
                                }   
                            });
        }
        senderPanel.setGroupToPrint(document);  
        if ( ! printDialog.isVisible()) printDialog.setLocationRelativeTo(this);
        printDialog.setVisible(true);
    }
    
    private String askForFile(boolean forSaving, File currentDirectory, FileNameExtensionFilter filter) {
        JFileChooser f = new JFileChooser();
        f.setFileFilter(filter);
        if ( currentDirectory != null) f.setCurrentDirectory(currentDirectory);
        
        int rVal;     
        if ( forSaving) rVal = f.showSaveDialog(this);
        else rVal = f.showOpenDialog(this);
        
        if ( rVal == JFileChooser.APPROVE_OPTION) {
            //lastImportDir = f.getSelectedFile().getParentFile();
            String fname = f.getSelectedFile().getAbsolutePath();
            if ( forSaving) {
                if ((fname.indexOf('.') == -1) && (filter.getExtensions().length == 1))
                        fname = fname.concat(filter.getExtensions()[0]);

                if ((new File( fname)).exists() )
                    if ((JOptionPane.showConfirmDialog(this, fname + " \nFile exits, overwrite it ?", "File Exist", JOptionPane.WARNING_MESSAGE)== JOptionPane.CANCEL_OPTION))
                        return null; 
            } else {
                if (! (new File( fname)).exists() ) {
                   JOptionPane.showMessageDialog(this, "File not found:\n"+fname, "Error", JOptionPane.ERROR_MESSAGE);
                   return null;
                } 
            }
            return fname;
        }
        return null;
    }  

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCopy;
    private javax.swing.JButton jButtonCut;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonExecSelection;
    private javax.swing.JButton jButtonExecute;
    private javax.swing.JButton jButtonGRBLHome;
    private javax.swing.JButton jButtonKillAlarm;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonPaste;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JButton jButtonText;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JCheckBox jCheckBoxAllAtOnce;
    private javax.swing.JCheckBox jCheckBoxDisabled;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemGRBLShowLaserPosition;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowEditor;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowGrid;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowHeadPosition;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowMoves;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowObjectSurface;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowStart;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowToolBar;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowWorkspace;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemSnapGrid;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemSnapPoints;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItenItemShowPicture;
    private javax.swing.JLabel jLabelContent;
    private javax.swing.JLabel jLabelDepth;
    private javax.swing.JLabel jLabelEditType;
    private javax.swing.JLabel jLabelEnd;
    private javax.swing.JLabel jLabelFeed;
    private javax.swing.JLabel jLabelFormInfo;
    private javax.swing.JLabel jLabelGRBLState;
    private javax.swing.JLabel jLabelMessage;
    private javax.swing.JLabel jLabelMousePosition;
    private javax.swing.JLabel jLabelPass;
    private javax.swing.JLabel jLabelPower;
    private javax.swing.JLabel jLabelStart;
    private javax.swing.JList<Object> jListGCode;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenuAdds;
    private javax.swing.JMenu jMenuAlign;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuBlocks;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuGCODE;
    private javax.swing.JMenu jMenuGRBL;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAddArc;
    private javax.swing.JMenuItem jMenuItemAddBounds;
    private javax.swing.JMenuItem jMenuItemAddCircle;
    private javax.swing.JMenuItem jMenuItemAddCross;
    private javax.swing.JMenuItem jMenuItemAddCurvesCircle;
    private javax.swing.JMenuItem jMenuItemAddCustom;
    private javax.swing.JMenuItem jMenuItemAddCylindricalPocket;
    private javax.swing.JMenuItem jMenuItemAddDrill;
    private javax.swing.JMenuItem jMenuItemAddFromHeadPos;
    private javax.swing.JMenuItem jMenuItemAddGear;
    private javax.swing.JMenuItem jMenuItemAddHeaderFooter;
    private javax.swing.JMenuItem jMenuItemAddHull;
    private javax.swing.JMenuItem jMenuItemAddIntersectionPoints;
    private javax.swing.JMenuItem jMenuItemAddLinkedPath;
    private javax.swing.JMenuItem jMenuItemAddMixedPath;
    private javax.swing.JMenuItem jMenuItemAddOval;
    private javax.swing.JMenuItem jMenuItemAddPocket;
    private javax.swing.JMenuItem jMenuItemAddPoints;
    private javax.swing.JMenuItem jMenuItemAddPolygon;
    private javax.swing.JMenuItem jMenuItemAddRectangle;
    private javax.swing.JMenuItem jMenuItemAddRipple;
    private javax.swing.JMenuItem jMenuItemAddRoubndRect;
    private javax.swing.JMenuItem jMenuItemAddSphericalPocket;
    private javax.swing.JMenuItem jMenuItemAddSpiral;
    private javax.swing.JMenuItem jMenuItemAddStar;
    private javax.swing.JMenuItem jMenuItemAddText;
    private javax.swing.JMenuItem jMenuItemAddTextOnPath;
    private javax.swing.JMenuItem jMenuItemAddcurve;
    private javax.swing.JMenuItem jMenuItemAlignBottom;
    private javax.swing.JMenuItem jMenuItemAlignHorizontaly;
    private javax.swing.JMenuItem jMenuItemAlignLeft;
    private javax.swing.JMenuItem jMenuItemAlignRight;
    private javax.swing.JMenuItem jMenuItemAlignTop;
    private javax.swing.JMenuItem jMenuItemAlignVertical;
    private javax.swing.JMenuItem jMenuItemChStartPos;
    private javax.swing.JMenuItem jMenuItemConf;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCopyOpenScadPolygon;
    private javax.swing.JMenuItem jMenuItemCursorAtCenter;
    private javax.swing.JMenuItem jMenuItemCursorAtHead;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemDistance;
    private javax.swing.JMenuItem jMenuItemDuplicate;
    private javax.swing.JMenuItem jMenuItemExecuteAll;
    private javax.swing.JMenuItem jMenuItemExecuteSelected;
    private javax.swing.JMenuItem jMenuItemExportDXF;
    private javax.swing.JMenuItem jMenuItemExportSVG;
    private javax.swing.JMenuItem jMenuItemExtract;
    private javax.swing.JMenuItem jMenuItemFilter;
    private javax.swing.JMenuItem jMenuItemFlipH;
    private javax.swing.JMenuItem jMenuItemFlipV;
    private javax.swing.JMenuItem jMenuItemFocus;
    private javax.swing.JMenuItem jMenuItemGRBLCmd;
    private javax.swing.JMenuItem jMenuItemGRBLConnect;
    private javax.swing.JMenuItem jMenuItemGRBLDisconnect;
    private javax.swing.JMenuItem jMenuItemGRBLHome;
    private javax.swing.JMenuItem jMenuItemGRBLJogWindow;
    private javax.swing.JMenuItem jMenuItemGRBLKillAlarm;
    private javax.swing.JMenuItem jMenuItemGRBLMoveHead;
    private javax.swing.JMenuItem jMenuItemGRBLSetMPos;
    private javax.swing.JMenuItem jMenuItemGRBLSettings;
    private javax.swing.JMenuItem jMenuItemGRBLShowBoundaries;
    private javax.swing.JMenuItem jMenuItemGRBLShowLogWindow;
    private javax.swing.JMenuItem jMenuItemGRBLSoftReset;
    private javax.swing.JMenuItem jMenuItemGRBLWPosAsMPos;
    private javax.swing.JMenuItem jMenuItemGroup;
    private javax.swing.JMenuItem jMenuItemImport;
    private javax.swing.JMenuItem jMenuItemInvertSelection;
    private javax.swing.JMenuItem jMenuItemJoin;
    private javax.swing.JMenuItem jMenuItemMakeCutPathI;
    private javax.swing.JMenuItem jMenuItemMakeCutPathO;
    private javax.swing.JMenuItem jMenuItemMakeFlatten;
    private javax.swing.JMenuItem jMenuItemMove;
    private javax.swing.JMenuItem jMenuItemMoveCenter;
    private javax.swing.JMenuItem jMenuItemMoveDown;
    private javax.swing.JMenuItem jMenuItemMoveUp;
    private javax.swing.JMenuItem jMenuItemMoveWithMouse;
    private javax.swing.JMenuItem jMenuItemNew;
    private javax.swing.JMenuItem jMenuItemOpenGCode;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemQuit;
    private javax.swing.JMenuItem jMenuItemRedo;
    private javax.swing.JMenuItem jMenuItemRemoveSelection;
    private javax.swing.JMenuItem jMenuItemRename;
    private javax.swing.JMenuItem jMenuItemReverse;
    private javax.swing.JMenuItem jMenuItemRotate;
    private javax.swing.JMenuItem jMenuItemRotateCenter;
    private javax.swing.JMenuItem jMenuItemRotateP;
    private javax.swing.JMenuItem jMenuItemSaveAs;
    private javax.swing.JMenuItem jMenuItemSaveGCode;
    private javax.swing.JMenuItem jMenuItemScale;
    private javax.swing.JMenuItem jMenuItemScaleCenter;
    private javax.swing.JMenuItem jMenuItemScaleP;
    private javax.swing.JMenuItem jMenuItemSelectAll;
    private javax.swing.JMenuItem jMenuItemSetAsFooter;
    private javax.swing.JMenuItem jMenuItemSetAsHeader;
    private javax.swing.JMenuItem jMenuItemSetCursor;
    private javax.swing.JMenuItem jMenuItemSetCursorTo;
    private javax.swing.JMenuItem jMenuItemSimplify;
    private javax.swing.JMenuItem jMenuItemSimplifyByDistance;
    private javax.swing.JMenuItem jMenuItemSimplifyP;
    private javax.swing.JMenuItem jMenuItemSort;
    private javax.swing.JMenuItem jMenuItemUndo;
    private javax.swing.JMenuItem jMenuItemUngroup;
    private javax.swing.JMenu jMenuMakeCutPath;
    private javax.swing.JMenu jMenuPoints;
    private javax.swing.JMenu jMenuRecent;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelEditedInfo;
    private javax.swing.JPanel jPanelEditor;
    private javax.swing.JPanel jPanelProperties;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JToolBar.Separator jSeparator21;
    private javax.swing.JToolBar.Separator jSeparator22;
    private javax.swing.JToolBar.Separator jSeparator23;
    private javax.swing.JPopupMenu.Separator jSeparator24;
    private javax.swing.JPopupMenu.Separator jSeparator25;
    private javax.swing.JPopupMenu.Separator jSeparator26;
    private javax.swing.JPopupMenu.Separator jSeparator27;
    private javax.swing.JPopupMenu.Separator jSeparator28;
    private javax.swing.JPopupMenu.Separator jSeparator29;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator30;
    private javax.swing.JPopupMenu.Separator jSeparator31;
    private javax.swing.JPopupMenu.Separator jSeparator32;
    private javax.swing.JPopupMenu.Separator jSeparator33;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JTextField jTextFieldEditedBlock;
    private javax.swing.JTextField jTextFieldFeed;
    private javax.swing.JTextField jTextFieldPass;
    private javax.swing.JTextField jTextFieldPassDepht;
    private javax.swing.JTextField jTextFieldPower;
    private javax.swing.JTextField jTextFieldZEnd;
    private javax.swing.JTextField jTextFieldZStart;
    private javax.swing.JToggleButton jToggleButtonAddCircles;
    private javax.swing.JToggleButton jToggleButtonAddLines;
    private javax.swing.JToggleButton jToggleButtonAddRects;
    private javax.swing.JToggleButton jToggleButtonHold;
    private javax.swing.JToggleButton jToggleButtonMoveHead;
    private javax.swing.JToggleButton jToggleButtonShowAngle;
    private javax.swing.JToggleButton jToggleButtonShowDistance;
    private javax.swing.JToggleButton jToggleButtonShowLaser;
    private javax.swing.JToggleButton jToggleButtonZoom;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
