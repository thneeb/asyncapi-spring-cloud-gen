package de.neebs;

import de.neebs.asyncapi.Components;
import de.neebs.asyncapi.Definition;
import de.neebs.asyncapi.Message;

import java.util.Map;

public class ListenerGenerator {
    private final GeneratorConfig config;

    private final FileUtils fileUtils;

    private final AsyncApi2Java asyncApi2Java;

    public ListenerGenerator(GeneratorConfig config, FileUtils fileUtils, AsyncApi2Java asyncApi2Java) {
        this.config = config;
        this.fileUtils = fileUtils;
        this.asyncApi2Java = asyncApi2Java;
    }

    public void generateListener(Components components) {
        for (Map.Entry<String, Message> entry : components.getMessages().entrySet()) {
            generateReceiver(entry.getKey(), entry.getValue(), components.getSchemas());
        }
    }

    private void generateReceiver(String name, Message message, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(config.getApiPackage()).append(";");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("import ").append(config.getModelPackage()).append(".*;");
        sb.append(System.lineSeparator());
        sb.append("import org.apache.kafka.clients.consumer.ConsumerRecord;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.beans.BeanWrapper;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.beans.BeanWrapperImpl;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.core.convert.TypeDescriptor;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.kafka.annotation.KafkaListener;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.beans.factory.annotation.Autowired;");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("public interface ").append(name).append("Receiver {");
        sb.append(System.lineSeparator());
        Definition payload = asyncApi2Java.dereferenceDefinition(message.getPayload(), schemas);
        for (Definition definition : payload.getOneOf()) {
            sb.append(generateOnCallback(message, definition, schemas));
        }
        String masterClass = asyncApi2Java.convertDataType(message.getPayload(), true);
        Definition masterDefinition = schemas.get(masterClass);
        String discriminator = masterDefinition.getDiscriminator();
        String keyClass = asyncApi2Java.extractKeyDataType(message);
        sb.append("\tdefault void processEvent(");
        sb.append("ConsumerRecord<").append(keyClass).append(", ").append(message.getPayload().getReference().substring(message.getPayload().getReference().lastIndexOf("/") + 1)).append("> record) {");
        sb.append(System.lineSeparator());
        sb.append("\t\tString ").append(discriminator).append(" = record.value().get").append(discriminator.substring(0, 1).toUpperCase()).append(discriminator.substring(1)).append("();");
        sb.append(System.lineSeparator());
        sb.append("\t\tBeanWrapper wrapper = new BeanWrapperImpl(record.value());");
        sb.append(System.lineSeparator());
        sb.append("\t\tObject obj = wrapper.getPropertyValue(").append(discriminator).append(");");
        sb.append(System.lineSeparator());
        sb.append("\t\tTypeDescriptor descriptor = wrapper.getPropertyTypeDescriptor(").append(discriminator).append(");");
        sb.append(System.lineSeparator());
        sb.append("\t\tif (descriptor != null) {");
        sb.append(System.lineSeparator());
        for (Definition definition : payload.getOneOf()) {
            sb.append(generateCallbackCall(message, definition, schemas));
            sb.append(System.lineSeparator());
        }
        sb.append("\t\t}\n");
        sb.append(System.lineSeparator());
        sb.append("\t}\n");
        sb.append(System.lineSeparator());
        sb.append("}");
        sb.append(System.lineSeparator());
//        System.out.println(new String(sb));
        fileUtils.writeJavaFile(name + "Receiver", config.getSourceFolder(), config.getApiPackage(), new String(sb));
    }

    private String generateCallbackCall(Message message, Definition definition, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        String payloadClass = asyncApi2Java.convertDataType(definition, true);
        sb.append("\t\t\tif (\"").append(config.getModelPackage()).append(".").append(payloadClass).append("\".equals(descriptor.getName())) {");
        sb.append(System.lineSeparator());
        sb.append("\t\t\t\ton").append(payloadClass).append("((").append(payloadClass).append(") obj);");
        sb.append(System.lineSeparator());
        sb.append("\t\t\t}");
        sb.append(System.lineSeparator());
        return new String(sb);
    }

    private String generateOnCallback(Message message, Definition payload, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        String payloadClass = asyncApi2Java.convertDataType(payload == null ? message.getPayload() : payload, true);
        String payloadAttribute = payloadClass.substring(0, 1).toLowerCase() + payloadClass.substring(1);
        sb.append("\tpublic void on").append(payloadClass).append("(")
                .append(payloadClass).append(" ").append(payloadAttribute)
                .append(");");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        return new String(sb);
    }
}
