package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.JDTTypes;

public class MethodSourceCode extends MethodOrConstructorSourceCode<Method> {
    public static class Builder extends MethodOrConstructorSourceCode.Builder<Method, MethodSourceCode, Builder> {
        protected static final TypeToken<?> DEFAULT_RETURN_TYPE = GoogleTypes.VOID;
        protected static final String METHOD_PREFERRED_NAME = "method";
        
        protected Builder() {
            this(DEFAULT_RETURN_TYPE);
        }
        
        protected Builder(TypeToken<?> returnType) {
            super(METHOD_PREFERRED_NAME);
            setReturnType(returnType);
        }
        
        @Override
        public TypeToken<?> getReturnType() {
            return super.getReturnType();
        }
        
        @Override
        public Builder setReturnType(TypeToken<?> returnType) {
            return super.setReturnType(returnType);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
        
        @Override
        protected MethodSourceCode build(int modifiers, String name, List<SourceCode<? extends Statement>> children,
                TypeToken<?> returnType, List<TypeToken<?>> parameters, List<String> parameterNames,
                List<TypeToken<? extends Throwable>> thro) {
            return new MethodSourceCode(modifiers, name, children, returnType, parameters, parameterNames, thro);
        }
    }
    
    protected MethodSourceCode(int modifiers, String name, List<SourceCode<? extends Statement>> children,
            TypeToken<?> returnType, List<TypeToken<?>> parameters, List<String> parameterNames,
            List<TypeToken<? extends Throwable>> thro) {
        super(modifiers, name, children, returnType, parameters, parameterNames, thro);
    }
    
    @Override
    public TypeToken<?> getReturnType() {
        return super.getReturnType();
    }
    
    @Override
    public Method getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return clazz.getMethod(getName(), getRawTypes());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static MethodSourceCode get(MethodDeclaration decl) throws ClassNotFoundException {
        MethodSourceCode.Builder b = builder();
        initializeFrom(b, decl).setReturnType(JDTTypes.getType(decl.getReturnType2()));
        return b.build();
    }
}
