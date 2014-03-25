package falgout.jrepl.command.execute.codegen;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.LocalVariable;
import falgout.jrepl.reflection.JDTTypes;
import falgout.jrepl.reflection.NestedClass;

public abstract class SourceCode<T> {
    private final String name;
    
    protected SourceCode(String name) {
        this.name = name;
    }
    
    public abstract T getTarget(Class<?> clazz) throws ReflectiveOperationException;
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SourceCode)) {
            return false;
        }
        SourceCode<?> other = (SourceCode<?>) obj;
        if (toString() == null) {
            if (other.toString() != null) {
                return false;
            }
        } else if (!toString().equals(other.toString())) {
            return false;
        }
        return true;
    }
    
    @Override
    public abstract String toString();
    
    public static SourceCode<Statement> from(Statement statement) {
        return new SourceCode<Statement>(null) {
            @Override
            public Statement getTarget(Class<?> clazz) throws ReflectiveOperationException {
                return statement;
            }
            
            @Override
            public String toString() {
                return statement.toString();
            }
        };
    }
    
    public static SourceCode<NestedClass<?>> from(AbstractTypeDeclaration decl) {
        List<Modifier> modifiers = decl.modifiers();
        if (!JDTTypes.isStatic(modifiers)) {
            AST ast = decl.getAST();
            Modifier mod = ast.newModifier(ModifierKeyword.STATIC_KEYWORD);
            modifiers.add(mod);
        }
        
        String name = decl.getName().toString();
        return new SourceCode<NestedClass<?>>(name) {
            @Override
            public NestedClass<?> getTarget(Class<?> clazz) {
                for (Class<?> nested : clazz.getDeclaredClasses()) {
                    if (nested.getSimpleName().equals(name)) {
                        return new NestedClass<>(nested);
                    }
                }
                
                throw new AssertionError();
            }
            
            @Override
            public String toString() {
                return decl.toString();
            }
        };
    }
    
    public static GeneratedSourceCode<Statement, Void> createInitializer(Map<LocalVariable<?>, Expression> initialize) {
        return new GeneratedSourceCode<Statement, Void>(null) {
            @Override
            public Statement getTarget(Class<?> clazz) throws ReflectiveOperationException {
                return null;
            }
            
            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append("try {\n");
                for (Entry<LocalVariable<?>, Expression> e : initialize.entrySet()) {
                    String name = e.getKey().getName();
                    Expression init = e.getValue();
                    b.append(TAB).append(name).append(" = ").append(init).append(";\n");
                }
                b.append("} catch (Throwable $e) {\n");
                b.append(TAB).append("throw new ExceptionInInitializerError($e);\n");
                b.append("}");
                return b.toString();
            }
        };
    }
}
