package pmedit.ext;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.List;

public class ProPdfWriter extends BasicPdfWriter {
    Logger LOG = LoggerFactory.getLogger(ProPdfWriter.class);

    public ProPdfWriter(){

    }

    @Override
    public boolean allFieldsSupported(MetadataCollection mc) {
        return  true;
    }

    protected static class AppliedChangesFull extends AppliedChanges {
        boolean xmpRightsSet;

        AppliedChangesFull(boolean docSet, XMPMetadata xmpOld, MetadataInfo.XmpSchemaOnDemand newXmp, boolean xmpBasicSet, boolean xmpPdfSet, boolean xmpDcSet, boolean xmpRightsSet) {
            super(docSet, xmpOld, newXmp, xmpBasicSet, xmpPdfSet, xmpDcSet);
            this.xmpRightsSet = xmpRightsSet;
        }

        AppliedChangesFull(AppliedChanges o){
            super(o.docSet, o.xmpOld, o.newXmp, o.xmpBasicSet, o.xmpPdfSet, o.xmpDcSet);
            xmpRightsSet = false;
        }

        @Override
        public boolean anyXmp(){
            return super.anyXmp() || xmpRightsSet;
        }
    }

    protected AppliedChangesFull applyToDocument(MetadataInfo md, PDDocument document) throws Exception {
        AppliedChangesFull changes = new AppliedChangesFull(super.applyToDocument(md, document));
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


        XMPMetadata xmpOld = changes.xmpOld;
        MetadataInfo.XmpSchemaOnDemand newXmp = changes.newXmp;
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

        changes.xmpRightsSet = atLeastOneXmpRightsSet;
        return changes;
    }

    @Override
    public boolean saveToPDF(MetadataCollection mc, PDDocument document,  File pdfFile) throws Exception {
        if(!(mc instanceof MetadataInfo md)){
            IllegalArgumentException e = new IllegalArgumentException("Unsupported Metadata collection type: " +  mc.getClass());
            LOG.error("saveToPDF:",e);
            throw e;
        }
        boolean atLeastOneChange =
                md.docEnabled.atLeastOne()
                        || md.basicEnabled.atLeastOne() || md.pdfEnabled.atLeastOne()
                        || md.dcEnabled.atLeastOne() || md.rightsEnabled.atLeastOne()
                        || md.viewerEnabled.atLeastOne()
                        || md.propEnabled.atLeastOne()
                        || FileOptimizer.isOptimiserEnabled(FileOptimizer.Enum.PDFBOX)
                        || md.removeXmp || md.removeDocumentInfo;

        if (!atLeastOneChange) {
            return false;
        }
        AppliedChangesFull changes = applyToDocument(md, document);

        // Do the save
        if (changes.anyXmp()) {
            PDMetadata metadataStream = new PDMetadata(document);
            try {
                metadataStream.importXMPMetadata(MetadataInfoUtils.serializeXMPMetadata(changes.newXmp.getXmpMetadata()));
            } catch (TransformerException e) {
                throw new Exception("Failed to save document:" + e.getMessage());
            }
            document.getDocumentCatalog().setMetadata(metadataStream);
        } else {
            document.getDocumentCatalog().setMetadata(null);
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
            LOG.error("saveAsPDF:",e);
            throw e;
        }

        File writeFile = saveAsTempPDF(md, pdfFile);

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
