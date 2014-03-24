package falgout.jrepl.reflection;

import java.util.function.Function;

/**
 * Classes use two different type separators for their inner types. Canonical
 * names (ones found in code, for instance) use a dot:
 * {@code foo.bar.Type.InnerType}. Class names (internal representation in
 * {@link Class}) use a dollar sign: {@code foo.bar.Type$InnerType}.
 * 
 * @author jeffrey
 */
public enum TypeSeparator implements Function<Class<?>, String> {
    DOT(".") {
        @Override
        public String apply(Class<?> t) {
            return t.getCanonicalName();
        }
    },
    DOLLAR("$") {
        @Override
        public String apply(Class<?> t) {
            return t.getName();
        }
    };
    
    private final String delim;
    
    private TypeSeparator(String delim) {
        this.delim = delim;
    }
    
    @Override
    public String toString() {
        return delim;
    }
    
    @Override
    public abstract String apply(Class<?> t);
}
