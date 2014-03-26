package falgout.jrepl.command;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import falgout.jrepl.command.AbstractCommandFactory.Pair;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.parse.ClassBodyDeclarations;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.command.parse.ExpressionParser;
import falgout.jrepl.command.parse.Statements;

public class CommandModule extends AbstractModule {
    private static final TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>> PROVIDED = new TypeLiteral<CommandFactory<? extends Collection<? extends Optional<?>>>>() {};
    
    @Override
    protected void configure() {
        bind(CommandFactory.class).to(PROVIDED);
        bind(new TypeLiteral<CommandFactory<?>>() {}).to(PROVIDED);
    }
    
    @SuppressWarnings("unchecked")
    @Provides
    public CommandFactory<? extends Collection<? extends Optional<?>>> createCommandFactory(
            Executor<Expression, Object> expressionExec,
            Executor<CompilationUnit, List<? extends Optional<?>>> compilationExec,
            Executor<Block, List<? extends Optional<?>>> statementExec,
            Executor<Iterable<? extends MethodDeclaration>, List<? extends Method>> methodDefiner) {
        Executor<Expression, Collection<? extends Optional<?>>> exp = expressionExec.andThen(Optional::ofNullable)
                .andThen(Collections::singleton);
        Executor<TypeDeclaration, List<? extends Optional<?>>> methods = (env, input) -> {
            List<? extends BodyDeclaration> decl = ((List<BodyDeclaration>) input.bodyDeclarations()).stream()
                    .filter(d -> d instanceof MethodDeclaration)
                    .collect(toList());
            List<Optional<?>> opt = new ArrayList<>();
            for (Method m : methodDefiner.execute(env, (Iterable<? extends MethodDeclaration>) decl)) {
                opt.add(Optional.of(m));
            }
            return opt;
        };
        
        return new JavaCommandFactory<>(new Pair<>(ExpressionParser.INSTANCE, exp), new Pair<>(
                ClassDeclaration.INSTANCE, compilationExec), new Pair<>(Statements.INSTANCE, statementExec),
                new Pair<>(ClassBodyDeclarations.INSTANCE, methods));
    }
}
