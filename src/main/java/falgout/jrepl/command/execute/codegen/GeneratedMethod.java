package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.reflection.GoogleTypes;

public class GeneratedMethod extends GeneratedSourceCode<Method, WrappedStatement> {
    public GeneratedMethod(Environment env) {
        super(env);
    }
    
    @Override
    public Method getTarget(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod(getName());
    }
    
    public TypeToken<?> getReturnType() {
        for (SourceCode<? extends WrappedStatement> child : getChildren()) {
            // still kind of hacky, but it's a bit better
            try {
                if (child.getTarget(null).isReturn()) {
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
        b.append("public ").append(getReturnType()).append(" ").append(getName());
        b.append("() throws Throwable {\n");
        for (SourceCode<?> child : getChildren()) {
            for (String line : child.toString().split("\n")) {
                b.append(TAB).append(line).append("\n");
            }
        }
        b.append("}");
        return b.toString();
    }
}
