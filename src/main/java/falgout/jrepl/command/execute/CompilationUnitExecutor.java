package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.reflection.NestedClass;
import falgout.util.Optionals;

@Singleton
public class CompilationUnitExecutor extends AbstractExecutor<CompilationUnit, List<? extends Optional<?>>> {
    private final Executor<Iterable<? extends ImportDeclaration>, List<? extends Import>> importer;
    private final Executor<Iterable<? extends AbstractTypeDeclaration>, List<? extends NestedClass<?>>> classDefiner;
    
    @Inject
    public CompilationUnitExecutor(Executor<Iterable<? extends ImportDeclaration>, List<? extends Import>> importer,
            Executor<Iterable<? extends AbstractTypeDeclaration>, List<? extends NestedClass<?>>> classDefiner) {
        this.importer = importer;
        this.classDefiner = classDefiner;
    }
    
    @Override
    public List<? extends Optional<?>> execute(Environment env, CompilationUnit input) throws ExecutionException {
        List<ImportDeclaration> imports = input.imports();
        List<AbstractTypeDeclaration> decl = input.types();
        
        List<Optional<?>> ret = new ArrayList<>();
        ret.addAll(Optionals.optionalize(importer.execute(env, imports)));
        ret.addAll(Optionals.optionalize(classDefiner.execute(env, decl)));
        return ret;
    }
}
