package pmedit;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import pmedit.FieldID.FieldType;

@Retention(RetentionPolicy.RUNTIME)

public @interface MdStruct {
	String name() default "";
	public enum StructType {
		MdStruct,
		MdEnableStruct,
	};
	StructType type() default StructType.MdStruct;
}
