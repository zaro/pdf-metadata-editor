package pmedit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OsCheckTest {

    @TempDir
    Path tempDir;

    @Test
    public void readsFlatpakIdFromInfoFile() throws Exception {
        Path info = tempDir.resolve("flatpak-info");
        Files.writeString(info, "[Application]\nname=org.example.App\nruntime=org.freedesktop.Platform\n");

        assertEquals("org.example.App", OsCheck.flatpakIdFromInfo(info));
    }

    @Test
    public void returnsNullWhenInfoFileIsMissing() {
        assertNull(OsCheck.flatpakIdFromInfo(tempDir.resolve("missing")));
    }

    @Test
    public void returnsNullWhenInfoFileHasNoName() throws Exception {
        Path info = tempDir.resolve("flatpak-info");
        Files.writeString(info, "[Application]\nruntime=org.freedesktop.Platform\n");

        assertNull(OsCheck.flatpakIdFromInfo(info));
    }

    @Test
    public void defaultFlatpakIdIsTheApplicationId() {
        assertEquals("me.broken_by.PdfMetadataEditor", OsCheck.DEFAULT_FLATPAK_ID);
    }
}
