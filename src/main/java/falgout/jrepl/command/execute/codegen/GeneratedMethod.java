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
    public Method getTarget(Class<?> clazz) {
        try {
            return clazz.getMethod(getName());
        } catch (NoSuchMethodException e) {
            throw new Error("The method should have been created.", e);
        }
    }
    
    public TypeToken<?> getReturnType() {
        for (SourceCode<? extends WrappedStatement> child : getChildren()) {
            // still kind of hacky, but it's a bit better
            if (child.getTarget(null).isReturn()) {
                return GoogleTypes.OBJECT;
            }
        }
        
        return GoogleTypes.VOID;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("public ").append(getReturnType()).append(" ").append(getName()).append("() {\n");
        for (SourceCode<?> child : getChildren()) {
            for (String line : child.toString().split("\n")) {
                b.append(TAB).append(line).append("\n");
            }
        }
        b.append("}");
        return b.toString();
    }
}
