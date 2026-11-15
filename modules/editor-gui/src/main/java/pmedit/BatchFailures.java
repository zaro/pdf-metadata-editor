package pmedit;

import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;

/**
 * Maps batch operation failures to stable MCP error codes.
 */
public final class BatchFailures {

    private BatchFailures() {
    }

    public static BatchFailure classify(Throwable error, String ownerPassword, String defaultCode) {
        if (error instanceof BatchException batchError) {
            return new BatchFailure(batchError.code(), message(batchError), batchError);
        }

        InvalidPasswordException passwordError = findInvalidPassword(error);
        if (passwordError != null) {
            if (ownerPassword == null || ownerPassword.isEmpty()) {
                return new BatchFailure(
                        "password_required",
                        "This PDF is password-protected. Retry with the ownerPassword argument.",
                        error);
            }
            return new BatchFailure(
                    "invalid_password",
                    "The supplied ownerPassword could not open this PDF.",
                    error);
        }

        if (error instanceof NoSuchFileException || error instanceof FileNotFoundException) {
            return new BatchFailure("file_not_found", message(error), error);
        }
        if (error instanceof AccessDeniedException) {
            return new BatchFailure("write_failed", message(error), error);
        }

        return new BatchFailure(defaultCode, message(error), error);
    }

    public static String message(Throwable error) {
        if (error == null) {
            return "Unknown error";
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return message;
    }

    private static InvalidPasswordException findInvalidPassword(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InvalidPasswordException passwordException) {
                return passwordException;
            }
            current = current.getCause();
        }
        return null;
    }
}
