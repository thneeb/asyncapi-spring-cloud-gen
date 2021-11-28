package de.neebs;

import de.neebs.asyncapi.Components;
import de.neebs.asyncapi.Definition;
import de.neebs.asyncapi.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AvroGenerator {
    private final GeneratorConfig config;

    private final FileUtils fileUtils;

    private final Set<String> PRIMITIVES = Set.of("string", "int", "long", "float", "double", "boolean");

    public AvroGenerator(GeneratorConfig config, FileUtils fileUtils) {
        this.config = config;
        this.fileUtils = fileUtils;
    }

    public void generateAvroFile(Components components) {
        for (Map.Entry<String, Message> entry : components.getMessages().entrySet()) {
            generateAvroFile(entry.getKey(), entry.getValue(), components.getSchemas());
        }
    }

    private void generateAvroFile(String className, Message message, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(System.lineSeparator());
        sb.append("\t\"namespace\": \"").append(config.getModelPackage()).append("\",");
        sb.append((System.lineSeparator()));
        sb.append("\t\"type\": \"record\",");
        sb.append((System.lineSeparator()));
        sb.append("\t\"name\": \"").append(className).append("\",");
        sb.append((System.lineSeparator()));
        sb.append("\t\"fields\": [");
        sb.append((System.lineSeparator()));
        sb.append(generateAttribute("header", className, message.getHeaders(), schemas, 2));
        sb.append(",");
        sb.append((System.lineSeparator()));
        sb.append(generateAttribute("payload", className, message.getPayload(), schemas, 2));
        sb.append((System.lineSeparator()));
        sb.append("\t]");
        sb.append((System.lineSeparator()));
        sb.append("}");
        sb.append((System.lineSeparator()));
        fileUtils.writeAvroFile(config.getSourceFolder(), fileUtils.camelToKebap(className), new String(sb));
    }

    private String generateAttribute(String fieldName, String className, Definition definition, Map<String, Definition> schemas, int indent) {
        String spacer = "\t".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(spacer).append("{");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"name\": \"").append(fieldName).append("\",");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"type\": [");
        sb.append(System.lineSeparator());
        sb.append(generateDataType("object".equalsIgnoreCase(definition.getType()) ? className : definition.getType(), definition, schemas, indent));
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t]");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("}");
        return new String(sb);
    }

    private String generateComplexDataType(String className, Definition definition, Map<String, Definition> schemas, int indent) {
        String spacer = "\t".repeat(indent);
        List<String> fields = new ArrayList<>();
        if (definition.getProperties() != null && !definition.getProperties().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Definition> entry : definition.getProperties().entrySet()) {
                sb.append(generateAttribute(entry.getKey(), className, entry.getValue(), schemas, indent + 2));
                sb.append(",");
                sb.append(System.lineSeparator());
            }
            fields.add(sb.substring(0, sb.lastIndexOf(",")));
        }

        if (definition.getOneOf() != null && !definition.getOneOf().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Definition def : definition.getOneOf()) {
                String className2 = def.getReference().substring(def.getReference().lastIndexOf("/") + 1);
                String attributeName = className2.substring(0, 1).toLowerCase() + className2.substring(1);
                Definition def2 = schemas.get(className2);
                sb.append(generateAttribute(attributeName, className2, def2, schemas, indent + 2));
                sb.append(",");
                sb.append(System.lineSeparator());
            }
            fields.add(sb.substring(0, sb.lastIndexOf(",")));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(spacer).append("{");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"name\": \"").append(className).append("\",");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"type\": \"record\",");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"fields\": [");
        sb.append(System.lineSeparator());
        if (definition.getReference() != null) {
            String className2 = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
            Definition def = schemas.get(className2);
            sb.append(generateAttribute(className2, className, def, schemas, indent + 2));
            sb.append(",");
            sb.append(System.lineSeparator());
        } else {
            sb.append(String.join("," + System.lineSeparator(), fields));
            sb.append(System.lineSeparator());
        }
        sb.append(spacer).append("\t]");
        sb.append((System.lineSeparator()));
        sb.append(spacer).append("}");
        return new String(sb);
    }

    private String generateDataType(String className, Definition definition, Map<String, Definition> schemas, int indent) {
        StringBuilder sb = new StringBuilder();
        if (definition.getReference() != null) {
            className = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
            Definition def = schemas.get(className);
            sb.append(generateDataType(className, def, schemas, indent));
        } else if (PRIMITIVES.contains(className)) {
            String spacer = "\t".repeat(indent);
            sb.append(spacer).append("\t\t\"").append(className).append("\"");
        } else if (definition.getEnumeration() != null && !definition.getEnumeration().isEmpty()) {
            sb.append(generateEnumDataType(className, definition, indent + 2));
        } else {
            sb.append(generateComplexDataType(className, definition, schemas, indent + 2));
        }
        return new String(sb);
    }

    private String generateEnumDataType(String className, Definition definition, int indent) {
        String spacer = "\t".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(spacer).append("{");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"name\": \"").append(className).append("\",");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"type\": \"enum\",");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t\"symbols\": [");
        sb.append(System.lineSeparator());
        List<String> list = definition.getEnumeration().stream().map(f -> spacer + "\t\t\"" + f + "\"").collect(Collectors.toList());
        sb.append(String.join("," + System.lineSeparator(), list));
        sb.append(System.lineSeparator());
        sb.append(spacer).append("\t]");
        sb.append(System.lineSeparator());
        sb.append(spacer).append("}");
        sb.append(System.lineSeparator());
        return new String(sb);
    }
}
