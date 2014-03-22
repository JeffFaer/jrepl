package falgout.jrepl.command.parse;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public enum Statements implements JavaParserRule<Statement> {
    INSTANCE;
    @Override
    public List<? extends Statement> parse(ASTParser input) {
        input.setKind(ASTParser.K_STATEMENTS);
        return ((Block) input.createAST(null)).statements();
    }
}
