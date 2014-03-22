package falgout.jrepl.command.execute.codegen;

import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilationException extends IllegalArgumentException {
    private static final long serialVersionUID = 7774053476250643623L;
    
    private final SourceCode<? extends Class<?>> source;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public CompilationException(SourceCode<? extends Class<?>> source,
            List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super(createMessage(diagnostics));
        this.source = source;
        this.diagnostics = diagnostics;
    }
    
    private static String createMessage(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        return diagnostics.stream().map(d -> d.getMessage(null)).collect(Collectors.joining("\n"));
    }

    public SourceCode<? extends Class<?>> getSource() {
        return source;
    }
    
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }
}
