package falgout.jrepl.command.execute;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;

public class Importer implements Executor<ImportDeclaration, Import> {
    public static final Executor<ImportDeclaration, Import> INSTANCE = new Importer();
    public static final Executor<Iterable<? extends ImportDeclaration>, List<Import>> LIST = Executor.process(INSTANCE);
    public static final Executor<CompilationUnit, List<Import>> FILTERED = Executor.filter(LIST,
            new Function<CompilationUnit, List<ImportDeclaration>>() {
        @Override
        public List<ImportDeclaration> apply(CompilationUnit t) {
            return t.imports();
        }
    });
    public static final Executor<Iterable<? extends CompilationUnit>, List<Import>> PARSE = Executor.flatProcess(FILTERED);

    @Override
    public Optional<Import> execute(Environment env, ImportDeclaration input) {
        Import _import = Import.create(input);
        env.getImports().add(_import);
        return Optional.of(_import);
    }
}
