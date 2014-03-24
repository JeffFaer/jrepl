package falgout.jrepl.command.execute.codegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;

public abstract class CodeCompiler<T> implements Executor<List<? extends SourceCode<? extends T>>, List<? extends T>> {
    @SafeVarargs
    public final List<? extends T> execute(Environment env, SourceCode<? extends T>... input) throws ExecutionException {
        return execute(env, Arrays.asList(input));
    }
    
    public T execute(Environment env, SourceCode<? extends T> input) throws ExecutionException {
        return execute(env, Arrays.asList(input)).get(0);
    }
    
    @SafeVarargs
    public final List<? extends T> execute(GeneratedSourceCode<? extends T, ?>... input) throws ExecutionException {
        return execute(Arrays.asList(input));
    }
    
    public T execute(GeneratedSourceCode<? extends T, ?> input) throws ExecutionException {
        return execute(Arrays.asList(input)).get(0);
    }
    
    public List<? extends T> execute(List<? extends GeneratedSourceCode<? extends T, ?>> input)
            throws ExecutionException {
        if (input.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        return execute(input.get(0).getEnvironment(), input);
    }
}
