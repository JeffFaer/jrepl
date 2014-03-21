package falgout.jrepl.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.Executor;
import falgout.jrepl.command.execute.Importer;
import falgout.jrepl.command.execute.LocalVariableDeclarer;
import falgout.jrepl.command.parse.ClassDeclaration;
import falgout.jrepl.command.parse.JavaParserRule;
import falgout.jrepl.command.parse.Parser;
import falgout.jrepl.command.parse.Statements;

@Singleton
public class JavaCommandFactory implements CommandFactory {
    private static class Intermediate<M extends ASTNode, R extends Collection<?>> implements
            Parser<ASTParser, IProblem[]>, Command<Optional<? extends R>> {
        private final JavaParserRule<? extends M> parser;
        private final Executor<? super List<? extends M>, ? extends R> executor;
        private List<? extends M> intermediary;

        @SafeVarargs
        public Intermediate(JavaParserRule<? extends M> parser,
                Executor<? super List<? extends M>, ? extends R>... executors) {
            this.parser = parser;
            this.executor = Executor.sequence(executors);
        }

        @Override
        public IProblem[] parse(ASTParser input) {
            intermediary = parser.parse(input);
            if (intermediary.size() == 0) {
                return null;
            } else {
                return ((CompilationUnit) intermediary.get(0).getRoot()).getProblems();
            }
        }
        
        @Override
        public Optional<? extends R> execute(Environment env) throws IOException {
            return executor.execute(env, intermediary);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Intermediate [parser=");
            builder.append(parser);
            builder.append("]");
            return builder.toString();
        }
    }
    
    private final List<Intermediate<?, ?>> intermediates;
    
    @Inject
    public JavaCommandFactory() {
        intermediates = Arrays.asList(new Intermediate<>(new Statements(), LocalVariableDeclarer.PARSE),
                new Intermediate<>(new ClassDeclaration(), Importer.PARSE));
    }
    
    @Override
    public Command<? extends Optional<? extends Collection<?>>> getCommand(Environment env, String input) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);

        char[] source = input.toCharArray();
        Map<?, ?> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        
        int min = Integer.MAX_VALUE;
        List<IProblem[]> problems = new ArrayList<>();
        for (Intermediate<?, ?> i : intermediates) {
            parser.setCompilerOptions(options);
            parser.setSource(source);

            IProblem[] temp = i.parse(parser);
            if (temp != null) {
                if (temp.length == 0) {
                    return i;
                } else if (temp.length < min) {
                    problems.clear();
                    min = temp.length;
                }
                
                if (temp.length <= min) {
                    problems.add(temp);
                }
            }
        }
        
        for (IProblem[] p : problems) {
            for (IProblem i : p) {
                env.getError().println(i.getMessage());

                int start = i.getSourceStart();
                if (start != -1) {
                    int count = i.getSourceEnd() - start + 1;
                    String problem = new String(source, start, count);
                    if (!problem.isEmpty()) {
                        env.getError().printf("\t%s\n", problem);
                    }
                }
            }
        }
        
        return null;
    }
}
