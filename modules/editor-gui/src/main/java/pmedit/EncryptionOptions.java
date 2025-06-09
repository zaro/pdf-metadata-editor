package pmedit;

import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

public class EncryptionOptions {
    public boolean hasEncryption;
    public AccessPermission permission;
    public String ownerPassword;
    public String userPassword;

    EncryptionOptions(boolean hasEncryption, AccessPermission permission, String ownerPassword, String userPassword) {
        this.hasEncryption =hasEncryption;
        this.permission = permission;
        this.ownerPassword = ownerPassword;
        this.userPassword = userPassword;
    }
}
