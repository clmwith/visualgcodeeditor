package gelements;

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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * manage Undo/Redo records of the modifications of a document.
 * @author Clément Gérardin @ Marseille.fr
 */
public class UndoManager {
    
    /** [0 ... undo records  ... <undoStackPosition> ... redo record ... undoStack.size()] */
    ArrayList<UndoRecord> undoStack;
    /** The current stack position (for redo) */
    int undoStackPosition = 0;
    
    public UndoManager() {
        undoStack = new ArrayList<>(100);
    }

    /**
     * Erase all the content of this stack.
     */
    public void clear() {
        undoStack.clear();
        undoStackPosition = 0;
    }

    public boolean canUndo() {
        return undoStackPosition > 1;
    }
    
    public boolean canRedo() {
        return undoStackPosition < undoStack.size();
    }
    
    /**
     * Get all modified elements of this group and erases their modified flag.
     * @param document
     * @param modified all modified element are in this group
     */
    private void getModified(GGroup group, ArrayList<GElement> modified, boolean onlyClear) {
        if ( group.modified) {
            group.modified = false;
            if ( ! onlyClear) 
                modified.add(group);
            
            group.getAll().forEach((e) -> {  
                if ( e instanceof GGroup) 
                    getModified((GGroup)e, null, true);
                e.modified = false;
            });
        } else
            group.getAll().forEach((e) -> {  
                if ( e instanceof GGroup)
                    getModified((GGroup)e, modified, onlyClear);
                else 
                    if ( e.modified) {
                        if ( ! onlyClear) 
                            modified.add(e);
                        
                    }
                e.modified = false;    
            });
    }
    
    /**
     * Apply last undo record on the document
     * @param document
     * @return the edited element or group at save time
     */
    public GElement undo(GGroup document) {
        GElement res = null;
        if ( canUndo()) { 
            UndoRecord prev = undoStack.get(--undoStackPosition); 
            System.out.println("UndoRec="+prev);
            System.out.println("undo stack["+undoStackPosition+" / "+ undoStack.size()+"]");
            System.out.flush();
            
            // full restoration of the document
            if ( (prev.modified.length==1) && (prev.modified[0].getID() == document.getID())) {     
                document.clear();
                ((GGroup)prev.modified[0]).getAll().forEach((l) -> { document.elements.add((GElement)l.cloneWithSameID()); });
                clearModifiedFlagOn(document);
                return document;
            }
            
            // restore previous version of what have changed at last backup
            for( GElement m : prev.modified) {
                GElement last = findLastVersionOf(m.getID());
                if ( last == null) {
                    // that was a new element, we remove it
                    last = document.getElementID(m.getID()); 
                    GGroup g = document.getParent(last);
                    g.remove( last);
                    g.modified = false;
                    return g;
                } else {
                    if ( last instanceof GGroup) {
                        // restore all modified element of this group since his last backup
                        restoreLastStateOfElementsOf((GGroup)last);
                    }
                    GGroup g = document.getParent(document.getElementID(last.getID()));
                    if ( g == null) {
                        // no parent, that is the root group
                        document.clear();
                        if ( last instanceof GGroup)
                            document.addAll(((GGroup)last).cloneWithSameID().getAll());
                        else
                            document.add( last.cloneWithSameID());
                        
                        document.modified = false;
                        res = document;
                    } else {
                        g.remplace(last);
                        g.modified = false;
                        res = g;
                    }   
                    clearModifiedFlagOn(res);
                }
            }
            return document.getElementID(prev.editedElement);
        }
        return null;
    }
    
    /**
     * Apply undo on the document
     * @param document
     * @return the edited element or group at save time
     */
    public GElement redo(GGroup document) {
        if ( canRedo()) {                
            UndoRecord rec = undoStack.get(undoStackPosition++);
            System.out.println("RedoRec="+rec);
            System.out.println("redo stack["+undoStackPosition+" / "+ undoStack.size()+"]");
            System.out.flush();
            
            for ( GElement e : rec.modified)
                updateElement(document, e);
            
            clearModifiedFlagOn(document);
            return document.getElementID(rec.editedElement);
        }
        return null;
    }
    
