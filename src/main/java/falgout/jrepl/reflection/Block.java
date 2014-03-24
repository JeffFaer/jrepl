package falgout.jrepl.reflection;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class Block implements Member {
    private final Class<?> declaringClass;
    private final boolean _static;
    
    public Block(Class<?> declaringClass, boolean _static) {
        this.declaringClass = declaringClass;
        this._static = _static;
    }
    
    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }
    
    @Override
    public String getName() {
        return null;
    }
    
    @Override
    public int getModifiers() {
        return _static ? Modifier.STATIC : 0;
    }
    
    @Override
    public boolean isSynthetic() {
        return false;
    }
}
