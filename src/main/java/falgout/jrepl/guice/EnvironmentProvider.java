package falgout.jrepl.guice;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentClassLoader;

@Singleton
public class EnvironmentProvider implements Provider<Environment> {
    private final Environment env;
    private final EnvironmentClassLoader cl;
    
    @Inject
    public EnvironmentProvider(Reader in, @Stdout Writer out, @Stderr Writer err,
            @CodeDirectory Path generatedCodeLocation) {
        env = new Environment(in, out, err, generatedCodeLocation);
        cl = new EnvironmentClassLoader(env);
    }
    
    @Override
    public Environment get() {
        Thread.currentThread().setContextClassLoader(cl);
        return env;
    }
}
