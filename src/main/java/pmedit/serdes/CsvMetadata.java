package pmedit.serdes;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.CommandLine;
import pmedit.MetadataInfo;
import pmedit.OsCheck;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CsvMetadata {
    static Logger LOG = LoggerFactory.getLogger(CsvMetadata.class);

    public static List<MetadataInfo> readFile(File filename) throws IOException, CsvException {
        ArrayList<MetadataInfo> parsed = new ArrayList<MetadataInfo>();
        final CSVParser parser =
                new CSVParserBuilder()
                        .withEscapeChar(OsCheck.isWindows() ? '\0' : '\\')
                        .build();
        final CSVReader reader =
                new CSVReaderBuilder(new FileReader(filename))
                        .withCSVParser(parser)
                        .build();
        List<String[]> entries = reader.readAll();
        reader.close();
        String[] header = entries.remove(0);
        for (int i = 0; i < header.length; ++i) {
            header[i] = header[i].trim();
        }
        if (!Arrays.asList(header).contains("file.fullPath")) {
            RuntimeException e = new RuntimeException("The header must specify a 'file.fullPath' column");
            LOG.error("Invalid CSV header", e);
            throw e;
        }
        try {
            for (String[] row : entries) {
                MetadataInfo metadata = new MetadataInfo();
                metadata.setEnabled(false);
                for (int idx = 0; idx < row.length; ++idx) {
                    String id = header[idx];
                    if (CommandLine.validMdNames.contains(id)) {
                        String value = row[idx].trim();
                        metadata.setAppendFromString(id, value);
                        metadata.setEnabled(id, true);
                    }
                }
                parsed.add(metadata);
            }
        } catch (RuntimeException e){
            LOG.error("Error while parsing CSV data", e);
            throw e;
        }
        return parsed;
    }

    public static class Writer {
        CSVWriter writer;
        List<String> header;

        Writer(File filename) throws IOException {
            writer = new CSVWriter(new FileWriter(filename.toString()));
        }

        public void writeHeader(Map<String, Object> data) {
            Set<String> headerSet = data.keySet();

            final String prefix = "file.";

            // Split the list using Stream API
            List<String> withPrefix = headerSet.stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());

            List<String> withoutPrefix = headerSet.stream()
                    .filter(s -> !s.startsWith(prefix))
                    .collect(Collectors.toList());

            if(withPrefix.contains("file.fullPath")) {
                withPrefix.remove("file.fullPath");
            }

            withPrefix.add(0, "file.fullPath");

            header = new ArrayList<>();
            header.addAll(withPrefix);
            header.addAll(withoutPrefix);
            writer.writeNext(header.toArray(new String[0]));
        }

        public void writeNext(Map<String, Object> data){
            if(header == null){
                writeHeader(data);
            }
            LinkedHashMap<String, String> out = new LinkedHashMap<>();
            for(String h: header){
                Object o = data.get(h);
                out.put(h, o != null ? o.toString(): null);
            }
            writer.writeNext(out.values().toArray(new String[0]));
        }

        public void close() throws IOException {
            writer.close();
        }

    }

    public static Writer newWriter(File filename) throws IOException {
        return new Writer(filename);
    }

    public static  void writeFile(File filename, List<Map> data) {
        try {
            Writer writer = new Writer(filename);
            for (Map md : data) {
                writer.writeNext(md);
            }
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
