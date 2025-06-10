package pmedit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.junit.jupiter.api.Test;
import pmedit.FilesTestHelper.PMTuple;

import static org.junit.jupiter.api.Assertions.*;

public class BatchCommandTest {

	static int NUM_FILES = 5;
	@Test
	public void testClearAll() throws FileNotFoundException, IOException, Exception {
		List<PMTuple> fileList = FilesTestHelper.randomFiles(NUM_FILES);
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.add("all");

		for(PMTuple t: fileList){
			args.add(t.file.getAbsolutePath());
		}

		CommandLine c = CommandLine.parse(args);
		PDFMetadataEditBatch batch =new PDFMetadataEditBatch(c.params);
		batch.runCommand(c.command, FileList.fileList(c.fileList), null, new ActionStatus(){
			public void showStatus(String filename, String message) {
			}

			public void showError(String filename, Throwable error) {
                fail(error);
			}

		});
		MetadataInfo empty = new MetadataInfo();
		for(PMTuple t: fileList){
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			//System.out.println(pdf.getAbsolutePath());
			FilesTestHelper.assertEqualsAllExceptFileProps(empty, loaded, "File metadata not cleared");
		}
	}

	@Test
	public void testClearNone() throws FileNotFoundException, IOException, Exception {
		List<PMTuple> fileList = FilesTestHelper.randomFiles(NUM_FILES);
		List<String> args = new ArrayList<String>();
		args.add("clear");
		args.add("none");

		for(PMTuple t: fileList){
			args.add(t.file.getAbsolutePath());
		}

		CommandLine c = CommandLine.parse(args);
		PDFMetadataEditBatch batch =new PDFMetadataEditBatch(c.params);
		batch.runCommand(c.command, FileList.fileList(c.fileList), null,  new ActionStatus(){
			public void showStatus(String filename, String message) {
			}

			public void showError(String filename, Throwable error) {
				fail(error);
			}

		});
		MetadataInfo empty = new MetadataInfo();
		for(PMTuple t: fileList){
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			//System.out.println(pdf.getAbsolutePath());
			FilesTestHelper.assertEqualsAllExceptFileProps(t.md, loaded, "Metadata not supported to be cleared is cleared");

		}
	}

	@Test
	public void testEditSome() throws FileNotFoundException, IOException, Exception {
		List<PMTuple> fileList = FilesTestHelper.randomFiles(NUM_FILES);
		List<String> args = new ArrayList<String>();
		args.add("edit");
		args.add("doc.title=doc.title");
		args.add("basic.creatorTool=basic.creatorTool");
		args.add("dc.languages=en");
		args.add("dc.languages=pl");

		for (PMTuple t : fileList) {
			args.add(t.file.getAbsolutePath());
		}

		CommandLine c = CommandLine.parse(args);
		PDFMetadataEditBatch batch = new PDFMetadataEditBatch(c.params);
		batch.runCommand(c.command, FileList.fileList(c.fileList), null,  new ActionStatus() {
			public void showStatus(String filename, String message) {
			}

			public void showError(String filename, Throwable error) {
				System.out.println(error);
				fail(error);
			}

		});

		for (PMTuple t : fileList) {
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			assertEquals("doc.title", loaded.doc.title);
			assertEquals("basic.creatorTool", loaded.basic.creatorTool );
			assertEquals(Arrays.asList("en","pl"), loaded.dc.languages );
			MetadataInfo check = t.md.clone();
			check.doc.title = "doc.title";
			check.basic.creatorTool = "basic.creatorTool";
			check.dc.languages=Arrays.asList("en","pl");
			// System.out.println(pdf.getAbsolutePath());
			FilesTestHelper.assertEqualsAllExceptFileProps(check, loaded, "Edited metadata differs");
		}
	}

	@Test
	public void testFromCSV() throws FileNotFoundException, IOException, Exception {
		List<PMTuple> fileList = FilesTestHelper.randomFiles(NUM_FILES);
		ArrayList<String> csvLines = new ArrayList<String>();
		csvLines.add("file.fullPath,doc.author,dc.title");
		for(PMTuple t: fileList){
			csvLines.add(t.file.getAbsolutePath() + ",AUTHOR-AUTHOR,\"TITLE,TITLE\"");
		}

		File csvFile = FilesTestHelper.csvFile(csvLines);
		List<String> args = new ArrayList<String>();
		args.add("fromcsv");


		args.add(csvFile.getAbsolutePath());

		CommandLine c = CommandLine.parse(args);
		PDFMetadataEditBatch batch =new PDFMetadataEditBatch(c.params);
		batch.runCommand(c.command, FileList.fileList(c.fileList), null, new ActionStatus(){
			public void showStatus(String filename, String message) {
			}

			public void showError(String filename, Throwable error) {
				System.out.println(error);
				fail(error);
			}

		});
		MetadataInfo empty = new MetadataInfo();
		for(PMTuple t: fileList){
			MetadataInfo loaded = FilesTestHelper.load(t.file);

			//System.out.println(pdf.getAbsolutePath());
			//assertTrue(t.md.isEquivalent(loaded));
			assertEquals(loaded.doc.author,"AUTHOR-AUTHOR");
			assertEquals(loaded.dc.title,"TITLE,TITLE");
		}
	}



}
