package falgout.jrepl;

import com.google.common.reflect.TypeToken;

public class Variable<T> {
    private final TypeToken<T> type;
    private T value;
    
    public Variable(T value, TypeToken<T> type) {
        this.type = type;
        this.value = value;
    }
    
    public Variable(T value, Class<T> clazz) {
        this(value, TypeToken.of(clazz));
    }
    
    public TypeToken<T> getType() {
        return type;
    }
    
    public T get() {
        return value;
    }
    
    public void set(T value) {
        this.value = value;
    }
    
    @SuppressWarnings("unchecked")
    public <E> boolean set(E value, TypeToken<E> type) {
        if (this.type.isAssignableFrom(type)) {
            this.value = (T) value;
            return true;
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
