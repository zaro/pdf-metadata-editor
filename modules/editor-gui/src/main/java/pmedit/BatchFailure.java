package pmedit;

/**
 * A structured per-file failure.
 *
 * @param code    machine-readable error code, for example {@code password_required}
 * @param message message intended for API clients
 * @param cause   original failure, when one exists
 */
public record BatchFailure(String code, String message, Throwable cause) {

    public BatchFailure {
        code = code == null || code.isBlank() ? "tool_error" : code;
        message = message == null || message.isBlank() ? "Unknown error" : message;
    }
}
