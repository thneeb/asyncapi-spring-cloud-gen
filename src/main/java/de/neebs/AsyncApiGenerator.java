package de.neebs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.neebs.asyncapi.AsyncApi;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class AsyncApiGenerator {
    private final ObjectMapper objectMapper;

    private final ObjectMapper yamlObjectMapper;

    public AsyncApiGenerator(ObjectMapper objectMapper, ObjectMapper yamlObjectMapper) {
        this.objectMapper = objectMapper;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    public void run(GeneratorConfig config) {
        try {
            Map<String, Object> map = yamlObjectMapper.readValue(new File(config.getInputSpec()), new TypeReference<Map<String, Object>>() {});
            if (map.get("asyncapi") == null) {
                System.out.println("Only AsyncAPI is supported");
                return;
            }
            AsyncApi asyncApi = objectMapper.convertValue(map, AsyncApi.class);
            FileUtils fileUtils = new FileUtils();
            if (asyncApi.getComponents() != null && asyncApi.getComponents().getSchemas() != null) {
                if (config.isAvro()) {
                    MultiFileAvroGenerator avroGenerator = new MultiFileAvroGenerator(config, fileUtils);
                    avroGenerator.generateMessage(asyncApi.getComponents());
                } else {
                    final ModelClassGenerator modelClassGenerator = new ModelClassGenerator(config, fileUtils);
                    modelClassGenerator.generateModelClasses(asyncApi.getComponents().getSchemas());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
