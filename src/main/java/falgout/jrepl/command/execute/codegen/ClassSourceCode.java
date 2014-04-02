package falgout.jrepl.command.execute.codegen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.List;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Environment;
import falgout.jrepl.Import;
import falgout.jrepl.reflection.TypeIdentifier;

public class ClassSourceCode extends TypeSourceCode {
    public static class Builder extends TypeSourceCode.Builder<ClassSourceCode, Builder> {
        protected static final String CLASS_PREFERRED_NAME = "Class";
        
        public Builder() {
            super(CLASS_PREFERRED_NAME);
        }
        
        @Override
        public TypeToken<?> getSuperclass() {
            return super.getSuperclass();
        }
        
        @Override
        public Builder setSuperclass(TypeToken<?> superclass) {
            return super.setSuperclass(superclass);
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
        protected ClassSourceCode build(List<SourceCode<? extends Annotation>> annotations, int modifiers, String name,
                List<SourceCode<? extends Member>> children, String _package, List<Import> imports,
                TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
            return new ClassSourceCode(annotations, modifiers, name, children, _package, imports, superclass,
                    superinterfaces);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    protected ClassSourceCode(List<SourceCode<? extends Annotation>> annotations, int modifiers, String name,
            List<SourceCode<? extends Member>> children, String _package, List<Import> imports,
            TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
        super(annotations, modifiers, name, children, _package, imports, superclass, superinterfaces);
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
        return TypeIdentifier.CLASS;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(Environment env) {
        Builder b = builder();
        b.setPackage(env.getGeneratedCodePackage());
        env.getMembers().forEach(m -> b.addImports(Import.create(m)));
        b.addImports(env.getImports());
        
        return b;
    }
}
