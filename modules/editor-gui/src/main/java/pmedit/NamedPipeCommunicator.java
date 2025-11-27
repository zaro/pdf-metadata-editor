package pmedit;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NamedPipeCommunicator {
    final Logger LOG = LoggerFactory.getLogger(NamedPipeCommunicator.class);

    // Define pipe name (must match between client and server)
    private static final String PIPE_NAME = "\\\\.\\pipe\\" + Version.getAppName().replace(" ", "_") + "_SingleInstancePipe";

    // Windows API via JNA
    public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        WinNT.HANDLE CreateNamedPipeW(
                WString lpName,
                int dwOpenMode,
                int dwPipeMode,
                int nMaxInstances,
                int nOutBufferSize,
                int nInBufferSize,
                int nDefaultTimeOut,
                WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes
        );

        boolean ConnectNamedPipe(WinNT.HANDLE hNamedPipe, WinBase.OVERLAPPED lpOverlapped);
        boolean DisconnectNamedPipe(WinNT.HANDLE hNamedPipe);
        boolean ReadFile(
                WinNT.HANDLE hFile,
                byte[] lpBuffer,
                int nNumberOfBytesToRead,
                IntByReference lpNumberOfBytesRead,
                WinBase.OVERLAPPED lpOverlapped
        );
        boolean WriteFile(
                WinNT.HANDLE hFile,
                byte[] lpBuffer,
                int nNumberOfBytesToWrite,
                IntByReference lpNumberOfBytesWritten,
                WinBase.OVERLAPPED lpOverlapped
        );
        WinNT.HANDLE CreateFileW(
                WString lpFileName,
                int dwDesiredAccess,
                int dwShareMode,
                WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes,
                int dwCreationDisposition,
                int dwFlagsAndAttributes,
                WinNT.HANDLE hTemplateFile
        );
    }

    private final Kernel32 kernel32 = Kernel32.INSTANCE;

    private String getLastErrorAsString(int errorCode) {
        if (errorCode == 0) {
            return "No error";
        }
        try {
            return Kernel32Util.formatMessage(errorCode);
        } catch (Exception e) {
            return "Failed to format error " + errorCode + ": " + e.getMessage();
        }
    }

    // -----------------------------
    // SERVER (First Instance)
    // -----------------------------
    public void startServer(CommandHandler handler) {
        new Thread(() -> {
            // Create the named pipe ONCE
            WString pipeName = new WString(PIPE_NAME);
            WinNT.HANDLE pipe = kernel32.CreateNamedPipeW(
                    pipeName,
                    WinNT.PIPE_ACCESS_DUPLEX,
                    WinNT.PIPE_TYPE_BYTE | WinNT.PIPE_READMODE_BYTE | WinNT.PIPE_WAIT,
                    1, // max instances
                    1024,
                    1024,
                    0,
                    null
            );

            if (WinBase.INVALID_HANDLE_VALUE.equals(pipe)) {
                int lastError = kernel32.GetLastError();
                String errorMessage = getLastErrorAsString(lastError);
                LOG.error("Failed to create named pipe [{}]: {}", PIPE_NAME, errorMessage);
                return;
            }

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Wait for a client to connect
                    boolean connected = kernel32.ConnectNamedPipe(pipe, null);
                    if (!connected) {
                        int lastError = kernel32.GetLastError();
                        if (lastError != WinError.ERROR_PIPE_CONNECTED) {
                            String errorMessage = getLastErrorAsString(lastError);
                            LOG.error("ConnectNamedPipe failed: {}", errorMessage);
                            break;
                        }
                        // Else: client connected during CreateFile (fast client), proceed
                    }

                    // Read the message
                    byte[] buffer = new byte[4096];
                    IntByReference bytesRead = new IntByReference(0);
                    boolean readSuccess = kernel32.ReadFile(pipe, buffer, buffer.length, bytesRead, null);

                    if (readSuccess && bytesRead.getValue() > 0) {
                        String received = new String(buffer, 0, bytesRead.getValue(), StandardCharsets.UTF_8);
                        String[] args = received.split("\0", -1); // -1 to keep trailing empty
                        LOG.info("Received arguments : {}", (Object) args);
                        handler.handleCommand(args);
                    } else {
                        int err = kernel32.GetLastError();
                        if (err != 0) {
                            String errorMessage = getLastErrorAsString(err);
                            LOG.error("ReadFile error: {}" , errorMessage);
                        }
                    }

                    // Disconnect to allow next client — DO NOT close handle
                    kernel32.DisconnectNamedPipe(pipe);
                    // Loop back to ConnectNamedPipe
                }
            } finally {
                kernel32.CloseHandle(pipe);
            }
        }, "NamedPipeServer").start();
    }

    // -----------------------------
    // CLIENT (Second Instance)
    // -----------------------------
    public boolean sendArgumentsToServer(String[] args) {
        LOG.info("Sending arguments to already running process : {}", (Object) args);

        final long timeoutMs = 3000;
        final long intervalMs = 100;
        final long start = System.currentTimeMillis();

        // Serialize args (null-separated)
        String payload = String.join("\0", args);
        byte[] buffer = payload.getBytes(StandardCharsets.UTF_8);
        WString pipeName = new WString(PIPE_NAME);

        while (System.currentTimeMillis() - start < timeoutMs) {
            WinNT.HANDLE hPipe = kernel32.CreateFileW(
                    pipeName,
                    WinNT.GENERIC_WRITE,
                    0, // no sharing
                    null,
                    WinNT.OPEN_EXISTING,
                    0, // blocking mode
                    null
            );

            if (!WinBase.INVALID_HANDLE_VALUE.equals(hPipe)) {
                try {
                    IntByReference bytesWritten = new IntByReference(0);
                    boolean success = kernel32.WriteFile(
                            hPipe,
                            buffer,
                            buffer.length,
                            bytesWritten,
                            null
                    );

                    LOG.info("success={} bufferSize={} bytesWritten={}", success, buffer.length, bytesWritten.getValue());
                    if (success && bytesWritten.getValue() == buffer.length) {
                        return true; // success!
                    }
                } finally {
                    kernel32.CloseHandle(hPipe);
                }
            }

            int lastError = kernel32.GetLastError();
            String errorMessage = getLastErrorAsString(lastError);
            LOG.error("Pipe not available , retry in {}ms. Error: {}", intervalMs, errorMessage);
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        // Timeout: server not ready or not running
        LOG.error("Timeout: server not ready or not running");
        return false;
    }

    // -----------------------------
    // Callback Interface
    // -----------------------------
    @FunctionalInterface
    public interface CommandHandler {
        void handleCommand(String[] args);
    }
}