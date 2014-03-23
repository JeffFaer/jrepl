package falgout.jrepl.guice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.inject.Provider;

public class TemporaryDirectoryProvider implements Provider<Path> {
    private final String prefix;
    
    public TemporaryDirectoryProvider(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public Path get() {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
