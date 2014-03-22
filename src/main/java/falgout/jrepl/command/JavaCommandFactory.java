package falgout.jrepl.command;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.GeneratedSourceCode;

@Singleton
public class JavaCommandFactory<R> extends AbstractCommandFactory<ASTParser, List<? extends ASTNode>, R> {
    private static final Function<List<? extends ASTNode>, IProblem[]> CONVERTOR = l -> {
        if (l.size() == 0) {
            return null;
        } else {
            return ((CompilationUnit) l.get(0).getRoot()).getProblems();
        }
    };
    private static Predicate<List<? extends ASTNode>> ACCEPT = l -> {
        IProblem[] problems = CONVERTOR.apply(l);
        return problems != null && problems.length == 0;
    };
    private static Comparator<List<? extends ASTNode>> RANKER = (l1, l2) -> {
        IProblem[] p1 = CONVERTOR.apply(l1);
        IProblem[] p2 = CONVERTOR.apply(l2);
        int s1 = p1 == null ? Integer.MAX_VALUE : p1.length;
        int s2 = p2 == null ? Integer.MAX_VALUE : p2.length;

        return Integer.compare(s1, s2);
    };

    private final Map<?, ?> options;
    private final ThreadLocal<char[]> source = new ThreadLocal<char[]>();

    @SafeVarargs
    @Inject
    public JavaCommandFactory(Pair<? super ASTParser, ? extends List<? extends ASTNode>, ? extends R>... pairs) {
        super(ACCEPT, RANKER, pairs);
        options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
    }
    
    @Override
    protected ASTParser createNewInput() {
        source.set(null);
        return ASTParser.newParser(AST.JLS4);
    }

    @Override
    protected ASTParser initialize(ASTParser blank, String input) {
        char[] s = source.get();
        if (s == null) {
            s = input.toCharArray();
            source.set(s);
        }
        blank.setCompilerOptions(options);
        blank.setSource(s);
        return blank;
    }

    @Override
    protected void reportSuccess(Environment env, List<? extends ASTNode> success) {
        source.set(null);
    }

    @Override
    protected ParsingException createParsingException(List<? extends List<? extends ASTNode>> min) {
        StringBuilder message = new StringBuilder();
        for (List<? extends ASTNode> nodes : min) {
            IProblem[] ps = CONVERTOR.apply(nodes);
            for (IProblem p : ps) {
                message.append(p.getMessage()).append("\n");
                
                int start = p.getSourceStart();
                if (start != -1) {
                    int count = p.getSourceEnd() - start + 1;
                    if (count > 0) {
                        String problem = new String(source.get(), start, count);
                        message.append(GeneratedSourceCode.TAB).append(problem).append("\n");
                    }
                }
            }
        }
        
        source.set(null);
        
        return new ParsingException(message.toString().trim());
    }
}
