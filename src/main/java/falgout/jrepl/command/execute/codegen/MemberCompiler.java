package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
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
    @SuppressWarnings("unchecked") public static final MemberCompiler<Field> FIELD_COMPILER = (MemberCompiler<Field>) INSTANCE;
    
    @Override
    public List<? extends M> execute(Environment env, List<? extends SourceCode<? extends M>> input)
            throws ExecutionException {
        GeneratedClass generated = new GeneratedClass(env);
        input.forEach(generated::addChild);
        Class<?> clazz = ClassCompiler.INSTANCE.execute(env, Arrays.asList(generated)).get(0);
        
        return input.stream().map(code -> {
            try {
                return code.getTarget(clazz);
            } catch (ReflectiveOperationException e) {
                throw new Error("We just made the method, it should be there", e);
            }
        }).collect(toList());
    }
}
