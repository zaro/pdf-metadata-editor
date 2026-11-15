package pmedit;

/**
 * A request-level batch failure that is not tied to one input file.
 */
public record BatchError(String displayLabel, String code, String message, Throwable cause) {

    public BatchError {
        displayLabel = displayLabel == null || displayLabel.isBlank() ? "*" : displayLabel;
        code = code == null || code.isBlank() ? "tool_error" : code;
        message = message == null || message.isBlank() ? "Unknown error" : message;
    }
}
