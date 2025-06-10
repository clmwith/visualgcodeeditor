/*
 * Copyright (C) 2025 moi
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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.*;
import javax.swing.text.Document;

/**
 *
 * @author moi
 */
public class JTextPaneLineNumber extends JComponent {

    private final JTextPane textPane;

    public JTextPaneLineNumber(JTextPane textPane) {
        this.textPane = textPane;
        setPreferredWidth(40); // Largeur du composant des numéros de ligne
    }

    private void setPreferredWidth(int width) {
        Dimension dim = getPreferredSize();
        dim.width = width;
        setPreferredSize(dim);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Obtenir les informations sur le document
        Document doc = textPane.getDocument();
        int lineHeight = textPane.getFontMetrics(textPane.getFont()).getHeight();
        int totalLines = doc.getDefaultRootElement().getElementCount();

        // Dessiner les numéros de ligne
        for (int i = 1; i <= totalLines; i++) {
            int y = (i - 1) * lineHeight;
            g.drawString(String.valueOf(i), 10, y + lineHeight - 5);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getPreferredWidth(), textPane.getHeight());
    }

    private int getPreferredWidth() {
        FontMetrics fontMetrics = textPane.getFontMetrics(textPane.getFont());
        int maxWidth = fontMetrics.stringWidth(String.valueOf(textPane.getDocument().getDefaultRootElement().getElementCount())) + 20;
        return Math.max(maxWidth, 40); // Largeur minimale de 40 pixels
    }
}
