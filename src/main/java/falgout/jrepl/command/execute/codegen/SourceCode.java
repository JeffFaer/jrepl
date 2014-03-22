package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.base.Defaults;

import falgout.jrepl.Variable;
import falgout.jrepl.reflection.NestedClass;

public abstract class SourceCode<T> {
    private final String name;
    
    protected SourceCode(String name) {
        this.name = name;
    }
    
    public abstract T getTarget(Class<?> clazz);
    
    public String getName() {
        return name;
    }
    
    @Override
    public abstract String toString();
    
    public static SourceCode<WrappedStatement> createReturnStatement(Variable<?> variable) {
        return new WrappedStatementSourceCode(variable);
    }
    
    public static SourceCode<WrappedStatement> createReturnStatement(Expression expression) {
        return new WrappedStatementSourceCode(expression);
    }
    
    public static SourceCode<WrappedStatement> from(Statement statement) {
        return new WrappedStatementSourceCode(statement);
    }
    
    public static SourceCode<NestedClass<?>> from(AbstractTypeDeclaration decl) {
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
    
    public static SourceCode<Field> from(Variable<?> variable) {
        return new SourceCode<Field>(variable.getIdentifier()) {
            @Override
            public Field getTarget(Class<?> clazz) {
                try {
                    return clazz.getField(getName());
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }
            
            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append("@com.google.inject.Inject ");
                b.append("@javax.annotation.Nullable ");
                b.append("@com.google.inject.name.Named(\"").append(getName()).append("\")");
                b.append(" public ");
                if (variable.isFinal()) {
                    b.append("final ");
                }
                b.append(variable.getType()).append(" ").append(getName());
                if (variable.isFinal()) {
                    Object val = Defaults.defaultValue(variable.getType().getRawType());
                    b.append(" = ").append(Variable.toString(val));
                }
                b.append(";\n");
                return b.toString();
            }
        };
    }
}
