package falgout.jrepl.command.execute.codegen;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Statement;

import falgout.jrepl.Variable;
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
}
