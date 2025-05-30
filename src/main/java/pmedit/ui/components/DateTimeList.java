package pmedit.ui.components;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateTimeList extends MetadataFormComponent {
    public JList<Calendar> calendarList;
    public JPanel topPanel;
    public JButton clearAllButton;
    public JButton addNewButton;
    public DateTimePicker dateTimePicker;
    private DefaultListModel<Calendar> listModel;

    public DateTimeList() {
        listModel = new DefaultListModel<>();
        calendarList.setModel(listModel);
        calendarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        calendarList.setCellRenderer(new DateTimeList.CalendarListCellRenderer(this));
        calendarList.setVisibleRowCount(-1);

        addNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewDate();
            }
        });
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllDates();
            }
        });

        dateTimePicker.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateSelectedCalendarFromDateChooser();
            }
        });

        // Add selection listener to update the date chooser
        calendarList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDateChooserFromSelection();
            }
        });

        // Add mouse listener to handle button clicks in the list
        calendarList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = calendarList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = calendarList.getCellBounds(index, index);
                    DateTimeList.CalendarListCellRenderer renderer = (DateTimeList.CalendarListCellRenderer) calendarList.getCellRenderer();
                    Component comp = renderer.getListCellRendererComponent(calendarList,
                            listModel.getElementAt(index), index, false, false);

                    // Check if click is within the remove button area (last 30 pixels of the cell)
                    if (e.getX() > cellBounds.x + cellBounds.width - 30) {
                        removeCalendarAt(index);
                    }
                }
            }
        });
    }

    public void addDataChangeLister(ListDataListener l) {
        listModel.addListDataListener(l);
    }

    private void addNewDate() {
        Calendar newCalendar = Calendar.getInstance();
        listModel.addElement(newCalendar);

        // Select the newly added item
        int newIndex = listModel.getSize() - 1;
        calendarList.setSelectedIndex(newIndex);
        calendarList.ensureIndexIsVisible(newIndex);

    }

    private void removeCalendarAt(int index) {
        if (index >= 0 && index < listModel.getSize()) {
            listModel.removeElementAt(index);

            // Select next item or previous if we removed the last
            if (listModel.getSize() > 0) {
                int newSelection = Math.min(index, listModel.getSize() - 1);
                calendarList.setSelectedIndex(newSelection);
            } else {
                dateTimePicker.setDate(null);
            }
        }
    }

    private void updateSelectedCalendarFromDateChooser() {
        int selectedIndex = calendarList.getSelectedIndex();
        Calendar c = dateTimePicker.getCalendar();
        if (selectedIndex >= 0 && c != null) {
            listModel.set(selectedIndex, c);
        }
    }

    private void updateDateChooserFromSelection() {
        Calendar selected = calendarList.getSelectedValue();
        if (selected != null) {
            dateTimePicker.setDate(selected.getTime());
        }
    }

    // Public methods to access the calendar list
    public List<Calendar> getCalendarList() {
        List<Calendar> result = new ArrayList<Calendar>();
        for (int i = 0; i < listModel.getSize(); i++) {
            result.add((Calendar) listModel.getElementAt(i).clone());
        }
        return result;
    }

    public void setCalendarList(List<Calendar> newCalendarList) {
        listModel.clear();
        if (newCalendarList != null) {
            for (Calendar cal : newCalendarList) {
                listModel.addElement((Calendar) cal.clone());
            }
            if (listModel.getSize() > 0) {
                calendarList.setSelectedIndex(0);
            }
        }
    }

    public void clearAllDates() {
        listModel.clear();
        dateTimePicker.setDate(null);
    }

    @Override
    public Container getContainer() {
        return topPanel;
    }

    @Override
    public void initMetadataFieldId(String id) {
        topPanel.putClientProperty("MetadataFieldId", id);
        topPanel.putClientProperty(MetadataFormComponent.OWNER_PROPERTY, this);
    }

    private static class CalendarListCellRenderer extends DefaultListCellRenderer {
        private static final SimpleDateFormat formatter =
                new SimpleDateFormat(DateTimePicker.DATE_TIME_FORMAT);
        private DateTimeList parent;

        public CalendarListCellRenderer(DateTimeList parent) {
            this.parent = parent;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Calendar) {
                Calendar cal = (Calendar) value;
                String formattedDate = formatter.format(cal.getTime());

                // Create the text with the remove button
                String displayText = (index + 1) + ". " + formattedDate;
                setText(displayText);
                setToolTipText("Click on [−] to remove this date, or click elsewhere to edit");

                // Set font to monospace for better alignment of the remove button
//                setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            }

            return this;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            // Draw the remove button area with a slight background
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                int buttonWidth = 25;
                int buttonX = getWidth() - buttonWidth - 5;
                int buttonY = 2;
                int buttonHeight = getHeight() - 4;

                // Set rendering hints for better appearance
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw button background
                if (isMouseOverRemoveButton()) {
                    g2.setColor(new Color(255, 100, 100, 100)); // Light red on hover
                } else {
                    g2.setColor(new Color(200, 200, 200, 50)); // Light gray
                }
                g2.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 4, 4);

                // Draw button border
                g2.setColor(Color.GRAY);
                g2.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 4, 4);

                // Draw the [−] symbol
                g2.setColor(Color.RED);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String symbol = "−";
                int symbolX = buttonX + (buttonWidth - fm.stringWidth(symbol)) / 2;
                int symbolY = buttonY + (buttonHeight + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(symbol, symbolX, symbolY);

            } finally {
                g2.dispose();
            }
        }

        private boolean isMouseOverRemoveButton() {
            // This would need mouse tracking, simplified for now
            return false;
        }
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(2, 4, new Insets(3, 3, 3, 3), -1, -1));
        topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        calendarList = new JList();
        topPanel.add(calendarList, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clearAllButton = new JButton();
        clearAllButton.setText("Clear All");
        topPanel.add(clearAllButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addNewButton = new JButton();
        addNewButton.setText("Add New");
        topPanel.add(addNewButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateTimePicker = new DateTimePicker();
        topPanel.add(dateTimePicker, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

}
