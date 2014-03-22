package falgout.jrepl.command;

public class ParsingException extends IllegalArgumentException {
    private static final long serialVersionUID = 3487001505260496057L;
    
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ParsingException(String s) {
        super(s);
    }
    
    public ParsingException(Throwable cause) {
        super(cause);
    }
}
