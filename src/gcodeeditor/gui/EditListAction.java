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

/**
 * Used to edit Gcode lines
 * @author Clément, inspired by https://tips4java.wordpress.com/2008/10/19/list-editor/
 */

import gelements.GGroup;
import gelements.GElement;
import gelements.GTextOnPath;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/*
 *  A simple popup editor for a JList that allows you to change
 *  the value in the selected row.
 *
 *  The default implementation has a few limitations:
 *
 *  a) the JList must be using the DefaultListModel
 *  b) the data in the model is replaced with a String object
 *
 *  If you which to use a different model or different data then you must
 *  extend this class and:
 *
 *  a) invoke the setModelClass(...) method to specify the ListModel you need
 *  b) override the applyValueToModel(...) method to update the model
 */
public class EditListAction extends AbstractAction
{
	private JList list;

	private JPopupMenu editPopup;
	private JTextField editTextField;
        
        private final JProjectEditorPanel shapeviewer;

	public EditListAction(JProjectEditorPanel v)
	{
            shapeviewer = v;
        }

	/*
	 * Enter GGroup / Edit Element or display the popup editor whene [ENTER] or 2bl click on jListEditor
	 */
        @Override
	public void actionPerformed(ActionEvent e)
	{          
            list = (JList)e.getSource();
            ListModel model = list.getModel();
            if ( list.getSelectedIndex() == -1) return;

            // Enter in a Group ?
            if ( (model instanceof GGroup) || (model instanceof JProjectEditorPanel.DocumentListModel)) { 
                // we are not in a block then no line to edit
                shapeviewer.editElement(list.getSelectedIndex());
                return;
            }
                   
            if ( (model instanceof GTextOnPath) && (list.getSelectedIndex() == 0)) {
                // Edit the Font (line 0) of this TextOnPath
                Container p = shapeviewer.getParent();
                do {
                    p = p.getParent();
                    if ( p instanceof JEditorFrame ) {
                        ((JEditorFrame)p).editFontOf((GElement)model);
                        return;
                    }
                } while (p != null);
            }

            //  Do a lazy creation of the popup editor
            if (editPopup == null) createEditPopup();

            //  Position the popup editor over top of the selected row
            int row = list.getSelectedIndex();
            Rectangle r = list.getCellBounds(row, row);

            editPopup.setPreferredSize(new Dimension(r.width, r.height));
            editPopup.show(list, r.x, r.y);

            //  Prepare the text field for editing
            String t = list.getSelectedValue().toString();
            editTextField.setText( t );
            if ( t.startsWith(";") && t.contains("=") ) {
                // select only the value of param
                editTextField.select( t.indexOf('=')+1, t.length());
            } else 
                editTextField.selectAll();
            
            editTextField.requestFocus();
	}

	/*
	 *  Create the popup editor
	 */
	private void createEditPopup()
	{
		//  Use a text field as the editor
		editTextField = new JTextField();
		Border border = UIManager.getBorder("List.focusCellHighlightBorder");
		editTextField.setBorder( border );

                editTextField.addFocusListener( new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        shapeviewer.setKeyFocus(false);
                    }

                    @Override
                    public void focusLost(FocusEvent e) {                       
                        shapeviewer.setKeyFocus(true);
                    }
                });
		//  Add an Action to the text field to save the new value to the model
		editTextField.addActionListener((ActionEvent e) -> {
                    String value = editTextField.getText();
                    ListModel model = list.getModel();
                    int row = list.getSelectedIndex();
                    editPopup.setVisible(false);
                    shapeviewer.updateEditedRow(value, model, row);                    
                });

		//  Add the editor to the popup

	    editPopup = new JPopupMenu();
            editPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
            editPopup.add(editTextField);
	}
}