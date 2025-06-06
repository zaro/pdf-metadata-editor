package pmedit;

/**
 * @param args
 */
public abstract class ActionStatus {
    public abstract void showStatus(String filename, String message);

    public abstract void showError(String filename, Throwable error);

    public void addStatus(String filename, String message){
        showStatus(filename, message);
    }

    public void addError(String filename, Throwable error){
        showError(filename, error);
    }

    public void addErrorWithCause(String filename, String message, Throwable cause){
        Throwable t = new Exception(message);
        t.initCause(cause);
        addError(filename, t);
    }
}
