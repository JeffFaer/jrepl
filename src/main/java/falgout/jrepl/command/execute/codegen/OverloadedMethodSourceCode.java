package falgout.jrepl.command.execute.codegen;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class OverloadedMethodSourceCode extends NestedSourceCode<Method, Method> {
    public static class Builder extends NestedSourceCode.Builder<Method, Method, OverloadedMethodSourceCode, Builder> {
        private NamedSourceCode<? extends Method> primary;
        
        public Builder(NamedSourceCode<? extends Method> primary,
                Collection<? extends SourceCode<? extends Method>> overloads) {
            setPrimary(primary);
            addChildren(overloads);
        }
        
        @Override
        public Builder initialize(OverloadedMethodSourceCode source) {
            return super.initialize(source).setPrimary(source.getPrimary());
        }
        
        public NamedSourceCode<? extends Method> getPrimary() {
            return primary;
        }
        
        public Builder setPrimary(NamedSourceCode<? extends Method> primary) {
            this.primary = Objects.requireNonNull(primary);
            setModifiers(primary.getModifiers()).setName(primary.getName());
            
            return getBuilder();
        }
        
        @Override
        protected OverloadedMethodSourceCode build(int modifiers, String name,
                List<SourceCode<? extends Method>> children) {
            return new OverloadedMethodSourceCode(modifiers, name, children, primary);
        }
        
        @Override
        protected Builder getBuilder() {
            return this;
        }
    }
    
    private final NamedSourceCode<? extends Method> primary;
    
    protected OverloadedMethodSourceCode(int modifiers, String name, List<SourceCode<? extends Method>> children,
            NamedSourceCode<? extends Method> primary) {
        super(modifiers, name, children);
        this.primary = primary;
    }
    
    public NamedSourceCode<? extends Method> getPrimary() {
        return primary;
    }
    
    @Override
    public Method getTarget(Class<?> clazz) throws ReflectiveOperationException {
        return primary.getTarget(clazz);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((primary == null) ? 0 : primary.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OverloadedMethodSourceCode)) {
            return false;
        }
        OverloadedMethodSourceCode other = (OverloadedMethodSourceCode) obj;
        if (primary == null) {
            if (other.primary != null) {
                return false;
            }
        } else if (!primary.equals(other.primary)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder allOverloads = new StringBuilder();
        allOverloads.append(primary);
        allOverloads.append(getChildren().stream().map(SourceCode::toString).collect(joining("\n", "\n", "")));
        return allOverloads.toString();
    }
    
    @SafeVarargs
    public static Builder builder(NamedSourceCode<? extends Method> primary, SourceCode<? extends Method>... overloads) {
        return builder(primary, Arrays.asList(overloads));
    }
    
    public static Builder builder(NamedSourceCode<? extends Method> primary,
            Collection<? extends SourceCode<? extends Method>> overloads) {
        return new Builder(primary, overloads);
    }
}
