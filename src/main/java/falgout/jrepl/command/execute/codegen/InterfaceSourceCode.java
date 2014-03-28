package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Member;
import java.util.List;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Import;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.TypeIdentifier;

public class InterfaceSourceCode extends TypeSourceCode {
    public static class Builder extends TypeSourceCode.Builder<InterfaceSourceCode, Builder> {
        protected static final String INTERFACE_PREFERRED_NAME = "interface";
        
        public Builder() {
            super(INTERFACE_PREFERRED_NAME);
        }
        
        @Override
        protected InterfaceSourceCode build(int modifiers, String name, List<SourceCode<? extends Member>> children,
                String _package, List<Import> imports, TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
            return new InterfaceSourceCode(modifiers, name, children, _package, imports, superinterfaces);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    protected InterfaceSourceCode(int modifiers, String name, List<SourceCode<? extends Member>> children,
            String _package, List<Import> imports, List<TypeToken<?>> superinterfaces) {
        super(modifiers, name, children, _package, imports, GoogleTypes.OBJECT, superinterfaces);
    }
    
    @Override
    protected TypeIdentifier getTypeIdentifier() {
        return TypeIdentifier.INTERFACE;
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
