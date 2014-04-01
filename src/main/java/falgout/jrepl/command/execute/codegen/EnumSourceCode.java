package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.List;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Import;
import falgout.jrepl.reflection.TypeIdentifier;

public class EnumSourceCode extends TypeSourceCode {
    public static class Builder extends TypeSourceCode.Builder<EnumSourceCode, Builder> {
        protected static final String ENUM_PREFFERED_NAME = "Enum";
        
        protected Builder() {
            super(ENUM_PREFFERED_NAME);
        }
        
        @Override
        protected EnumSourceCode build(int modifiers, String name, List<SourceCode<? extends Member>> children,
                String _package, List<Import> imports, TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
            return new EnumSourceCode(modifiers, name, children, _package, imports, superinterfaces);
        }
        
        @Override
        public List<TypeToken<?>> getSuperinterfaces() {
            return super.getSuperinterfaces();
        }
        
        @Override
        public Builder addSuperinterfaces(TypeToken<?>... superinterfaces) {
            return super.addSuperinterfaces(superinterfaces);
        }
        
        @Override
        public Builder setSuperinterfaces(List<TypeToken<?>> superinterfaces) {
            return super.setSuperinterfaces(superinterfaces);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    protected EnumSourceCode(int modifiers, String name, List<SourceCode<? extends Member>> children, String _package,
            List<Import> imports, List<TypeToken<?>> superinterfaces) {
        super(modifiers, name, children, _package, imports, TypeToken.of(Enum.class), superinterfaces);
    }
    
    @Override
    public TypeToken<?> getSuperclass() {
        return super.getSuperclass();
    }
    
    @Override
    public List<? extends TypeToken<?>> getSuperinterfaces() {
        return super.getSuperinterfaces();
    }
    
    @Override
    protected TypeIdentifier getTypeIdentifier() {
        return TypeIdentifier.ENUM;
    }
    
    public static EnumSourceCode.Builder builder() {
        return new Builder();
    }
}
