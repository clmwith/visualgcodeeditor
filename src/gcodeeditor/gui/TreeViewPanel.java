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

import gelements.GElement;
import gelements.GGroup;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author moi
 */
public class TreeViewPanel extends javax.swing.JPanel {

    public class GElementTransferHandler extends TransferHandler {

        public static final DataFlavor GElementFlavor = new DataFlavor(TreePath[].class, "GElement TreePaths");

        // Sert à transporter les TreePath sélectionnés (en interne uniquement)
        public static class GElementTransferable implements Transferable {
            private final TreePath[] paths;

            public GElementTransferable(TreePath[] paths) {
                this.paths = paths;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{GElementFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return GElementFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) {
                if (isDataFlavorSupported(flavor)) return paths;
                return null;
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] selected = tree.getSelectionPaths();
            if (selected == null || selected.length == 0) return null;
            return new GElementTransferable(selected);
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) return false;
            if (!support.isDataFlavorSupported(GElementFlavor)) return false;

            // Vérifie que la cible est bien un GGroup
            JTree.DropLocation dropLoc = (JTree.DropLocation) support.getDropLocation();
            TreePath dropPath = dropLoc.getPath();
            if (dropPath == null) return false;

            Object node = dropPath.getLastPathComponent();
            //if (!(node instanceof DefaultMutableTreeNode)) return false;

            //Object userObj = ((DefaultMutableTreeNode) node).getUserObject();
            return (node instanceof gelements.GGroup);  // ✅ Seul un GGroup peut recevoir des enfants
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;

            try {
                JTree tree = (JTree) support.getComponent();
                Transferable t = support.getTransferable();
                TreePath[] draggedPaths = (TreePath[]) t.getTransferData(GElementFlavor);

                TreePath dropPath = ((JTree.DropLocation) support.getDropLocation()).getPath();
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();

                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

                for (TreePath path : draggedPaths) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                    if (!node.isNodeAncestor(targetNode)) {
                        model.removeNodeFromParent(node);
                        model.insertNodeInto(node, targetNode, targetNode.getChildCount());
                    }
                }

                tree.expandPath(dropPath);
                return true;

            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    public interface JTreeChangeListenner {
        public void selectionHasChanged( GElement newSel);
    }
    ArrayList<JTreeChangeListenner> listenners = new ArrayList<>();
    
    JProjectEditorPanel jProjectEditor;    
    
    boolean discareChangesNotification;
    
    public TreeViewPanel( JProjectEditorPanel content) {
        jProjectEditor = content;
        initComponents();
        
        jTree.setDragEnabled(true);
        jTree.setDropMode(DropMode.ON_OR_INSERT);
        jTree.setTransferHandler(new GElementTransferHandler());

        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        jTree.addTreeSelectionListener(e -> {
            TreePath[] paths = jTree.getSelectionPaths();

            if (paths == null || paths.length <= 1) return;

            TreePath parentPath = paths[0].getParentPath();
            boolean sameParent = true;

            for (TreePath path : paths) {
                if (!path.getParentPath().equals(parentPath)) {
                    sameParent = false;
                    break;
                }
            }

            if (!sameParent) {
                // Invalide la sélection multiple : garde le premier seul
                jTree.setSelectionPath(paths[0]);
            }
        });

        
        // Tree selection listener
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                
                TreePath[] paths = jTree.getSelectionPaths();
                if ( paths != null) { 
                    for ( Object o : paths[0].getPath())
                        System.out.print(o + " => ");
                    System.out.println( " : " + paths[0].getLastPathComponent());
                }
                
                Object selectedNode = paths[0].getLastPathComponent();
                if ( ! discareChangesNotification && (selectedNode != null)) {
                    if ( selectedNode instanceof GElement)
                    for ( JTreeChangeListenner l : listenners) 
                        l.selectionHasChanged((GElement)selectedNode);
                }
            }            
        });
    }
    
    public ArrayList<TreePath> getOpennedPath() {
    ArrayList<TreePath> openedPaths = new ArrayList<>();

    for (int i = 0; i < jTree.getRowCount(); i++) {
        if (jTree.isExpanded(i)) {
            TreePath path = jTree.getPathForRow(i);
            if (path != null) {
                openedPaths.add(path);
            }
        }
    }

    return openedPaths;
}

    
    public void updateContent( GGroup document, GGroup editedGroup, GElement editedElement, ArrayList<GElement> selectedElements) {
        // save openned tree state
        ArrayList<TreePath> expandedPaths = getOpennedPath();
        /*Set<TreePath> expandedPaths = new HashSet<>();
        for (int i = 0; i < jTree.getRowCount(); i++) {
        if (jTree.isExpanded(i)) {
        expandedPaths.add(jTree.getPathForRow(i));
        }
        }*/
        
        // update model        
        jTree.setModel( new DefaultTreeModel( buildTreeNodes(document)));
        
        // reopen tree
        for ( TreePath p : expandedPaths) {
            jTree.scrollPathToVisible(p);
        }
        
        // update selection
        discareChangesNotification = true;
        for ( GElement e : selectedElements ) {
            
            TreePath p2 = findPathForElement( e, new TreePath(jProjectEditor));
            if (p2 != null) {
                SwingUtilities.invokeLater( () -> {
                    jTree.scrollPathToVisible(p2);
                    jTree.setSelectionPath(p2);
                });          
                
            }
        }
        discareChangesNotification = false;

        jTree.invalidate();
        revalidate();
       
    }
    
    public void setSelection( ArrayList<GElement> newSel) {
        if (newSel == null || newSel.isEmpty()) return;

        GElement target = newSel.get(0); // On sélectionne uniquement le premier

        TreePath path = findPathForElement(target, null);
        if (path != null) {
            jTree.setSelectionPath(path);
            jTree.scrollPathToVisible(path);
        }
    }
    
    private TreePath findPathForElement(GElement target, TreePath parent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        Object userObject = node.getUserObject();

        if (userObject instanceof GElement && userObject.equals(target)) {
            return parent;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = parent.pathByAddingChild(child);
            TreePath result = findPathForElement(target, childPath);
            if (result != null) {
                return result;
            }
        }

        return null; // Non trouvé
    }

    
    public void addListenner( JTreeChangeListenner l) {
        listenners.add(l);
    }
    
    public void removeListenner( JTreeChangeListenner l) {
        listenners.remove(l);
    }
    
    private DefaultMutableTreeNode buildTreeNodes(GElement el) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(el);
        if ( el instanceof GGroup)
                for (GElement child : ((GGroup)el).getAll()) {
                    node.add(buildTreeNodes(child));
                }
        
        return node;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTree = new javax.swing.JTree( jProjectEditor.getTreeModel());

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(jTree);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree;
    // End of variables declaration//GEN-END:variables
}
