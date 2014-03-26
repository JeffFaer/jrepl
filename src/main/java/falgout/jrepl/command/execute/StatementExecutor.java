package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.LocalVariable;
import falgout.util.Optionals;

public class StatementExecutor extends AbstractExecutor<Block, List<? extends Optional<?>>> {
    private final Executor<Iterable<? extends VariableDeclarationStatement>, List<? extends List<LocalVariable<?>>>> variableDeclarer;
    private final Executor<Iterable<? extends Expression>, List<? extends Object>> expressionExecutor;
    
    @Inject
    public StatementExecutor(
            Executor<Iterable<? extends VariableDeclarationStatement>, List<? extends List<LocalVariable<?>>>> variableDeclarer,
            Executor<Iterable<? extends Expression>, List<? extends Object>> expressionExecutor) {
        this.variableDeclarer = variableDeclarer;
        this.expressionExecutor = expressionExecutor;
    }
    
    @Override
    public List<? extends Optional<?>> execute(Environment env, Block input) throws ExecutionException {
        List<Statement> statements = new ArrayList<>();
        Class<?> current = null;
        
        List<Optional<?>> ret = new ArrayList<>();
        for (Statement st : (List<Statement>) input.statements()) {
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
            List<Expression> expressions = l.stream()
                    .map(s -> ((ExpressionStatement) s).getExpression())
                    .collect(Collectors.toList());
            expressionExecutor.execute(env, expressions).forEach(ret::add);
        } else {
            throw new AssertionError("Should only have been one of these three types.");
        }
        
        return Optional.of(ret);
    }
}
