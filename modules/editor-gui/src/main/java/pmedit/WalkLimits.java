package pmedit;

/**
 * Limits applied while a {@link pmedit.util.FilesWalker} expands directory inputs.
 *
 * <p>CLI and GUI batches use {@link #unlimited()}; the MCP server uses bounded
 * walks so a large directory can never silence a tool call.</p>
 *
 * @param timeoutSeconds         stop walking after this many seconds; 0 means no timeout
 * @param skipHiddenDirectories  do not descend into directories whose name starts with a dot
 * @param skipSymlinkedDirectories do not descend into symlinked directories below the roots
 */
public record WalkLimits(
        int timeoutSeconds,
        boolean skipHiddenDirectories,
        boolean skipSymlinkedDirectories) {

    public static final int DEFAULT_TIMEOUT_SECONDS = 20;

    public WalkLimits {
        if (timeoutSeconds < 0) {
            timeoutSeconds = 0;
        }
    }

    public static WalkLimits unlimited() {
        return new WalkLimits(0, false, false);
    }
}
