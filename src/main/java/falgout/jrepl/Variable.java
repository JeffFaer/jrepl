package falgout.jrepl;

import com.google.common.reflect.TypeToken;

public interface Variable<T> {
    public boolean isFinal();
    
    public TypeToken<T> getType();
    
    public String getName();
    
    public boolean isInitialized();
    
    public T get();
    
    default public boolean canAssignTo(TypeToken<?> type) {
        return type.isAssignableFrom(getType());
    }
    
    @SuppressWarnings("unchecked")
    default public <E> E get(TypeToken<E> type) {
        return canAssignTo(type) ? (E) get() : null;
    }
    
    public boolean set(T value);
    
    @SuppressWarnings("unchecked")
    default public <E> boolean set(TypeToken<? extends E> type, E value) {
        return getType().isAssignableFrom(type) ? set((T) value) : false;
    }
    
    default public <E> boolean set(Variable<E> other) {
        return set(other.getType(), other.get());
    }
}
