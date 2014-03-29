package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class MethodInvoker {
    private final MethodLocator l;
    
    public MethodInvoker(MethodLocator l) {
        this.l = l;
    }
    
    public MethodLocator getMethodLocator() {
        return l;
    }
    
    public Object invoke(Object instance, String name, Object... args) throws AmbiguousDeclarationException,
        NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
        return invoke(instance, l.getMethod(instance.getClass(), name, args), args);
    }
    
    public Object invokeDeclared(Object instance, String name, Object... args) throws AmbiguousDeclarationException,
        NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
        return invoke(instance, l.getDeclaredMethod(instance.getClass(), name, args), args);
    }
    
    public Object invokeStatic(Class<?> clazz, String name, Object... args) throws InvocationTargetException,
        IllegalAccessException, IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        return invoke(null, l.getMethod(clazz, name, args), args);
    }
    
    public Object invokeDeclaredStatic(Class<?> clazz, String name, Object... args) throws InvocationTargetException,
        IllegalAccessException, IllegalArgumentException, AmbiguousDeclarationException, NoSuchMethodException {
        return invoke(null, l.getMethod(clazz, name, args), args);
    }
    
    protected abstract Object invoke(Object instance, Method m, Object... args) throws InvocationTargetException,
        IllegalAccessException, IllegalArgumentException;
    
    public <T> T invoke(Class<T> clazz, Object... args) throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException, AmbiguousDeclarationException, NoSuchMethodException {
        return invoke(l.getConstructor(clazz, args), args);
    }
    
    public <T> T invokeDeclared(Class<T> clazz, Object... args) throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException, AmbiguousDeclarationException, NoSuchMethodException {
        return invoke(l.getDeclaredConstructor(clazz, args), args);
    }
    
    protected abstract <T> T invoke(Constructor<T> cons, Object... args) throws InstantiationException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    
    private static final MethodInvoker DEFAULT = new VarArgsMethodInvoker(new JLSMethodLocator());
    
    public static MethodInvoker getDefault() {
        return DEFAULT;
    }
}
