package falgout.jrepl.command.execute.codegen;

import java.io.IOException;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.SimpleJavaFileObject;

public abstract class SourceCode<T> extends SimpleJavaFileObject {
    private final String name;
    
    protected SourceCode(String name) {
        super(URI.create("string:///" + (name == null ? "" : name.replace('.', '/')) + Kind.SOURCE.extension),
                Kind.SOURCE);
        this.name = name;
    }
    
    public abstract T getTarget(Class<?> clazz);
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public abstract NestingKind getNestingKind();
    
    @Override
    public abstract Modifier getAccessLevel();
    
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return toString();
    }
    
    @Override
    public abstract String toString();
}
