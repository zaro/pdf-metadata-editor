import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Calendar;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

import java.util.Properties;
// Unifinshed attemp for a coomand line tool :)

public class PDFMetadataEdit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.out.println("Available comands:");
			System.out.println("dump - dump metadata");
			System.exit(1);
		}
		if (args[0].equals("dump")) {
			System.out.println("Parsing document" + args[1]);
			MetadataInfo md = new MetadataInfo();
			try{
				md.loadFromPDF(new File(args[1]));
				System.out.print(md.toYAML());
			} catch (Exception e) {
				System.out.println("Failed to parse: " + args[1]);
				System.out.println(e.toString());
				System.exit(1);
			}
		}
	}

}
