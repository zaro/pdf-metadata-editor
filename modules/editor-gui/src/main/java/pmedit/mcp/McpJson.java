package pmedit.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pmedit.MetadataInfo;
import pmedit.annotations.FieldDataType.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON helpers for the MCP server.
 *
 * <p>The wire mapper is deliberately compact: MCP stdio frames are
 * newline-delimited, so pretty-printed JSON must never be written to the
 * transport stream. A separate pretty mapper is used only for the human
 * readable text content inside tool results.</p>
 */
public final class McpJson {

    private static final ObjectMapper WIRE_MAPPER = new ObjectMapper();
    private static final ObjectMapper PRETTY_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private McpJson() {
    }

    public static ObjectNode object() {
        return WIRE_MAPPER.createObjectNode();
    }

    public static ArrayNode array() {
        return WIRE_MAPPER.createArrayNode();
    }

    public static JsonNode parse(String json) throws JsonProcessingException {
        return WIRE_MAPPER.readTree(json);
    }

    public static String write(JsonNode node) {
        try {
            return WIRE_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    public static String pretty(Object value) {
        try {
            return PRETTY_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON", e);
        }
    }

    /**
     * Convert a JSON value to the metadata value type expected by
     * {@link MetadataInfo#set(String, Object)}.
     */
    public static Object toMetadataValue(MetadataInfo.FieldDescription fd, JsonNode node) throws McpToolException {
        if (node == null || node.isNull()) {
            return null;
        }

        Object value;
        try {
            value = WIRE_MAPPER.convertValue(node, Object.class);
        } catch (IllegalArgumentException e) {
            throw new McpToolException("Invalid value for field " + fd.name + ": " + e.getMessage(), e);
        }

        if (value instanceof Map) {
            throw new McpToolException("Field " + fd.name + " does not accept a JSON object");
        }
        if (!fd.isList && value instanceof List) {
            throw new McpToolException("Field " + fd.name + " does not accept a JSON array");
        }
        if (fd.isList && !(value instanceof List)) {
            value = List.of(value);
        }

        if (fd.type == FieldType.StringField
                || fd.type == FieldType.TextField
                || fd.type == FieldType.EnumField) {
            if (fd.isList) {
                List<String> strings = new ArrayList<>();
                for (Object item : (List<?>) value) {
                    if (item instanceof Map || item instanceof List) {
                        throw new McpToolException("Field " + fd.name + " accepts only scalar values");
                    }
                    strings.add(item == null ? null : String.valueOf(item));
                }
                value = strings;
            } else {
                value = String.valueOf(value);
            }
        }

        if (value instanceof String stringValue
                && fd.type != null
                && fd.type != FieldType.StringField
                && fd.type != FieldType.TextField
                && fd.type != FieldType.EnumField) {
            try {
                value = fd.makeValueFromString(stringValue);
            } catch (RuntimeException e) {
                throw new McpToolException("Invalid value for field " + fd.name + ": " + e.getMessage(), e);
            }
        }

        Object processed;
        try {
            processed = fd.postProcessDeserializedValue(value);
        } catch (RuntimeException e) {
            throw new McpToolException("Invalid value for field " + fd.name + ": " + e.getMessage(), e);
        }

        if (fd.type == FieldType.EnumField && processed != null) {
            try {
                fd.enumValueFromString(String.valueOf(processed));
            } catch (IllegalArgumentException e) {
                throw new McpToolException(
                        "Invalid value for field " + fd.name + ": '" + processed + "'. Allowed values: " + enumValues(fd));
            }
        }

        return processed;
    }

    private static String enumValues(MetadataInfo.FieldDescription fd) {
        String[] values = fd.getEnumValuesAsStrings();
        return values == null ? "<none>" : String.join(", ", values);
    }

    // ---------------------------------------------------------------------
    // JSON Schema helpers
    // ---------------------------------------------------------------------

    public static ObjectNode objectSchema() {
        ObjectNode schema = object();
        schema.put("type", "object");
        schema.set("properties", object());
        schema.set("required", array());
        schema.put("additionalProperties", false);
        return schema;
    }

    public static ObjectNode outputObjectSchema() {
        ObjectNode schema = objectSchema();
        schema.put("additionalProperties", true);
        return schema;
    }

    public static ObjectNode stringProperty(String description) {
        ObjectNode property = object();
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    public static ObjectNode booleanProperty(String description) {
        ObjectNode property = object();
        property.put("type", "boolean");
        property.put("description", description);
        return property;
    }

    public static ObjectNode stringArrayProperty(String description) {
        ObjectNode property = object();
        property.put("type", "array");
        ObjectNode items = object();
        items.put("type", "string");
        property.set("items", items);
        property.put("description", description);
        return property;
    }

    public static ObjectNode objectProperty(String description) {
        ObjectNode property = object();
        property.put("type", "object");
        property.put("additionalProperties", true);
        property.put("description", description);
        return property;
    }

    public static ObjectNode objectMapProperty(String description) {
        ObjectNode property = object();
        property.put("type", "object");
        ObjectNode additional = object();
        additional.put("type", "string");
        property.set("additionalProperties", additional);
        property.put("description", description);
        return property;
    }

    public static ObjectNode enumProperty(String description, String... values) {
        ObjectNode property = object();
        property.put("type", "string");
        ArrayNode allowed = array();
        for (String value : values) {
            allowed.add(value);
        }
        property.set("enum", allowed);
        property.put("description", description);
        return property;
    }

    public static ObjectNode arrayProperty(String description, ObjectNode items) {
        ObjectNode property = object();
        property.put("type", "array");
        property.set("items", items);
        property.put("description", description);
        return property;
    }

    public static void addProperty(ObjectNode schema, String name, ObjectNode property, boolean required) {
        ObjectNode properties = (ObjectNode) schema.get("properties");
        properties.set(name, property);
        if (required) {
            ((ArrayNode) schema.get("required")).add(name);
        }
    }

    public static ObjectNode annotations(boolean readOnly, boolean destructive, boolean idempotent) {
        ObjectNode annotations = object();
        annotations.put("readOnlyHint", readOnly);
        annotations.put("destructiveHint", destructive);
        annotations.put("idempotentHint", idempotent);
        annotations.put("openWorldHint", false);
        return annotations;
    }
}
