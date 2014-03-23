package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.reflection.Invokable;

public enum GeneratedMethodExecutor implements Executor<GeneratedMethod, Object> {
    INSTANCE;
    
    /**
     * Creates an {@link Invokable} from the given {@code GeneratedMethod}. When
     * {@link Invokable#invoke invoked}, it will execute the method. The
     * receiver object is initialized with a {@link GeneratorModule}.
     */
    @Override
    public Object execute(Environment env, GeneratedMethod input) throws ExecutionException {
        java.lang.reflect.Method method = METHOD_COMPILER.execute(env, input);
        Object receiver;
        if (Modifier.isStatic(method.getModifiers())) {
            receiver = null;
        } else {
            Class<?> clazz = method.getDeclaringClass();
            Injector i = Guice.createInjector(new GeneratorModule(env, clazz));
            receiver = i.getInstance(clazz);
        }
        try {
            return method.invoke(receiver);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }
    
    public Object execute(GeneratedMethod input) throws ExecutionException {
        return execute(input.getEnvironment(), input);
    }
}
