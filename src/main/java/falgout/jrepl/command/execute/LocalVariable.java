package falgout.jrepl.command.execute;

import static com.google.common.reflect.Types2.addArraysToType;
import static falgout.jrepl.reflection.Types.getType;
import static falgout.jrepl.reflection.Types.isFinal;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.command.parse.JavaParser.BlockStatementsContext;
import falgout.jrepl.command.parse.JavaParser.LocalVariableDeclarationContext;
import falgout.jrepl.command.parse.JavaParser.VariableDeclaratorContext;
import falgout.jrepl.command.parse.JavaParser.VariableDeclaratorRestContext;
import falgout.jrepl.command.parse.JavaParser.VariableInitializerContext;
import falgout.jrepl.reflection.ModifierException;

public class LocalVariable
        extends
        PartialRuleExecutor<BlockStatementsContext, LocalVariableDeclarationContext, Set<Variable<?>>, Set<Variable<?>>> {
    public LocalVariable() {
        super(LocalVariableDeclarationContext.class, 3);
    }
    
    @Override
    public Set<Variable<?>> doExecute(Environment env, LocalVariableDeclarationContext input) throws IOException {
        try {
            boolean _final = isFinal(input.variableModifier());
            TypeToken<?> baseType = getType(env.getImportClassLoader(), input.type());
            
            Set<String> names = new LinkedHashSet<>();
            Set<Variable<?>> variables = new LinkedHashSet<>();
            for (VariableDeclaratorContext varCtx : input.variableDeclarators().variableDeclarator()) {
                String name = varCtx.Identifier().getText();
                
                if (env.containsVariable(name) || names.contains(name)) {
                    env.getError().printf("%s already exists.\n", name);
                    return Collections.EMPTY_SET;
                }
                names.add(name);
                
                VariableDeclaratorRestContext rest = varCtx.variableDeclaratorRest();
                int extraArrays = rest.L_BRACKET().size();
                TypeToken<?> varType = addArraysToType(baseType, extraArrays);
                
                Variable<?> var = new Variable<>(_final, varType, name);
                VariableInitializerContext init = rest.variableInitializer();
                if (init != null) {
                    Object value = evaluate(varType, init);
                    var.set(varType, value);
                }
                
                variables.add(var);
            }
            
            if (!env.addVariables(variables)) {
                throw new Error("Something went horribly wrong");
            }
            
            return variables;
        } catch (ModifierException | ClassNotFoundException e) {
            env.getError().println(e.getMessage());
            return Collections.EMPTY_SET;
        }
    }
    
    private Object evaluate(TypeToken<?> type, VariableInitializerContext init) {
        Object ret;
        if (init.arrayInitializer() != null) {
            List<VariableInitializerContext> values = init.arrayInitializer().variableInitializer();
            TypeToken<?> componentType = type.getComponentType();
            ret = Array.newInstance(componentType.getRawType(), values.size());
            for (int i = 0; i < values.size(); i++) {
                Array.set(ret, i, evaluate(componentType, values.get(i)));
            }
        } else {
            init.expression();
            ret = null;
        }
        
        return ret;
    }
    
    @Override
    protected Set<Variable<?>> fold(List<Set<Variable<?>>> results) {
        if (results.isEmpty()) {
            return Collections.EMPTY_SET;
        } else {
            Iterator<Set<Variable<?>>> i = results.iterator();
            Set<Variable<?>> fold = i.next();
            while (i.hasNext()) {
                fold.addAll(i.next());
            }
            
            return fold;
        }
    }
}
