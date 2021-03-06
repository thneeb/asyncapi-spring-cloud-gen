package de.neebs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class AsyncApiGenTest {
    private AsyncApiGen asyncApiGen;

    private GeneratorConfig config;

    private AsyncApiGenerator asyncApiGenerator;

    private AvroGenerator multiFileAvroGenerator;

    @BeforeEach
    public void init() {
        RestConfig restConfig = new RestConfig();
        ObjectMapper objectMapper = restConfig.jacksonBuilder().build();
        ObjectMapper yamlObjectMapper = restConfig.yamlObjectMapper();
        asyncApiGen = new AsyncApiGen(objectMapper, yamlObjectMapper);
        config = new GeneratorConfig("src/test/resources/testfiles/pin-event.yaml", "tmp", "de.neebs.model", "de.neebs.api", true, true);
        asyncApiGenerator = new AsyncApiGenerator(objectMapper, yamlObjectMapper);
    }

    @Test
    public void testWithoutInput(CapturedOutput outputCapture) {
        asyncApiGen.run();
        assertThat(outputCapture).contains("You need to pass the AsyncApi file.");
    }

    @Test
    public void testAsyncApi(CapturedOutput outputCapture) {
        asyncApiGen.run( "src/test/resources/testfiles/pin-event.yaml");
    }

    @Test
    public void testMarketingConsent(CapturedOutput outputCapture) {
        config.setAvro(true);
        config.setInputSpec("src/test/resources/testfiles/marketing-consent-event.yaml");
        asyncApiGenerator.run(config);
        config.setAvro(false);
        asyncApiGen.run( );
    }

    @Test
    public void testAsyncApiAvroGen(CapturedOutput outputCapture) {
        config.setAvro(true);
        asyncApiGenerator.run(config);
        config.setAvro(false);
    }

    @Test
    public void testAccountEvent(CapturedOutput outputCapture) {
        config.setAvro(true);
        config.setInputSpec("src/test/resources/testfiles/account-event.yaml");
        asyncApiGenerator.run(config);
        config.setAvro(false);
        asyncApiGen.run( );
    }
}
