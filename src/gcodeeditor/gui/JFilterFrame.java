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
package gcodeeditor.gui;

import gelements.G1Path;
import gcodeeditor.GCode;
import gelements.GElement;
import gcodeeditor.GWord;
import java.awt.Frame;
import java.util.ArrayList;
import javax.swing.JDialog;

/**
 * A Frame to configure filter algorithm.
 * @author Clément
 */
public class JFilterFrame extends javax.swing.JPanel {
    
    JDialog frame;
    boolean applyFilter = false;
    /**
     * Creates new form jTest
     */
    public JFilterFrame() {
        initComponents();
        frame = new JDialog((Frame)null, "G-Code Filter", true);
        frame.getContentPane().add(this);
        frame.pack();
    }
    
     public boolean applyFilterOn( ArrayList<GElement> group) {
        frame.setVisible(true);
        if ( applyFilter) {
            for ( GElement o : group) {
                if ( o instanceof G1Path) {
                G1Path blockToFilter = (G1Path)o;
                ArrayList<GCode> lineToRemove = new ArrayList<>();

                for( GCode l : blockToFilter) {
                    boolean toRemove = false;
                    for ( int i = 0; i < l.size();) {
                        GWord w = l.get(i);
                        int oldSize = l.size();
                        
                        switch ( w.getLetter()) {
                            case ';':   if ( jCheckBoxComma.isSelected()) toRemove=true;
                                        break;
                            case '(':   if ( jCheckBoxPatenthesis.isSelected()) toRemove=true;
                                        break;
                            case 'Z':   if ( jCheckBoxZcoords.isSelected()) l.remove(w);
                                        break;
                            case 'F':   if ( jCheckBoxFeedG9x.isSelected()) l.remove(w);
                                        break;
                            case 'S':   if ( jCheckBoxM345.isSelected()) l.remove(w);
                                        break; 
                            case 'H':
                            case 'O': 
                            case 'N': 
                            case 'T':   if ( jCheckBoxToolCoice.isSelected()) l.remove(w); 
                                        break;
                            case 'M':   switch ( l.getM()) {
                                            case 3: case 4: case 5:
                                                if ( jCheckBoxCoolantM789.isSelected()) l.remove(w);
                                                break;
                                            case 7: case 8: case 9:
                                                if ( jCheckBoxSplines.isSelected()) l.remove(w);
                                                break;
                                            case 0: case 1: case 2: case 6: case 30: case 60:
                                                if ( ! jCheckBoxMStopping.isSelected()) break;
                                            default:
                                                l.remove(w);
                                                break;  
                                        }
                                        break;
                            case 'G':   int g = w.getIntValue();
                                        switch ( g/10) {
                                            case 0:
                                                if ( (g==4) && jCheckBoxTempo.isSelected()) l.remove(w);
                                                if ( (g==5) && jCheckBoxSplines.isSelected()) l.remove(w);
                                                break;
                                            case 1: case 2: case 3:
                                                if ( jCheckBoxSpaceG123.isSelected()) l.remove(w);
                                                break;
                                            case 4: case 7: case 8:
                                                if ( jCheckBoxToolLength.isSelected()) l.remove(w);
                                                break;
                                            case 5:
                                                if ( jCheckBoxG54x.isSelected()) l.remove(w);
                                                break;
                                            case 9:
                                                if ( jCheckBoxFeedG9x.isSelected()) l.remove(w);
                                                break;
                                            case 6:
                                                if ( jCheckBoxG6x.isSelected()) l.remove(w);
                                                break;
                                        }
                                        break;     
                        }
                        if ( l.isEmpty() || toRemove) {
                            lineToRemove.add(l);
                            break;
                        } else if ( oldSize == l.size()) i++;
                    }
                }
                // 2nd pass to remove G0 /G1 alone
                for( GCode l : blockToFilter) {
                    if ( (l.getG()==0) && ! l.isAPoint()) lineToRemove.add(l);
                    if ( (l.getG()==1) && ! l.isAPoint()) lineToRemove.add(l);
                }
                lineToRemove.forEach((l) -> {
                    blockToFilter.remove(l);
                    });
            }
            }
        }
        return applyFilter;
    }
     
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
            java.util.logging.Logger.getLogger(JFilterFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new JFilterFrame().applyFilterOn(null);
            //new JFilterFrame().applyFilterOn(null);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonRemove = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanelMotion = new javax.swing.JPanel();
        jCheckBoxSpaceG123 = new javax.swing.JCheckBox();
        jCheckBoxG54x = new javax.swing.JCheckBox();
        jCheckBoxFeedG9x = new javax.swing.JCheckBox();
        jCheckBoxG6x = new javax.swing.JCheckBox();
        jPanelModal = new javax.swing.JPanel();
        jCheckBoxMStopping = new javax.swing.JCheckBox();
        jCheckBoxM345 = new javax.swing.JCheckBox();
        jCheckBoxCoolantM789 = new javax.swing.JCheckBox();
        jCheckBoxAllOtherMCode = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxComma = new javax.swing.JCheckBox();
        jCheckBoxPatenthesis = new javax.swing.JCheckBox();
        jCheckBoxTempo = new javax.swing.JCheckBox();
        jCheckBoxSplines = new javax.swing.JCheckBox();
        jCheckBoxToolCoice = new javax.swing.JCheckBox();
        jCheckBoxToolLength = new javax.swing.JCheckBox();
        jCheckBoxZcoords = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jButtonRemove.setText("Remove");
        jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 96;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 12, 0);
        add(jButtonRemove, gridBagConstraints);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 46;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 102, 12, 0);
        add(jButtonCancel, gridBagConstraints);

        jLabel1.setText("Choose GCODE to remove :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(30, 12, 0, 0);
        add(jLabel1, gridBagConstraints);

        jPanelMotion.setBorder(javax.swing.BorderFactory.createTitledBorder("Motion"));
        jPanelMotion.setLayout(new javax.swing.BoxLayout(jPanelMotion, javax.swing.BoxLayout.PAGE_AXIS));

        jCheckBoxSpaceG123.setText("Space (G1x G2x G3x)");
        jPanelMotion.add(jCheckBoxSpaceG123);

        jCheckBoxG54x.setSelected(true);
        jCheckBoxG54x.setText("Workspace (G5x)");
        jPanelMotion.add(jCheckBoxG54x);

        jCheckBoxFeedG9x.setSelected(true);
        jCheckBoxFeedG9x.setText("Feed rate (Fx G9x)");
        jPanelMotion.add(jCheckBoxFeedG9x);

        jCheckBoxG6x.setSelected(true);
        jCheckBoxG6x.setText("Path control (G6x)");
        jPanelMotion.add(jCheckBoxG6x);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(jPanelMotion, gridBagConstraints);

        jPanelModal.setBorder(javax.swing.BorderFactory.createTitledBorder("Modal"));
        jPanelModal.setLayout(new javax.swing.BoxLayout(jPanelModal, javax.swing.BoxLayout.PAGE_AXIS));

        jCheckBoxMStopping.setSelected(true);
        jCheckBoxMStopping.setText("Stopping (M0-2,30,60)");
        jPanelModal.add(jCheckBoxMStopping);

        jCheckBoxM345.setSelected(true);
        jCheckBoxM345.setText("Spindle/Laser (M3-5 Sx)");
        jPanelModal.add(jCheckBoxM345);

        jCheckBoxCoolantM789.setSelected(true);
        jCheckBoxCoolantM789.setText("Coolant (M7,8,9)");
        jPanelModal.add(jCheckBoxCoolantM789);

        jCheckBoxAllOtherMCode.setSelected(true);
        jCheckBoxAllOtherMCode.setText("All the others");
        jPanelModal.add(jCheckBoxAllOtherMCode);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
        add(jPanelModal, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Misc"));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jCheckBoxComma.setSelected(true);
        jCheckBoxComma.setText("Comment ( comma )");
        jPanel1.add(jCheckBoxComma);

        jCheckBoxPatenthesis.setSelected(true);
        jCheckBoxPatenthesis.setText("Parenthesis");
        jPanel1.add(jCheckBoxPatenthesis);

        jCheckBoxTempo.setSelected(true);
        jCheckBoxTempo.setText("Tempo (G4)");
        jPanel1.add(jCheckBoxTempo);

        jCheckBoxSplines.setText("Splines (G5)");
        jPanel1.add(jCheckBoxSplines);

        jCheckBoxToolCoice.setSelected(true);
        jCheckBoxToolCoice.setText("Tool Choice (Ox Tx)");
        jPanel1.add(jCheckBoxToolCoice);

        jCheckBoxToolLength.setSelected(true);
        jCheckBoxToolLength.setText("Tool Length (G4x G7x G8x)");
        jPanel1.add(jCheckBoxToolLength);

        jCheckBoxZcoords.setText("Z coordinate");
        jPanel1.add(jCheckBoxZcoords);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 12, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        applyFilter = false;
        frame.setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveActionPerformed
        applyFilter = true;
        frame.setVisible(false);
    }//GEN-LAST:event_jButtonRemoveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JCheckBox jCheckBoxAllOtherMCode;
    private javax.swing.JCheckBox jCheckBoxComma;
    private javax.swing.JCheckBox jCheckBoxCoolantM789;
    private javax.swing.JCheckBox jCheckBoxFeedG9x;
    private javax.swing.JCheckBox jCheckBoxG54x;
    private javax.swing.JCheckBox jCheckBoxG6x;
    private javax.swing.JCheckBox jCheckBoxM345;
    private javax.swing.JCheckBox jCheckBoxMStopping;
    private javax.swing.JCheckBox jCheckBoxPatenthesis;
    private javax.swing.JCheckBox jCheckBoxSpaceG123;
    private javax.swing.JCheckBox jCheckBoxSplines;
    private javax.swing.JCheckBox jCheckBoxTempo;
    private javax.swing.JCheckBox jCheckBoxToolCoice;
    private javax.swing.JCheckBox jCheckBoxToolLength;
    private javax.swing.JCheckBox jCheckBoxZcoords;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelModal;
    private javax.swing.JPanel jPanelMotion;
    // End of variables declaration//GEN-END:variables

    public void dispose() {
        frame.dispose();
    }
}
