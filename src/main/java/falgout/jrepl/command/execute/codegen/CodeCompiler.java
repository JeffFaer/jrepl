package falgout.jrepl.command.execute.codegen;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.command.execute.BatchExecutor;

public abstract class CodeCompiler<T> extends BatchExecutor<SourceCode<? extends T>, T> {
    public T execute(GeneratedSourceCode<? extends T, ?> input) throws ExecutionException {
        return execute(Arrays.asList(input)).get(0);
    }
    
    @SafeVarargs
    public final List<? extends T> execute(GeneratedSourceCode<? extends T, ?>... input) throws ExecutionException {
        return execute(Arrays.asList(input));
    }
    
    public List<? extends T> execute(Iterable<? extends GeneratedSourceCode<? extends T, ?>> input)
            throws ExecutionException {
        Iterator<? extends GeneratedSourceCode<?, ?>> i = input.iterator();
        if (!i.hasNext()) {
            new IllegalArgumentException("No input!");
        }
        
        return execute(i.next().getEnvironment(), input);
    }
}
