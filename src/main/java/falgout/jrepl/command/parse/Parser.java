package falgout.jrepl.command.parse;

import java.util.function.Function;

@FunctionalInterface
public interface Parser<I, R> extends Function<I, R> {
    public R parse(I input);
    
    @Override
    default public R apply(I t) {
        return parse(t);
    }
}
