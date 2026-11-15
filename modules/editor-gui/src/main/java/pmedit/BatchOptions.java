package pmedit;

import pmedit.ext.PmeExtension;

import java.util.function.BooleanSupplier;

/**
 * Options for a shared batch run.
 *
 * @param checkLicense          when true, the engine performs the single license gate
 * @param licenseGate           returns true when batch operations are licensed
 * @param maxFiles              maximum number of input files; 0 means unlimited
 * @param deduplicate           skip files that resolve to the same real path
 * @param rejectExplicitNonPdf  reject an explicitly supplied file with the wrong extension
 * @param reportIgnoredFiles    report files ignored by the walker as per-file errors
 * @param ownerPassword         optional owner password passed to readers and writers
 * @param pathPolicy            input/output path policy
 */
public record BatchOptions(
        boolean checkLicense,
        BooleanSupplier licenseGate,
        int maxFiles,
        boolean deduplicate,
        boolean rejectExplicitNonPdf,
        boolean reportIgnoredFiles,
        String ownerPassword,
        BatchPathPolicy pathPolicy) {

    public BatchOptions {
        if (licenseGate == null) {
            licenseGate = () -> false;
        }
        if (pathPolicy == null) {
            pathPolicy = BatchPathPolicy.permissive();
        }
        if (maxFiles < 0) {
            maxFiles = 0;
        }
    }

    /**
     * CLI and GUI behavior: unlimited files, no deduplication, ignored files
     * reported as before, and the existing license gate.
     */
    public static BatchOptions cli() {
        return new BatchOptions(
                true,
                () -> PmeExtension.get().hasBatch(),
                0,
                false,
                false,
                true,
                null,
                BatchPathPolicy.permissive());
    }
}