    /**
     * Return the newer version of a element on the stack.
     * @param id the ID of the element to find
     * @return a clone of the element or null if not found.
     */
    private GElement findLastVersionOf(int id) {
        //GElement e;
        int recNum = undoStackPosition;
        while ( recNum > 0) {
            recNum--;
            UndoRecord r = undoStack.get(recNum);
            for( GElement e : r.modified) {
                if ( e.getID() == id)
                    return e.cloneWithSameID();
                else 
                    if ( e instanceof GGroup) {
                        GElement el = ((GGroup)e).getElementID(id);
                        if (el != null) 
                            return el.cloneWithSameID();
                    }
            }
        }
        return null;
    }
    
    /**
     * Save all modifications of the document into the stack if needed.
     * @param document
     * @param editedElement 
     */
    public void saveState( GGroup document, int editedElement) {
        while( undoStackPosition < undoStack.size()) {
            undoStack.remove(undoStack.size()-1);
        }
        ArrayList<GElement> modified = new ArrayList<>();
        getModified(document, modified, false);
        if ( modified.isEmpty()) return;
        
        UndoRecord r = new UndoRecord(modified.size());
        int i = 0;
        for( GElement e : modified) {
            r.modified[i++] = e.cloneWithSameID();           
        }

        r.editedElement = editedElement;
        System.out.println("Add: " + r);
        undoStack.add(r);
        undoStackPosition++;
        
        System.out.println("stack["+undoStackPosition+" / "+ undoStack.size()+"]");
        System.out.flush();
        StackTraceElement[] stackTrace = Thread.getAllStackTraces().get(Thread.currentThread());
        for( i = 2; i < 6; i++) System.err.println(stackTrace[i]);
        System.err.println();
        System.err.flush();
    }

    /**
     * Eventualy update newE into the <i>document</i>.
     * @param document
     * @param newE
     * @return 
     */
    private GElement updateElement(GGroup document, GElement newE) {
        GElement old = document.getElementID(newE.getID());
        if ( old == null){
            System.err.println("updateElement: newE is not in this group");
            return null;
        }
        GGroup p = (GGroup)document.getParent(old);
        if ( p == null) {
            // thas is the root group
            document.clear();
            document.addAll( ((GGroup)newE).cloneWithSameID().getAll());
            document.modified = false;
            return document;
        } else {
            int i = p.indexOf(old);
            p.remove(old);
            p.add(i, old=newE.cloneWithSameID()); 
            p.modified = false;
            return old;
        } 
    }

    private void clearModifiedFlagOn(GElement el) {
        if ( el instanceof GGroup) {
            ((GGroup)el).elements.forEach((e) -> { clearModifiedFlagOn(e); });
        }
        el.modified = false;
    }

    private void restoreLastStateOfElementsOf(GGroup group) {
        HashMap<Integer, GElement> mostRecents = new HashMap<>();
        int i, recNum = undoStackPosition;
        
        // retreive all modifications into 'group' since his last recording
        while ( recNum > 0) {
            recNum--;
            UndoRecord r = undoStack.get(recNum);
            for( GElement e : r.modified) {
                if ( (e.id != group.id) && ( group.getElementID(e.id) != null)) {
                    if ( mostRecents.get(e.id)==null) {
                        mostRecents.put(e.id, e);
                        if ( e instanceof GGroup)
                            restoreLastStateOfElementsOf((GGroup)e);
                    }

                } else {
                    // apply modifications on 'group'
                    mostRecents.keySet().forEach((id) -> {
                        updateElement(group, mostRecents.get(id));
                    });
                    return;
                }
            }
        } 
        
    }
        
    class UndoRecord {  
        /*
        public static final int FULL_DUMP_RECORD = 0;
        public static final int ADDED_RECORD = 1;
        public static final int MODIFIED_RECORD = 2;
        public static final int REMOVED_RECORD = 3;
        int recordType;
        ArrayList<GElement> addedElements;
        ArrayList<int> modifiedOrRemovedIds;   
        */

        GElement modified[];
        int editedElement;

        public UndoRecord(int size) {
            modified = new GElement[size];
        }

        @Override
        public String toString() {
            String res = "UndoRecord[";
            for( GElement e : modified)
                res += e.toString() + ",";
            return res + "] edit="+ editedElement + "}";
        }
    }

    
}
