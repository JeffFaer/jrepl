package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import falgout.jrepl.Environment;

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

    @Override
    public M execute(Environment env, SourceCode<? extends M> input) throws ExecutionException {
        GeneratedClass genClass = new GeneratedClass(env);
        genClass.addChild(input);
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env, genClass);
        return input.getTarget(clazz);
    }
}
