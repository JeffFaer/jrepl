package falgout.jrepl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public interface ThrowingBiFunction<T, U, R, X extends Throwable> {
    public R apply(T t, U u) throws X;
    
    default public List<? extends R> apply(Collection<? extends T> ts, U u) throws X {
        List<R> ret = new ArrayList<>(ts.size());
        for (T t : ts) {
            ret.add(apply(t, u));
        }
        
        return ret;
    }
    
    default public List<? extends R> apply(T t, Collection<? extends U> us) throws X {
        List<R> ret = new ArrayList<>(us.size());
        for (U u : us) {
            ret.add(apply(t, u));
        }
        
        return ret;
    }
    
    default public <RR> ThrowingBiFunction<T, U, RR, X> andThen(
            ThrowingFunction<? super R, ? extends RR, ? extends X> after) {
        Objects.requireNonNull(after);
        return (t, u) -> after.apply(apply(t, u));
    }
    
    default public <RR> ThrowingBiFunction<T, U, RR, X> andThen(Function<? super R, ? extends RR> after) {
        return (t, u) -> after.apply(apply(t, u));
    }
}
