package falgout.jrepl.command.execute;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import falgout.jrepl.Environment;

@FunctionalInterface
public interface Executor<I, R> {
    /**
     * Performs a value-bearing action on an input. This method modifies the
     * {@code Environment} as needed. If, for any reason, the {@code input}
     * cannot be executed, this method will throw an {@code ExecutionException}.
     *
     * @param env The {@code Environment} to execute in.
     * @param input The {@code input} to execute
     * @return A non-{@code null} result.
     * @throws ExecutionException If an exception occurs during execution.
     */
    public R execute(Environment env, I input) throws ExecutionException;
    
    default public <RR> Executor<I, RR> andThen(Function<? super R, ? extends RR> convert) {
        return (env, input) -> convert.apply(execute(env, input));
    }
}
