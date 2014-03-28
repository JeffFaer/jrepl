package falgout.jrepl.command.execute.codegen;

import java.util.Objects;

public class DelegateSourceCode<T> implements SourceCode<T> {
    public static class Builder<T> extends SourceCode.Builder<T, DelegateSourceCode<T>, Builder<T>> {
        private Object delegate;
        
        protected Builder() {}
        
        public Object getDelegate() {
            return delegate;
        }
        
        public Builder<T> setDelegate(Object delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            return getBuilder();
        }
        
        @Override
        public Builder<T> initialize(DelegateSourceCode<T> source) {
            setDelegate(source.getDelegate());
            return getBuilder();
        }
        
        @Override
        protected Builder<T> getBuilder() {
            return this;
        }
        
        @Override
        public DelegateSourceCode<T> build() {
            return new DelegateSourceCode<>(delegate);
        }
    }
    
    private final Object delegate;
    
    protected DelegateSourceCode(Object delegate) {
        this.delegate = delegate;
    }
    
    public Object getDelegate() {
        return delegate;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
        if (!(obj instanceof DelegateSourceCode)) {
            return false;
        }
        DelegateSourceCode<?> other = (DelegateSourceCode<?>) obj;
        if (delegate == null) {
            if (other.delegate != null) {
                return false;
            }
        } else if (!delegate.equals(other.delegate)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return delegate.toString();
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}
