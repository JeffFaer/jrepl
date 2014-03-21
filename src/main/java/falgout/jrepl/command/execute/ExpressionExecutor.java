package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.GeneratedMethod;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.command.execute.codegen.WrappedStatement;
import falgout.jrepl.reflection.Invokable;

public class ExpressionExecutor implements Executor<Expression, Invokable.Method> {
    public static final Executor<Expression, Invokable.Method> INSTANCE = new ExpressionExecutor();

    /**
     * Creates an {@link Invokable} which requires no arguments. When
     * {@link Invokable#invoke invoked}, it will return the value of the
     * expression.
     */
    @Override
    public Optional<? extends Invokable.Method> execute(Environment env, Expression input) throws IOException {
        SourceCode<WrappedStatement> code = SourceCode.from(input);
        GeneratedMethod gen = new GeneratedMethod(env);
        gen.addChild(code);

        return Invokable.from(env, gen);
    }
}
