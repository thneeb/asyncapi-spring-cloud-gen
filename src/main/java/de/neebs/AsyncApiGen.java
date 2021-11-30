package de.neebs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
        GeneratorConfig config = new GeneratorConfig(filename, "tmp", "de.neebs.model", "de.neebs.api", false, true);
        AsyncApiGenerator asyncApiGenerator = new AsyncApiGenerator(objectMapper, yamlObjectMapper);
        asyncApiGenerator.run(config);
    }
}
