package falgout.jrepl.command.execute.codegen;

public class DelegateSourceCode<T> extends SourceCode<T> {
    private final Object delegate;
    
    public DelegateSourceCode(Object delegate) {
        this.delegate = delegate;
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
}
