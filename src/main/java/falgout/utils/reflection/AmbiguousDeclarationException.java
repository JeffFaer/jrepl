package falgout.utils.reflection;

public class AmbiguousDeclarationException extends ReflectiveOperationException {
    private static final long serialVersionUID = 2639606828434023342L;
    
    public AmbiguousDeclarationException() {
        super();
    }
    
    public AmbiguousDeclarationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AmbiguousDeclarationException(String message) {
        super(message);
    }
    
    public AmbiguousDeclarationException(Throwable cause) {
        super(cause);
    }
}
