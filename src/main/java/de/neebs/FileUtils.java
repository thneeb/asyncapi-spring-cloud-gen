package de.neebs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public void writeFile(String className, String folder, String packageName, String content) {
        try {
            Path path = Paths.get(folder + File.separator + packageName.replaceAll("\\.", "/") + File.separator + className + ".java");
            path.toFile().getParentFile().mkdirs();
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
