package falgout.jrepl.command.execute;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

public abstract class BatchExecutor<I, R> implements Executor<I, R> {
    @Override
    public R execute(Environment env, I input) throws ExecutionException {
        return execute(env, Arrays.asList(input)).get(0);
    }
    
    @SafeVarargs
    public final List<? extends R> execute(Environment env, I... input) throws ExecutionException {
        return execute(env, Arrays.asList(input));
    }
    
    @Override
    public abstract List<? extends R> execute(Environment env, Iterable<? extends I> input) throws ExecutionException;
}
