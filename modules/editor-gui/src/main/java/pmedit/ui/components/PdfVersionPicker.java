package pmedit.ui.components;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


public class PdfVersionPicker extends JComboBox<Float> {
    static List<Float> versions = List.of(1.3f, 1.4f, 1.5f, 1.6f, 1.7f);
    DefaultComboBoxModel<Float> model;

    public PdfVersionPicker(){

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Double) {
                    // Format the double value to round it to 2 decimal places
                    DecimalFormat df = new DecimalFormat("#.##");
                    setText(df.format((Double) value));
                }

                return renderer;
            }
        });

        model = new DefaultComboBoxModel<Float>() ;
        model.addAll(versions);
        setModel(model);
    }

    public void setVersion(Float version) {
        setSelectedItem(version);
    }

    public Float getVersion() {
        return (Float) model.getSelectedItem();
    }
}