package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import pmedit.CommandLine;
import pmedit.CommandLine.ParseError;
import pmedit.MetadataInfo;

public class CommandLineTest {

	List<String> mdFieldList = Arrays.asList(new String[] { "basic.title", "basic.author", "basic.subject", "basic.keywords",
			"basic.creator", "basic.producer", "basic.creationDate", "basic.modificationDate", "basic.trapped",
			"xmpBasic.creatorTool", "xmpBasic.createDate", "xmpBasic.modifyDate", "xmpBasic.title", "xmpBasic.baseURL",
			"xmpBasic.rating", "xmpBasic.label", "xmpBasic.nickname", "xmpBasic.identifiers", "xmpBasic.advisories",
			"xmpBasic.metadataDate", "xmpPdf.pdfVersion", "xmpPdf.keywords", "xmpPdf.producer", "xmpDc.title",
			"xmpDc.description", "xmpDc.creators", "xmpDc.contributors", "xmpDc.coverage", "xmpDc.dates",
			"xmpDc.format", "xmpDc.identifier", "xmpDc.languages", "xmpDc.publishers", "xmpDc.relationships",
			"xmpDc.rights", "xmpDc.source", "xmpDc.subjects", "xmpDc.types" });

	
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
		args.add("!basic.title");
		c = CommandLine.parse(args);
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNotNull(c.command);
		assertTrue(c.command.is("clear"));
		for(String field: mdFieldList){
			if( field.equals("basic.title") ){
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
			} else if(md.getFieldType(field).isAssignableFrom(List.class)){
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
				assertEquals(((Calendar) c.params.metadata.get(field)), cal);
			} else  if(field.endsWith(".dates")){
				assertEquals(c.params.metadata.get(field), Arrays.asList(cal, cal));
			} else if(field.endsWith(".rating")){
				assertEquals(c.params.metadata.get(field), 17);
			} else if(md.getFieldType(field).isAssignableFrom(List.class)){
				assertEquals(c.params.metadata.get(field), Arrays.asList(field, field));
			} else {
				assertEquals(c.params.metadata.get(field), field);
			}
		}
		assertTrue(c.fileList.isEmpty());
	}

	
	
	@Test
	public void testValid2() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				 "basic.title=title"
		});
		assertNotNull(c);
		assertFalse(c.noGui);
		assertNull(c.command);
		assertEquals(c.params.metadata.basic.title, "title");
	}

	@Test(expected = ParseError.class)
	public void testInvalid1() throws ParseError {
		CommandLine c;
		c = CommandLine.parse(new String[]{
				 "--something", "editv", "basic.creationDate"
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
