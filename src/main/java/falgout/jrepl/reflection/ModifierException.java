package falgout.jrepl.reflection;

public class ModifierException extends ReflectiveOperationException {
    private static final long serialVersionUID = 3265915788737670559L;
    
    public ModifierException() {
        super();
    }
    
    public ModifierException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ModifierException(String message) {
        super(message);
    }
    
    public ModifierException(Throwable cause) {
        super(cause);
    }
}
