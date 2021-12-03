package de.neebs;

import de.neebs.asyncapi.Components;
import de.neebs.asyncapi.Definition;
import de.neebs.asyncapi.Message;

import java.util.Map;

public class SenderGenerator {
    private final GeneratorConfig config;

    private final FileUtils fileUtils;

    private final AsyncApi2Java asyncApi2Java;

    public SenderGenerator(GeneratorConfig config, FileUtils fileUtils, AsyncApi2Java asyncApi2Java) {
        this.config = config;
        this.fileUtils = fileUtils;
        this.asyncApi2Java = asyncApi2Java;
    }

    public void generateSender(Components components) {
        for (Map.Entry<String, Message> entry : components.getMessages().entrySet()) {
            generateSender(entry.getKey(), entry.getValue(), components.getSchemas());
        }
    }

    private void generateSender(String name, Message message, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(config.getApiPackage()).append(";");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("import ").append(config.getModelPackage()).append(".*;");
        sb.append(System.lineSeparator());
        sb.append("import org.springframework.kafka.core.KafkaTemplate;");
        sb.append(System.lineSeparator());
        sb.append("import org.apache.kafka.clients.producer.ProducerRecord;");
        sb.append(System.lineSeparator());
        if (config.isSpring()) {
            sb.append("import org.springframework.stereotype.Component;");
            sb.append(System.lineSeparator());
            sb.append("import org.springframework.beans.factory.annotation.Value;");
            sb.append(System.lineSeparator());
            sb.append("import org.springframework.beans.factory.annotation.Autowired;");
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        if (config.isSpring()) {
            sb.append("@Component");
            sb.append(System.lineSeparator());
        }
        sb.append("public class ").append(name).append("Sender {");
        sb.append(System.lineSeparator());
        sb.append("\tprivate final KafkaTemplate kafkaTemplate;");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("\t@Value(\"${topic.mapping.pinMessage}\")");
        sb.append(System.lineSeparator());
        sb.append("\tprivate String topic;");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        if (config.isSpring()) {
            sb.append("\t@Autowired");
            sb.append(System.lineSeparator());
        }
        sb.append("\tpublic ").append(name).append("Sender").append("(KafkaTemplate kafkaTemplate) {");
        sb.append(System.lineSeparator());
        sb.append("\t\tthis.kafkaTemplate = kafkaTemplate;");
        sb.append(System.lineSeparator());
        sb.append("\t}");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(sendMessage(message, null, schemas));
        sb.append(System.lineSeparator());
        Definition payload = asyncApi2Java.dereferenceDefinition(message.getPayload(), schemas);
        for (Definition definition : payload.getOneOf()) {
            sb.append(sendMessage(message, definition, schemas));
            sb.append(System.lineSeparator());
        }
        sb.append("}");
        sb.append(System.lineSeparator());
//        System.out.println(new String(sb));
        fileUtils.writeJavaFile(name + "Sender", config.getSourceFolder(), config.getApiPackage(), new String(sb));
    }

    private String sendMessage(Message message, Definition payload, Map<String, Definition> schemas) {
        StringBuilder sb = new StringBuilder();
        String payloadClass = asyncApi2Java.convertDataType(payload == null ? message.getPayload() : payload, true);
        String payloadAttribute = payloadClass.substring(0, 1).toLowerCase() + payloadClass.substring(1);
        String keyClass = asyncApi2Java.extractKeyDataType(message);
        sb.append("\tpublic void send").append(payload == null ? "Message" : asyncApi2Java.convertDataType(payload, true)).append("(")
                .append(keyClass).append(" key, ")
                .append(payloadClass).append(" ").append(payloadAttribute).append(", ")
                .append(asyncApi2Java.convertDataType(message.getHeaders(), true)).append(" header")
                .append(") {");
        sb.append(System.lineSeparator());
        String masterClass = asyncApi2Java.convertDataType(message.getPayload(), true);
        String masterAttribute = masterClass.substring(0, 1).toLowerCase() + masterClass.substring(1);
        if (payload != null) {
            Definition masterDefinition = schemas.get(masterClass);
            String discriminator = masterDefinition.getDiscriminator();
            sb.append("\t\t").append(masterClass).append(" ").append(masterAttribute).append(" = ")
                    .append(masterClass).append(".newBuilder().")
                    .append("set").append(payloadClass).append("(").append(payloadAttribute).append(").")
                    .append("set").append(discriminator.substring(0,1).toUpperCase()).append(discriminator.substring(1)).append("(\"").append(payloadClass).append("\").")
                    .append("build();");
            sb.append(System.lineSeparator());
        }
        sb.append("\t\tProducerRecord<").append(keyClass).append(", ").append(masterClass).append("> record = new ProducerRecord<>(topic, key, ").append(masterAttribute).append(");");
        sb.append(System.lineSeparator());
//        sb.append("\t\tif (header.getCorrelationId() != null) {");
//        sb.append(System.lineSeparator());
//        sb.append("\t\t\trecord.headers().add(\"correlationId\", header.getCorrelationId().getBytes());");
//        sb.append(System.lineSeparator());
//        sb.append("\t\t}");
//        sb.append(System.lineSeparator());
        sb.append("\t\tkafkaTemplate.send(record);");
        sb.append(System.lineSeparator());
        sb.append("\t}");
        sb.append(System.lineSeparator());
        return new String(sb);
    }
}
