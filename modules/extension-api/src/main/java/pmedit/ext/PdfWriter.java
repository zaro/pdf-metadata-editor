package pmedit.ext;

import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import pmedit.MetadataCollection;

import java.io.File;
import java.io.IOException;

public interface PdfWriter {
    float getCompressionMinimumSupportedVersion();

    boolean saveToPDF(MetadataCollection mc, PDDocument document,  File pdfFile) throws Exception;
    File saveAsPDF(MetadataCollection mc, File pdfFile, File newFile) throws Exception ;
    File saveAsPDF(MetadataCollection mc, File pdfFile) throws Exception;
}
