package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    
    default public List<? extends R> execute(Environment env, Iterable<? extends I> input) throws ExecutionException {
        List<R> ret = new ArrayList<>();
        for (I i : input) {
            ret.add(execute(env, i));
        }
        
        return ret;
    }
    
    public static <I, R> Executor<I, Optional<? extends R>> optional(Executor<? super I, ? extends R> executor) {
        return (env, input) -> {
            return Optional.of(executor.execute(env, input));
        };
    }
    
    public static <I, R> Executor<Iterable<? extends I>, List<R>> process(
            Executor<? super I, Optional<? extends R>> executor) {
        return process(executor, Collectors.toList());
    }
    
    public static <I, T, C extends Collection<T>> Executor<Iterable<? extends I>, Optional<? extends C>> flatProcess(
            Executor<? super I, Optional<? extends C>> executor) {
        return process(executor, Collectors.reducing((c1, c2) -> {
            c1.addAll(c2);
            return c1;
        }));
    }
    
    public static <I, R, A, T> Executor<Iterable<? extends I>, R> process(
            Executor<? super I, Optional<? extends T>> executor, Collector<? super T, A, ? extends R> collector) {
        return (env, inputs) -> {
            A a = collector.supplier().get();
            Iterator<? extends I> itr = inputs.iterator();
            while (itr.hasNext()) {
                Optional<? extends T> opt = executor.execute(env, itr.next());
                if (opt.isPresent()) {
                    collector.accumulator().accept(a, opt.get());
                    itr.remove();
                }
            }
            return collector.finisher().apply(a);
        };
    }
    
    @SafeVarargs
    public static <I, R> Executor<I, Optional<? extends R>> sequence(
            Executor<? super I, ? extends Optional<? extends R>>... executors) {
        return (env, input) -> {
            for (Executor<? super I, ? extends Optional<? extends R>> e : executors) {
                Optional<? extends R> opt = e.execute(env, input);
                if (opt.isPresent()) {
                    return opt;
                }
            }
            
            return Optional.empty();
        };
    }
    
    public static <I, R, F> Executor<I, Optional<? extends R>> filter(Executor<? super F, ? extends R> executor,
            Function<? super I, ? extends F> filter) {
        return (env, input) -> {
            F filtered = filter.apply(input);
            return filtered == null ? Optional.empty() : Optional.of(executor.execute(env, filtered));
        };
    }
}
