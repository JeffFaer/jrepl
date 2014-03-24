package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

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
    public List<? extends Class<?>> execute(Environment env, List<? extends SourceCode<? extends Class<?>>> input)
            throws ExecutionException {
        List<JavaFileObject> sources = input.stream().map(code -> new SourceCodeJavaFile(code)).collect(toList());
        Stream<String> names = input.stream().map(code -> code.getName());
        
        String out = env.getGeneratedCodeLocation().toAbsolutePath().toString();
        String cp = System.getProperty("java.class.path") + File.pathSeparator + out;
        List<String> options = Arrays.asList("-d", out, "-cp", cp);
        
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileManager manager = JAVAC.getStandardFileManager(diagnostics, null, null);
        
        System.out.println(input);
        CompilationTask task = JAVAC.getTask(env.getError(), manager, diagnostics, options, null, sources);
        
        if (task.call()) {
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            return names.map(name -> {
                try {
                    return l.loadClass(name);
                } catch (ClassNotFoundException e) {
                    throw new Error("We just made this class, it should be there.", e);
                }
            }).collect(toList());
        } else {
            throw new ExecutionException(new CompilationException(input, diagnostics.getDiagnostics()));
        }
    }
}
