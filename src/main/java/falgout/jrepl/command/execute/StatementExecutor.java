package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.util.Optionals;

public class StatementExecutor extends BatchExecutor<Statement, Optional<?>> {
    private final LocalVariableDeclarer variableDeclarer;
    
    @Inject
    public StatementExecutor(LocalVariableDeclarer variableDeclarer) {
        this.variableDeclarer = variableDeclarer;
    }
    
    @Override
    public List<? extends Optional<?>> execute(Environment env, Iterable<? extends Statement> input)
            throws ExecutionException {
        List<Statement> statements = new ArrayList<>();
        Class<?> current = null;
        
        List<Optional<?>> ret = new ArrayList<>();
        for (Statement st : input) {
            Class<?> temp;
            if (st instanceof VariableDeclarationStatement) {
                temp = VariableDeclarationStatement.class;
            } else if (st instanceof ExpressionStatement) {
                temp = ExpressionStatement.class;
            } else {
                temp = Statement.class;
            }
            
            if (temp == current) {
                statements.add(st);
            } else {
                execute(env, current, statements).ifPresent(l -> ret.addAll(Optionals.optionalize(l)));
                
                current = temp;
                statements.clear();
                statements.add(st);
            }
        }
        execute(env, current, statements).ifPresent(l -> ret.addAll(Optionals.optionalize(l)));
        
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<List<Object>> execute(Environment env, Class<?> current, List<Statement> statements)
            throws ExecutionException {
        if (current == null || statements.isEmpty()) {
            return Optional.empty();
        }
        
        if (current == Statement.class) {
            env.getError().println("Not yet implemented.");
            return Optional.empty();
        }
        
        List<Object> ret = new ArrayList<>();
        List<? extends Statement> l = statements;
        if (current == VariableDeclarationStatement.class) {
            variableDeclarer.execute(env, (List<VariableDeclarationStatement>) l).forEach(ret::addAll);
        } else if (current == ExpressionStatement.class) {
            env.getError().println("Not yet implemented.");
        } else {
            throw new AssertionError("Should only have been one of these three types.");
        }
        
        return Optional.of(ret);
    }
}
