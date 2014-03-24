package falgout.jrepl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.command.execute.codegen.SourceCode;

public class LocalVariable<T> extends AbstractVariable<T> {
    private final boolean _final;
    private final TypeToken<T> type;
    private final String name;
    private T value;
    private boolean initialized;
    
    public LocalVariable(TypeToken<T> type, String name) {
        this(false, type, name);
    }
    
    public LocalVariable(boolean _final, TypeToken<T> type, String name) {
        this(_final, type, name, null);
        initialized = false;
    }
    
    public LocalVariable(TypeToken<T> type, String name, T value) {
        this(false, type, name, value);
    }
    
    public LocalVariable(boolean _final, TypeToken<T> type, String name, T value) {
        this._final = _final;
        this.type = type;
        this.name = name;
        set(value);
    }
    
    @Override
    public boolean isFinal() {
        return _final;
    }
    
    @Override
    public TypeToken<T> getType() {
        return type;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public T get() {
        return value;
    }
    
    @Override
    protected void doSet(T value) {
        this.value = value;
        initialized = true;
    }
    
    @Override
    protected int getModifiers() {
        return 0;
    }
    
    public SourceCode<Field> asField() {
        return new SourceCode<Field>(name) {
            @Override
            public Field getTarget(Class<?> clazz) throws NoSuchFieldException {
                return clazz.getField(getName());
            }
            
            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append("@com.google.inject.Inject ");
                b.append("@javax.annotation.Nullable ");
                b.append("@com.google.inject.name.Named(\"").append(getName()).append("\")");
                b.append(getHeader(Modifier.PUBLIC | Modifier.STATIC));
                if (_final) {
                    Object val = getDefaultValue();
                    b.append(" = ").append(AbstractVariable.toString(val));
                }
                b.append(";\n");
                return b.toString();
            }
        };
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_final ? 1231 : 1237);
        result = prime * result + (initialized ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (!(obj instanceof LocalVariable)) {
            return false;
        }
        LocalVariable<?> other = (LocalVariable<?>) obj;
        if (_final != other._final) {
            return false;
        }
        if (initialized != other.initialized) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
