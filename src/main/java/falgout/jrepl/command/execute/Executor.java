package falgout.jrepl.command.execute;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import falgout.jrepl.Environment;

@FunctionalInterface
public interface Executor<I, R> {
    public R execute(Environment env, I input) throws ExecutionException;
    
    default public <RR> Executor<I, RR> andThen(Function<? super R, ? extends RR> convert) {
        return (env, input) -> convert.apply(execute(env, input));
    }
}
