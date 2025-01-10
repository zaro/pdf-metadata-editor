package pmedit;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class SerDeslUtils {
    public static String toJSON(boolean pretty, Object jsonObject) {
        try {
            var mapper = new ObjectMapper(new JsonFactory())
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Object> fromJSON(String jsonString) {
        try {
            var mapper = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build());
            return mapper.readValue(jsonString, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<HashMap<String, Object>> listFromJSON(String jsonString) {
        try {
            var mapper = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build());
            return  mapper.readerForListOf(HashMap.class).readValue(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }



    public static String toYAML(boolean pretty, Object yamlObject) {
        try {
            var mapper = new ObjectMapper(new YAMLFactory()
                            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            return mapper.writeValueAsString(yamlObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object fromYAML(String yamlString) {
        try {
            var mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(yamlString, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean objectToFile(Object o, String file ) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(o);
            return true;
        } catch ( IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object objectFromFile(String file){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return  objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
