package falgout.jrepl.command.execute;

import static falgout.jrepl.command.execute.codegen.MemberCompiler.NESTED_CLASS_COMPILER;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
            new Function<CompilationUnit, List<AbstractTypeDeclaration>>() {
        @Override
        public List<AbstractTypeDeclaration> apply(CompilationUnit t) {
            return t.types();
        }
    });
    public static final Executor<Iterable<? extends CompilationUnit>, Optional<? extends List<Class<?>>>> PARSE = Executor.flatProcess(FILTERED);
    
    @Override
    public Class<?> execute(Environment env, AbstractTypeDeclaration input) throws ExecutionException {
        SourceCode<NestedClass<?>> code = SourceCode.from(input);
        String name = code.getName();
        if(env.containsClass(name)) {
            String message = String.format("%s already exists.", name);
            throw new ExecutionException(new IllegalArgumentException(message));
        }
        
        NestedClass<?> clazz = NESTED_CLASS_COMPILER.execute(env, code);
        env.addClass(clazz);
        return clazz.getDeclaredClass();
    }
}
