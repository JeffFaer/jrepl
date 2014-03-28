package falgout.jrepl.reflection;

import java.util.Optional;

public enum TypeIdentifier {
    CLASS("extends", "implements"), ENUM(null, "implements"), INTERFACE(null, "extends");
    private final String classExtender;
    private final String interfaceExtender;
    
    private TypeIdentifier(String classExtender, String interfaceExtender) {
        this.classExtender = classExtender;
        this.interfaceExtender = interfaceExtender;
    }
    
    public String getInterfaceExtender() {
        return interfaceExtender;
    }
    
    public Optional<String> getClassExtender() {
        return Optional.ofNullable(classExtender);
    }
    
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
