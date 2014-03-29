package falgout.utils.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

abstract class Invokable<M extends AccessibleObject & GenericDeclaration & Member, R> extends Parameterized<M> {
    public static class Method extends Invokable<java.lang.reflect.Method, Object> {
        private final Object receiver;
        
        public Method(java.lang.reflect.Method m, Object receiver) {
            super(m);
            this.receiver = receiver;
        }
        
        @Override
        public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
            return member.invoke(receiver, args);
        }
        
        @Override
        public Class<?>[] getParameterTypes() {
            return member.getParameterTypes();
        }
        
        @Override
        public boolean isVarArgs() {
            return member.isVarArgs();
        }
    }
    
    public static class Constructor<T> extends Invokable<java.lang.reflect.Constructor<T>, T> {
        public Constructor(java.lang.reflect.Constructor<T> c) {
            super(c);
        }
        
        @Override
        public T invoke(Object... args) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
            return member.newInstance(args);
        }
        
        @Override
        public Class<?>[] getParameterTypes() {
            return member.getParameterTypes();
        }
        
        @Override
        public boolean isVarArgs() {
            return member.isVarArgs();
        }
    }
    
    public Invokable(M member) {
        super(member);
    }
    
    public abstract R invoke(Object... args) throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException;
}
