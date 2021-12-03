package de.neebs;

import de.neebs.asyncapi.Components;
import de.neebs.asyncapi.Definition;
import de.neebs.asyncapi.Message;

import java.util.*;
import java.util.stream.Collectors;

public class AvroGenerator {
    private final GeneratorConfig config;

    private final FileUtils fileUtils;

    private final Set<String> PRIMITIVES = Set.of("string", "integer", "long", "float", "double", "boolean");

    public AvroGenerator(GeneratorConfig config, FileUtils fileUtils) {
        this.config = config;
        this.fileUtils = fileUtils;
    }

    public void generateAvro(Components components) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Definition> entry : components.getSchemas().entrySet()) {
            map.put(entry.getKey(), generateModelClass(entry.getKey(), entry.getValue(), components.getSchemas()));
        }
        Map<String, Set<String>> dependencies = createDependencyList(components, map.keySet());
        List<String> list = orderByDependencies(map, dependencies);
        String s = "["+ System.lineSeparator() + String.join("," + System.lineSeparator(), list) + System.lineSeparator() + "]";
        fileUtils.writeAvroFile(config.getSourceFolder(), "canonical", s);
    }

    private List<String> orderByDependencies(Map<String, String> map, Map<String, Set<String>> dependencies) {
        List<String> list = new ArrayList<>();
        Set<String> unused = map.keySet();
        while (!unused.isEmpty()) {
            Set<String> set = new HashSet<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Set<String> unresolved = new HashSet<>(dependencies.get(entry.getKey()));
                unresolved.retainAll(unused);
                if (unresolved.isEmpty()) {
                    list.add(entry.getValue());
                    set.add(entry.getKey());
                }
            }
            unused.removeAll(set);
        }
        return list;
    }

    private Map<String, Set<String>> createDependencyList(Components components, Set<String> availableObjects) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Map.Entry<String, Message> entry : components.getMessages().entrySet()) {
            Set<String> list = new HashSet<>();
            list.addAll(createDependencyList(entry.getValue().getHeaders(), availableObjects));
            list.addAll(createDependencyList(entry.getValue().getPayload(), availableObjects));
            map.put(entry.getKey(), list);
        }
        for (Map.Entry<String, Definition> entry : components.getSchemas().entrySet()) {
            Set<String> list = createDependencyList(entry.getValue(), availableObjects);
            map.put(entry.getKey(), list);
        }
        return map;
    }

    private Set<String> createDependencyList(Definition definition, Set<String> availableObjects) {
        Set<String> list = new HashSet<>();
        if (definition.getReference() != null) {
            list.add(definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1));
        }
        if (definition.getProperties() != null) {
            for (Definition def : definition.getProperties().values()) {
                list.addAll(createDependencyList(def, availableObjects));
            }
        }
        if (definition.getType() != null && availableObjects.contains((definition.getType()))) {
            list.add(definition.getType());
        }
        if (definition.getOneOf() != null) {
            for (Definition def : definition.getOneOf()) {
                list.addAll(createDependencyList(def, availableObjects));
            }
        }
        if (definition.getAllOf() != null) {
            for (Definition def : definition.getAllOf()) {
                list.addAll(createDependencyList(def, availableObjects));
            }
        }
        if (definition.getAnyOf() != null) {
            for (Definition def : definition.getAnyOf()) {
                list.addAll(createDependencyList(def, availableObjects));
            }
        }
        return list;
    }

    private String generateModelClass(String className, Definition definition, Map<String, Definition> schemas) {
        List<String> fields = new ArrayList<>();
        if (definition.getProperties() != null && !definition.getProperties().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Definition> entry : definition.getProperties().entrySet()) {
                boolean required = definition.getRequired() != null && definition.getRequired().contains(entry.getKey());
                sb.append(generateAttribute(entry.getKey(), className, entry.getValue(), required));
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
                sb.append(generateAttribute(attributeName, className2, def2, false));
                sb.append(",");
                sb.append(System.lineSeparator());
            }
            fields.add(sb.substring(0, sb.lastIndexOf(",")));
        }

        if (definition.getAllOf() != null && !definition.getAllOf().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Definition def : definition.getAllOf()) {
                String className2 = def.getReference().substring(def.getReference().lastIndexOf("/") + 1);
                Definition def2 = schemas.get(className2);
                String attributeName = className2.substring(0, 1).toLowerCase() + className2.substring(1);
                sb.append(generateAttribute(attributeName, className2, def2, true));
                sb.append(",");
                sb.append(System.lineSeparator());
            }
            fields.add(sb.substring(0, sb.lastIndexOf(",")));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(System.lineSeparator());
        sb.append("\t\"namespace\": \"").append(config.getModelPackage()).append("\",");
        sb.append(System.lineSeparator());
        sb.append("\t\"name\": \"").append(className).append("\",");
        sb.append(System.lineSeparator());
        if (fields.isEmpty() && definition.getEnumeration() != null && !definition.getEnumeration().isEmpty()) {
            sb.append("\t\"type\": \"enum\",");
            sb.append(System.lineSeparator());
            sb.append("\t\"symbols\": [");
            sb.append(System.lineSeparator());
            List<String> list = definition.getEnumeration().stream().map(f -> "\t\t\"" + f + "\"").collect(Collectors.toList());
            sb.append(String.join("," + System.lineSeparator(), list));
            sb.append(System.lineSeparator());
            sb.append("\t]");
        } else {
            sb.append("\t\"type\": \"record\",");
            sb.append(System.lineSeparator());
            sb.append("\t\"fields\": [");
            sb.append(System.lineSeparator());
            if (definition.getReference() != null) {
                String className2 = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
                Definition def = schemas.get(className2);
                sb.append(generateAttribute(className2, className, def, false));
                sb.append(",");
                sb.append(System.lineSeparator());
            } else {
                sb.append(String.join("," + System.lineSeparator(), fields));
                sb.append(System.lineSeparator());
            }
            sb.append("\t]");
        }
        sb.append((System.lineSeparator()));
        sb.append("}");
        return new String(sb);
    }

    private String generateAttribute(String fieldName, String className, Definition definition, boolean required) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\t{");
        sb.append(System.lineSeparator());
        sb.append("\t\t\t\"name\": \"").append(fieldName).append("\",");
        sb.append(System.lineSeparator());
        String dataType = generateDataType("object".equalsIgnoreCase(definition.getType()) ? className : definition.getType(), definition);
        if (required) {
            sb.append("\t\t\t\"type\": ").append(dataType);
        } else {
            sb.append("\t\t\t\"type\": [ \"null\", ").append(dataType).append(" ], \"default\": null");
        }
        sb.append(System.lineSeparator());
        sb.append("\t\t}");
        return new String(sb);
    }

    private String generateDataType(String className, Definition definition) {
        StringBuilder sb = new StringBuilder();
        if (definition.getReference() != null) {
            className = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
            sb.append("\"").append(className).append("\"");
        } else if ("array".equalsIgnoreCase(className)) {
            definition = definition.getItems();
            String dataType = generateDataType("object".equalsIgnoreCase(definition.getType()) ? className : definition.getType(), definition);
            sb.append("{ \"type\": \"array\", \"items\": ").append(dataType).append("}");
        } else if (PRIMITIVES.contains(className)) {
            sb.append("\"").append(convertDataTypeToAvro(className)).append("\"");
        } else {
            sb.append("\"").append(className).append("\"");
        }
        return new String(sb);
    }

    private String convertDataTypeToAvro(String className) {
        if ("integer".equalsIgnoreCase(className)) {
            return "int";
        }
        return className;
    }
}
