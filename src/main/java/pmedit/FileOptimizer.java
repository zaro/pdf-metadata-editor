package pmedit;

import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOptimizer {
    public enum Enum {
        NONE,
        PDFBOX,
    }

    public static  void setPdfBoxCompression(int c){
        Main.getPreferences().putInt("PdfBoxCompression", c);
    }

    public static  int getPdfBoxCompression(){
        return Main.getPreferences().getInt("PdfBoxCompression", CompressParameters.DEFAULT_OBJECT_STREAM_SIZE);
    }


    public static  void setCurrentOptimizer(Enum e){
        Main.getPreferences().put("FileOptimizer", e.toString());
    }

    public static Enum getCurrentOptimizer(){
        var opt = Main.getPreferences().get("FileOptimizer", FileOptimizer.Enum.NONE.toString());
        FileOptimizer.Enum optEnum;
        try {
            optEnum = FileOptimizer.Enum.valueOf(opt);
        } catch(IllegalArgumentException e){
            optEnum = FileOptimizer.Enum.NONE;
        }
        return optEnum;
    }

    public static boolean isOptimiserEnabled(Enum e){
        return  getCurrentOptimizer() == e;
    }

    public static PDDocument optimizeWithPdfBox(PDDocument srcDoc){
        var doc = new PDDocument();
        doc.setDocumentInformation(srcDoc.getDocumentInformation());
        doc.getDocumentCatalog().setMetadata(srcDoc.getDocumentCatalog().getMetadata());
        for(var i =0 ; i < srcDoc.getNumberOfPages(); i++){
            doc.addPage(srcDoc.getPage(i));
        }
        return doc;
    }

}
