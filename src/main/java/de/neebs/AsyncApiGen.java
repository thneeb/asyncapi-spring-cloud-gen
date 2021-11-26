package de.neebs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.neebs.asyncapi.AsyncApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class AsyncApiGen {
    private final ObjectMapper objectMapper;

    private final ObjectMapper yamlObjectMapper;

    @Autowired
    public AsyncApiGen(ObjectMapper objectMapper, @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        this.objectMapper = objectMapper;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    public void run(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("You need to pass the AsyncApi file.");
            return;
        }
        final String filename = args[0];
        GeneratorConfig config = new GeneratorConfig(filename, "tmp", "de.neebs.model", "de.neebs.api");
        AsyncApiGenerator asyncApiGenerator = new AsyncApiGenerator(objectMapper, yamlObjectMapper);
        asyncApiGenerator.run(config);
    }
}
