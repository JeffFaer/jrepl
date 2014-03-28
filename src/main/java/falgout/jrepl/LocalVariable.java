package falgout.jrepl;

import java.lang.reflect.Modifier;

import com.google.common.reflect.TypeToken;

import falgout.jrepl.command.execute.codegen.FieldSourceCode;

public class LocalVariable<T> extends AbstractVariable<T> {
    private final boolean _final;
    private final TypeToken<T> type;
    private final String name;
    private T value;
    private boolean initialized;
    
    public LocalVariable(TypeToken<T> type, String name) {
        this(false, type, name);
    }
    
    public LocalVariable(boolean _final, TypeToken<T> type, String name) {
        this(_final, type, name, null);
        initialized = false;
    }
    
    public LocalVariable(TypeToken<T> type, String name, T value) {
        this(false, type, name, value);
    }
    
    public LocalVariable(boolean _final, TypeToken<T> type, String name, T value) {
        this._final = _final;
        this.type = type;
        this.name = name;
        set(value);
    }
    
    @Override
    public boolean isFinal() {
        return _final;
    }
    
    @Override
    public TypeToken<T> getType() {
        return type;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public T get() {
        return value;
    }
    
    @Override
    protected void doSet(T value) {
        this.value = value;
        initialized = true;
    }
    
    @Override
    protected int getModifiers() {
        return 0;
    }
    
    public FieldSourceCode asField() {
        FieldSourceCode.Builder b = new FieldSourceCode.Builder();
        b.addModifier(Modifier.STATIC).setType(type).setName(name);
        if (_final) {
            b.addModifier(Modifier.FINAL);
        }
        return b.build();
    }
}
