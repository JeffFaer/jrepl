package falgout.jrepl.command.execute.codegen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;

public class ConstructorSourceCode extends MethodOrConstructorSourceCode<Constructor<?>> {
    public static class Builder extends
            MethodOrConstructorSourceCode.Builder<Constructor<?>, ConstructorSourceCode, Builder> {
        public Builder() {}
        
        @Override
        protected ConstructorSourceCode build(List<SourceCode<? extends Annotation>> annotations, int modifiers,
                String name, List<SourceCode<? extends Statement>> children, TypeToken<?> returnType,
                List<TypeToken<?>> parameters, List<String> parameterNames, List<TypeToken<? extends Throwable>> thro) {
            return new ConstructorSourceCode(annotations, modifiers, name, children, parameters, parameterNames, thro);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    protected ConstructorSourceCode(List<SourceCode<? extends Annotation>> annotations, int modifiers, String name,
            List<SourceCode<? extends Statement>> children, List<TypeToken<?>> parameters, List<String> parameterNames,
            List<TypeToken<? extends Throwable>> thro) {
        super(annotations, modifiers, name, children, null, parameters, parameterNames, thro);
    }
    
    @Override
    public Constructor<?> getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return clazz.getConstructor(getRawTypes());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ConstructorSourceCode get(MethodDeclaration decl) throws ClassNotFoundException {
        return initializeFrom(builder(), decl);
    }
}
