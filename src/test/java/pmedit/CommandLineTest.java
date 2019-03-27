package pmedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import pmedit.CommandLine.ParseError;

public class CommandLineTest {

	List<String> mdFieldList = Arrays.asList(new String[] { "doc.title", "doc.author", "doc.subject", "doc.keywords",
			"doc.creator", "doc.producer", "doc.creationDate", "doc.modificationDate", "doc.trapped",
			"basic.creatorTool", "basic.createDate", "basic.modifyDate", "basic.baseURL",
			"basic.rating", "basic.label", "basic.nickname", "basic.identifiers", "basic.advisories",
			"basic.metadataDate", "pdf.pdfVersion", "pdf.keywords", "pdf.producer", "dc.title",
			"dc.description", "dc.creators", "dc.contributors", "dc.coverage", "dc.dates",
			"dc.format", "dc.identifier", "dc.languages", "dc.publishers", "dc.relationships",
			"dc.rights", "dc.source", "dc.subjects", "dc.types" });

	
	@Test
	public void testValid() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				"-nogui", "edit", "--", "file1", "file2"
		});
		assertNotNull(c);
		assertTrue(c.noGui);
		assertTrue(c.command.is("edit"));
		assertTrue(c.fileList.equals(Arrays.asList("file1", "file2")));
	}

	@Test
	public void testClear() throws ParseError {
		CommandLine c;
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.addAll(mdFieldList);
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("clear"));
		for(String field: mdFieldList){
			assertTrue(c.params.metadata.isEnabled(field));
		}
		assertTrue(c.fileList.isEmpty());
	}

	@Test
	public void testClearNone() throws ParseError {
		CommandLine c;
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.add("none");
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("clear"));
		for(String field: mdFieldList){
			assertFalse(c.params.metadata.isEnabled(field));
		}
		assertTrue(c.fileList.isEmpty());
	}

	@Test
	public void testClearAll() throws ParseError {
		CommandLine c;
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.add("all");
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("clear"));
		for(String field: mdFieldList){
			assertTrue(c.params.metadata.isEnabled(field));
		}
		assertTrue(c.fileList.isEmpty());
	}

	@Test
	public void testClearSome() throws ParseError {
		CommandLine c;
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.add("all");
		args.add("!doc.title");
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("clear"));
		for(String field: mdFieldList){
			if( field.equals("doc.title") ){
				assertFalse(c.params.metadata.isEnabled(field));				
			} else {
				assertTrue(c.params.metadata.isEnabled(field));
			}
		}
		assertTrue(c.fileList.isEmpty());
	}
	
	@Test
	public void testEditAll() throws ParseError {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2012, 03, 03);
		String dateString = "2012-04-03";
		List<String> genList = new ArrayList<String>();
		MetadataInfo md = new MetadataInfo();
		for(String field: mdFieldList){
			if(field.endsWith("Date")){
				genList.add(field + "=" + dateString );
			} else  if(field.endsWith(".dates")){
				genList.add(field + "=" + dateString );
				genList.add(field + "=" + dateString );
			} else if(field.endsWith(".rating")){
				genList.add(field + "=17");
			} else if(md.getFieldDescription(field).isList){
				genList.add(field + "=" + field );
				genList.add(field + "=" + field );
			} else {
				genList.add(field + "=" + field );
			}
		}
		
		CommandLine c;
		List<String> args = new ArrayList<String>();
		args.add("edit");
		args.addAll(genList);
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("edit"));
		for(String field: mdFieldList){
			assertTrue(c.params.metadata.isEnabled(field));
			if(field.endsWith("Date")){
				assertEquals(cal, ((Calendar) c.params.metadata.get(field)));
			} else  if(field.endsWith(".dates")){
				assertEquals(Arrays.asList(cal, cal), c.params.metadata.get(field));
			} else if(field.endsWith(".rating")){
				assertEquals(17, c.params.metadata.get(field));
			} else if(md.getFieldDescription(field).isList){
				assertEquals(Arrays.asList(field, field), c.params.metadata.get(field));
			} else {
				assertEquals(field, c.params.metadata.get(field));
			}
		}
		assertTrue(c.fileList.isEmpty());
	}

	
	
	@Test
	public void testValid2() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				 "doc.title=title"
		});
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNull(c.command);
		assertEquals(c.params.metadata.doc.title, "title");
	}

	@Test(expected = ParseError.class)
	public void testInvalid1() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				 "--something", "editv", "doc.creationDate"
		});
	}

	@Test(expected = ParseError.class)
	public void testInvalid2() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				 "--renameTemplate"
		});
	}

}
