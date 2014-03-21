package falgout.jrepl.command.execute.codegen;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Optional;

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
    public Optional<? extends M> execute(Environment env, SourceCode<? extends M> input) throws IOException {
        GeneratedClass genClass = new GeneratedClass(env);
        genClass.addChild(input);
        Optional<? extends Class<?>> opt = ClassCompiler.INSTANCE.execute(env, genClass);
        if (opt.isPresent()) {
            return Optional.of(input.getTarget(opt.get()));
        } else {
            return Optional.empty();
        }
    }
}
