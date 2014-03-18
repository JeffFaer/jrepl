package falgout.jrepl.command.parse;

import falgout.jrepl.parser.JavaParser;
import falgout.jrepl.parser.JavaParser.ImportDeclarationContext;

public class ImportDeclaration implements JavaParserRule<ImportDeclarationContext> {
    @Override
    public ImportDeclarationContext parse(JavaParser input) {
        return input.importDeclaration();
    }
}
