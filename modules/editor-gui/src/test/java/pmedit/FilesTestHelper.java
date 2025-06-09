package pmedit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.jupiter.api.Assertions;
import pmedit.ext.BasicPdfWriter;
import pmedit.ext.PdfReader;
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


public class FilesTestHelper {
//    protected static  File tempDir;
    protected static Stack<File> tempDirs = new Stack<>();

    public static void pushTempDir(String name){
        File tempDir = new File(getTempDir(), name);
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
        tempDirs.push(tempDir);
    }

    public static void popTempDir(){
        tempDirs.pop();
    }

    public static File getTempDir(){
        if(tempDirs.isEmpty()){
            File tempDir = new File("target" + File.separator + "test-data" + File.separator + "run-" +DateFormat.formatDateTimeForPath(Calendar.getInstance()));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            tempDirs.push(tempDir);
        }
        return tempDirs.peek();
    }

    public static File emptyPdf() throws Exception {
        File temp = File.createTempFile("test-file-", ".pdf", getTempDir());
        PDDocument doc = new PDDocument();
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

    public static File csvFile(List<String> lines) throws Exception {
        File temp = File.createTempFile("test-csv", ".csv");
        Files.write(temp.toPath(), lines, Charset.forName("UTF-8"));
        temp.deleteOnExit();
        return temp;
    }

    public static List<PMTuple> randomFiles(int numFiles) throws Exception {
        return randomFiles(numFiles, true, null);
    }
    public static List<PMTuple> randomFiles(int numFiles, Consumer<MetadataInfo> beforeSave) throws Exception {
        return randomFiles(numFiles,  true, beforeSave);
    }

    public static List<PMTuple> randomFiles(int numFiles, boolean allFields) throws Exception {
        return randomFiles(numFiles, allFields, null);
    }

    public static List<PMTuple> randomFiles(int numFiles,  boolean allFields, Consumer<MetadataInfo> beforeSave) throws Exception {
        List<String> fields = MetadataInfo.keys();
        List<PMTuple> rval = new ArrayList<FilesTestHelper.PMTuple>();

        // Filter out fields, that should not be random
        fields = fields.stream().filter(f -> {
            return !(
                    f.startsWith("file.") || (
                            f.startsWith("prop.")
                            && !f.equals("prop.version")
                            && !f.equals("prop.compression")
                    )
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


                MetadataInfo.FieldDescription fd = MetadataInfo.getFieldDescription(field);
                switch (fd.type) {
                    case LongField:
                        md.setAppend(field, (long) rand.nextInt(1000));
                        break;
                    case IntField:
                        md.setAppend(field, rand.nextInt(1000));
                        break;
                    case BoolField:
                        md.setAppend(field, ((rand.nextInt(1000) & 1) == 1) ? true : false);
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
                        md.setAppend(field, new BigInteger(130, rand).toString(32));
                        break;
                }
            }

            File pdf = emptyPdf();

            if(beforeSave != null ){
                beforeSave.accept(md);
            }

            // Ensure there keyLength
            if(md.prop.encryption != null && md.prop.encryption && md.prop.keyLength == null) {
                md.prop.keyLength  = Arrays.asList(40, 128, 256).get(rand.nextInt(3));
            }

            //Ensure we are not using compression if not supported by PDF version
            if(md.prop.version != null && md.prop.version < PmeExtension.get().newPdfWriter().getCompressionMinimumSupportedVersion()) {
                md.prop.compression = false;
            }

            md.setEnabledForPrefix("file.", false);
            PmeExtension.get().newPdfWriter().saveAsPDF(md, pdf);
            md.setEnabledForPrefix("file.", true);
            PmeExtension.get().newPdfReader().loadPDFFileInfo(pdf, md);
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
