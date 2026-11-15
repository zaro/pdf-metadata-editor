package pmedit.mcp;

import pmedit.BatchError;
import pmedit.BatchFileOutcome;
import pmedit.BatchListener;
import pmedit.MetadataInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts shared batch events to the MCP result envelope.
 */
final class McpBatchListener implements BatchListener {

    private final McpPaths paths;
    private final McpBatchResult result = new McpBatchResult();
    private BatchError batchError;

    McpBatchListener(McpPaths paths) {
        this.paths = paths;
    }

    @Override
    public void onFile(BatchFileOutcome outcome) {
        if (outcome.ok()) {
            if (outcome.input() == null) {
                result.addError(
                        outcome.displayLabel(),
                        "invalid_argument",
                        "Batch result has no input file");
                return;
            }

            List<String> fields = new ArrayList<>();
            for (String field : outcome.fields()) {
                if (!McpMetadata.SECRET_FIELDS.contains(field)
                        && MetadataInfo.keyIsWritable(field)) {
                    fields.add(field);
                }
            }

            Path input = outcome.input().toPath();
            Path output = outcome.output() != null ? outcome.output().toPath() : input;
            result.addMutationSuccess(
                    input,
                    output,
                    paths,
                    outcome.action(),
                    fields,
                    outcome.warnings());
        } else {
            String input = outcome.input() == null
                    ? outcome.displayLabel()
                    : paths.relative(outcome.input().toPath());
            result.addError(input, outcome.failure().code(), outcome.failure().message());
        }
    }

    @Override
    public void onBatchError(BatchError error) {
        this.batchError = error;
    }

    McpBatchResult finish() throws McpToolException {
        if (batchError != null) {
            String message = "license_required".equals(batchError.code())
                    ? McpMetadata.LICENSE_MESSAGE
                    : batchError.message();
            throw new McpToolException(batchError.code(), message, batchError.cause());
        }
        return result;
    }
}
