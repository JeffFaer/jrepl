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

import falgout.jrepl.command.execute.codegen.GeneratedSourceCode;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.TypeSeparator;

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
            return GoogleTypes.toString((TypeToken<?>) o, TypeSeparator.DOT,
                    clazz -> isGenerated(clazz) ? clazz.getSimpleName() : clazz.getCanonicalName());
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
    
    private static boolean isGenerated(Class<?> clazz) {
        Package p = clazz.getPackage();
        return p != null && p.getName().equals("jrepl") && clazz.getName().contains(GeneratedSourceCode.TEMPLATE);
    }
}
