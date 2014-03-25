package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.reflection.NestedClass;

public class ClassDefiner extends BatchExecutor<AbstractTypeDeclaration, NestedClass<?>> {
    public static final ClassDefiner INSTANCE = new ClassDefiner();
    
    @Override
    public List<? extends NestedClass<?>> execute(Environment env, Iterable<? extends AbstractTypeDeclaration> input)
            throws ExecutionException {
        List<SourceCode<NestedClass<?>>> code = new ArrayList<>();
        input.forEach(decl -> code.add(SourceCode.from(decl)));
        Optional<? extends List<? extends NestedClass<?>>> opt = env.getClassRepository().compile(env, code);
        if (opt.isPresent()) {
            return opt.get();
        }
        
        for (SourceCode<NestedClass<?>> c : code) {
            String name = c.getName();
            if (env.getClassRepository().contains(c.getName())) {
                String message = String.format("%s already exists.", name);
                throw new ExecutionException(new IllegalArgumentException(message));
            }
        }
        throw new AssertionError("One of the classes must have already existed.");
    }
}
