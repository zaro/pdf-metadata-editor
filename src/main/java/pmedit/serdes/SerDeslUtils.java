package pmedit.serdes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import pmedit.ext.PmeExtension;

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

    public static List<String> stringListFromJSON(String jsonString) {
        try {
            var mapper = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build());
            return  mapper.readerForListOf(String.class).readValue(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    protected static ObjectMapper yamlMapper(){
        ObjectMapper mapper =  new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Register the custom serializer and deserializer
        SimpleModule module = new SimpleModule();
        PmeExtension.get().initSerializer(module);
        mapper.registerModule(module);

        return mapper;
    }

    public static String toYAML(boolean pretty, Object yamlObject) {
        try {
            return yamlMapper().writeValueAsString(yamlObject);
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

    public static void toYamlFile(File file, Object yamlObject){
        try {
            yamlMapper().writeValue(file, yamlObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object fromYamlFile(File file, Class<?> clazz){
        try {
            return yamlMapper().readValue(file, clazz);
        } catch (IOException e) {
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
