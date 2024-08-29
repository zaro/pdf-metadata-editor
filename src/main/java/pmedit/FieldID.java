package pmedit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface FieldID {
    String value();

    FieldType type() default FieldType.StringField;

    enum FieldType {
        StringField,
        TextField,
        LongField,
        IntField,
        DateField,
        BoolField
    }
}
