package falgout.jrepl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.command.execute.codegen.GeneratedSourceCode;
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
    private final Map<String, Method> methods = new LinkedHashMap<>();
    private final Map<String, NestedClass<?>> classes = new LinkedHashMap<>();
    
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
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
            truncateStackTrace(t);
            t.printStackTrace(err);
        } else {
            while (t != null) {
                String message = t.getLocalizedMessage();
                if (!message.isEmpty()) {
                    err.println(message);
                }
                t = t.getCause();
            }
        }
    }
    
    private void truncateStackTrace(Throwable t) {
        if (t == null) {
            return;
        }
        
        StackTraceElement[] st = t.getStackTrace();
        int i;
        for (i = 0; i < st.length && st[i].getClassName().contains(GeneratedSourceCode.TEMPLATE); i++) {}
        t.setStackTrace(Arrays.copyOf(st, i));
        t.printStackTrace(err);
        
        truncateStackTrace(t.getCause());
        for (Throwable s : t.getSuppressed()) {
            truncateStackTrace(s);
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
    
    public boolean addMethod(Method method) {
        if (methods.containsKey(method.getName())) {
            return false;
        }
        
        methods.put(method.getName(), method);
        return true;
    }
    
    public boolean addClass(NestedClass<?> clazz) {
        if (classes.containsKey(clazz.getName())) {
            return false;
        }
        
        classes.put(clazz.getName(), clazz);
        return true;
    }
    
    public boolean containsClass(String className) {
        return classes.containsKey(className);
    }
    
    public NestedClass<?> getClass(String className) {
        return classes.get(className);
    }
    
    public Set<? extends Member> getMembers() {
        Set<Member> members = new LinkedHashSet<>(methods.size() + classes.size());
        members.addAll(methods.values());
        members.addAll(classes.values());
        
        return Collections.unmodifiableSet(members);
    }
    
    @Override
    public void close() throws IOException {
        cl.close();
    }
}
