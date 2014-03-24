package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.LocalVariable;

public class GeneratedClass extends GeneratedSourceCode<Class<?>, Member> {
    public static final String PACKAGE = "jrepl";
    
    public GeneratedClass(Environment env) {
        super(env);
    }
    
    @Override
    public Class<?> getTarget(Class<?> clazz) {
        return clazz;
    }
    
    @Override
    public String toString() {
        String toString = getChildren().stream().map(member -> member.toString()).map(member -> {
            StringBuilder b2 = new StringBuilder();
            for (String line : member.split("\n")) {
                b2.append(TAB).append(line).append("\n");
            }
            return b2.toString();
        }).collect(Collectors.joining("\n", "\n", ""));
        
        Environment env = getEnvironment();
        StringBuilder b = new StringBuilder();
        
        // package
        b.append("package ").append(PACKAGE).append(";\n");
        b.append("\n");
        
        // imports
        List<Collection<Import>> imports = new ArrayList<>();
        imports.add(env.getImports());
        // class / method imports
        List<Import> runtimeImports = env.getMembers()
                .stream()
                .sequential()
                .map(member -> Import.create(member))
                .collect(Collectors.toList());
        if (!runtimeImports.isEmpty()) {
            imports.add(runtimeImports);
        }
        
        if (imports.size() > 0) {
            for (Collection<Import> group : imports) {
                group.stream().map(i -> i.toString() + "\n").forEach(b::append);
                b.append("\n");
            }
        }
        
        // class declaration
        b.append("public class ").append(getName()).append(" {\n");
        
        // environment variables
        Collection<? extends LocalVariable<?>> variables = env.getVariables();
        if (variables.size() > 0) {
            for (LocalVariable<?> var : variables) {
                if (toString.contains(var.getName())) {
                    b.append(TAB).append(var.asField());
                }
            }
            b.append("\n");
        }
        
        // constructor
        b.append(TAB).append("public ").append(getName()).append("() {}\n");
        
        // members
        b.append(toString);
        
        b.append("}\n");
        
        return b.toString();
    }
}
