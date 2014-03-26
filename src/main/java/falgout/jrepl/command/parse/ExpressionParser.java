package falgout.jrepl.command.parse;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;

public enum ExpressionParser implements JavaParserRule<Expression> {
    INSTANCE;
    @Override
    public Expression parse(ASTParser input) {
        input.setKind(ASTParser.K_EXPRESSION);
        ASTNode n = input.createAST(null);
        if (n instanceof Expression) {
            return (Expression) n;
        }
        return null;
    }
}
