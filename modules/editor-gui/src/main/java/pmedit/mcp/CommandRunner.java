package pmedit.mcp;

import pmedit.ext.CommandResult;

/**
 * Runs an extension command on behalf of an MCP tool.
 *
 * <p>The MCP layer only knows command names and an opaque argument string; the
 * extension decides what they mean. Injecting this keeps tool tests free of the
 * extension implementation.</p>
 */
@FunctionalInterface
public interface CommandRunner {

    CommandResult execute(String command, String argument);
}
