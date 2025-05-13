package pmedit.ui;

import org.netbeans.jemmy.ComponentChooser;

import javax.swing.*;
import java.awt.*;

public class CustomPropertyComponentChooser implements ComponentChooser {
    final String propertyName;
    final String propertyValue;
    CustomPropertyComponentChooser(String name, String value){
        propertyName = name;
        propertyValue = value;
    }
    @Override
    public boolean checkComponent(Component comp) {
        if(comp instanceof JComponent jc){
            Object pv = jc.getClientProperty(propertyName);
            if(pv instanceof String s){
                return s.equals(propertyValue);
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
            return("JComponent with custom property with ['" + propertyName + "'] = '" + propertyValue + "' accessible description");
    }
}
