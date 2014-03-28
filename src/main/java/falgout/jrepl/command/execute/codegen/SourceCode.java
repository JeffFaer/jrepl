package falgout.jrepl.command.execute.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class SourceCode<T> {
    public static abstract class Builder<T, S extends SourceCode<T>, B extends Builder<T, S, B>> {
        protected abstract B getBuilder();
        
        public abstract S build();
        
        @SafeVarargs
        protected final <E> List<E> requireNonNull(E... es) {
            return requireNonNull(Arrays.asList(es));
        }
        
        protected <E, I extends Iterable<E>> I requireNonNull(I i) {
            i.forEach(e -> Objects.requireNonNull(e));
            
            return i;
        }
    }
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract boolean equals(Object o);
    
    @Override
    public abstract String toString();
}
