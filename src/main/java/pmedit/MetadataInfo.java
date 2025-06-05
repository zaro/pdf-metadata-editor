package pmedit;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryption;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.xml.XmpParsingException;
import pmedit.CommandLine.ParseError;
import pmedit.annotations.FieldDataType;
import pmedit.annotations.MdStruct;
import pmedit.annotations.MdStruct.StructType;
import pmedit.ext.PdfWriter;
import pmedit.ext.PmeExtension;
import pmedit.serdes.SerDeslUtils;
import pmedit.util.CrossPlatformFileTimeModifier;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;

public class MetadataInfo {

    final static Map<String, List<FieldDescription>> _mdFields;
    final static Map<String, List<FieldDescription>> _mdEnabledFields;
    protected static String[] hrSizes = new String[]{"B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    static {
        _mdFields = new LinkedHashMap<String, List<FieldDescription>>();
        _mdEnabledFields = new LinkedHashMap<String, List<FieldDescription>>();
        traverseFields(new ArrayList<FieldDescription>(), false, MetadataInfo.class, StructType.MdStruct, new Function<List<FieldDescription>, Void>() {
            @Override
            public Void apply(List<FieldDescription> t) {
                if (t.size() > 0) {
                    _mdFields.put(t.get(t.size() - 1).name, t);
                }
                return null;
            }
        });
        traverseFields(new ArrayList<FieldDescription>(), false, MetadataInfo.class, StructType.MdEnableStruct, new Function<List<FieldDescription>, Void>() {
            @Override
            public Void apply(List<FieldDescription> t) {
                if (t.size() > 0) {
                    _mdEnabledFields.put(t.get(t.size() - 1).name, t);
                }
                return null;
            }
        });
    }

    @MdStruct
    public Basic doc;

    @MdStruct
    public XmpBasic basic;

    @MdStruct
    public XmpPdf pdf;

    @MdStruct
    public XmpDublinCore dc;

    @MdStruct
    public XmpRights rights;

    @MdStruct
    public ViewerOptions viewer;

    @MdStruct(name = "file", type = MdStruct.StructType.MdStruct, access = MdStruct.Access.ReadOnly)
    public FileInfo file;

    @MdStruct(name = "doc", type = MdStruct.StructType.MdEnableStruct)
    public BasicEnabled docEnabled;

    @MdStruct(name = "basic", type = MdStruct.StructType.MdEnableStruct)
    public XmpBasicEnabled basicEnabled;
    @MdStruct(name = "pdf", type = MdStruct.StructType.MdEnableStruct)
    public XmpPdfEnabled pdfEnabled;
    @MdStruct(name = "dc", type = MdStruct.StructType.MdEnableStruct)
    public XmpDublinCoreEnabled dcEnabled;
    @MdStruct(name = "rights", type = MdStruct.StructType.MdEnableStruct)
    public XmpRightsEnabled rightsEnabled;

    @MdStruct(name = "viewer", type = MdStruct.StructType.MdEnableStruct)
    public ViewerOptionsEnabled viewerEnabled;

    @MdStruct(name = "file", type = MdStruct.StructType.MdEnableStruct, access = MdStruct.Access.ReadOnly)
    public FileInfoEnabled fileEnabled;

    public boolean removeDocumentInfo;
    public boolean removeXmp;
    public EncryptionOptions encryptionOptions;


    public MetadataInfo() {
        super();
        clear();
    }

    public static List<String> keys() {
        return new ArrayList<String>(_mdFields.keySet());
    }

    public static boolean keyIsWritable(String key) {
        FieldDescription fd = getFieldDescription(key);
        return fd != null && fd.isWritable;
    }

    public static MetadataInfo fromPersistenceString(String yamlString) {
        Map<String, Object> map = (Map<String, Object>) SerDeslUtils.fromYAML(yamlString);

        MetadataInfo md = new MetadataInfo();
        md.fromYAML(yamlString);

        Object enMap = map.get("_enabled");
        if (enMap != null && Map.class.isAssignableFrom(enMap.getClass())) {
            Map<String, Object> enabledMap = (Map<String, Object>) enMap;

            for (String fieldName : _mdEnabledFields.keySet()) {
                if (enabledMap.containsKey(fieldName)) {
                    md.setEnabled(fieldName, (Boolean) enabledMap.get(fieldName));
                }
            }
        }

        return md;
    }

    protected static void traverseFields(List<FieldDescription> ancestors, boolean all, Class<?> klass, MdStruct.StructType mdType, Function<List<FieldDescription>, Void> f) {
        for (Field field : klass.getFields()) {
            MdStruct mdStruct = field.getAnnotation(MdStruct.class);
            if (mdStruct != null && mdStruct.type() == mdType) {
                String prefix = ancestors.size() > 0 ? ancestors.get(ancestors.size() - 1).name : "";
                if (prefix.length() > 0) {
                    prefix += ".";
                }
                String name = mdStruct.name().length() > 0 ? mdStruct.name() : field.getName();
                FieldDescription t = new FieldDescription(prefix + name, field, mdStruct.access() == MdStruct.Access.ReadWrite);
                List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
                a.add(t);
                traverseFields(a, true, field.getType(), mdType, f);
            } else {
                FieldDataType fieldType = field.getAnnotation(FieldDataType.class);
                boolean isParentWritable = ancestors.size() <= 0 || ancestors.get(ancestors.size() - 1).isWritable;
                String prefix = ancestors.size() > 0 ? ancestors.get(ancestors.size() - 1).name : "";
                if (prefix.length() > 0) {
                    prefix += ".";
                }
                if (fieldType != null) {
                    FieldDescription t = new FieldDescription(prefix + field.getName(), field, fieldType, isParentWritable);
                    List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
                    a.add(t);
                    f.apply(a);
                } else if (all) {
                    FieldDescription t = new FieldDescription(prefix + field.getName(), field, isParentWritable);
                    List<FieldDescription> a = new ArrayList<FieldDescription>(ancestors);
                    a.add(t);
                    f.apply(a);
                }
            }
        }
    }

    public static FieldDescription getFieldDescription(String id) {
        List<FieldDescription> fields = _mdFields.get(id);
        if (fields.size() > 0) {
            return fields.get(fields.size() - 1);
        }
        return null;
    }

    public static MetadataInfo getSampleMetadata() {
        MetadataInfo md = new MetadataInfo();
        // Spec is at : http://partners.adobe.com/public/developer/en/xmp/sdk/XMPspecification.pdf
        md.doc.title = "Dracula";
        md.doc.author = "Bram Stoker";
        md.doc.subject = "Horror tales, Epistolary fiction, Gothic fiction (Literary genre), Vampires -- Fiction, Dracula, Count (Fictitious character) -- Fiction, Transylvania (Romania) -- Fiction, Whitby (England) -- Fiction";
        md.doc.keywords = "Horror, Gothic, Vampires";
        md.doc.creator = "Adobe InDesign CS4 (6.0.6)";
        md.doc.producer = "Adobe PDF Library 9.0";
        md.doc.creationDate = DateFormat.parseDateOrNull("2012-12-12 00:00:00");
        md.doc.modificationDate = DateFormat.parseDateOrNull("2012-12-13 00:00:00");
        md.doc.trapped = "True";

        md.basic.creatorTool = "Adobe InDesign CS4 (6.0.6)";
        md.basic.createDate = md.doc.creationDate;
        md.basic.modifyDate = md.doc.modificationDate;
        md.basic.baseURL = "https://www.gutenberg.org/";
        md.basic.rating = 3;
        md.basic.label = "Horror Fiction Collection";
        md.basic.nickname = "dracula";
        md.basic.identifiers = List.of("Dracula_original_edition");
        //md.xmpBasic.advisories ;
        md.basic.metadataDate = DateFormat.parseDateOrNull("2012-12-14 00:00:00");

        md.pdf.pdfVersion = "1.5";
        md.pdf.keywords = md.doc.keywords;
        md.pdf.producer = "Adobe PDF Library 9.0";

        md.dc.title = md.doc.title;
        md.dc.description = "The famous Bram Stocker book";
        md.dc.creators = new ArrayList<String>();
        md.dc.creators.add("Bram Stocker");
        md.dc.subjects = Arrays.asList(md.doc.subject.split("\\s*,\\s*"));

        return md;
    }

    public void clear() {
        this.doc = new Basic();
        this.basic = new XmpBasic();
        this.pdf = new XmpPdf();
        this.dc = new XmpDublinCore();
        this.rights = new XmpRights();
        this.viewer = new ViewerOptions();
        this.file = new FileInfo();

        this.docEnabled = new BasicEnabled();
        this.basicEnabled = new XmpBasicEnabled();
        this.pdfEnabled = new XmpPdfEnabled();
        this.dcEnabled = new XmpDublinCoreEnabled();
        this.rightsEnabled = new XmpRightsEnabled();
        this.viewerEnabled = new ViewerOptionsEnabled();
        this.fileEnabled = new FileInfoEnabled();
    }

