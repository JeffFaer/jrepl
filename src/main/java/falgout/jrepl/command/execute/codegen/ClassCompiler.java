package falgout.jrepl.command.execute.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentClassLoader;

public enum ClassCompiler implements CodeCompiler<Class<?>> {
    INSTANCE;
    private static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();
    static {
        if (JAVAC == null) {
            throw new Error("Please run this program with the JDK");
        }
    }
    
    @Override
    public Class<?> execute(Environment env, SourceCode<? extends Class<?>> input) throws ExecutionException {
        try {
            EnvironmentClassLoader cl = env.getImportClassLoader();
            
            String out = cl.getDynamicCodeLocation().toAbsolutePath().toString();
            String cp = System.getProperty("java.class.path") + File.pathSeparator + out;
            List<String> options = Arrays.asList("-d", out, "-cp", cp);
            
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaFileManager manager = JAVAC.getStandardFileManager(diagnostics, null, null);
            
            CompilationTask task = JAVAC.getTask(env.getError(), manager, diagnostics, options, null,
                    Arrays.asList(new SourceCodeJavaFile(input)));
            
            if (task.call()) {
                try {
                    return cl.loadClass(input.getName());
                } catch (ClassNotFoundException e) {
                    throw new Error("We just made this class, it should be there.", e);
                }
            } else {
                throw new ExecutionException(new CompilationException(input, diagnostics.getDiagnostics()));
            }
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
}
