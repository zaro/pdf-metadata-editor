package pmedit.ui.components;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateTimePicker extends JDateChooser {
    public DateTimePicker(){
        super();
        setDateFormatString("yyyy-MM-dd HH:mm:ss");
    }

    public void setCalendar(LocalDateTime value){
        Calendar calendar = null;
        if (value != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
        }
        setCalendar(calendar);
    }

    @Override
    public Border getBorder() {
        if(this.dateEditor != null) {
            return this.dateEditor.getUiComponent().getBorder();
        }
        return null;
    }

    @Override
    public void setBorder(Border newBorder) {
        if(this.dateEditor != null) {
            this.dateEditor.getUiComponent().setBorder(newBorder);
        }
    }

    public JTextComponent getTextComponent(){
        if(this.dateEditor instanceof JTextFieldDateEditor de){
            return de;
        }
        throw new RuntimeException("DateTimePicker has no text component");
    }
}
