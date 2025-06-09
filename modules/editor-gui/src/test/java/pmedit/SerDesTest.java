package pmedit;


import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultTimeZone;
import pmedit.serdes.SerDeslUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DefaultTimeZone("UTC")
public class SerDesTest {
	static final String ALL_NULLS_YAML =
			"""
			doc.title: null
			doc.author: null
			doc.subject: null
			doc.keywords: null
			doc.creator: null
			doc.producer: null
			doc.creationDate: null
			doc.modificationDate: null
			doc.trapped: null
			basic.creatorTool: null
			basic.createDate: null
			basic.modifyDate: null
			basic.baseURL: null
			basic.rating: null
			basic.label: null
			basic.nickname: null
			basic.identifiers: null
			basic.advisories: null
			basic.metadataDate: null
			pdf.pdfVersion: null
			pdf.keywords: null
			pdf.producer: null
			dc.title: null
			dc.description: null
			dc.creators: null
			dc.contributors: null
			dc.coverage: null
			dc.dates: null
			dc.format: null
			dc.identifier: null
			dc.languages: null
			dc.publishers: null
			dc.relationships: null
			dc.rights: null
			dc.source: null
			dc.subjects: null
			dc.types: null
			rights.certificate: null
			rights.marked: null
			rights.owner: null
			rights.usageTerms: null
			rights.webStatement: null
			viewer.hideToolbar: null
			viewer.hideMenuBar: null
			viewer.hideWindowUI: null
			viewer.fitWindow: null
			viewer.centerWindow: null
			viewer.displayDocTitle: null
			viewer.nonFullScreenPageMode: null
			viewer.readingDirection: null
			viewer.viewArea: null
			viewer.viewClip: null
			viewer.printArea: null
			viewer.printClip: null
			viewer.duplex: null
			viewer.printScaling: null
			viewer.pageLayout: null
			viewer.pageMode: null
			prop.version: null
			prop.compression: null
			prop.encryption: null
			prop.keyLength: null
			prop.ownerPassword: null
			prop.userPassword: null
			prop.canPrint: null
			prop.canModify: null
			prop.canExtractContent: null
			prop.canModifyAnnotations: null
			prop.canFillFormFields: null
			prop.canExtractForAccessibility: null
			prop.canAssembleDocument: null
			prop.canPrintFaithful: null
			file.fullPath: null
			file.sizeBytes: null
			file.size: null
			file.nameWithExt: null
			file.name: null
			file.createTime: null
			file.modifyTime: null
			""";
	static final String ALL_NULLS_JSON =
			"""
            {
              "doc.title" : null,
              "doc.author" : null,
              "doc.subject" : null,
              "doc.keywords" : null,
              "doc.creator" : null,
              "doc.producer" : null,
              "doc.creationDate" : null,
              "doc.modificationDate" : null,
              "doc.trapped" : null,
              "basic.creatorTool" : null,
              "basic.createDate" : null,
              "basic.modifyDate" : null,
              "basic.baseURL" : null,
              "basic.rating" : null,
              "basic.label" : null,
              "basic.nickname" : null,
              "basic.identifiers" : null,
              "basic.advisories" : null,
              "basic.metadataDate" : null,
              "pdf.pdfVersion" : null,
              "pdf.keywords" : null,
              "pdf.producer" : null,
              "dc.title" : null,
              "dc.description" : null,
              "dc.creators" : null,
              "dc.contributors" : null,
              "dc.coverage" : null,
              "dc.dates" : null,
              "dc.format" : null,
              "dc.identifier" : null,
              "dc.languages" : null,
              "dc.publishers" : null,
              "dc.relationships" : null,
              "dc.rights" : null,
              "dc.source" : null,
              "dc.subjects" : null,
              "dc.types" : null,
              "rights.certificate" : null,
              "rights.marked" : null,
              "rights.owner" : null,
              "rights.usageTerms" : null,
              "rights.webStatement" : null,
              "viewer.hideToolbar" : null,
              "viewer.hideMenuBar" : null,
              "viewer.hideWindowUI" : null,
              "viewer.fitWindow" : null,
              "viewer.centerWindow" : null,
              "viewer.displayDocTitle" : null,
              "viewer.nonFullScreenPageMode" : null,
              "viewer.readingDirection" : null,
              "viewer.viewArea" : null,
              "viewer.viewClip" : null,
              "viewer.printArea" : null,
              "viewer.printClip" : null,
              "viewer.duplex" : null,
              "viewer.printScaling" : null,
              "viewer.pageLayout" : null,
              "viewer.pageMode" : null,
              "prop.version" : null,
              "prop.compression" : null,
              "prop.encryption" : null,
              "prop.keyLength" : null,
              "prop.ownerPassword" : null,
              "prop.userPassword" : null,
              "prop.canPrint" : null,
              "prop.canModify" : null,
              "prop.canExtractContent" : null,
              "prop.canModifyAnnotations" : null,
              "prop.canFillFormFields" : null,
              "prop.canExtractForAccessibility" : null,
              "prop.canAssembleDocument" : null,
              "prop.canPrintFaithful" : null,
              "file.fullPath" : null,
              "file.sizeBytes" : null,
              "file.size" : null,
              "file.nameWithExt" : null,
              "file.name" : null,
              "file.createTime" : null,
              "file.modifyTime" : null
            }
            """;



