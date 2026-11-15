package pmedit;

/**
 * Aborts a batch after a request-level failure has already been classified.
 */
public class BatchAbortException extends RuntimeException {

    private final BatchError error;

    public BatchAbortException(BatchError error) {
        super(error == null ? "Batch aborted" : error.message());
        this.error = error;
    }

    public BatchError error() {
        return error;
    }
}
