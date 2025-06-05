package pmedit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.io.IOException;
import java.util.Calendar;

public class CrossPlatformFileTimeModifier {
    static Logger LOG = LoggerFactory.getLogger(CrossPlatformFileTimeModifier.class);

    public static void setFileTimes(File file, Calendar creationTime,  Calendar modifiedTime) throws IOException {
        FileTime ftCreationTime = creationTime != null ? FileTime.from(creationTime.toInstant()) : null;
        FileTime ftModifiedTime = modifiedTime != null ? FileTime.from(modifiedTime.toInstant()) : null;
        setFileTimes(file.toPath(), ftCreationTime, ftModifiedTime);
    }
    public static void setFileTimes(Path filePath, FileTime creationTime, FileTime modifiedTime)
            throws IOException {

        LOG.trace("setCreateFileTime({}, {}) ", filePath, creationTime);

        if(creationTime != null || modifiedTime != null) {
            // Try to set creation time using BasicFileAttributeView
            try {
                BasicFileAttributeView attributeView = Files.getFileAttributeView(
                        filePath, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);

                if (attributeView != null) {
                    // Read current attributes to preserve access time
                    BasicFileAttributes currentAttrs = attributeView.readAttributes();

                    FileTime accessTime = modifiedTime != null ? modifiedTime :creationTime;
                    // Set times: modified, access, creation
                    attributeView.setTimes(
                            modifiedTime == null ? currentAttrs.lastModifiedTime(): modifiedTime,  // last modified time
                            accessTime,  // preserve current access time
                            creationTime                     // creation time
                    );
                    LOG.trace("Creation time set successfully (if supported by file system)");
                }
            } catch (UnsupportedOperationException e) {
                LOG.error("Creation time modification not supported on this file system", e);
            } catch (IOException e) {
                LOG.error("Failed to set creation time: ", e);
            }
        }

    }


}