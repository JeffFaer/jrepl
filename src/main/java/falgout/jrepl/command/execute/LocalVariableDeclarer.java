package falgout.jrepl.command.execute;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
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
import falgout.jrepl.command.execute.codegen.ClassSourceCode;
import falgout.jrepl.command.execute.codegen.CodeCompiler;
import falgout.jrepl.command.execute.codegen.DelegateSourceCode;
import falgout.jrepl.command.execute.codegen.NamedSourceCode;
import falgout.jrepl.command.execute.codegen.NestedSourceCode;
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
        List<NamedSourceCode<Field>> source = new ArrayList<>();
        variables.forEach(var -> source.add(var.asField()));
        
        ClassSourceCode.Builder b = ClassSourceCode.builder(env);
        b.addChildren(source);
        
        if (initialize.size() > 0) {
            NestedSourceCode<Member, Member> block = new NestedSourceCode<Member, Member>(Collections.EMPTY_LIST,
                    Modifier.STATIC, null, Collections.singletonList(createInitializer(initialize))) {
                @Override
                public Member getTarget(Class<?> clazz) throws ReflectiveOperationException {
                    return null;
                }
                
                @Override
                public String toString() {
                    StringBuilder b = new StringBuilder();
                    b.append("static {\n");
                    b.append("try {\n");
                    b.append(createChildrenString("\n"));
                    b.append("} catch (Throwable $e) {\n");
                    b.append(TAB).append("throw new ExceptionInInitializerError($e);\n");
                    b.append("}\n");
                    b.append("}");
                    return b.toString();
                }
            };
            b.addChildren(block);
        }
        
        Class<?> c = classCompiler.execute(env, b.build());
        
        for (int i = 0; i < source.size(); i++) {
            NamedSourceCode<Field> s = source.get(i);
            
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
    
    private static SourceCode<Member> createInitializer(Map<LocalVariable<?>, Expression> initialize) {
        return new DelegateSourceCode<Member>(initialize) {
            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                initialize.forEach((var, exp) -> b.append(var.getName()).append(" = ").append(exp).append(";\n"));
                return b.toString();
            }
        };
    }
}
