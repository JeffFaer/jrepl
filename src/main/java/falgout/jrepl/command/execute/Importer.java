package falgout.jrepl.command.execute;

import org.eclipse.jdt.core.dom.ImportDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;

public class Importer extends AbstractExecutor<ImportDeclaration, Import> {
    public static final Importer INSTANCE = new Importer();
    
    @Override
    public Import execute(Environment env, ImportDeclaration input) {
        Import _import = Import.create(input);
        env.getImports().add(_import);
        return _import;
    }
}
