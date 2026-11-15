package pmedit;

import java.io.File;
import java.util.List;

/**
 * One per-file batch outcome.
 *
 * <p>{@code displayLabel} and {@code displayMessage} preserve the existing CLI
 * and GUI output. Structured consumers such as MCP use {@code input},
 * {@code outputs}, {@code action}, {@code fields}, {@code warnings}, and
 * {@code failure} instead.</p>
 */
public record BatchFileOutcome(
        File input,
        List<File> outputs,
        String action,
        List<String> fields,
        List<String> warnings,
        String displayLabel,
        String displayMessage,
        BatchFailure failure) {

    public BatchFileOutcome {
        outputs = outputs == null ? List.of() : List.copyOf(outputs);
        fields = fields == null ? List.of() : List.copyOf(fields);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public boolean ok() {
        return failure == null;
    }

    public File output() {
        return outputs.isEmpty() ? null : outputs.get(0);
    }

    public static BatchFileOutcome success(
            File input,
            File output,
            String action,
            List<String> fields,
            List<String> warnings,
            String displayLabel,
            String displayMessage) {
        return new BatchFileOutcome(
                input,
                output == null ? List.of() : List.of(output),
                action,
                fields,
                warnings,
                displayLabel,
                displayMessage,
                null);
    }

    public static BatchFileOutcome error(
            File input,
            String displayLabel,
            String displayMessage,
            BatchFailure failure) {
        return new BatchFileOutcome(
                input,
                List.of(),
                null,
                List.of(),
                List.of(),
                displayLabel,
                displayMessage,
                failure);
    }
}
