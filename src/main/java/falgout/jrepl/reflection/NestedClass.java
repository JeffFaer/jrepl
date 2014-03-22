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
}
