package falgout.jrepl.command;

import java.util.Collection;
import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.execute.Importer;
import falgout.jrepl.command.execute.LocalVariableDeclarer;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.command.parse.Statements;

public class CommandModule extends AbstractModule {
    private static final TypeLiteral<CommandFactory<Optional<? extends Collection<?>>>> PROVIDED = new TypeLiteral<CommandFactory<Optional<? extends Collection<?>>>>() {};

    @Override
    protected void configure() {
        bind(CommandFactory.class).to(PROVIDED);
        bind(new TypeLiteral<CommandFactory<?>>() {}).to(PROVIDED);
    }

    @Provides
    public CommandFactory<Optional<? extends Collection<?>>> createCommandFactory() {
        return new JavaCommandFactory<Collection<?>>(new Pair<>(Statements.INSTANCE, LocalVariableDeclarer.PARSE),
                new Pair<>(ClassDeclaration.INSTANCE, Importer.PARSE));
    }
}
