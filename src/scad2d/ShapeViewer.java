package scad2d;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class ShapeViewer extends JPanel {

    private java.util.List<ColoredShape> shapes;
    boolean darkMode = false;

    public ShapeViewer(boolean dark) {
        darkMode = dark;
        setBackground(darkMode ? Color.BLACK : Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
        setMinimumSize(new Dimension(320, 320));
    }

    public void updateContent(java.util.List<ColoredShape> content, boolean darkMode) {
        shapes = content;
        this.darkMode = darkMode;

        setBackground(darkMode ? Color.BLACK : Color.WHITE);
        invalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (shapes == null || shapes.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calcul de l'enveloppe globale pour centrer et adapter l’échelle
        Rectangle2D bounds = getGlobalBounds(shapes);
        if (bounds == null) {
            return;
        }

        double margin = 20;
        double scaleX = (getWidth() - 2 * margin) / bounds.getWidth();
        double scaleY = (getHeight() - 2 * margin) / bounds.getHeight();
        double scale = Math.min(scaleX, scaleY);

        AffineTransform transform = new AffineTransform();
        transform.translate(getWidth() / 2.0, getHeight() / 2.0);
        transform.scale(scale, -scale); // inverse Y
        transform.translate(-bounds.getCenterX(), -bounds.getCenterY());

        // ==== DESSIN DES AXES ====
        // On calcule les extrémités des axes en coordonnées monde
        Line2D xAxisWorld = new Line2D.Double(-10000, 0, 10000, 0);
        Line2D yAxisWorld = new Line2D.Double(0, -10000, 0, 10000);

        // Transformer ces axes dans le repère écran
        Shape xAxisScreen = transform.createTransformedShape(xAxisWorld);
        Shape yAxisScreen = transform.createTransformedShape(yAxisWorld);

        g2.setColor(Color.GRAY);
        g2.draw(xAxisScreen);
        g2.draw(yAxisScreen);

        for (ColoredShape cs : shapes) {
            Shape transformedShape = transform.createTransformedShape(cs.getShape());

            // Couleur : chaîne -> Color
            Color fill = parseColor(cs.getColor());
            g2.setColor(fill);
            g2.fill(transformedShape);

            g2.setColor(Color.BLACK);
            g2.draw(transformedShape);
        }
    }

    private Rectangle2D getGlobalBounds(List<ColoredShape> shapes) {
        Rectangle2D bounds = null;
        for (ColoredShape cs : shapes) {
            Rectangle2D sBounds = cs.getShape().getBounds2D();
            if (bounds == null) {
                bounds = new Rectangle2D.Double(sBounds.getX(), sBounds.getY(), sBounds.getWidth(), sBounds.getHeight());
            } else {
                Rectangle2D.union(bounds, sBounds, bounds);
            }
        }
        return bounds;
    }

    private Color parseColor(String colorName) {
        if (colorName == null) {
            return darkMode ? Color.WHITE : Color.BLACK;
        }

        // remove ":<name>" if any
        if (colorName.contains(":")) {
            colorName = colorName.substring(0, colorName.indexOf(":"));
        }

        switch (colorName.toLowerCase()) {
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "blue":
                return Color.BLUE;
            case "yellow":
                return Color.YELLOW;
            case "cyan":
                return Color.CYAN;
            case "magenta":
                return Color.MAGENTA;
            case "black":
                return Color.BLACK;
            case "white":
                return Color.WHITE;
            case "gray":
                return Color.GRAY;
            case "orange":
                return Color.ORANGE;
            case "pink":
                return Color.PINK;
            default:
                // Supporte éventuellement les couleurs HTML : "#RRGGBB"
                try {
                    return Color.decode(colorName);
                } catch (NumberFormatException e) {
                    return darkMode ? Color.white : Color.BLACK; // Couleur non reconnue
                }
        }
    }
}
