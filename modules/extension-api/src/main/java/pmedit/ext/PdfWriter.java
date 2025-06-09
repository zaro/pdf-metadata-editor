package pmedit.ext;

import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class PdfWriter {
    static Logger LOG = LoggerFactory.getLogger(PdfWriter.class);
    protected PDDocument pdDocument;

    public float getCompressionMinimumSupportedVersion() {
        return COSWriterCompressionPool.MINIMUM_SUPPORTED_VERSION;
    }


    public PdfWriter(PDDocument document){
        this.pdDocument = document;
    }

    public void write(File file, int pdfBoxCompression) throws IOException{
        LOG.debug("write(File) {}", file);
        pdDocument.save(file, new CompressParameters(pdfBoxCompression));

    }
}
