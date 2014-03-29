package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import falgout.jrepl.Environment;

/**
 * This class can only execute {@code static} methods.
 * 
 * @author jeffrey
 *
 */
public class MethodExecutor extends CodeExecutor<Method, Object> {
    private final CodeCompiler<Method> compiler;
    private final Object[] args;
    
    @Inject
    public MethodExecutor(CodeCompiler<Method> compiler, @Assisted Object[] args) {
        this.compiler = compiler;
        this.args = args;
    }
    
    @Override
    public List<? extends Object> execute(Environment env, Iterable<? extends NamedSourceCode<? extends Method>> input)
        throws ExecutionException {
        List<? extends Method> methods = compiler.execute(env, input);
        List<Object> ret = new ArrayList<>(methods.size());
        for (Method m : methods) {
            try {
                ret.add(m.invoke(null, args));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ExecutionException(e);
            }
        }
        return ret;
    }
}
