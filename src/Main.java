/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import gcodeeditor.JBlocksViewer;
import gcodeeditor.gui.JEditorFrame;
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
            
            //JEditorFrame f = new JEditorFrame("/home/clm/lamborghini.gcode");
            JEditorFrame f;
            try {
            if ( args.length > 1) {
                
                    f = new JEditorFrame(args[1]);
                    f.setVisible(true);
               
            } else {
                f = new JEditorFrame(false, true);
               // f.addGElement(JBlocksViewer.importSVG("/tmp/dessin.svg"));
                //f.addGElement(JBlocksViewer.importGCODE2("/home/clm/Documents/Perso/Créations/Imprimante3D/CNC_Fraisage/piecesV4/gabari_percage_3axes.gcode")); 
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
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            //f.addGElement( GBlock.makeCircle(new Point2D.Double(0,0), 24, 30));
            // try {
            
            /*     GBlock s = new GBlock("triangle");
            s.addGElement( new Point(10, 10, 0));
            s.addGElement( new Point(100, 30, 0));
            s.addGElement( new Point(50, 100, 0));
            s.addGElement( new Point(10, 10, 0));
            f.addGElement( s);*/
            //f.addGElement( GBlock.getPathForText(new java.awt.Font("Arial-10", 0, 30), "Super", new GCLine(10,10)));
            /*      try {
            // f.addGElement(shapeList);
            
            f.blocksviewer.importDXF("/home/clm/Documents/Perso/Machine_CNC/Laser/Creations/plateau.dxf");
            
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
