package falgout.jrepl.command.execute;

import java.io.IOException;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.command.parse.JavaParser.ImportDeclarationContext;

public class ImportExecutor implements Executor<ImportDeclarationContext, Void> {
    @Override
    public Void execute(Environment env, ImportDeclarationContext input) throws IOException {
        env.getImports().add(Import.create(input));
        return null;
    }
}
