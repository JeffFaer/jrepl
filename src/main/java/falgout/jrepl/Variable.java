package falgout.jrepl;

import java.util.Arrays;

import com.google.common.reflect.TypeToken;

public class Variable<T> {
    private final boolean _final;
    private final TypeToken<? extends T> type;
    private final String identifier;
    private T value;
    private boolean isInitialized;
    
    public Variable(TypeToken<? extends T> type, String identifier) {
        this(false, type, identifier);
    }
    
    public Variable(boolean _final, TypeToken<? extends T> type, String identifier) {
        this._final = _final;
        this.type = type;
        this.identifier = identifier;
        isInitialized = false;
    }
    
    public Variable(TypeToken<? extends T> type, String identifier, T value) {
        this(false, type, identifier, value);
    }
    
    public Variable(boolean _final, TypeToken<? extends T> type, String identifier, T value) {
        this(_final, type, identifier);
        this.value = value;
        isInitialized = true;
    }
    
    public boolean isFinal() {
        return _final;
    }
    
    public TypeToken<? extends T> getType() {
        return type;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public boolean isInitialized() {
        return isInitialized;
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
    public <E> boolean set(TypeToken<? extends E> type, E value) {
        if (this.type.isAssignableFrom(type)) {
            return set((T) value);
        }
        
        return false;
    }
    
    public <E> boolean set(Variable<E> other) {
        return set(other.getType(), other.get());
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
        StringBuilder b = new StringBuilder();
        if (_final) {
            b.append("final ");
        }
        b.append(type).append(" ").append(identifier);
        if (isInitialized) {
            b.append(" = ").append(toString(value));
        }
        b.append(";");
        
        return b.toString();
    }
    
    private String toString(T value) {
        if (value.getClass().isArray()) {
            return Arrays.deepToString((Object[]) value);
        } else {
            return value.toString();
        }
    }
}
