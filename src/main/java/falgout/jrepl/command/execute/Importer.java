package falgout.jrepl.command.execute;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import falgout.jrepl.Environment;

public class Importer implements Executor<ImportDeclaration, Boolean> {
    public static final Collector<Boolean, ?, Boolean> AND = Collectors.reducing(true, (b1, b2) -> b1 && b2);
    public static final Executor<ImportDeclaration, Boolean> INSTANCE = new Importer();
    public static final Executor<Iterable<? extends ImportDeclaration>, Boolean> LIST = Executor.process(INSTANCE, AND);
    public static final Executor<CompilationUnit, Boolean> FILTERED = Executor.filter(LIST,
            new Function<CompilationUnit, List<ImportDeclaration>>() {
        @Override
        public List<ImportDeclaration> apply(CompilationUnit t) {
            return t.imports();
        }
    });
    public static final Executor<Iterable<? extends CompilationUnit>, Boolean> PARSE = Executor.process(FILTERED, AND);

    @Override
    public Optional<Boolean> execute(Environment env, ImportDeclaration input) throws IOException {
        // TODO
        return Optional.of(true);
    }
}
