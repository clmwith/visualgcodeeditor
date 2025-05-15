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

import gcodeeditor.Configuration;
import gcodeeditor.GCodeDocumentRender;
import gelements.GGroup;
import gcodeeditor.GRBLControler;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A frame to execute the document to GRBL and/or into a file.
 * @author Clément
 */
public class JRunningPanel extends javax.swing.JPanel implements GCodeDocumentRender.RenderListener {

    GRBLControler grbl;
    Thread sender = null;
    Window parent;
    Configuration conf;
    private GCodeDocumentRender gcodeRunner;
    private GGroup elements;
    private File lastOutputDir;
    
    public JRunningPanel( Window parent, GRBLControler grbl, Configuration conf) {
        this.conf = conf;
        this.parent = parent;
        this.grbl = grbl;
        initComponents();
        gcodeRunner = new GCodeDocumentRender(conf, this);
        
        grbl.addListenner(new GRBLControler.GRBLCommListennerInterface() {
            @Override
            public void wPosChanged() { 
                updateGUI();
            }
            @Override
            public void stateChanged() { }
            @Override
            public void receivedError(int errorNo, String line) {
                if ( isPrinting()) {
                    stopPrint();
                    EventQueue.invokeLater(() -> { 
                        JOptionPane.showMessageDialog(parent, "GRBL Error " + errorNo + " :\n" + GRBLControler.getErrorMsg(errorNo) + "\nFor line ["+line+"]\n\nJob arborted.", "Error encountred", JOptionPane.ERROR_MESSAGE);     
                    });
                }
            }
            @Override
            public void receivedAlarm(int alarmno) {
                if ( isPrinting()) {
                    stopPrint();
                    EventQueue.invokeLater(() -> { 
                        JOptionPane.showMessageDialog(parent, "Alarm received from GRBL, job arborted.", "ALAMR received", JOptionPane.ERROR_MESSAGE);     
                    });
                }
            }
            @Override
            public void accessoryStateChanged() { 
                jToggleButtonCoolant.setEnabled(grbl.isCoolantEnabled());
                jToggleButtonMist.setEnabled(grbl.isMistEnabled());
            }
            @Override
            public void feedSpindleChanged() { updateFeedSpindleValues(); }
            @Override
            public void sendedLine(String cmd) { }
            @Override
            public void receivedLine(String l) { }
            @Override
            public void overrideChanged() { updateOvValues(); }
            @Override
            public void receivedMessage(String substring) { }
            @Override
            public void exceptionInGRBLComThread(Exception ex) {
                gcodeRunner.stop();
            }
            @Override
            public void settingsReady() { }
            @Override
            public void probFinished(String substring) { }

            @Override
            public void limitSwitchChanged() { }
        });    
        updateOvValues();
        updateFeedSpindleValues();
    }
 
    public void setGroupToPrint(GGroup group) {
        elements = group;
        gcodeRunner.setDocumentToPrint(group, grbl);
        if ( grbl.isConnected() && jTextFieldOutputToFile.getText().isEmpty())
            jCheckBoxSaveToFile.setSelected( false );
        
        updateGUI();
    }
    
