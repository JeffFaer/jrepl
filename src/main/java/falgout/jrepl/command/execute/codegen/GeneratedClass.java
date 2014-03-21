package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.NestingKind;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;

public class GeneratedClass extends GeneratedSourceCode<Class<?>, Member> {
    public GeneratedClass(Environment env) {
        super(env);
    }
    
    @Override
    public Class<?> getTarget(Class<?> clazz) {
        return clazz;
    }
    
    @Override
    public NestingKind getNestingKind() {
        return NestingKind.TOP_LEVEL;
    }
    
    @Override
    public String toString() {
        Environment env = getEnvironment();
        StringBuilder b = new StringBuilder();

        // class declaration
        b.append("public class ").append(getName()).append(" {\n");
        
        // environment variables
        for (Variable<?> var : env.getVariables()) {
            String id = var.getIdentifier();
            b.append(TAB);
            b.append("@com.google.inject.Inject ");
            b.append("@javax.annotation.Nullable ");
            b.append("@com.google.inject.name.Named(\"").append(id).append("\") ");
            b.append("public ").append(var.getType()).append(" ").append(id).append(";\n");
        }

        // constructor
        b.append("\n");
        b.append(TAB).append("public ").append(getName()).append("() {}\n");

        List<? extends SourceCode<? extends Member>> children = getChildren();
        if (children.size() > 0) {
            b.append("\n");
            Iterator<? extends SourceCode<? extends Member>> membs = children.iterator();
            while (membs.hasNext()) {
                for (String line : membs.next().toString().split("\n")) {
                    b.append(TAB).append(line).append("\n");
                }

                if (membs.hasNext()) {
                    b.append("\n");
                }
            }
        }

        b.append("}\n");

        return b.toString();
    }
}
