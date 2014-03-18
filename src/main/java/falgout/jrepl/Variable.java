package falgout.jrepl;

import com.google.common.reflect.TypeToken;

public class Variable<T> {
    private T value;
    private boolean isInitialized;
    private final TypeToken<? extends T> type;
    private final boolean _final;
    
    public Variable(TypeToken<? extends T> type) {
        this(type, false);
    }
    
    public Variable(TypeToken<? extends T> type, boolean _final) {
        isInitialized = false;
        this.type = type;
        this._final = _final;
    }
    
    public Variable(T value, TypeToken<? extends T> type) {
        this(value, type, false);
    }
    
    public Variable(T value, TypeToken<? extends T> type, boolean _final) {
        this(type, _final);
        this.value = value;
        isInitialized = true;
    }
    
    public TypeToken<? extends T> getType() {
        return type;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public boolean isFinal() {
        return _final;
    }
    
    public T get() {
        return value;
    }
    
    public boolean set(T value) {
        if (_final && isInitialized) {
            return false;
        } else {
            this.value = value;
            isInitialized = true;
            return true;
        }
    }
    
    @SuppressWarnings("unchecked")
    public <E> boolean set(E value, TypeToken<? extends E> type) {
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
