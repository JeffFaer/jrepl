package falgout.jrepl.command.parse;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

public enum Statements implements JavaParserRule<Block> {
    INSTANCE;
    @Override
    public Block parse(ASTParser input) {
        input.setKind(ASTParser.K_STATEMENTS);
        ASTNode node = input.createAST(null);
        return node instanceof Block ? (Block) node : null;
    }
}
