package falgout.jrepl.command.execute;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;
import falgout.jrepl.command.execute.codegen.TypeSourceCode;
import falgout.jrepl.reflection.NestedClass;

public class ClassDefiner extends RepositoryDefiner<AbstractTypeDeclaration, NestedClass<?>> {
    public static final ClassDefiner INSTANCE = new ClassDefiner();
    
    @Override
    protected NamedSourceCode<? extends NestedClass<?>> getSourceCode(AbstractTypeDeclaration node)
        throws ClassNotFoundException {
        
        return TypeSourceCode.get(node).asNestedClass(true);
    }
    
    @Override
    protected CodeRepository<NestedClass<?>> getRepository(Environment env) {
        return env.getClassRepository();
    }
}
