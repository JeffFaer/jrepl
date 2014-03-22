package falgout.jrepl.command.execute;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Variable;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.Invokable;
import falgout.jrepl.reflection.JDTTypes;

public class LocalVariableDeclarer implements Executor<VariableDeclarationStatement, Set<Variable<?>>> {
    public static final Executor<VariableDeclarationStatement, Set<Variable<?>>> INSTANCE = new LocalVariableDeclarer();
    public static final Executor<Statement, Set<Variable<?>>> FILTER = Executor.filter(INSTANCE,
            s -> (s instanceof VariableDeclarationStatement) ? (VariableDeclarationStatement) s : null);
    public static final Executor<Iterable<? extends Statement>, Set<Variable<?>>> PARSE = Executor.flatProcess(FILTER);
    
    @Override
    public Optional<Set<Variable<?>>> execute(Environment env, VariableDeclarationStatement input) throws IOException {
        try {
            TypeToken<?> baseType = JDTTypes.getType(input.getType());
            
            Set<String> names = new LinkedHashSet<>();
            Set<Variable<?>> variables = new LinkedHashSet<>();
            
            boolean _final = JDTTypes.isFinal(input.modifiers());
            
            for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) input.fragments()) {
                String name = frag.getName().getIdentifier();
                if (names.contains(name) || env.containsVariable(name)) {
                    env.getError().printf("%s already exists.\n", name);
                    return Optional.empty();
                }
                
                int extraDims = frag.getExtraDimensions();
                TypeToken<?> variableType = GoogleTypes.addArrays(baseType, extraDims);
                Variable<?> var = new Variable<>(_final, variableType, name);
                
                Expression init = frag.getInitializer();
                if (init != null) {
                    try {
                        Optional<? extends Invokable.Method> opt = ExpressionExecutor.INSTANCE.execute(env, init);
                        if (opt.isPresent()) {
                            Object value = opt.get().invoke();
                            var.set(variableType, value);
                        } else {
                            return Optional.empty();
                        }
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
                
                variables.add(var);
                names.add(name);
            }
            
            for (Variable<?> var : variables) {
                env.getOutput().println(var);
                env.addVariable(var);
            }
            
            return Optional.of(variables);
        } catch (ClassNotFoundException e) {
            env.getError().println(e.getMessage());
        }

        return Optional.empty();
    }
}