    protected static Set<COSName> OBJ_STM_COMPRESSION_FILTERS = Set.of(COSName.FLATE_DECODE, COSName.FLATE_DECODE_ABBREVIATION, COSName.LZW_DECODE, COSName.LZW_DECODE_ABBREVIATION);
    protected boolean hasCompressedObjects(PDDocument document) {
        COSDocument cosDoc = document.getDocument();

        // Check if any objects are COSStream with /Type /ObjStm
        for (COSBase obj : cosDoc.getObjectsByType(COSName.OBJ_STM)) {
            if (obj instanceof COSObject objStm) {
                // Get the filter(s) used
                if(objStm.getObject() instanceof COSDictionary objStmDict ) {
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

    public void loadFromPDF(PDDocument document) throws IOException, XmpParsingException {
        PDDocumentInformation info = document.getDocumentInformation();

        // Basic info
        doc.title = info.getTitle();
        doc.author = info.getAuthor();
        doc.subject = info.getSubject();
        doc.keywords = info.getKeywords();
        doc.creator = info.getCreator();
        doc.producer = info.getProducer();
        doc.creationDate = info.getCreationDate();
        doc.modificationDate = info.getModificationDate();
        doc.trapped = info.getTrapped();
        file.pdfVersion = document.getVersion();
        file.pdfCompression = hasCompressedObjects(document);

        // Load Document catalog
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSDictionary cd = catalog.getCOSObject();
        if(cd.containsKey(COSName.PAGE_MODE)){
            viewer.pageMode = catalog.getPageMode().stringValue();
        }
        if(cd.containsKey(COSName.PAGE_LAYOUT)){
            viewer.pageLayout = catalog.getPageLayout().stringValue();
        }
        // For initial page and zoom
        // catalog.getOpenAction()
        PDViewerPreferences preferences = catalog.getViewerPreferences();
        if(preferences != null ){
            COSDictionary pd = preferences.getCOSObject();
            if(pd.containsKey(COSName.HIDE_TOOLBAR)) {
                viewer.hideToolbar = preferences.hideToolbar();
            }
            if(pd.containsKey(COSName.HIDE_MENUBAR)) {
                viewer.hideMenuBar = preferences.hideMenubar();
            }
            if(pd.containsKey(COSName.HIDE_WINDOWUI)) {
                viewer.hideWindowUI = preferences.hideWindowUI();
            }
            if(pd.containsKey(COSName.FIT_WINDOW)) {
                viewer.fitWindow = preferences.fitWindow();
            }
            if(pd.containsKey(COSName.CENTER_WINDOW)) {
                viewer.centerWindow = preferences.centerWindow();
            }
            if(pd.containsKey(COSName.DISPLAY_DOC_TITLE)) {
                viewer.displayDocTitle = preferences.displayDocTitle();
            }
            if(pd.containsKey(COSName.NON_FULL_SCREEN_PAGE_MODE)) {
                viewer.nonFullScreenPageMode = preferences.getNonFullScreenPageMode();
            }
            if(pd.containsKey(COSName.DIRECTION)) {
                viewer.readingDirection = preferences.getReadingDirection();
            }
            if(pd.containsKey(COSName.VIEW_AREA)) {
                viewer.viewArea = preferences.getViewArea();
            }
            if(pd.containsKey(COSName.VIEW_CLIP)) {
                viewer.viewClip = preferences.getViewClip();
            }
            if(pd.containsKey(COSName.PRINT_AREA)) {
                viewer.printArea = preferences.getPrintArea();
            }
            if(pd.containsKey(COSName.PRINT_CLIP)) {
                viewer.printClip = preferences.getPrintClip();
            }
            if(pd.containsKey(COSName.DUPLEX)) {
                viewer.duplex = preferences.getDuplex();
            }
            if(pd.containsKey(COSName.PRINT_SCALING)) {
                viewer.printScaling = preferences.getPrintScaling();
            }

        }

        // Load XMP Metadata
        PDMetadata meta = catalog.getMetadata();


        if (meta != null) {

            XMPMetadata xmp = MetadataInfoUtils.loadXMPMetadata(meta.createInputStream());

            // XMP Basic
            XMPBasicSchema bi = xmp.getXMPBasicSchema();
            if (bi != null) {

                basic.creatorTool = bi.getCreatorTool();
                basic.createDate = bi.getCreateDate();
                basic.modifyDate = bi.getModifyDate();
                basic.baseURL = bi.getBaseURL();
                basic.rating = bi.getRating();
                basic.label = bi.getLabel();
                basic.nickname = bi.getNickname();
                basic.identifiers = bi.getIdentifiers();
                basic.advisories = bi.getAdvisory();
                basic.metadataDate = bi.getMetadataDate();
            }

            // XMP PDF
            AdobePDFSchema pi = xmp.getAdobePDFSchema();
            if (pi != null) {
                pdf.pdfVersion = pi.getPDFVersion();
                pdf.keywords = pi.getKeywords();
                pdf.producer = pi.getProducer();
            }

            // XMP Dublin Core
            DublinCoreSchema dcS = xmp.getDublinCoreSchema();
            if (dcS != null) {
                try {
                    dc.title = dcS.getTitle();
                } catch (BadFieldValueException e) {
                    dc.title = "[INVALID FIELD VALUE]";
                }
                try {
                    dc.description = dcS.getDescription();
                } catch (BadFieldValueException e) {
                    dc.description = "[INVALID FIELD VALUE]";
                }
                dc.creators = dcS.getCreators();
                dc.contributors = dcS.getContributors();
                dc.coverage = dcS.getCoverage();
                dc.dates = dcS.getDates();
                dc.format = dcS.getFormat();
                dc.identifier = dcS.getIdentifier();
                dc.languages = dcS.getLanguages();
                // It appears there are some PDF out there where the languages is stored as plain
                // string instead of list. Try to workaround that.
                if (dc.languages == null) {
                    var s = dcS.getProperty(DublinCoreSchema.LANGUAGE);
                    if (s instanceof TextType) {
                        dc.languages = List.of(((TextType) s).getStringValue());
                    }
                }
                dc.publishers = dcS.getPublishers();
                dc.relationships = dcS.getRelations();
                try {
                    dc.rights = dcS.getRights();
                } catch (BadFieldValueException e) {
                    dc.title = "[INVALID FIELD VALUE]";
                }
                dc.source = dcS.getSource();
                dc.subjects = dcS.getSubjects();
                dc.types = dcS.getTypes();
            }

            // XMP Rights
            XMPRightsManagementSchema ri = xmp.getXMPRightsManagementSchema();
            if (ri != null) {
                rights.certificate = ri.getCertificate();
                // rights.marked  = ri.getMarked(); // getMarked() return false on null value
                rights.marked = ri.getMarked();
                rights.owner = ri.getOwners();
                try {
                    rights.usageTerms = ri.getUsageTerms();
                } catch (BadFieldValueException e) {
                    dc.title = "[INVALID FIELD VALUE]";
                }
                var c = ri.getProperty("Copyright");
                rights.webStatement = ri.getWebStatement();
            }
        }

        // Load encryption options
        PDEncryption enc = document.getEncryption();
        boolean hasEncryption = enc != null;
        AccessPermission permission =  hasEncryption ? new AccessPermission(enc.getPermissions()) : new AccessPermission();
        encryptionOptions = new EncryptionOptions(hasEncryption, permission, null, null);
        //System.err.println("Loaded:");
        //System.err.println(toYAML());

    }

    public void loadFromPDF(File pdfFile) throws
            IOException, XmpParsingException {

        loadPDFFileInfo(pdfFile);

        PDDocument document = Loader.loadPDF(pdfFile);
        loadFromPDF(document);

        document.close();
    }

    public void loadPDFFileInfo(File pdfFile) throws IOException {
        file.fullPath = pdfFile.getAbsolutePath();
        file.nameWithExt = pdfFile.getName();
        BasicFileAttributes attrs = Files.readAttributes(pdfFile.toPath(), BasicFileAttributes.class);
        file.sizeBytes = attrs.size();
        file.createTime = Calendar.getInstance();
        file.createTime.setTimeInMillis(attrs.creationTime().toInstant().toEpochMilli());
        file.modifyTime = Calendar.getInstance();
        file.modifyTime.setTimeInMillis(attrs.lastModifiedTime().toInstant().toEpochMilli());

        // filename w/o extension
        if (file.nameWithExt != null) {
            int dotPos = file.nameWithExt.lastIndexOf('.');
            if (dotPos >= 0) {
                file.name = file.nameWithExt.substring(0, dotPos);
            } else {
                file.name = file.nameWithExt;
            }
        }
        // human readable file size
        double size = file.sizeBytes;
        int idx;
        for (idx = 0; idx < hrSizes.length; ++idx) {
            if (size < 1000) {
                break;
            }
            size /= 1000;
        }
        file.size = String.format("%.2f%s", size, hrSizes[idx]);
    }

    protected boolean saveToPDF(PDDocument document, File pdfFile) throws Exception {
        boolean atLeastOneChange =
                docEnabled.atLeastOne() || basicEnabled.atLeastOne() || pdfEnabled.atLeastOne() || dcEnabled.atLeastOne() || rightsEnabled.atLeastOne()
                        || FileOptimizer.isOptimiserEnabled(FileOptimizer.Enum.PDFBOX)
                        || removeXmp || removeDocumentInfo;

        if (!atLeastOneChange) {
            return false;
        }
        //System.err.println("Saving:");
        //System.err.println(toYAML());
        // Basic info
        if (docEnabled.atLeastOne()) {
            PDDocumentInformation info = document.getDocumentInformation();
            if (docEnabled.title) {
                info.setTitle(doc.title);
            }
            if (docEnabled.author) {
                info.setAuthor(doc.author);
            }
            if (docEnabled.subject) {
                info.setSubject(doc.subject);
            }
            if (docEnabled.keywords) {
                info.setKeywords(doc.keywords);
            }
            if (docEnabled.creator) {
                info.setCreator(doc.creator);
            }
            if (docEnabled.producer) {
                info.setProducer(doc.producer);
            }
            if (docEnabled.creationDate) {
                info.setCreationDate(doc.creationDate);
            }
            if (docEnabled.modificationDate) {
                info.setModificationDate(doc.modificationDate);
            }
            if (docEnabled.trapped) {
                info.setTrapped(doc.trapped);
            }
            document.setDocumentInformation(info);
        }

        // XMP
        PDDocumentCatalog catalog = document.getDocumentCatalog();

        if(viewerEnabled.atLeastOne()){
            COSDictionary cd = catalog.getCOSObject();
            if(viewerEnabled.pageMode){
                cd.setName(COSName.PAGE_MODE, viewer.pageMode);
            }
            if(viewerEnabled.pageLayout){
                cd.setName(COSName.PAGE_LAYOUT, viewer.pageLayout);
            }
            if(viewerEnabled.atLeastOnePreference()){
                PDViewerPreferences preferences = catalog.getViewerPreferences();
                if(preferences == null ) {
                    preferences = new PDViewerPreferences(cd);
                }
                COSDictionary pd = preferences.getCOSObject();
                if (viewerEnabled.hideToolbar ) {
                    if(viewer.hideToolbar != null) {
                        preferences.setHideToolbar(viewer.hideToolbar);
                    } else {
                        pd.removeItem(COSName.HIDE_TOOLBAR);
                    }
                }
                if (viewerEnabled.hideMenuBar ) {
                    if(viewer.hideMenuBar != null) {
                        preferences.setHideMenubar(viewer.hideMenuBar);
                    } else {
                        pd.removeItem(COSName.HIDE_MENUBAR);
                    }
                }
                if (viewerEnabled.hideWindowUI ) {
                    if(viewer.hideWindowUI != null) {
                        preferences.setHideWindowUI(viewer.hideWindowUI);
                    } else {
                        pd.removeItem(COSName.HIDE_WINDOWUI);
                    }
                }
                if (viewerEnabled.fitWindow ) {
                    if(viewer.fitWindow != null) {
                        preferences.setFitWindow(viewer.fitWindow);
                    } else {
                        pd.removeItem(COSName.FIT_WINDOW);
                    }
                }
                if (viewerEnabled.centerWindow ) {
                    if(viewer.centerWindow != null) {
                        preferences.setCenterWindow(viewer.centerWindow);
                    } else {
                        pd.removeItem(COSName.CENTER_WINDOW);
                    }
                }
                if (viewerEnabled.displayDocTitle ) {
                    if(viewer.displayDocTitle != null) {
                        preferences.setDisplayDocTitle(viewer.displayDocTitle);
                    } else {
                        pd.removeItem(COSName.DISPLAY_DOC_TITLE);
                    }
                }
                if (viewerEnabled.nonFullScreenPageMode ) {
                    pd.setName(COSName.NON_FULL_SCREEN_PAGE_MODE, viewer.nonFullScreenPageMode);
                }
                if (viewerEnabled.readingDirection ) {
                    pd.setName(COSName.DIRECTION, viewer.readingDirection);
                }
                if (viewerEnabled.viewArea ) {
                    pd.setName(COSName.VIEW_AREA, viewer.viewArea);
                }
                if (viewerEnabled.viewClip ) {
                    pd.setName(COSName.VIEW_CLIP, viewer.viewClip);
                }
                if (viewerEnabled.printArea ) {
                    pd.setName(COSName.PRINT_AREA, viewer.printArea);
                }
                if (viewerEnabled.printClip ) {
                    pd.setName(COSName.PRINT_CLIP,viewer.printClip );
                }
                if (viewerEnabled.duplex ) {
                    pd.setName(COSName.DUPLEX, viewer.duplex);
                }
                if (viewerEnabled.printScaling ) {
                    pd.setName(COSName.PRINT_SCALING, viewer.printScaling);
                }
                catalog.setViewerPreferences(preferences);
            }
        }
        PDMetadata meta = catalog.getMetadata();

        XMPMetadata xmpOld = null;
        if (meta != null) {
            xmpOld = MetadataInfoUtils.loadXMPMetadata(meta.createInputStream());
        }
        XMPMetadata xmpNew = XMPMetadata.createXMPMetadata();
        XmpSchemaOnDemand newXmp = new XmpSchemaOnDemand(xmpNew);
        // XMP Basic
        XMPBasicSchema biOld = xmpOld != null ? xmpOld.getXMPBasicSchema() : null;
        boolean atLeastOneXmpBasicSet = false;
        if (basicEnabled.atLeastOne() || (biOld != null)) {

            if (basicEnabled.advisories) {
                if (basic.advisories != null) {
                    for (String a : basic.advisories) {
                        newXmp.basic().addAdvisory(a);
                        atLeastOneXmpBasicSet = true;
                    }
                }
            } else if (biOld != null) {
                List<String> old = biOld.getAdvisory();
                if (old != null) {
                    for (String a : old) {
                        newXmp.basic().addAdvisory(a);
                        atLeastOneXmpBasicSet = true;
                    }
                }
            }

            if (basicEnabled.baseURL) {
                if (basic.baseURL != null) {
                    newXmp.basic().setBaseURL(basic.baseURL);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String baseUrl = biOld.getBaseURL();
                if (baseUrl != null) {
                    newXmp.basic().setBaseURL(baseUrl);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.createDate) {
                if (basic.createDate != null) {
                    newXmp.basic().setCreateDate(basic.createDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getCreateDate();
                if (old != null) {
                    newXmp.basic().setCreateDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.modifyDate) {
                if (basic.modifyDate != null) {
                    newXmp.basic().setModifyDate(basic.modifyDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getModifyDate();
                if (old != null) {
                    newXmp.basic().setModifyDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.creatorTool) {
                if (basic.creatorTool != null) {
                    newXmp.basic().setCreatorTool(basic.creatorTool);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getCreatorTool();
                if (old != null) {
                    newXmp.basic().setCreatorTool(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.identifiers) {
                if (basic.identifiers != null) {
                    for (String i : basic.identifiers) {
                        newXmp.basic().addIdentifier(i);
                        atLeastOneXmpBasicSet = true;
                    }
                }
            } else if (biOld != null) {
                List<String> old = biOld.getIdentifiers();
                if (old != null) {
                    for (String a : old) {
                        newXmp.basic().addIdentifier(a);
                        atLeastOneXmpBasicSet = true;
                    }
                }
            }

            if (basicEnabled.label) {
                if (basic.label != null) {
                    newXmp.basic().setLabel(basic.label);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getLabel();
                if (old != null) {
                    newXmp.basic().setLabel(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.metadataDate) {
                if (basic.metadataDate != null) {
                    newXmp.basic().setMetadataDate(basic.metadataDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getMetadataDate();
                if (old != null) {
                    newXmp.basic().setMetadataDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (basicEnabled.nickname) {
                if (basic.nickname != null) {
                    newXmp.basic().setNickname(basic.nickname);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getNickname();
                if (old != null) {
                    newXmp.basic().setNickname(old);
                    atLeastOneXmpBasicSet = true;
                }
            }
            if (basicEnabled.rating) {
                if (basic.rating != null) {
                    newXmp.basic().setRating(basic.rating);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Integer old = biOld.getRating();
                if (old != null) {
                    newXmp.basic().setRating(old);
                    atLeastOneXmpBasicSet = true;
                }
            }
        }
        // XMP PDF
        AdobePDFSchema piOld = xmpOld != null ? xmpOld.getAdobePDFSchema() : null;
        boolean atLeastOneXmpPdfSet = false;
        if (pdfEnabled.atLeastOne() || (piOld != null)) {

            if (pdfEnabled.keywords) {
                if (pdf.keywords != null) {
                    newXmp.pdf().setKeywords(pdf.keywords);
                    atLeastOneXmpPdfSet = true;
                }
            } else if (piOld != null) {
                String old = piOld.getKeywords();
                if (old != null) {
                    newXmp.pdf().setKeywords(old);
                    atLeastOneXmpPdfSet = true;
                }
            }

            if (pdfEnabled.producer) {
                if (pdf.producer != null) {
                    newXmp.pdf().setProducer(pdf.producer);
                    atLeastOneXmpPdfSet = true;
                }
            } else if (piOld != null) {
                String old = piOld.getProducer();
                if (old != null) {
                    newXmp.pdf().setProducer(old);
                    atLeastOneXmpPdfSet = true;
                }
            }

            if (pdfEnabled.pdfVersion) {
                if (pdf.pdfVersion != null) {
                    newXmp.pdf().setPDFVersion(pdf.pdfVersion);
                    atLeastOneXmpPdfSet = true;
                }
            } else if (piOld != null) {
                String old = piOld.getPDFVersion();
                if (old != null) {
                    newXmp.pdf().setPDFVersion(old);
                    atLeastOneXmpPdfSet = true;
                }
            }
        }

        // XMP Dublin Core
        DublinCoreSchema dcOld = xmpOld != null ? xmpOld.getDublinCoreSchema() : null;
        boolean atLeastOneXmpDcSet = false;
        if (dcEnabled.atLeastOne() || (dcOld != null)) {

            if (dcEnabled.title) {
                if (dc.title != null) {
                    newXmp.dc().setTitle(dc.title);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getTitle();
                if (old != null) {
                    newXmp.dc().setTitle(old);
                    atLeastOneXmpDcSet = true;
                }
            }
            //
            if (dcEnabled.contributors) {
                if (dc.contributors != null) {
                    for (String i : dc.contributors) {
                        newXmp.dc().addContributor(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getContributors();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addContributor(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.publishers) {
                if (dc.publishers != null) {
                    for (String i : dc.publishers) {
                        newXmp.dc().addPublisher(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getPublishers();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addPublisher(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.relationships) {
                if (dc.relationships != null) {
                    for (String i : dc.relationships) {
                        newXmp.dc().addRelation(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getRelations();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addRelation(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.subjects) {
                if (dc.subjects != null) {
                    for (String i : dc.subjects) {
                        newXmp.dc().addSubject(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getSubjects();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addSubject(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.types) {
                if (dc.types != null) {
                    for (String i : dc.types) {
                        newXmp.dc().addType(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getTypes();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addType(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.languages) {
                if (dc.languages != null) {
                    for (String i : dc.languages) {
                        newXmp.dc().addLanguage(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getLanguages();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addLanguage(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.creators) {
                if (dc.creators != null) {
                    for (String i : dc.creators) {
                        newXmp.dc().addCreator(i);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<String> old = dcOld.getCreators();
                if (old != null) {
                    for (String a : old) {
                        newXmp.dc().addCreator(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
            //
            if (dcEnabled.coverage) {
                if (dc.coverage != null) {
                    newXmp.dc().setCoverage(dc.coverage);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getCoverage();
                if (old != null) {
                    newXmp.dc().setCoverage(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (dcEnabled.format) {
                if (dc.format != null) {
                    newXmp.dc().setFormat(dc.format);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getFormat();
                if (old != null) {
                    newXmp.dc().setFormat(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (dcEnabled.identifier) {
                if (dc.identifier != null) {
                    newXmp.dc().setIdentifier(dc.identifier);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getIdentifier();
                if (old != null) {
                    newXmp.dc().setIdentifier(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (dcEnabled.rights) {
                if (dc.rights != null) {
                    newXmp.dc().addRights(null, dc.rights);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                List<String> rll = dcOld.getRightsLanguages();
                if (rll != null) {
                    for (String rl : rll) {
                        String rights = dcOld.getRights(rl);
                        if (rights != null) {
                            newXmp.dc().addRights(rl, rights);
                            atLeastOneXmpDcSet = true;
                        }
                    }
                }
            }

            if (dcEnabled.source) {
                if (dc.source != null) {
                    newXmp.dc().setSource(dc.source);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getSource();
                if (old != null) {
                    newXmp.dc().setSource(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (dcEnabled.description) {
                if (dc.description != null) {
                    newXmp.dc().setDescription(dc.description);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getDescription();
                if (old != null) {
                    newXmp.dc().setDescription(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (dcEnabled.dates) {
                if (dc.dates != null) {
                    for (Calendar date : dc.dates) {
                        newXmp.dc().addDate(date);
                        atLeastOneXmpDcSet = true;
                    }
                }
            } else if (dcOld != null) {
                List<Calendar> old = dcOld.getDates();
                if (old != null) {
                    for (Calendar a : old) {
                        newXmp.dc().addDate(a);
                        atLeastOneXmpDcSet = true;
                    }
                }
            }
        }

        // XMP Rights
        XMPRightsManagementSchema riOld = xmpOld != null ? xmpOld.getXMPRightsManagementSchema() : null;
        boolean atLeastOneXmpRightsSet = false;
        if (rightsEnabled.atLeastOne() || (riOld != null)) {

            if (rightsEnabled.certificate) {
                if (rights.certificate != null) {
                    newXmp.rights().setCertificate(rights.certificate);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                String old = riOld.getCertificate();
                if (old != null) {
                    newXmp.rights().setCertificate(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (rightsEnabled.marked) {
                if (rights.marked != null) {
                    newXmp.rights().setMarked(rights.marked);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                Boolean old = riOld.getMarked();
                if (old != null) {
                    newXmp.rights().setMarked(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (rightsEnabled.owner) {
                if (rights.owner != null) {
                    for (String i : rights.owner) {
                        newXmp.rights().addOwner(i);
                        atLeastOneXmpRightsSet = true;
                    }
                }
            } else if (riOld != null) {
                List<String> old = riOld.getOwners();
                if (old != null) {
                    for (String a : old) {
                        newXmp.rights().addOwner(a);
                        atLeastOneXmpRightsSet = true;
                    }
                }
            }

            if (rightsEnabled.usageTerms) {
                if (rights.usageTerms != null) {
                    newXmp.rights().setUsageTerms(rights.usageTerms);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                String old = riOld.getUsageTerms();
                if (old != null) {
                    newXmp.rights().setUsageTerms(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (rightsEnabled.webStatement) {
                if (rights.webStatement != null) {
                    newXmp.rights().setWebStatement(rights.webStatement);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                String old = riOld.getWebStatement();
                if (old != null) {
                    newXmp.rights().setWebStatement(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

        }

        // Do the save
        if (basicEnabled.atLeastOne() || pdfEnabled.atLeastOne() || dcEnabled.atLeastOne() || rightsEnabled.atLeastOne()) {
            if (atLeastOneXmpBasicSet || atLeastOneXmpPdfSet || atLeastOneXmpDcSet || atLeastOneXmpRightsSet) {
                PDMetadata metadataStream = new PDMetadata(document);
                try {
                    metadataStream.importXMPMetadata(MetadataInfoUtils.serializeXMPMetadata(xmpNew));
                } catch (TransformerException e) {
                    throw new Exception("Failed to save document:" + e.getMessage());
                }
                catalog.setMetadata(metadataStream);
            } else {
                catalog.setMetadata(null);
            }
        }

        PmeExtension.get().onDocumentSave(document, pdfFile, this);

        if (FileOptimizer.isOptimiserEnabled(FileOptimizer.Enum.PDFBOX)){
            document = FileOptimizer.optimizeWithPdfBox(document);
        }

        if(removeDocumentInfo) {
            document.getDocument().getTrailer().removeItem(COSName.INFO);
        }

        if(removeXmp) {
            document.getDocumentCatalog().setMetadata(null);
        }

        float saveAsVersion = file.pdfVersion != null ? file.pdfVersion : 0;
        if(saveAsVersion>0){
            if (saveAsVersion >= 1.4F) {
                document.getDocumentCatalog().setVersion(Float.toString(saveAsVersion));
                document.getDocument().setVersion(saveAsVersion);
            } else {
                document.getDocument().setVersion(saveAsVersion);
                document.getDocumentCatalog().setVersion(null);
            }
        }

        if(encryptionOptions !=null) {
            if (encryptionOptions.hasEncryption) {
                // Define the length of the encryption key.
                // Possible values are 40, 128 or 256.
                int keyLength = 40;

                AccessPermission ap = encryptionOptions.permission;
                String ownerPass = encryptionOptions.ownerPassword != null ? encryptionOptions.ownerPassword : "";
                String userPass = encryptionOptions.userPassword != null ? encryptionOptions.userPassword : "";

                StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPass, userPass, ap);
                spp.setEncryptionKeyLength(keyLength);

                document.protect(spp);
            } else {
                document.setAllSecurityToBeRemoved(true);
            }
        }

        PdfWriter writer = PmeExtension.get().createPdfWriter(document);
        int pdfCompression = 0 ;
        if(saveAsVersion >= writer.getCompressionMinimumSupportedVersion()) {
            if (fileEnabled.pdfCompression ) {
                pdfCompression = file.pdfCompression ? FileOptimizer.getPdfBoxCompression() : 0;
            } else {
                pdfCompression = document.getDocument().isXRefStream() ? FileOptimizer.getPdfBoxCompression() : 0;
            }
        }
        writer.write(pdfFile, pdfCompression);
        return true;
    }

    public File saveAsPDF(File pdfFile) throws Exception {
        return saveAsPDF(pdfFile, null);
    }

    public File saveAsPDF(File pdfFile, File newFile) throws Exception {
        PDDocument document = null;
        String password = encryptionOptions != null ? encryptionOptions.userPassword : "";
        document = Loader.loadPDF(pdfFile, password);
        File writeFile = File.createTempFile(pdfFile.getName() + "-", null, pdfFile.getParentFile());

        boolean fileSaved = saveToPDF(document, writeFile);
        if(!fileSaved){
            Files.copy(pdfFile.toPath(), writeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        document.close();

        File target = newFile != null ? newFile : pdfFile;
        if(fileEnabled.name) {
            target = new File(target.getParentFile(), file.name + ".pdf");
        }
        Files.move(writeFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if(fileEnabled.createTime || fileEnabled.modifyTime) {
            CrossPlatformFileTimeModifier.setFileTimes(target, file.createTime, file.modifyTime);
        }
        return target;
    }

    public void copyDocToXMP() {
        pdf.keywords = doc.keywords;
        pdf.producer = doc.producer;
        pdf.pdfVersion = String.format("%.1f", file.pdfVersion);
        pdfEnabled.keywords = docEnabled.keywords;
        pdfEnabled.producer = docEnabled.producer;
        pdfEnabled.pdfVersion = fileEnabled.pdfVersion;

        basic.createDate = doc.creationDate;
        basic.modifyDate = doc.modificationDate;
        basicEnabled.createDate = docEnabled.creationDate;
        basicEnabled.modifyDate = docEnabled.modificationDate;

        basic.creatorTool = doc.creator;
        basicEnabled.creatorTool = docEnabled.creator;

        dc.title = doc.title;
        dc.description = doc.subject;
        dc.creators = Arrays.asList(doc.author);
        dcEnabled.title = docEnabled.title;
        dcEnabled.description = docEnabled.subject;
        dcEnabled.creators = docEnabled.author;
    }

    public void copyXMPToDoc() {
        doc.keywords = pdf.keywords;
        doc.producer = pdf.producer;
        docEnabled.keywords = pdfEnabled.keywords;
        docEnabled.producer = pdfEnabled.producer;

        doc.creationDate = basic.createDate;
        doc.modificationDate = basic.modifyDate;
        docEnabled.creationDate = basicEnabled.createDate;
        docEnabled.modificationDate = basicEnabled.modifyDate;


        doc.creator = basic.creatorTool;
        docEnabled.creator = basicEnabled.creatorTool;

        doc.title = dc.title;
        doc.subject = dc.description;
        String author = "";
        if (dc.creators != null) {
            String delim = "";
            for (String creator : dc.creators) {
                author += delim + creator;
                delim = ", ";
            }
        } else {
            author = null;
        }
        doc.author = author;
        docEnabled.title = dcEnabled.title;
        docEnabled.subject = dcEnabled.description;
        docEnabled.author = dcEnabled.creators;

    }

    public void clearDoc() {
        this.doc = new Basic();
        this.docEnabled = new BasicEnabled();
    }

    public void clearXmp() {
        this.basic = new XmpBasic();
        this.pdf = new XmpPdf();
        this.dc = new XmpDublinCore();
        this.rights = new XmpRights();
        this.file = new FileInfo();

        this.basicEnabled = new XmpBasicEnabled();
        this.pdfEnabled = new XmpPdfEnabled();
        this.dcEnabled = new XmpDublinCoreEnabled();
        this.rightsEnabled = new XmpRightsEnabled();
        this.fileEnabled = new FileInfoEnabled();
    }

    public void setEnabled(boolean value) {
        docEnabled.setAll(value);
        basicEnabled.setAll(value);
        pdfEnabled.setAll(value);
        dcEnabled.setAll(value);
        rightsEnabled.setAll(value);
        viewerEnabled.setAll(value);
    }

    public void setEnabled(String id, boolean value) {
        _setObjectEnabled(id, value);
    }

    public boolean isEnabled(String id) {
        return _getObjectEnabled(id);
    }

    public <T> Map<String, T> asFlatMap( boolean onlyEnabled, Function<Object, T> convertor) {
        LinkedHashMap<String, T> map = new LinkedHashMap<String, T>();

        for (String fieldName : keys()) {
            Object o = get(fieldName);
            Object v = convertor.apply(o);
            if(onlyEnabled && !isEnabled(fieldName)) {
                continue;
            }
            map.put(fieldName, (T) v);

        }
        return map;
    }

    public Map<String, Object> asFlatMap(boolean onlyEnabled) {
        return asFlatMap(onlyEnabled, new Function<Object, Object>() {
            @Override
            public Object apply(Object t) {
                return t;
            }
        });
    }

    public Map<String, Object> asFlatMap() {
        return asFlatMap(false);
    }
    public Map<String, String> asFlatStringMap() {
        return asFlatStringMap(false);
    }

    public Map<String, String> asFlatStringMap(boolean onlyEnabled) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

        for (String fieldName : keys()) {
            if(onlyEnabled && !isEnabled(fieldName)) {
                continue;
            }
            map.put(fieldName, getString(fieldName));
        }
        return map;
    }

    public void fromFlatMap(Map<String, Object> map) {
        for (String fieldName : keys()) {
            if (map.containsKey(fieldName)) {
                FieldDescription fd = getFieldDescription(fieldName);
                set(fieldName, fd.postProcessDeserializedValue(map.get(fieldName)));
            }
        }
    }


    public MetadataInfo clone() {
        MetadataInfo md = new MetadataInfo();
        md.copyFrom(this);
        return md;
    }

    public void copyFrom(MetadataInfo other) {
        for (String fieldName : keys()) {
            set(fieldName, other.get(fieldName));
        }
    }

    public void copyEnabled(MetadataInfo other) {
        for (String fieldName : keys()) {
            setEnabled(fieldName, other.isEnabled(fieldName));
        }
    }

    public void copyUnset(MetadataInfo other) {
        for (String fieldName : keys()) {
            Object o = get(fieldName);
            if (o == null) {
                set(fieldName, other.get(fieldName));
            }
        }
    }

    public void copyUnsetOnly(MetadataInfo other) {
        for (String fieldName : keys()) {
            Object o = get(fieldName);
            Object otherVal = other.get(fieldName);
            if (o == null && otherVal != null) {
                set(fieldName, otherVal);
            }
        }
    }

    public void copyOnlyEnabled(MetadataInfo other) {
        for (String fieldName : keys()) {
            if(other.isEnabled(fieldName)) {
                set(fieldName, other.get(fieldName));
            }
        }
    }

    public MetadataInfo defaultsToApply(MetadataInfo defaults) {
        MetadataInfo diff = new MetadataInfo();
        for (String fieldName : keys()) {
            Object o = get(fieldName);
            Object otherVal = defaults.get(fieldName);
            if (o == null && otherVal != null) {
                diff.set(fieldName, otherVal);
            }
        }
        return diff;
    }

    public void copyFromWithExpand(MetadataInfo other,  MetadataInfo expandInfo) {
        for (String fieldName : keys()) {
            if (!other.isEnabled(fieldName)) {
                continue;
            }
            Object o = other.get(fieldName);
            if (o != null) {
                Object expandedVal = o;
                if (expandedVal instanceof String) {
                    TemplateString ts = new TemplateString((String) expandedVal);
                    expandedVal = ts.process(expandInfo);
                }
                set(fieldName, expandedVal);
            }
        }
    }

    public void expandVariables(){
        copyFromWithExpand(this, this);
    }

    public void enableOnlyNonNull() {
        Map<String, Object> values = asFlatMap();
        docEnabled.setAll(false);
        basicEnabled.setAll(false);
        pdfEnabled.setAll(false);
        dcEnabled.setAll(false);
        rightsEnabled.setAll(false);
        viewerEnabled.setAll(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                setEnabled(entry.getKey(), true);
            }
        }

    }

    public String toJson() {
        return toJson(false);
    }

    public String toJson(boolean pretty) {
        return  SerDeslUtils.toJSON(pretty, asFlatMap());
    }

    public void fromJson(String jsonString) {
        Map<String, Object> map = (Map<String, Object>) SerDeslUtils.fromJSON(jsonString);
        fromFlatMap(map);
    }

    public String toYAML() {
        return toYAML(false);
    }

    public String toYAML(boolean onlyEnabled) {
        return SerDeslUtils.toYAML(asFlatMap(onlyEnabled));
    }
    public void fromYAML(String yamlString) {
        Map<String, Object> map = (Map<String, Object>) SerDeslUtils.fromYAML(yamlString);
        fromFlatMap(map);
    }

    public boolean isEmpty(){
        for (String fieldName : keys()) {
            Object o = get(fieldName);
            if( o != null){
                return false;
            }
        }
        return true;
    }

    public boolean isEquivalent(MetadataInfo other) {
        return isEquivalent(other, false);
    }

    public boolean isEquivalent(MetadataInfo other, boolean onlyEnabled) {
        for (Entry<String, List<FieldDescription>> e : _mdFields.entrySet()) {
            if(onlyEnabled){
                if(!isEnabled(e.getKey())){
                     continue;
                }
            }
            // Skip file.* fields, as they are read only and come from file metadata
            if (e.getKey().startsWith("file.")) {
                continue;
            }
            // Skip "dc.dates" for now as loading them from PDF is broken in xmpbox <= 2.0.2
            //if("dc.dates".equals(e.getKey())){
            //	continue;
            //}
            Object t = get(e.getKey());
            Object o = other.get(e.getKey());
            FieldDescription fd = e.getValue().get(e.getValue().size() - 1);
            if (t == null) {
                if (o == null) {
                    continue;
                } else {
                    return false;
                }
            }
            if (fd.isList && (fd.type == FieldDataType.FieldType.DateField)) {
                List<Calendar> tl = (List<Calendar>) t;
                List<Calendar> ol = (List<Calendar>) o;
                if (tl.size() != ol.size()) {
                    return false;
                }
                for (int i = 0; i < tl.size(); ++i) {
                    Calendar tc = tl.get(i);
                    Calendar oc = ol.get(i);
                    if (tc == null) {
                        if (oc == null) {
                            continue;
                        } else {
                            return false;
                        }
                    }
                    if ((tc.getTimeInMillis() / 1000) != (oc.getTimeInMillis() / 1000)) {
                        return false;
                    }
                }

            } else if (t instanceof Calendar && o instanceof Calendar) {
                if ((((Calendar) t).getTimeInMillis() / 1000) != (((Calendar) o).getTimeInMillis() / 1000)) {
                    return false;
                }
            } else if (!t.equals(o)) {
                return false;
            }
        }
        return true;
    }

    public String asPersistenceString() {
        Map<String, Object> map = asFlatMap();
        // Don't store null values as they are the default
        for (String key : _mdFields.keySet()) {
            if (map.get(key) == null) {
                map.remove(key);
            }
        }
        Map<String, Boolean> enabledMap = new LinkedHashMap<String, Boolean>();
        // Don't store true values as they are the default
        for (String keyEnabled : _mdEnabledFields.keySet()) {
            if (!isEnabled(keyEnabled)) {
                enabledMap.put(keyEnabled, false);
            }
        }
        if (enabledMap.size() > 0) {
            map.put("_enabled", enabledMap);
        }

        return SerDeslUtils.toYAML( map);
    }

    protected Object _getStructObject(String id, Map<String, List<FieldDescription>> mdFields, boolean parent, FieldDataType.FieldType toType, boolean useDefault, Object defaultValue) {
        List<FieldDescription> fields = mdFields.get(id);
        if (fields == null || fields.size() == 0) {
            if (useDefault) {
                return defaultValue;
            }
            throw new RuntimeException("_getStructObject('" + id + "') No such field");
        }
        Object current = this;
        FieldDescription fieldD = null;
        for (int i = 0; i < fields.size() - (parent ? 1 : 0); ++i) {
            try {
                fieldD = fields.get(i);
                current = fieldD.field.get(current);
            } catch (IllegalArgumentException e) {
                if (useDefault) {
                    return defaultValue;
                }
                throw new RuntimeException("_getStructObject('" + id + "') IllegalArgumentException:" + e);
            } catch (IllegalAccessException e) {
                if (useDefault) {
                    return defaultValue;
                }
                throw new RuntimeException("_getStructObject('" + id + "') IllegalAccessException" + e);
            }
        }
        if(current == null && useDefault){
            return defaultValue;
        }
        if(toType != null) {
            switch (toType) {
                case EnumField:
                    return fieldD.enumValueFromString((String) current);
                case StringField:
                    return fieldD.makeStringFromValue(current);
                default:
                    return current;
            }
        }
        return current;
    }

    public Object get(String id) {
        return _getStructObject(id, _mdFields, false, null, false, null);
    }

    public String getString(String id) {
        return (String) _getStructObject(id, _mdFields, false, FieldDataType.FieldType.StringField, false, null);
    }

    public Object get(String id, Object defaultValue) {
        return _getStructObject(id, _mdFields, false, null, true, defaultValue);
    }

    public String getString(String id, String defaultValue) {
        return (String) _getStructObject(id, _mdFields, false, FieldDataType.FieldType.StringField, true, defaultValue);
    }


    public List<String> getManyStrings(List<String> keys){
        List<String> result = new ArrayList<>();
        for(String key: keys){
            result.add(getString(key));
        }
        return result;
    }

    protected boolean _getObjectEnabled(String id) {
        return (Boolean) _getStructObject(id, _mdEnabledFields, false, null, true, false);
    }

    protected void _setStructObject(String id, Object value, boolean append, boolean fromString, Map<String, List<FieldDescription>> mdFields) {
        List<FieldDescription> fields = mdFields.get(id);
        if (fields == null || fields.size() == 0) {
            throw new RuntimeException("_setStructObject('" + id + "') No such field");
        }
        Object current = _getStructObject(id, mdFields, true, null, false, null);
        if (current == null) {
            throw new RuntimeException("_setStructObject('" + id + "') No such field");
        }
        try {
            FieldDescription fieldD = fields.get(fields.size() - 1);
            if (fromString && (value != null)) {
                value = fieldD.makeValueFromString(value.toString());
            }
            if (fieldD.isList && append) {
                List<Object> l = (List<Object>) fieldD.field.get(current);
                if (l == null) {
                    l = new ArrayList<Object>();
                }
                if (List.class.isAssignableFrom(value.getClass())) {
                    l.addAll((List) value);
                } else {
                    l.add(value);
                }
                fieldD.field.set(current, l);
            }else if (fieldD.isNumeric && value != null){
                if(value instanceof Number num) {
                    switch (fieldD.type){
                        case IntField -> fieldD.field.set(current, num.intValue());
                        case LongField -> fieldD.field.set(current, num.longValue());
                        case FloatField -> fieldD.field.set(current, num.floatValue());
                        default -> throw new RuntimeException("_setStructObject('" + id + "') Trying to assign number to non numeric field!");
                    }
                }else{
                    throw new RuntimeException("_setStructObject('" + id + "') Trying to assign non number to numeric field!");
                }
            } else {
                fieldD.field.set(current, value);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("_setStructObject('" + id + "') IllegalArgumentException:" + e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("_setStructObject('" + id + "') IllegalAccessException" + e);
        }
    }

    public void set(String id, Object value) {
        _setStructObject(id, value, false, false, _mdFields);
    }

    public void setAppend(String id, Object value) {
        _setStructObject(id, value, true, false, _mdFields);
    }

    public void setFromString(String id, String value) {
        _setStructObject(id, value, false, true, _mdFields);
    }

    public void setAppendFromString(String id, String value) {
        _setStructObject(id, value, true, true, _mdFields);
    }

    protected void _setObjectEnabled(String id, boolean value) {
        _setStructObject(id, value, false, false, _mdEnabledFields);
    }

    public List<String> enabledKeys(){
        return keys().stream().filter(this::isEnabled).toList();
    }

    // Copy of java.util.functions.Function from Java8
    public interface Function<T, R> {
        R apply(T t);
    }

    public static class FileInfo {
        @FieldDataType(value = FieldDataType.FieldType.StringField, readOnly = true)
        public String fullPath;
        @FieldDataType(value = FieldDataType.FieldType.LongField, readOnly = true)
        public Long sizeBytes;
        @FieldDataType(value = FieldDataType.FieldType.StringField, readOnly = true)
        public String size;
        @FieldDataType(value = FieldDataType.FieldType.StringField, readOnly = true)
        public String nameWithExt;
        public String name;
        public Calendar createTime;
        public Calendar modifyTime;
        public Float pdfVersion;
        public Boolean pdfCompression;
    }

    public static class FileInfoEnabled {
        public boolean fullPath = true;
        public boolean name = true;
        public boolean nameWithExt = true;
        public boolean sizeBytes = true;
        public boolean size = true;
        public boolean createTime = true;
        public boolean modifyTime = true;
        public boolean pdfVersion = true;
        public boolean pdfCompression = true;


        public boolean atLeastOne() {
            return pdfVersion;
        }

        public void setAll(boolean value) {
            name = false;
            nameWithExt = false;
            sizeBytes = false;
            size = false;
            createTime = false;
            modifyTime = false;
            fullPath = false;
            pdfVersion = false;
            pdfCompression = false;
        }
    }

    public static class Basic {
        public String title;
        public String author;
        public String subject;
        public String keywords;
        public String creator;
        public String producer;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public Calendar creationDate;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public Calendar modificationDate;
        public String trapped;
    }

    public static class BasicEnabled {
        public boolean title = true;
        public boolean author = true;
        public boolean subject = true;
        public boolean keywords = true;
        public boolean creator = true;
        public boolean producer = true;
        public boolean creationDate = true;
        public boolean modificationDate = true;
        public boolean trapped = true;

        public boolean atLeastOne() {
            return title || author || subject || keywords || creator || producer || creationDate || modificationDate
                    || trapped ;
        }

        public void setAll(boolean value) {
            title = value;
            author = value;
            subject = value;
            keywords = value;
            creator = value;
            producer = value;
            creationDate = value;
            modificationDate = value;
            trapped = value;
        }
    }

    public static class XmpBasic {
        public String creatorTool;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public Calendar createDate;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public Calendar modifyDate;
        public String baseURL;
        public Integer rating;
        public String label;
        public String nickname;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> identifiers;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> advisories;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public Calendar metadataDate;
    }

    public static class XmpBasicEnabled {
        public boolean creatorTool = true;
        public boolean createDate = true;
        public boolean modifyDate = true;
        public boolean baseURL = true;
        public boolean rating = true;
        public boolean label = true;
        public boolean nickname = true;
        public boolean identifiers = true;
        public boolean advisories = true;
        public boolean metadataDate = true;

        public boolean atLeastOne() {
            return creatorTool || createDate || modifyDate || baseURL || rating || label || nickname
                    || identifiers || advisories || metadataDate;
        }

        public void setAll(boolean value) {
            creatorTool = value;
            createDate = value;
            modifyDate = value;
            baseURL = value;
            rating = value;
            label = value;
            nickname = value;
            identifiers = value;
            advisories = value;
            metadataDate = value;
        }
    }

    public static class XmpPdf {
        public String pdfVersion;
        public String keywords;
        public String producer;
    }

    public static class XmpPdfEnabled {
        public boolean pdfVersion = true;
        public boolean keywords = true;
        public boolean producer = true;

        public boolean atLeastOne() {
            return pdfVersion || keywords || producer;
        }

        public void setAll(boolean value) {
            pdfVersion = value;
            keywords = value;
            producer = value;
        }
    }

    public static class XmpDublinCore {
        public String title;
        public String description;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> creators;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> contributors;
        public String coverage;
        @FieldDataType(FieldDataType.FieldType.DateField)
        public List<Calendar> dates;
        public String format;
        public String identifier;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> languages;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> publishers;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> relationships;
        public String rights;
        public String source;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> subjects;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> types;
    }

    public static class XmpDublinCoreEnabled {
        public boolean title = true;
        public boolean description = true;
        public boolean creators = true;
        public boolean contributors = true;
        public boolean coverage = true;
        public boolean dates = true;
        public boolean format = true;
        public boolean identifier = true;
        public boolean languages = true;
        public boolean publishers = true;
        public boolean relationships = true;
        public boolean rights = true;
        public boolean source = true;
        public boolean subjects = true;
        public boolean types = true;

        public boolean atLeastOne() {
            return title || description || creators || contributors || coverage || dates || format || identifier
                    || languages || publishers || relationships || rights || source || subjects || types;
        }

        public void setAll(boolean value) {
            title = value;
            description = value;
            creators = value;
            contributors = value;
            coverage = value;
            dates = value;
            format = value;
            identifier = value;
            languages = value;
            publishers = value;
            relationships = value;
            rights = value;
            source = value;
            subjects = value;
            types = value;
        }
    }

    public static class XmpRights {
        public String certificate;
        public Boolean marked;
        @FieldDataType(FieldDataType.FieldType.TextField)
        public List<String> owner;
        public String usageTerms;
        public String webStatement;
    }

    public static class XmpRightsEnabled {
        public boolean certificate = true;
        public boolean marked = true;
        public boolean owner = true;
        public boolean usageTerms = true;
        public boolean webStatement = true;

        public boolean atLeastOne() {
            return certificate || marked || owner || usageTerms || webStatement;
        }

        public void setAll(boolean value) {
            certificate = value;
            marked = value;
            owner = value;
            usageTerms = value;
            webStatement = value;
        }
    }

    public static class ViewerOptions {
        public Boolean hideToolbar;
        public Boolean hideMenuBar;
        public Boolean hideWindowUI;
        public Boolean fitWindow;
        public Boolean centerWindow;
        public Boolean displayDocTitle;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.NON_FULL_SCREEN_PAGE_MODE.class)
        public String nonFullScreenPageMode;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.READING_DIRECTION.class)
        public String readingDirection;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.BOUNDARY.class)
        public String viewArea;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.BOUNDARY.class)
        public String viewClip;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.BOUNDARY.class)
        public String printArea;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.BOUNDARY.class)
        public String printClip;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.DUPLEX.class)
        public String duplex;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PDViewerPreferences.PRINT_SCALING.class)
        public String printScaling;
        //
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PageLayout.class)
        public String pageLayout;
        @FieldDataType(value = FieldDataType.FieldType.EnumField, enumClass = PageMode.class)
        public String pageMode;
    }

    public static class ViewerOptionsEnabled {
        public boolean hideToolbar = true;
        public boolean hideMenuBar = true;
        public boolean hideWindowUI = true;
        public boolean fitWindow = true;
        public boolean centerWindow = true;
        public boolean displayDocTitle = true;
        public boolean nonFullScreenPageMode = true;
        public boolean readingDirection = true;
        public boolean viewArea = true;
        public boolean viewClip = true;
        public boolean printArea = true;
        public boolean printClip = true;
        public boolean duplex = true;
        public boolean printScaling = true;
        //
        public boolean pageMode = true;
        public boolean pageLayout = true;

        //
        public boolean initialPage;
        public boolean pageFit;

        public boolean atLeastOne() {
            return
                    hideToolbar ||
                            hideMenuBar ||
                            hideWindowUI ||
                            fitWindow ||
                            centerWindow ||
                            displayDocTitle ||
                            nonFullScreenPageMode ||
                            readingDirection ||
                            viewArea ||
                            viewClip ||
                            printArea ||
                            printClip ||
                            duplex ||
                            printScaling ||
                            pageMode ||
                            pageLayout
                    ;
        }
        public boolean atLeastOnePreference() {
            return
                    hideToolbar ||
                            hideMenuBar ||
                            hideWindowUI ||
                            fitWindow ||
                            centerWindow ||
                            displayDocTitle ||
                            nonFullScreenPageMode ||
                            readingDirection ||
                            viewArea ||
                            viewClip ||
                            printArea ||
                            printClip ||
                            duplex ||
                            printScaling
                    ;
        }

        public void setAll(boolean value) {
            hideToolbar = value;
            hideMenuBar = value;
            hideWindowUI = value;
            fitWindow = value;
            centerWindow = value;
            displayDocTitle = value;
            nonFullScreenPageMode = value;
            readingDirection = value;
            viewArea = value;
            viewClip = value;
            printArea = value;
            printClip = value;
            duplex = value;
            printScaling = value;
            pageLayout = value;
            pageMode = value;
        }
    }

    //////////////////////////////
    public static class FieldDescription {
        public final String name;
        public final FieldDataType.FieldType type;
        public final String nullValueText;
        public final Class<? extends Enum> enumClass;
        public final boolean isList;
        public final boolean isWritable;
        public final boolean isNumeric;
        public final boolean isReadonly;
        final Field field;
        protected Method toStringMethod;

        public FieldDescription(String name, Field field, FieldDataType type, boolean isWritable) {
            this.name = name;
            this.field = field;
            this.type = type.value();
            this.nullValueText = type.nullValueText();
            this.enumClass = type.enumClass() != FieldDataType.NoEnumConfigured.class ? type.enumClass() : null;
            this.isWritable = isWritable;
            isList = List.class.isAssignableFrom(field.getType());
            isNumeric = this.type == FieldDataType.FieldType.LongField || this.type == FieldDataType.FieldType.IntField || this.type == FieldDataType.FieldType.FloatField;
            isReadonly =  type.readOnly();
        }

        public FieldDescription(String name, Field field, boolean isWritable) {
            Class<?> klass = field.getType();
            if (Boolean.class.isAssignableFrom(klass)) {
                this.type = FieldDataType.FieldType.BoolField;
            } else if (Calendar.class.isAssignableFrom(klass)) {
                this.type = FieldDataType.FieldType.DateField;
            } else if (Integer.class.isAssignableFrom(klass)) {
                this.type = FieldDataType.FieldType.IntField;
            } else if (Long.class.isAssignableFrom(klass)) {
                this.type = FieldDataType.FieldType.LongField;
            } else if (Float.class.isAssignableFrom(klass)) {
                this.type = FieldDataType.FieldType.FloatField;
            } else if (String.class.isAssignableFrom(klass)){
                this.type = FieldDataType.FieldType.StringField;
            } else {
                this.type = null;
            }
            this.name = name;
            this.field = field;
            this.nullValueText = "";
            this.enumClass = null;
            this.isWritable = isWritable;
            isList = List.class.isAssignableFrom(klass);
            isNumeric = this.type == FieldDataType.FieldType.LongField || this.type == FieldDataType.FieldType.IntField || this.type == FieldDataType.FieldType.FloatField;
            isReadonly = false;
        }

        String textForNull(){
            return  nullValueText != null  && !nullValueText.isEmpty() ? nullValueText : "Unset";
        }

        protected String makeStringFromValueSingle(Object value){
            if (value == null) {
                return "";
            }
            if(value instanceof String s){
                return s;
            }
            switch (type) {
                case DateField:
                    return DateFormat.formatDateTime((Calendar) value);
                case BoolField:
                    return ((Boolean) value) ? "true" : "false";
                case EnumField:
                    return enumValueToString((Enum<?>) value);
                default:
                    return value.toString();
            }
        }

        public String makeStringFromValue(Object value) {
            if (value == null) {
                return "";
            }
            if (isList) {
                List<String> l = ((List) value).stream().map(e -> makeStringFromValueSingle(e)).toList();
                return String.join("\n", l);
            }
            return makeStringFromValueSingle(value);
        }

        public Object postProcessDeserializedValue(Object value) {
            if (value == null) {
                return null;
            }
            if (isList) {
                if (type == FieldDataType.FieldType.DateField  ) {
                    List values = value instanceof List<?> ? (List)value : List.of(value);
                    List<Calendar> rval = new ArrayList<Calendar>();
                    for(Object singleValue: values) {
                        if( singleValue instanceof Calendar cal ){
                            rval.add(cal);
                        } else if (singleValue instanceof String stringValue) {
                            for (String line : stringValue.split("\n")) {
                                try {
                                    rval.add(DateFormat.parseDate(line.trim()));
                                } catch (ParseError e) {
                                    throw new RuntimeException("postProcessDeserializedValue() Invalid date format:" + line);
                                }
                            }
                        } else if (value instanceof Date d) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(d);
                            rval.add(cal);
                        } else {
                            throw new RuntimeException("postProcessDeserializedValue() Invalid date type:" + singleValue.getClass());
                        }
                    }
                    return rval;
                }
            } else {
               if (type == FieldDataType.FieldType.DateField) {
                   if(value instanceof String stringValue) {

                       try {
                           return DateFormat.parseDate(stringValue);
                       } catch (ParseError e) {
                           throw new RuntimeException("postProcessDeserializedValue() Invalid date format:" + stringValue);
                       }
                   }
                   if (value instanceof Date d) {
                       Calendar cal = Calendar.getInstance();
                       cal.setTime(d);
                       return cal;
                   }
               }
               if(type == FieldDataType.FieldType.FloatField){
                   if(value instanceof Number n){
                       return n.floatValue();
                   }
               }
            }
            return value;
        }


        public Object makeValueFromString(String value) {
            if (value == null) {
                return null;
            }
            if(isNumeric && value.isEmpty()){
                return null;
            }
            if (isList) {
                if (type == FieldDataType.FieldType.StringField) {
                    return List.of(value);
                } else if (type == FieldDataType.FieldType.TextField) {
                    return Arrays.asList(value.split("\n"));
                } else if (type == FieldDataType.FieldType.IntField) {
                    // TODO: possible allow comma separated interger list
                    return List.of(Integer.parseInt(value));
                } else if (type == FieldDataType.FieldType.LongField) {
                    // TODO: possible allow comma separated interger list
                    return List.of(Long.parseLong(value));
                } else if (type == FieldDataType.FieldType.FloatField) {
                    // TODO: possible allow comma separated interger list
                    return List.of(Float.parseFloat(value));
                } else if (type == FieldDataType.FieldType.BoolField) {
                    // TODO: possible allow comma separated boolean list
                    String v = value.toLowerCase().trim();
                    Boolean b = null;
                    if (v.equals("true") || v.equals("yes")) b = true;
                    if (v.equals("false") || v.equals("no")) b = false;
                    return Collections.singletonList(b);
                } else if (type == FieldDataType.FieldType.DateField) {
                    List<Calendar> rval = new ArrayList<Calendar>();
                    for (String line : value.split("\n")) {
                        try {
                            rval.add(DateFormat.parseDate(line.trim()));
                        } catch (ParseError e) {
                            throw new RuntimeException("makeValueFromString() Invalid date format:" + line);
                        }
                    }
                    return rval;
                }
            } else {
                if (type == FieldDataType.FieldType.StringField) {
                    return value;
                } else if (type == FieldDataType.FieldType.TextField) {
                    return value;
                } else if (type == FieldDataType.FieldType.IntField) {
                    return Integer.parseInt(value);
                } else if (type == FieldDataType.FieldType.LongField) {
                    return Long.parseLong(value);
                } else if (type == FieldDataType.FieldType.FloatField) {
                    return Float.parseFloat(value);
                } else if (type == FieldDataType.FieldType.BoolField) {
                    String v = value.toLowerCase().trim();
                    if (v.equals("true") || v.equals("yes")) return true;
                    if (v.equals("false") || v.equals("no")) return false;
                    return null;
                }else if(type == FieldDataType.FieldType.EnumField) {
                    return value.isEmpty() ? null : value;
                } else if (type == FieldDataType.FieldType.DateField) {
                    try {
                        return DateFormat.parseDate(value);
                    } catch (ParseError e) {
                        throw new RuntimeException("makeValueFromString() Invalid date format:" + value);
                    }
                }
            }
            throw new RuntimeException("makeValueFromString() :Don't know how to convert to type:" + type);
        }

        public Enum<?> enumValueFromString(String s){
            if(s == null || s.isEmpty()){
                return  null;
            }
            if(textForNull().equals(s)){
                return null;
            }
            return Enum.valueOf(enumClass, s);
        }

        public String enumValueToString(Enum<?> v) {
            if(toStringMethod == null){
                try {
                    toStringMethod  = enumClass.getMethod("stringValue");
                } catch (NoSuchMethodException e) {
                }
                if( toStringMethod == null ){
                    try {
                        toStringMethod  = enumClass.getMethod("toString");
                    } catch (NoSuchMethodException e) {
                    }
                }
            }
            try {
                return (String) toStringMethod.invoke(v);
            } catch (IllegalAccessException ex) {
            } catch (InvocationTargetException ex) {
            }

            return "[! Don't know how to convert" + v.getClass().getName() + " to string ]";
        }

        public String[] getEnumValuesAsStrings(){
            if(type != FieldDataType.FieldType.EnumField || enumClass == null) {
                return null;
            }

            var elements = enumClass.getEnumConstants();
            String[] model = new String[elements.length + 1];
            int i = 0;
            model[i++] = textForNull();
            for(var e : elements){
                model[i++] = makeStringFromValue(e);
            }
            return model;
        }
    }
    //////////////////////////////

    protected class XmpSchemaOnDemand {
        protected XMPMetadata xmpNew;
        protected XMPBasicSchema _basic;
        protected AdobePDFSchema _pdf;
        protected DublinCoreSchema _dc;
        protected XMPRightsManagementSchema _rights;

        public XmpSchemaOnDemand(XMPMetadata xmp) {
            this.xmpNew = xmp;
        }

        public XMPBasicSchema basic() {
            if (this._basic == null) {
                this._basic = this.xmpNew.createAndAddXMPBasicSchema();
            }
            return this._basic;
        }

        public AdobePDFSchema pdf() {
            if (this._pdf == null) {
                this._pdf = this.xmpNew.createAndAddAdobePDFSchema();
            }
            return this._pdf;
        }

        public DublinCoreSchema dc() {
            if (this._dc == null) {
                this._dc = this.xmpNew.createAndAddDublinCoreSchema();
            }
            return this._dc;
        }

        public XMPRightsManagementSchema rights() {
            if (this._rights == null) {
                this._rights = this.xmpNew.createAndAddXMPRightsManagementSchema();
            }
            return this._rights;
        }


    }


}