	@Test
	public void testToYaml() {
		MetadataInfo md = new MetadataInfo();
		md.doc.author = "AUTHOR";
		md.doc.creationDate = new GregorianCalendar(2020,2, 2, 1, 1, 1);
		String expected = ALL_NULLS_YAML
				.replace("doc.author: null", "doc.author: \"AUTHOR\"")
				.replace("doc.creationDate: null", "doc.creationDate: \"2020-03-02T01:01:01.000+00:00\"");
		assertEquals(expected, md.toYAML());;
	}

	@Test
	public void testFromYaml() {
		MetadataInfo md = new MetadataInfo();
		md.doc.author = "AUTHOR";
		md.doc.creationDate = new GregorianCalendar(2020,2, 2, 1, 1, 1);
		String yaml = ALL_NULLS_YAML
				.replace("doc.author: null", "doc.author: \"AUTHOR\"")
				.replace("doc.creationDate: null", "doc.creationDate: \"2020-03-02T01:01:01.000+00:00\"");
		MetadataInfo parsed = new MetadataInfo();
		parsed.fromYAML(yaml);;
		assertTrue(md.isEquivalent(parsed));
	}


	@Test
	public void testToJson() {
		MetadataInfo md = new MetadataInfo();
		md.doc.author = "AUTHOR";
		md.doc.creationDate = new GregorianCalendar(2020,2, 2, 1, 1, 1);
		String expected = ALL_NULLS_JSON
				.replace("\"doc.author\" : null", "\"doc.author\" : \"AUTHOR\"")
				.replace("\"doc.creationDate\" : null", "\"doc.creationDate\" : \"2020-03-02T01:01:01.000+00:00\"");
		String expectedJson = expected.trim();
		if(OsCheck.isWindows()){
			expectedJson = expectedJson.replace("\n", "\r\n");
		}
		assertEquals(expectedJson, md.toJson().trim());;
	}

	@Test
	public void testFromJson() {
		MetadataInfo md = new MetadataInfo();
		md.doc.author = "AUTHOR";
		md.doc.creationDate = new GregorianCalendar(2020,2, 2, 1, 1, 1);
		String json = ALL_NULLS_JSON
				.replace("\"doc.author\" : null", "\"doc.author\" : \"AUTHOR\"")
				.replace("\"doc.creationDate\" : null", "\"doc.creationDate\" : \"2020-03-02T01:01:01.000+00:00\"");
		MetadataInfo parsed = new MetadataInfo();
		parsed.fromJson(json);;
		assertTrue(md.isEquivalent(parsed));
	}

	@Test
	public void testFromJson2() {
		List<HashMap<String, Object>> o = SerDeslUtils.listFromJSON("""
				[{"a":1}, {"b":2}]
		""");
        assertInstanceOf(List.class, o);
		assertEquals(o.get(0).get("a"), 1);
		assertEquals(o.get(1).get("b"), 2);
	}


}
