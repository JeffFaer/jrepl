package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.reflection.GoogleTypes;

public class GeneratedMethod extends GeneratedSourceCode<Method, Statement> {
    public GeneratedMethod(Environment env) {
        super(env);
    }
    
    @Override
    public Method getTarget(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(getName());
    }
    
    public TypeToken<?> getReturnType() {
        for (SourceCode<? extends Statement> child : getChildren()) {
            // still kind of hacky, but it's a bit better
            try {
                if (child.getTarget(null) instanceof ReturnStatement) {
                    return GoogleTypes.OBJECT;
                }
            } catch (ReflectiveOperationException e) {
                // this shouldn't happen. The package controls the creation of
                // SourceCode<WrappedStatement> and WrappedStatement
                throw new Error(e);
            }
        }
        
        return GoogleTypes.VOID;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("public static ").append(getReturnType()).append(" ").append(getName());
        b.append("() throws Throwable {\n");
        b.append(addTabsToChildren());
        b.append("}");
        return b.toString();
    }
}
