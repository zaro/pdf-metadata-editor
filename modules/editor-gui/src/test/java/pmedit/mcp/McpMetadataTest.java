package pmedit.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pmedit.DateFormat;
import pmedit.FilesTestHelper;
import pmedit.MetadataInfo;
import pmedit.serdes.SerDeslUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
    public void readReturnsPopulatedFieldsOnlyByDefault() throws Exception {
        FilesTestHelper.PMTuple file = createMinimalFile("McpTitle");

        ObjectNode result = metadata(false, true).read(file.file.getName(), null, null);

        assertEquals(file.file.getName(), result.get("path").asText());
        assertEquals("McpTitle", result.get("fields").get("doc.title").asText());
        assertFalse(result.get("fields").has("prop.ownerPassword"));
        assertFalse(result.get("fields").has("prop.userPassword"));
        assertFalse(result.get("fields").has("dc.coverage"));
    }

    @Test
    public void readWithRequestedFieldsReturnsExactlyThoseFields() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");

        ObjectNode result = metadata(false, true).read(
                file.file.getName(), null, List.of("doc.title", "doc.author"));

        assertEquals(2, result.get("fields").size());
        assertEquals("McpTitle", result.get("fields").get("doc.title").asText());
        assertEquals("McpAuthor", result.get("fields").get("doc.author").asText());
    }

    @Test
    public void setUpdatesFieldsAndPreservesUntouchedMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        String preservedSubject = file.md.doc.subject;

        ObjectNode values = McpJson.object();
        values.put("doc.title", "New Title");
        values.put("doc.author", "New Author");
        values.put("dc.creators", "New Creator");

        ObjectNode result = metadata(false, true).set(file.file.getName(), values, null, null);

        assertEquals(3, result.get("updated").size());
        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertEquals("New Title", loaded.doc.title);
        assertEquals("New Author", loaded.doc.author);
        assertEquals(List.of("New Creator"), loaded.dc.creators);
        assertEquals(preservedSubject, loaded.doc.subject);
    }

    @Test
    public void setCanWriteToADifferentOutputPath() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        String originalMetadataTitle = file.md.doc.title;

        ObjectNode values = McpJson.object();
        values.put("doc.title", "Copied Title");

        metadata(false, true).set(file.file.getName(), values, "copy.pdf", null);

        assertEquals(originalMetadataTitle, FilesTestHelper.load(file.file).doc.title);
        File copy = new File(tempDir.toFile(), "copy.pdf");
        assertTrue(copy.exists());
        assertEquals("Copied Title", FilesTestHelper.load(copy).doc.title);
    }

    @Test
    public void convertsJsonValuesToMetadataTypes() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.creationDate", "2020-01-02T03:04:05");
        values.put("basic.rating", 4);
        values.put("dc.creators", McpJson.array().add("Alice").add("Bob"));
        values.put("basic.identifiers", McpJson.array().add("id-1"));

        metadata(false, true).set(file.file.getName(), values, null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertNotNull(loaded.doc.creationDate);
        assertTrue(DateFormat.formatDateTime(loaded.doc.creationDate).startsWith("2020-01-02"));
        assertEquals(4, loaded.basic.rating);
        assertEquals(List.of("Alice", "Bob"), loaded.dc.creators);
        assertEquals(List.of("id-1"), loaded.basic.identifiers);
    }

    @Test
    public void rejectsUnknownReadOnlyAndSecretFields() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        McpMetadata metadata = metadata(false, true);

        ObjectNode unknown = McpJson.object();
        unknown.put("does.not.exist", "x");
        McpToolException unknownError = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), unknown, null, null));
        assertTrue(unknownError.getMessage().contains("Unknown metadata field"));

        ObjectNode readOnly = McpJson.object();
        readOnly.put("file.name", "x");
        McpToolException readOnlyError = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), readOnly, null, null));
        assertTrue(readOnlyError.getMessage().contains("read-only"));

        ObjectNode secret = McpJson.object();
        secret.put("prop.ownerPassword", "x");
        McpToolException secretError = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), secret, null, null));
        assertTrue(secretError.getMessage().contains("ownerPassword"));
    }

    @Test
    public void rejectsInvalidDateValue() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");

        ObjectNode values = McpJson.object();
        values.put("doc.creationDate", "not-a-date");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).set(file.file.getName(), values, null, null));
        assertTrue(error.getMessage().contains("doc.creationDate"));
    }

    @Test
    public void clearSubsetClearsOnlySelectedFields() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");

        metadata(false, true).clear(file.file.getName(), List.of("doc.title"), null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        assertNull(loaded.doc.title);
        assertEquals("McpAuthor", loaded.doc.author);
    }

    @Test
    public void clearAllRemovesMetadata() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");

        metadata(false, true).clear(file.file.getName(), null, null, null);

        MetadataInfo loaded = FilesTestHelper.load(file.file);
        FilesTestHelper.assertEqualsAllExceptFileProps(new MetadataInfo(), loaded, "Metadata was not cleared");
    }

    @Test
    public void renameUsesMetadataTemplate() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpRenameTitle");

        ObjectNode result = metadata(false, true).rename(file.file.getName(), "{doc.title}", null, null);

        assertEquals("McpRenameTitle.pdf", result.get("outputPath").asText());
        assertTrue(new File(tempDir.toFile(), "McpRenameTitle.pdf").exists());
        assertFalse(file.file.exists());
    }

    @Test
    public void renameRefusesExistingDestination() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpRenameTitle");
        assertTrue(new File(tempDir.toFile(), "McpRenameTitle.pdf").createNewFile());

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata(false, true).rename(file.file.getName(), "{doc.title}", null, null));

        assertTrue(error.getMessage().contains("already exists"));
    }

    @Test
    public void exportReturnsJsonAndYaml() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        McpMetadata metadata = metadata(false, true);

        ObjectNode json = metadata.export(file.file.getName(), "json", null, null);
        assertEquals("json", json.get("format").asText());
        assertTrue(json.get("content").asText().contains("\"doc.title\""));
        assertFalse(json.get("content").asText().contains("ownerPassword"));

        ObjectNode yaml = metadata.export(file.file.getName(), "yaml", "export.yaml", null);
        assertEquals("yaml", yaml.get("format").asText());
        assertEquals("export.yaml", yaml.get("outputPath").asText());
        assertTrue(new File(tempDir.toFile(), "export.yaml").exists());
    }

    @Test
    public void inlineExportTextIsTheRawDocument() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        McpTools tools = new McpTools(metadata(false, true));

        for (String format : List.of("json", "yaml")) {
            ObjectNode args = McpJson.object();
            args.put("path", file.file.getName());
            args.put("format", format);

            ObjectNode result = tools.call("pdf_export_metadata", args);
            assertFalse(result.get("isError").asBoolean());

            String text = result.get("content").get(0).get("text").asText();
            String content = result.get("structuredContent").get("content").asText();
            assertEquals(content, text);

            if ("json".equals(format)) {
                JsonNode parsed = McpJson.parse(text);
                assertEquals("McpTitle", parsed.get("doc.title").asText());
            } else {
                assertNotNull(SerDeslUtils.fromYAML(text));
            }
        }
    }

    @Test
    public void readsAndExportsEncryptedPdfWithoutLeakingPassword() throws Exception {
        FilesTestHelper.PMTuple file = createEncryptedFile("Encrypted Title");
        McpMetadata metadata = metadata(false, true);

        ObjectNode read = metadata.read(file.file.getName(), "ownerpass", null);
        assertEquals("Encrypted Title", read.get("fields").get("doc.title").asText());
        assertFalse(read.get("fields").has("prop.ownerPassword"));
        assertFalse(read.get("fields").has("prop.userPassword"));

        ObjectNode json = metadata.export(file.file.getName(), "json", null, "ownerpass");
        String jsonText = json.get("content").asText();
        assertFalse(jsonText.contains("ownerpass"));
        assertFalse(jsonText.contains("ownerPassword"));
        JsonNode parsedJson = McpJson.parse(jsonText);
        assertEquals("Encrypted Title", parsedJson.get("doc.title").asText());

        ObjectNode yaml = metadata.export(file.file.getName(), "yaml", null, "ownerpass");
        String yamlText = yaml.get("content").asText();
        assertFalse(yamlText.contains("ownerpass"));
        assertFalse(yamlText.contains("ownerPassword"));
        assertNotNull(SerDeslUtils.fromYAML(yamlText));
    }

    @Test
    public void passwordErrorsAreClassified() throws Exception {
        FilesTestHelper.PMTuple file = createEncryptedFile("Encrypted Title");
        McpMetadata metadata = metadata(false, true);

        McpToolException requiredRead = assertThrows(McpToolException.class,
                () -> metadata.read(file.file.getName(), null, null));
        assertEquals("password_required", requiredRead.code());
        assertTrue(requiredRead.getMessage().contains("password-protected"));

        McpToolException requiredExport = assertThrows(McpToolException.class,
                () -> metadata.export(file.file.getName(), "json", null, null));
        assertEquals("password_required", requiredExport.code());

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");
        McpToolException requiredSet = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), values, null, null));
        assertEquals("password_required", requiredSet.code());

        McpToolException invalidRead = assertThrows(McpToolException.class,
                () -> metadata.read(file.file.getName(), "wrong", null));
        assertEquals("invalid_password", invalidRead.code());
        assertTrue(invalidRead.getMessage().contains("could not open"));
    }

    @Test
    public void passwordToolErrorIsStructuredJson() throws Exception {
        FilesTestHelper.PMTuple file = createEncryptedFile("Encrypted Title");
        McpTools tools = new McpTools(metadata(false, true));

        ObjectNode args = McpJson.object();
        args.put("path", file.file.getName());

        ObjectNode result = tools.call("pdf_read_metadata", args);
        assertTrue(result.get("isError").asBoolean());

        JsonNode error = McpJson.parse(result.get("content").get(0).get("text").asText());
        assertEquals("password_required", error.get("error").asText());
        assertTrue(error.get("message").asText().contains("password-protected"));
    }

    @Test
    public void readOnlyModeRejectsMutations() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        McpMetadata metadata = metadata(true, true);

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), values, null, null));
        assertTrue(error.getMessage().contains("read-only"));
    }

    @Test
    public void missingLicenseRejectsMutationsButAllowsReads() throws Exception {
        FilesTestHelper.PMTuple file = createFile("McpTitle");
        McpMetadata metadata = metadata(false, false);

        ObjectNode read = metadata.read(file.file.getName(), null, null);
        assertEquals("McpTitle", read.get("fields").get("doc.title").asText());

        ObjectNode values = McpJson.object();
        values.put("doc.title", "x");

        McpToolException error = assertThrows(McpToolException.class,
                () -> metadata.set(file.file.getName(), values, null, null));
        assertTrue(error.getMessage().contains("requires a license"));
        assertFalse(error.getMessage().matches("(?is).*\\b(pro|oss|basic|professional)\\b.*"));
    }

    private McpMetadata metadata(boolean readOnly, boolean licensed) throws Exception {
        Path root = tempDir.toRealPath();
        return new McpMetadata(
                McpPaths.forRoot(root),
                new McpOptions(root, readOnly, () -> licensed));
    }

    private FilesTestHelper.PMTuple createFile(String title) throws Exception {
        List<FilesTestHelper.PMTuple> files = FilesTestHelper.randomFiles(1, true, metadata -> {
            metadata.doc.title = title;
            metadata.doc.author = "McpAuthor";
        }, tempDir.toFile());
        return files.get(0);
    }

    private FilesTestHelper.PMTuple createMinimalFile(String title) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        metadata.set("doc.title", title);
        metadata.setEnabled("doc.title", true);

        File pdf = FilesTestHelper.emptyPdf(tempDir.toFile());
        FilesTestHelper.save(metadata, pdf);
        return new FilesTestHelper.PMTuple(pdf, metadata);
    }

    private FilesTestHelper.PMTuple createEncryptedFile(String title) throws Exception {
        MetadataInfo metadata = new MetadataInfo();
        metadata.setEnabled(false);
        metadata.set("doc.title", title);
        metadata.setEnabled("doc.title", true);

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
