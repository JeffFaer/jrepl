package falgout.jrepl.command.execute;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.ASTNode;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.SourceCode;

public abstract class RepositoryDefiner<D extends ASTNode, M extends Member> extends BatchExecutor<D, M> {
    protected RepositoryDefiner() {}
    
    @Override
    public List<? extends M> execute(Environment env, Iterable<? extends D> input) throws ExecutionException {
        List<SourceCode<? extends M>> code = new ArrayList<>();
        input.forEach(d -> code.add(getSourceCode(d)));
        Optional<? extends List<? extends M>> opt = getRepository(env).compile(env, code);
        if (opt.isPresent()) {
            return opt.get();
        }
        
        for (SourceCode<? extends M> c : code) {
            String name = c.getName();
            if (env.getClassRepository().contains(c.getName())) {
                String message = String.format("%s already exists.", name);
                throw new ExecutionException(new IllegalArgumentException(message));
            }
        }
        throw new AssertionError("One of the members must have already existed.");
    }
    
    protected abstract SourceCode<? extends M> getSourceCode(D node);
    
    protected abstract CodeRepository<M> getRepository(Environment env);
}
