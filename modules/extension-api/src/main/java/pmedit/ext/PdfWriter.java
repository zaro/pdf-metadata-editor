package pmedit.ext;

import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;

import java.io.File;
import java.io.IOException;

public interface PdfWriter {
    float getCompressionMinimumSupportedVersion();

    void write(File file, int pdfBoxCompression) throws IOException;
}
