package pmedit;

/**
 * @param args
 */
public interface ActionStatus {
    void addStatus(String filename, String message);

    void addError(String filename, String error);
}
