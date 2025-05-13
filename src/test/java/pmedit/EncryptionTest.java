package pmedit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class EncryptionTest {
    public static int NUM_FILES=1;

    @Test
    void testAddEncryption() throws Exception, IOException, Exception {
        for (FilesTestHelper.PMTuple t : FilesTestHelper.randomFiles(NUM_FILES)) {
            MetadataInfo loaded = new MetadataInfo();
            loaded.loadFromPDF(t.file);
            var p = new AccessPermission();
            p.setCanModify(false);
            loaded.encryptionOptions = new EncryptionOptions(true, p, "op", "up");
            loaded.saveAsPDF(t.file);

            PDDocument doc = Loader.loadPDF(t.file, "up");
            assertNotNull(doc.getEncryption());

            var actualPermissions = new AccessPermission(doc.getEncryption().getPermissions());
            assertEquals(p.getPermissionBytes(), actualPermissions.getPermissionBytes());
        }
    }



    @Test
    void testRemove() throws Exception, IOException, Exception {
        var p = new AccessPermission();
        p.setCanModify(false);

        for (FilesTestHelper.PMTuple t : FilesTestHelper.randomFiles(1, p, false)) {
            MetadataInfo loaded = new MetadataInfo();
            loaded.loadFromPDF(t.file);
            assertNotNull(loaded.encryptionOptions);
            assertTrue(loaded.encryptionOptions.hasEncryption);

            loaded.encryptionOptions.hasEncryption = false;
            loaded.saveAsPDF(t.file);

            PDDocument doc = Loader.loadPDF(t.file);
            assertNull(doc.getEncryption());

        }
    }
}