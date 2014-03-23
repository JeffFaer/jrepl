package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;
import falgout.jrepl.reflection.NestedClass;

/**
 * Compiles an arbitrary {@code Member} by creating a {@link GeneratedClass} for
 * it.
 *
 * @author jeffrey
 *
 * @param <M> An arbitrary {@code Member}
 */
public class MemberCompiler<M extends Member> implements CodeCompiler<M> {
    private static final MemberCompiler<?> INSTANCE = new MemberCompiler<>();
    @SuppressWarnings("unchecked") public static final MemberCompiler<Method> METHOD_COMPILER = (MemberCompiler<Method>) INSTANCE;
    @SuppressWarnings("unchecked") public static final MemberCompiler<NestedClass<?>> NESTED_CLASS_COMPILER = (MemberCompiler<NestedClass<?>>) INSTANCE;
    
    @Override
    public M execute(Environment env, SourceCode<? extends M> input) throws ExecutionException {
        GeneratedClass genClass = new GeneratedClass(env);
        genClass.addChild(input);
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env, genClass);
        try {
            return input.getTarget(clazz);
        } catch (ReflectiveOperationException e) {
            throw new ExecutionException(e);
        }
    }
}
