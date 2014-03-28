package falgout.jrepl.command.execute.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface SourceCode<T> {
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
    public int hashCode();
    
    @Override
    public boolean equals(Object o);
    
    @Override
    public String toString();
}
