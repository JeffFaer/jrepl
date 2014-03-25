package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class MemberCompiler<M extends Member> extends CodeCompiler<M> {
    private static final MemberCompiler<?> INSTANCE = new MemberCompiler<>();
    @SuppressWarnings("unchecked") public static final MemberCompiler<Method> METHOD_COMPILER = (MemberCompiler<Method>) INSTANCE;
    @SuppressWarnings("unchecked") public static final MemberCompiler<NestedClass<?>> NESTED_CLASS_COMPILER = (MemberCompiler<NestedClass<?>>) INSTANCE;
    
    @Override
    public List<? extends M> execute(Environment env, Iterable<? extends SourceCode<? extends M>> input)
            throws ExecutionException {
        GeneratedClass generated = new GeneratedClass(env);
        input.forEach(generated::addChild);
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env, Arrays.asList(generated)).get(0);
        
        List<M> members = new ArrayList<>(generated.getChildren().size());
        for (SourceCode<? extends M> code : input) {
            try {
                members.add(code.getTarget(clazz));
            } catch (ReflectiveOperationException e) {
                throw new Error("We just made the member.", e);
            }
        }
        return members;
    }
}
