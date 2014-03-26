package falgout.jrepl.guice;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentClassLoader;
import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.reflection.NestedClass;

@Singleton
public class EnvironmentProvider implements Provider<Environment> {
    private final Environment env;
    private final EnvironmentClassLoader cl;
    
    @Inject
    public EnvironmentProvider(Reader in, @Stdout Writer out, @Stderr Writer err,
            @CodeDirectory Path generatedCodeLocation, CodeCompiler<NestedClass<?>> classCompiler,
            CodeCompiler<Method> methodCompiler) {
        env = new Environment(in, out, err, generatedCodeLocation, classCompiler, methodCompiler);
        cl = new EnvironmentClassLoader(env);
    }
    
    @Override
    public Environment get() {
        Thread.currentThread().setContextClassLoader(cl);
        return env;
    }
}
