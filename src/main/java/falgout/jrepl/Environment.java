package falgout.jrepl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.inject.ProvidedBy;

import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.guice.EnvironmentProvider;
import falgout.jrepl.reflection.NestedClass;
import falgout.jrepl.util.Closeables;

@ProvidedBy(EnvironmentProvider.class)
public class Environment implements Closeable {
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    
    private Path generatedCodeLocation;
    private final Set<Import> imports = new ImportSet();
    {
        imports.add(Import.create(false, "java.lang", true));
    }
    
    private final Map<String, FieldVariable<?>> variables = new LinkedHashMap<>();
    private final CodeRepository<NestedClass<?>> classes;
    private final CodeRepository<Method> methods;
    
    public Environment(Reader in, Writer out, Writer err, Path generatedCodeLocation,
            CodeCompiler<NestedClass<?>> classCompiler, CodeCompiler<Method> methodCompiler) {
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);
        
        this.generatedCodeLocation = generatedCodeLocation;
        
        classes = new CodeRepository<>(classCompiler);
        methods = new CodeRepository<>(methodCompiler);
    }
    
    private PrintWriter createPrintWriter(Writer w) {
        return w instanceof PrintWriter ? (PrintWriter) w : new PrintWriter(w, true);
    }
    
    public BufferedReader getInput() {
        return in;
    }
    
    public PrintWriter getOutput() {
        return out;
    }
    
    public PrintWriter getError() {
        return err;
    }
    
    public Path getGeneratedCodeLocation() {
        return generatedCodeLocation;
    }
    
    public String getGeneratedCodePackage() {
        return "jrepl";
    }
    
    public boolean addVariable(FieldVariable<?> variable) {
        if (containsVariable(variable.getName())) {
            return false;
        }
        variables.put(variable.getName(), variable);
        return true;
    }
    
    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    
    public Optional<? extends Variable<?>> getVariable(String variableName) {
        return Optional.ofNullable(variables.get(variableName));
    }
    
    public Collection<? extends Variable<?>> getVariables() {
        return Collections.unmodifiableCollection(variables.values());
    }
    
    public Set<Import> getImports() {
        return imports;
    }
    
    public CodeRepository<NestedClass<?>> getClassRepository() {
        return classes;
    }
    
    public CodeRepository<Method> getMethodRepository() {
        return methods;
    }
    
    public Collection<? extends Member> getMembers() {
        Collection<Member> members = new ArrayList<>();
        variables.values().stream().map(var -> var.getField()).forEach(members::add);
        members.addAll(classes.getAllCompiled());
        members.addAll(methods.getAllCompiled());
        return Collections.unmodifiableCollection(members);
    }
    
    @Override
    public void close() throws IOException {
        if (Files.exists(generatedCodeLocation)) {
            List<IOException> exceptions = new ArrayList<>();
            
            Files.walkFileTree(generatedCodeLocation, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        exceptions.add(exc);
                    }
                    
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            
            Closeables.throwFirst(exceptions);
        }
    }
}
