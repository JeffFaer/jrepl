package falgout.jrepl.command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

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
            Executor<CompilationUnit, List<? extends Optional<?>>> compilationExec,
            Executor<Block, List<? extends Optional<?>>> statementExec) {
        
        return new JavaCommandFactory<>(new Pair<>(ClassDeclaration.INSTANCE, compilationExec), new Pair<>(
                Statements.INSTANCE, statementExec));
    }
}
