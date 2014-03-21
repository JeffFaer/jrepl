package falgout.jrepl.command.execute.codegen;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

public final class WrappedStatement {
    private Statement statement;
    private Expression expression;
    
    WrappedStatement(Statement statement) {
        this.statement = statement;
    }

    /**
     * Creates a return statement out of the expression.
     *
     * @param expression
     */
    WrappedStatement(Expression expression) {
        this.expression = expression;
    }

    public boolean isReturn() {
        return statement instanceof ReturnStatement || expression != null;
    }

    @Override
    public String toString() {
        if (statement != null) {
            return statement.toString();
        } else {
            StringBuilder b = new StringBuilder("return ");
            b.append(expression).append(";");
            return b.toString();
        }
    }
}
