package pmedit;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileList {

	static class Finder extends SimpleFileVisitor<Path> {
		private final PathMatcher matcher;
		List<File> fileList = new ArrayList<File>();

		Finder(String pattern) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		// Compares the glob pattern against
		// the file or directory name.
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				fileList.add(file.toFile());
			}
		}

		// Invoke the pattern matching
		// method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		// Invoke the pattern matching
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return FileVisitResult.CONTINUE;
		}
	}

	static Pattern isGlob = Pattern.compile("[\\[\\]\\{\\}\\*\\?]");
	public static List<File> fileList(List<String> fileNames) {
		ArrayList<File> rval = new ArrayList<File>();

		for (String fileName : fileNames) {
			File file = new File(fileName);

			Matcher m = isGlob.matcher(file.getName());
			if(m.find()){
				String dir = file.getParent();
				if(dir == null){
					dir= ".";
				}
		        Finder finder = new Finder(file.getName());
				try {
					Files.walkFileTree(new File(dir).toPath(), finder);
				} catch (IOException e) {
					System.err.println(e);
				}
				rval.addAll(finder.fileList);
			} else {
				rval.add(file);
			}
		}
		return rval;
	}

	public static List<File> fileList(String[] fileNames) {
		return fileList(Arrays.asList(fileNames));
	}

}
