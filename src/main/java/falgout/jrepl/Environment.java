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
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.command.Command;
import falgout.jrepl.command.JavaCommand;
import falgout.jrepl.guice.Stderr;
import falgout.jrepl.guice.Stdout;

@Singleton
public class Environment {
    private static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    
    private final Set<Import> imports = new ImportSet();
    private final EnvironmentClassLoader cl = new EnvironmentClassLoader(imports);
    {
        imports.addAll(Import.create("import java.lang.*;"));
    }
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();
    
    public Environment(InputStream in, OutputStream out, OutputStream err) {
        this(new InputStreamReader(in), new OutputStreamWriter(out), new OutputStreamWriter(out));
    }
    
    @Inject
    public Environment(Reader in, @Stdout Writer out, @Stderr Writer err) {
        this.in = in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
        this.out = createPrintWriter(out);
        this.err = createPrintWriter(err);
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
    
    public boolean addVariables(Map<String, Variable<?>> variables) {
        boolean modified = false;
        for (Entry<String, Variable<?>> e : variables.entrySet()) {
            if (!this.variables.containsKey(e.getKey())) {
                this.variables.put(e.getKey(), e.getValue());
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
            return (T) var.get();
        }
        return null;
    }
    
    public Map<String, ? extends Object> getVariables() {
        return getVariables(OBJECT);
    }
    
    public <T> Map<String, ? extends T> getVariables(TypeToken<T> type) {
        Map<String, T> ret = new LinkedHashMap<>();
        for (Entry<String, Variable<?>> e : variables.entrySet()) {
            if (type.isAssignableFrom(e.getValue().getType())) {
                ret.put(e.getKey(), (T) e.getValue().get());
            }
        }
        
        return ret;
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
            Command c = JavaCommand.getCommand(input, err);
            if (c == null) {
                return;
            }
            c.execute(this);
        } finally {
            out.flush();
            err.flush();
        }
    }
}
