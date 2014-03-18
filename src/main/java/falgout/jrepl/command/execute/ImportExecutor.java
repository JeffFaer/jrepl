package falgout.jrepl.command.execute;

import java.io.IOException;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.parser.JavaParser.ImportDeclarationContext;

public class ImportExecutor implements Executor<ImportDeclarationContext> {
    @Override
    public boolean execute(Environment env, ImportDeclarationContext input) throws IOException {
        env.getImports().add(Import.create(input));
        return true;
    }
}
