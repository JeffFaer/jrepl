package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;

/**
 * Compiles an arbitrary {@code Member} by wrapping it in
 * {@code ClassSourceCode}.
 *
 * @author jeffrey
 *
 * @param <M> An arbitrary {@code Member}
 */
@Singleton
public class MemberCompiler<M extends Member> extends CodeCompiler<M> {
    private final CodeCompiler<Class<?>> compiler;
    
    @Inject
    public MemberCompiler(CodeCompiler<Class<?>> compiler) {
        this.compiler = compiler;
    }
    
    @Override
    public List<? extends M> execute(Environment env, Iterable<? extends NamedSourceCode<? extends M>> input)
        throws ExecutionException {
        ClassSourceCode.Builder b = ClassSourceCode.builder(env);
        b.addChildren(input);
        
        ClassSourceCode generated = b.build();
        
        if (generated.getChildren().size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        Class<?> clazz = compiler.execute(env, generated);
        
        List<M> members = new ArrayList<>(generated.getChildren().size());
        for (NamedSourceCode<? extends M> code : input) {
            try {
                members.add(code.getTarget(clazz));
            } catch (ReflectiveOperationException e) {
                throw new Error("We just made the member.", e);
            }
        }
        return members;
    }
}
