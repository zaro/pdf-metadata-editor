package pmedit.ui;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.accessibility.AccessibleDescriptionChooser;
import org.netbeans.jemmy.operators.*;
import pmedit.DateFormat;
import pmedit.MetadataInfo;
import pmedit.annotations.FieldDataType;
import pmedit.ui.components.DateTimeList;
import pmedit.ui.components.DateTimePicker;
import pmedit.ui.components.MetadataFormComponent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UiTestHelpers {
    static void openFileChooser(String dialogTitle, File testFile){
        var chooserFrame = new JDialogOperator(dialogTitle);
        var fileChooser = new JFileChooserOperator(
                JFileChooserOperator.findJFileChooser((Container) chooserFrame.getSource()));
        fileChooser.setCurrentDirectory(testFile.getParentFile());
        fileChooser.setSelectedFile(testFile);
        new JButtonOperator(fileChooser, "Open").push();
    }
    static void saveFileChooser(String dialogTitle, File testFile){
        var chooserFrame = new JDialogOperator(dialogTitle);
        var fileChooser = new JFileChooserOperator(
                JFileChooserOperator.findJFileChooser((Container) chooserFrame.getSource()));
        fileChooser.setCurrentDirectory(testFile.getParentFile());
        fileChooser.setSelectedFile(testFile);
        new JButtonOperator(fileChooser, "Save").push();
    }

    static void ensureTab(JTabbedPaneOperator tab, String findInTabTitle){
        int tabIndex = tab.findPage(findInTabTitle);
        if(tabIndex < 0){
            fail("Can't find "+ findInTabTitle +" in any tab");
        }
        if(tabIndex != tab.getSelectedIndex()){
            tab.setSelectedIndex(tabIndex);
        }
    }

    static void checkMetadataPaneValues(ContainerOperator frame,MetadataInfo expected){
        var tab = new JTabbedPaneOperator(frame);

        for(String k: MetadataInfo.keys()) {
            String[] parts = k.split("\\.");
            if(parts[0].startsWith("file")){
                // Ignore file.* props, they are not shown
                continue;
            }
            String findInTt = parts[0].equals("dc") ? "Dublin" : parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
            ensureTab(tab, findInTt);
            checkMetadataFieldValue(frame, k, expected);
        }
    }

    static void checkMetadataFieldValue(ContainerOperator frame, String name, MetadataInfo expected){
        Component c = ComponentOperator.findComponent((Container) frame.getSource(), new CustomPropertyComponentChooser("MetadataFieldId", name));
        if(c == null){
            fail("Checking "+ name + " - no component found");
        }
        String message = "Values differ on '" + name + "' ["+ expected.file.nameWithExt + "]";
        if(c instanceof JTextField tf ) {
            assertEquals(expected.getString(name, ""), tf.getText(), message);
        } else if(c instanceof JTextArea ta ) {
            assertEquals(expected.getString(name, ""), ta.getText(), message);
        } else if(c instanceof DateTimePicker dt ) {
            assertEquals(expected.getString(name, null), DateFormat.formatDateTime(dt.getCalendar()), message);
        } else if(c instanceof JComboBox cb ) {
            MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(name);
            if(fd.type == FieldDataType.FieldType.BoolField){
                String s = "Unset";
                Boolean v  = (Boolean) expected.get(name, null);
                if(v != null){
                  s = v ? "Yes"  :"No" ;
                }
                assertEquals(s, cb.getSelectedItem(), message);
            } else {
                assertEquals(expected.getString(name, "Unset"), cb.getSelectedItem(), message);
            }
        } else {
            // Handle form components
            boolean found = false;
            if(c instanceof JPanel p) {
                Object owner = p.getClientProperty(MetadataFormComponent.OWNER_PROPERTY);
                if(owner != null){
                    if(owner instanceof DateTimeList dtl){
                        found = true;
                        MetadataInfo tmp = new MetadataInfo();
                        tmp.dc.dates = dtl.getCalendarList();
                        assertEquals(expected.getString(name, null), tmp.getString("dc.dates", null), message);
                    }
                }
            }
            if(!found) {
                fail("Unknown component:" + c.getClass());
            }
        }
    }

    static void populateMetadataPaneValues(ContainerOperator frame, MetadataInfo info) {
        populateMetadataPaneValues(frame, info, false);
    }

    static void populateMetadataPaneValues(ContainerOperator frame, MetadataInfo info, boolean setEnabled){
        var tab = new JTabbedPaneOperator(frame);

        for(String k: MetadataInfo.keys()) {
            String[] parts = k.split("\\.");
            if(parts[0].startsWith("file")){
                // Ignore file.* props, they are not shown
                continue;
            }

            String findInTt = parts[0].equals("dc") ? "Dublin" : parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
            ensureTab(tab, findInTt);
            if(!info.isEnabled(k)){
                if(setEnabled) {
                    populateEnabled(tab, k, false);
                }
                continue;
            }
            populateMetadataPaneValue(tab, k, info);
            if(setEnabled){
                populateEnabled(tab, k, info.isEnabled(k));
            }
        }
    }
    static void populateEnabled(JTabbedPaneOperator tab, String name, boolean isEnabled) {
        JCheckBox c = (JCheckBox)ComponentOperator.findComponent((Container) tab.getSource(), new CustomPropertyComponentChooser("EnabledMetadataFieldId", name));
        c.setSelected(isEnabled);
    }

    static void populateMetadataPaneValue(JTabbedPaneOperator tab, String name, MetadataInfo expected){
        Component c = ComponentOperator.findComponent((Container) tab.getSource(), new CustomPropertyComponentChooser("MetadataFieldId", name));
        if(c == null){
            fail("Populating "+ name + " - no component found");
        }
        if(c instanceof JTextComponent tf ) {
            tf.setText(expected.getString(name, ""));
        } else if(c instanceof DateTimePicker dt ) {
            dt.setCalendar((Calendar) expected.get(name));
        } else if(c instanceof JComboBox cb ) {
            MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(name);
            if(fd.type == FieldDataType.FieldType.BoolField){
                String s = "Unset";
                Boolean v  = (Boolean) expected.get(name, null);
                if(v != null){
                    s = v ? "Yes"  :"No" ;
                }
                cb.setSelectedItem(s);
            } else {
                cb.setSelectedItem(expected.getString(name, "Unset"));
            }
        } else {
            // Handle form components
            boolean found = false;
            if(c instanceof JPanel p) {
                Object owner = p.getClientProperty(MetadataFormComponent.OWNER_PROPERTY);
                if(owner != null){
                    if(owner instanceof DateTimeList dtl){
                        found = true;
                        dtl.setCalendarList((List<Calendar>) expected.get(name));
                    }
                }
            }
            if(!found) {
                fail("Unknown component:" + c.getClass());
            }

        }
    }

    public static ContainerOperator getPanelByTitle(ContainerOperator frame, String title){
        return new ContainerOperator(frame, new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                if (comp instanceof JPanel p) {
                    if (p.getBorder() instanceof TitledBorder tb) {
                        return title.equals(tb.getTitle());
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "JPanel with text \""+  title +"\" as border title";
            }
        });
    }

    public static void delay(long ms) {
        JemmyProperties.setCurrentTimeout("MyCustomDelay", ms); // 1 second
        JemmyProperties.getCurrentTimeouts().sleep("MyCustomDelay");
    }
}
