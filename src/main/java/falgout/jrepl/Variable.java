package falgout.jrepl;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableMap;
import com.google.common.escape.ArrayBasedCharEscaper;
import com.google.common.escape.Escaper;
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
        set((T) null);
        isInitialized = false;
    }

    public Variable(TypeToken<? extends T> type, String identifier, T value) {
        this(false, type, identifier, value);
    }

    public Variable(boolean _final, TypeToken<? extends T> type, String identifier, T value) {
        this(_final, type, identifier);
        set(value);
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

    @SuppressWarnings("unchecked")
    public <E> E get(TypeToken<E> type) {
        return type.isAssignableFrom(this.type) ? (E) value : null;
    }

    @SuppressWarnings("unchecked")
    public boolean set(T value) {
        if (_final && isInitialized) {
            return false;
        } else {
            if (value == null) {
                // assume they meant the default value
                this.value = (T) Defaults.defaultValue(type.getRawType());
            } else {
                this.value = value;
            }
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
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
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
        if (!(obj instanceof Variable)) {
            return false;
        }
        Variable<?> other = (Variable<?>) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
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

    public static String toString(Object value) {
        if (value == null) {
            return "null";
        } else if (value.getClass().isArray()) {
            return Arrays.deepToString((Object[]) value);
        } else if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        } else if (value instanceof Character) {
            return "'" + escape((char) value) + "'";
        } else {
            return value.toString();
        }
    }

    private static final Escaper JAVA_ESCAPER;
    static {
        Map<Character, String> escapes = ImmutableMap.<Character, String> builder()
                .put('\t', "\\t")
                .put('\b', "\\b")
                .put('\n', "\\n")
                .put('\r', "\\r")
                .put('\f', "\\f")
                .put('\'', "\\\'")
                .put('\"', "\\\"")
                .put('\\', "\\\\")
                .build();
        JAVA_ESCAPER = new ArrayBasedCharEscaper(escapes, (char) 32, (char) 126) {
            @Override
            protected char[] escapeUnsafe(char c) {
                return String.format("\\u%04x", (int) c).toCharArray();
            }
        };
    }

    private static String escape(String str) {
        return JAVA_ESCAPER.escape(str);
    }
    
    private static String escape(char ch) {
        return JAVA_ESCAPER.escape(String.valueOf(ch));
    }
}
