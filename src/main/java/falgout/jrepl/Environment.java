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

import com.google.common.reflect.TypeToken;

import falgout.jrepl.command.Command;
import falgout.jrepl.command.JavaCommand;

public class Environment {
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    
    private final Map<String, Variable<?>> variables = new LinkedHashMap<>();
    
    public Environment(InputStream in, OutputStream out, OutputStream err) {
        this(new InputStreamReader(in), new OutputStreamWriter(out), new OutputStreamWriter(out));
    }
    
    public Environment(Reader in, Writer out, Writer err) {
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
    
    public <T> T get(String variableName, TypeToken<T> type) {
        Variable<?> var = variables.get(variableName);
        if (var != null && type.isAssignableFrom(var.getType())) {
            return (T) var.get();
        }
        return null;
    }
    
    public <T> Map<String, ? extends T> get(TypeToken<T> type) {
        Map<String, T> ret = new LinkedHashMap<>();
        for (Entry<String, Variable<?>> e : variables.entrySet()) {
            if (type.isAssignableFrom(e.getValue().getType())) {
                ret.put(e.getKey(), (T) e.getValue().get());
            }
        }
        
        return ret;
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
