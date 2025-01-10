package pmedit;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;

import pmedit.MetadataInfo.FieldDescription;

class MetadataInfoTest {
	static int NUM_FILES = 5;
	
	public static class PMTuple{
		final File file;
		final MetadataInfo md;
		public PMTuple(File file, MetadataInfo md){
			this.file = file;
			this.md = md;
		}
	}
	
	public static File emptyPdf() throws Exception{
		File tempDir = new File("target/test-data");
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}
		File temp = File.createTempFile("test-file", ".pdf", tempDir);
        PDDocument doc = new PDDocument();
        try {
            // a valid PDF document requires at least one page
            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
            doc.save(temp);
        } finally {
            doc.close();
        }
        temp.deleteOnExit();
        return temp;
	}

	public static File csvFile(List<String> lines) throws Exception{
		File temp = File.createTempFile("test-csv", ".csv");
		Files.write(temp.toPath(), lines, Charset.forName("UTF-8"));
		temp.deleteOnExit();
        return temp;
	}

	public static List<PMTuple> randomFiles(int numFiles) throws Exception {
		return randomFiles(numFiles, null);
	}

	public static List<PMTuple> randomFiles(int numFiles, AccessPermission permission) throws Exception{
		List<String> fields = MetadataInfo.keys();
		int numFields = fields.size();
		List<PMTuple> rval = new ArrayList<MetadataInfoTest.PMTuple>();
		
		Random rand = new Random();
		for(int i=0; i<numFiles; ++i) {
			MetadataInfo md = new MetadataInfo();
			//int genFields = rand.nextInt(numFields);
			int genFields = numFields;
			for(int j=0; j< genFields; ++j){
				String field = fields.get(rand.nextInt(numFields));
				// 	ignore file fields as they are read only
				if(field.startsWith("file.")) {
					--j;
					continue;
				}
				if(field.equals("doc.trapped")){
					md.setAppend(field, Arrays.asList("False", "True","Unknown").get(rand.nextInt(3)));
					continue;
				}
				if(field.equals("rights.copyright")){
					continue;
				}

				FieldDescription fd = MetadataInfo.getFieldDescription(field); 
				switch(fd.type){
				case LongField:
					md.setAppend(field, (long)rand.nextInt(1000));
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
					md.setAppend(field, cal);
					break;
				default:
					md.setAppend(field, new BigInteger(130, rand).toString(32));
					break;
				}
			}
			File pdf = emptyPdf();
			if (permission != null ) {
				md.encryptionOptions =new EncryptionOptions(true, permission, "pass", "");
			}
			md.saveAsPDF(pdf);
			rval.add(new PMTuple(pdf, md));
		}
		return rval;
	}
	
	@BeforeEach
	public void setUp() throws Exception {
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
    void testSimpleEquality() {
		Assertions.assertTrue(new MetadataInfo().isEquivalent(new MetadataInfo()));
		Assertions.assertTrue(MetadataInfo.getSampleMetadata().isEquivalent(MetadataInfo.getSampleMetadata()));
		
		MetadataInfo md1 = new MetadataInfo();
		MetadataInfo md2 = new MetadataInfo();


		md1.setAppendFromString("doc.title", "a title");
		Assertions.assertFalse(md1.isEquivalent(md2));
		
		md2.setAppendFromString("doc.title", md1.getString("doc.title"));
		Assertions.assertTrue(md1.isEquivalent(md2));

		md1.setAppendFromString("basic.rating", "333");
		Assertions.assertFalse(md1.isEquivalent(md2));
		
		md2.setAppendFromString("basic.rating", "333");
		Assertions.assertTrue(md1.isEquivalent(md2));

		md1.setAppendFromString("rights.marked", "true");
		Assertions.assertFalse(md1.isEquivalent(md2));

		md2.setAppendFromString("rights.marked", "true");
		Assertions.assertTrue(md1.isEquivalent(md2));
	}
	
	@Test
    void testEmptyLoad() throws Exception, IOException, Exception{
		MetadataInfo md = new MetadataInfo();
		md.loadFromPDF(emptyPdf());
		Assertions.assertTrue(md.isEquivalent(new MetadataInfo()));
	}
	
	@Test
    void testFuzzing() throws Exception {
		for(PMTuple t: randomFiles(NUM_FILES)){
			MetadataInfo loaded = new MetadataInfo();
			loaded.loadFromPDF(t.file);
			if( !t.md.isEquivalent(loaded) ){
				System.out.println(t.file.getAbsolutePath());
				System.out.println("SAVED:" );
				System.out.println("=========" );
				System.out.println(t.md.toYAML(true));
				System.out.println("LOADED:" );
				System.out.println("=========" );
				System.out.println(loaded.toYAML(true));
			}

			Assertions.assertTrue(t.md.isEquivalent(loaded));
		}
	}

	@Test
    void testRemove() throws Exception, IOException, Exception{
		for(PMTuple t: randomFiles(1)){
			MetadataInfo loaded = new MetadataInfo();
			loaded.loadFromPDF(t.file);
			loaded.removeDocumentInfo = true;
			loaded.removeXmp = true;

			loaded.saveAsPDF(t.file);

            try (PDDocument document = Loader.loadPDF(t.file)) {
				Assertions.assertNull(document.getDocument().getTrailer().getCOSDictionary(COSName.INFO));
				Assertions.assertNull(document.getDocumentCatalog().getMetadata());
				Assertions.assertNull(document.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.METADATA));
            }
		}
	}


}
