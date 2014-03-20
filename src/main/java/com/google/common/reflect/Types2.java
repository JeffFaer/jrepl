package com.google.common.reflect;

import java.lang.reflect.Type;

/**
 * com.google.common.reflect.Types is package private, but I need access to some
 * of its methods.
 *
 * @author jeffrey
 */
public class Types2 {
    public static TypeToken<?> addArraysToType(TypeToken<?> baseType, int extraArrays) {
        Type composite = baseType.getType();
        for (int i = 0; i < extraArrays; i++) {
            composite = Types.newArrayType(composite);
        }
        return TypeToken.of(composite);
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
