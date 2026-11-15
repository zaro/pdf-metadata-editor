package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.DateFormat;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

        metadata(false, true).clearBatch(List.of(file.file.getName()), null, null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        FilesTestHelper.assertEqualsAllExceptFileProps(new MetadataInfo(), loaded, "Metadata was not cleared");
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