    private void updateGUI() {
        if ( ! grbl.isConnected() || jCheckBoxSaveToFile.isSelected()) {
            jButtonStartSave.setText("Save");           
        } else { 
            jButtonStartSave.setText("Start");
        }
        
        jTextFieldOutputToFile.setEnabled( sender==null);
        jButtonChooseOutputFile.setEnabled( sender==null);
        jButtonStartSave.setEnabled((sender==null) && (! grbl.isConnected() || (grbl.isConnected() && grbl.getState()==GRBLControler.GRBL_STATE_IDLE)));
        jButtonKillAlarm.setEnabled(grbl.getState()==GRBLControler.GRBL_STATE_ALARM );
        
        jButtonHome.setEnabled( grbl.canHome());
        jButtonStop.setText((sender==null) ? "Close" : "STOP");
        
        boolean fixedProps = jCheckBoxOverridePropsEnabled.isSelected();
        jPanelCut.setEnabled((sender==null) && fixedProps);
        jLabelEndZ.setEnabled(jCheckBoxCutDepth.isSelected());
        jLabelStartZ.setEnabled(jCheckBoxCutDepth.isSelected());
        jLabelFeed.setEnabled(fixedProps && (sender==null));
        jLabelPower.setEnabled(fixedProps && (sender==null));
        jLabelPassCount.setEnabled(fixedProps && (sender==null));
        
        jTextFieldPassCount.setEnabled((sender==null) && fixedProps);
        jTextFieldPassDepth.setEnabled((sender==null) && fixedProps);
        jCheckBoxCutDepth.setEnabled((sender==null) && fixedProps);
        jTextFieldZSart.setEnabled((sender==null) && fixedProps);
        jTextFieldZEnd.setEnabled((sender==null) && fixedProps);
        jTextFieldPower.setEnabled((sender==null) && fixedProps);
        jTextFieldFeed.setEnabled((sender==null) && fixedProps);
        jTextFieldPassDepth.setEditable(jCheckBoxCutDepth.isSelected());
        jTextFieldZSart.setEditable(jCheckBoxCutDepth.isSelected());
        jTextFieldZEnd.setEditable(jCheckBoxCutDepth.isSelected());
        jToggleButonHold.setSelected(grbl.getState() == GRBLControler.GRBL_STATE_HOLD);
        jToggleButonHold.setEnabled( grbl.isConnected());
        jToggleButtonCoolant.setEnabled( grbl.isConnected());
        jToggleButtonCoolant.setSelected(grbl.isCoolantEnabled());
  
        jToggleButtonMist.setEnabled(grbl.getOPT().contains("M"));
        jLabelGRBLState.setText( grbl.getStateStr());
        jTextFieldFeed.setText(Double.toString(conf.feedRate));
        jTextFieldPower.setText(Integer.toString(conf.spindleLaserPower));
                jCheckBoxSaveToFile.setEnabled( sender==null);
        
        if ( grbl.getWPos() != null) jLabelCurrentZheight.setText(""+grbl.getWPos().getZ());
        updateFeedSpindleValues();
        updateOvValues();
    }
 

    private void updateOvValues() {
        int[] ov = grbl.getOverrideValues();
        jTextFieldOvFeed.setText("" + ov[0]+ " %");
        jTextFieldOvSpeed.setText("" + ov[1]+ " %");
        jTextFieldOvSpindle.setText("" + ov[2]+ " %");
    }
    
    /*private EngravingProperties getFixedEngravingPropreties() {
        EngravingProperties res = null;
        if ( jCheckBoxUseFixedProps.isSelected()) {
            res = new EngravingProperties();
            try { res.passDepth = Double.valueOf(jTextFieldPassDepth.getText()); } catch ( NumberFormatException e) { } 
            try { res.zStart = Double.valueOf(jTextFieldZSart.getText()); } catch ( NumberFormatException e) { } 
            try { res.zEnd = Double.valueOf(jTextFieldZEnd.getText()); } catch ( NumberFormatException e) { } 
            try { res.passCount = Integer.valueOf(jTextFieldPassCount.getText()); } catch ( NumberFormatException e) { } 
            try { res.feed = Double.valueOf(jTextFieldFeed.getText()); } catch ( NumberFormatException e) { } 
        }
        return res;
    }*/
    private void updateFeedSpindleValues() {
        jLabelCurrentFeedRate.setText( "" + grbl.getFeedRate());
        jLabelCurrentLaserPower.setText( "" + grbl.getSpindleSpeed());
    }
    
   
    /**
     * Used by GCodeDocumentRender to actualise GUI about his job (path and zlevel, etc...).
     * @param state 
     */
    @Override
    public void updateGUI(GCodeDocumentRender.ExecutionState state) {
        jLabelGRBLState.setText( grbl.getStateStr());
        jLabelGRBLState.setForeground(grbl.getState() == GRBLControler.GRBL_STATE_ALARM ? Color.red : Color.black );   
        
        jLabelCurrentGroup.setText(state.currentGroupName);
        jLabelCurrentBlock.setText(state.currentElementName);
                
        jLabelCurrentPass.setText( state.currentPass + "/" + state.currentPassCount);
                        
        if ( Double.isNaN(state.currentZ)) jLabelCurrentZheight.setText("<not used>");
        else jLabelCurrentZheight.setText(String.format(Locale.ROOT, "%.3f into [%.3f , %.3f] step %.3f",
                    state.currentZ, state.currentZStart, state.currentZEnd, state.currentZDepth));
        
    }
    
