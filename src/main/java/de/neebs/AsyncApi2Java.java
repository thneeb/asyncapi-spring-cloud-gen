package de.neebs;

import de.neebs.asyncapi.Definition;
import de.neebs.asyncapi.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AsyncApi2Java {
    public String convertDataType(Definition definition, boolean required) {
        return convertDataType(definition.getType(), definition.getReference(), required);
    }

    public Definition dereferenceDefinition(Definition definition, Map<String, Definition> schemas) {
        if (definition.getReference() != null) {
            String className2 = definition.getReference().substring(definition.getReference().lastIndexOf("/") + 1);
            return dereferenceDefinition(schemas.get(className2), schemas);
        } else {
            return definition;
        }
    }

    public String convertDataType(String type, String reference, boolean required) {
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

    public String extractKeyDataType(Message message) {
        if (message.getBindings() != null && message.getBindings().getKafka() != null && message.getBindings().getKafka().getKey() != null) {
            return convertDataType(message.getBindings().getKafka().getKey(), true);
        } else {
            return "String";
        }
    }
}
