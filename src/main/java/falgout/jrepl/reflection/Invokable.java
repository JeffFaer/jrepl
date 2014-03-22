package falgout.jrepl.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;

public abstract class Invokable<E extends Executable, T> {
    public static class Method extends Invokable<java.lang.reflect.Method, Object> {
        private final Object receiver;

        public Method(java.lang.reflect.Method executable, Object receiver) {
            super(executable);
            this.receiver = receiver;
        }
        
        @Override
        public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException,
        IllegalArgumentException {
            return getExecutable().invoke(receiver, args);
        }
    }

    public static class Constructor<T> extends Invokable<java.lang.reflect.Constructor<T>, T> {
        public Constructor(java.lang.reflect.Constructor<T> executable) {
            super(executable);
        }

        @Override
        public T invoke(Object... args) throws InstantiationException, InvocationTargetException,
        IllegalAccessException, IllegalArgumentException {
            return getExecutable().newInstance(args);
        }
    }
    
    private final E executable;

    protected Invokable(E executable) {
        this.executable = executable;
    }
    
    public E getExecutable() {
        return executable;
    }
    
    public abstract T invoke(Object... args) throws IllegalAccessException, InstantiationException,
    InvocationTargetException;

    public static Invokable.Method from(Object receiver, java.lang.reflect.Method method) {
        return new Invokable.Method(method, receiver);
    }
    
    public static <T> Invokable.Constructor<T> from(java.lang.reflect.Constructor<T> constructor) {
        return new Invokable.Constructor<>(constructor);
    }
}
