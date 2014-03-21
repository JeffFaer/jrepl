package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;

import javax.lang.model.element.NestingKind;

import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.reflection.Types;

public class GeneratedMethod extends GeneratedSourceCode<Method, Statement> {
    public GeneratedMethod(Environment env) {
        super(env);
    }
    
    @Override
    public NestingKind getNestingKind() {
        return NestingKind.MEMBER;
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
        for (SourceCode<? extends Statement> child : getChildren()) {
            String statement = child.toString();
            // TODO come up with a better method, this feels hacky
            if (statement.matches("return[^\\s;]+;")) {
                return Types.OBJECT;
            }
        }

        return Types.VOID;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("public ").append(getReturnType()).append(" ").append(getName()).append("() {\n");
        for (SourceCode<? extends Statement> child : getChildren()) {
            for (String line : child.toString().split("\n")) {
                b.append(TAB).append(line).append("\n");
            }
        }
        b.append("}");
        return b.toString();
    }
}
