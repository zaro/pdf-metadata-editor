package pmedit.ext;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.xml.XmpParsingException;
import pmedit.MetadataCollection;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PdfReader {
    void loadFromPDF(PDDocument document, MetadataCollection mc) throws IOException, XmpParsingException ;
    void loadFromPDF(File pdfFile, MetadataCollection mc) throws IOException, XmpParsingException ;
    void loadFromPDF(File pdfFile, String ownerPassword, MetadataCollection mc) throws IOException, XmpParsingException ;
    void loadPDFFileInfo(File pdfFile, MetadataCollection mc) throws IOException;

}
