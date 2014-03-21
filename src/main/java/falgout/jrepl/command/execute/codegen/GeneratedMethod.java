package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;

import javax.lang.model.element.NestingKind;

import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Environment;

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
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return null;
    }
}
