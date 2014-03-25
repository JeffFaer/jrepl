package falgout.jrepl.command.execute;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import falgout.jrepl.Environment;
import falgout.jrepl.FieldVariable;
import falgout.jrepl.LocalVariable;
import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.command.execute.codegen.GeneratedBlock;
import falgout.jrepl.command.execute.codegen.GeneratedClass;
import falgout.jrepl.command.execute.codegen.SourceCode;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;

public class LocalVariableDeclarer extends AbstractExecutor<VariableDeclarationStatement, List<LocalVariable<?>>> {
    private final CodeCompiler<Class<?>> classCompiler;
    
    @Inject
    public LocalVariableDeclarer(CodeCompiler<Class<?>> classCompiler) {
        this.classCompiler = classCompiler;
    }
    
    @Override
    public List<LocalVariable<?>> execute(Environment env, VariableDeclarationStatement input)
            throws ExecutionException {
        TypeToken<?> baseType;
        try {
            baseType = JDTTypes.getType(input.getType());
        } catch (ClassNotFoundException e) {
            throw new ExecutionException(e);
        }
        if (baseType.equals(GoogleTypes.VOID)) {
            throw new ExecutionException(new IllegalArgumentException("Cannot have a void variable."));
        }
        
        boolean _final = JDTTypes.isFinal(input.modifiers());
        
        List<LocalVariable<?>> variables = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        Map<LocalVariable<?>, Expression> initialize = new LinkedHashMap<>();
        
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
                initialize.put(var, init);
            } else if (_final) {
                throw new ExecutionException(new IllegalArgumentException(
                        "Uninitialized final variables are not allowed: " + var));
            }
            
            variables.add(var);
            names.add(name);
        }
        
        createFieldVariables(env, variables, initialize);
        
        return variables;
    }
    
    private void createFieldVariables(Environment env, List<LocalVariable<?>> variables,
            Map<LocalVariable<?>, Expression> initialize) throws ExecutionException {
        List<SourceCode<Field>> source = new ArrayList<>();
        GeneratedClass clazz = new GeneratedClass(env);
        for (LocalVariable<?> var : variables) {
            SourceCode<Field> code = var.asField();
            source.add(code);
            clazz.addChild(code);
        }
        
        if (initialize.size() > 0) {
            GeneratedBlock block = new GeneratedBlock(env, true);
            block.addChild(SourceCode.createInitializer(initialize));
            clazz.addChild(block);
        }
        
        Class<?> c = classCompiler.execute(clazz);
        
        for (int i = 0; i < source.size(); i++) {
            SourceCode<Field> s = source.get(i);
            
            try {
                Field f = s.getTarget(c);
                f.setAccessible(true);
                
                LocalVariable<?> var = variables.get(i);
                FieldVariable<?> fv = new FieldVariable<>(var.getType(), f);
                var.set(fv);
                env.addVariable(fv);
            } catch (ReflectiveOperationException | ExceptionInInitializerError e) {
                throw new ExecutionException(e);
            }
        }
    }
}
