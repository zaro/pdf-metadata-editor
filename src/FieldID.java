import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)

public @interface FieldID {
	String value();
	public enum FieldType {
		StringField,
		TextField,
		IntField,
		DateField
	};
	FieldType type() default FieldType.StringField;
}
