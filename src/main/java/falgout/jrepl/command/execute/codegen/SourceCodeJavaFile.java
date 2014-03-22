package falgout.jrepl.command.execute.codegen;

import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.SimpleJavaFileObject;

public class SourceCodeJavaFile extends SimpleJavaFileObject {
    private final SourceCode<? extends Class<?>> code;
    
    public SourceCodeJavaFile(SourceCode<? extends Class<?>> code) {
        super(URI.create("string:///" + code.getName().replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }
    
    @Override
    public NestingKind getNestingKind() {
        return NestingKind.TOP_LEVEL;
    }
    
    @Override
    public Modifier getAccessLevel() {
        return Modifier.PUBLIC;
    }
    
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code.toString();
    }
}
