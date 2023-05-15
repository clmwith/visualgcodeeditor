/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import gcodeeditor.gui.JEditorFrame;
import gelements.GArc;
import gelements.GMixedPath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


/**
 *
 * @author Clément
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
          /* Set<Thread> threads = Thread.getAllStackTraces().keySet();
            
            for (Thread t : threads) {
            // do something with each thread
            System.out.println(t);
            }*/
            
            // TODO code application logic here
            // java.awt.EventQueue.invokeLater(() -> {
                               
            try {
            if ( System.getProperty("user.home").startsWith("/")) {
                java.io.File prefRep = new java.io.File(System.getProperty("user.home")+"/.java/.userPrefs");
                if (!prefRep.isDirectory()) prefRep.mkdir();
            }
            
            
            try {                
                if ( args.length > 0) {  
                    for( String arg : args) {
                        if ( arg.endsWith(".svg")) {
                            JEditorFrame f1 = new JEditorFrame(false, true);
                            f1.addGElement(f1.projectViewer.readSVGfile(arg)); 
                            EventQueue.invokeLater( new Runnable() {                                
                                @Override
                                public void run() { f1.setVisible(true); } });
                        } else if ( arg.endsWith(".dxf")) {
                            JEditorFrame f2 = new JEditorFrame(false, true);
                            f2.projectViewer.importDXF(arg);                        
                            EventQueue.invokeLater( new Runnable() {                                
                                @Override
                                public void run() { f2.setVisible(true); } });
                        } else {                    
                            JEditorFrame f3 = new JEditorFrame(arg);
                            EventQueue.invokeLater( new Runnable() {                                
                                @Override
                                public void run() { f3.setVisible(true); } });
                        }
                    }  
                } else {
                    JEditorFrame
                    f = new JEditorFrame(false, true);
                    //f.addGElement(GMixedPath.makeRounRect(200, 300, 30));
                    //f.addGElement( new GArc("arc", new GCode(0,0), 50, 45, -360));
                    //f.addGElement( new GArc("arc", new GCode(0,0), 50, 45, -150));
                    //f.addGElement( new GArc("arc", new GCode(100,100), 50, 45, 150));
                    //f.addGElement( JProjectEditor.importGCODE("/tmp/toto.gcode", null));
                    //f.addGElement(f.projectViewer.readSVGfile("/tmp/dessin.svg"));
                    //f.addGElement(f.projectViewer.readSVGfile("/tmp/tools-report-bug.svg")); // tools-report-bug
                    //f.addGElement(f.projectViewer.readSVGfile("/tmp/coeur2.svg"));
                    //f.addGElement(JProjectEditor.importGCODE2("/home/clm/Documents/Perso/Créations/Imprimante3D/CNC_Fraisage/piecesV4/gabari_percage_3axes.gcode")); 
                    //f.addGElement(GArc.makeBulge(new GCLine(1.5,0), new GCLine(0,1.5), -0.414214));
                    //new GArc("circle", new GCLine(1.5,1.5), 1.5, 90, 90));
                    /* LibreCadFont font;
                    try {
                    font = LibreCadFont.getFont(15);
                    f.addGElement( font.getTextPaths("e"));
                    } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }*/

                    EventQueue.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            f.setVisible(true);                             
                        }
                    });
                }
            } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error",  JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
            }
            //f.addGElement( GElement.makeCircle(new Point2D.Double(0,0), 24, 30));
            // try {
            
            /*     GElement s = new GElement("triangle");
            s.addGElement( new Point(10, 10, 0));
            s.addGElement( new Point(100, 30, 0));
            s.addGElement( new Point(50, 100, 0));
            s.addGElement( new Point(10, 10, 0));
            f.addGElement( s);*/
            //f.addGElement( GElement.getPathForText(new java.awt.Font("Arial-10", 0, 30), "Super", new GCLine(10,10)));
            /*      try {
            // f.addGElement(shapeList);
            
            f.projectViewer.importDXF("/home/clm/Documents/Perso/Machine_CNC/Laser/Creations/plateau.dxf");
            
            //f.shapeviewer.importDXF("/home/clm/Documents/Perso/Machine_CNC/#chevre.dxf");
            //f.shapeviewer.importDXF("/home/clm/Documents/Perso/Machine_CNC/Laser/Creations/soleil.dxf");
            
            } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
            
                
            } catch ( Exception e) {
                JOptionPane.showMessageDialog(null, "An exception occured in the programm, please see the console.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
   
     //   });
        
        /*
        Parser parser = ParserBuilder.createDefaultParser();
        try {
            parser.parse("/home/clm/Documents/Perso/Machine_CNC/Laser/Creations/soleil.dxf", DXFParser.DEFAULT_ENCODING);
        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        DXFDocument doc = parser.getDocument();
        
        for( Iterator<DXFLayer> i = doc.getDXFLayerIterator(); i.hasNext(); )
        {
            DXFLayer layer = i.next();
            
            for( Iterator e = layer.getDXFEntityTypeIterator(); e.hasNext();)
            {
                Object type = e.next();
                System.out.println("***" + type);
                List<DXFEntity> le = layer.getDXFEntities( type.toString());
                for( DXFEntity el : le)
                {
                    if ( el instanceof DXFLWPolyline) {
                        DXFLWPolyline poly = (DXFLWPolyline)el;
                        for( int n = 0; n < poly.getVertexCount(); n++)
                            System.out.println(poly.getVertex(n).getPoint());
                    } else {    
                        DXFLine line = (DXFLine)el;
                        System.out.println(line.getStartPoint());
                    }
                }
            }
            
            
        }
        
    //    DXFLayer layer = doc.getDXFLayer("layer_name");
    //    List<DXFCircle> arcs = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_);
*/
    }
       
    
}


