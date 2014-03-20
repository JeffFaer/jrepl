package falgout.jrepl.command.parse;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ClassBodyDeclarations implements JavaParserRule<BodyDeclaration> {
    @Override
    public List<? extends BodyDeclaration> parse(ASTParser input) {
        input.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        return ((TypeDeclaration) input.createAST(null)).bodyDeclarations();
    }
}
