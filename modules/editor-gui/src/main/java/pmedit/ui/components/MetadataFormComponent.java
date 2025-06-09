package pmedit.ui.components;

import java.awt.*;

public abstract class MetadataFormComponent {
    public static final String OWNER_PROPERTY = "pmeOwnerClass";
    public abstract Container getContainer();
    public abstract void initMetadataFieldId(String id);
}
