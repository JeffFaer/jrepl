package falgout.jrepl.command.parse;

import falgout.jrepl.command.parse.JavaParser.ImportDeclarationContext;

public class ImportDeclaration implements JavaParserRule<ImportDeclarationContext> {
    @Override
    public ImportDeclarationContext parse(JavaParser input) {
        return input.importDeclaration();
    }
}
