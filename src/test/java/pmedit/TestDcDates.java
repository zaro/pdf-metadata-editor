package pmedit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.Test;

public class TestDcDates {
	
	
	@Test
	public void test() throws TransformerException, IOException, XmpParsingException {
		File temp = File.createTempFile("test-file", ".pdf");
        temp.deleteOnExit();
        Calendar cal = Calendar.getInstance();
        
        // Create empty document
        PDDocument doc = new PDDocument();
        try {
            // a valid PDF document requires at least one page
            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
    		XMPMetadata xmpNew = XMPMetadata.createXMPMetadata();
			DublinCoreSchema dcS = xmpNew.createAndAddDublinCoreSchema();

			dcS.addDate(cal);

			PDDocumentCatalog catalog = doc.getDocumentCatalog();
			PDMetadata metadataStream = new PDMetadata(doc);

			XmpSerializer serializer = new XmpSerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.serialize(xmpNew, baos, true);
			metadataStream.importXMPMetadata(baos.toByteArray());
			catalog.setMetadata(metadataStream);

            doc.save(temp);
        } finally {
            doc.close();
        }
        
        // Read the DC dates field
		PDDocument document =  PDDocument.load(new FileInputStream(temp));
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata meta = catalog.getMetadata();
		DomXmpParser xmpParser = new DomXmpParser();
		XMPMetadata metadata = xmpParser.parse(meta.createInputStream());
		DublinCoreSchema dcS = metadata.getDublinCoreSchema();

		List<Calendar> actual = dcS.getDates();
		
		//assertEquals(1, actual.size());
		//assertEquals(cal, actual.get(0));

	}

}
