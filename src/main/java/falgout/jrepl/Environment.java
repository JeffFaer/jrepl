package falgout.jrepl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.command.Command;
import falgout.jrepl.command.CommandFactory;
import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

@Singleton
public final class Environment {
    private static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);
    private final CommandFactory factory;
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    
    private final Set<Import> imports = new ImportSet();
    private final EnvironmentClassLoader cl = new EnvironmentClassLoader(imports);
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();
    
    public Environment(CommandFactory factory, InputStream in, OutputStream out, OutputStream err) {
        this(factory, new InputStreamReader(in), new OutputStreamWriter(out), new OutputStreamWriter(err));
    }
    
    @Inject
    public Environment(CommandFactory factory, Reader in, @Stdout Writer out, @Stderr Writer err) {
        this.factory = factory;
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);

        try {
            execute("import java.lang.*;");
        } catch (IOException e) {
            throw new Error(e);
        }
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
    
    public boolean addVariables(Set<Variable<?>> variables) {
        boolean modified = false;
        for (Variable<?> var : variables) {
            if (!this.variables.containsKey(var.getIdentifier())) {
                this.variables.put(var.getIdentifier(), var);
                modified = true;
            }
        }
        
        return modified;
    }
    
    public Object getVariable(String variableName) {
        return getVariable(variableName, OBJECT);
    }
    
    public <T> T getVariable(String variableName, TypeToken<T> type) {
        Variable<?> var = variables.get(variableName);
        if (var != null && type.isAssignableFrom(var.getType())) {
            return var.get(type);
        }
        return null;
    }
    
    public boolean containsVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    
    public Set<Import> getImports() {
        return imports;
    }
    
    public ClassLoader getImportClassLoader() {
        return cl;
    }
    
    public void execute(String input) throws IOException {
        try {
            Command<?> c = factory.getCommand(this, input);
            if (c != null) {
                c.execute(this);
            }
        } finally {
            out.flush();
            err.flush();
        }
    }
}
