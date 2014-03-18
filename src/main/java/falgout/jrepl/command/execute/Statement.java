package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.List;

import falgout.jrepl.Environment;
import falgout.jrepl.command.parse.JavaParser.BlockStatementsContext;
import falgout.jrepl.command.parse.JavaParser.StatementContext;

public class Statement extends PartialRuleExecutor<BlockStatementsContext, StatementContext, Object, List<Object>> {
    public static Statement INSTANCE = new Statement();
    
    public Statement() {
        super(StatementContext.class, 3);
    }
    
    @Override
    public Object doExecute(Environment env, StatementContext input) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected List<Object> fold(List<Object> results) {
        return results;
    }
}
