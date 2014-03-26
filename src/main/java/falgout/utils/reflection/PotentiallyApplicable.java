package falgout.utils.reflection;

import java.util.function.Predicate;

/*
 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2.1"
 */
class PotentiallyApplicable implements Predicate<Parameterized<?>> {
    private final String name;
    private final int arity;
    
    public PotentiallyApplicable(String name, Object[] args) {
        this(name, args.length);
    }
    
    public PotentiallyApplicable(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }
    
    @Override
    public boolean test(Parameterized<?> t) {
        if (!t.getName().equals(name)) {
            return false;
        }
        
        if (t.isVarArgs()) {
            return arity >= t.getParameterTypes().length - 1;
        } else {
            return arity == t.getParameterTypes().length;
        }
    }
}
