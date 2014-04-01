package falgout.jrepl.reflection;

import java.util.Optional;

public enum TypeIdentifier {
    ANNOTATION(null, null, "@interface"), CLASS("extends", "implements"), ENUM(null, "implements"), INTERFACE(null,
            "extends");
    private final String classExtender;
    private final String interfaceExtender;
    private final String toString;
    
    private TypeIdentifier(String classExtender, String interfaceExtender) {
        this.classExtender = classExtender;
        this.interfaceExtender = interfaceExtender;
        toString = super.toString().toLowerCase();
    }
    
    private TypeIdentifier(String classExtender, String interfaceExtender, String toString) {
        this.classExtender = classExtender;
        this.interfaceExtender = interfaceExtender;
        this.toString = toString;
    }
    
    public Optional<String> getInterfaceExtender() {
        return Optional.ofNullable(interfaceExtender);
    }
    
    public Optional<String> getClassExtender() {
        return Optional.ofNullable(classExtender);
    }
    
    @Override
    public String toString() {
        return toString;
    }
}
