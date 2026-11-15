package pmedit;

/**
 * Receives progress and results from the shared batch engine.
 */
public interface BatchListener {

    void onFile(BatchFileOutcome outcome);

    void onBatchError(BatchError error);
}
