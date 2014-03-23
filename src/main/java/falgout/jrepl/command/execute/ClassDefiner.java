package falgout.jrepl.command.execute;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.reflection.NestedClass;

public enum ClassDefiner implements Executor<AbstractTypeDeclaration, Class<?>> {
    INSTANCE;
    public static final Executor<AbstractTypeDeclaration, Optional<? extends Class<?>>> OPT = Executor.optional(INSTANCE);
    public static final Executor<Iterable<? extends AbstractTypeDeclaration>, List<Class<?>>> LIST = Executor.process(OPT);
    public static final Executor<CompilationUnit, Optional<? extends List<Class<?>>>> FILTERED = Executor.filter(LIST,
            t -> {
                List<AbstractTypeDeclaration> l = t.types();
                return l;
            });
    public static final Executor<Iterable<? extends CompilationUnit>, Optional<? extends List<Class<?>>>> PARSE = Executor.flatProcess(FILTERED);
    
    @Override
    public Class<?> execute(Environment env, AbstractTypeDeclaration input) throws ExecutionException {
        SourceCode<NestedClass<?>> code = SourceCode.from(input);
        Optional<? extends NestedClass<?>> opt = env.compile(code);
        if (opt.isPresent()) {
            return opt.get().getDeclaredClass();
        }
        
        String message = String.format("%s already exists.", code.getName());
        throw new ExecutionException(new IllegalArgumentException(message));
    }
}
