package falgout.jrepl.command.execute;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import falgout.jrepl.Environment;
import falgout.jrepl.command.execute.codegen.CodeRepository;
import falgout.jrepl.command.execute.codegen.MethodSourceCode;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;

public class MethodDefiner extends RepositoryDefiner<MethodDeclaration, Method> {
    public static final MethodDefiner INSTANCE = new MethodDefiner();
    
    @Override
    protected NamedSourceCode<? extends Method> getSourceCode(MethodDeclaration node) throws ClassNotFoundException {
        MethodSourceCode.Builder b = MethodSourceCode.builder().initialize(MethodSourceCode.get(node));
        return b.addModifier(Modifier.STATIC).build();
    }
    
    @Override
    protected CodeRepository<Method> getRepository(Environment env) {
        return env.getMethodRepository();
    }
}
