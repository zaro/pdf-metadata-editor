package pmedit.ext;

import org.apache.pdfbox.pdmodel.PDDocument;
import pmedit.FileOptimizer;
import pmedit.pdf.CompressionAndOptimisation;
import pmedit.preset.PresetValues;

import java.io.*;

public class PdfWriter {
    protected PDDocument pdDocument;
    public PdfWriter(PDDocument document){
        this.pdDocument = document;
    }

    public void write(File file) throws IOException{
        final CompressionAndOptimisation compressParameters= new CompressionAndOptimisation(FileOptimizer.getPdfBoxCompression());
        pdDocument.save(file, compressParameters.getCompressParameters());

    }

    public <T extends CompressionAndOptimisation> void write(OutputStream output) throws IOException {
        final CompressionAndOptimisation compressParameters= new CompressionAndOptimisation(FileOptimizer.getPdfBoxCompression());
        pdDocument.save(output, compressParameters.getCompressParameters());
    }
}
