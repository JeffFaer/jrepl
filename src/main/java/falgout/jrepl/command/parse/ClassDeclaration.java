package falgout.jrepl.command.parse;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public enum ClassDeclaration implements JavaParserRule<CompilationUnit> {
    INSTANCE;
    @Override
    public CompilationUnit parse(ASTParser input) {
        input.setKind(ASTParser.K_COMPILATION_UNIT);
        return (CompilationUnit) input.createAST(null);
    }
}
