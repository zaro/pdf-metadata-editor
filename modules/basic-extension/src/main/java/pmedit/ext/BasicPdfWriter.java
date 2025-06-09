package pmedit.ext;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.compress.COSWriterCompressionPool;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPRightsManagementSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.FileOptimizer;
import pmedit.MetadataCollection;
import pmedit.MetadataInfo;
import pmedit.MetadataInfoUtils;
import pmedit.util.CrossPlatformFileTimeModifier;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.List;

public class BasicPdfWriter implements PdfWriter {
    static Logger LOG = LoggerFactory.getLogger(BasicPdfWriter.class);

    public float getCompressionMinimumSupportedVersion() {
        return COSWriterCompressionPool.MINIMUM_SUPPORTED_VERSION;
    }


    public BasicPdfWriter(){

    }

    @Override
    public boolean saveToPDF(MetadataCollection mc, PDDocument document,  File pdfFile) throws Exception {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("saveToPDF:",e);
            throw e;
        }
        boolean atLeastOneChange =
                md.docEnabled.atLeastOne() || md.basicEnabled.atLeastOne() || md.pdfEnabled.atLeastOne() || md.dcEnabled.atLeastOne() || md.rightsEnabled.atLeastOne()
                        || FileOptimizer.isOptimiserEnabled(FileOptimizer.Enum.PDFBOX)
                        || md.removeXmp || md.removeDocumentInfo;

        if (!atLeastOneChange) {
            return false;
        }
        //System.err.println("Saving:");
        //System.err.println(toYAML());
        // Basic info
        if (md.docEnabled.atLeastOne()) {
            PDDocumentInformation info = document.getDocumentInformation();
            if (md.docEnabled.title) {
                info.setTitle(md.doc.title);
            }
            if (md.docEnabled.author) {
                info.setAuthor(md.doc.author);
            }
            if (md.docEnabled.subject) {
                info.setSubject(md.doc.subject);
            }
            if (md.docEnabled.keywords) {
                info.setKeywords(md.doc.keywords);
            }
            if (md.docEnabled.creator) {
                info.setCreator(md.doc.creator);
            }
            if (md.docEnabled.producer) {
                info.setProducer(md.doc.producer);
            }
            if (md.docEnabled.creationDate) {
                info.setCreationDate(md.doc.creationDate);
            }
            if (md.docEnabled.modificationDate) {
                info.setModificationDate(md.doc.modificationDate);
            }
            if (md.docEnabled.trapped) {
                info.setTrapped(md.doc.trapped);
            }
            document.setDocumentInformation(info);
        }

        // XMP
        PDDocumentCatalog catalog = document.getDocumentCatalog();

