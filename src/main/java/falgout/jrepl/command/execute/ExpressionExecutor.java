package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;

import falgout.jrepl.Environment;

public class ExpressionExecutor implements Executor<Expression, Object> {
    public static final Executor<Expression, Object> INSTANCE = new ExpressionExecutor();
    
    @Override
    public Optional<? extends Object> execute(Environment env, Expression input) throws IOException {
        
        // TODO Auto-generated method stub
        return Optional.empty();
    }
    
}
