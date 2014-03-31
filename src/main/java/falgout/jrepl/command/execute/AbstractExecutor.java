package falgout.jrepl.command.execute;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

public abstract class AbstractExecutor<I, R> implements Executor<I, R> {
    protected AbstractExecutor() {}
    
    @SafeVarargs
    public final List<? extends R> execute(Environment env, I... input) throws ExecutionException {
        return execute(env, Arrays.asList(input));
    }
}
