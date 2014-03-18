package falgout.jrepl.command.parse;

import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.BlockStatementContext;
import falgout.jrepl.parser.JavaParser.BlockStatementsContext;

public class BlockStatements implements JavaParserRule<BlockStatementsContext> {
    // TODO
    // statement executor
    @Override
    public BlockStatementsContext parse(JavaParser input) {
        BlockStatementContext ctx = input.blockStatement();
        BlockStatementsContext ctx2 = input.blockStatements();
        
        if (ctx2.getChildCount() == 0) {
            ctx2.addChild(ctx);
        } else {
            ctx2.children.add(0, ctx);
        }
        return ctx2;
    }
}
