package falgout.jrepl.command.execute.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
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
    public Optional<? extends Class<?>> execute(Environment env, SourceCode<? extends Class<?>> input)
            throws IOException {
        EnvironmentClassLoader cl = env.getImportClassLoader();
        
        String out = cl.getDynamicCodeLocation().toAbsolutePath().toString();
        String cp = System.getProperty("java.class.path") + File.pathSeparator + out;
        List<String> options = Arrays.asList("-d", out, "-cp", cp);
        
        DiagnosticListener<JavaFileObject> errorReporter = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                env.getError().println(diagnostic.getMessage(null));
            }
        };
        JavaFileManager manager = JAVAC.getStandardFileManager(errorReporter, null, null);

        CompilationTask task = JAVAC.getTask(env.getError(), manager, errorReporter, options, null,
                Arrays.asList(input));

        if (task.call()) {
            try {
                return Optional.of(cl.loadClass(input.getName()));
            } catch (ClassNotFoundException e) {
                throw new Error("We just made this class, it should be there.", e);
            }
        } else {
            return Optional.empty();
        }
    }
}
