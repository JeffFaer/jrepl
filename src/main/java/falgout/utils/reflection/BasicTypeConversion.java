package falgout.utils.reflection;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public enum BasicTypeConversion implements TypeConversion {
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.1"
     */
    IDENTITY {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            return to.equals(from);
        }
    },
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2"
     * 
     * This conversion also encapsulated the {@link #IDENTITY identity
     * conversion}.
     */
    WIDENING_PRIMITIVE {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            if (from == null || !from.isPrimitive() || !to.isPrimitive() || !PRIMITIVES.contains(from)
                    || !PRIMITIVES.contains(to) || to == char.class) {
                return from == to;
            } else if (from == char.class) {
                if (to == short.class) {
                    return false;
                }
                from = short.class;
            }
            
            return PRIMITIVES.indexOf(from) <= PRIMITIVES.indexOf(to);
        }
    },
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.4"
     */
    WIDENING_AND_NARROWING_PRIMITIVE {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            if (from == null) {
                return false;
            }
            
            return from == byte.class && to == char.class;
        }
    },
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.5"
     * 
     * This conversion only tests for improper subtypes (includes the
     * {@link #IDENTITY identity conversion}).
     */
    WIDENING_REFERENCE {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            if (to.isPrimitive()) {
                return false;
            }
            return from == null || to.isAssignableFrom(from);
        }
    },
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.7"
     * 
     * This conversion also applies a {@link #WIDENING_REFERENCE widening
     * reference conversion} if necessary.
     */
    BOXING {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            if (from == null || !from.isPrimitive() || to.isPrimitive()) {
                return false;
            }
            Class<?> boxed = BOX.get(from);
            return boxed == null ? false : WIDENING_REFERENCE.convert(boxed, to);
        }
    },
    /**
     * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.8"
     * 
     * This conversion also applies a {@link #WIDENING_PRIMITIVE widening
     * primitive conversion} if necessary.
     */
    UNBOXING {
        @Override
        protected boolean doConvert(Class<?> from, Class<?> to) {
            if (from == null || from.isPrimitive() || !to.isPrimitive()) {
                return false;
            }
            Class<?> unboxed = UNBOX.get(from);
            return unboxed == null ? false : WIDENING_PRIMITIVE.convert(unboxed, to);
        }
    };
    private static final List<Class<?>> PRIMITIVES = Arrays.<Class<?>> asList(byte.class, char.class, short.class,
            int.class, long.class, float.class, double.class);
    private static final Map<Class<?>, Class<?>> BOX = new LinkedHashMap<>();
    private static final Map<Class<?>, Class<?>> UNBOX = new LinkedHashMap<>();
    static {
        BOX.put(boolean.class, Boolean.class);
        BOX.put(byte.class, Byte.class);
        BOX.put(short.class, Short.class);
        BOX.put(char.class, Character.class);
        BOX.put(int.class, Integer.class);
        BOX.put(long.class, Long.class);
        BOX.put(float.class, Float.class);
        BOX.put(double.class, Double.class);
        
        for (Entry<Class<?>, Class<?>> e : BOX.entrySet()) {
            UNBOX.put(e.getValue(), e.getKey());
        }
    }
    
    @Override
    public final boolean convert(Class<?> from, Class<?> to) {
        if (to == null) {
            throw new IllegalArgumentException("to == null");
        } else if (to == void.class) {
            throw new IllegalArgumentException("to == void");
        } else if (from == void.class) {
            throw new IllegalArgumentException("from == void");
        }
        
        return doConvert(from, to);
    }
    
    protected abstract boolean doConvert(Class<?> from, Class<?> to);
}
