package pmedit;

import pmedit.util.FilesWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The shared batch runner used by the CLI, GUI, and MCP server.
 *
 * <p>The engine owns preflight validation (path policy, deduplication, file
 * limit, license gate) and delegates the actual work to
 * {@link PDFMetadataEditBatch}. Callers receive typed {@link BatchFileOutcome}
 * events.</p>
 */
public final class BatchEngine {

    public static final int DEFAULT_MAX_FILES = 100;
    public static final String[] PDF_EXTENSIONS = {"pdf"};

    public BatchPreflight preflight(CommandDescription command, BatchRequest request) throws BatchException {
        if (command == null) {
            throw new BatchException("invalid_command", "Invalid command");
        }
        return preflight(command.inputFileExtensions, request);
    }

    public BatchPreflight preflight(String[] extensions, BatchRequest request) throws BatchException {
        return preflight(extensions, request, true, true);
    }

    public BatchPreflight preflight(
            String[] extensions,
            BatchRequest request,
            boolean enforceFileLimit) throws BatchException {
        return preflight(extensions, request, enforceFileLimit, true);
    }

    public BatchPreflight preflight(
            String[] extensions,
            BatchRequest request,
            boolean enforceFileLimit,
            boolean prepareOutput) throws BatchException {
        BatchOptions options = request.options();
        if (request.roots().isEmpty()) {
            if (options.rejectExplicitNonPdf()) {
                throw new BatchException("no_input_files", "At least one path is required");
            }
            return new BatchPreflight(
                    List.of(),
                    List.of(),
                    options.pathPolicy().prepareOutputDirectory(request.outputDir()));
        }

        List<File> roots = new ArrayList<>();
        for (File root : request.roots()) {
            if (root == null) {
                throw new BatchException("invalid_argument", "A file path is required");
            }

            File validated = options.pathPolicy().validateInput(root);
            if (validated.isFile()
                    && !hasExtension(validated, extensions)
                    && options.rejectExplicitNonPdf()) {
                throw new BatchException(nonPdfCode(extensions), nonPdfMessage(extensions, root));
            }
            roots.add(validated);
        }

        FilesWalker walker = new FilesWalker(
                extensions,
                roots,
                request.outputDir(),
                options.deduplicate(),
                options.walkLimits(),
                enforceFileLimit ? options.maxFiles() : 0);
        List<File> files = walker.list();
        if (walker.aborted() != null) {
            throw new BatchException(walker.aborted().code(), walker.aborted().getMessage());
        }
        for (File file : files) {
            options.pathPolicy().validateInput(file);
        }

        if (files.isEmpty() && options.rejectExplicitNonPdf()) {
            throw new BatchException("no_input_files", "No matching input files were found in the supplied paths");
        }
        if (options.maxFiles() > 0 && enforceFileLimit && files.size() > options.maxFiles()) {
            throw new BatchException(
                    "batch_too_large",
                    "The batch contains " + files.size() + " PDF files; the maximum is "
                            + options.maxFiles() + ". Split the request into smaller batches.");
        }

        File outputDir = prepareOutput
                ? options.pathPolicy().prepareOutputDirectory(request.outputDir())
                : request.outputDir();
        return new BatchPreflight(roots, files, outputDir);
    }

    public void run(
            CommandDescription command,
            BatchOperationParameters params,
            BatchRequest request,
            BatchListener listener) {
        BatchOptions options = request.options();

        if (options.checkLicense() && !options.licenseGate().getAsBoolean()) {
            listener.onBatchError(new BatchError(
                    "*",
                    "license_required",
                    "Invalid license, you can get a license at " + WebsiteConstants.batchLicenseUrl,
                    null));
            return;
        }

        BatchPreflight preflight;
        boolean isImport = isImportCommand(command);
        try {
            preflight = preflight(command.inputFileExtensions, request, !isImport, !isImport);
        } catch (BatchException e) {
            listener.onBatchError(new BatchError("*", e.code(), e.getMessage(), e));
            return;
        }

        FilesWalker walker = new FilesWalker(
                command.inputFileExtensions,
                preflight.roots(),
                preflight.outputDir(),
                options.deduplicate(),
                options.walkLimits(),
                isImport ? 0 : options.maxFiles());
        PDFMetadataEditBatch batch = new PDFMetadataEditBatch(params, options);
        try {
            batch.runCommand(command, walker, preflight.outputDir(), listener);
        } catch (FilesWalker.WalkAborted e) {
            listener.onBatchError(new BatchError("*", e.code(), e.getMessage(), e));
        } catch (BatchAbortException e) {
            listener.onBatchError(e.error());
        } catch (RuntimeException e) {
            listener.onBatchError(new BatchError(
                    "*", "tool_error", BatchFailures.message(e), e));
        }
    }

    public static boolean isImportCommand(CommandDescription command) {
        return command != null
                && (command.is(CommandDescription.FROM_JSON)
                || command.is(CommandDescription.FROM_YAML)
                || command.is(CommandDescription.FROM_CSV));
    }

    private static boolean hasExtension(File file, String[] extensions) {
        if (extensions == null) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String extension : extensions) {
            if (name.endsWith("." + extension.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPdfExtensions(String[] extensions) {
        if (extensions != null) {
            for (String extension : extensions) {
                if ("pdf".equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String nonPdfCode(String[] extensions) {
        return isPdfExtensions(extensions) ? "not_a_pdf" : "invalid_input";
    }

    private static String nonPdfMessage(String[] extensions, File root) {
        return isPdfExtensions(extensions)
                ? "Not a PDF file: " + root
                : "Not a supported input file: " + root;
    }
}
