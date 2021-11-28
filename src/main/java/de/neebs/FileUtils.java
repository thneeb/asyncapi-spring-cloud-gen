package de.neebs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public void writeJavaFile(String className, String folder, String packageName, String content) {
        try {
            Path path = Paths.get(folder + File.separator + packageName.replaceAll("\\.", "/") + File.separator + className + ".java");
            path.toFile().getParentFile().mkdirs();
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void writeAvroFile(String folder, String filename, String content) {
        try {
            Path path = Paths.get(folder + File.separator + filename + ".avsc");
            path.toFile().getParentFile().mkdirs();
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String camelToKebap(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return str.replaceAll(regex, replacement).toLowerCase();
    }
}
