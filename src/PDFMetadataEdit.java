import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
			System.out.println("rename - rename file accorfing to metadata");
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
		if (args[0].equals("rename")) {
			String template = args[1];
			if(!template.toLowerCase().endsWith(".pdf"))
				template += ".pdf";
			TemplateString ts = new TemplateString(template);
			try{
				for(int i=2; i < args.length; ++i){
					MetadataInfo md = new MetadataInfo();
					File from = new File(args[i]);
					md.loadFromPDF(from);
					String toName = ts.process(md);
					System.out.print(from.toString() + " -> " + toName);
					String toDir= from.getParent();
					File to = new File(toDir,toName);
					boolean success =  from.renameTo(to);
					System.out.println(success? " OK" : " FAIL");
					
				}
			} catch (IOException e) {
				System.out.println("Failed to parse: " + args[1]);
				System.out.println(e.toString());
				System.exit(1);
			}
		}
	}

}
