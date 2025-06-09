package pmedit.ext;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.MetadataCollection;
import pmedit.MetadataInfo;
import pmedit.MetadataInfoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class BasicPdfReader implements PdfReader{
    static Logger LOG = LoggerFactory.getLogger(BasicPdfWriter.class);

    protected static String[] hrSizes = new String[]{"B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    protected static Set<COSName> OBJ_STM_COMPRESSION_FILTERS = Set.of(COSName.FLATE_DECODE, COSName.FLATE_DECODE_ABBREVIATION, COSName.LZW_DECODE, COSName.LZW_DECODE_ABBREVIATION);

    protected boolean hasCompressedObjects(PDDocument document) {
        COSDocument cosDoc = document.getDocument();

        // Check if any objects are COSStream with /Type /ObjStm
        for (COSBase obj : cosDoc.getObjectsByType(COSName.OBJ_STM)) {
            if (obj instanceof COSObject objStm) {
                // Get the filter(s) used
                if (objStm.getObject() instanceof COSDictionary objStmDict) {
                    COSBase filterObj = objStmDict.getDictionaryObject(COSName.FILTER);

                    if (filterObj instanceof COSName filter) {
                        if (OBJ_STM_COMPRESSION_FILTERS.contains(filter)) {
                            return true;
                        }

                    } else if (filterObj instanceof COSArray filters) {
                        for (int i = 0; i < filters.size(); i++) {
                            COSName filter = (COSName) filters.getObject(i);
                            if (OBJ_STM_COMPRESSION_FILTERS.contains(filter)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void loadFromPDF(PDDocument document, MetadataCollection mc) throws IOException, XmpParsingException, IllegalArgumentException {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("loadFromPDF:",e);
            throw e;
        }

        PDDocumentInformation info = document.getDocumentInformation();

        // Basic info
        md.doc.title = info.getTitle();
        md.doc.author = info.getAuthor();
        md.doc.subject = info.getSubject();
        md.doc.keywords = info.getKeywords();
        md.doc.creator = info.getCreator();
        md.doc.producer = info.getProducer();
        md.doc.creationDate = info.getCreationDate();
        md.doc.modificationDate = info.getModificationDate();
        md.doc.trapped = info.getTrapped();

        // Load Document catalog
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSDictionary cd = catalog.getCOSObject();
        if (cd.containsKey(COSName.PAGE_MODE)) {
            md.viewer.pageMode = catalog.getPageMode().stringValue();
        }
        if (cd.containsKey(COSName.PAGE_LAYOUT)) {
            md.viewer.pageLayout = catalog.getPageLayout().stringValue();
        }
        // For initial page and zoom
        // catalog.getOpenAction()
        PDViewerPreferences preferences = catalog.getViewerPreferences();
        if (preferences != null) {
            COSDictionary pd = preferences.getCOSObject();
            if (pd.containsKey(COSName.HIDE_TOOLBAR)) {
                md.viewer.hideToolbar = preferences.hideToolbar();
            }
            if (pd.containsKey(COSName.HIDE_MENUBAR)) {
                md.viewer.hideMenuBar = preferences.hideMenubar();
            }
            if (pd.containsKey(COSName.HIDE_WINDOWUI)) {
                md.viewer.hideWindowUI = preferences.hideWindowUI();
            }
            if (pd.containsKey(COSName.FIT_WINDOW)) {
                md.viewer.fitWindow = preferences.fitWindow();
            }
            if (pd.containsKey(COSName.CENTER_WINDOW)) {
                md.viewer.centerWindow = preferences.centerWindow();
            }
            if (pd.containsKey(COSName.DISPLAY_DOC_TITLE)) {
                md.viewer.displayDocTitle = preferences.displayDocTitle();
            }
            if (pd.containsKey(COSName.NON_FULL_SCREEN_PAGE_MODE)) {
                md.viewer.nonFullScreenPageMode = preferences.getNonFullScreenPageMode();
            }
            if (pd.containsKey(COSName.DIRECTION)) {
                md.viewer.readingDirection = preferences.getReadingDirection();
            }
            if (pd.containsKey(COSName.VIEW_AREA)) {
                md.viewer.viewArea = preferences.getViewArea();
            }
            if (pd.containsKey(COSName.VIEW_CLIP)) {
                md.viewer.viewClip = preferences.getViewClip();
            }
            if (pd.containsKey(COSName.PRINT_AREA)) {
                md.viewer.printArea = preferences.getPrintArea();
            }
            if (pd.containsKey(COSName.PRINT_CLIP)) {
                md.viewer.printClip = preferences.getPrintClip();
            }
            if (pd.containsKey(COSName.DUPLEX)) {
                md.viewer.duplex = preferences.getDuplex();
            }
            if (pd.containsKey(COSName.PRINT_SCALING)) {
                md.viewer.printScaling = preferences.getPrintScaling();
            }

        }

        // Load XMP Metadata
        PDMetadata meta = catalog.getMetadata();


        if (meta != null) {

            XMPMetadata xmp = MetadataInfoUtils.loadXMPMetadata(meta.createInputStream());

            // XMP Basic
            XMPBasicSchema bi = xmp.getXMPBasicSchema();
            if (bi != null) {

                md.basic.creatorTool = bi.getCreatorTool();
                md.basic.createDate = bi.getCreateDate();
                md.basic.modifyDate = bi.getModifyDate();
                md.basic.baseURL = bi.getBaseURL();
                md.basic.rating = bi.getRating();
                md.basic.label = bi.getLabel();
                md.basic.nickname = bi.getNickname();
                md.basic.identifiers = bi.getIdentifiers();
                md.basic.advisories = bi.getAdvisory();
                md.basic.metadataDate = bi.getMetadataDate();
            }

            // XMP PDF
            AdobePDFSchema pi = xmp.getAdobePDFSchema();
            if (pi != null) {
                md.pdf.pdfVersion = pi.getPDFVersion();
                md.pdf.keywords = pi.getKeywords();
                md.pdf.producer = pi.getProducer();
            }

            // XMP Dublin Core
            DublinCoreSchema dcS = xmp.getDublinCoreSchema();
            if (dcS != null) {
                try {
                    md.dc.title = dcS.getTitle();
                } catch (BadFieldValueException e) {
                    md.dc.title = "[INVALID FIELD VALUE]";
                }
                try {
                    md.dc.description = dcS.getDescription();
                } catch (BadFieldValueException e) {
                    md.dc.description = "[INVALID FIELD VALUE]";
                }
                md.dc.creators = dcS.getCreators();
                md.dc.contributors = dcS.getContributors();
                md.dc.coverage = dcS.getCoverage();
                md.dc.dates = dcS.getDates();
                md.dc.format = dcS.getFormat();
                md.dc.identifier = dcS.getIdentifier();
                md.dc.languages = dcS.getLanguages();
                // It appears there are some PDF out there where the languages is stored as plain
                // string instead of list. Try to workaround that.
                if (md.dc.languages == null) {
                    var s = dcS.getProperty(DublinCoreSchema.LANGUAGE);
                    if (s instanceof TextType) {
                        md.dc.languages = List.of(((TextType) s).getStringValue());
                    }
                }
                md.dc.publishers = dcS.getPublishers();
                md.dc.relationships = dcS.getRelations();
                try {
                    md.dc.rights = dcS.getRights();
                } catch (BadFieldValueException e) {
                    md.dc.title = "[INVALID FIELD VALUE]";
                }
                md.dc.source = dcS.getSource();
                md.dc.subjects = dcS.getSubjects();
                md.dc.types = dcS.getTypes();
            }

            // XMP Rights
            XMPRightsManagementSchema ri = xmp.getXMPRightsManagementSchema();
            if (ri != null) {
                md.rights.certificate = ri.getCertificate();
                // rights.marked  = ri.getMarked(); // getMarked() return false on null value
                md.rights.marked = ri.getMarked();
                md.rights.owner = ri.getOwners();
                try {
                    md.rights.usageTerms = ri.getUsageTerms();
                } catch (BadFieldValueException e) {
                    md.rights.usageTerms = "[INVALID FIELD VALUE]";
                }
                var c = ri.getProperty("Copyright");
                md.rights.webStatement = ri.getWebStatement();
            }
        }

        // Load encryption options
        PDEncryption enc = document.getEncryption();
        boolean hasEncryption = enc != null;
        md.prop.version = document.getVersion();
        md.prop.compression = hasCompressedObjects(document);
        if (hasEncryption) {
            md.prop.encryption = hasEncryption;

            AccessPermission permission = hasEncryption ? new AccessPermission(enc.getPermissions()) : new AccessPermission();
            md.prop.canPrint = permission.canPrint();
            md.prop.canModify = permission.canModify();
            md.prop.canExtractContent = permission.canExtractContent();
            md.prop.canModifyAnnotations = permission.canModifyAnnotations();
            md.prop.canFillFormFields = permission.canFillInForm();
            md.prop.canExtractForAccessibility = permission.canExtractForAccessibility();
            md.prop.canAssembleDocument = permission.canAssembleDocument();
            md.prop.canPrintFaithful = permission.canPrintFaithful();
            md.prop.keyLength = enc.getSecurityHandler().getKeyLength();
        }

    }

    @Override
    public void loadFromPDF(File pdfFile, MetadataCollection mc) throws IOException, XmpParsingException {
        loadFromPDF(pdfFile, null, mc);
    }

    @Override
    public void loadFromPDF(File pdfFile, String ownerPassword, MetadataCollection mc) throws IOException, XmpParsingException {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("loadFromPDF:",e);
            throw e;
        }

        loadPDFFileInfo(pdfFile, mc);

        PDDocument document = Loader.loadPDF(pdfFile, ownerPassword != null ? ownerPassword : "");
        loadFromPDF(document, mc);
        if(ownerPassword != null){
            md.prop.ownerPassword = ownerPassword;
        }

        document.close();
    }

    @Override
    public void loadPDFFileInfo(File pdfFile, MetadataCollection mc) throws IOException {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("loadPDFFileInfo:",e);
            throw e;
        }

        md.file.fullPath = pdfFile.getAbsolutePath();
        md.file.nameWithExt = pdfFile.getName();
        BasicFileAttributes attrs = Files.readAttributes(pdfFile.toPath(), BasicFileAttributes.class);
        md.file.sizeBytes = attrs.size();
        md.file.createTime = Calendar.getInstance();
        md.file.createTime.setTimeInMillis(attrs.creationTime().toInstant().toEpochMilli());
        md.file.modifyTime = Calendar.getInstance();
        md.file.modifyTime.setTimeInMillis(attrs.lastModifiedTime().toInstant().toEpochMilli());

        // filename w/o extension
        if (md.file.nameWithExt != null) {
            int dotPos = md.file.nameWithExt.lastIndexOf('.');
            if (dotPos >= 0) {
                md.file.name = md.file.nameWithExt.substring(0, dotPos);
            } else {
                md.file.name = md.file.nameWithExt;
            }
        }
        // human readable file size
        double size = md.file.sizeBytes;
        int idx;
        for (idx = 0; idx < hrSizes.length; ++idx) {
            if (size < 1000) {
                break;
            }
            size /= 1000;
        }
        md.file.size = String.format("%.2f%s", size, hrSizes[idx]);
    }
}
