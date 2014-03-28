package falgout.jrepl.command.execute.codegen;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NamedSourceCode<T> implements SourceCode<T> {
    public static abstract class Builder<T, S extends NamedSourceCode<T>, B extends Builder<T, S, B>> extends
            SourceCode.Builder<T, S, B> {
        private static final String GENERATED_NAME_TEMPLATE = "$%s%d";
        private static final AtomicInteger ID = new AtomicInteger(0);
        protected static final int DEFAULT_MODIFIERS = Modifier.PUBLIC;
        protected static final String DEFAULT_NAME = "Generated";
        
        private final String preferredName;
        private int modifiers = DEFAULT_MODIFIERS;
        private String name;
        
        protected Builder() {
            this(DEFAULT_NAME);
        }
        
        protected Builder(String preferredName) {
            this.preferredName = preferredName;
        }
        
        public int getModifiers() {
            return modifiers;
        }
        
        public B addModifier(int modifier) {
            modifiers |= modifier;
            return getBuilder();
        }
        
        public B setModifiers(int modifiers) {
            this.modifiers = modifiers;
            return getBuilder();
        }
        
        public String getName() {
            return name;
        }
        
        public B setName(String name) {
            this.name = name;
            return getBuilder();
        }
        
        @Override
        public S build() {
            return build(modifiers,
                    name == null ? String.format(GENERATED_NAME_TEMPLATE, preferredName, ID.incrementAndGet()) : name);
        }
        
        protected abstract S build(int modifiers, String name);
    }
    
    private final int modifiers;
    private final String name;
    
    protected NamedSourceCode(int modifiers, String name) {
        this.modifiers = modifiers;
        this.name = name;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    protected String getModifierString() {
        return getModifierString(modifiers);
    }
    
    protected String getModifierString(int modifiers) {
        String mods = Modifier.toString(modifiers);
        return mods.isEmpty() ? mods : mods + " ";
    }
    
    public String getName() {
        return name;
    }
    
    public abstract T getTarget(Class<?> clazz) throws ReflectiveOperationException;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NamedSourceCode)) {
            return false;
        }
        NamedSourceCode<?> other = (NamedSourceCode<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
