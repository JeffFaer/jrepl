package falgout.utils.reflection;

import static falgout.utils.reflection.CompoundTypeConversion.METHOD_INVOCATION;
import static falgout.utils.reflection.CompoundTypeConversion.SUBTYPING;

enum Phase {
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2.2"
     */
    ONE(SUBTYPING),
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2.3"
     */
    TWO,
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.12.2.4"
     */
    THREE {
        @Override
        public boolean isApplicable(Class<?>[] args, Parameterized<?> p) {
            if (!p.isVarArgs()) {
                return false;
            }
            
            int k = args.length;
            int n = p.getParameterTypes().length;
            
            for (int i = 0; i < n - 1; i++) {
                if (!c.convert(args[i], p.getParameterTypes()[i])) {
                    return false;
                }
            }
            
            if (k >= n) {
                for (int i = n - 1; i < args.length; i++) {
                    if (!c.convert(args[i], p.getParameterTypes()[n - 1].getComponentType())) {
                        return false;
                    }
                }
            }
            
            return true;
        }
    };
    
    protected final TypeConversion c;
    
    private Phase() {
        this(METHOD_INVOCATION);
    }
    
    private Phase(TypeConversion c) {
        this.c = c;
    }
    
    public boolean isApplicable(Class<?>[] args, Parameterized<?> p) {
        // var args methods are actually treated as regular methods whose last
        // parameter is an array during method resolution.
        
        for (int i = 0; i < args.length; i++) {
            if (!c.convert(args[i], p.getParameterTypes()[i])) {
                return false;
            }
        }
        return true;
    }
}
