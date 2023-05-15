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

import gcodeeditor.GFont;
import gcodeeditor.HersheyFont;
import gcodeeditor.LibreCadFont;
import gcodeeditor.SystemFont;
import gelements.GElement;
import gelements.GGroup;
import gelements.PaintContext;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author Clément Gérardin @ Marseille.fr
 */
public class JFontChooserPanel extends javax.swing.JPanel {
    
    /** The list of possible font sizes. */
    private static final String[] SIZES =
            {"5", "8", "9", "10", "11", "12", "13", "14", "16", "18", "20", "24", "26", "28", "32", "36", "40", "48", "56", "64", "72", "90", "102", "120", "140", "160", "180", "200", "220", "250", "300" };  
    
    /** The list of possible FONTS. */
    private static final String[] SYSTEM_FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
 
    /** Is True if the Dialog was closed with the buytton OK. */
    private boolean validated;
    
    /** The Dialog uset to show this panel */
    private JDialog dialog;
    
    /** List of fonts for jComboBoxNames */
    private static DefaultComboBoxModel<String> hersheyFontModel, libreCadFontModel, systemFontModel, SIZE_MODEL;

    /** Show the preview of current font */
    class FontPreview extends JPanel {
        GFont font;
        String text;
        PaintContext pc = new PaintContext();
        
        public FontPreview( GFont f, String text) {
            font = f;
            this.text = text;
            setPreferredSize(new Dimension(500, 200));
        }
        @Override
        public void paintComponent( Graphics g) {
            pc.g = (Graphics2D)g;
            g.setColor(Color.white);
            g.fillRect(0,0, getWidth(), getHeight());
            if ( (font != null) && ( text != null)) {
                AffineTransform t = pc.g.getTransform();
                g.setColor(Color.black);
                
                GElement gText = font.getTextPaths(text);
                Rectangle2D b = gText.getBounds();
                if ( b != null) {
                    pc.zoomFactor = 1;
                    pc.g.translate(5, b.getHeight()+ 5);
                    gText.paint(pc);
                    pc.zoomFactor = 4;
                    pc.g.translate(0, b.getHeight()*4 + 5);
                    gText.paint(pc);
                    pc.g.setTransform(t);
                }
            } else {
                if ( font == null) {
                    g.setColor(Color.BLACK);
                    g.drawString("No font found.", 10, 40);
                }
            }
        }
        private void setFont( GFont f) { font = f; repaint(); }
        public void setText( String text) { this.text = text; repaint(); }
    }
    private FontPreview preview;
    
        
    /**
     * Creates new form JFontChooserPanel
     */
    public JFontChooserPanel() {
        if ( hersheyFontModel == null)
            hersheyFontModel = new javax.swing.DefaultComboBoxModel<>(HersheyFont.FONTS);
        
        if ( libreCadFontModel == null)
            libreCadFontModel = new DefaultComboBoxModel<>(LibreCadFont.LIBRECAD_FONTS);        
           
        if ( systemFontModel == null)
            systemFontModel= new DefaultComboBoxModel<>(SYSTEM_FONTS);
        
        SIZE_MODEL = new DefaultComboBoxModel<>( SIZES);       
        
        initComponents();
        preview.setText(jTextFieldText.getText());
        jComboBoxSize.setSelectedItem(12);
    }
    
    /** Change the text to render */
    void setText(String text) {
        jTextFieldText.setText(text);
        preview.setText(text);
        updateGUI();
    }
    
