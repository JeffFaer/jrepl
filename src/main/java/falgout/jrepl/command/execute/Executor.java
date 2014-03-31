package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    
    default public List<? extends R> execute(Environment env, Collection<? extends I> input) throws ExecutionException {
        List<R> ret = new ArrayList<>(input.size());
        for (I i : input) {
            ret.add(execute(env, i));
        }
        
        return ret;
    }
    
    default public <RR> Executor<I, RR> andThen(Function<? super R, ? extends RR> after) {
        Objects.requireNonNull(after);
        return (env, input) -> after.apply(execute(env, input));
    }
}
