package falgout.jrepl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Member;
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
import java.util.concurrent.ExecutionException;

import com.google.inject.ProvidedBy;

import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.guice.EnvironmentProvider;
import falgout.jrepl.reflection.NestedClass;
import falgout.util.Closeables;

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
    private final Map<String, LocalVariable<?>> variables = new LinkedHashMap<>();
    private final CodeRepository<NestedClass<?>> classes;
    
    public Environment(Reader in, Writer out, Writer err, Path generatedCodeLocation,
            CodeCompiler<NestedClass<?>> classCompiler) {
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);
        this.generatedCodeLocation = generatedCodeLocation;
        classes = new CodeRepository<>(classCompiler);
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
    
    public boolean addVariable(LocalVariable<?> variable) {
        if (containsVariable(variable.getName())) {
            return false;
        } else if (variable.isFinal() && !variable.isInitialized()) {
            throw new IllegalArgumentException("Uninitialized final variables are not allowed.");
        }
        
        variables.put(variable.getName(), variable);
        return true;
    }
    
    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    
    public LocalVariable<?> getVariable(String variableName) {
        return variables.get(variableName);
    }
    
    public Collection<? extends LocalVariable<?>> getVariables() {
        return Collections.unmodifiableCollection(variables.values());
    }
    
    public Set<Import> getImports() {
        return imports;
    }
    
    public boolean containsClass(String name) {
        return classes.contains(name);
    }
    
    public NestedClass<?> getClass(String name) {
        return classes.getCompiled(name);
    }
    
    public Optional<? extends NestedClass<?>> compile(SourceCode<? extends NestedClass<?>> code)
            throws ExecutionException {
        return classes.compile(this, code);
    }
    
    public Collection<? extends Member> getMembers() {
        return classes.getAllCompiled();
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
