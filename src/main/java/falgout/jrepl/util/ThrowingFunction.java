package falgout.jrepl.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> {
    public R apply(T t) throws X;
    
    default public <RR> ThrowingFunction<T, RR, X> andThen(ThrowingFunction<? super R, ? extends RR, ? extends X> after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }
    
    default public <RR> ThrowingFunction<T, RR, X> andThen(Function<? super R, ? extends RR> after) {
        Objects.requireNonNull(after);
        return t -> after.apply(apply(t));
    }
}
