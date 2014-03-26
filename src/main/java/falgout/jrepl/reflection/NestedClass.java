package falgout.jrepl.reflection;

import java.lang.reflect.Member;

public class NestedClass<T> implements Member {
    private final Class<T> clazz;
    
    public NestedClass(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    /**
     * @return The {@code Class} representing this {@code NestedClass}.
     */
    public Class<T> getDeclaredClass() {
        return clazz;
    }
    
    /**
     * @return The enclosing {@code Class} of this {@code NestedClass}.
     */
    @Override
    public Class<?> getDeclaringClass() {
        return clazz.getDeclaringClass();
    }
    
    @Override
    public String getName() {
        return clazz.getSimpleName();
    }
    
    @Override
    public int getModifiers() {
        return clazz.getModifiers();
    }
    
    @Override
    public boolean isSynthetic() {
        return clazz.isSynthetic();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NestedClass)) {
            return false;
        }
        NestedClass<?> other = (NestedClass<?>) obj;
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return clazz.toGenericString();
    }
}
