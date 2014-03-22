package falgout.jrepl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;
import falgout.jrepl.reflection.NestedClass;

@Singleton
public final class Environment implements Closeable {
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    
    private final Set<Import> imports = new ImportSet();
    {
        imports.add(Import.create(false, "java.lang", true));
    }
    private final EnvironmentClassLoader cl = new EnvironmentClassLoader(this);
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();
    private final Map<String, SourceCode<Method>> methods = new LinkedHashMap<>();
    private final Map<String, SourceCode<NestedClass<?>>> classes = new LinkedHashMap<>();
    
    @Inject
    public Environment(Reader in, @Stdout Writer out, @Stderr Writer err) {
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);
        
        Thread.currentThread().setContextClassLoader(cl);
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
    
    public void printStackTrace(Throwable t) {
        while (t != null) {
            String message = t.getLocalizedMessage();
            if (!message.isEmpty()) {
                err.println(message);
            }
            t = t.getCause();
        }
    }
    
    public boolean addVariable(Variable<?> variable) {
        if (containsVariable(variable.getIdentifier())) {
            return false;
        } else if (variable.isFinal() && !variable.isInitialized()) {
            throw new IllegalArgumentException("Uninitialized final variables are not allowed.");
        }
        
        variables.put(variable.getIdentifier(), variable);
        return true;
    }
    
    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    
    public Variable<?> getVariable(String variableName) {
        return variables.get(variableName);
    }
    
    public Collection<? extends Variable<?>> getVariables() {
        return Collections.unmodifiableCollection(variables.values());
    }
    
    public Set<Import> getImports() {
        return imports;
    }
    
    public EnvironmentClassLoader getImportClassLoader() {
        return cl;
    }
    
    public boolean addMethod(SourceCode<Method> code) {
        if (methods.containsKey(code.getName())) {
            return false;
        }
        
        methods.put(code.getName(), code);
        return true;
    }
    
    public boolean addClass(SourceCode<NestedClass<?>> code) {
        if (classes.containsKey(code.getName())) {
            return false;
        }
        
        classes.put(code.getName(), code);
        return true;
    }
    
    public boolean containsClass(String className) {
        return classes.containsKey(className);
    }
    
    public SourceCode<NestedClass<?>> getClass(String className) {
        return classes.get(className);
    }
    
    public List<? extends SourceCode<? extends Member>> getMembers() {
        List<SourceCode<? extends Member>> members = new ArrayList<>(methods.size() + classes.size());
        members.addAll(methods.values());
        members.addAll(classes.values());
        
        return Collections.unmodifiableList(members);
    }
    
    @Override
    public void close() throws IOException {
        cl.close();
    }
}
