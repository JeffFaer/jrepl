package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Field;
import java.util.Objects;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.reflection.GoogleTypes;

public class FieldSourceCode extends NamedSourceCode<Field> {
    public static class Builder extends NamedSourceCode.Builder<Field, FieldSourceCode, Builder> {
        protected static final String FIELD_PREFERRED_NAME = "field";
        private TypeToken<?> type = GoogleTypes.OBJECT;
        
        public Builder() {
            super(FIELD_PREFERRED_NAME);
        }
        
        public TypeToken<?> getType() {
            return type;
        }
        
        public Builder setType(TypeToken<?> type) {
            this.type = Objects.requireNonNull(type);
            return getBuilder();
        }
        
        @Override
        protected FieldSourceCode build(int modifiers, String name) {
            return new FieldSourceCode(modifiers, name, type);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    private final TypeToken<?> type;
    
    protected FieldSourceCode(int modifiers, String name, TypeToken<?> type) {
        super(modifiers, name);
        this.type = type;
    }
    
    public TypeToken<?> getType() {
        return type;
    }
    
    @Override
    public Field getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return clazz.getField(getName());
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getModifierString())
                .append(GoogleTypes.toCanonicalString(type))
                .append(" ")
                .append(getName())
                .append(";");
        return b.toString();
    }
}
