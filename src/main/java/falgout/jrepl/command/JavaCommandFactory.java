package falgout.jrepl.command;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.GeneratedSourceCode;

public class JavaCommandFactory<R> extends AbstractCommandFactory<ASTParser, List<? extends ASTNode>, R> {
    private static final Function<List<? extends ASTNode>, IProblem[]> CONVERTOR = l -> {
        if (l.size() == 0) {
            return null;
        } else {
            return ((CompilationUnit) l.get(0).getRoot()).getProblems();
        }
    };
    private final Map<?, ?> options;
    private char[] source;

    @SafeVarargs
    @Inject
    public JavaCommandFactory(Pair<? super ASTParser, ? extends List<? extends ASTNode>, ? extends R>... pairs) {
        super(l -> {
            IProblem[] problems = CONVERTOR.apply(l);
            return problems != null && problems.length == 0;
        }, (l1, l2) -> {
            IProblem[] p1 = CONVERTOR.apply(l1);
            IProblem[] p2 = CONVERTOR.apply(l2);
            int s1 = p1 == null ? Integer.MAX_VALUE : p1.length;
            int s2 = p2 == null ? Integer.MAX_VALUE : p2.length;

            return Integer.compare(s1, s2);
        }, pairs);
        options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
    }
    
    @Override
    protected ASTParser createNewInput() {
        source = null;
        return ASTParser.newParser(AST.JLS4);
    }

    @Override
    protected ASTParser initialize(ASTParser blank, String input) {
        if (source == null) {
            source = input.toCharArray();
        }
        blank.setCompilerOptions(options);
        blank.setSource(source);
        return blank;
    }

    @Override
    protected void reportError(Environment env, List<? extends List<? extends ASTNode>> min) {
        for (List<? extends ASTNode> nodes : min) {
            IProblem[] ps = CONVERTOR.apply(nodes);
            for (IProblem p : ps) {
                env.getError().println(p.getMessage());

                int start = p.getSourceStart();
                if (start != -1) {
                    int count = p.getSourceEnd() - start + 1;
                    if (count > 0) {
                        String problem = new String(source, start, count);
                        env.getError().print(GeneratedSourceCode.TAB);
                        env.getError().println(problem);
                    }
                }
            }
        }
    }
}