        if(md.viewerEnabled.atLeastOne()){
            COSDictionary cd = catalog.getCOSObject();
            if(md.viewerEnabled.pageMode){
                cd.setName(COSName.PAGE_MODE, md.viewer.pageMode);
            }
            if(md.viewerEnabled.pageLayout){
                cd.setName(COSName.PAGE_LAYOUT, md.viewer.pageLayout);
            }
            if(md.viewerEnabled.atLeastOnePreference()){
                PDViewerPreferences preferences = catalog.getViewerPreferences();
                if(preferences == null ) {
                    preferences = new PDViewerPreferences(cd);
                }
                COSDictionary pd = preferences.getCOSObject();
                if (md.viewerEnabled.hideToolbar ) {
                    if(md.viewer.hideToolbar != null) {
                        preferences.setHideToolbar(md.viewer.hideToolbar);
                    } else {
                        pd.removeItem(COSName.HIDE_TOOLBAR);
                    }
                }
                if (md.viewerEnabled.hideMenuBar ) {
                    if(md.viewer.hideMenuBar != null) {
                        preferences.setHideMenubar(md.viewer.hideMenuBar);
                    } else {
                        pd.removeItem(COSName.HIDE_MENUBAR);
                    }
                }
                if (md.viewerEnabled.hideWindowUI ) {
                    if(md.viewer.hideWindowUI != null) {
                        preferences.setHideWindowUI(md.viewer.hideWindowUI);
                    } else {
                        pd.removeItem(COSName.HIDE_WINDOWUI);
                    }
                }
                if (md.viewerEnabled.fitWindow ) {
                    if(md.viewer.fitWindow != null) {
                        preferences.setFitWindow(md.viewer.fitWindow);
                    } else {
                        pd.removeItem(COSName.FIT_WINDOW);
                    }
                }
                if (md.viewerEnabled.centerWindow ) {
                    if(md.viewer.centerWindow != null) {
                        preferences.setCenterWindow(md.viewer.centerWindow);
                    } else {
                        pd.removeItem(COSName.CENTER_WINDOW);
                    }
                }
                if (md.viewerEnabled.displayDocTitle ) {
                    if(md.viewer.displayDocTitle != null) {
                        preferences.setDisplayDocTitle(md.viewer.displayDocTitle);
                    } else {
                        pd.removeItem(COSName.DISPLAY_DOC_TITLE);
                    }
                }
                if (md.viewerEnabled.nonFullScreenPageMode ) {
                    pd.setName(COSName.NON_FULL_SCREEN_PAGE_MODE, md.viewer.nonFullScreenPageMode);
                }
                if (md.viewerEnabled.readingDirection ) {
                    pd.setName(COSName.DIRECTION, md.viewer.readingDirection);
                }
                if (md.viewerEnabled.viewArea ) {
                    pd.setName(COSName.VIEW_AREA, md.viewer.viewArea);
                }
                if (md.viewerEnabled.viewClip ) {
                    pd.setName(COSName.VIEW_CLIP, md.viewer.viewClip);
                }
                if (md.viewerEnabled.printArea ) {
                    pd.setName(COSName.PRINT_AREA, md.viewer.printArea);
                }
                if (md.viewerEnabled.printClip ) {
                    pd.setName(COSName.PRINT_CLIP,md.viewer.printClip );
                }
                if (md.viewerEnabled.duplex ) {
                    pd.setName(COSName.DUPLEX, md.viewer.duplex);
                }
                if (md.viewerEnabled.printScaling ) {
                    pd.setName(COSName.PRINT_SCALING, md.viewer.printScaling);
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
        MetadataInfo.XmpSchemaOnDemand newXmp = new MetadataInfo.XmpSchemaOnDemand(xmpNew);
        // XMP Basic
        XMPBasicSchema biOld = xmpOld != null ? xmpOld.getXMPBasicSchema() : null;
        boolean atLeastOneXmpBasicSet = false;
        if (md.basicEnabled.atLeastOne() || (biOld != null)) {

            if (md.basicEnabled.advisories) {
                if (md.basic.advisories != null) {
                    for (String a : md.basic.advisories) {
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

            if (md.basicEnabled.baseURL) {
                if (md.basic.baseURL != null) {
                    newXmp.basic().setBaseURL(md.basic.baseURL);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String baseUrl = biOld.getBaseURL();
                if (baseUrl != null) {
                    newXmp.basic().setBaseURL(baseUrl);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.createDate) {
                if (md.basic.createDate != null) {
                    newXmp.basic().setCreateDate(md.basic.createDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getCreateDate();
                if (old != null) {
                    newXmp.basic().setCreateDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.modifyDate) {
                if (md.basic.modifyDate != null) {
                    newXmp.basic().setModifyDate(md.basic.modifyDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getModifyDate();
                if (old != null) {
                    newXmp.basic().setModifyDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.creatorTool) {
                if (md.basic.creatorTool != null) {
                    newXmp.basic().setCreatorTool(md.basic.creatorTool);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getCreatorTool();
                if (old != null) {
                    newXmp.basic().setCreatorTool(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.identifiers) {
                if (md.basic.identifiers != null) {
                    for (String i : md.basic.identifiers) {
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

            if (md.basicEnabled.label) {
                if (md.basic.label != null) {
                    newXmp.basic().setLabel(md.basic.label);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getLabel();
                if (old != null) {
                    newXmp.basic().setLabel(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.metadataDate) {
                if (md.basic.metadataDate != null) {
                    newXmp.basic().setMetadataDate(md.basic.metadataDate);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                Calendar old = biOld.getMetadataDate();
                if (old != null) {
                    newXmp.basic().setMetadataDate(old);
                    atLeastOneXmpBasicSet = true;
                }
            }

            if (md.basicEnabled.nickname) {
                if (md.basic.nickname != null) {
                    newXmp.basic().setNickname(md.basic.nickname);
                    atLeastOneXmpBasicSet = true;
                }
            } else if (biOld != null) {
                String old = biOld.getNickname();
                if (old != null) {
                    newXmp.basic().setNickname(old);
                    atLeastOneXmpBasicSet = true;
                }
            }
            if (md.basicEnabled.rating) {
                if (md.basic.rating != null) {
                    newXmp.basic().setRating(md.basic.rating);
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
        if (md.pdfEnabled.atLeastOne() || (piOld != null)) {

            if (md.pdfEnabled.keywords) {
                if (md.pdf.keywords != null) {
                    newXmp.pdf().setKeywords(md.pdf.keywords);
                    atLeastOneXmpPdfSet = true;
                }
            } else if (piOld != null) {
                String old = piOld.getKeywords();
                if (old != null) {
                    newXmp.pdf().setKeywords(old);
                    atLeastOneXmpPdfSet = true;
                }
            }

            if (md.pdfEnabled.producer) {
                if (md.pdf.producer != null) {
                    newXmp.pdf().setProducer(md.pdf.producer);
                    atLeastOneXmpPdfSet = true;
                }
            } else if (piOld != null) {
                String old = piOld.getProducer();
                if (old != null) {
                    newXmp.pdf().setProducer(old);
                    atLeastOneXmpPdfSet = true;
                }
            }

            if (md.pdfEnabled.pdfVersion) {
                if (md.pdf.pdfVersion != null) {
                    newXmp.pdf().setPDFVersion(md.pdf.pdfVersion);
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
        if (md.dcEnabled.atLeastOne() || (dcOld != null)) {

            if (md.dcEnabled.title) {
                if (md.dc.title != null) {
                    newXmp.dc().setTitle(md.dc.title);
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
            if (md.dcEnabled.contributors) {
                if (md.dc.contributors != null) {
                    for (String i : md.dc.contributors) {
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
            if (md.dcEnabled.publishers) {
                if (md.dc.publishers != null) {
                    for (String i : md.dc.publishers) {
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
            if (md.dcEnabled.relationships) {
                if (md.dc.relationships != null) {
                    for (String i : md.dc.relationships) {
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
            if (md.dcEnabled.subjects) {
                if (md.dc.subjects != null) {
                    for (String i : md.dc.subjects) {
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
            if (md.dcEnabled.types) {
                if (md.dc.types != null) {
                    for (String i : md.dc.types) {
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
            if (md.dcEnabled.languages) {
                if (md.dc.languages != null) {
                    for (String i : md.dc.languages) {
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
            if (md.dcEnabled.creators) {
                if (md.dc.creators != null) {
                    for (String i : md.dc.creators) {
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
            if (md.dcEnabled.coverage) {
                if (md.dc.coverage != null) {
                    newXmp.dc().setCoverage(md.dc.coverage);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getCoverage();
                if (old != null) {
                    newXmp.dc().setCoverage(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (md.dcEnabled.format) {
                if (md.dc.format != null) {
                    newXmp.dc().setFormat(md.dc.format);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getFormat();
                if (old != null) {
                    newXmp.dc().setFormat(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (md.dcEnabled.identifier) {
                if (md.dc.identifier != null) {
                    newXmp.dc().setIdentifier(md.dc.identifier);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getIdentifier();
                if (old != null) {
                    newXmp.dc().setIdentifier(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (md.dcEnabled.rights) {
                if (md.dc.rights != null) {
                    newXmp.dc().addRights(null, md.dc.rights);
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

            if (md.dcEnabled.source) {
                if (md.dc.source != null) {
                    newXmp.dc().setSource(md.dc.source);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getSource();
                if (old != null) {
                    newXmp.dc().setSource(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (md.dcEnabled.description) {
                if (md.dc.description != null) {
                    newXmp.dc().setDescription(md.dc.description);
                    atLeastOneXmpDcSet = true;
                }
            } else if (dcOld != null) {
                String old = dcOld.getDescription();
                if (old != null) {
                    newXmp.dc().setDescription(old);
                    atLeastOneXmpDcSet = true;
                }
            }

            if (md.dcEnabled.dates) {
                if (md.dc.dates != null) {
                    for (Calendar date : md.dc.dates) {
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
        if (md.rightsEnabled.atLeastOne() || (riOld != null)) {

            if (md.rightsEnabled.certificate) {
                if (md.rights.certificate != null) {
                    newXmp.rights().setCertificate(md.rights.certificate);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                String old = riOld.getCertificate();
                if (old != null) {
                    newXmp.rights().setCertificate(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (md.rightsEnabled.marked) {
                if (md.rights.marked != null) {
                    newXmp.rights().setMarked(md.rights.marked);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                Boolean old = riOld.getMarked();
                if (old != null) {
                    newXmp.rights().setMarked(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (md.rightsEnabled.owner) {
                if (md.rights.owner != null) {
                    for (String i : md.rights.owner) {
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

            if (md.rightsEnabled.usageTerms) {
                if (md.rights.usageTerms != null) {
                    newXmp.rights().setUsageTerms(md.rights.usageTerms);
                    atLeastOneXmpRightsSet = true;
                }
            } else if (riOld != null) {
                String old = riOld.getUsageTerms();
                if (old != null) {
                    newXmp.rights().setUsageTerms(old);
                    atLeastOneXmpRightsSet = true;
                }
            }

            if (md.rightsEnabled.webStatement) {
                if (md.rights.webStatement != null) {
                    newXmp.rights().setWebStatement(md.rights.webStatement);
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
        if (md.basicEnabled.atLeastOne() || md.pdfEnabled.atLeastOne() || md.dcEnabled.atLeastOne() || md.rightsEnabled.atLeastOne()) {
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

        PmeExtension.get().onDocumentSave(document, pdfFile, md);


        if(md.removeDocumentInfo) {
            document.getDocument().getTrailer().removeItem(COSName.INFO);
        }

        if(md.removeXmp) {
            document.getDocumentCatalog().setMetadata(null);
        }

        float saveAsVersion = md.prop.version != null ? md.prop.version : 0;
        if(saveAsVersion>0){
            if (saveAsVersion >= 1.4F) {
                document.getDocumentCatalog().setVersion(Float.toString(saveAsVersion));
                document.getDocument().setVersion(saveAsVersion);
            } else {
                document.getDocument().setVersion(saveAsVersion);
                document.getDocumentCatalog().setVersion(null);
            }
        }

        if(md.prop.encryption != null && md.prop.encryption) {

            AccessPermission permission = md.getAccessPermissions();

            StandardProtectionPolicy spp = new StandardProtectionPolicy(md.prop.ownerPassword, md.prop.userPassword, permission);
            spp.setEncryptionKeyLength(md.prop.keyLength);

            document.protect(spp);
        } else {
            document.setAllSecurityToBeRemoved(true);
        }


        int pdfCompression = 0 ;
        if(saveAsVersion >= this.getCompressionMinimumSupportedVersion()) {
            if (md.propEnabled.compression ) {
                pdfCompression = (md.prop.compression != null && md.prop.compression) ? FileOptimizer.getPdfBoxCompression() : 0;
            } else {
                pdfCompression = document.getDocument().isXRefStream() ? FileOptimizer.getPdfBoxCompression() : 0;
            }
        }
        document.save(pdfFile, new CompressParameters(pdfCompression));
        return true;
    }

    @Override
    public File saveAsPDF(MetadataCollection mc, File pdfFile, File newFile) throws Exception {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("saveToPDF:",e);
            throw e;
        }

        PDDocument document = null;
        String password = md.prop.ownerPassword;
        document = Loader.loadPDF(pdfFile, password);
        File writeFile = File.createTempFile(pdfFile.getName() + "-", null, pdfFile.getParentFile());

        boolean fileSaved = saveToPDF(md, document, writeFile);
        if(!fileSaved){
            Files.copy(pdfFile.toPath(), writeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        document.close();

        File target;
        if(newFile != null){
            target = newFile;
        } else {
            target = pdfFile;
            if (md.fileEnabled.name && md.file.name != null) {
                target = new File(target.getParentFile(), md.file.name + ".pdf");
            }
        }
        Files.move(writeFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if((md.fileEnabled.createTime && md.file.createTime != null) || (md.fileEnabled.modifyTime && md.file.modifyTime != null)) {
            CrossPlatformFileTimeModifier.setFileTimes(target, md.file.createTime, md.file.modifyTime);
        }
        return target;
    }

    @Override
    public File saveAsPDF(MetadataCollection mc, File pdfFile) throws Exception {
        return saveAsPDF(mc, pdfFile, null);
    }

}
