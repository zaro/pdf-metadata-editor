package pmedit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

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
    protected static  File tempDir;

    public static File getTempDir(){
        if(tempDir == null){
            tempDir = new File("target" + File.separator + "test-data" + File.separator + "run-" +DateFormat.formatDateTimeForPath(Calendar.getInstance()));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
        }
        return tempDir;
    }

    public static File emptyPdf() throws Exception {
        File temp = File.createTempFile("test-file", ".pdf", getTempDir());
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

    public static class PMTuple {
        public final File file;
        public final MetadataInfo md;

        public PMTuple(File file, MetadataInfo md) {
            this.file = file;
            this.md = md;
        }
    }
}
