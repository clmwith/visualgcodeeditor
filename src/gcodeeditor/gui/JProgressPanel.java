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

import java.awt.Frame;
import javax.swing.JDialog;

/**
 *
 * @author Clément
 */
public class JProgressPanel extends javax.swing.JPanel {

    public interface ProgressListenner {
        public void cancelAsked();
    }
    
    ProgressListenner listener;
    JDialog d;
    
    /**
     * Creates new form JProgressPanel
     */
    public JProgressPanel(Frame owner) {
        initComponents();
        d=new JDialog(owner, true);
    }
    
    public void doProgress( ProgressListenner withMe) {
        listener = withMe;
        jProgressBar.setValue(0);
        d.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelMessage = new javax.swing.JLabel();
        jProgressBar = new javax.swing.JProgressBar();
        jButtonCancel = new javax.swing.JButton();

        jLabelMessage.setText("jLabel1");
        add(jLabelMessage);
        add(jProgressBar);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        add(jButtonCancel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        d.setVisible(false);
        listener.cancelAsked();
    }//GEN-LAST:event_jButtonCancelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JLabel jLabelMessage;
    private javax.swing.JProgressBar jProgressBar;
    // End of variables declaration//GEN-END:variables
}