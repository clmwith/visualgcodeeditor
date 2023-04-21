/*
 * Copyright (C) 2023 moi
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
package gcodeeditor.gui.dialogs;

import java.awt.Color;
import javax.swing.JTextField;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Used to simplify GUI dialogs managments
 * 
 * @author clement
 */
public abstract class ManagedPanel extends javax.swing.JPanel {
    String title;
    DialogManager dialog;

    public ManagedPanel( String title0) {
        title = title0;
    }
    
    public static boolean isNaN( double value) {
        return Double.isNaN(value);
    }
    
    /**
     * @param value
     * @return true if <i>value</i> == Integer.MAX_VALUE
     */
    public static boolean isNaN( int value) {
        return (value == Integer.MAX_VALUE);
    }
    
    /**
     * Verify that content of the field is a number.
     * 
     * @param field 
     * @param optional set to true to allow empty/optional value (that will return Double.NEGATIV_INFINITY)
     * @return Double.NEGATIVE_INFINITY, NaN, or the value
     */
    public static double containsValidNumber( JTextField field, boolean optional) {

        String val = field.getText();
        
        field.setForeground(Color.black);
        if ( val.isBlank()) {
            if ( optional ) return Double.NEGATIVE_INFINITY;
            else val ="err";
        }
        
        double n =  isValidExpression( val);
        if ( Double.isNaN(n) && ! val.isBlank()) field.setForeground(Color.red);
        
        return n;
    }    
    
    /**
     * Verify that content of the field is a int
     * @param field 
     * @param optional set to true to allow empty/optional value (that will return Integer.MIN_VALUE)
     * @return the truncated int value or Integer.MIN_VALUE, or Integer.MAX_VALUE if not a number
     */
    public static int containsValidInteger( JTextField field, boolean optional) {

        field.setForeground(Color.black);
        if ( optional && field.getText().isBlank()) return Integer.MIN_VALUE;
        
        double n = isValidExpression( field.getText());
        if ( Double.isNaN(n)) {
            field.setForeground(Color.red);
            return Integer.MAX_VALUE;
        }
        
        field.setText( Integer.toString((int)n));
        return (int)n;
    }
    
    /**
     * Verify that the value is a number or an expression
     * @param field 
     * @return the result of expression or NaN if wrong
     */
    public static double isValidExpression( String value) {
        try {
            return new ExpressionBuilder(value).variables("pi").build().setVariable("pi", Math.PI).evaluate();
            //return Double.parseDouble(value);
        } catch ( Exception e) {
            System.out.println("err:"+e);
            return Double.NaN;
        }
    }
    
    /**
     * Verify that the value is a int
     * @param field 
     * @return Integer.MAX_VALUE if wrong number
     */
    public static int isValidInteger( String value) {
        try {
            return Integer.parseInt(value);
        } catch ( Exception e) { 
            
            return Integer.MAX_VALUE;
        }       
    }
    
    /**
     * Used when 'ok' button selected to verify all parameters
     * @return true if all values are ok
     */
    abstract public boolean validateFields();        

    /**
     * Called by a DialogManager before displaying the panel
     * @param param 
     */
    void setParam(Object param) { }
}
