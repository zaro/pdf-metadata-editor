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
			Properties props = new Properties();
			DateFormat df =DateFormat.getInstance();
			try {
				PDDocument document = PDDocument.load(new FileInputStream(
						args[1]));
				PDDocumentInformation info = document.getDocumentInformation();
				props.setProperty("basic.title", info.getTitle());
				props.setProperty("basic.author", info.getAuthor());
				props.setProperty("basic.subject", info.getSubject());
				props.setProperty("basic.keywords", info.getKeywords());
				props.setProperty("basic.creator", info.getCreator());
				props.setProperty("basic.producer", info.getProducer());
				props.setProperty("basic.creationDate", df.format(info.getCreationDate()));
				props.setProperty("basic.modificationDate", df.format(info.getModificationDate()));
				props.setProperty("basic.trapped", info.getTrapped());

				PDDocumentCatalog catalog = document.getDocumentCatalog();
				PDMetadata metadata = catalog.getMetadata();

				XMPMetadata xmp = XMPMetadata
						.load(metadata.createInputStream());
				XMPSchemaBasic b = xmp.getBasicSchema();
				System.out.println("---- XMP Basic");
				System.out.println("> Advisories");
				if (b.getAdvisories() != null) {
					for (String s : b.getAdvisories()) {
						System.out.println(s);
					}
				} else {
					System.out.println("*Not set*");					
				}
				System.out.print("> BaseURL=");
				System.out.println(b.getBaseURL());
				System.out.print("> CreateDate=");
				System.out.println(String.format("%1$td-%1$tm-%1$tY",
						b.getCreateDate()));
				System.out.print("> CreatorTool=");
				System.out.println(b.getCreatorTool());
				System.out.println("> Identifiers");
				if (b.getIdentifiers() != null) {
					for (String s : b.getIdentifiers()) {
						System.out.println(s);
					}
				} else {
					System.out.println("*Not set*");					
				}
				System.out.print("> Label=");
				System.out.println(b.getLabel());
				System.out.print("> MetadataDate=");
				System.out.println(b.getMetadataDate());
				System.out.print("> ModifyDate=");
				System.out.println(b.getModifyDate());
				System.out.print("> Nickname=");
				System.out.println(b.getNickname());
				System.out.print("> Rating=");
				System.out.println(b.getRating());
				System.out.print("> Title=");
				System.out.println(b.getTitle());

				XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
				System.out.println("---- XMP Dublin Core");
				System.out.println("> Contributors");
				if (dc.getContributors() != null) {
					for (String s : dc.getContributors()) {
						System.out.println(s);
					}
				} else {
					System.out.println("*Not set*");					
				}
				System.out.print("> Coverage=");
				System.out.println(dc.getCoverage());
				System.out.println("> Creators");
				if (dc.getContributors() != null) {
					for (String s : dc.getCreators()) {
						System.out.println(s);
					}
				} else {
					System.out.println("*Not set*");					
				}				
				System.out.println("> Dates");
				if (dc.getDates() != null) {
					for (Calendar s : dc.getDates()) {
						System.out.println(String.format("%1$td-%1$tm-%1$tY",s));
					}
				} else {
					System.out.println("*Not set*");					
				}				
				System.out.print("> Description=");
				System.out.println(dc.getDescription());
				System.out.print("> Format=");
				System.out.println(dc.getFormat());
				System.out.print("> Identifer=");
				System.out.println(dc.getIdentifier());
				System.out.println("> Languages");
				if (dc.getLanguages() != null) {
					for (String s : dc.getLanguages()) {
						System.out.println(s);					
					}
				} else {
					System.out.println("*Not set*");					
				}				
				System.out.println("> Publishers");
				if (dc.getPublishers() != null) {
					for (String s : dc.getPublishers()) {
						System.out.println(s);					
					}
				} else {
					System.out.println("*Not set*");					
				}				
				System.out.println("> Relationships");
				if (dc.getRelationships() != null) {
					for (String s : dc.getRelationships()) {
						System.out.println(s);					
					}
				} else {
					System.out.println("*Not set*");					
				}
				System.out.print("> Rights=");
				System.out.println(dc.getRights());
				System.out.print("> Source=");
				System.out.println(dc.getSource());
				System.out.println("> Subjects");
				if (dc.getSubjects() != null) {
					for (String s : dc.getSubjects()) {
						System.out.println(s);					
					}
				} else {
					System.out.println("*Not set*");					
				}					
				System.out.print("> Title=");
				System.out.println(dc.getTitle());
				System.out.println("> Types");
				if (dc.getTypes() != null) {
					for (String s : dc.getTypes()) {
						System.out.println(s);					
					}
				} else {
					System.out.println("*Not set*");					
				}					
				/*
				 * InputStreamReader xmlInputStream = new
				 * InputStreamReader(metadata.createInputStream());
				 * BufferedReader xmlStream = new
				 * BufferedReader(xmlInputStream); String line; while((line =
				 * xmlStream.readLine()) != null){ System.out.println(line); }
				 */
			} catch (Exception e) {
				System.out.println("Failed to parse: " + args[1]);
				System.out.println(e.toString());
				System.exit(1);
			}
		}
	}

}
