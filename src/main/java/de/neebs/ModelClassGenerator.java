package de.neebs;

import de.neebs.asyncapi.Definition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ModelClassGenerator {
    private final GeneratorConfig config;

    private final FileUtils fileUtils;

    public ModelClassGenerator(GeneratorConfig config, FileUtils fileUtils) {
        this.config = config;
        this.fileUtils = fileUtils;
    }

    public void generate(Map<String, Definition> schemas) {
        for (Map.Entry<String, Definition> entry : schemas.entrySet()) {
            generate(entry.getKey(), entry.getValue());
        }
    }

    private void generate(String className, Definition definition) {
        if (definition.getEnumeration() != null) {
            generateEnum(className, definition.getEnumeration());
        } else {
            generateClass(className, definition);
        }
    }

    private void generateEnum(String className, List<String> enumeration) {
        StringBuilder sb = new StringBuilder();
        sb.append(generatePackage());
        sb.append("public enum ").append(className).append(" {");
        sb.append(System.lineSeparator());
        sb.append(generateEnumeration(enumeration));
        if (isValueEnum(enumeration)) {
            sb.append(System.lineSeparator());
            sb.append("\tprivate String value;");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("\tprivate ").append(className).append("(String value) {");
            sb.append(System.lineSeparator());
            sb.append("\t\tthis.value = value;");
            sb.append(System.lineSeparator());
            sb.append("\t}");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("\tpublic String get").append(className).append("() {");
            sb.append(System.lineSeparator());
            sb.append("\t\treturn value;");
            sb.append(System.lineSeparator());
            sb.append("\t}");
            sb.append(System.lineSeparator());
        }
        sb.append("}");
        sb.append(System.lineSeparator());
        fileUtils.writeFile(className, config.getSourceFolder(), config.getModelPackage(), new String(sb));
    }

    private boolean isValueEnum(List<String> enumeration) {
        return !enumeration.stream().allMatch(f -> f.equals(f.toUpperCase()));
    }

    private String generateEnumeration(List<String> enumeration) {
        StringBuilder sb = new StringBuilder();
        for (String s : enumeration) {
            sb.append("\t");
            sb.append(s.toUpperCase());
            if (!s.equals(s.toUpperCase())) {
                sb.append("(\"");
                sb.append(s);
                sb.append("\")");
            }
            sb.append(",");
            sb.append(System.lineSeparator());
        }
        if (sb.length() > 1) {
            return sb.substring(0, sb.lastIndexOf(",")) + ";" + System.lineSeparator();
        } else {
            return System.lineSeparator();
        }
    }

    private void generateClass(String className, Definition definition) {
        StringBuilder sb = new StringBuilder();
        sb.append(generatePackage());
        sb.append("import lombok.Getter;");
        sb.append(System.lineSeparator());
        sb.append("import lombok.Setter;");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("@Getter");
        sb.append(System.lineSeparator());
        sb.append("@Setter");
        sb.append(System.lineSeparator());
        sb.append("public class ");
        sb.append(className);
        sb.append(" {");
        sb.append(System.lineSeparator());
        sb.append(generateAttributes(definition.getProperties(), definition.getRequired()));
        sb.append(generateOneOf(definition.getOneOf()));
        sb.append("}");
        sb.append(System.lineSeparator());
        fileUtils.writeFile(className, config.getSourceFolder(), config.getModelPackage(), new String(sb));
    }

    private String generateOneOf(List<Definition> oneOf) {
        StringBuilder sb = new StringBuilder();
        if (oneOf != null) {
            for (Definition definition : oneOf) {
                String className = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
                sb.append("\tprivate ");
                sb.append(className);
                sb.append(" ");
                sb.append(className.substring(0, 1).toLowerCase());
                sb.append(className.substring(1));
                sb.append(";");
                sb.append(System.lineSeparator());
            }
        }
        return new String(sb);
    }

    private String generatePackage() {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(config.getModelPackage()).append(";");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        return new String(sb);
    }

    private String generateAttributes(Map<String, Definition> properties, List<String> required) {
        StringBuilder sb = new StringBuilder();
        if (properties != null) {
            for (Map.Entry<String, Definition> entry : properties.entrySet()) {
                sb.append(generateAttribute(entry.getKey(), entry.getValue(), required != null && required.contains(entry.getKey())));
            }
        }
        return new String(sb);
    }

    private String generateAttribute(String attributeName, Definition definition, boolean required) {
        StringBuilder sb = new StringBuilder();
        sb.append("\tprivate ");
        sb.append(convertDataType(definition.getType(), definition.getReference(), required));
        sb.append(" ");
        sb.append(attributeName);
        sb.append(";");
        sb.append(System.lineSeparator());
        return new String(sb);
    }

    private String convertDataType(String type, String reference, boolean required) {
        if ("String".equalsIgnoreCase(type)) {
            return "String";
        } else if ("int".equalsIgnoreCase(type) || "Integer".equalsIgnoreCase(type)) {
            if (required) {
                return "int";
            } else {
                return "Integer";
            }
        } else if ("boolean".equalsIgnoreCase(type)) {
            if (required) {
                return "boolean";
            } else {
                return "Boolean";
            }
        } else if (type == null) {
            return reference.substring(reference.lastIndexOf("/") + 1);
        } else {
            return "Unknown type: " + type;
        }
    }
}
