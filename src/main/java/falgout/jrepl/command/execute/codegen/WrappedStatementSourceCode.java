package falgout.jrepl.command.execute.codegen;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Variable;

class WrappedStatementSourceCode extends SourceCode<WrappedStatement> {
    private final WrappedStatement w;
    
    WrappedStatementSourceCode(WrappedStatement w) {
        super(null);
        this.w = w;
    }
    
    WrappedStatementSourceCode(Expression expression) {
        this(new WrappedStatement(expression));
    }
    
    WrappedStatementSourceCode(Statement statement) {
        this(new WrappedStatement(statement));
    }
    
    WrappedStatementSourceCode(Variable<?> variable) {
        this(new WrappedStatement(variable));
    }
    
    @Override
    public WrappedStatement getTarget(Class<?> clazz) {
        return w;
    }
    
    @Override
    public String toString() {
        return w.toString();
    }
}
