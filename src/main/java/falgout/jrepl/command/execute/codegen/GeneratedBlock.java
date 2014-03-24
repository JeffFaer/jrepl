package falgout.jrepl.command.execute.codegen;

import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Environment;
import falgout.jrepl.reflection.Block;

public class GeneratedBlock extends GeneratedSourceCode<Block, Statement> {
    private final boolean _static;
    
    public GeneratedBlock(Environment env, boolean _static) {
        super(env);
        this._static = _static;
    }
    
    @Override
    public Block getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return new Block(clazz, _static);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (_static) {
            b.append("static ");
        }
        b.append("{\n");
        b.append(addTabsToChildren("", "\n", ""));
        b.append("}");
        
        return b.toString();
    }
}