    public void setFont( GFont f, double fontSize) {
        String p[] = f.getName().split(";");
        switch (p[0]) {
            case "Hershey":
                jComboBoxType.setSelectedIndex(0);
                if ( jComboBoxNames.getModel() != hersheyFontModel)
                        jComboBoxNames.setModel( hersheyFontModel);
                       
                for( int i =0 ; i < HersheyFont.FONTS.length; i++) {
                    if ( HersheyFont.FONTS[i].equals( p[1])) {
                        jComboBoxNames.setSelectedIndex(i);
                        break;
                    }
                }
                break;
                
            case "LCAD":
                jComboBoxType.setSelectedIndex(1);
                if ( jComboBoxNames.getModel() != libreCadFontModel) 
                        jComboBoxNames.setModel(libreCadFontModel);
                
                for( int i =0 ; i < LibreCadFont.LIBRECAD_FONTS.length; i++) {
                    if ( LibreCadFont.LIBRECAD_FONTS[i ].equals( p[1])) {
                        jComboBoxNames.setSelectedIndex(i);
                        break;
                    }
                }
                break;
                
            case "System": 
                jComboBoxType.setSelectedIndex(2); 
                if ( jComboBoxNames.getModel() != systemFontModel)
                        jComboBoxNames.setModel( systemFontModel);
                
                for( int i =0 ; i < SYSTEM_FONTS.length; i++) {
                    if ( SYSTEM_FONTS[i].equals( p[1])) {
                        jComboBoxNames.setSelectedIndex(i);
                        break;
                    }           
                }
                break;
                
            default:
                throw new AssertionError("Unknow font type :" + p[0]);
        }
        jComboBoxSize.setSelectedItem( gcodeeditor.GWord.roundForGCODE(fontSize));
        jCheckBoxBold.setSelected( f.isBold());
        jCheckBoxItalic.setSelected( f.isItalic());
        updateFont();
    }
    
    private void updateGUI() {
        jCheckBoxBold.setEnabled( jComboBoxType.getSelectedIndex() == 2);
        jCheckBoxItalic.setEnabled( jComboBoxType.getSelectedIndex() == 2); 
        preview.repaint();
    }
    
    private void updateFont() {
        try {
            switch( jComboBoxType.getSelectedIndex()) {
                case 0: // Hershey
                    if ( jComboBoxNames.getModel() != hersheyFontModel) {
                        jComboBoxNames.setModel( hersheyFontModel);
                        //jComboBoxNames.setSelectedIndex(0);
                    }             
                    preview.setFont( (GFont)HersheyFont.getFont( this.getClass(), jComboBoxNames.getSelectedIndex()));
                    break;
                    
                case 1: // LibreCad
                    if ( jComboBoxNames.getModel() != libreCadFontModel) {
                        jComboBoxNames.setModel( libreCadFontModel);
                    }
                    preview.setFont( (GFont)LibreCadFont.getFont( this.getClass(),jComboBoxNames.getSelectedIndex()));
                    break;
                    
                case 2: // System
                    if ( jComboBoxNames.getModel() != systemFontModel) 
                        jComboBoxNames.setModel( systemFontModel);
                    
                    int sizeInt = 12;
                    try {
                            sizeInt = Integer.parseInt(jComboBoxSize.getSelectedItem().toString());
                    } catch ( NumberFormatException e) {
                        jComboBoxSize.getEditor().setItem( 12);
                    }
                    
                    preview.setFont( new SystemFont((String)jComboBoxNames.getSelectedItem(), 
                            jCheckBoxBold.isSelected(), jCheckBoxItalic.isSelected(), sizeInt));        
            }
            updateGUI();
            
        } catch (IOException ex) {
            Logger.getLogger(JFontChooserPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        jPanel3 = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jComboBoxNames = new javax.swing.JComboBox<>();
        jTextFieldText = new javax.swing.JTextField();
        jLabelFont = new javax.swing.JLabel();
        jLabelText = new javax.swing.JLabel();
        jComboBoxType = new javax.swing.JComboBox<>();
        jCheckBoxBold = new javax.swing.JCheckBox();
        jCheckBoxItalic = new javax.swing.JCheckBox();
        jComboBoxSize = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setMinimumSize(new java.awt.Dimension(200, 200));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add( preview = new FontPreview(null, null), java.awt.BorderLayout.CENTER);
        try {
            preview.setFont( HersheyFont.getFont(this.getClass(), 0));
        } catch ( IOException e) { }
        add(jPanel1, java.awt.BorderLayout.CENTER);

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanel3.add(jButtonOk);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(jButtonCancel);

        add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jComboBoxNames.setModel(hersheyFontModel = new javax.swing.DefaultComboBoxModel<>(HersheyFont.FONTS));
        jComboBoxNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxNamesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jComboBoxNames, gridBagConstraints);

        jTextFieldText.setColumns(40);
        jTextFieldText.setText("Type your text here");
        jTextFieldText.setMinimumSize(new java.awt.Dimension(200, 29));
        jTextFieldText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(jTextFieldText, gridBagConstraints);

        jLabelFont.setText("Font");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabelFont, gridBagConstraints);

        jLabelText.setText("Text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel2.add(jLabelText, gridBagConstraints);

        jComboBoxType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hershey", "LibreCad", "System"}));
        jComboBoxType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jComboBoxType, gridBagConstraints);

        jCheckBoxBold.setText("Bold");
        jCheckBoxBold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxBoldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jCheckBoxBold, gridBagConstraints);

