package falgout.jrepl.command.execute.codegen;

import java.util.Arrays;
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
}
