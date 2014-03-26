package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ReflectionUtilities {
    private ReflectionUtilities() {}
    
    public static Class<?>[] getClasses(Object... args) {
        Class<?>[] classes = new Class<?>[args.length];
        for (int x = 0; x < args.length; x++) {
            classes[x] = args[x] == null ? null : args[x].getClass();
        }
        return classes;
    }
    
    public static <T> Set<Constructor<T>> getConstructors(Class<T> clazz) {
        return createConstructorList(clazz.getConstructors());
    }
    
    public static <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz) {
        return createConstructorList(clazz.getDeclaredConstructors());
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Set<Constructor<T>> createConstructorList(Constructor<?>... constructors) {
        Set<Constructor<T>> ctors = new LinkedHashSet<>(constructors.length);
        
        for (Constructor<?> ctor : constructors) {
            ctors.add((Constructor<T>) ctor);
        }
        
        return ctors;
    }
}
