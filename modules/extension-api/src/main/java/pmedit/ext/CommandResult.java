package pmedit.ext;

/**
 * Outcome of an extension command.
 *
 * <p>Extensions return the outcome instead of writing it to stdout, because the
 * MCP server reserves stdout for protocol frames. The host treats the message
 * as opaque text.</p>
 *
 * @param ok      true when the command did what the caller asked
 * @param message human readable status, including failure reasons
 */
public record CommandResult(boolean ok, String message) {

    public static CommandResult success(String message) {
        return new CommandResult(true, message);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message);
    }
}
