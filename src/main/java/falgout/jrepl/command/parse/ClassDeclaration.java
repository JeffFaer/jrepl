package falgout.jrepl.command.parse;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ClassDeclaration implements JavaParserRule<CompilationUnit> {
    @Override
    public List<? extends CompilationUnit> parse(ASTParser input) {
        input.setKind(ASTParser.K_COMPILATION_UNIT);
        return Collections.singletonList((CompilationUnit) input.createAST(null));
    }
}
