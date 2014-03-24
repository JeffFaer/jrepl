package falgout.jrepl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.reflect.TypeToken;

public class FieldVariable<T> extends AbstractVariable<T> {
    private final TypeToken<T> type;
    private final Field field;
    private final Object receiver;
    
    public FieldVariable(TypeToken<T> type, Field field) {
        this(type, field, ensureStatic(field));
    }
    
    private static Object ensureStatic(Field field) {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException(field + " must be static.");
        }
        
        return null;
    }
    
    public FieldVariable(TypeToken<T> type, Field field, Object receiver) {
        if (!field.isAccessible()) {
            throw new IllegalArgumentException(field + " must be accessible.");
        }
        this.type = type;
        this.field = field;
        this.receiver = receiver;
    }
    
    public Field getField() {
        return field;
    }
    
    @Override
    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }
    
    @Override
    public TypeToken<T> getType() {
        return type;
    }
    
    @Override
    public String getName() {
        return field.getName();
    }
    
    @Override
    public boolean isInitialized() {
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        try {
            return (T) field.get(receiver);
        } catch (IllegalAccessException e) {
            throw new Error("We checked accessibility in the constructor", e);
        }
    }
    
    @Override
    protected void doSet(T value) {
        try {
            field.set(receiver, value);
        } catch (IllegalAccessException e) {
            throw new Error("We checked accessibility in the constructor", e);
        }
    }
    
    @Override
    protected int getModifiers() {
        return field.getModifiers();
    }
}
