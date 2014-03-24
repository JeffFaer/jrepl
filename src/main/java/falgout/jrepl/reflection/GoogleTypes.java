package falgout.jrepl.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.function.Function;

import com.google.common.reflect.TypeToken;
import com.google.common.reflect.Types2;
import com.google.inject.TypeLiteral;

/**
 * Utility methods for dealing with {@code TypeToken} and {@code TypeLiteral}
 * from Guava and Guice.
 *
 * @author jeffrey
 */
public class GoogleTypes {
    public static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);
    public static final TypeToken<?> VOID = TypeToken.of(void.class);
    public static final TypeToken<Integer> INT = TypeToken.of(int.class);
    public static final TypeToken<Character> CHAR = TypeToken.of(char.class);
    
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
            t = Types2.arrayOf(t);
        }
        
        return TypeToken.of(t);
    }
    
    public static TypeToken<?> newParameterizedType(TypeToken<?> owner, TypeToken<?> raw, TypeToken<?>... arguments) {
        Type[] args = new Type[arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = arguments[i].getType();
        }
        
        return TypeToken.of(Types2.newParameterizedTypeWithOwner(owner == null ? null : owner.getType(),
                raw.getRawType(), args));
    }
    
    public static TypeToken<?> subtypeOf(TypeToken<?> upper) {
        return TypeToken.of(Types2.subtypeOf(upper.getType()));
    }
    
    public static TypeToken<?> supertypeOf(TypeToken<?> lower) {
        return TypeToken.of(Types2.supertypeOf(lower.getType()));
    }
    
    public static String toString(TypeToken<?> type, TypeSeparator delim) {
        return toString(type, delim, delim);
    }
    
    public static String toString(TypeToken<?> type, TypeSeparator delim, Function<? super Class<?>, String> toString) {
        return toString(type.getType(), delim, toString);
    }
    
    private static String toString(Type type, TypeSeparator delim, Function<? super Class<?>, String> toString) {
        StringBuilder b = new StringBuilder();
        if (type instanceof GenericArrayType) {
            b.append(toString(((GenericArrayType) type).getGenericComponentType(), delim, toString));
            b.append("[]");
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> raw = TypeToken.of(pt.getRawType()).getRawType();
            if (pt.getOwnerType() != null) {
                b.append(toString(pt.getOwnerType(), delim, toString));
                b.append(delim);
                b.append(raw.getSimpleName());
            } else {
                b.append(toString(raw, delim, toString));
            }
            
            b.append("<");
            for (int i = 0; i < pt.getActualTypeArguments().length; i++) {
                if (i > 0) {
                    b.append(", ");
                }
                
                b.append(toString(pt.getActualTypeArguments()[i], delim, toString));
            }
            b.append(">");
        } else if (type instanceof TypeVariable) {
            b.append(((TypeVariable<?>) type).getName());
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            b.append("?");
            
            for (Type lower : wt.getLowerBounds()) {
                b.append(" super ").append(toString(lower, delim, toString));
            }
            for (Type upper : wt.getUpperBounds()) {
                b.append(" extends ").append(toString(upper, delim, toString));
            }
        } else {
            b.append(toString.apply((Class<?>) type));
        }
        
        return b.toString();
    }
}
