package falgout.jrepl;

import com.google.common.reflect.TypeToken;

public class Variable<T> {
    private T value;
    private final TypeToken<T> type;
    private final boolean isFinal;
    
    public Variable(T value, Class<T> clazz) {
        this(value, TypeToken.of(clazz));
    }
    
    public Variable(T value, TypeToken<T> type) {
        this(value, type, false);
    }
    
    public Variable(T value, TypeToken<T> type, boolean isFinal) {
        this.value = value;
        this.type = type;
        this.isFinal = isFinal;
    }
    
    public TypeToken<T> getType() {
        return type;
    }
    
    public T get() {
        return value;
    }
    
    public boolean set(T value) {
        if (isFinal) {
            return false;
        } else {
            this.value = value;
            return true;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <E> boolean set(E value, TypeToken<E> type) {
        if (this.type.isAssignableFrom(type)) {
            return set((T) value);
        }
        
        return false;
    }
    
    public <E> boolean set(Variable<E> other) {
        return set(other.get(), other.getType());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        Variable<?> other = (Variable<?>) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Variable [type=");
        builder.append(type);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }
}
