package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import falgout.jrepl.Environment;

@FunctionalInterface
public interface Executor<I, R> {
    public Optional<? extends R> execute(Environment env, I input) throws IOException;

    public static <I, R> Executor<Iterable<? extends I>, List<? extends R>> process(
            Executor<? super I, ? extends R> executor) {
        return process(executor, Collectors.toList());
    }
    
    public static <I, R, A, T> Executor<Iterable<? extends I>, R> process(Executor<? super I, ? extends T> executor,
            Collector<? super T, A, ? extends R> collector) {
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
            return Optional.of(collector.finisher().apply(a));
        };
    }
    
    @SafeVarargs
    public static <I, R> Executor<I, R> combine(Executor<? super I, ? extends R>... executors) {
        return (env, input) -> {
            for (Executor<? super I, ? extends R> e : executors) {
                Optional<? extends R> opt = e.execute(env, input);
                if (opt.isPresent()) {
                    return opt;
                }
            }

            return Optional.empty();
        };
    }
    
    public static <I, R, F> Executor<I, R> filter(Executor<? super F, ? extends R> executor,
            Function<? super I, ? extends F> filter) {
        return (env, input) -> {
            F filtered = filter.apply(input);
            return filtered == null ? Optional.empty() : executor.execute(env, filtered);
        };
    }
}
