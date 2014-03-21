package falgout.jrepl.reflection;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.GeneratorModule;
import falgout.jrepl.command.execute.codegen.MemberCompiler;
import falgout.jrepl.command.execute.codegen.SourceCode;

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

    /**
     * This method
     * {@link falgout.jrepl.command.execute.codegen.MemberCompiler#execute(Environment, SourceCode)
     * compiles} the given {@code SourceCode} and creates a receiver instance
     * for its {@link #from(Object, java.lang.reflect.Method) Invokable} by
     * injecting the required members with a {@link GeneratorModule}.
     *
     * @param env The environment to compile in
     * @param method The code to compile
     * @return An {@code Optional} that contains the {@code Invokable}, if one
     *         was created.
     * @throws IOException If an {@code IOException} occurs during
     *         {@link falgout.jrepl.command.execute.codegen.MemberCompiler#execute(Environment, SourceCode)
     *         compilation}.
     */
    public static Optional<Invokable.Method> from(Environment env, SourceCode<? extends java.lang.reflect.Method> method)
            throws IOException {
        Optional<? extends java.lang.reflect.Method> opt = MemberCompiler.METHOD_COMPILER.execute(env, method);
        if (opt.isPresent()) {
            java.lang.reflect.Method m = opt.get();
            Object receiver;
            if (Modifier.isStatic(m.getModifiers())) {
                receiver = null;
            } else {
                Class<?> clazz = m.getDeclaringClass();
                Injector i = Guice.createInjector(new GeneratorModule(env));
                receiver = i.getInstance(clazz);
            }

            return Optional.of(from(receiver, m));
        } else {
            return Optional.empty();
        }
    }

    public static <T> Invokable.Constructor<T> from(java.lang.reflect.Constructor<T> constructor) {
        return new Invokable.Constructor<>(constructor);
    }
}
