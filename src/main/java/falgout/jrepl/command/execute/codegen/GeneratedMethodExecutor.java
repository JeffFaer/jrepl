package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;

public enum GeneratedMethodExecutor implements Executor<GeneratedMethod, Object> {
    INSTANCE;
    
    /**
     * Compiles and invokes the given {@code GeneratedMethod}. The receiver
     * object is initialized with a {@link GeneratorModule} before invocation.
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
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new ExecutionException(e);
        }
    }
    
    public Object execute(GeneratedMethod input) throws ExecutionException {
        return execute(input.getEnvironment(), input);
    }
}
