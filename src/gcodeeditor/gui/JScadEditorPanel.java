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

import scad2d.ShapeViewer;
import gelements.GScad2DComposition;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.html.HTMLEditorKit;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import scad2d.ColoredShape;
import scad2d.Scad2DLexer;
import scad2d.Scad2DParser;

/**
 *
 * @author moi
 */
public class JScadEditorPanel extends javax.swing.JPanel {

    boolean darkMode;
    ShapeViewer geomViewer;
    List<ColoredShape> result;
    ActionListener listener;
    JTextPaneLineNumber lineNumberComponent;
    private String lastHighlightedText;
    public static final String HELP_STRING = """
<html>
   <h1>The Scad2D Language Reference</h1>

     <h2>General Syntax</h2>
     <p>Scad2D is a scripting language designed for creating 2D geometric shapes and transformations. It supports various control structures and functions to facilitate complex designs.</p>

     <h3>Conditionals</h3>
     <p>Conditional statements in Scad2D use the <code>if</code> keyword. They can optionally include an <code>else</code> clause.</p>
     <pre>
 if (condition) {
     // code to execute if condition is true
 } else {
     // code to execute if condition is false
 }
     </pre>

     <h3>Loops</h3>
     <p>Scad2D supports <code>for</code> loops for iterating over a range of values.</p>
     <pre>
 for (i = [start:end]) {
     // code to execute in each iteration
 }

 for (i = [start:step:end]) {
     // code to execute in each iteration with custom step
 }
     </pre>

     <h3>Modules</h3>
     <p>Modules allow you to define reusable blocks of code with parameters.</p>
     <pre>
 module myModule(param1, param2) {
     // module code
 }

 myModule(value1, value2);
     </pre>

     <h2>Variables and Values</h2>
     <p>Variables in Scad2D can hold various types of values, including numbers, booleans, and lists.</p>

     <h3>Numbers and Booleans</h3>
     <p>Variables can be assigned numeric or boolean values.</p>
     <pre>
 x = 10;
 y = true;
     </pre>

     <h3>Lists</h3>
     <p>Lists are collections of values, which can be of any type. Lists are defined using square brackets.</p>
     <pre>
 myList = [1, 2, 3, [12,23], [15, [23,27] ,45]];
     </pre>

     <h2>Functions</h2>
     <p>Scad2D includes several built-in functions for mathematical operations and conditional logic.</p>

     <h3>Mathematical Functions</h3>
     <p>Scad2D supports common mathematical functions.</p>
     <pre>
 x = sin(45);
 y = max(10,vector,12);
 z = abs(a); 
 ...
     </pre>

     <h3>Ternary Operator</h3>
     <p>The ternary operator allows for inline conditional expressions.</p>
     <pre>
 condition ? valueIfTrue : valueIfFalse;
     </pre>

     <h2>Shape Creators and Modifiers</h2>
     <p>Scad2D provides several functions to create and modify shapes.</p>

     <h3>Shape Creators</h3>

     <h4>Circle</h4>
     <p>Creates a circle with a given radius.</p>
     <pre>
 circle(radius);
 circle(r=5);
     </pre>

     <h4>Rectangle</h4>
     <p>Creates a rectangle with given dimensions.</p>
     <pre>
 square(size);
 rect(width, height);
     </pre>

     <h4>Polygon</h4>
     <p>Creates a polygon from a list of points.</p>
     <pre>
 poly([[0,0], [10,0], [10,10]]);
     </pre>

     <h3>Shape Modifiers</h3>

     <h4>Union</h4>
     <p>Combines multiple shapes into a single shape.</p>
     <pre>
 union() {
     circle(5);
     square(5);
 }
     </pre>

     <h4>Difference</h4>
     <p>Subtracts shapes from a base shape.</p>
     <pre>
 difference() {
     square(10);
     circle(5);
 }
     </pre>

     <h4>Intersection</h4>
     <p>Keeps only the overlapping parts of shapes.</p>
     <pre>
 intersection() {
     circle(5);
     square(5);
 }
     </pre>

     <h4>Hull</h4>
     <p>Creates a convex hull around a set of shapes.</p>
     <pre>
 hull() {
     circle(5);
     square(5);
 }
     </pre>

     <h4>Translate</h4>
     <p>Moves shapes by a specified vector.</p>
     <pre>
 translate([x, y]) {
     circle(5);
 }
     </pre>

     <h4>Scale</h4>
     <p>Scales shapes by a specified factor.</p>
     <pre>
 scale([x, y]) {
     circle(5);
 }
     </pre>

     <h4>Rotate</h4>
     <p>Rotates shapes by a specified angle.</p>
     <pre>
 rotate(angle) {
     circle(5);
 }
     </pre>

     <h4>Mirror</h4>
     <p>Mirrors shapes across a specified line.</p>
     <pre>
 mirror([x, y]) {
     circle(5);
 }
     </pre>             
</html>
    """;
    
