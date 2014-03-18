package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import falgout.jrepl.Environment;
import falgout.jrepl.antlr4.ParseTreeUtils;

public abstract class PartialRuleExecutor<I extends ParserRuleContext, C extends ParserRuleContext, R, F> implements
        Executor<I, F> {
    private final Class<C> clazz;
    private final int depth;
    
    protected PartialRuleExecutor(Class<C> clazz, int depth) {
        this.clazz = clazz;
        this.depth = depth;
    }
    
    @Override
    public F execute(Environment env, I input) throws IOException {
        List<R> ret = new ArrayList<>();
        for (C child : ParseTreeUtils.getChildren(input, clazz, depth)) {
            ret.add(doExecute(env, child));
        }
        
        return fold(ret);
    }
    
    public abstract R doExecute(Environment env, C input) throws IOException;
    
    protected abstract F fold(List<R> results);
}
