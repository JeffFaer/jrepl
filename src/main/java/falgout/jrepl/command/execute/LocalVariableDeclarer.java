package falgout.jrepl.command.execute;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.LocalVariable;
import falgout.jrepl.command.execute.codegen.GeneratedMethod;
import falgout.jrepl.command.execute.codegen.GeneratedMethodExecutor;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;

public enum LocalVariableDeclarer implements Executor<VariableDeclarationStatement, List<LocalVariable<?>>> {
    INSTANCE;
    public static final Executor<Statement, Optional<? extends List<LocalVariable<?>>>> FILTER = Executor.filter(
            INSTANCE, s -> (s instanceof VariableDeclarationStatement) ? (VariableDeclarationStatement) s : null);
    public static final Executor<Iterable<? extends Statement>, Optional<? extends List<LocalVariable<?>>>> PARSE = Executor.flatProcess(FILTER);
    
    @Override
    public List<LocalVariable<?>> execute(Environment env, VariableDeclarationStatement input)
            throws ExecutionException {
        try {
            TypeToken<?> baseType = JDTTypes.getType(input.getType());
            if (baseType.equals(GoogleTypes.VOID)) {
                throw new ExecutionException(new IllegalArgumentException("Cannot have a void variable."));
            }
            
            Set<String> names = new LinkedHashSet<>();
            List<LocalVariable<?>> variables = new ArrayList<>();
            
            boolean _final = JDTTypes.isFinal(input.modifiers());
            
            for (VariableDeclarationFragment frag : (List<VariableDeclarationFragment>) input.fragments()) {
                String name = frag.getName().getIdentifier();
                if (names.contains(name) || env.containsVariable(name)) {
                    String message = String.format("%s already exists.", name);
                    throw new ExecutionException(new IllegalArgumentException(message));
                }
                
                int extraDims = frag.getExtraDimensions();
                TypeToken<?> variableType = GoogleTypes.addArrays(baseType, extraDims);
                LocalVariable<?> var = new LocalVariable<>(_final, variableType, name);
                
                Expression init = frag.getInitializer();
                if (init != null) {
                    GeneratedMethod method = new GeneratedMethod(env);
                    method.addChild(SourceCode.from(input));
                    method.addChild(SourceCode.createReturnStatement(var));
                    
                    Object value = GeneratedMethodExecutor.INSTANCE.execute(method);
                    var.set(variableType, value);
                }
                
                variables.add(var);
                names.add(name);
            }
            
            for (LocalVariable<?> var : variables) {
                if (!env.addVariable(var)) {
                    throw new Error("How the heck did this happen?!");
                }
            }
            
            return variables;
        } catch (ClassNotFoundException e) {
            throw new ExecutionException(e);
        }
    }
}
