package falgout.jrepl.command.execute;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

import falgout.jrepl.Environment;

public class ExpressionExecutor implements Executor<Expression, Object> {
    public static final Executor<Expression, Object> INSTANCE = new ExpressionExecutor();

    @Override
    public Optional<? extends Object> execute(Environment env, Expression input) {

        // TODO Auto-generated method stub
        return Optional.empty();
    }
}
