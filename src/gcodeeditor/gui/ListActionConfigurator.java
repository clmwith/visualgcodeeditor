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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.KeyStroke;

/**
 * Class to configure Keyboard and Mouse actions into the JList (GCode editor).
 * 
 * @author Clément
 */
public class ListActionConfigurator extends MouseAdapter
{
    public static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    public static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    public static final KeyStroke DELETE1 = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
    public static final KeyStroke DELETE2 = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
    public static final KeyStroke INSERT = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0);
    public static final KeyStroke F2 = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        
    private final JList list;
    /** Double Click KeyStroke emulation. */
    private final KeyStroke dblClickKeyStrokeEquivalent;

    /*
     * Create ListActionConfigurator to manage mouse Click and set shortcut to <i>list</i>
     */
    public ListActionConfigurator(JList list)
    {
        this.list = list;
        list.addMouseListener( this );
        dblClickKeyStrokeEquivalent = ENTER;
    }

    /**
     *  Add the Action to the ActionMap with a new keyStroke
     * @param key
     * @param action
     */
    public void setAction(KeyStroke key, Action action )
    {
        //  Add the KeyStroke to the InputMap
        InputMap im = list.getInputMap();
        im.put(key, key);

        list.getActionMap().put(key, action);
    }

    //  Implement MouseListener interface

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if ( e.getButton() == 3) { // generate action corresponding to (Escape)
            Action action = list.getActionMap().get(ESCAPE);
            if (action != null)
            {
                    ActionEvent event = new ActionEvent(
                            list,
                            ActionEvent.ACTION_PERFORMED,
                            "EXIT");
                    action.actionPerformed(event);
            }
        }
        if (e.getClickCount() == 2) // generate the action corresponding to keyStroke (ENTER)
        {
            Action action = list.getActionMap().get(dblClickKeyStrokeEquivalent);
            if (action != null)
            {
                    ActionEvent event = new ActionEvent(
                            list,
                            ActionEvent.ACTION_PERFORMED,
                            "ENTER");
                    action.actionPerformed(event);
            }
        }
    }

}