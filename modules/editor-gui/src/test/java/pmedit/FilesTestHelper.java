package pmedit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;
import pmedit.ext.BasicPdfWriter;
import pmedit.ext.PdfReader;
import pmedit.ext.PdfWriter;
import pmedit.ext.PmeExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class FilesTestHelper {
//    protected static  File tempDir;
    protected static float DEFAULT_PDF_VERSION = 1.6f;
    protected static List<Integer> ALLOWED_KEY_LENGTHS = Arrays.asList(FileOptimizer.ALLOWED_KEY_LENGTHS);



    public static File emptyPdf(File dir) throws Exception {
        File temp = File.createTempFile("test-file-", ".pdf", dir);
        PDDocument doc = new PDDocument();
        doc.setVersion(DEFAULT_PDF_VERSION);
        try {
            // a valid PDF document requires at least one page
            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
            doc.save(temp);
        } finally {
            doc.close();
        }
//        temp.deleteOnExit();
        return temp;
    }
    public static List<Path> listFiles(Path dir) throws IOException {
        return listFiles(dir, ".pdf");
    }
    public static List<Path> listFiles(Path dir, String... extensions) throws IOException {
        List<Path> out = new ArrayList<>();
        for(Path p: Files.list(dir).toList()){
            for(String ext: extensions){
                if(p.getFileName().toString().endsWith(ext)){
                    out.add(p);
                    break;
                }
            }
        }
        return out;
    }

    public static File csvFile(List<String> lines) throws Exception {
        File temp = File.createTempFile("test-csv", ".csv");
        Files.write(temp.toPath(), lines, Charset.forName("UTF-8"));
        temp.deleteOnExit();
        return temp;
    }

    public static List<PMTuple> randomFiles(int numFiles, File dir) throws Exception {
        return randomFiles(numFiles, true, null, dir);
    }
    public static List<PMTuple> randomFiles(int numFiles, Consumer<MetadataInfo> beforeSave, File dir) throws Exception {
        return randomFiles(numFiles,  true, beforeSave, dir);
    }

    public static List<PMTuple> randomFiles(int numFiles, boolean allFields, File dir) throws Exception {
        return randomFiles(numFiles, allFields, null, dir);
    }

    public static List<PMTuple> randomFiles(int numFiles,  boolean allFields, Consumer<MetadataInfo> beforeSave, File dir) throws Exception {
        List<String> fields = MetadataInfo.keys();
        List<PMTuple> rval = new ArrayList<FilesTestHelper.PMTuple>();

        PmeExtension extension = PmeExtension.get();
        PdfWriter writer = extension.newPdfWriter();
        PdfReader reader = extension.newPdfReader();


        // Filter out fields, that should not be random
        fields = fields.stream().filter(f -> {
            return !(
                    f.startsWith("file.") ||
                            ( !extension.hasBatch() && (
                                    f.startsWith("rights.") || f.startsWith("prop.") || f.startsWith("viewer.")
                            ))

            );
        }).toList();


        Random rand = new Random();
        for (int i = 0; i < numFiles; ++i) {
            MetadataInfo md = new MetadataInfo();
            //int genFields = rand.nextInt(numFields);
           List<String> genFields;
           if(allFields){
               genFields = fields;
           } else {
               int numFields = rand.nextInt(5, fields.size());
               genFields = new ArrayList<>(numFields);
               while(numFields>0){
                   String s = fields.get(rand.nextInt(fields.size()));
                   if(!genFields.contains(s)){
                       genFields.add(s);
                       numFields --;
                   }
               }
           }
            for (int j = 0; j < genFields.size(); ++j) {
                String field = fields.get(j);

                if (field.equals("doc.trapped")) {
                    md.setAppend(field, Arrays.asList("False", "True", "Unknown").get(rand.nextInt(3)));
                    continue;
                }

                if (field.equals("prop.version")) {
                    md.setAppend(field, Arrays.asList(1.3f, 1.4f, 1.5f, 1.6f, 1.7f).get(rand.nextInt(5)));
                    continue;
                }

                // Encryption can be only explicitly enabled
                if (field.equals("prop.encryption")) {
                    continue;
                }


                MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(field);
                switch (fd.type) {
                    case LongField:
                        md.setAppend(field, (long) rand.nextInt(1000));
                        break;
                    case IntField:
                        md.setAppend(field, rand.nextInt(5));
                        break;
                    case BoolField:
                        md.setAppend(field, (rand.nextInt(1000) & 1) == 1);
                        break;
                    case DateField:
                        Calendar cal = Calendar.getInstance();
                        cal.setLenient(false);
                        cal.set(Calendar.MILLISECOND, 0);
                        md.setAppend(field, cal);
                        break;
                    case EnumField:
                        String[] choices = fd.getEnumValuesAsStrings();
                        if("Unset".equals(choices[0])){
                            choices[0] = null;
                        }
                        String choice = choices[rand.nextInt(choices.length)];
                        md.set(field, choice != null ? fd.makeStringFromValue(choice): null);
                        break;
                    default:
                        if(fd.isList){
                            for(int c  =rand.nextInt(5); c>=0; c--) {
                                md.setAppendFromString(field, new BigInteger(130, rand).toString(32));
                            }
                        } else {
                            md.setAppend(field, new BigInteger(130, rand).toString(32));
                        }
                        break;
                }
            }

            File pdf = emptyPdf(dir);

            // Ensure version & compression are always set for tests, as they always have value when read
            if(md.prop.version == null){
                md.prop.version = DEFAULT_PDF_VERSION;
            }
            if(md.prop.compression == null){
                if(extension.hasBatch()) {
                    md.prop.compression = (rand.nextInt(1000) & 1) == 1;
                } else {
                    md.prop.compression = FileOptimizer.getPdfBoxCompression() != 0;
                }
            }



            if(beforeSave != null ){
                beforeSave.accept(md);
            }

            // Ensure there is keyLength
            if(md.prop.encryption != null && md.prop.encryption && !ALLOWED_KEY_LENGTHS.contains(md.prop.keyLength)) {
                md.prop.keyLength  = ALLOWED_KEY_LENGTHS.get(rand.nextInt(3));
                assertTrue(extension.hasBatch(), "Trying to create encrypted PDF, but the current PdfWriter doesn't support it");
            }
            if(md.prop.encryption == null || !md.prop.encryption){
                for(String f: MetadataInfo.keys()){
                    if(f.startsWith("prop.can")){
                        md.set(f, null);
                    }
                }
                md.prop.keyLength = null;
                md.prop.ownerPassword = null;
                md.prop.userPassword = null;
            }

            //Ensure we are not using compression if not supported by PDF version
            if(md.prop.version != null && md.prop.version < writer.getCompressionMinimumSupportedVersion()) {
                md.prop.compression = false;
            }

            md.setEnabledForPrefix("file.", false);
            writer.saveAsPDF(md, pdf);

            md.setEnabledForPrefix("file.", true);
            reader.loadPDFFileInfo(pdf, md);
            rval.add(new PMTuple(pdf, md));
        }
        return rval;
    }

    public static void removeDirectory(File dir){
        Path pathToBeDeleted = dir.toPath();
        try (Stream<Path> paths = Files.walk(pathToBeDeleted)) {
            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
        }
    }

    public static void assertEqualsOnlyEnabledExceptFile(MetadataInfo expected, MetadataInfo actual, String message) {
        if(!actual.isEquivalent(expected, EnumSet.of(MetadataInfo.EqualityOptions.ONLY_ENABLED, MetadataInfo.EqualityOptions.IGNORE_FILE_PROPERTIES))){
            Assertions.assertEquals(expected.toYAML(false), actual.toYAML(false), message);
        }
    }

    public static void assertEqualsAllExceptFileProps(MetadataInfo expected, MetadataInfo actual,  String message) {
        if(!actual.isEquivalent(expected, EnumSet.of(MetadataInfo.EqualityOptions.IGNORE_FILE_PROPERTIES))){
            Assertions.assertEquals(expected.toYAML(false), actual.toYAML(false), message);
        }
    }

    public static void assertEqualsAll(MetadataInfo expected, MetadataInfo actual,  String message) {
        if(!actual.isEquivalent(expected, EnumSet.noneOf(MetadataInfo.EqualityOptions.class))){
            Assertions.assertEquals(expected.toYAML(false), actual.toYAML(false), message);
        }
    }


    public static void checkFileHasChangedMetadata(PMTuple initialFile, File savedAs, MetadataInfo changed) throws XmpParsingException, IOException {
        MetadataInfo saved = new MetadataInfo();
        if(savedAs != null) {
            PmeExtension.get().newPdfReader().loadFromPDF(initialFile.file, saved);
            assertEqualsAll(initialFile.md, saved , "Original file metadata differs");
        } else {
            savedAs = initialFile.file;
        }
        PmeExtension.get().newPdfReader().loadFromPDF(savedAs, saved);
        List<String> expectedChangedKeys = changed.enabledKeys();
        for(String k: expectedChangedKeys){
            saved.setEnabled(k, false);
        }
        assertEqualsOnlyEnabledExceptFile(initialFile.md, saved , "Non edited metadata differs");
        saved.setEnabled(false);
        for(String k: expectedChangedKeys){
            saved.setEnabled(k, true);
        }
        assertEqualsOnlyEnabledExceptFile(changed, saved, "Edited metadata differs");

    }

    public static  MetadataInfo load(File file) throws XmpParsingException, IOException {
        return load(file, null);
    }

    public static  MetadataInfo load(File file, String password) throws XmpParsingException, IOException {
        MetadataInfo metadataInfo = new MetadataInfo();
        PmeExtension.get().newPdfReader().loadFromPDF(file, password, metadataInfo);
        return metadataInfo;
    }


    public static  File  save(MetadataInfo md, File file) throws Exception {
        return PmeExtension.get().newPdfWriter().saveAsPDF(md, file);
    }

    public static class PMTuple {
        public final File file;
        public final MetadataInfo md;

        public PMTuple(File file, MetadataInfo md) {
            this.file = file;
            this.md = md;
        }
    }
}
