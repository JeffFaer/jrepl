package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
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
    public String toString() {
        Environment env = getEnvironment();
        StringBuilder b = new StringBuilder();
        
        // imports
        Set<Import> imports = env.getImports();
        if (imports.size() > 0) {
            for (Import i : imports) {
                b.append(i).append("\n");
            }
            b.append("\n");
        }
        
        // class declaration
        b.append("public class ").append(getName()).append(" {\n");
        
        // environment variables
        Collection<? extends Variable<?>> variables = env.getVariables();
        if (variables.size() > 0) {
            for (Variable<?> var : variables) {
                b.append(TAB).append(SourceCode.from(var));
            }
            b.append("\n");
        }
        
        // constructor
        b.append(TAB).append("public ").append(getName()).append("() {}\n");
        
        // members
        Set<SourceCode<? extends Member>> members = new LinkedHashSet<>();
        members.addAll(getChildren());
        members.addAll(env.getMembers());
        
        String toString = members.stream().map(member -> member.toString()).map(member -> {
            StringBuilder b2 = new StringBuilder();
            for (String line : member.split("\n")) {
                b2.append(TAB).append(line).append("\n");
            }
            return b2.toString();
        }).collect(Collectors.joining("\n", "\n", ""));
        b.append(toString);
        
        b.append("}\n");
        
        return b.toString();
    }
}
