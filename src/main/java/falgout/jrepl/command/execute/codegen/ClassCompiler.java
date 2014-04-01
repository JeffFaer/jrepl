package falgout.jrepl.command.execute.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import falgout.jrepl.Environment;

public class ClassCompiler extends CodeCompiler<Class<?>> {
    private static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();
    static {
        if (JAVAC == null) {
            throw new Error("Please run this program with the JDK");
        }
    }
    public static final ClassCompiler INSTANCE = new ClassCompiler();
    
    @Override
    public List<? extends Class<?>> execute(Environment env,
            Collection<? extends NamedSourceCode<? extends Class<?>>> input) throws ExecutionException {
        List<JavaFileObject> sources = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        for (NamedSourceCode<? extends Class<?>> code : input) {
            sources.add(new SourceCodeJavaFile(code));
            names.add(code.getName());
        }
        
        String out = env.getGeneratedCodeLocation().toAbsolutePath().toString();
        String cp = System.getProperty("java.class.path") + File.pathSeparator + out;
        List<String> options = Arrays.asList("-d", out, "-cp", cp);
        
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileManager manager = JAVAC.getStandardFileManager(diagnostics, null, null);
        
        CompilationTask task = JAVAC.getTask(env.getError(), manager, diagnostics, options, null, sources);
        
        if (task.call()) {
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            List<Class<?>> classes = new ArrayList<>(names.size());
            for (String name : names) {
                try {
                    classes.add(l.loadClass(name));
                } catch (ClassNotFoundException e) {
                    throw new Error("We just made this class.", e);
                }
            }
            return classes;
        } else {
            throw new ExecutionException(new CompilationException(input, diagnostics.getDiagnostics()));
        }
    }
}
