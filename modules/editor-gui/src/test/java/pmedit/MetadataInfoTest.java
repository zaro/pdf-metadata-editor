package pmedit;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import pmedit.ext.PmeExtension;

class MetadataInfoTest extends BaseTest{
	static int NUM_FILES = 5;

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
		MetadataInfo loaded = FilesTestHelper.load(FilesTestHelper.emptyPdf(getTempDir()));

		FilesTestHelper.assertEqualsAllExceptFileProps(loaded,new MetadataInfo(),  "Empty MD Test");
	}
	
	@Test
	@EnabledIfSystemProperty(named = "flavour" , matches ="ext-dev")
	void testFuzzing() throws Exception {
		for(FilesTestHelper.PMTuple t: randomFiles(NUM_FILES)){
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			if( !t.md.isEquivalent(loaded) ){
				System.out.println(t.file.getAbsolutePath());
				System.out.println("SAVED:" );
				System.out.println("=========" );
				System.out.println(t.md.toYAML());
				System.out.println("LOADED:" );
				System.out.println("=========" );
				System.out.println(loaded.toYAML());
			}

			Assertions.assertTrue(t.md.isEquivalent(loaded));
		}
	}

	@Test
	@EnabledIfSystemProperty(named = "flavour" , matches ="ext-dev")
    void testRemove() throws Exception, IOException, Exception{
		for(FilesTestHelper.PMTuple t: randomFiles(1)){
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			loaded.removeDocumentInfo = true;
			loaded.removeXmp = true;

			FilesTestHelper.save(loaded, t.file);

            try (PDDocument document = Loader.loadPDF(t.file)) {
				Assertions.assertNull(document.getDocument().getTrailer().getCOSDictionary(COSName.INFO));
				Assertions.assertNull(document.getDocumentCatalog().getMetadata());
				Assertions.assertNull(document.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.METADATA));
            }
		}
	}


}
