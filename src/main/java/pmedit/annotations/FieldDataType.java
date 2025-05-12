package pmedit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface FieldDataType {
    FieldType value() default FieldType.StringField;

    String nullValueText() default "";


    Class<? extends Enum<?>> enumClass() default NoEnumConfigured.class;

    enum FieldType {
        StringField,
        TextField,
        LongField,
        IntField,
        DateField,
        BoolField,
        EnumField
    }

    enum NoEnumConfigured {

    }
}
