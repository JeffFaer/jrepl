package falgout.jrepl.command.parse;

import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.ClassBodyDeclarationContext;

public class ClassBodyDeclaration implements JavaParserRule<ClassBodyDeclarationContext> {
    // TODO
    // semicolon
    // modifier* memberDecl
    // methodOrField (won't be field, LOCAL picks it up first)
    // voidMethodDeclarator
    // constructor (don't want this)
    // genericMethodOrConstructor (don' want constructor
    // static? block
    @Override
    public ClassBodyDeclarationContext parse(JavaParser input) {
        return input.classBodyDeclaration();
    }
}
