package falgout.jrepl.command.execute;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;
import falgout.jrepl.util.ThrowingBiFunction;

public abstract class RepositoryDefiner<D extends ASTNode, M extends Member> extends AbstractBatchExecutor<D, M> {
    private final ThrowingBiFunction<Environment, D, NamedSourceCode<? extends M>, ClassNotFoundException> convertor;
    
    protected RepositoryDefiner(
            ThrowingBiFunction<Environment, D, NamedSourceCode<? extends M>, ClassNotFoundException> convertor) {
        this.convertor = convertor;
    }
    
    @Override
    public List<? extends M> execute(Environment env, Collection<? extends D> input) throws ExecutionException {
        List<? extends NamedSourceCode<? extends M>> code;
        try {
            code = convertor.apply(env, input);
        } catch (ClassNotFoundException e) {
            throw new ExecutionException(e);
        }
        
        Optional<? extends List<? extends M>> opt = getRepository(env).compile(env, code);
        if (opt.isPresent()) {
            return opt.get();
        }
        
        for (NamedSourceCode<? extends M> c : code) {
            String name = c.getName();
            if (getRepository(env).contains(c.getName())) {
                String message = String.format("%s already exists.", name);
                throw new ExecutionException(new IllegalArgumentException(message));
            }
        }
        throw new AssertionError("One of the members must have already existed.");
    }
    
    protected abstract CodeRepository<M> getRepository(Environment env);
}
