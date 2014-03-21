package falgout.jrepl.command.execute.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.EnvironmentClassLoader;
import falgout.jrepl.Variable;

public class ClassGenerator {
    private static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();
    static {
        if (JAVAC == null) {
            throw new Error("Please run this program with the JDK");
        }
    }
    private static final AtomicInteger ID = new AtomicInteger(0);
    public static final String TAB = "    ";

    private final Environment env;
    private final String className;

    public ClassGenerator(Environment env) {
        this.env = env;
        className = "$Generated" + ID.incrementAndGet();
    }
    
    public String getGeneratedClassName() {
        return className;
    }
    
    public Class<?> compile() throws IOException, ClassNotFoundException {
        EnvironmentClassLoader cl = env.getImportClassLoader();
        
        String out = cl.getDynamicCodeLocation().toAbsolutePath().toString();
        String cp = System.getProperty("java.class.path") + File.pathSeparator + out;
        List<String> options = Arrays.asList("-d", out, "-cp", cp);
        
        JavaFileObject compile = new StringJavaFileObject(className, toString());
        
        DiagnosticListener<JavaFileObject> errorReporter = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                env.getError().println(diagnostic.getMessage(null));
            }
        };
        JavaFileManager manager = JAVAC.getStandardFileManager(errorReporter, null, null);

        CompilationTask task = JAVAC.getTask(env.getError(), manager, errorReporter, options, null,
                Arrays.asList(compile));

        if (task.call()) {
            return cl.loadClass(className);
        } else {
            throw new Error("Couldn't compile...check the diagnostics");
        }
    }
    
    public Object getInstance() throws IOException, ClassNotFoundException {
        Class<?> clazz = compile();
        Injector i = Guice.createInjector(new GeneratorModule(env));
        Object o = i.getInstance(clazz);
        i.injectMembers(o);
        return o;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        // class declaration
        b.append("public class ").append(className).append(" {\n");
        
        // environment variables
        for (Variable<?> var : env.getVariables()) {
            String id = var.getIdentifier();
            b.append(TAB);
            b.append("@com.google.inject.Inject ");
            b.append("@javax.annotation.Nullable ");
            b.append("@com.google.inject.name.Named(\"").append(id).append("\") ");
            b.append("public ").append(var.getType()).append(" ").append(id).append(";\n");
        }

        // constructor
        b.append("\n");
        b.append(TAB).append("public ").append(className).append("() {}\n");

        b.append("}\n");

        return b.toString();
    }
}
