package falgout.jrepl.command.parse;

import falgout.jrepl.command.parse.JavaParser.BlockStatementContext;
import falgout.jrepl.command.parse.JavaParser.BlockStatementsContext;

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
