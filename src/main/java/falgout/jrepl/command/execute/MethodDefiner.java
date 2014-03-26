package falgout.jrepl.command.execute;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.SourceCode;

public class MethodDefiner extends RepositoryDefiner<MethodDeclaration, Method> {
    public static final MethodDefiner INSTANCE = new MethodDefiner();
    
    @Override
    protected SourceCode<? extends Method> getSourceCode(MethodDeclaration node) {
        return SourceCode.from(node);
    }
    
    @Override
    protected CodeRepository<Method> getRepository(Environment env) {
        return env.getMethodRepository();
    }
}
