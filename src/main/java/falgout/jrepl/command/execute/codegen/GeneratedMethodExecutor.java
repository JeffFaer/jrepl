package falgout.jrepl.command.execute.codegen;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;

public enum GeneratedMethodExecutor implements Executor<GeneratedMethod, Object> {
    INSTANCE;
    
    @Override
    public Object execute(Environment env, GeneratedMethod input) throws ExecutionException {
        java.lang.reflect.Method method = METHOD_COMPILER.execute(env, input);
        Object receiver;
        if (Modifier.isStatic(method.getModifiers())) {
            receiver = null;
        } else {
            throw new Error("Should've been static");
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