        jCheckBoxItalic.setText("Italic");
        jCheckBoxItalic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxItalicActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jCheckBoxItalic, gridBagConstraints);

        jComboBoxSize.setEditable(true);
        jComboBoxSize.setModel(SIZE_MODEL);
        jComboBoxSize.setToolTipText("The height of the font");
        jComboBoxSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSizeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jComboBoxSize, gridBagConstraints);

        jPanel4.add(jPanel2, java.awt.BorderLayout.CENTER);

        add(jPanel4, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTextActionPerformed
        preview.setText(jTextFieldText.getText());
    }//GEN-LAST:event_jTextFieldTextActionPerformed

    private void jComboBoxNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxNamesActionPerformed
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateFont();
            }
        });
    }//GEN-LAST:event_jComboBoxNamesActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        validated = true;
        dialog.setVisible(false);
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        validated = false;
        dialog.setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jComboBoxTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTypeActionPerformed
        EventQueue.invokeLater(() -> { updateFont(); });      
    }//GEN-LAST:event_jComboBoxTypeActionPerformed

    private void jComboBoxSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSizeActionPerformed
        EventQueue.invokeLater(() -> { updateFont(); });  
    }//GEN-LAST:event_jComboBoxSizeActionPerformed

    private void jCheckBoxBoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBoldActionPerformed
        EventQueue.invokeLater(() -> { updateFont(); });
    }//GEN-LAST:event_jCheckBoxBoldActionPerformed

    private void jCheckBoxItalicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxItalicActionPerformed
        EventQueue.invokeLater(() -> { updateFont(); }); 
    }//GEN-LAST:event_jCheckBoxItalicActionPerformed

    /**
     * Show a modal dialog to choose a text and a font.
     * @return the new GElement corresponding to the text and font choosed.
     */
    public GGroup showFontChooserWindow() {
        if ( dialog == null) {
            dialog = new JDialog((java.awt.Frame)null, true);
            dialog.getContentPane().add(this);
            dialog.pack();
        }
        dialog.setVisible(true);
        preview.text = jTextFieldText.getText();
        return (! validated || (preview.font == null)) ? null : preview.font.getTextPaths(preview.text);
    }
    
    /**
     * @return The size of the font or NaN if not set.
     */
    public float getChoosedSize() {
        try {
            return Float.parseFloat(jComboBoxSize.getSelectedItem().toString());
        } catch (NullPointerException | NumberFormatException e) {
            return Float.NaN;
        }
    }
    public GFont getChoosedFont() {
        return preview.font;
    }
    
    public String getChoosedText() {
        return jTextFieldText.getText();
    }
                
    public static void main(String args[]) {
        new JFontChooserPanel().showFontChooserWindow();
        System.out.println("fini");
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JCheckBox jCheckBoxBold;
    private javax.swing.JCheckBox jCheckBoxItalic;
    private javax.swing.JComboBox<String> jComboBoxNames;
    private javax.swing.JComboBox<String> jComboBoxSize;
    private javax.swing.JComboBox<String> jComboBoxType;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextFieldText;
    // End of variables declaration//GEN-END:variables
}
