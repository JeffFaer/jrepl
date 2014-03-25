package falgout.jrepl.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import falgout.jrepl.Environment;
import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.execute.AbstractExecutor;
import falgout.jrepl.command.execute.ClassDefiner;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.execute.Importer;
import falgout.jrepl.command.execute.StatementExecutor;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.command.parse.Statements;
import falgout.util.Optionals;

public class CommandModule extends AbstractModule {
    private static final TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>> PROVIDED = new TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>>() {};
    
    @Override
    protected void configure() {
        bind(CommandFactory.class).to(PROVIDED);
        bind(new TypeLiteral<CommandFactory<?>>() {}).to(PROVIDED);
    }
    
    @Provides
    public CommandFactory<? extends Collection<? extends Optional<?>>> createCommandFactory(
            StatementExecutor statementExec) {
        AbstractExecutor<CompilationUnit, List<? extends Optional<?>>> units = new AbstractExecutor<CompilationUnit, List<? extends Optional<?>>>() {
            @Override
            public List<? extends Optional<?>> execute(Environment env, CompilationUnit input)
                    throws ExecutionException {
                List<ImportDeclaration> imports = input.imports();
                List<AbstractTypeDeclaration> decl = input.types();
                
                List<Optional<?>> ret = new ArrayList<>();
                ret.addAll(Optionals.optionalize(Importer.INSTANCE.execute(env, imports)));
                ret.addAll(Optionals.optionalize(ClassDefiner.INSTANCE.execute(env, decl)));
                return ret;
            }
        };
        
        Executor<Iterable<? extends CompilationUnit>, List<? extends List<? extends Optional<?>>>> unitExec = units::execute;
        
        return new JavaCommandFactory<>(
                new Pair<>(ClassDeclaration.INSTANCE, unitExec.andThen(flatten(ArrayList::new))), new Pair<>(
                        Statements.INSTANCE, statementExec::execute));
    }
    
    private <T, C extends Collection<? extends T>, C2 extends Collection<T>> Function<Collection<? extends C>, C2> flatten(
            Supplier<C2> init) {
        return cs -> {
            C2 ret = init.get();
            for (C c : cs) {
                ret.addAll(c);
            }
            return ret;
        };
    }
}
