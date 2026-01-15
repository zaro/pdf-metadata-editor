package pmedit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import pmedit.ext.PmeExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.Permission;

@EnabledIfSystemProperty(named = "flavour" , matches ="pro")
public class EncryptionTest extends BaseTest{
    public static int NUM_FILES=1;

    @Test
    void testAddEncryption() throws Exception, IOException, Exception {
        for (FilesTestHelper.PMTuple t : randomFiles(NUM_FILES)) {
            MetadataInfo loaded = FilesTestHelper.load(t.file);


            loaded.prop.encryption = true;
            loaded.prop.keyLength = 40;
            loaded.prop.canModify = false;
            loaded.prop.ownerPassword = "op";
            loaded.prop.userPassword = "up";

            FilesTestHelper.save(loaded, t.file);

            PDDocument doc = Loader.loadPDF(t.file, "op");
            assertNotNull(doc.getEncryption());

            assertEquals(loaded.prop.keyLength, doc.getEncryption().getSecurityHandler().getKeyLength());
            assertEquals(
                    String.format("%32s", Integer.toBinaryString(t.md.getAccessPermissions().getPermissionBytes()))
                            .replace(' ', '0'),
                    String.format("%32s", Integer.toBinaryString(loaded.getAccessPermissions().getPermissionBytes()))
                            .replace(' ', '0'),
                    "Loaded Permission bits differ!"
            );

            var actualPermissions = new AccessPermission(doc.getEncryption().getPermissions());
            assertEquals(
                    String.format("%32s", Integer.toBinaryString(t.md.getAccessPermissions().getPermissionBytes()))
                            .replace(' ', '0'),
                    String.format("%32s", Integer.toBinaryString(actualPermissions.getPermissionBytes()))
                            .replace(' ', '0'),
                    "Permission bits differ!"
                    );
        }
    }


    @Test
    void testRemove() throws Exception, IOException, Exception {
        for (FilesTestHelper.PMTuple t : randomFiles(1, md -> {
            md.prop.encryption = true;
            md.prop.canModify = false;
            md.prop.ownerPassword = "op";
            md.prop.userPassword = "up";
        })) {
            MetadataInfo loaded = FilesTestHelper.load(t.file, "op");
            assertTrue(loaded.prop.encryption);


            loaded.prop.encryption  = false;
            FilesTestHelper.save(loaded, t.file);

            PDDocument docOut = Loader.loadPDF(t.file);
            assertNull(docOut.getEncryption());

        }
    }
}