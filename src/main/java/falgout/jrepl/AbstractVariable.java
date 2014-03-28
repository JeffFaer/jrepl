package falgout.jrepl;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Defaults;
import com.google.common.collect.ImmutableMap;
import com.google.common.escape.ArrayBasedCharEscaper;
import com.google.common.escape.Escaper;
import com.google.common.reflect.TypeToken;

import falgout.jrepl.reflection.GoogleTypes;

public abstract class AbstractVariable<T> implements Variable<T> {
    protected AbstractVariable() {}
    
    @Override
    public boolean set(T value) {
        if (isFinal() && isInitialized()) {
            return false;
        }
        doSet(value == null ? getDefaultValue() : value);
        return true;
    }
    
    protected abstract void doSet(T value);
    
    @SuppressWarnings("unchecked")
    protected T getDefaultValue() {
        return (T) Defaults.defaultValue(getType().getRawType());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isFinal() ? 1231 : 1237);
        result = prime * result + (isInitialized() ? 1231 : 1237);
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((get() == null) ? 0 : get().hashCode());
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
        if (isFinal() != other.isFinal()) {
            return false;
        }
        if (isInitialized() != other.isInitialized()) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getType() == null) {
            if (other.getType() != null) {
                return false;
            }
        } else if (!getType().equals(other.getType())) {
            return false;
        }
        if (get() == null) {
            if (other.get() != null) {
                return false;
            }
        } else if (!get().equals(other.get())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getHeader(getModifiers()));
        if (isInitialized()) {
            b.append(" = ").append(toString(get()));
        }
        b.append(";");
        return b.toString();
    }
    
    protected abstract int getModifiers();
    
    protected String getHeader(int modifiers) {
        StringBuilder b = new StringBuilder();
        if (isFinal()) {
            modifiers |= Modifier.FINAL;
        }
        String mods = Modifier.toString(modifiers);
        if (!mods.isEmpty()) {
            b.append(Modifier.toString(modifiers)).append(" ");
        }
        
        b.append(toString(getType())).append(" ");
        b.append(getName());
        
        return b.toString();
    }
    
    static String toString(Object o) {
        if (o == null) {
            return "null";
        } else if (o.getClass().isArray()) {
            return Arrays.deepToString((Object[]) o);
        } else if (o instanceof Character) {
            return "'" + escape((char) o) + "'";
        } else if (o instanceof String) {
            return "\"" + escape((String) o) + "\"";
        } else if (o instanceof TypeToken) {
            return GoogleTypes.toCanonicalString((TypeToken<?>) o);
        } else if (o instanceof Type) {
            return toString(TypeToken.of((Type) o));
        } else {
            return o.toString();
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
    
    private static String escape(char ch) {
        return escape(String.valueOf(ch));
    }
    
    private static String escape(String str) {
        return JAVA_ESCAPER.escape(str);
    }
}
