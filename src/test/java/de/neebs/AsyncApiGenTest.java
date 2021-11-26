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

    @BeforeEach
    public void init() {
        RestConfig restConfig = new RestConfig();
        ObjectMapper objectMapper = restConfig.jacksonBuilder().build();
        ObjectMapper yamlObjectMapper = restConfig.yamlObjectMapper();
        asyncApiGen = new AsyncApiGen(objectMapper, yamlObjectMapper);
    }

    @Test
    public void testWithoutInput(CapturedOutput outputCapture) throws Exception {
        asyncApiGen.run();
        assertThat(outputCapture).contains("You need to pass the AsyncApi file.");
    }

    @Test
    public void testSwagger2File(CapturedOutput outputCapture) throws Exception {
        asyncApiGen.run( "src/test/resources/testfiles/pin-event.yaml");
        assertThat(outputCapture).contains("Only AsyncAPI is supported");
    }

    @Test
    public void testAsyncApi(CapturedOutput outputCapture) throws Exception {
        asyncApiGen.run( "src/test/resources/testfiles/pin-event.yaml");
    }
}
