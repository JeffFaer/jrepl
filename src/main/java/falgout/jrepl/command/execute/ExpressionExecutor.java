package falgout.jrepl.command.execute;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.METHOD_COMPILER;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

import com.google.inject.Guice;
import com.google.inject.Injector;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.GeneratedMethod;
import falgout.jrepl.command.execute.codegen.GeneratorModule;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.command.execute.codegen.WrappedStatement;
import falgout.jrepl.reflection.Invokable;

public enum ExpressionExecutor implements Executor<Expression, Invokable.Method> {
    INSTANCE;
    
    /**
     * Creates an {@link Invokable} which requires no arguments. When
     * {@link Invokable#invoke invoked}, it will return the value of the
     * expression. This method initializes the receiver object with a
     * {@link GeneratorModule}.
     */
    @Override
    public Optional<? extends Invokable.Method> execute(Environment env, Expression input) throws IOException {
        SourceCode<WrappedStatement> code = SourceCode.from(input);
        GeneratedMethod gen = new GeneratedMethod(env);
        gen.addChild(code);
        
        Optional<? extends java.lang.reflect.Method> opt = METHOD_COMPILER.execute(env, gen);
        if (opt.isPresent()) {
            java.lang.reflect.Method m = opt.get();
            Object receiver;
            if (Modifier.isStatic(m.getModifiers())) {
                receiver = null;
            } else {
                Class<?> clazz = m.getDeclaringClass();
                Injector i = Guice.createInjector(new GeneratorModule(env));
                receiver = i.getInstance(clazz);
            }

            return Optional.of(Invokable.from(receiver, m));
        } else {
            return Optional.empty();
        }
    }
}
