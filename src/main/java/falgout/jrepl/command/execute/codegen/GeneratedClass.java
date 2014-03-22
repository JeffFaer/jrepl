package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        Set<Import> imports = env.getImports();
        Collection<? extends Variable<?>> variables = env.getVariables();
        List<? extends SourceCode<? extends Member>> children = getChildren();
        
        // imports
        if (imports.size() > 0) {
            for (Import i : imports) {
                b.append(i).append("\n");
            }
            b.append("\n");
        }
        
        // class declaration
        b.append("public class ").append(getName()).append(" {\n");
        
        // environment variables
        if (variables.size() > 0) {
            for (Variable<?> var : variables) {
                b.append(TAB).append(SourceCode.from(var));
            }
            b.append("\n");
        }
        
        // constructor
        b.append(TAB).append("public ").append(getName()).append("() {}\n");
        
        // members
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
