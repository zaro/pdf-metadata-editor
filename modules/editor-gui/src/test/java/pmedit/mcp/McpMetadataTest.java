package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.DateFormat;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.serdes.CsvMetadata;
import pmedit.serdes.SerDeslUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpMetadataTest {

    @TempDir
    Path tempDir;

    @TempDir
    Path outside;

    @Test
    public void listFieldsReturnsWritableFieldMetadata() throws Exception {
        ObjectNode result = metadata(false, true).listFields();
        JsonNode fields = result.get("fields");
        assertTrue(fields.size() > 10);

        boolean foundTitle = false;
        for (JsonNode field : fields) {
            assertFalse(field.get("id").asText().equals("prop.ownerPassword"));
            assertFalse(field.get("id").asText().equals("prop.userPassword"));
            if ("doc.title".equals(field.get("id").asText())) {
                foundTitle = true;
                assertEquals("StringField", field.get("type").asText());
                assertTrue(field.get("writable").asBoolean());
            }
        }
        assertTrue(foundTitle);
    }

    @Test
    public void readBatchReturnsOneResultPerFile() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("a.pdf", "Title A");
        FilesTestHelper.PMTuple second = createNamedFile("b.pdf", "Title B");

        ObjectNode batch = node(metadata(false, true).readBatch(
                List.of(first.file.getName(), second.file.getName()), null, null));

        assertEquals(2, batch.get("summary").get("total").asInt());
        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals(0, batch.get("summary").get("failed").asInt());

        JsonNode a = batch.get("results").get(0);
        assertEquals("a.pdf", a.get("input").asText());
        assertTrue(a.get("ok").asBoolean());
        assertEquals("Title A", a.get("fields").get("doc.title").asText());
        assertEquals("a.pdf", a.get("file").get("path").asText());

        JsonNode b = batch.get("results").get(1);
        assertEquals("b.pdf", b.get("input").asText());
        assertEquals("Title B", b.get("fields").get("doc.title").asText());
    }

    @Test
    public void readBatchExpandsDirectoriesRecursively() throws Exception {
        createNamedFile("docs/a.pdf", "A");
        createNamedFile("docs/sub/b.pdf", "B");
        Files.writeString(tempDir.resolve("docs/notes.txt"), "not a pdf");

        ObjectNode batch = node(metadata(false, true).readBatch(List.of("docs"), null, null));

        assertEquals(2, batch.get("summary").get("total").asInt());
        assertEquals("docs/a.pdf", batch.get("results").get(0).get("input").asText());
        assertEquals("docs/sub/b.pdf", batch.get("results").get(1).get("input").asText());
    }

    @Test
    public void readBatchDeduplicatesOverlappingPaths() throws Exception {
        createNamedFile("docs/a.pdf", "A");
        createNamedFile("docs/sub/b.pdf", "B");

        ObjectNode batch = node(metadata(false, true).readBatch(
                List.of("docs", "docs/a.pdf", "docs/../docs/sub/b.pdf"), null, null));

        assertEquals(2, batch.get("summary").get("total").asInt());
    }

    @Test
    public void readWithRequestedFieldsReturnsExactlyThoseFields() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode batch = node(metadata(false, true).readBatch(
                List.of(file.file.getName()), null, List.of("doc.title", "doc.author")));

        JsonNode fields = batch.get("results").get(0).get("fields");
        assertEquals(2, fields.size());
        assertEquals("McpTitle", fields.get("doc.title").asText());
        assertEquals("McpAuthor", fields.get("doc.author").asText());
    }

    @Test
    public void readRejectsUnknownAndSecretFields() throws Exception {
        createNamedFile("a.pdf", "McpTitle");

        McpToolException unknown = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(
                        List.of("a.pdf"), null, List.of("does.not.exist")));
        assertEquals("invalid_argument", unknown.code());

        McpToolException secret = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(
                        List.of("a.pdf"), null, List.of("prop.ownerPassword")));
        assertEquals("invalid_argument", secret.code());
    }

    @Test
    public void readBatchReturnsPopulatedFieldsOnlyByDefault() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        JsonNode fields = node(metadata(false, true).readBatch(
                List.of(file.file.getName()), null, null)).get("results").get(0).get("fields");

        assertEquals("McpTitle", fields.get("doc.title").asText());
        assertFalse(fields.has("prop.ownerPassword"));
        assertFalse(fields.has("prop.userPassword"));
        assertFalse(fields.has("dc.coverage"));
    }

    @Test
    public void readBatchRejectsMissingPathBeforeReading() throws Exception {
        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of("missing.pdf"), null, null));

        assertEquals("file_not_found", error.code());
        assertTrue(error.getMessage().contains("File not found"));
    }

    @Test
    public void readBatchRejectsExplicitNonPdf() throws Exception {
        Files.writeString(tempDir.resolve("notes.txt"), "not a pdf");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of("notes.txt"), null, null));

        assertEquals("not_a_pdf", error.code());
    }

    @Test
    public void readBatchRejectsEmptyPathList() throws Exception {
        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of(), null, null));

        assertEquals("no_input_files", error.code());
    }

    @Test
    public void readBatchRejectsEmptyDirectory() throws Exception {
        Files.createDirectories(tempDir.resolve("empty"));

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of("empty"), null, null));

        assertEquals("no_input_files", error.code());
    }

    @Test
    public void readBatchRejectsBatchesOverTheLimit() throws Exception {
        Files.createDirectories(tempDir.resolve("many"));
        for (int i = 0; i <= McpPaths.MAX_BATCH_FILES; i++) {
            Files.writeString(tempDir.resolve("many/file-" + i + ".pdf"), "not parsed");
        }

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of("many"), null, null));

        assertEquals("batch_too_large", error.code());
        assertTrue(error.getMessage().contains(String.valueOf(McpPaths.MAX_BATCH_FILES)));
    }

    @Test
    public void batchLimitIsConfigurable() throws Exception {
        createNamedFile("a.pdf", "A");
        createNamedFile("b.pdf", "B");
        Path root = tempDir.toRealPath();

        McpMetadata limited = new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, false, () -> true, 1));
        McpToolException error = assertThrows(McpToolException.class,
                () -> limited.readBatch(List.of("a.pdf", "b.pdf"), null, null));
        assertEquals("batch_too_large", error.code());
        assertTrue(error.getMessage().contains("1"));

        McpMetadata unlimited = new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, false, () -> true, 0));
        ObjectNode batch = node(unlimited.readBatch(List.of("a.pdf", "b.pdf"), null, null));
        assertEquals(2, batch.get("summary").get("succeeded").asInt());
    }

    @Test
    public void setBatchUpdatesEveryFile() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("a.pdf", "Title A");
        FilesTestHelper.PMTuple second = createNamedFile("b.pdf", "Title B");

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Updated");

        ObjectNode batch = node(metadata(false, true).setBatch(
                List.of(first.file.getName(), second.file.getName()), values, null, null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals("Updated", FilesTestHelper.load(first.file).doc.title);
        assertEquals("Updated", FilesTestHelper.load(second.file).doc.title);
        assertEquals("updated", batch.get("results").get(0).get("action").asText());
        assertTrue(batch.get("results").get(0).get("fields").toString().contains("doc.title"));
    }

    @Test
    public void setBatchExpandsTemplatesPerFile() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("a.pdf", md -> set(md, "doc.author", "Alice"));
        FilesTestHelper.PMTuple second = createNamedFile("b.pdf", md -> set(md, "doc.author", "Bob"));

        ObjectNode values = McpJson.object();
        values.put("doc.title", "{doc.author} report");

        metadata(false, true).setBatch(
                List.of(first.file.getName(), second.file.getName()), values, null, null);

        assertEquals("Alice report", FilesTestHelper.load(first.file).doc.title);
        assertEquals("Bob report", FilesTestHelper.load(second.file).doc.title);
    }

    @Test
    public void setBatchPreservesUntouchedMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");
        String preservedSubject = file.md.doc.subject;

        ObjectNode values = McpJson.object();
        values.put("doc.title", "New Title");
        values.put("doc.author", "New Author");
        values.put("dc.creators", "New Creator");

        metadata(false, true).setBatch(List.of(file.file.getName()), values, null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertEquals("New Title", loaded.doc.title);
        assertEquals("New Author", loaded.doc.author);
        assertEquals(List.of("New Creator"), loaded.dc.creators);
        assertEquals(preservedSubject, loaded.doc.subject);
    }

    @Test
    public void setBatchWritesUnderOutputDirPreservingStructure() throws Exception {
        createNamedFile("docs/a.pdf", "Title A");
        createNamedFile("docs/sub/b.pdf", "Title B");

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Copied Title");

        ObjectNode batch = node(metadata(false, true).setBatch(
                List.of("docs"), values, "out", null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals("out/a.pdf", batch.get("results").get(0).get("output").asText());
        assertEquals("out/sub/b.pdf", batch.get("results").get(1).get("output").asText());

        assertTrue(Files.exists(tempDir.resolve("out/a.pdf")));
        assertTrue(Files.exists(tempDir.resolve("out/sub/b.pdf")));
        assertEquals("Copied Title", FilesTestHelper.load(tempDir.resolve("out/a.pdf").toFile()).doc.title);
        assertEquals("Title A", FilesTestHelper.load(tempDir.resolve("docs/a.pdf").toFile()).doc.title);
    }

    @Test
    public void setBatchWritesASingleFileToOutputDir() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Copied Title");

        ObjectNode batch = node(metadata(false, true).setBatch(
                List.of(file.file.getName()), values, "copy", null));

        assertEquals("copy/a.pdf", batch.get("results").get(0).get("output").asText());
        assertEquals("McpTitle", FilesTestHelper.load(file.file).doc.title);
        assertEquals("Copied Title", FilesTestHelper.load(tempDir.resolve("copy/a.pdf").toFile()).doc.title);
    }

    @Test
    public void convertsJsonValuesToMetadataTypes() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.creationDate", "2020-01-02T03:04:05");
        values.put("basic.rating", 4);
        values.set("dc.creators", McpJson.array().add("Alice").add("Bob"));
        values.set("basic.identifiers", McpJson.array().add("id-1"));

        metadata(false, true).setBatch(List.of(file.file.getName()), values, null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertNotNull(loaded.doc.creationDate);
        assertTrue(DateFormat.formatDateTime(loaded.doc.creationDate).startsWith("2020-01-02"));
        assertEquals(4, loaded.basic.rating);
        assertEquals(List.of("Alice", "Bob"), loaded.dc.creators);
        assertEquals(List.of("id-1"), loaded.basic.identifiers);
    }

    @Test
    public void rejectsUnknownReadOnlyAndSecretFields() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");
        McpMetadata metadata = metadata(false, true);

        ObjectNode unknown = McpJson.object();
        unknown.put("does.not.exist", "x");
        McpToolException unknownError = assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), unknown, null, null));
        assertTrue(unknownError.getMessage().contains("Unknown metadata field"));

        ObjectNode readOnly = McpJson.object();
        readOnly.put("file.name", "x");
        McpToolException readOnlyError = assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), readOnly, null, null));
        assertTrue(readOnlyError.getMessage().contains("read-only"));

        ObjectNode secret = McpJson.object();
        secret.put("prop.ownerPassword", "x");
        McpToolException secretError = assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), secret, null, null));
        assertTrue(secretError.getMessage().contains("ownerPassword"));
    }

    @Test
    public void rejectsInvalidDateValue() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.creationDate", "not-a-date");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).setBatch(List.of(file.file.getName()), values, null, null));
        assertTrue(error.getMessage().contains("doc.creationDate"));
    }

    @Test
    public void clearBatchClearsOnlySelectedFields() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("a.pdf", "Title A");
        FilesTestHelper.PMTuple second = createNamedFile("b.pdf", "Title B");

        ObjectNode batch = node(metadata(false, true).clearBatch(
                List.of(first.file.getName(), second.file.getName()),
                List.of("doc.title"), null, null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());

        MetadataInfo loadedFirst = FilesTestHelper.load(first.file);
        assertNull(loadedFirst.doc.title);
        assertEquals("McpAuthor", loadedFirst.doc.author);

        MetadataInfo loadedSecond = FilesTestHelper.load(second.file);
        assertNull(loadedSecond.doc.title);
        assertEquals("McpAuthor", loadedSecond.doc.author);
    }

    @Test
    public void clearAllRemovesMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode batch = node(metadata(false, true).clearBatch(
                List.of(file.file.getName()), null, null, null));

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        FilesTestHelper.assertEqualsAllExceptFileProps(new MetadataInfo(), loaded, "Metadata was not cleared");

        JsonNode fields = batch.get("results").get(0).get("fields");
        assertTrue(fields.toString().contains("doc.title"));
        assertFalse(fields.toString().contains("prop.ownerPassword"));
        assertFalse(fields.toString().contains("prop.userPassword"));
        assertFalse(fields.toString().contains("file.name"));
    }

    @Test
    public void renameBatchUsesMetadataTemplate() throws Exception {
        createNamedFile("a.pdf", "McpRenameTitle");

        ObjectNode batch = node(metadata(false, true).renameBatch(
                List.of("a.pdf"), "{doc.title}", null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("McpRenameTitle.pdf", batch.get("results").get(0).get("output").asText());
        assertTrue(new File(tempDir.toFile(), "McpRenameTitle.pdf").exists());
        assertFalse(tempDir.resolve("a.pdf").toFile().exists());
    }

    @Test
    public void renameBatchCopiesUnderOutputDirPreservingStructure() throws Exception {
        createNamedFile("docs/a.pdf", "McpRenameTitle");

        ObjectNode batch = node(metadata(false, true).renameBatch(
                List.of("docs"), "{doc.title}", "renamed", null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("renamed/McpRenameTitle.pdf", batch.get("results").get(0).get("output").asText());
        assertTrue(tempDir.resolve("docs/a.pdf").toFile().exists());
        assertTrue(tempDir.resolve("renamed/McpRenameTitle.pdf").toFile().exists());
    }

    @Test
    public void renameRefusesExistingDestinationPerFile() throws Exception {
        createNamedFile("a.pdf", "McpRenameTitle");
        assertTrue(tempDir.resolve("McpRenameTitle.pdf").toFile().createNewFile());

        ObjectNode batch = node(metadata(false, true).renameBatch(
                List.of("a.pdf"), "{doc.title}", null, null));

        assertEquals(0, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("destination_exists",
                batch.get("results").get(0).get("error").get("code").asText());
        assertTrue(tempDir.resolve("a.pdf").toFile().exists());
    }

    @Test
    public void passwordErrorsAreClassifiedPerFile() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");
        McpMetadata metadata = metadata(false, true);

        JsonNode requiredRead = node(metadata.readBatch(
                List.of(encrypted.file.getName()), null, null)).get("results").get(0);
        assertFalse(requiredRead.get("ok").asBoolean());
        assertEquals("password_required", requiredRead.get("error").get("code").asText());
        assertTrue(requiredRead.get("error").get("message").asText().contains("password-protected"));

        JsonNode invalidRead = node(metadata.readBatch(
                List.of(encrypted.file.getName()), "wrong", null)).get("results").get(0);
        assertEquals("invalid_password", invalidRead.get("error").get("code").asText());

        JsonNode validRead = node(metadata.readBatch(
                List.of(encrypted.file.getName()), "ownerpass", null)).get("results").get(0);
        assertTrue(validRead.get("ok").asBoolean());
        assertEquals("Encrypted Title", validRead.get("fields").get("doc.title").asText());

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        JsonNode requiredSet = node(metadata.setBatch(
                List.of(encrypted.file.getName()), values, null, null)).get("results").get(0);
        assertEquals("password_required", requiredSet.get("error").get("code").asText());

        JsonNode invalidSet = node(metadata.setBatch(
                List.of(encrypted.file.getName()), values, null, "wrong")).get("results").get(0);
        assertEquals("invalid_password", invalidSet.get("error").get("code").asText());
    }

    @Test
    public void setBatchHandlesEncryptedPdfWithCorrectPassword() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Updated Encrypted");

        ObjectNode batch = node(metadata(false, true).setBatch(
                List.of(encrypted.file.getName()), values, null, "ownerpass"));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("Updated Encrypted",
                FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);
    }

    @Test
    public void clearBatchHandlesEncryptedPdfPasswords() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");
        McpMetadata metadata = metadata(false, true);

        ObjectNode required = node(metadata.clearBatch(
                List.of(encrypted.file.getName()), List.of("doc.title"), null, null));
        assertFalse(required.get("results").get(0).get("ok").asBoolean());
        assertEquals("password_required",
                required.get("results").get(0).get("error").get("code").asText());
        assertEquals("Encrypted Title", FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);

        ObjectNode invalid = node(metadata.clearBatch(
                List.of(encrypted.file.getName()), List.of("doc.title"), null, "wrong"));
        assertEquals("invalid_password",
                invalid.get("results").get(0).get("error").get("code").asText());
        assertEquals("Encrypted Title", FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);

        ObjectNode cleared = node(metadata.clearBatch(
                List.of(encrypted.file.getName()), List.of("doc.title"), null, "ownerpass"));
        assertEquals(1, cleared.get("summary").get("succeeded").asInt());
        assertNull(FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);
    }

    @Test
    public void renameBatchHandlesEncryptedPdfPasswords() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Rename");
        McpMetadata metadata = metadata(false, true);

        ObjectNode required = node(metadata.renameBatch(
                List.of(encrypted.file.getName()), "{doc.title}", null, null));
        assertEquals("password_required",
                required.get("results").get(0).get("error").get("code").asText());
        assertTrue(encrypted.file.exists());

        ObjectNode invalid = node(metadata.renameBatch(
                List.of(encrypted.file.getName()), "{doc.title}", null, "wrong"));
        assertEquals("invalid_password",
                invalid.get("results").get(0).get("error").get("code").asText());
        assertTrue(encrypted.file.exists());

        ObjectNode renamed = node(metadata.renameBatch(
                List.of(encrypted.file.getName()), "{doc.title}", null, "ownerpass"));
        assertEquals(1, renamed.get("summary").get("succeeded").asInt());
        assertEquals("Encrypted Rename.pdf",
                renamed.get("results").get(0).get("output").asText());
        assertTrue(tempDir.resolve("Encrypted Rename.pdf").toFile().exists());
        assertFalse(encrypted.file.exists());
    }

    @Test
    public void clearBatchWritesUnderOutputDirPreservingStructure() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("docs/a.pdf", "Title A");
        FilesTestHelper.PMTuple second = createNamedFile("docs/sub/b.pdf", "Title B");

        ObjectNode batch = node(metadata(false, true).clearBatch(
                List.of("docs"), List.of("doc.title"), "cleared", null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals("cleared/a.pdf", batch.get("results").get(0).get("output").asText());
        assertEquals("cleared/sub/b.pdf", batch.get("results").get(1).get("output").asText());

        assertEquals("Title A", FilesTestHelper.load(first.file).doc.title);
        assertEquals("Title B", FilesTestHelper.load(second.file).doc.title);

        MetadataInfo outputFirst = FilesTestHelper.load(tempDir.resolve("cleared/a.pdf").toFile());
        assertNull(outputFirst.doc.title);
        assertEquals("McpAuthor", outputFirst.doc.author);

        MetadataInfo outputSecond = FilesTestHelper.load(tempDir.resolve("cleared/sub/b.pdf").toFile());
        assertNull(outputSecond.doc.title);
        assertEquals("McpAuthor", outputSecond.doc.author);
    }

    @Test
    public void renameTemplateCannotEscapeRoot() throws Exception {
        createNamedFile("docs/a.pdf", "EscapeRename");

        ObjectNode batch = node(metadata(false, true).renameBatch(
                List.of("docs/a.pdf"), "../../EscapeRename", null, null));

        assertEquals(0, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("path_outside_root",
                batch.get("results").get(0).get("error").get("code").asText());
        assertTrue(tempDir.resolve("docs/a.pdf").toFile().exists());
        assertFalse(tempDir.getParent().resolve("EscapeRename.pdf").toFile().exists());
    }

    @Test
    public void globPatternsAreTreatedAsLiteralPaths() throws Exception {
        createNamedFile("a.pdf", "A");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).readBatch(List.of("*.pdf"), null, null));

        assertEquals("file_not_found", error.code());
        assertTrue(error.getMessage().contains("*.pdf"));
    }

    @Test
    public void clearAndRenameRejectBatchesOverTheLimitBeforeWriting() throws Exception {
        Files.createDirectories(tempDir.resolve("many"));
        for (int i = 0; i <= McpPaths.MAX_BATCH_FILES; i++) {
            Files.writeString(tempDir.resolve("many/file-" + i + ".pdf"), "not parsed");
        }

        McpToolException clearError = assertThrows(McpToolException.class,
                () -> metadata(false, true).clearBatch(
                        List.of("many"), null, "clear-out", null));
        assertEquals("batch_too_large", clearError.code());
        assertFalse(Files.exists(tempDir.resolve("clear-out")));

        McpToolException renameError = assertThrows(McpToolException.class,
                () -> metadata(false, true).renameBatch(
                        List.of("many"), "{doc.title}", "rename-out", null));
        assertEquals("batch_too_large", renameError.code());
        assertFalse(Files.exists(tempDir.resolve("rename-out")));
    }

    @Test
    public void setBatchRejectsSymlinkedOutputSubdirectory() throws Exception {
        createNamedFile("docs/sub/a.pdf", "Title A");
        Files.createDirectories(tempDir.resolve("out"));
        try {
            Files.createSymbolicLink(tempDir.resolve("out/sub"), outside);
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.assumeTrue(false, "Symbolic links are not supported on this platform");
            return;
        }

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Escaped");

        ObjectNode batch = node(metadata(false, true).setBatch(
                List.of("docs"), values, "out", null));

        assertEquals(0, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("path_outside_root",
                batch.get("results").get(0).get("error").get("code").asText());
        assertFalse(Files.exists(outside.resolve("a.pdf")));
        assertEquals("Title A",
                FilesTestHelper.load(tempDir.resolve("docs/sub/a.pdf").toFile()).doc.title);
    }

    @Test
    public void exportBatchWritesAggregateJsonWithAllFields() throws Exception {
        createNamedFile("a.pdf", "Export Title");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "json", List.of("a.pdf"), null, null, null, null, null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertNotNull(batch.get("outputs"));
        assertEquals("metadata-export.json", batch.get("outputs").get(0).asText());

        File exported = tempDir.resolve("metadata-export.json").toFile();
        assertTrue(exported.exists());
        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(exported);
        assertEquals(1, rows.size());
        assertEquals("Export Title", rows.get(0).get("doc.title"));
        assertEquals("a.pdf", rows.get(0).get("file.fullPath"));
        assertTrue(rows.get(0).containsKey("file.nameWithExt"));
        assertFalse(rows.get(0).containsKey("prop.ownerPassword"));
    }

    @Test
    public void exportBatchAggregateCountsEachInput() throws Exception {
        createNamedFile("a.pdf", "A");
        createNamedFile("b.pdf", "B");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "json", List.of("a.pdf", "b.pdf"), null, null, "meta", null, null, null));

        assertEquals(2, batch.get("summary").get("total").asInt());
        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("outputs").size());
        assertEquals(2, batch.get("results").size());
    }

    @Test
    public void exportBatchAggregateContinuesAfterReadFailure() throws Exception {
        createNamedFile("a.pdf", "Good Export");
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Export");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "json", List.of("a.pdf", encrypted.file.getName()),
                null, null, "meta", null, null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertTrue(tempDir.resolve("meta.json").toFile().exists());
        assertTrue(batch.get("outputs").toString().contains("meta.json"));

        JsonNode failure = null;
        for (JsonNode result : batch.get("results")) {
            if (!result.get("ok").asBoolean()) {
                failure = result;
            }
        }
        assertNotNull(failure);
        assertEquals("password_required", failure.get("error").get("code").asText());
    }

    @Test
    public void exportBatchWritesOneFilePerPdf() throws Exception {
        createNamedFile("a.pdf", "Export A");
        createNamedFile("b.pdf", "Export B");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "yaml", List.of("a.pdf", "b.pdf"), null, null, null, true, null, null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertTrue(batch.get("outputs").toString().contains("a.yaml"));
        assertTrue(batch.get("outputs").toString().contains("b.yaml"));

        List<Map<String, Object>> rows = SerDeslUtils.fromYAMLFileAsList(
                tempDir.resolve("a.yaml").toFile());
        assertEquals("Export A", rows.get(0).get("doc.title"));
    }

    @Test
    public void exportBatchWritesCsvWithExplicitFields() throws Exception {
        createNamedFile("a.pdf", "Export Csv");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "csv", List.of("a.pdf"), List.of("doc.title", "doc.author"),
                null, "people", null, null, null));

        assertEquals("people.csv", batch.get("outputs").get(0).asText());
        List<MetadataInfo> rows = CsvMetadata.readFile(tempDir.resolve("people.csv").toFile());
        assertEquals(1, rows.size());
        assertEquals("Export Csv", rows.get(0).doc.title);
        assertEquals("McpAuthor", rows.get(0).doc.author);
    }

    @Test
    public void exportBatchRejectsInvalidFieldSelections() throws Exception {
        createNamedFile("a.pdf", "A");
        McpMetadata metadata = metadata(false, true);

        McpToolException secret = assertThrows(McpToolException.class,
                () -> metadata.exportBatch("json", List.of("a.pdf"),
                        List.of("prop.ownerPassword"), null, null, null, null, null));
        assertEquals("invalid_argument", secret.code());

        McpToolException none = assertThrows(McpToolException.class,
                () -> metadata.exportBatch("json", List.of("a.pdf"),
                        List.of("none"), null, null, null, null, null));
        assertEquals("invalid_argument", none.code());

        McpToolException empty = assertThrows(McpToolException.class,
                () -> metadata.exportBatch("json", List.of("a.pdf"),
                        List.of(), null, null, null, null, null));
        assertEquals("invalid_argument", empty.code());

        McpToolException mixed = assertThrows(McpToolException.class,
                () -> metadata.exportBatch("json", List.of("a.pdf"),
                        List.of("all", "doc.title"), null, null, null, null, null));
        assertEquals("invalid_argument", mixed.code());

        McpToolException path = assertThrows(McpToolException.class,
                () -> metadata.exportBatch("json", List.of("a.pdf"),
                        null, null, "sub/report", null, null, null));
        assertEquals("invalid_argument", path.code());
    }

    @Test
    public void exportBatchCanWriteAbsolutePaths() throws Exception {
        createNamedFile("a.pdf", "A");

        metadata(false, true).exportBatch(
                "json", List.of("a.pdf"), List.of("file.fullPath"),
                null, null, null, false, null);

        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(
                tempDir.resolve("metadata-export.json").toFile());
        String fullPath = (String) rows.get(0).get("file.fullPath");
        assertTrue(new File(fullPath).isAbsolute());
        assertTrue(fullPath.endsWith("a.pdf"));
    }

    @Test
    public void exportBatchHandlesEncryptedPdf() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Export");

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "json", List.of(encrypted.file.getName()), null, null, null, null, null, "ownerpass"));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(
                tempDir.resolve("metadata-export.json").toFile());
        assertEquals("Encrypted Export", rows.get(0).get("doc.title"));
        assertFalse(rows.get(0).containsKey("prop.ownerPassword"));
    }

    @Test
    public void exportBatchReportsAggregateWriteFailureAsRequestError() throws Exception {
        createNamedFile("a.pdf", "A");
        Files.createDirectories(tempDir.resolve("metadata-export.json"));

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).exportBatch(
                        "json", List.of("a.pdf"), null, null, null, null, null, null));

        assertEquals("output_not_writable", error.code());
    }

    @Test
    public void exportBatchContinuesAfterPerFileWriteFailure() throws Exception {
        createNamedFile("a.pdf", "A");
        createNamedFile("b.pdf", "B");
        Files.createDirectories(tempDir.resolve("a.json"));

        ObjectNode batch = node(metadata(false, true).exportBatch(
                "json", List.of("a.pdf", "b.pdf"), null, null, null, true, null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("output_not_writable",
                batch.get("results").get(0).get("error").get("code").asText());
        assertTrue(tempDir.resolve("b.json").toFile().exists());
        assertFalse(batch.get("outputs").toString().contains("a.json"));
        assertTrue(batch.get("outputs").toString().contains("b.json"));
    }

    @Test
    public void exportBatchRejectsBatchesOverTheLimit() throws Exception {
        Files.createDirectories(tempDir.resolve("many"));
        for (int i = 0; i <= McpPaths.MAX_BATCH_FILES; i++) {
            Files.writeString(tempDir.resolve("many/file-" + i + ".pdf"), "not parsed");
        }

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).exportBatch(
                        "json", List.of("many"), null, "out", null, null, null, null));

        assertEquals("batch_too_large", error.code());
        assertFalse(Files.exists(tempDir.resolve("out")));
    }

    @Test
    public void exportRequiresLicense() throws Exception {
        createNamedFile("a.pdf", "A");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, false).exportBatch(
                        "json", List.of("a.pdf"), null, null, null, null, null, null));

        assertEquals("license_required", error.code());
    }

    @Test
    public void exportToolReturnsOutputs() throws Exception {
        createNamedFile("a.pdf", "A");
        McpTools tools = new McpTools(metadata(false, true));

        ObjectNode args = McpJson.object();
        args.set("paths", McpJson.array().add("a.pdf"));

        ObjectNode result = tools.call("pdf_export_json", args);

        assertFalse(result.get("isError").asBoolean());
        assertTrue(result.get("structuredContent").get("outputs").toString()
                .contains("metadata-export.json"));
    }

    @Test
    public void importFromFilenameExtractsMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("Alice - My Title.pdf", md -> {
            set(md, "doc.title", "Old Title");
            set(md, "doc.author", "Old Author");
        });

        ObjectNode batch = node(metadata(false, true).importFromFilenameBatch(
                List.of(file.file.getName()),
                "{doc.author} - {doc.title}",
                null,
                null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("imported", batch.get("results").get(0).get("action").asText());
        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertEquals("Alice", loaded.doc.author);
        assertEquals("My Title", loaded.doc.title);
    }

    @Test
    public void importFromJsonAppliesExportedMetadata() throws Exception {
        FilesTestHelper.PMTuple first = createNamedFile("a.pdf", "Import A");
        FilesTestHelper.PMTuple second = createNamedFile("b.pdf", "Import B");

        metadata(false, true).exportBatch(
                "json", List.of("a.pdf", "b.pdf"), null, null, "meta", null, null, null);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Changed");
        metadata(false, true).setBatch(List.of("a.pdf", "b.pdf"), values, null, null);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("meta.json"), null, null));

        assertEquals(2, batch.get("summary").get("succeeded").asInt());
        assertEquals("Import A", FilesTestHelper.load(first.file).doc.title);
        assertEquals("Import B", FilesTestHelper.load(second.file).doc.title);
        assertEquals("meta.json", batch.get("results").get(0).get("source").asText());
    }

    @Test
    public void importFromYamlAppliesExportedMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "Yaml Import");
        metadata(false, true).exportBatch(
                "yaml", List.of("a.pdf"),
                List.of("doc.title", "doc.author", "file.fullPath"),
                null, "meta", null, null, null);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Changed");
        metadata(false, true).setBatch(List.of("a.pdf"), values, null, null);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "yaml", List.of("meta.yaml"), null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("Yaml Import", FilesTestHelper.load(file.file).doc.title);
    }

    @Test
    public void importFromCsvAppliesExportedMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "Csv Import");
        metadata(false, true).exportBatch(
                "csv", List.of("a.pdf"),
                List.of("doc.title", "doc.author", "file.fullPath"),
                null, "meta", null, null, null);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Changed");
        metadata(false, true).setBatch(List.of("a.pdf"), values, null, null);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "csv", List.of("meta.csv"), null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("Csv Import", FilesTestHelper.load(file.file).doc.title);
    }

    @Test
    public void importUsesPerRecordPassword() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Import");
        metadata(false, true).exportBatch(
                "json", List.of(encrypted.file.getName()),
                List.of("doc.title", "file.fullPath"),
                null, "meta", null, null, "ownerpass");

        File json = tempDir.resolve("meta.json").toFile();
        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(json);
        rows.get(0).put("doc.title", "Imported Encrypted");
        rows.get(0).put("prop.ownerPassword", "ownerpass");
        SerDeslUtils.toJSONFile(json, rows);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("meta.json"), null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("Imported Encrypted",
                FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);
        assertFalse(batch.get("results").get(0).get("fields").toString()
                .contains("prop.ownerPassword"));
    }

    @Test
    public void importRejectsExplicitWrongExtension() throws Exception {
        createNamedFile("a.pdf", "A");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).importBatch(
                        "json", List.of("a.pdf"), null, null));

        assertEquals("invalid_input", error.code());
    }

    @Test
    public void importUsesTopLevelPasswordFallback() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Import");
        metadata(false, true).exportBatch(
                "json", List.of(encrypted.file.getName()),
                List.of("doc.title", "file.fullPath"),
                null, "meta", null, null, "ownerpass");

        File json = tempDir.resolve("meta.json").toFile();
        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(json);
        rows.get(0).put("doc.title", "Top Level Import");
        rows.get(0).remove("prop.ownerPassword");
        SerDeslUtils.toJSONFile(json, rows);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("meta.json"), null, "ownerpass"));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals("Top Level Import",
                FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);
    }

    @Test
    public void importReportsWrongPerRecordPassword() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Import");
        metadata(false, true).exportBatch(
                "json", List.of(encrypted.file.getName()),
                List.of("doc.title", "file.fullPath"),
                null, "meta", null, null, "ownerpass");

        File json = tempDir.resolve("meta.json").toFile();
        List<Map<String, Object>> rows = SerDeslUtils.fromJSONFileAsList(json);
        rows.get(0).put("doc.title", "Should Not Apply");
        rows.get(0).put("prop.ownerPassword", "wrong");
        SerDeslUtils.toJSONFile(json, rows);

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("meta.json"), null, null));

        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("invalid_password",
                batch.get("results").get(0).get("error").get("code").asText());
        assertEquals("Encrypted Import",
                FilesTestHelper.load(encrypted.file, "ownerpass").doc.title);
    }

    @Test
    public void importContinuesAfterParseFailure() throws Exception {
        createNamedFile("a.pdf", "Good Import");
        metadata(false, true).exportBatch(
                "json", List.of("a.pdf"), null, null, "good", null, null, null);
        Files.writeString(tempDir.resolve("bad.json"), "{not-json");

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("bad.json", "good.json"), null, null));

        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());

        boolean parseFailed = false;
        boolean imported = false;
        for (JsonNode result : batch.get("results")) {
            if ("bad.json".equals(result.get("source").asText())) {
                parseFailed = "parse_failed".equals(result.get("error").get("code").asText());
                assertFalse(result.has("input"));
            }
            if ("good.json".equals(result.get("source").asText())) {
                imported = result.get("ok").asBoolean();
            }
        }
        assertTrue(parseFailed);
        assertTrue(imported);
    }

    @Test
    public void importRejectsReferencedPathOutsideRoot() throws Exception {
        File outsidePdf = FilesTestHelper.emptyPdf(outside.toFile());
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        set(metadata, "doc.title", "Outside");
        FilesTestHelper.save(metadata, outsidePdf);

        SerDeslUtils.toJSONFile(tempDir.resolve("outside.json").toFile(), List.of(Map.of(
                "file.fullPath", outsidePdf.getAbsolutePath(),
                "doc.title", "Outside Import")));

        ObjectNode batch = node(this.metadata(false, true).importBatch(
                "json", List.of("outside.json"), null, null));

        assertEquals(0, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("path_outside_root",
                batch.get("results").get(0).get("error").get("code").asText());
        assertFalse(batch.get("results").get(0).has("input"));
    }

    @Test
    public void importRejectsEmptyImportFiles() throws Exception {
        SerDeslUtils.toJSONFile(tempDir.resolve("empty.json").toFile(), List.of());

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).importBatch(
                        "json", List.of("empty.json"), null, null));

        assertEquals("no_input_files", error.code());
    }

    @Test
    public void importRejectsRecordWithoutFullPath() throws Exception {
        SerDeslUtils.toJSONFile(tempDir.resolve("nopath.json").toFile(),
                List.of(Map.of("doc.title", "No Path")));

        ObjectNode batch = node(metadata(false, true).importBatch(
                "json", List.of("nopath.json"), null, null));

        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertEquals("invalid_argument",
                batch.get("results").get(0).get("error").get("code").asText());
        assertFalse(batch.get("results").get(0).has("input"));
    }

    @Test
    public void importRejectsBatchesOverTheLimitBeforeWriting() throws Exception {
        createNamedFile("a.pdf", "A");
        createNamedFile("b.pdf", "B");
        metadata(false, true).exportBatch(
                "json", List.of("a.pdf", "b.pdf"), null, null, "meta", null, null, null);

        Path root = tempDir.toRealPath();
        McpMetadata limited = new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, false, () -> true, 1));

        McpToolException error = assertThrows(McpToolException.class,
                () -> limited.importBatch(
                        "json", List.of("meta.json"), "out", null));

        assertEquals("batch_too_large", error.code());
        assertFalse(Files.exists(tempDir.resolve("out")));
    }

    @Test
    public void importToolReturnsSource() throws Exception {
        createNamedFile("a.pdf", "Tool Import");
        metadata(false, true).exportBatch(
                "json", List.of("a.pdf"), null, null, "meta", null, null, null);

        McpTools tools = new McpTools(metadata(false, true));
        ObjectNode args = McpJson.object();
        args.set("importPaths", McpJson.array().add("meta.json"));

        ObjectNode result = tools.call("pdf_import_from_json", args);

        assertFalse(result.get("isError").asBoolean());
        assertEquals("meta.json",
                result.get("structuredContent").get("results").get(0).get("source").asText());
    }

    @Test
    public void partialFailureIsNotAToolError() throws Exception {
        createNamedFile("a.pdf", "Title A");
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");

        McpBatchResult batchResult = metadata(false, true).readBatch(
                List.of("a.pdf", encrypted.file.getName()), null, null);
        ObjectNode batch = batchResult.toNode();

        assertFalse(batchResult.isError());
        assertEquals(1, batch.get("summary").get("succeeded").asInt());
        assertEquals(1, batch.get("summary").get("failed").asInt());
        assertTrue(batch.get("results").get(0).get("ok").asBoolean());
        assertEquals("password_required",
                batch.get("results").get(1).get("error").get("code").asText());
    }

    @Test
    public void allFailuresMakeTheToolResultAnError() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");

        McpBatchResult batch = metadata(false, true).readBatch(
                List.of(encrypted.file.getName()), null, null);

        assertTrue(batch.isError());
    }

    @Test
    public void passwordToolResultExposesStructuredError() throws Exception {
        FilesTestHelper.PMTuple encrypted = createEncryptedFile("Encrypted Title");
        McpTools tools = new McpTools(metadata(false, true));

        ObjectNode args = McpJson.object();
        args.set("paths", McpJson.array().add(encrypted.file.getName()));

        ObjectNode result = tools.call("pdf_read_metadata", args);
        assertTrue(result.get("isError").asBoolean());

        JsonNode error = result.get("structuredContent").get("results").get(0).get("error");
        assertEquals("password_required", error.get("code").asText());
        assertTrue(error.get("message").asText().contains("password-protected"));
        assertTrue(result.get("content").get(0).get("text").asText().contains("password_required"));
    }

    @Test
    public void readOnlyModeRejectsMutations() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");
        McpMetadata metadata = metadata(true, true);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), values, null, null));
        assertEquals("read_only", error.code());
        assertTrue(error.getMessage().contains("read-only"));
    }

    @Test
    public void missingLicenseRejectsMutationsButAllowsReads() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");
        McpMetadata metadata = metadata(false, false);

        ObjectNode read = node(metadata.readBatch(List.of(file.file.getName()), null, null));
        assertEquals("McpTitle", read.get("results").get(0).get("fields").get("doc.title").asText());

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), values, null, null));
        assertEquals("license_required", error.code());
        assertTrue(error.getMessage().contains("requires a license"));
        assertFalse(error.getMessage().matches("(?is).*\\b(pro|oss|basic|professional)\\b.*"));
    }

    @Test
    public void everyProToolRequiresALicense() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");
        McpMetadata metadata = metadata(false, false);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        assertEquals("license_required", assertThrows(McpToolException.class,
                () -> metadata.setBatch(List.of(file.file.getName()), values, null, null)).code());
        assertEquals("license_required", assertThrows(McpToolException.class,
                () -> metadata.clearBatch(List.of(file.file.getName()), null, null, null)).code());
        assertEquals("license_required", assertThrows(McpToolException.class,
                () -> metadata.renameBatch(List.of(file.file.getName()), "{doc.title}", null, null)).code());
    }

    @Test
    public void batchTooLargeDoesNotCreateTheOutputDirectory() throws Exception {
        Files.createDirectories(tempDir.resolve("many"));
        for (int i = 0; i <= McpPaths.MAX_BATCH_FILES; i++) {
            Files.writeString(tempDir.resolve("many/file-" + i + ".pdf"), "not parsed");
        }

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).setBatch(List.of("many"), values, "out", null));

        assertEquals("batch_too_large", error.code());
        assertFalse(Files.exists(tempDir.resolve("out")));
    }

    @Test
    public void outputDirOutsideRootIsRejected() throws Exception {
        FilesTestHelper.PMTuple file = createNamedFile("a.pdf", "McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).setBatch(
                        List.of(file.file.getName()), values, "../outside", null));

        assertEquals("path_outside_root", error.code());
    }

    private McpMetadata metadata(boolean readOnly, boolean licensed) throws Exception {
        Path root = tempDir.toRealPath();
        return new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, readOnly, () -> licensed));
    }

    private static ObjectNode node(McpBatchResult batch) {
        return batch.toNode();
    }

    private static void set(MetadataInfo metadata, String id, Object value) {
        metadata.set(id, value);
        metadata.setEnabled(id, true);
    }

    private FilesTestHelper.PMTuple createNamedFile(String relativeName, String title) throws Exception {
        return createNamedFile(relativeName, metadata -> set(metadata, "doc.title", title));
    }

    private FilesTestHelper.PMTuple createNamedFile(
            String relativeName,
            Consumer<MetadataInfo> configure) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        configure.accept(metadata);
        if (metadata.doc.author == null) {
            set(metadata, "doc.author", "McpAuthor");
        }

        File pdf = new File(tempDir.toFile(), relativeName);
        Files.createDirectories(pdf.getParentFile().toPath());
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdf);
        }
        FilesTestHelper.save(metadata, pdf);
        return new FilesTestHelper.PMTuple(pdf, metadata);
    }

    private FilesTestHelper.PMTuple createEncryptedFile(String title) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        set(metadata, "doc.title", title);

        File source = FilesTestHelper.emptyPdf(tempDir.toFile());
        FilesTestHelper.save(metadata, source);

        File encrypted = new File(tempDir.toFile(), "encrypted.pdf");
        try (PDDocument document = Loader.loadPDF(source)) {
            AccessPermission permissions = new AccessPermission();
            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    "ownerpass", "userpass", permissions);
            policy.setEncryptionKeyLength(128);
            document.protect(policy);
            document.save(encrypted);
        }
        return new FilesTestHelper.PMTuple(encrypted, metadata);
    }
}
