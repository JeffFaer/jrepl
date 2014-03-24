package falgout.jrepl.command.execute.codegen;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Variable;

public final class WrappedStatement {
    private Statement statement;
    private Expression expression;
    private Variable<?> variable;
    
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
    
    /**
     * Returns the variable
     *
     * @param variable
     */
    WrappedStatement(Variable<?> variable) {
        this.variable = variable;
    }
    
    public boolean isReturn() {
        return statement instanceof ReturnStatement || expression != null || variable != null;
    }
    
    @Override
    public String toString() {
        if (statement != null) {
            return statement.toString();
        } else {
            StringBuilder b = new StringBuilder("return ");
            if (expression == null) {
                b.append(variable.getName());
            } else {
                b.append(expression);
            }
            b.append(";");
            return b.toString();
        }
    }
}
