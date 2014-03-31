package falgout.jrepl.command.execute;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

public interface BatchExecutor<I, R> extends Executor<I, R> {
    @Override
    default public R execute(Environment env, I input) throws ExecutionException {
        return execute(env, Arrays.asList(input)).get(0);
    }
    
    @Override
    public abstract List<? extends R> execute(Environment env, Collection<? extends I> input) throws ExecutionException;
}
