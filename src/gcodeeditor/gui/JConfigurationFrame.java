/*
 * Copyright (C) 2019 Clément GERARDIN @ Marseille.fr
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
package gcodeeditor.gui;

import gcodeeditor.Configuration;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Clément
 */
public class JConfigurationFrame extends javax.swing.JFrame {

    ComboBoxModel<String> comboModel;

    /**
     * @param confName
     * @return true if this configuration name was found
     */
    boolean getConfiguration(String confName) {
        if ( conf.restore(confName)) {            
            jComboBox1.getEditor().setItem(selected=confName);
            updateGUI();
            return true;
            
        }
        return false;
    }
    
    public interface JConfigurationChangeListener {
        public void configurationChanged();
    }
    protected JConfigurationChangeListener listenner = null;
    
    Configuration conf;
    DecimalFormat df = (DecimalFormat)DecimalFormat.getInstance(Locale.ROOT);
    String selected = Configuration.DEFAULT;
    ArrayList<ListDataListener> listeners = new ArrayList<>(); 
    
    /**
     * Creates new form JConfigurationFrame
     * @param appConf the application configuration
     * @param listener the listenner when something has changed
     */
    public JConfigurationFrame(Configuration appConf, JConfigurationChangeListener listener) {
        df.applyPattern("0.00###");
        listenner = listener;      
        conf = appConf;
        
        
        comboModel = new ComboBoxModel<String>() {
            ArrayList<ListDataListener> listeners = new ArrayList<>(); 
            @Override
            public void setSelectedItem(Object anItem) {
                if ( selected.equals(anItem)) return;
                //System.out.println("setSelectedItem("+anItem+")");
                selected = anItem.toString();
                if ( ! conf.getSavedNames().contains(anItem.toString())) {
                    updateConf();
                    conf.save(selected = anItem.toString());
                    listeners.forEach((l) -> {
                        l.contentsChanged(
                                new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
                    });
                } else {
                    if ( conf.restore(selected)) {
                        jComboBox1.setSelectedIndex(conf.getSavedNames().indexOf(selected)+1);
                        updateGUI();
                    }
                } 
            }
            @Override
            public Object getSelectedItem() { return selected; }
            @Override
            public int getSize() { return conf.getSavedNames().size()+1; }
            @Override
            public String getElementAt(int index) {
                if ( index == 0) return Configuration.DEFAULT;
                return conf.getSavedNames().get(index-1);
            }
            @Override
            public void addListDataListener(ListDataListener l) { listeners.add(l); }
            @Override
            public void removeListDataListener(ListDataListener l) { listeners.remove(l); }
        };
        initComponents();
        updateGUI();
        conf.getSavedNames();
    }
    
    public void updateButtons() {
        jRadioButtonUL.setSelected(conf.workspaceOrigin==0);
        jRadioButtonUR.setSelected(conf.workspaceOrigin==1);
        jRadioButtonDL.setSelected(conf.workspaceOrigin==2);
        jRadioButtonDR.setSelected(conf.workspaceOrigin==3);
        jRadioButton1.setSelected(conf.axeAIs==0);
        jRadioButton2.setSelected(conf.axeAIs==1);
        jRadioButton3.setSelected(conf.axeAIs==2);
        jRadioButton4.setSelected(conf.axeAIs==3);        
    }
    
    public final void updateGUI() {
        updateButtons();
        if ( conf.configurationFileName != null) setTitle("Configuration - " + conf.configurationFileName);
        jTextFieldWidth.setText(Integer.toString(conf.workspaceWidth));
        jTextFieldHeight.setText(Integer.toString(conf.workspaceHeight));
        jTextFieldFeedR.setText(Double.toString(conf.feedRate));
        jTextFieldLaserP.setText(Integer.toString(conf.spindleLaserPower));
        jTextFieldPulse.setText(Double.toString(conf.pulseByUnit));
        jTextFieldDiameter.setText(Double.toString(conf.objectDiameter));
        jTextFieldUnitFor1Turn.setText(Double.toString(conf.unitForATurn));
        jTextArea1.setText(conf.GCODEHeader);
        jTextArea2.setText(conf.GCODEFooter);
        jTextFieldPerimeter.setText(df.format(Math.PI * (conf.objectDiameter)));
        jTextFieldMoveZ.setText(df.format(conf.safeZHeightForMoving));
        jTextFieldToolDiameter.setText(df.format(conf.toolDiameter));
        jCheckBoxAdaptativePower.setSelected(conf.adaptativePower);
        jTextFieldJogSpeed.setText(Integer.toString(conf.jogSpeed));
        jCheckBoxBackLash.setSelected(conf.useBackLash);
        jTextFieldBSX.setText(df.format(conf.backLashX));
        jTextFieldBSY.setText(df.format(conf.backLashY));
        jTextFieldBSZ.setText(df.format(conf.backLashZ));
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

        jPanelHomePos = new javax.swing.JPanel();
        jRadioButtonUL = new javax.swing.JRadioButton();
        jRadioButtonUR = new javax.swing.JRadioButton();
        jRadioButtonDL = new javax.swing.JRadioButton();
        jRadioButtonDR = new javax.swing.JRadioButton();
        jPanelWorkspace = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldWidth = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldHeight = new javax.swing.JTextField();
        jPanelDefVal = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldFeedR = new javax.swing.JTextField();
        jTextFieldLaserP = new javax.swing.JTextField();
        jTextFieldMoveZ = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldToolDiameter = new javax.swing.JTextField();
        jCheckBoxAdaptativePower = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldJogSpeed = new javax.swing.JTextField();
        jPanelRotation = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldPulse = new javax.swing.JTextField();
        jTextFieldDiameter = new javax.swing.JTextField();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldUnitFor1Turn = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldPerimeter = new javax.swing.JTextField();
        jTextFieldObjectLength = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jPanelBackLash = new javax.swing.JPanel();
        jCheckBoxBackLash = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldBSX = new javax.swing.JTextField();
        jTextFieldBSY = new javax.swing.JTextField();
        jTextFieldBSZ = new javax.swing.JTextField();
        jPanelOkCancel = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jButtonCancel = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jButtonOk = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jPanelConf = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jButtonDelete = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButtonUpdate = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jButtonSaveConf = new javax.swing.JButton();
        jButtonLoadConf = new javax.swing.JButton();
        jPanelHeadFoot = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setTitle("Configuration");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanelHomePos.setBorder(javax.swing.BorderFactory.createTitledBorder("Home position"));
        jPanelHomePos.setLayout(new java.awt.GridLayout(2, 2));

        jRadioButtonUL.setText("Up-Left");
        jRadioButtonUL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonULActionPerformed(evt);
            }
        });
        jPanelHomePos.add(jRadioButtonUL);

        jRadioButtonUR.setText("Up-Right");
        jRadioButtonUR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonURActionPerformed(evt);
            }
        });
        jPanelHomePos.add(jRadioButtonUR);

        jRadioButtonDL.setText("Down-Left");
        jRadioButtonDL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDLActionPerformed(evt);
            }
        });
        jPanelHomePos.add(jRadioButtonDL);

        jRadioButtonDR.setText("Down-Right");
        jRadioButtonDR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDRActionPerformed(evt);
            }
        });
        jPanelHomePos.add(jRadioButtonDR);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jPanelHomePos, gridBagConstraints);

        jPanelWorkspace.setBorder(javax.swing.BorderFactory.createTitledBorder("Workspace dimensions"));
        jPanelWorkspace.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Width");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanelWorkspace.add(jLabel1, gridBagConstraints);

        jTextFieldWidth.setColumns(10);
        jTextFieldWidth.setText("200.00");
        jTextFieldWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldWidthActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanelWorkspace.add(jTextFieldWidth, gridBagConstraints);

        jLabel2.setText("Height ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanelWorkspace.add(jLabel2, gridBagConstraints);

        jTextFieldHeight.setColumns(10);
        jTextFieldHeight.setText("200.00");
        jTextFieldHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldHeightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        jPanelWorkspace.add(jTextFieldHeight, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jPanelWorkspace, gridBagConstraints);

        jPanelDefVal.setBorder(javax.swing.BorderFactory.createTitledBorder("Default values"));
        jPanelDefVal.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Feed rate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Spindle/Laser power");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jLabel4, gridBagConstraints);

        jTextFieldFeedR.setColumns(8);
        jTextFieldFeedR.setText("100");
        jTextFieldFeedR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFeedRActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jTextFieldFeedR, gridBagConstraints);

        jTextFieldLaserP.setColumns(8);
        jTextFieldLaserP.setText("1000");
        jTextFieldLaserP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLaserPActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jTextFieldLaserP, gridBagConstraints);

        jTextFieldMoveZ.setColumns(8);
        jTextFieldMoveZ.setText("10");
        jTextFieldMoveZ.setToolTipText("The Z position when moving");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jTextFieldMoveZ, gridBagConstraints);

        jLabel11.setText("Move at Z Level");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jLabel11, gridBagConstraints);

        jLabel12.setText("Tool diameter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jLabel12, gridBagConstraints);

        jTextFieldToolDiameter.setColumns(8);
        jTextFieldToolDiameter.setText("0");
        jTextFieldToolDiameter.setToolTipText("The working Z level");
        jTextFieldToolDiameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldToolDiameterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jTextFieldToolDiameter, gridBagConstraints);

        jCheckBoxAdaptativePower.setText("Adaptative power (M3/M4)");
        jCheckBoxAdaptativePower.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAdaptativePowerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelDefVal.add(jCheckBoxAdaptativePower, gridBagConstraints);

        jLabel13.setText("JOG Speed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelDefVal.add(jLabel13, gridBagConstraints);

        jTextFieldJogSpeed.setText("5000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 88;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelDefVal.add(jTextFieldJogSpeed, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(jPanelDefVal, gridBagConstraints);

        jPanelRotation.setBorder(javax.swing.BorderFactory.createTitledBorder("Rotation Axe"));
        jPanelRotation.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("Object diameter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Pulse / unit");
        jLabel6.setToolTipText("<html>Pulse by unit (to run one mm on the perimeter)<br>\nYou have to calculate it from the real pulse/unit of the choosed axe</A>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jLabel6, gridBagConstraints);

        jTextFieldPulse.setText("250.0");
        jTextFieldPulse.setToolTipText("<html>Number of <i>Driver step</i> to move 1 unit (mm/inch)<br>on axe choosed to be the rotation axe.</html>");
        jTextFieldPulse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPulseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 92;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jTextFieldPulse, gridBagConstraints);

        jTextFieldDiameter.setText("10.0");
        jTextFieldDiameter.setToolTipText("Real radius of the object to work");
        jTextFieldDiameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDiameterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 92;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jTextFieldDiameter, gridBagConstraints);

        jRadioButton1.setText("X");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelRotation.add(jRadioButton1, gridBagConstraints);

        jRadioButton2.setText("Y");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelRotation.add(jRadioButton2, gridBagConstraints);

        jRadioButton3.setText("Z");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelRotation.add(jRadioButton3, gridBagConstraints);

        jRadioButton4.setText("A");
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelRotation.add(jRadioButton4, gridBagConstraints);

        jLabel7.setText("Use axe");
        jLabel7.setToolTipText("Choose the axe GRBL will use as A axe");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanelRotation.add(jLabel7, gridBagConstraints);

        jLabel8.setText("Unit / turn");
        jLabel8.setToolTipText("Set how many unit (mm on the perimeter) to make a complet turn");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jLabel8, gridBagConstraints);

        jTextFieldUnitFor1Turn.setText("100");
        jTextFieldUnitFor1Turn.setToolTipText("<html>Number of (mm/inch) to move rotation Axe to accomplish 1 turn</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 92;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jTextFieldUnitFor1Turn, gridBagConstraints);

        jLabel9.setText("Perimeter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jLabel9, gridBagConstraints);

        jTextFieldPerimeter.setEditable(false);
        jTextFieldPerimeter.setColumns(8);
        jTextFieldPerimeter.setText("0");
        jTextFieldPerimeter.setToolTipText("The calculed perimeter of the object");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 92;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jTextFieldPerimeter, gridBagConstraints);

        jTextFieldObjectLength.setText("100.0");
        jTextFieldObjectLength.setToolTipText("The length of the rotating objext on rotation axe.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 92;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jTextFieldObjectLength, gridBagConstraints);

        jLabel10.setText("Object length");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanelRotation.add(jLabel10, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jPanelRotation, gridBagConstraints);

        jPanelBackLash.setBorder(javax.swing.BorderFactory.createTitledBorder("BackLash"));
        jPanelBackLash.setLayout(new java.awt.GridBagLayout());

        jCheckBoxBackLash.setText("Enable BackLash compensations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelBackLash.add(jCheckBoxBackLash, gridBagConstraints);

        jLabel14.setText("X value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelBackLash.add(jLabel14, gridBagConstraints);

        jLabel15.setText("Y value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanelBackLash.add(jLabel15, gridBagConstraints);

        jLabel16.setText("Z value");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanelBackLash.add(jLabel16, gridBagConstraints);

        jTextFieldBSX.setColumns(8);
        jTextFieldBSX.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelBackLash.add(jTextFieldBSX, gridBagConstraints);

        jTextFieldBSY.setColumns(8);
        jTextFieldBSY.setText("jTextField2");
        jTextFieldBSY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldBSYActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelBackLash.add(jTextFieldBSY, gridBagConstraints);

        jTextFieldBSZ.setColumns(8);
        jTextFieldBSZ.setText("jTextField3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelBackLash.add(jTextFieldBSZ, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(jPanelBackLash, gridBagConstraints);

        jLabel19.setText("    ");
        jPanelOkCancel.add(jLabel19);

        jButtonCancel.setText("Cancel");
        jButtonCancel.setToolTipText("Cancel changes");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelOkCancel.add(jButtonCancel);

        jLabel21.setText("  ");
        jPanelOkCancel.add(jLabel21);

        jButtonOk.setText("Apply");
        jButtonOk.setToolTipText("Use this configuration");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanelOkCancel.add(jButtonOk);

        jLabel20.setText("    ");
        jPanelOkCancel.add(jLabel20);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jPanelOkCancel, gridBagConstraints);

        jLabel17.setText("Configuration");
        jPanelConf.add(jLabel17);

        jButtonDelete.setText("Delete");
        jButtonDelete.setToolTipText("Delete this configuration");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        jPanelConf.add(jButtonDelete);

        jComboBox1.setEditable(true);
        jComboBox1.setModel(comboModel);
        jComboBox1.setToolTipText("Use same name as third GRBL $I value for auto-loading");
        jPanelConf.add(jComboBox1);

        jButtonUpdate.setText("Update");
        jButtonUpdate.setToolTipText("Update this configuration with current values");
        jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateActionPerformed(evt);
            }
        });
        jPanelConf.add(jButtonUpdate);

        jLabel18.setText("        ");
        jPanelConf.add(jLabel18);

        jButtonSaveConf.setText("Save");
        jButtonSaveConf.setToolTipText("Save a configuration to file");
        jButtonSaveConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveConfActionPerformed(evt);
            }
        });
        jPanelConf.add(jButtonSaveConf);

        jButtonLoadConf.setText("Load");
        jButtonLoadConf.setToolTipText("Load a configuration from file");
        jButtonLoadConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadConfActionPerformed(evt);
            }
        });
        jPanelConf.add(jButtonLoadConf);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jPanelConf, gridBagConstraints);

        jPanelHeadFoot.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanelHeadFoot.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("GCode header"));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jTextArea1.setColumns(15);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("GCode footer"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jTextArea2.setColumns(15);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel2);

        jPanelHeadFoot.add(jPanel1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanelHeadFoot, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        conf.axeAIs=0;
        updateButtons();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        conf.axeAIs=1; updateButtons();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jButtonSaveConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveConfActionPerformed
        updateConf();
        JFileChooser f = new JFileChooser();
        if ( conf.configurationFileName != null)
            f.setSelectedFile(new File(conf.configurationFileName));
        int rVal = f.showSaveDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) {
            conf.configurationFileName = f.getSelectedFile().getAbsolutePath();
            if ( conf.configurationFileName != null) {
                updateConf();
                conf.save(conf.configurationFileName);
            }
        }  
    }//GEN-LAST:event_jButtonSaveConfActionPerformed

    private void jButtonLoadConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadConfActionPerformed
        JFileChooser f = new JFileChooser();
        int rVal = f.showOpenDialog(this);
        if ( rVal == JFileChooser.APPROVE_OPTION) 
            try {
                conf.load(f.getSelectedFile().getAbsolutePath());
                updateGUI();
                if ( listenner != null) 
                    listenner.configurationChanged();
            } catch (IOException ex) {
                Logger.getLogger(JConfigurationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
    }//GEN-LAST:event_jButtonLoadConfActionPerformed

    protected void updateConf()
    {
        try {
            conf.workspaceWidth = Integer.decode(jTextFieldWidth.getText());
            conf.workspaceHeight = Integer.decode(jTextFieldHeight.getText());

            conf.unitForATurn = Double.parseDouble(jTextFieldUnitFor1Turn.getText());
            conf.pulseByUnit = Double.parseDouble(jTextFieldPulse.getText());
            conf.objectDiameter = Double.parseDouble(jTextFieldDiameter.getText());
            conf.objectLength = Double.parseDouble(jTextFieldObjectLength.getText());
            conf.GCODEHeader = jTextArea1.getText();
            conf.GCODEFooter = jTextArea2.getText();
            conf.adaptativePower = jCheckBoxAdaptativePower.isSelected();

            conf.feedRate = Double.parseDouble(jTextFieldFeedR.getText());
            conf.spindleLaserPower = Integer.decode(jTextFieldLaserP.getText());
            conf.safeZHeightForMoving = Double.parseDouble(jTextFieldMoveZ.getText());
            conf.engravingHeight = Double.parseDouble(jTextFieldToolDiameter.getText());
            conf.jogSpeed = Integer.parseInt(jTextFieldJogSpeed.getText());

            conf.useBackLash = jCheckBoxBackLash.isSelected();
            conf.backLashX = Double.parseDouble(jTextFieldBSX.getText());
            conf.backLashY = Double.parseDouble(jTextFieldBSY.getText());
            conf.backLashZ = Double.parseDouble(jTextFieldBSZ.getText());
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error saving configuration :\n"+e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        try {
            updateConf();
            if ( listenner != null) listenner.configurationChanged();
            setVisible(false);
        } catch ( Exception e) {
            JOptionPane.showMessageDialog(this, e + "\nVérifiez vos valeurs.", "Mise à jour valeur...", JOptionPane.ERROR_MESSAGE);
        }    
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jRadioButtonULActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonULActionPerformed
        conf.workspaceOrigin = 0; updateButtons();
    }//GEN-LAST:event_jRadioButtonULActionPerformed

    private void jRadioButtonURActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonURActionPerformed
        conf.workspaceOrigin = 1; updateButtons();
    }//GEN-LAST:event_jRadioButtonURActionPerformed

    private void jRadioButtonDLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDLActionPerformed
        conf.workspaceOrigin = 2; updateButtons();
    }//GEN-LAST:event_jRadioButtonDLActionPerformed

    private void jRadioButtonDRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDRActionPerformed
        conf.workspaceOrigin = 3; updateButtons();
    }//GEN-LAST:event_jRadioButtonDRActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        conf.axeAIs=2; updateButtons();
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        conf.axeAIs=3; updateButtons();
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jTextFieldWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldWidthActionPerformed
        conf.workspaceWidth = Integer.decode(jTextFieldWidth.getText());
    }//GEN-LAST:event_jTextFieldWidthActionPerformed

    private void jTextFieldHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldHeightActionPerformed
        conf.workspaceHeight = Integer.decode(jTextFieldHeight.getText());
    }//GEN-LAST:event_jTextFieldHeightActionPerformed

    private void jTextFieldFeedRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFeedRActionPerformed
        conf.feedRate = Integer.decode(jTextFieldFeedR.getText());
    }//GEN-LAST:event_jTextFieldFeedRActionPerformed

    private void jTextFieldLaserPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLaserPActionPerformed
        conf.spindleLaserPower = Integer.decode(jTextFieldLaserP.getText());
    }//GEN-LAST:event_jTextFieldLaserPActionPerformed

    private void jTextFieldPulseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPulseActionPerformed
        conf.pulseByUnit = Double.parseDouble(jTextFieldPulse.getText());
    }//GEN-LAST:event_jTextFieldPulseActionPerformed

    private void jTextFieldDiameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDiameterActionPerformed
        conf.objectDiameter = Double.parseDouble(jTextFieldDiameter.getText());
        jTextFieldPerimeter.setText(df.format(Math.PI * (conf.objectDiameter)));
    }//GEN-LAST:event_jTextFieldDiameterActionPerformed

    private void jCheckBoxAdaptativePowerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAdaptativePowerActionPerformed
        conf.adaptativePower = jCheckBoxAdaptativePower.isSelected();
    }//GEN-LAST:event_jCheckBoxAdaptativePowerActionPerformed

    private void jTextFieldBSYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldBSYActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldBSYActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if ( conf.getSavedNames().contains(selected)) {
            conf.delete(selected);
            conf.getDefault();
            comboModel.setSelectedItem(Configuration.DEFAULT);
            listeners.forEach((l) -> { l.contentsChanged(
                                new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 100));
                    });
            updateGUI();
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateActionPerformed
        updateConf();
        conf.save(jComboBox1.getSelectedItem().toString());
    }//GEN-LAST:event_jButtonUpdateActionPerformed

    private void jTextFieldToolDiameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldToolDiameterActionPerformed
        conf.toolDiameter = Double.parseDouble(jTextFieldToolDiameter.getText());
    }//GEN-LAST:event_jTextFieldToolDiameterActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JConfigurationFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new JConfigurationFrame(new Configuration(), () -> {
                System.out.println("conf changed !");
            }).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonLoadConf;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JButton jButtonSaveConf;
    private javax.swing.JButton jButtonUpdate;
    private javax.swing.JCheckBox jCheckBoxAdaptativePower;
    private javax.swing.JCheckBox jCheckBoxBackLash;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelBackLash;
    private javax.swing.JPanel jPanelConf;
    private javax.swing.JPanel jPanelDefVal;
    private javax.swing.JPanel jPanelHeadFoot;
    private javax.swing.JPanel jPanelHomePos;
    private javax.swing.JPanel jPanelOkCancel;
    private javax.swing.JPanel jPanelRotation;
    private javax.swing.JPanel jPanelWorkspace;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButtonDL;
    private javax.swing.JRadioButton jRadioButtonDR;
    private javax.swing.JRadioButton jRadioButtonUL;
    private javax.swing.JRadioButton jRadioButtonUR;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextFieldBSX;
    private javax.swing.JTextField jTextFieldBSY;
    private javax.swing.JTextField jTextFieldBSZ;
    private javax.swing.JTextField jTextFieldDiameter;
    private javax.swing.JTextField jTextFieldFeedR;
    private javax.swing.JTextField jTextFieldHeight;
    private javax.swing.JTextField jTextFieldJogSpeed;
    private javax.swing.JTextField jTextFieldLaserP;
    private javax.swing.JTextField jTextFieldMoveZ;
    private javax.swing.JTextField jTextFieldObjectLength;
    private javax.swing.JTextField jTextFieldPerimeter;
    private javax.swing.JTextField jTextFieldPulse;
    private javax.swing.JTextField jTextFieldToolDiameter;
    private javax.swing.JTextField jTextFieldUnitFor1Turn;
    private javax.swing.JTextField jTextFieldWidth;
    // End of variables declaration//GEN-END:variables
}