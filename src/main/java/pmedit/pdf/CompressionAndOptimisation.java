package pmedit.pdf;

import org.apache.pdfbox.pdfwriter.compress.CompressParameters;

public class CompressionAndOptimisation {

    CompressParameters compressParameters;

    public CompressionAndOptimisation(CompressParameters compressParameters) {
        this.compressParameters = compressParameters;
    }

    public CompressionAndOptimisation(int objectStreamSize){
        this(new CompressParameters(objectStreamSize));
    }

    public CompressionAndOptimisation(){
        this(CompressParameters.DEFAULT_COMPRESSION);
    }


    public CompressParameters getCompressParameters() {
        return compressParameters;
    }

    public CompressionAndOptimisation setCompressParameters(CompressParameters compressParameters) {
        this.compressParameters = compressParameters;
        return this;
    }

}
