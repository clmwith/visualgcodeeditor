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
package gcodeeditor.gui.dialogs;


import java.awt.geom.Rectangle2D;
import java.util.Locale;
import javax.swing.JOptionPane;

/**
 * A frame to choose scale elements options.
 * @author Clément
 */
public class JScalePanel extends ManagedPanel {

    public double xScale, yScale;
    public int copies;
    public boolean keepOriginal;
    public boolean fromCenter;
    
    private Rectangle2D dim;

    public JScalePanel() {
        super("Scale selection");
        initComponents();
    }

    @Override
    void setParam(Object param) {
        assert( param instanceof Rectangle2D);
        dim = (Rectangle2D)param;
        jTextFieldWidth.setText( String.format(Locale.ROOT, "%.3f", dim.getWidth()));
        jTextFieldHeight.setText( String.format(Locale.ROOT, "%.3f", dim.getHeight()));
    }     
    
    @Override
    public boolean validateFields() {
        fromCenter = jRadioButtonOCenter.isSelected();  
        
        if ( jTabbedPane1.getSelectedComponent() == jPanelFactor) {
            xScale = parseExpression(jTextFieldXFactor, false);
            yScale = parseExpression(jTextFieldYFactor, false);
            copies = parseIntExpression(jTextFieldCopies, false);
            keepOriginal = jCheckBoxKeepOrig.isSelected();
            if ( isNaN(copies)) return false;
            
        } else {            
            xScale = parseExpression(jTextFieldWidth, false);
            if ( ! isNaN(xScale)) xScale /= dim.getWidth();
            
            yScale = parseExpression(jTextFieldHeight, false);
            if ( ! isNaN(yScale)) yScale /= dim.getHeight();
            
            copies = 1;
            keepOriginal = false;
        }
        return ! (isNaN(xScale) || isNaN(yScale));
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

        jPanel1 = new javax.swing.JPanel();
        jRadioButtonO2D = new javax.swing.JRadioButton();
        jRadioButtonOCenter = new javax.swing.JRadioButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelFactor = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldYFactor = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldXFactor = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldCopies = new javax.swing.JTextField();
        jCheckBoxKeepOrig = new javax.swing.JCheckBox();
        jLabelWidth = new javax.swing.JLabel();
        jLabelHeight = new javax.swing.JLabel();
        jPanelDimension = new javax.swing.JPanel();
        jTextFieldWidth = new javax.swing.JTextField();
        jTextFieldHeight = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxIso = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Origin"));

        jRadioButtonO2D.setSelected(true);
        jRadioButtonO2D.setText("2D Cursor");
        jRadioButtonO2D.setToolTipText("Turn around 2D Cursor");
        jRadioButtonO2D.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonO2DActionPerformed(evt);
            }
        });

        jRadioButtonOCenter.setText("Center");
        jRadioButtonOCenter.setToolTipText("turn around center of path");
        jRadioButtonOCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonOCenterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jRadioButtonO2D)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonOCenter)
                .addGap(0, 146, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jRadioButtonO2D)
                .addComponent(jRadioButtonOCenter))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanelFactor.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Y *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanelFactor.add(jLabel3, gridBagConstraints);

        jTextFieldYFactor.setColumns(6);
        jTextFieldYFactor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldYFactorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 66;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        jPanelFactor.add(jTextFieldYFactor, gridBagConstraints);

        jLabel1.setText("X *");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 12, 0, 0);
        jPanelFactor.add(jLabel1, gridBagConstraints);

        jTextFieldXFactor.setColumns(6);
        jTextFieldXFactor.setToolTipText("Angle in degre");
        jTextFieldXFactor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldXFactorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 66;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanelFactor.add(jTextFieldXFactor, gridBagConstraints);

        jLabel2.setText("Count");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanelFactor.add(jLabel2, gridBagConstraints);

        jTextFieldCopies.setColumns(6);
        jTextFieldCopies.setText("1");
        jTextFieldCopies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldCopiesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 66;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        jPanelFactor.add(jTextFieldCopies, gridBagConstraints);

        jCheckBoxKeepOrig.setText("Keep original");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        jPanelFactor.add(jCheckBoxKeepOrig, gridBagConstraints);

        jLabelWidth.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanelFactor.add(jLabelWidth, gridBagConstraints);

        jLabelHeight.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanelFactor.add(jLabelHeight, gridBagConstraints);

        jTabbedPane1.addTab("Factor", jPanelFactor);

        jPanelDimension.setMinimumSize(new java.awt.Dimension(218, 0));
        jPanelDimension.setLayout(new java.awt.GridBagLayout());

        jTextFieldWidth.setColumns(6);
        jTextFieldWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldWidthActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 110;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        jPanelDimension.add(jTextFieldWidth, gridBagConstraints);

        jTextFieldHeight.setColumns(6);
        jTextFieldHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldHeightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 110;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
        jPanelDimension.add(jTextFieldHeight, gridBagConstraints);

        jLabel4.setText("Width");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanelDimension.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanelDimension.add(jLabel5, gridBagConstraints);

        jCheckBoxIso.setSelected(true);
        jCheckBoxIso.setText("keep ratio");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanelDimension.add(jCheckBoxIso, gridBagConstraints);

        jTabbedPane1.addTab("Dimension", jPanelDimension);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButtonO2DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonO2DActionPerformed
        jRadioButtonOCenter.setSelected(false);
    }//GEN-LAST:event_jRadioButtonO2DActionPerformed

    private void jRadioButtonOCenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonOCenterActionPerformed
        jRadioButtonO2D.setSelected(false);
    }//GEN-LAST:event_jRadioButtonOCenterActionPerformed

    private void jTextFieldCopiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCopiesActionPerformed
        try {
            int n = Integer.parseInt( jTextFieldCopies.getText());
            if ( n >= 1) {
                jCheckBoxKeepOrig.setSelected( n != 1);
                return;
            }
        } catch (Exception e) { }
        jTextFieldCopies.setText("1");
    }//GEN-LAST:event_jTextFieldCopiesActionPerformed

    private void jTextFieldXFactorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldXFactorActionPerformed
        try {
            xScale = Double.parseDouble(jTextFieldXFactor.getText());
            jLabelWidth.setText(String.format(Locale.ROOT, "= %.3f", xScale * dim.getWidth()));
            jTextFieldYFactor.requestFocus();
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Invalid Parameter detected", "Error", JOptionPane.ERROR_MESSAGE);
        }// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldXFactorActionPerformed

    private void jTextFieldYFactorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldYFactorActionPerformed
        try {         
            yScale = Double.parseDouble(jTextFieldYFactor.getText());
            jLabelHeight.setText(String.format(Locale.ROOT, "= %.3f", yScale * dim.getHeight()));
            jTextFieldCopies.requestFocus();
        } catch ( NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Invalid Parameter detected", "Error", JOptionPane.ERROR_MESSAGE);
        }// TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldYFactorActionPerformed

    private void jTextFieldWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldWidthActionPerformed
        if ( jCheckBoxIso.isSelected()) {
            try {
                xScale = yScale = Double.parseDouble(jTextFieldWidth.getText()) / dim.getWidth();
                jTextFieldHeight.setText( String.format(Locale.ROOT, "%f", xScale * dim.getHeight()));
                jTextFieldHeight.requestFocus();
            } catch ( NumberFormatException e) { 
                JOptionPane.showMessageDialog(dialog, "Invalid Parameter detected", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jTextFieldWidthActionPerformed

    private void jTextFieldHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldHeightActionPerformed
        if ( jCheckBoxIso.isSelected()) {
            try {
                xScale = yScale = Double.parseDouble(jTextFieldHeight.getText()) / dim.getHeight();
                jTextFieldWidth.setText( String.format(Locale.ROOT, "%f", yScale * dim.getWidth()));
                jTextFieldWidth.requestFocus();
            } catch ( NumberFormatException e) { 
                JOptionPane.showMessageDialog(dialog, "Invalid Parameter detected", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jTextFieldHeightActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxIso;
    private javax.swing.JCheckBox jCheckBoxKeepOrig;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelHeight;
    private javax.swing.JLabel jLabelWidth;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelDimension;
    private javax.swing.JPanel jPanelFactor;
    private javax.swing.JRadioButton jRadioButtonO2D;
    private javax.swing.JRadioButton jRadioButtonOCenter;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldCopies;
    private javax.swing.JTextField jTextFieldHeight;
    private javax.swing.JTextField jTextFieldWidth;
    private javax.swing.JTextField jTextFieldXFactor;
    private javax.swing.JTextField jTextFieldYFactor;
    // End of variables declaration//GEN-END:variables
}
