package pmedit;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.junit.Test;

public class TestPrismNamespace {
	
	
	@Test
	public void testPrism() throws IOException {
		String xmp = "<?xpacket begin=\"ï»¿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?><x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.4-c005 78.147326, 2012/08/23-13:03:03        \">\r\n" + 
				"   <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\r\n" + 
				"      <rdf:Description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\" xmlns:pdfx=\"http://ns.adobe.com/pdfx/1.3/\" xmlns:prism=\"http://prismstandard.org/namespaces/basic/2.0/\" xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" xmlns:xmpRights=\"http://ns.adobe.com/xap/1.0/rights/\" rdf:about=\"\">\r\n" + 
				"         <dc:format>application/pdf</dc:format>\r\n" + 
				"         <dc:identifier>doi:10.1016/j.survophthal.2012.08.008</dc:identifier>\r\n" + 
				"         <dc:title>\r\n" + 
				"            <rdf:Alt>\r\n" + 
				"               <rdf:li xml:lang=\"x-default\">Serpiginous Choroiditis and Infectious Multifocal Serpiginoid Choroiditis</rdf:li>\r\n" + 
				"            </rdf:Alt>\r\n" + 
				"         </dc:title>\r\n" + 
				"         <dc:creator>\r\n" + 
				"            <rdf:Seq>\r\n" + 
				"               \r\n" + 
				"               \r\n" + 
				"            <rdf:li>Hossein Nazari Khanamiri MD</rdf:li>\r\n" + 
				"<rdf:li>Narsing A. Rao MD</rdf:li>\r\n" + 
				"</rdf:Seq>\r\n" + 
				"         </dc:creator>\r\n" + 
				"         <dc:subject>\r\n" + 
				"            <rdf:Bag>\r\n" + 
				"               \r\n" + 
				"               \r\n" + 
				"               \r\n" + 
				"               \r\n" + 
				"               \r\n" + 
				"            <rdf:li>serpiginous choroiditis</rdf:li>\r\n" + 
				"<rdf:li>serpiginous-like choroiditis</rdf:li>\r\n" + 
				"<rdf:li>multifocal serpiginoid choroiditis</rdf:li>\r\n" + 
				"<rdf:li>tuberculosis</rdf:li>\r\n" + 
				"<rdf:li>herpes virus</rdf:li>\r\n" + 
				"</rdf:Bag>\r\n" + 
				"         </dc:subject>\r\n" + 
				"         <dc:description>\r\n" + 
				"            <rdf:Alt>\r\n" + 
				"               <rdf:li xml:lang=\"x-default\">Survey of Ophthalmology, 58 (2013) 203-232. doi:10.1016/j.survophthal.2012.08.008</rdf:li>\r\n" + 
				"            </rdf:Alt>\r\n" + 
				"         </dc:description>\r\n" + 
				"         <dc:publisher>\r\n" + 
				"            <rdf:Bag>\r\n" + 
				"               \r\n" + 
				"            <rdf:li>Elsevier Inc</rdf:li>\r\n" + 
				"</rdf:Bag>\r\n" + 
				"         </dc:publisher>\r\n" + 
				"         <prism:aggregationType>journal</prism:aggregationType>\r\n" + 
				"         <prism:publicationName>Survey of Ophthalmology</prism:publicationName>\r\n" + 
				"         <prism:copyright>Copyright Â©Â 2013 by Elsevier Inc.All rights reserved</prism:copyright>\r\n" + 
				"         <prism:issn>0039-6257</prism:issn>\r\n" + 
				"         <prism:volume>58</prism:volume>\r\n" + 
				"         <prism:number>3</prism:number>\r\n" + 
				"         <prism:coverDisplayDate>May-June 2013</prism:coverDisplayDate>\r\n" + 
				"         <prism:coverDate>2013-05-06</prism:coverDate>\r\n" + 
				"         <prism:pageRange>203-232</prism:pageRange>\r\n" + 
				"         <prism:startingPage>203</prism:startingPage>\r\n" + 
				"         <prism:endingPage>232</prism:endingPage>\r\n" + 
				"         <prism:doi>10.1016/j.survophthal.2012.08.008</prism:doi>\r\n" + 
				"         <prism:url>http://dx.doi.org/10.1016/j.survophthal.2012.08.008</prism:url>\r\n" + 
				"         <pdfx:ElsevierWebPDFSpecifications>6.3</pdfx:ElsevierWebPDFSpecifications>\r\n" + 
				"         <pdfx:doi>10.1016/j.survophthal.2012.08.008</pdfx:doi>\r\n" + 
				"         <pdfx:robots>noindex</pdfx:robots>\r\n" + 
				"         <xmp:CreatorTool>Elsevier</xmp:CreatorTool>\r\n" + 
				"         \r\n" + 
				"         \r\n" + 
				"         \r\n" + 
				"         <xmpRights:Marked>True</xmpRights:Marked>\r\n" + 
				"         <pdf:Producer>Acrobat Distiller 8.1.0 (Windows)</pdf:Producer>\r\n" + 
				"         <xmpMM:DocumentID>uuid:2fe3af88-bbfd-42a1-a58f-fa1902db88e0</xmpMM:DocumentID>\r\n" + 
				"         <xmpMM:InstanceID>uuid:0f9c640c-68b5-4937-9716-9ef527630e2c</xmpMM:InstanceID>\r\n" + 
				"      <xmp:CreateDate>2013-04-12T03:16:31+03:00</xmp:CreateDate>\r\n" + 
				"<xmp:ModifyDate>2016-07-22T01:53:00+03:00</xmp:ModifyDate>\r\n" + 
				"<xmp:MetadataDate>2014-05-14T01:22:38+03:00</xmp:MetadataDate>\r\n" + 
				"<xmp:Rating>0</xmp:Rating>\r\n" + 
				"<xmp:Title>TITLE</xmp:Title>\r\n" + 
				"</rdf:Description>\r\n" + 
				"   </rdf:RDF>\r\n" + 
				"</x:xmpmeta><?xpacket end=\"w\"?>\r\n" + 
				"";

		XMPMetadata meta = XMPMetadata.load(new ByteArrayInputStream(xmp.getBytes()));

	}

}
