package falgout.jrepl.command.parse;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public enum ClassBodyDeclarations implements JavaParserRule<TypeDeclaration> {
    INSTANCE;
    @Override
    public TypeDeclaration parse(ASTParser input) {
        input.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        return (TypeDeclaration) input.createAST(null);
    }
}