    @Override
    public void error(String error) {
        grbl.hold();
        if ( JOptionPane.showConfirmDialog(parent, "Parse error at execution:\n"+error+"\n\nAbort job ?") == JOptionPane.YES_OPTION)
            jButtonStopActionPerformed(null);
        else
            grbl.cycleStartResume();
    }

    @Override
    public void executionFinished() {
        sender = null;
        System.out.println("Execution finished.");
        if ( ! grbl.isConnected() || jCheckBoxSaveToFile.isSelected()) {
            JOptionPane.showMessageDialog(parent, "Job saved.");
            parent.setVisible(false);
        }    
        updateGUI();
    }
    
    public void stopPrint() {
        if ( grbl.isConnected()) grbl.hold();
        gcodeRunner.stop();
        if ( grbl.isConnected()) grbl.holdAndReset();     
        
        int i = 0;
        while ( (sender != null) )
            try { 
                Thread.sleep(1000); 
                if ( i++ == 10 ) {
                    System.out.println("Warning stopPrint(): document sender thread not finished after 10s");
                    break;
                }
            } catch ( InterruptedException e) { }
        
        grbl.holdAndReset();
        gcodeRunner.stop();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel9 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanelCut = new javax.swing.JPanel();
        jLabelPassCount = new javax.swing.JLabel();
        jTextFieldPassCount = new javax.swing.JTextField();
        jCheckBoxCutDepth = new javax.swing.JCheckBox();
        jTextFieldPassDepth = new javax.swing.JTextField();
        jLabelStartZ = new javax.swing.JLabel();
        jTextFieldZSart = new javax.swing.JTextField();
        jLabelEndZ = new javax.swing.JLabel();
        jTextFieldZEnd = new javax.swing.JTextField();
        jLabelFeed = new javax.swing.JLabel();
        jTextFieldFeed = new javax.swing.JTextField();
        jLabelPower = new javax.swing.JLabel();
        jTextFieldPower = new javax.swing.JTextField();
        jCheckBoxOverridePropsEnabled = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jButtonStartSave = new javax.swing.JButton();
        jToggleButonHold = new javax.swing.JToggleButton();
        jToggleButtonCoolant = new javax.swing.JToggleButton();
        jButtonHome = new javax.swing.JButton();
        jToggleButtonMist = new javax.swing.JToggleButton();
        jButtonKillAlarm = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jCheckBoxSaveToFile = new javax.swing.JCheckBox();
        jTextFieldOutputToFile = new javax.swing.JTextField();
        jButtonChooseOutputFile = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabelBlock = new javax.swing.JLabel();
        jLabelCurrentBlock = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelPass = new javax.swing.JLabel();
        jLabelCurrentPass = new javax.swing.JLabel();
        jLabel1ZHeight = new javax.swing.JLabel();
        jLabelCurrentZheight = new javax.swing.JLabel();
        jLabelGRBLState = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelSpindlePowerText = new javax.swing.JLabel();
        jLabelCurrentFeedRate = new javax.swing.JLabel();
        jLabelCurrentLaserPower = new javax.swing.JLabel();
        jLabelGroup = new javax.swing.JLabel();
        jLabelCurrentGroup = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jButtonSpeed25 = new javax.swing.JButton();
        jButtonSpeed50 = new javax.swing.JButton();
        jButtonSpeed100 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButtonFM1 = new javax.swing.JButton();
        jButtonFM10 = new javax.swing.JButton();
        F100 = new javax.swing.JButton();
        jButtonFP1 = new javax.swing.JButton();
        jButtonFP10 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButtonPM10 = new javax.swing.JButton();
        jButtonPP10 = new javax.swing.JButton();
        jButtonPM1 = new javax.swing.JButton();
        jButtonP100 = new javax.swing.JButton();
        jButtonPP1 = new javax.swing.JButton();
        jTextFieldOvSpeed = new javax.swing.JTextField();
        jTextFieldOvFeed = new javax.swing.JTextField();
        jTextFieldOvSpindle = new javax.swing.JTextField();
        jButtonStop = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel9.setLayout(new java.awt.GridBagLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanelCut.setBorder(javax.swing.BorderFactory.createTitledBorder("Override values"));
        jPanelCut.setToolTipText("Engraving values used for All path");
        jPanelCut.setLayout(new java.awt.GridLayout(0, 2));

        jLabelPassCount.setText("Number of pass");
        jPanelCut.add(jLabelPassCount);

        jTextFieldPassCount.setColumns(6);
        jTextFieldPassCount.setToolTipText("Number of time the execution will be procedded");
        jTextFieldPassCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPassCountActionPerformed(evt);
            }
        });
        jPanelCut.add(jTextFieldPassCount);

        jCheckBoxCutDepth.setText("Pass depth");
        jCheckBoxCutDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxCutDepthActionPerformed(evt);
            }
        });
        jPanelCut.add(jCheckBoxCutDepth);

        jTextFieldPassDepth.setColumns(6);
        jTextFieldPassDepth.setToolTipText("The depth cut at each pass");
        jTextFieldPassDepth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPassDepthActionPerformed(evt);
            }
        });
        jPanelCut.add(jTextFieldPassDepth);

        jLabelStartZ.setText("Start at Z");
        jPanelCut.add(jLabelStartZ);

        jTextFieldZSart.setEditable(false);
        jTextFieldZSart.setColumns(6);
        jTextFieldZSart.setToolTipText("<html>If set perform a GOZ<i>n</i> before sending each block<br>And a G0Z<i>safeZmoveHeight</i> at each ends, else use default Configuration values</html>");
        jTextFieldZSart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldZSartActionPerformed(evt);
            }
        });
        jPanelCut.add(jTextFieldZSart);

        jLabelEndZ.setText("End at Z");
        jPanelCut.add(jLabelEndZ);

        jTextFieldZEnd.setEditable(false);
        jTextFieldZEnd.setColumns(6);
        jTextFieldZEnd.setToolTipText("The depth of the last pass");
        jTextFieldZEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldZEndActionPerformed(evt);
            }
        });
        jPanelCut.add(jTextFieldZEnd);

        jLabelFeed.setText("Feed");
        jPanelCut.add(jLabelFeed);

        jTextFieldFeed.setColumns(6);
        jTextFieldFeed.setToolTipText("<html>If set, add a  <b>G1F<i>&lt;feedSpeed&gt;<i></b> at start of this job.");
        jPanelCut.add(jTextFieldFeed);

        jLabelPower.setText("Power (spindle)");
        jPanelCut.add(jLabelPower);

        jTextFieldPower.setColumns(6);
        jTextFieldPower.setToolTipText("<html>if set, add a <b>M[3/4] S&lt;power&gt;</b> at the beginning of this job.");
        jPanelCut.add(jTextFieldPower);

        jPanel3.add(jPanelCut, java.awt.BorderLayout.CENTER);

        jCheckBoxOverridePropsEnabled.setText("Use overriden values for all paths");
        jCheckBoxOverridePropsEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxOverridePropsEnabledActionPerformed(evt);
            }
        });
        jPanel3.add(jCheckBoxOverridePropsEnabled, java.awt.BorderLayout.PAGE_START);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(0, 2, 5, 5));

        jButtonStartSave.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jButtonStartSave.setText("Save");
        jButtonStartSave.setToolTipText("Start the transfer of the job");
        jButtonStartSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartSaveActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonStartSave);

        jToggleButonHold.setText("HOLD");
        jToggleButonHold.setToolTipText("Put GRBL in HOLD machine state");
        jToggleButonHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButonHoldActionPerformed(evt);
            }
        });
        jPanel2.add(jToggleButonHold);

        jToggleButtonCoolant.setText("Coolant");
        jToggleButtonCoolant.setToolTipText("Toogle Coolant M7");
        jToggleButtonCoolant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonCoolantActionPerformed(evt);
            }
        });
        jPanel2.add(jToggleButtonCoolant);

        jButtonHome.setText("Home");
        jButtonHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHomeActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonHome);

        jToggleButtonMist.setText("Mist");
        jToggleButtonMist.setToolTipText("Toogle Mist M8");
        jToggleButtonMist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonMistActionPerformed(evt);
            }
        });
        jPanel2.add(jToggleButtonMist);

        jButtonKillAlarm.setText("Kill Alarm");
        jButtonKillAlarm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKillAlarmActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonKillAlarm);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel8.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(jPanel8, gridBagConstraints);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jCheckBoxSaveToFile.setText("Save to file");
        jCheckBoxSaveToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSaveToFileActionPerformed(evt);
            }
        });
        jPanel5.add(jCheckBoxSaveToFile, new java.awt.GridBagConstraints());

        jTextFieldOutputToFile.setColumns(20);
        jTextFieldOutputToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldOutputToFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanel5.add(jTextFieldOutputToFile, gridBagConstraints);

        jButtonChooseOutputFile.setText("Choose");
        jButtonChooseOutputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseOutputFileActionPerformed(evt);
            }
        });
        jPanel5.add(jButtonChooseOutputFile, new java.awt.GridBagConstraints());

        jPanel7.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabelBlock.setText("Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabelBlock, gridBagConstraints);

        jLabelCurrentBlock.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentBlock, gridBagConstraints);

        jLabel3.setText("GRBL State");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabel3, gridBagConstraints);

        jLabelPass.setText("Pass");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabelPass, gridBagConstraints);

        jLabelCurrentPass.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentPass, gridBagConstraints);

        jLabel1ZHeight.setText("Z height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabel1ZHeight, gridBagConstraints);

        jLabelCurrentZheight.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentZheight, gridBagConstraints);

        jLabelGRBLState.setText("<unknow>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelGRBLState, gridBagConstraints);

        jLabel4.setText("Feed rate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabel4, gridBagConstraints);

        jLabelSpindlePowerText.setText("Spindle / Power");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabelSpindlePowerText, gridBagConstraints);

        jLabelCurrentFeedRate.setText("<unknow>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentFeedRate, gridBagConstraints);

        jLabelCurrentLaserPower.setText("<unknow>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentLaserPower, gridBagConstraints);

        jLabelGroup.setText("Group");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel4.add(jLabelGroup, gridBagConstraints);

        jLabelCurrentGroup.setText("-");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel4.add(jLabelCurrentGroup, gridBagConstraints);

        jPanel7.add(jPanel4);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Override"));

        jLabel6.setText("Speed");

        jButtonSpeed25.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.setRapid25p();
            }
        });
        jButtonSpeed25.setText("25%");

        jButtonSpeed50.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.setRapid50p();
            }
        });
        jButtonSpeed50.setText("50%");

        jButtonSpeed100.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.setRapid100p();
            }
        });
        jButtonSpeed100.setText("100%");

        jLabel7.setText("Feed");

        jButtonFM1.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.feedDecrease1p();
            }
        });
        jButtonFM1.setText("-1");
        jButtonFM1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFM1ActionPerformed(evt);
            }
        });

        jButtonFM10.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.feedDecrease10p();
            }
        });
        jButtonFM10.setText("-10");

        F100.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.feed100Percent();
            }
        });
        F100.setText("100%");

        jButtonFP1.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.feedIncrease1p();
            }
        });
        jButtonFP1.setText("+1");

        jButtonFP10.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.feedIncrease10p();
            }
        });
        jButtonFP10.setText("+10");

        jLabel8.setText("Power (spindle)");

        jButtonPM10.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.spindleDecrease10p();
            }
        });
        jButtonPM10.setText("-10");

        jButtonPP10.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.spindleIncrease10p();
            }
        });
        jButtonPP10.setText("+10");

        jButtonPM1.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.spindleDecrease1p();
            }
        });
        jButtonPM1.setText("-1");

        jButtonP100.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.spindle100Percent();
            }
        });
        jButtonP100.setText("100%");

        jButtonPP1.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grbl.spindleIncrease1p();
            }
        });
        jButtonPP1.setText("+1");

        jTextFieldOvSpeed.setEditable(false);
        jTextFieldOvSpeed.setColumns(6);
        jTextFieldOvSpeed.setText("<unknow>");

        jTextFieldOvFeed.setEditable(false);
        jTextFieldOvFeed.setColumns(6);
        jTextFieldOvFeed.setText("<unknow>");

        jTextFieldOvSpindle.setEditable(false);
        jTextFieldOvSpindle.setColumns(6);
        jTextFieldOvSpindle.setText("<unknow>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldOvSpindle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldOvFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldOvSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonPM10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonFM10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSpeed25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonFM1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSpeed50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonPM1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSpeed100)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonP100)
                            .addComponent(F100))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonPP1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonFP1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonPP10)
                            .addComponent(jButtonFP10))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonSpeed50)
                            .addComponent(jButtonSpeed25)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(jTextFieldOvSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jButtonFM1)
                            .addComponent(jButtonFM10)
                            .addComponent(jTextFieldOvFeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jButtonPM1)
                            .addComponent(jButtonPM10)
                            .addComponent(jTextFieldOvSpindle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButtonSpeed100)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(F100)
                            .addComponent(jButtonFP1)
                            .addComponent(jButtonFP10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonP100)
                            .addComponent(jButtonPP1)
                            .addComponent(jButtonPP10))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel5.add(jPanel7, gridBagConstraints);

        jPanel6.add(jPanel5, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel9.add(jPanel6, gridBagConstraints);

        add(jPanel9, java.awt.BorderLayout.CENTER);

        jButtonStop.setFont(new java.awt.Font("DejaVu Sans", 1, 48)); // NOI18N
        jButtonStop.setForeground(new java.awt.Color(255, 58, 0));
        jButtonStop.setText("STOP");
        jButtonStop.setToolTipText("<html>Immediately stop GRBL and cancel the job.<i>(press SPACE key to select)</i></html>");
        jButtonStop.setPreferredSize(new java.awt.Dimension(156, 100));
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });
        add(jButtonStop, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings("SleepWhileInLoop")
    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStopActionPerformed
        stopPrint();
        parent.setVisible(false);
    }//GEN-LAST:event_jButtonStopActionPerformed

    private void jButtonStartSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartSaveActionPerformed
        if ( sender == null) {       
            if ( jCheckBoxSaveToFile.isSelected() && jTextFieldOutputToFile.getText().equals("")) 
                // choose output file
                jButtonChooseOutputFileActionPerformed(null);    
            
            if ( ! grbl.isConnected() && jTextFieldOutputToFile.getText().equals("")) return;
            
            final boolean saveToFile = jCheckBoxSaveToFile.isSelected();
            final boolean laserMode = (! saveToFile && grbl.isConnected()) ? grbl.isLaserMode():
                         JOptionPane.showConfirmDialog(parent, "<html>Use Laser mode ?<br><i>(without failsafe Z height moves before each GO)</i></html>",
                            "Which mode...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                            new javax.swing.ImageIcon(getClass().getResource("/icons/Laser.png"))) == JOptionPane.YES_OPTION;
                            
            try { 
                
                boolean canSaveToFile = (saveToFile && (jTextFieldOutputToFile.getText().length() != 0));
                                
                if ( ! canSaveToFile && ! grbl.isConnected()) return; // can't save to file nor send GCode.
                
                gcodeRunner.setParam( laserMode, canSaveToFile ? jTextFieldOutputToFile.getText() : null );                                
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Error openning file:\n"+ ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
                
            jButtonStartSave.setEnabled(false);
            sender = new Thread( gcodeRunner, "GCodeRunnerThread");
            sender.start();
        }                    
    }//GEN-LAST:event_jButtonStartSaveActionPerformed

    public boolean isPrinting() {
        return sender != null;
    }
    
    private void jToggleButtonCoolantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonCoolantActionPerformed
        if ( grbl.isConnected()) grbl.toggleFloodCoolant();
    }//GEN-LAST:event_jToggleButtonCoolantActionPerformed

    private void jToggleButtonMistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonMistActionPerformed
        if ( grbl.isConnected()) grbl.toggleMistAccessory();
    }//GEN-LAST:event_jToggleButtonMistActionPerformed

    private void jToggleButonHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButonHoldActionPerformed
        jToggleButonHold.setSelected(grbl.getState() != GRBLControler.GRBL_STATE_HOLD);
        if( grbl.getState() == GRBLControler.GRBL_STATE_HOLD) grbl.cycleStartResume();
        else grbl.hold();
    }//GEN-LAST:event_jToggleButonHoldActionPerformed

    private void jTextFieldPassCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPassCountActionPerformed
        try {
            int nbPass  = Integer.parseInt(jTextFieldPassCount.getText());
            double zStart = jCheckBoxCutDepth.isSelected() ? Double.parseDouble(jTextFieldZSart.getText()) : 0;
            double passDepth = Double.parseDouble(jTextFieldPassDepth.getText());
            if ( jCheckBoxCutDepth.isSelected())
                jTextFieldZEnd.setText( String.format(Locale.ROOT, "%.2f", zStart - passDepth*(nbPass-1)));    
            updateGUI();
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Wrong value :\n"+e.getLocalizedMessage(),  "Override parameters error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jTextFieldPassCountActionPerformed

    private void jTextFieldPassDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPassDepthActionPerformed
        updateNumberOfPass();
    }//GEN-LAST:event_jTextFieldPassDepthActionPerformed

    private void jTextFieldZSartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldZSartActionPerformed
        updateNumberOfPass();
    }//GEN-LAST:event_jTextFieldZSartActionPerformed

    private void jTextFieldZEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldZEndActionPerformed
        updateNumberOfPass();
    }//GEN-LAST:event_jTextFieldZEndActionPerformed

    private void updateNumberOfPass() {
        if ( jTextFieldZEnd.getText().isEmpty() || jTextFieldZSart.getText().isEmpty() || jTextFieldPassDepth.getText().isEmpty())
            return;
        
        try {
            double zStart = jCheckBoxCutDepth.isSelected() ? Double.parseDouble(jTextFieldZSart.getText()) : 0;
            double zEnd = Double.parseDouble(jTextFieldZEnd.getText());
            double passDepth = Double.parseDouble(jTextFieldPassDepth.getText());
            double passCount = (zStart - zEnd) / passDepth;
            jTextFieldPassCount.setText( Integer.toString(1 + (int)Math.ceil( passCount)));
            updateGUI();
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Wrong value :\n"+e.getMessage(),  "Override parameters error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void jButtonFM1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFM1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonFM1ActionPerformed

    private void jCheckBoxCutDepthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCutDepthActionPerformed
        if ( jTextFieldZEnd.getText().equals("")) jTextFieldPassCountActionPerformed(null);
        else updateGUI();
    }//GEN-LAST:event_jCheckBoxCutDepthActionPerformed

    private void jButtonKillAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKillAlarmActionPerformed
        if ( grbl.isConnected()) grbl.killAlarm();
    }//GEN-LAST:event_jButtonKillAlarmActionPerformed

    private void jButtonHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHomeActionPerformed
        if ( grbl.isConnected()) grbl.goHome();
    }//GEN-LAST:event_jButtonHomeActionPerformed

    private void jCheckBoxSaveToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSaveToFileActionPerformed
        updateGUI();
    }//GEN-LAST:event_jCheckBoxSaveToFileActionPerformed

    private void jButtonChooseOutputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseOutputFileActionPerformed
        JFileChooser f = new JFileChooser();
        f.setDialogTitle("Save G-CODE to file ...");
        f.setFileFilter(new FileNameExtensionFilter("G-Code", "gcode"));
        if ( lastOutputDir != null) f.setCurrentDirectory(lastOutputDir);
        int rVal = f.showOpenDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) {   
            jTextFieldOutputToFile.setText(f.getSelectedFile().toString());
            lastOutputDir = f.getSelectedFile().getParentFile();
            updateGUI();
        }
    }//GEN-LAST:event_jButtonChooseOutputFileActionPerformed

    private void jTextFieldOutputToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldOutputToFileActionPerformed
        String t = jTextFieldOutputToFile.getText();
        if ( ! t.isBlank()) {
            if ( t.endsWith(".gcode")) jTextFieldOutputToFile.setText(t+".gcode");
            jCheckBoxSaveToFile.setSelected(true);
        } else {
            jCheckBoxSaveToFile.setSelected(false);
        }        
        updateGUI();
    }//GEN-LAST:event_jTextFieldOutputToFileActionPerformed

    private void jCheckBoxOverridePropsEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxOverridePropsEnabledActionPerformed
        updateGUI();
    }//GEN-LAST:event_jCheckBoxOverridePropsEnabledActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton F100;
    private javax.swing.JButton jButtonChooseOutputFile;
    private javax.swing.JButton jButtonFM1;
    private javax.swing.JButton jButtonFM10;
    private javax.swing.JButton jButtonFP1;
    private javax.swing.JButton jButtonFP10;
    private javax.swing.JButton jButtonHome;
    private javax.swing.JButton jButtonKillAlarm;
    private javax.swing.JButton jButtonP100;
    private javax.swing.JButton jButtonPM1;
    private javax.swing.JButton jButtonPM10;
    private javax.swing.JButton jButtonPP1;
    private javax.swing.JButton jButtonPP10;
    private javax.swing.JButton jButtonSpeed100;
    private javax.swing.JButton jButtonSpeed25;
    private javax.swing.JButton jButtonSpeed50;
    private javax.swing.JButton jButtonStartSave;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JCheckBox jCheckBoxCutDepth;
    private javax.swing.JCheckBox jCheckBoxOverridePropsEnabled;
    private javax.swing.JCheckBox jCheckBoxSaveToFile;
    private javax.swing.JLabel jLabel1ZHeight;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelBlock;
    private javax.swing.JLabel jLabelCurrentBlock;
    private javax.swing.JLabel jLabelCurrentFeedRate;
    private javax.swing.JLabel jLabelCurrentGroup;
    private javax.swing.JLabel jLabelCurrentLaserPower;
    private javax.swing.JLabel jLabelCurrentPass;
    private javax.swing.JLabel jLabelCurrentZheight;
    private javax.swing.JLabel jLabelEndZ;
    private javax.swing.JLabel jLabelFeed;
    private javax.swing.JLabel jLabelGRBLState;
    private javax.swing.JLabel jLabelGroup;
    private javax.swing.JLabel jLabelPass;
    private javax.swing.JLabel jLabelPassCount;
    private javax.swing.JLabel jLabelPower;
    private javax.swing.JLabel jLabelSpindlePowerText;
    private javax.swing.JLabel jLabelStartZ;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelCut;
    private javax.swing.JTextField jTextFieldFeed;
    private javax.swing.JTextField jTextFieldOutputToFile;
    private javax.swing.JTextField jTextFieldOvFeed;
    private javax.swing.JTextField jTextFieldOvSpeed;
    private javax.swing.JTextField jTextFieldOvSpindle;
    private javax.swing.JTextField jTextFieldPassCount;
    private javax.swing.JTextField jTextFieldPassDepth;
    private javax.swing.JTextField jTextFieldPower;
    private javax.swing.JTextField jTextFieldZEnd;
    private javax.swing.JTextField jTextFieldZSart;
    private javax.swing.JToggleButton jToggleButonHold;
    private javax.swing.JToggleButton jToggleButtonCoolant;
    private javax.swing.JToggleButton jToggleButtonMist;
    // End of variables declaration//GEN-END:variables
}
