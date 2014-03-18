package com.google.common.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class Types2 {
    public static TypeToken<?> addArraysToType(TypeToken<?> baseType, int extraArrays) {
        Type composite = baseType.getType();
        for (int i = 0; i < extraArrays; i++) {
            composite = Types.newArrayType(composite);
        }
        return TypeToken.of(composite);
    }
    
    public static ParameterizedType newParameterizedType(Class<?> rawType, Type... arguments) {
        return Types.newParameterizedType(rawType, arguments);
    }
    
    public static ParameterizedType newParameterizedType(Type ownerType, Class<?> rawType, Type... arguments) {
        return Types.newParameterizedTypeWithOwner(ownerType, rawType, arguments);
    }
    
    public static WildcardType subtypeOf(Type t) {
        return Types.subtypeOf(t);
    }
    
    public static WildcardType supertypeOf(Type t) {
        return Types.supertypeOf(t);
    }
}