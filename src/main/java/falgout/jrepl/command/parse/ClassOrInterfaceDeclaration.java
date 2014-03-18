package falgout.jrepl.command.parse;

import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.ClassOrInterfaceDeclarationContext;

public class ClassOrInterfaceDeclaration implements JavaParserRule<ClassOrInterfaceDeclarationContext> {
    // TODO
    // classOrInterfaceDeclaration executor
    @Override
    public ClassOrInterfaceDeclarationContext parse(JavaParser input) {
        return input.classOrInterfaceDeclaration();
    }
}
