/*
 * Copyright (C) 2023 moi
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

import gcodeeditor.gui.JEditorFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Used to simplify GUI dialogs managments
 * 
 * @author clement
 */
public class DialogManager extends javax.swing.JDialog {
    
    /** The content of the dialog */
    ManagedPanel content;
    
    /** true if 'ok' button used and content validated */
    boolean doAction;
    
    /** the window to center dialog to */
    private JEditorFrame fromWindow;

    /** all already created dialogs */
    private HashMap<Class,ManagedPanel> guiPanels;
    
   
    /**
     * Create the DialogManager associated to this frame
     * @param parentWindow 
     */
    public DialogManager( JEditorFrame parentWindow) {
        fromWindow = parentWindow;
        guiPanels = new HashMap<>();
    }
    
    /**
     * Show a JDialog containing the <i>panel</i> and return it if 'Ok' button pressed
     * @param panelClass the class of the panel to show
     * @param param param to use in panel
     * @return null if closed by 'cancel' button or the panel
     */
    @SuppressWarnings("unchecked")
    public Object showDialogFor(Class panelClass, Object param) {           
        ManagedPanel res = null; 
        
        if ( guiPanels.containsKey(panelClass)) res = guiPanels.get(panelClass);         
        else {
            try {
                Object o = panelClass.getDeclaredConstructor().newInstance();
                if ( o instanceof  ManagedPanel) res = (ManagedPanel)o;                    
            } catch (Exception e) {
                Logger.getLogger(JEditorFrame.class.getName()).log(Level.SEVERE, null, e);
                e.printStackTrace();
                return null;
            }
            guiPanels.put(panelClass, res);
            DialogManager d = new DialogManager(fromWindow, res.title, true, res);                
            d.getContentPane().add( res);
            d.pack();            
            res.dialog = d;
        }           
       
        res.setParam( param);
        res.dialog.setLocationRelativeTo(fromWindow);
        res.dialog.setVisible(true);
        return res.dialog.doAction ? res : null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButtonCancel = new javax.swing.JButton();
        jButtonOk = new javax.swing.JButton();

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonCancel);

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonOk);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Represente a JDialog used internaly to manage each panel interactions
     * @param parent
     * @param title
     * @param modal
     * @param panel
     */
    DialogManager(java.awt.Frame parent, String title, boolean modal, ManagedPanel panel) {        
        super(parent, title, modal);
        getContentPane().add(content = panel, java.awt.BorderLayout.CENTER);
        initComponents();
        getRootPane().setDefaultButton( jButtonOk);
        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                jButtonCancelActionPerformed(ae);
            } },KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        if ( content.validateFields()) {
            doAction = true;
            setVisible(false);
        }
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        doAction = false;
        setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
