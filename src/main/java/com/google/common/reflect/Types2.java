package com.google.common.reflect;

import java.lang.reflect.Type;

public class Types2 {
    public static Type arrayOf(Type t) {
        return Types.newArrayType(t);
    }
    
    public static Type newParameterizedTypeWithOwner(Type ownerType, Class<?> rawType, Type... arguments) {
        return Types.newParameterizedTypeWithOwner(ownerType, rawType, arguments);
    }
    
    public static Type subtypeOf(Type upperBound) {
        return Types.subtypeOf(upperBound);
    }
    
    public static Type supertypeOf(Type lowerBound) {
        return Types.supertypeOf(lowerBound);
    }
}
