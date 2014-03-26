package falgout.jrepl.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.command.parse.Statements;

public class CommandModule extends AbstractModule {
    private static final TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>> PROVIDED = new TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>>() {};
    
    @Override
    protected void configure() {
        bind(CommandFactory.class).to(PROVIDED);
        bind(new TypeLiteral<CommandFactory<?>>() {}).to(PROVIDED);
    }
    
    @Provides
    public CommandFactory<? extends Collection<? extends Optional<?>>> createCommandFactory(
            Executor<Iterable<? extends CompilationUnit>, List<? extends List<? extends Optional<?>>>> compilationExec,
            Executor<Iterable<? extends Statement>, List<? extends Optional<?>>> statementExec) {
        
        return new JavaCommandFactory<>(new Pair<>(ClassDeclaration.INSTANCE,
                compilationExec.andThen(flatten(ArrayList::new))), new Pair<>(Statements.INSTANCE, statementExec));
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
