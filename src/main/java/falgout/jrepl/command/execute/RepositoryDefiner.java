package falgout.jrepl.command.execute;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;

public abstract class RepositoryDefiner<D extends ASTNode, M extends Member> extends BatchExecutor<D, M> {
    protected RepositoryDefiner() {}
    
    @Override
    public List<? extends M> execute(Environment env, Collection<? extends D> input) throws ExecutionException {
        List<NamedSourceCode<? extends M>> code = new ArrayList<>();
        for (D d : input) {
            try {
                code.add(getSourceCode(d));
            } catch (ClassNotFoundException e) {
                throw new ExecutionException(e);
            }
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
    
    protected abstract NamedSourceCode<? extends M> getSourceCode(D node) throws ClassNotFoundException;
    
    protected abstract CodeRepository<M> getRepository(Environment env);
}
