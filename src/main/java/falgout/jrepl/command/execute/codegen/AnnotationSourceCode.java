package falgout.jrepl.command.execute.codegen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.List;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.Import;
import falgout.jrepl.reflection.GoogleTypes;
import falgout.jrepl.reflection.TypeIdentifier;

public class AnnotationSourceCode extends TypeSourceCode {
    public static class Builder extends TypeSourceCode.Builder<AnnotationSourceCode, Builder> {
        protected static final String ANNOTATION_PREFERRED_NAME = "Annotation";
        
        public Builder() {
            super(ANNOTATION_PREFERRED_NAME);
        }
        
        @Override
        protected AnnotationSourceCode build(int modifiers, String name, List<SourceCode<? extends Member>> children,
                String _package, List<Import> imports, TypeToken<?> superclass, List<TypeToken<?>> superinterfaces) {
            return new AnnotationSourceCode(modifiers, name, children, _package, imports, superinterfaces);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    protected AnnotationSourceCode(int modifiers, String name, List<SourceCode<? extends Member>> children,
            String _package, List<Import> imports, List<TypeToken<?>> superinterfaces) {
        super(modifiers, name, children, _package, imports, GoogleTypes.OBJECT,
                Collections.singletonList(TypeToken.of(Annotation.class)));
    }
    
    @Override
    protected TypeIdentifier getTypeIdentifier() {
        return TypeIdentifier.ANNOTATION;
    }
    
    public static AnnotationSourceCode.Builder builder() {
        return new Builder();
    }
}
