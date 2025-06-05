package pmedit.ext;

import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.FileOptimizer;
import pmedit.pdf.CompressionAndOptimisation;

import java.io.*;

public class PdfWriter {
    static Logger LOG = LoggerFactory.getLogger(PdfWriter.class);
    static public final float COMPRESSION_MIN_VERSION = 1.5f;
    protected PDDocument pdDocument;

    public float getCompressionMinimumSupportedVersion() {
        return COSWriterCompressionPool.MINIMUM_SUPPORTED_VERSION;
    }


    public PdfWriter(PDDocument document){
        this.pdDocument = document;
    }

    public void write(File file, int pdfBoxCompression) throws IOException{
        final CompressionAndOptimisation compressParameters= new CompressionAndOptimisation(pdfBoxCompression);
        LOG.debug("write(File) {}", file);
        pdDocument.save(file, compressParameters.getCompressParameters());

    }

    public <T extends CompressionAndOptimisation> void write(OutputStream output) throws IOException {
        final CompressionAndOptimisation compressParameters= new CompressionAndOptimisation(FileOptimizer.getPdfBoxCompression());
        LOG.debug("write(OutputStream)");
        pdDocument.save(output, compressParameters.getCompressParameters());
    }
}
