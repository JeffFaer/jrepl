package falgout.jrepl.reflection;

import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

/**
 * Utility methods for dealing with {@code TypeToken} and {@code TypeLiteral}
 * from Guava and Guice.
 *
 * @author jeffrey
 */
public class GoogleTypes {
    public static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);
    public static final TypeToken<?> VOID = TypeToken.of(void.class);
    public static final TypeToken<?> INT = TypeToken.of(int.class);
    public static final TypeToken<?> CHAR = TypeToken.of(char.class);

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> get(TypeToken<T> type) {
        return (TypeLiteral<T>) TypeLiteral.get(type.getType());
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> get(TypeLiteral<T> type) {
        return (TypeToken<T>) TypeToken.of(type.getType());
    }

    public static TypeToken<?> addArrays(TypeToken<?> component, int numArrays) {
        Type t = component.getType();
        for (int i = 0; i < numArrays; i++) {
            t = Types.arrayOf(t);
        }
        
        return TypeToken.of(t);
    }
    
    public static TypeToken<?> newParameterizedType(TypeToken<?> owner, TypeToken<?> raw, TypeToken<?>... arguments) {
        Type[] args = new Type[arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = arguments[i].getType();
        }

        return TypeToken.of(Types.newParameterizedTypeWithOwner(owner == null ? null : owner.getType(),
                raw.getRawType(), args));
    }

    public static TypeToken<?> subtypeOf(TypeToken<?> upper) {
        return TypeToken.of(Types.subtypeOf(upper.getType()));
    }

    public static TypeToken<?> supertypeOf(TypeToken<?> lower) {
        return TypeToken.of(Types.supertypeOf(lower.getType()));
    }
}