    JFrame jHelpFrame;
    ArrayList<String> undoStack = new ArrayList<>();
    int undoStackIndex = 0;
    boolean ignoreUpdates = false;

    /**
     * Creates new form JScadEditorPanel
     */
    public JScadEditorPanel(boolean dark) {
        darkMode = dark;
        geomViewer = new ShapeViewer(dark);

        initComponents();

        jTextPaneCode.addPropertyChangeListener("font", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Update the line number component when the font changes
                lineNumberComponent.invalidate();
                lineNumberComponent.repaint();
            }
        });

        // Définir le document stylisé pour la coloration syntaxique
        StyleContext sc = StyleContext.getDefaultStyleContext();
        // Créer des styles pour les mots-clés, les chaînes, les entiers et les commentaires
        Style keywordStyle = sc.addStyle("KeywordStyle", null);
        StyleConstants.setForeground(keywordStyle, Color.BLUE);

        Style stringStyle = sc.addStyle("StringStyle", null);
        StyleConstants.setForeground(stringStyle, Color.RED);

        Style numberStyle = sc.addStyle("NumberStyle", null);
        StyleConstants.setForeground(numberStyle, new Color(0, 128, 0)); // Vert

        Style commentStyle = sc.addStyle("CommentStyle", null);
        StyleConstants.setForeground(commentStyle, Color.GRAY);
        StyleConstants.setItalic(commentStyle, true);

        StyledDocument doc = jTextPaneCode.getStyledDocument();
        // Ajouter un écouteur de document pour recoloriser la syntaxe après chaque modification
        doc.addDocumentListener(new DocumentListener() {
            boolean into = false;

            void saveText() {
                // remove all redo possibilies
                if (ignoreUpdates) {
                    return;
                }
                String s = jTextPaneCode.getText();
                if ((undoStackIndex > 0) && s.equals(undoStack.get(undoStackIndex - 1))) {
                    return;
                }

                while (undoStack.size() > undoStackIndex) {
                    undoStack.removeLast();
                }
                undoStack.add(s);
                undoStackIndex++;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (into) {
                    return;
                }

                into = true;
                SwingUtilities.invokeLater(() -> {
                    highlightSyntax(jTextPaneCode);
                    saveText();
                    into = false;
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (into) {
                    return;
                }

                into = true;
                SwingUtilities.invokeLater(() -> {
                    highlightSyntax(jTextPaneCode);
                    saveText();
                    into = false;
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (into) {
                    return;
                }

                into = true;
                SwingUtilities.invokeLater(() -> {
                    highlightSyntax(jTextPaneCode);
                    saveText();
                    into = false;
                });
            }
        });

        // Bind Ctrl+Z (Undo)
        jTextPaneCode.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        jTextPaneCode.getActionMap().put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoStackIndex > 1) {
                    ignoreUpdates = true;
                    int c = jTextPaneCode.getCaretPosition();
                    jTextPaneCode.setText(undoStack.get(--undoStackIndex - 1));
                    jTextPaneCode.setCaretPosition(c);
                    EventQueue.invokeLater(() -> {
                        highlightSyntax(jTextPaneCode);
                        ignoreUpdates = false;
                    });
                }
            }
        });

        // Bind Ctrl+Y (Redo)
        jTextPaneCode.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        jTextPaneCode.getActionMap().put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoStackIndex < undoStack.size()) {
                    ignoreUpdates = true;
                    int c = jTextPaneCode.getCaretPosition();
                    jTextPaneCode.setText(undoStack.get(undoStackIndex++));
                    jTextPaneCode.setCaretPosition(c);
                    EventQueue.invokeLater(() -> {
                        highlightSyntax(jTextPaneCode);

                        ignoreUpdates = false;
                    });
                }
            }
        });

        // Bind F5
        jTextPaneCode.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "View");
        jTextPaneCode.getActionMap().put("View", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> jButtonViewActionPerformed(null));
            }
        });

        // Define Tab action
        Action indentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifySelectedLines(jTextPaneCode, true);
            }
        };

        // Define Shift+Tab (unindent) action
        Action unindentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifySelectedLines(jTextPaneCode, false);
            }
        };

        // Set custom key bindings
        InputMap im = jTextPaneCode.getInputMap();
        ActionMap am = jTextPaneCode.getActionMap();
        im.put(KeyStroke.getKeyStroke("TAB"), "indent");
        im.put(KeyStroke.getKeyStroke("shift TAB"), "unindent");
        am.put("indent", indentAction);
        am.put("unindent", unindentAction);

        changeDisplayMode();
        revalidate();
    }

    private static void modifySelectedLines(JTextPane textPane, boolean indent) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        Element root = doc.getDefaultRootElement();
        int startLine = root.getElementIndex(start);
        int endLine = root.getElementIndex(end);

        try {
            for (int i = startLine; i <= endLine; i++) {
                Element line = root.getElement(i);
                int lineStart = line.getStartOffset();
                int lineEnd = line.getEndOffset();
                String lineText = doc.getText(lineStart, lineEnd - lineStart);

                if (indent) {
                    doc.insertString(lineStart, "\t", null);
                } else if (lineText.startsWith("\t") || (lineText.startsWith(" "))) { // unindent space
                    doc.remove(lineStart, 1);
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    public void addListenner(ActionListener l) {
        listener = l;
    }

    public void setCode(String code) {
        SwingUtilities.invokeLater(() -> jTextPaneCode.setText(code));
    }

    public String getCode() {
        return jTextPaneCode.getText();
    }

    public static void main(String[] args) {
        // Exemple de texte avec coloration syntaxique
        String text = """
                      module Test(a, b) {
                          color("red") circle(a);
                          translate([-a/2, b]) rectangle(a);
                          echo("a=");
                          echo(a);
                      }
                      // Ceci est un commentaire sur une seule ligne
                      /* Ceci est un commentaire multiligne
                         qui s'\u00e9tend sur plusieurs lignes */
                      for (i = [0:1:4]) rotate(i*45) translate([100,0]) Test(10 * i, 20);
                      a = 10;
                      if (a >= 10) circle(10); else circle(100);""";

        // Créer et configurer la fenêtre principale
        JFrame frame = new JFrame("JScad2D Editor");

        JScadEditorPanel p = new JScadEditorPanel(true);
        p.addListenner(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Returned:" + ae.getActionCommand());
            }
        });
        frame.add(p);

        frame.setSize(800, 600);
        frame.pack();
        frame.setVisible(true);
        p.setCode(text);
    }

    public void runCode(String code) {
        jTextAreaLogs.setText("");
        try {
            CharStream cs = CharStreams.fromString(code);
            Scad2DLexer lexer = new Scad2DLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Scad2DParser parser = new Scad2DParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    jTextAreaLogs.append(String.format("Line %d:%d %s\n", line, charPositionInLine, msg));
                }
            });
            ParseTree tree = parser.file();
            GScad2DComposition.ShapeBuilder shapeBuilder = new GScad2DComposition.ShapeBuilder((String message) -> {
                jTextAreaLogs.append(message + "\n");
            });
            result = shapeBuilder.visit(tree);
            jTextAreaLogs.append("\nCreated " + result.size() + " patch(s)\n");
            jTextAreaLogs.setCaretPosition(jTextAreaLogs.getDocument().getLength());
        } catch (Exception e) {
            jTextAreaLogs.append("Error: " + e.getLocalizedMessage() + "\n");
            //e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> geomViewer.updateContent(result, darkMode));
    }

    private void highlightSyntax(JTextPane textPane) {
        if (jTextPaneCode.getText().equals(lastHighlightedText)) {
            return;
        }

        StyledDocument doc = jTextPaneCode.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();

        final int numSpaces = 3;
        FontMetrics fm = jTextPaneCode.getFontMetrics(jTextPaneCode.getFont());
        int charWidth = fm.charWidth(' ');
        int tabWidth = charWidth * numSpaces;
        // Create TabStops every tabWidth pixels
        int tabCount = 30;  // Enough tabs to cover a wide line
        TabStop[] tabs = new TabStop[tabCount];
        for (int i = 0; i < tabCount; i++) {
            tabs[i] = new TabStop((i + 1) * tabWidth);
        }
        TabSet tabSet = new TabSet(tabs);
        // Apply it to the document style
        Style style = jTextPaneCode.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setTabSet(style, tabSet);
        doc.setParagraphAttributes(0, doc.getLength(), style, false);

        Style keywordStyle = sc.getStyle("KeywordStyle");
        Style stringStyle = sc.getStyle("StringStyle");
        Style numberStyle = sc.getStyle("NumberStyle");
        Style commentStyle = sc.getStyle("CommentStyle");

        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }

        // Effacer les styles existants
        doc.setCharacterAttributes(0, text.length(), sc.getStyle(StyleContext.DEFAULT_STYLE), true);

        // Expressions régulières pour les mots-clés, les chaînes, les entiers et les commentaires
        String keywordRegex = "\\b(pi|PI|sin|cos|tan|min|max|abs|int|module|mirror|color|trans|translate|scale|rotate|union|diff|difference|inter|intersection|hull|if|else|for|echo|circle|cube|square|rect|rectangle|poly|polygon)\\b";
        String stringRegex = "\"[^\"]*\"";
        String numberRegex = "\\b(\\d+|true|false)\\b";

        String singleLineCommentRegex = "//.*";
        String multiLineCommentRegex = "/\\*.*?\\*/";

        // Appliquer les styles
        applyStyleToText(doc, keywordRegex, keywordStyle, Pattern.MULTILINE);
        applyStyleToText(doc, stringRegex, stringStyle, Pattern.MULTILINE);
        applyStyleToText(doc, numberRegex, numberStyle, Pattern.MULTILINE);
        applyStyleToText(doc, singleLineCommentRegex, commentStyle, Pattern.MULTILINE);
        applyStyleToText(doc, multiLineCommentRegex, commentStyle, Pattern.DOTALL | Pattern.MULTILINE);

        lastHighlightedText = jTextPaneCode.getText();
    }

    private static void applyStyleToText(StyledDocument doc, String regex, Style style, int flags) {
        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }

        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            doc.setCharacterAttributes(start, end - start, style, false);
        }
    }

    private void changeDisplayMode() {
        //StyledDocument doc = textPane.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();

        Style keywordStyle = sc.getStyle("KeywordStyle");
        Style stringStyle = sc.getStyle("StringStyle");
        Style numberStyle = sc.getStyle("NumberStyle");
        Style commentStyle = sc.getStyle("CommentStyle");

        if (darkMode) {
            // Mode sombre
            setBackground(Color.DARK_GRAY);
            jTextAreaLogs.setForeground(Color.WHITE);
            jTextAreaLogs.setBackground(Color.BLACK);

            jTextPaneCode.setBackground(Color.BLACK);
            jTextPaneCode.setForeground(Color.WHITE);
            jTextPaneCode.setCaretColor(Color.WHITE); // Changer la couleur du curseur en blanc pour le mode sombre

            StyleConstants.setForeground(keywordStyle, Color.CYAN);
            StyleConstants.setForeground(stringStyle, Color.ORANGE);
            StyleConstants.setForeground(numberStyle, new Color(0, 200, 0)); // Vert clair
            StyleConstants.setForeground(commentStyle, Color.LIGHT_GRAY);
        } else {
            // Mode clair
            setBackground(Color.WHITE);
            jTextAreaLogs.setForeground(Color.BLACK);
            jTextAreaLogs.setBackground(Color.WHITE);

            jTextPaneCode.setBackground(Color.WHITE);
            jTextPaneCode.setForeground(Color.BLACK);
            jTextPaneCode.setCaretColor(Color.BLACK); // Changer la couleur du curseur en blanc pour le mode sombre

            StyleConstants.setForeground(keywordStyle, Color.BLUE);
            StyleConstants.setForeground(stringStyle, Color.RED);
            StyleConstants.setForeground(numberStyle, new Color(0, 128, 0)); // Vert
            StyleConstants.setForeground(commentStyle, Color.GRAY);
        }

        // Recoloriser la syntaxe après le changement de mode
        SwingUtilities.invokeLater(() -> {
            geomViewer.updateContent(result, darkMode);
            // Appliquer la coloration syntaxique
            highlightSyntax(jTextPaneCode);
            jTextAreaLogs.invalidate();
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

        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneCode = new javax.swing.JTextPane();
        jSplitPaneRight = new javax.swing.JSplitPane();
        jScrollPaneLogs = new javax.swing.JScrollPane();
        jTextAreaLogs = new javax.swing.JTextArea();
        jScrollPaneGraph = new javax.swing.JScrollPane();
        jPanelBottom = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButtonZoomMinus = new javax.swing.JButton();
        jCheckBoxDark = new javax.swing.JCheckBox();
        jButtonZoomPlus = new javax.swing.JButton();
        jLabelSpace = new javax.swing.JLabel();
        jButtonView = new javax.swing.JButton();
        jButtonDone = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        jLabel1.setText("        ");

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(500);

        jTextPaneCode.setFont(new java.awt.Font("Courier 10 Pitch", 0, 15)); // NOI18N
        jTextPaneCode.setToolTipText("See OpenSCAD for the documentation but in 2D only.\n\nTo fix name of generate shape, use:  color(\"red:my_fixed_name\") ...");
        jTextPaneCode.setPreferredSize(new java.awt.Dimension(640, 640));
        // Créer un composant pour les numéros de ligne
        lineNumberComponent = new JTextPaneLineNumber(jTextPaneCode);

        // Ajouter le composant des numéros de ligne au JScrollPane
        jScrollPane1.setRowHeaderView(lineNumberComponent);
        jScrollPane1.setViewportView(jTextPaneCode);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jSplitPaneRight.setDividerLocation(550);
        jSplitPaneRight.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTextAreaLogs.setColumns(30);
        jTextAreaLogs.setRows(3);
        jTextAreaLogs.setTabSize(3);
        jTextAreaLogs.setWrapStyleWord(true);
        jScrollPaneLogs.setViewportView(jTextAreaLogs);

        jSplitPaneRight.setBottomComponent(jScrollPaneLogs);

        jScrollPaneGraph.setViewportView(geomViewer);

        jSplitPaneRight.setTopComponent(jScrollPaneGraph);

        jSplitPane1.setBottomComponent(jSplitPaneRight);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jButton1.setText("Help");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButton1);

        jLabel2.setText("     ");
        jPanelBottom.add(jLabel2);

        jButtonZoomMinus.setText("+");
        jButtonZoomMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonZoomMinusActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButtonZoomMinus);

        jCheckBoxDark.setText("Dark");
        jCheckBoxDark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDarkActionPerformed(evt);
            }
        });
        jCheckBoxDark.setSelected(darkMode);
        jPanelBottom.add(jCheckBoxDark);

        jButtonZoomPlus.setText("-");
        jButtonZoomPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonZoomPlusActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButtonZoomPlus);

        jLabelSpace.setText("           ");
        jPanelBottom.add(jLabelSpace);

        jButtonView.setText("View");
        jButtonView.setToolTipText("or use F5 to generate result");
        jButtonView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButtonView);

        jButtonDone.setText("Generate");
        jButtonDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDoneActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButtonDone);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelBottom.add(jButtonCancel);

        add(jPanelBottom, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDarkActionPerformed
        darkMode = jCheckBoxDark.isSelected();
        changeDisplayMode();
    }//GEN-LAST:event_jCheckBoxDarkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        listener.actionPerformed(new ActionEvent(jButtonCancel, 0, "btCancel"));
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDoneActionPerformed
        listener.actionPerformed(new ActionEvent(jButtonDone, 1, "btOk"));
    }//GEN-LAST:event_jButtonDoneActionPerformed

    private void jButtonViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewActionPerformed
        runCode(jTextPaneCode.getText());
        jTextPaneCode.requestFocus();
    }//GEN-LAST:event_jButtonViewActionPerformed

    private void jButtonZoomMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonZoomMinusActionPerformed
        Font f = jTextPaneCode.getFont();
        jTextPaneCode.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 1));
    }//GEN-LAST:event_jButtonZoomMinusActionPerformed

    private void jButtonZoomPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonZoomPlusActionPerformed
        Font f = jTextPaneCode.getFont();
        if (f.getSize() > 5)
            jTextPaneCode.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));
    }//GEN-LAST:event_jButtonZoomPlusActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (jHelpFrame == null) {
            jHelpFrame = new JFrame("Scad2D Language reference");
            // Set the content type to HTML
            JTextPane textPane = new JTextPane();
            textPane.setContentType("text/html");

            // Set the editor kit to handle HTML content
            textPane.setEditorKit(new HTMLEditorKit());
            textPane.setText(HELP_STRING);
            jHelpFrame.add(new JScrollPane(textPane));
            jHelpFrame.pack();
        }
        jHelpFrame.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDone;
    private javax.swing.JButton jButtonView;
    private javax.swing.JButton jButtonZoomMinus;
    private javax.swing.JButton jButtonZoomPlus;
    private javax.swing.JCheckBox jCheckBoxDark;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelSpace;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneGraph;
    private javax.swing.JScrollPane jScrollPaneLogs;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPaneRight;
    private javax.swing.JTextArea jTextAreaLogs;
    private javax.swing.JTextPane jTextPaneCode;
    // End of variables declaration//GEN-END:variables
}
