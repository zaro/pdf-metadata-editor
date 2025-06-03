package pmedit.serdes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pmedit.ext.PmeExtension;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerDeslUtils {
    static final Logger LOG = LoggerFactory.getLogger(SerDeslUtils.class);

    protected static ObjectMapper jsonMapper(){
        var mapper = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Register the custom serializer and deserializer
        SimpleModule module = new SimpleModule();
        PmeExtension.get().initSerializer(module);
        mapper.registerModule(module);

        return mapper;
    }

    public static String toJSON(boolean pretty, Object jsonObject) {
        try {
            return jsonMapper().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            LOG.error("toJSON", e);
            throw new RuntimeException(e);
        }
    }

    public static void toJSONFile(File file, Object jsonObject) {
        try {
            jsonMapper().writeValue(file, jsonObject);
        } catch (IOException e) {
            LOG.error("toJSONFile", e);
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Object> fromJSON(String jsonString) {
        try {
            return jsonMapper().readValue(jsonString, HashMap.class);
        } catch (JsonProcessingException e) {
            LOG.error("fromJSON", e);
            throw new RuntimeException(e);
        }
    }

    public static List<HashMap<String, Object>> listFromJSON(String jsonString) {
        try {
            return  jsonMapper().readerForListOf(HashMap.class).readValue(jsonString);
        } catch (JsonProcessingException e) {
            LOG.error("listFromJSON", e);
            throw new RuntimeException(e);
        }

    }

    public static List<String> stringListFromJSON(String jsonString) {
        try {
            return jsonMapper().readerForListOf(String.class).readValue(jsonString);
        } catch (JsonProcessingException e) {
            LOG.error("stringListFromJSON", e);
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> fromJSONFileAsList(File file) {
        try {
            var mapper = jsonMapper();
            JsonNode rootNode = mapper.readTree(file);
            if (rootNode.isArray()) {
                return mapper.convertValue(rootNode, List.class);
            } else {
                Map<String, Object> v = mapper.convertValue(rootNode, Map.class);
                return List.of(v);
            }
        } catch (IOException e) {
            LOG.error("fromJSONFileAsList", e);
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

    public static String toYAML(Object yamlObject) {
        try {
            return yamlMapper().writeValueAsString(yamlObject);
        } catch (JsonProcessingException e) {
            LOG.error("toYAML", e);
            throw new RuntimeException(e);
        }
    }

    public static Object fromYAML(String yamlString) {
        try {
            return yamlMapper().readValue(yamlString, HashMap.class);
        } catch (JsonProcessingException e) {
            LOG.error("fromYAML", e);
            throw new RuntimeException(e);
        }

    }

    public static void toYamlFile(File file, Object yamlObject){
        try {
            yamlMapper().writeValue(file, yamlObject);
        } catch (IOException e) {
            LOG.error("toYamlFile", e);
            throw new RuntimeException(e);
        }
    }

    public static Object fromYamlFile(File file, Class<?> clazz){
        try {
            return yamlMapper().readValue(file, clazz);
        } catch (IOException e) {
            LOG.error("fromYamlFile", e);
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> fromYAMLFileAsList(File file) {
        try {
            var mapper = yamlMapper();
            JsonNode rootNode = mapper.readTree(file);
            if (rootNode.isArray()) {
                return mapper.convertValue(rootNode, List.class);
            } else {
                Map<String, Object> v = mapper.convertValue(rootNode, Map.class);
                return List.of(v);
            }
        } catch (IOException e) {
            LOG.error("fromYAMLFileAsList", e);
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
            LOG.error("objectToFile", e);
            return false;
        }
    }

    public static Object objectFromFile(String file){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return  objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("objectFromFile", e);
            return null;
        }
    }
}
