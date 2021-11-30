package de.neebs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneratorConfig {
    private String inputSpec;
    private String sourceFolder;
    private String modelPackage;
    private String apiPackage;
    private boolean avro;
    private boolean spring;
}
