package pmedit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.jupiter.api.Assertions;

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
        return randomFiles(numFiles, null, true, null);
    }
    public static List<PMTuple> randomFiles(int numFiles, Consumer<MetadataInfo> beforeSave) throws Exception {
        return randomFiles(numFiles, null, true, beforeSave);
    }

    public static List<PMTuple> randomFiles(int numFiles, boolean allFields) throws Exception {
        return randomFiles(numFiles, null, allFields, null);
    }
    public static List<PMTuple> randomFiles(int numFiles, AccessPermission permission, boolean allFields) throws Exception {
        return randomFiles(numFiles, permission, allFields, null);
    }

    public static List<PMTuple> randomFiles(int numFiles, AccessPermission permission, boolean allFields, Consumer<MetadataInfo> beforeSave) throws Exception {
        List<String> fields = MetadataInfo.keys();
        List<PMTuple> rval = new ArrayList<FilesTestHelper.PMTuple>();

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
           genFields = genFields.stream().filter( f -> !f.startsWith("file.")).toList();
            for (int j = 0; j < genFields.size(); ++j) {
                String field = fields.get(j);

                if (field.equals("doc.trapped")) {
                    md.setAppend(field, Arrays.asList("False", "True", "Unknown").get(rand.nextInt(3)));
                    continue;
                }

                if (field.equals("doc.pdfVersion")) {
                    md.setAppend(field, Arrays.asList(1.3f, 1.4f, 1.5f, 1.6f, 1.7f).get(rand.nextInt(3)));
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
            if (permission != null) {
                md.encryptionOptions = new EncryptionOptions(true, permission, "pass", "");
            }
            if(beforeSave != null ){
                beforeSave.accept(md);
            }
            md.saveAsPDF(pdf);
            md.loadPDFFileInfo(pdf);
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

    public static void assertEquals(MetadataInfo expected, MetadataInfo actual, boolean onlyEnabled, String message) {
        if(!actual.isEquivalent(expected, onlyEnabled)){
            Assertions.assertEquals(expected.toYAML(onlyEnabled), actual.toYAML(onlyEnabled), message);
        }
    }


    public static void checkFileHasChangedMetadata(PMTuple initialFile, File savedAs, MetadataInfo changed) throws XmpParsingException, IOException {
        MetadataInfo saved = new MetadataInfo();
        if(savedAs != null) {
            saved.loadFromPDF(initialFile.file);
            assertEquals(initialFile.md, saved , false, "Original file metadata differs");
        } else {
            savedAs = initialFile.file;
        }
        saved.loadFromPDF(savedAs);
        List<String> expectedChangedKeys = changed.enabledKeys();
        for(String k: expectedChangedKeys){
            saved.setEnabled(k, false);
        }
        assertEquals(initialFile.md, saved , true, "Non edited metadata differs");
        saved.setEnabled(false);
        for(String k: expectedChangedKeys){
            saved.setEnabled(k, true);
        }
        assertEquals(changed, saved, true, "Edited metadata differs");

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
