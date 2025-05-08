package pmedit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

@Retention(RetentionPolicy.RUNTIME)

public @interface MdStruct {
    String name() default "";

    StructType type() default StructType.MdStruct;

    Access access() default Access.ReadWrite;

    enum StructType {
        MdStruct,
        MdEnableStruct,
    }

    enum Access {
        ReadOnly,
        ReadWrite,
    }
}
